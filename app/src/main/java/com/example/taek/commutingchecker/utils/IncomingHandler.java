package com.example.taek.commutingchecker.utils;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.ui.CalibrationFragment;
import com.example.taek.commutingchecker.ui.MainActivity;
import com.example.taek.commutingchecker.ui.ThresholdAdjustmentFragment;

/**
 * Created by Taek on 2016-08-31.
 */
public class IncomingHandler extends Handler{
    int mHandlerType;
    Context mContext;

    public IncomingHandler(int handlerType, Context context) {
        mHandlerType = handlerType;
        mContext = context;
    }

    @Override
    public void handleMessage(Message msg){
        switch (mHandlerType) {
            case Constants.HANDLER_TYPE_ACTIVITY: // Activity's handleMessage
                switch (msg.what) {
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
                        ThresholdAdjustmentFragment.fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ThresholdAdjustmentFragment.tvCheckThreshold.setText("출근존");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    ThresholdAdjustmentFragment.tvCheckThreshold.setTextColor(MainActivity.MainActivityContext.getResources().getColor(R.color.colorGreen, null));
                                else {
                                    ThresholdAdjustmentFragment.tvCheckThreshold.setTextColor(MainActivity.MainActivityContext.getResources().getColor(R.color.colorGreen));
                                }
                            }
                        });
                        break;
                    case Constants.HANDLE_MESSAGE_TYPE_SETTEXT_NOT_ATTENDANCE_ZONE:
                        Log.d("MessengerCommunication", "Activity receive 4");
                        ThresholdAdjustmentFragment.fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ThresholdAdjustmentFragment.tvCheckThreshold.setText("출근존이 아님");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    ThresholdAdjustmentFragment.tvCheckThreshold.setTextColor(MainActivity.MainActivityContext.getResources().getColor(R.color.colorRed, null));
                                else {
                                    ThresholdAdjustmentFragment.tvCheckThreshold.setTextColor(MainActivity.MainActivityContext.getResources().getColor(R.color.colorRed));
                                }
                            }
                        });
                        break;
                    case Constants.HANDLE_MESSAGE_TYPE_REGISTER_CALIBRATION:
                        Log.d("MessengerCommunication", "Activity receive 5");
                        try {
                            MainActivity.mainActivity.unbindService(MainActivity.conn);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MainActivity.conn = null;

                        MainActivity.unbindHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.MainActivityContext, "Calibration을 완료하였습니다. 앱을 재실행하세요.", Toast.LENGTH_SHORT).show();

                                MainActivity.conn.onServiceDisconnected(MainActivity.serviceComponentName);
                            }
                        }, 2000);
                        break;
                }
                break;

            case Constants.HANDLER_TYPE_SERVICE: // Service's handleMessage
                BLEScanService mBLEScanService = (BLEScanService) mContext;
                switch (msg.what){
                    case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION:
                        Log.d("MessengerCommunication", "Service receive 1");
                        mBLEScanService.calibrationResetFlag = false;
                        mBLEScanService.CalibrationFlag = true;
                        mBLEScanService.CompleteCalibraton = false;
                        mBLEScanService.replyToActivityMessenger = msg.replyTo;

                        try{
                            do {
                                Thread.sleep(100);
                            } while (mBLEScanService.mSocketIO.connected() == false);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        mBLEScanService.scanLeDevice(true);
                        /*
                            *****************변경사항********************
                            *****************바운드 서비스에서 동작하기*******************
                         */
                        // mBLEScanService.calibration();
                        break;
                    case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION_RESET:
                        Log.d("MessengerCommunication", "Service receive 2");
                        mBLEScanService.calibrationResetFlag = true;
                        Log.d("ServHandler_ResetFlag", String.valueOf(mBLEScanService.calibrationResetFlag));
                        break;
                    case Constants.HANDLE_MESSAGE_TYPE_CEHCK_THRESHOLD:
                        Log.d("MessengerCommunication", "Service receive 3");
                        mBLEScanService.mBLEServiceUtils.comeToWorkCheckTime();
                        break;
                    case Constants.HANDLE_MESSAGE_TYPE_COMPLETE_CALIBRATION:
                        Log.d("MessengerCommunication", "Service receive 4");
                        mBLEScanService.CompleteCalibraton = true;
                        break;
                    case Constants.HANDLE_MESSAGE_TYPE_SEEKBAR_VALUE_CHANGED:
                        Log.d("MessengerCommunication", "Service receive 5");
                        BLEServiceUtils.threshold_Calibration = msg.arg1;
                        break;
                }
                break;
        }
    }
}
