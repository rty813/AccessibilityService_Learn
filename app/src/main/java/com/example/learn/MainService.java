package com.example.learn;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class MainService extends Service {
    private static final String TAG = "MainService";
    private LinearLayout toucherLayout;
    private WindowManager.LayoutParams params;
    private static WindowManager manager;
    public static Button btnStart;
    int statusBarHeight = -1;
    public static boolean start = false;
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private static LayoutInflater inflater;

    public MainService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");

        powerManager = (PowerManager) this.getSystemService(Service.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myApp:My Lock");

        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        manager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = 150;
        params.height = 150;

        inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (LinearLayout) inflater.inflate(R.layout.window_main,null);
        //添加toucherlayout
        manager.addView(toucherLayout, params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        btnStart = toucherLayout.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = true;
            }
        });
        btnStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //ImageButton我放在了布局中心，布局一共300dp
                params.x = (int) event.getRawX() - 75;
                //这就是状态栏偏移量用的地方
                params.y = (int) event.getRawY() - 75 - statusBarHeight;
                manager.updateViewLayout(toucherLayout,params);
                return false;
            }
        });
        toucherLayout.findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });

        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        if (btnStart != null) {
            manager.removeView(toucherLayout);
            wakeLock.release();
        }
        super.onDestroy();
    }
}
