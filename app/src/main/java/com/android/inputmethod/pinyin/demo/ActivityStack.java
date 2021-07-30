package com.android.inputmethod.pinyin.demo;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

public class ActivityStack {
    public static List<Activity> activityStack = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activityStack.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activityStack.remove(activity);
    }

    public static void removeActivityBySimpleName(String simpleName) {
        for (Activity activity : activityStack) {
            if (simpleName.equals(activity.getClass().getSimpleName())) {
                activity.finish();
                activityStack.remove(activity);
            }
        }
    }

    public static void removeActivityDifLast() {
        Activity lastActivity = getLastActivity();
        for (int i = 0; i < activityStack.size() - 1; i++) {
            Activity activity = activityStack.get(i);
            assert lastActivity != null;
            if (lastActivity.getClass().getSimpleName().equals(activity.getClass().getSimpleName())) {
                activity.finish();
                activityStack.remove(activity);
            }
        }
    }

    public static Activity getActivityBySimpleName(String simpleName) {
        for (Activity activity : activityStack) {
            if (simpleName.equals(activity.getClass().getSimpleName())) {
                return activity;
            }
        }
        return null;
    }

    public static void removeAllActivity() {
        for (Activity activity : activityStack) {
            activity.finish();
        }
        activityStack.clear();
    }

    public static Activity getLastActivity() {
        if (activityStack.size() <= 0) {
            return null;
        }
        return activityStack.get(activityStack.size() - 1);
    }

    public static Activity getFirstActivity() {
        return activityStack.get(0);
    }

    public interface ActionCallBack {
        void onActionFinish();
    }
}
