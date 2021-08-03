package com.android.inputmethod.pinyin.custom;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;

import com.android.inputmethod.pinyin.PinyinIME;
import com.android.inputmethod.pinyin.R;

import java.util.Timer;
import java.util.TimerTask;

public class PinYInIMEWindow {
    private static final String TAG = "PinYInIMEWindow";
    private Activity activity;//显示自定义输入法的activity
    private InputConnection inputConnection;//输入链接、一切输入用它作为桥梁
    PinyinIME pinyinIME;//抽离出InputMethodService的输入法
    View inputView;//键盘view
    View candidateView;//候选词view
    PopupWindow inputWindow;//键盘容器窗口
    PopupWindow candidateWindow;//候选词容器窗口
    private final int screenHeight;
    private DragPopupWindow dragPopupWindow;
    private boolean isShowing = false;

    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final Timer delayTimer = new Timer();

    public PopupWindow getInputWindow() {
        return inputWindow;
    }

    /**
     * 构造方法
     *
     * @param activity        输入法依附于此活动
     * @param inputConnection 输入连接、可以是EditText或者是WebView的连接等等
     */
    public PinYInIMEWindow(Activity activity, InputConnection inputConnection) {
        this.activity = activity;
        this.inputConnection = inputConnection;
        WindowManager wm = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        screenHeight = d.getHeight();
        onCreate();
    }

    /**
     * 显示输入法
     * @param x 显示位置、相对于屏幕的绝对x坐标
     * @param y 显示位置、相对于屏幕的绝对y坐标
     */
    public void show(final int x, final int y) {
        isShowing = true;
        Log.d(TAG, "show: " + candidateView.getHeight());
        //候选词高度为零、即是第一次显示
        if (candidateView.getMeasuredHeight() == 0) {
            //显示键盘
            inputWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
            //显示候选词窗口
            candidateWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y - dpTpPx(activity.getResources().getDimension(R.dimen.composing_height), activity));
            //这里延时200毫秒隐藏候选词窗口、目的是得到候选词view的高度
            delayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            candidateWindow.dismiss();
                            candidateWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y - candidateView.getMeasuredHeight());
                            candidateView.getRootView().setVisibility(View.GONE);
                            //拖拽窗口宽度应跟键盘一致、查阅源码发现还有一个边距5dp需要加上、否则宽度不一致
                            dragPopupWindow.setWidth(candidateView.getMeasuredWidth() + dpTpPx(5, activity));
                            dragPopupWindow.setHeight(candidateView.getMeasuredHeight());
                            //显示拖拽窗口
                            dragPopupWindow.show(x, y - candidateView.getMeasuredHeight());
                        }
                    });
                }
            },200);
        } else {
            //不是第一次显示
            dragPopupWindow.getContentView().getRootView().setVisibility(View.VISIBLE);
            inputView.getRootView().setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏输入法
     */
    public void hide() {
        if (!isShowing) {
            return;
        }
        //窗口的隐藏不用dismiss(),原因是再次用showAsLocation()显示位置会错乱、用View.GONE/View.VISIBLE不会
        inputView.getRootView().setVisibility(View.GONE);
        candidateView.getRootView().setVisibility(View.GONE);
        dragPopupWindow.getContentView().getRootView().setVisibility(View.GONE);
        isShowing = false;

    }

    /**
     * 创建输入法窗口
     */
    public void onCreate() {
        pinyinIME = new PinyinIME(activity, inputConnection);
        pinyinIME.onCreate();
        pinyinIME.setIsCandidateEmptyListener(new PinyinIME.IsCandidateEmptyListener() {
            @Override
            public void isCandidateEmpty(boolean isEmpty) {
                if (isEmpty) {
                    candidateView.getRootView().setVisibility(View.GONE);
                    dragPopupWindow.getContentView().getRootView().setVisibility(View.VISIBLE);
                } else {
                    candidateView.getRootView().setVisibility(View.VISIBLE);
                    dragPopupWindow.getContentView().getRootView().setVisibility(View.GONE);
                }
            }
        });
        inputView = pinyinIME.onCreateInputView();
        candidateView = pinyinIME.onCreateCandidatesView();

        pinyinIME.onStartInputView(new EditorInfo(), false);
        candidateWindow = new PopupWindow(activity);
        candidateWindow.setContentView(candidateView);

        inputWindow = new PopupWindow(activity);
        inputWindow.setContentView(inputView);
        inputWindow.setOutsideTouchable(false);
        inputWindow.setTouchable(true);

        dragPopupWindow = new DragPopupWindow(activity);
        dragPopupWindow.setOnDragMoveListener(new DragPopupWindow.OnDragMoveListener() {
            @Override
            public void onDragMove(int moveX, int moveY) {
                Log.d(TAG, "onDragMove: moveX=" + moveX + ",moveY=" + moveY);
                boolean isMoveY = true;

                if (moveY < candidateView.getMeasuredHeight() || moveY + inputView.getMeasuredHeight() + candidateView.getMeasuredHeight() + dpTpPx(5, activity) > screenHeight) {
                    Log.d(TAG, "onDragMove: 超出边界");
                    isMoveY = false;
                }

                if (isMoveY) {
                    dragPopupWindow.update(moveX, moveY, -1, -1, true);
                    candidateWindow.update(moveX, moveY, -1, -1, true);
                    inputWindow.update(moveX, moveY + candidateView.getMeasuredHeight(), -1, -1, true);
                }

            }
        });
    }

    /**
     * 释放窗口
     */
    public void onDestroy() {
        inputWindow.dismiss();//释放键盘
        candidateWindow.dismiss();//释放候选词
        dragPopupWindow.dismiss();//释放拖拽长窗口
        pinyinIME.onFinishInput();
        pinyinIME.onDestroy();
        activity = null;
        inputConnection = null;
    }

    /**
     * 动态dp转pixel
     *
     * @param value   dp值
     * @param context 上下文
     * @return
     */
    public static int dpTpPx(float value, Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) ((double) TypedValue.applyDimension(1, value, dm) + 0.5D);
    }
}
