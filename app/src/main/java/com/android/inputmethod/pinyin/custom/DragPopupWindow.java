package com.android.inputmethod.pinyin.custom;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import com.android.inputmethod.pinyin.R;

public class DragPopupWindow extends PopupWindow implements View.OnTouchListener {
    View contentView;

    private int downX;
    private int downY;
    private OnDragMoveListener onDragMoveListener;
    private Activity activity;
    public DragPopupWindow(Activity activity) {
        super(activity, null);
        this.activity = activity;
        setTouchInterceptor(this);
    }

    @Override
    public void setContentView(View contentView) {
        super.setContentView(contentView);
        this.contentView = contentView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = (int) event.getX();
                downY = (int) event.getY();

            }

            break;
            case MotionEvent.ACTION_MOVE: {
                if (onDragMoveListener != null) {
                    onDragMoveListener.onDragMove((int)event.getRawX() - downX,(int)event.getRawY() - downY);
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                downY = 0;
                downX = 0;
            }
            break;
        }
        return false;
    }

    public void show(int x, int y) {
        View view = activity.getWindow().getDecorView();
        View contentView = LayoutInflater.from(activity).inflate(R.layout.drag_layout, null);
        setContentView(contentView);
        setOutsideTouchable(false);
        setTouchable(true);

        showAtLocation(view, Gravity.NO_GRAVITY, x, y);
    }

    public void setOnDragMoveListener(OnDragMoveListener onDragMoveListener) {
        this.onDragMoveListener = onDragMoveListener;
    }
    public interface OnDragMoveListener{
        void onDragMove(int x, int y);
    }

}
