package com.example.learn;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.content.Intent;
import android.graphics.Path;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class AutoSwipeAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoSwipeService";
    private boolean scrollFlag = false;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d(TAG, "onAccessibilityEvent: Start:" + MainService.start);
        try{
            AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
                Log.d(TAG, "onAccessibilityEvent: " + accessibilityEvent.getSource().getClassName());
            }
            if (rootInfo == null || scrollFlag || !MainService.start) {
                return;
            }

            Path path = new Path();
            path.moveTo(400, 1000);
            path.lineTo(400, 800);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            GestureDescription gestureDescription = builder
                    .addStroke(new GestureDescription.StrokeDescription(
                            path, 200, 5000))
                    .build();
            scrollFlag = true;
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "onCompleted: 滑动结束");
                    MainService.btnStart.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollFlag = false;
                        }
                    }, 1000);
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.d(TAG, "onCancelled: 滑动取消");
                    Toast.makeText(AutoSwipeAccessibilityService.this, "滑动取消", Toast.LENGTH_SHORT).show();
                    scrollFlag = false;
                    MainService.start = false;
                    super.onCancelled(gestureDescription);
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {

    }
}
