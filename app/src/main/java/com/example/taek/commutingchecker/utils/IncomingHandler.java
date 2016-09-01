package com.example.taek.commutingchecker.utils;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.ui.CalibrationFragment;
import com.example.taek.commutingchecker.ui.MainActivity;
import com.example.taek.commutingchecker.ui.ThresholdAdjustmentFragment;

/**
 * Created by Taek on 2016-08-31.
 */
public class IncomingHandler extends Handler{

    @Override
    public void handleMessage(Message msg){
        switch (msg.what){
            case Constants.HANDLE_MESSAGE_TYPE_SETTEXT_NEXT:
                // Toast.makeText(MainActivity.MainActivityContext, "Complete calibration", Toast.LENGTH_SHORT).show();
                Log.d("MessengerCommunication", "Activity receive 1");
                CalibrationFragment.btnCalibrationStart.setText("NEXT");
                break;
            case Constants.HANDLE_MESSAGE_TYPE_ADD_TIMESECOND:
                Log.d("MessengerCommunication", "Activity receive 2");
                CalibrationFragment.timerSecond++;
                CalibrationFragment.progressBar.setProgress(CalibrationFragment.timerSecond);
                break;
            case Constants.HANDLE_MESSAGE_TYPE_SETTEXT_ATTENDANCE_ZONE:
                Log.d("MessengerCommunication", "Activity receive 3");
                ThresholdAdjustmentFragment.tvCheckThreshold.setText("출근존");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    ThresholdAdjustmentFragment.tvCheckThreshold.setTextColor(MainActivity.MainActivityContext.getResources().getColor(R.color.colorGreen, null));
                break;
            case Constants.HANDLE_MESSAGE_TYPE_SETTEXT_NOT_ATTENDANCE_ZONE:
                Log.d("MessengerCommunication", "Activity receive 4");
                ThresholdAdjustmentFragment.tvCheckThreshold.setText("출근존이 아님");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    ThresholdAdjustmentFragment.tvCheckThreshold.setTextColor(MainActivity.MainActivityContext.getResources().getColor(R.color.colorRed, null));
                break;
            case Constants.HANDLE_MESSAGE_TYPE_REGISTER_CALIBRATION:
                Log.d("MessengerCommunication", "Activity receive 5");
                Toast.makeText(MainActivity.MainActivityContext, "Calibration을 완료하였습니다. 앱을 재실행하세요.", Toast.LENGTH_SHORT).show();
                MainActivity.mainActivity.unbindService(MainActivity.conn);
                MainActivity.mainActivity.finish();
        }
    }
}
