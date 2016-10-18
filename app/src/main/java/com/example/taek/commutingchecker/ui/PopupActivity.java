package com.example.taek.commutingchecker.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.taek.commutingchecker.R;

public class PopupActivity extends AppCompatActivity {
    private TextView tv_alert_title, tv_alert_text;
    private Button btn_open_activity, btn_close_alert;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);

        tv_alert_title = (TextView)findViewById(R.id.alert_title);
        tv_alert_text = (TextView)findViewById(R.id.alert_text);
        btn_open_activity = (Button) findViewById(R.id.btn_open_activity);
        btn_close_alert = (Button) findViewById(R.id.btn_close_alert);

        // 화면을 깨우는 부분
        mRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    turnOffScreen();
                    PopupActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 60000); // 1분 뒤에 화면을 끄고 종료
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
                PopupActivity.this.finish();
            }
        });
    }

    private void wakeScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                // 화면 유지하기
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void turnOffScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                // 키잠금 해제하기
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // 화면 켜기
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                // 화면 유지하기
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
