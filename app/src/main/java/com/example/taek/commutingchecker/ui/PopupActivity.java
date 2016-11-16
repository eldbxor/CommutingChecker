package com.example.taek.commutingchecker.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.RegisterReceiver;

import java.util.Timer;
import java.util.TimerTask;

public class PopupActivity extends AppCompatActivity {
    public static boolean finishFlag;
    private TextView tv_alert_title, tv_alert_text;
    private Button btn_open_activity, btn_close_alert;
    private Handler mHandler;
    private Runnable mRunnable;
    BroadcastReceiver CloseAlertReceiver;
    RegisterReceiver mRegisterReceiver;
    // Timer timer;
    Window window;
    String TAG = "PopupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);

        tv_alert_title = (TextView)findViewById(R.id.alert_title);
        tv_alert_text = (TextView)findViewById(R.id.alert_text);
        btn_open_activity = (Button) findViewById(R.id.btn_open_activity);
        btn_close_alert = (Button) findViewById(R.id.btn_close_alert);
        finishFlag = false;
        mRegisterReceiver = new RegisterReceiver(this);
        CloseAlertReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_CLOSE_ALERT);
        registerReceiver(CloseAlertReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_CLOSE_ALERT));

/*
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "onCreate(): timer scheduling, finishFlag - " + String.valueOf(finishFlag));

                if(finishFlag) {
                    turnOffScreen();

                    finish();
                }
            }
        }, 3000, 3000);
*/

/*
        mRunnable = new Runnable() {
            @Override
            public void run() {
                turnOffScreen();

                PopupActivity.this.finish();
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 60000); // 1분 뒤에 화면을 끄고 종료
*/
        wakeScreen();

        btn_open_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 확인버튼을 누르면 앱의 런처액티비티를 호출한다.
                Intent intent = new Intent(PopupActivity.this, MainActivity.class);
                startActivityForResult(intent, 1);
                PopupActivity.this.finish();
            }
        });

        btn_close_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!finishFlag) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        turnOffScreen();

                        finish();
                    }
                });
            }
        });
        thread.start();
    }

    public void wakeScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                // 화면 유지하기
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "wakeScreen(): turn on the screen");
    }

    public void turnOffScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                // 화면 유지하기
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "turnOffScreen(): turn off the screen");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
/*
        try {
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        try {
            Log.d(TAG, "unregisterReceiver");
            if (CloseAlertReceiver != null) {
                unregisterReceiver(CloseAlertReceiver);
                CloseAlertReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onDestroy(): finish PopupActivity");
    }
}
