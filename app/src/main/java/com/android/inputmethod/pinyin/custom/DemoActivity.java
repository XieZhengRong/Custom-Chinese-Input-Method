package com.android.inputmethod.pinyin.custom;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;


import com.android.inputmethod.pinyin.PinyinIME;
import com.android.inputmethod.pinyin.R;

import java.lang.reflect.Method;


public class DemoActivity extends Activity {
    private EditText edit;
    private PinYInIMEWindow pinYInIMEWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        edit = (EditText) findViewById(R.id.edit);

        disableShowInput(edit);
        edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (pinYInIMEWindow != null) {
                    if (hasFocus) {
                        pinYInIMEWindow.show(300, 300);
                    } else {
                        pinYInIMEWindow.hide();
                    }
                } else {
                    Toast.makeText(DemoActivity.this, "请先创建输入法", Toast.LENGTH_SHORT).show();
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
    }

    public void createInput(View view) {
        if (pinYInIMEWindow != null) {
            Toast.makeText(DemoActivity.this, "请勿重复创建输入法", Toast.LENGTH_SHORT).show();
            return;
        }
        pinYInIMEWindow = new PinYInIMEWindow(this, edit.onCreateInputConnection(new EditorInfo()));
        pinYInIMEWindow.getInputWindow().setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                edit.clearFocus();
            }
        });
        Toast.makeText(DemoActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
    }

    public void destroyInput(View view) {
        pinYInIMEWindow.onDestroy();
        pinYInIMEWindow = null;
        Toast.makeText(DemoActivity.this, "销毁成功", Toast.LENGTH_SHORT).show();
    }
}
