package com.android.inputmethod.pinyin.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupWindow;


import com.android.inputmethod.pinyin.PinyinIME;
import com.android.inputmethod.pinyin.R;

import java.lang.reflect.Method;


public class DemoActivity extends Activity {
    private EditText edit;
    PinyinIME pinyinIME;
    View inputView;
    View candidateView;
    PopupWindow inputWindow;
    PopupWindow candidateWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        ActivityStack.addActivity(this);
        edit = (EditText) findViewById(R.id.edit);
        pinyinIME = new PinyinIME(this, edit.onCreateInputConnection(new EditorInfo()));
        pinyinIME.onCreate();
        inputView = pinyinIME.onCreateInputView();
        candidateView = pinyinIME.onCreateCandidatesView();

        pinyinIME.onStartInputView(new EditorInfo(), false);
        candidateWindow = new PopupWindow(DemoActivity.this);
        candidateWindow.setContentView(candidateView);

        inputWindow = new PopupWindow(DemoActivity.this);
        inputWindow.setContentView(inputView);
        inputWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                edit.clearFocus();
            }
        });

        inputWindow.setOutsideTouchable(false);

        inputWindow.setTouchable(true);
        disableShowInput(edit);
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    candidateWindow.showAsDropDown(edit);
                    inputWindow.showAtLocation(edit, Gravity.BOTTOM, 0, 0);
                } else {
                    inputWindow.dismiss();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    public static void disableShowInput(EditText et) {
        Class<EditText> cls = EditText.class;
        Method method;
        try {
            //setShowSoftInputOnFocus方法是EditText从TextView继承来的的
            //可以用来设置当EditText获得焦点时软键盘是否可见
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(et, false);
        } catch (Exception e) {//TODO: handle exception
        }
        try {
            //这里是没用的，直接删掉即可，因为EditText类，TextView类，View类中，
            // 都没有setSoftInputShownOnFocus这个方法，不可能获得method对象的
            method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(et, false);
        } catch (Exception e) {//TODO: handle exception
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.removeActivity(this);
    }
}
