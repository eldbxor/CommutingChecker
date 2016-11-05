package com.example.taek.commutingchecker.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.services.CalibrationService;
import com.example.taek.commutingchecker.ui.CalibrationFragment;
import com.example.taek.commutingchecker.ui.MainActivity;
import com.example.taek.commutingchecker.ui.ThresholdAdjustmentFragment;

import java.util.ArrayList;

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
                        if (CalibrationFragment.timerSecond <= 60) {
                            CalibrationFragment.timerSecond++;
                            CalibrationFragment.progressBar.setProgress(CalibrationFragment.timerSecond);
                            CalibrationFragment.timerTextUpdate();
                        }
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
                                MainActivity.activity.finish();

                                // MainActivity.conn.onServiceDisconnected(MainActivity.serviceComponentName);
                            }
                        }, 2000);
                        break;

                    case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION_RESULT:
                        Log.d("MessengerCommunication", "Activity receive 6");
                        try {
                            MainActivity.strOfCalibrationResult = String.valueOf(msg.obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case Constants.HANDLE_MESSAGE_TYPE_PRESENT_COORDINATE:
                        Log.d("MessengerCommunication", "Activity receive 7");
                        final int coordinate[] = (int []) msg.obj;
                        ThresholdAdjustmentFragment.fragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ThresholdAdjustmentFragment.tvCoordinateX.setText(String.valueOf(coordinate[0]));
                                ThresholdAdjustmentFragment.tvCoordinateY.setText(String.valueOf(coordinate[1]));
                                ThresholdAdjustmentFragment.tvCoordinateZ.setText(String.valueOf(coordinate[2]));
                            }
                        });
                        break;
                }
                break;

            case Constants.HANDLER_TYPE_SERVICE: // Service's handleMessage
                CalibrationService mCalibrationService = (CalibrationService) mContext;
                switch (msg.what){
                    case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION:
                        Log.d("MessengerCommunication", "Service receive 1");
                        mCalibrationService.calibrationResetFlag = false;
                        mCalibrationService.CalibrationFlag = true;
                        mCalibrationService.CompleteCalibraton = false;
                        mCalibrationService.replyToActivityMessenger = msg.replyTo;

                        try{
                            do {
                                Thread.sleep(100);
                            }while(mCalibrationService.EssentialDataArray.size() == 0);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        mCalibrationService.scanLeDevice(true);

                        mCalibrationService.mCalibration.calibration();
                        break;

                    case Constants.HANDLE_MESSAGE_TYPE_CALIBRATION_RESET:
                        Log.d("MessengerCommunication", "Service receive 2");
                        mCalibrationService.calibrationResetFlag = true;
                        Log.d("ServHandler_ResetFlag", String.valueOf(mCalibrationService.calibrationResetFlag));
                        break;

                    case Constants.HANDLE_MESSAGE_TYPE_CEHCK_THRESHOLD:
                        Log.d("MessengerCommunication", "Service receive 3");
                        mCalibrationService.mBLEServiceUtils.comeToWorkCheckTime(Constants.CALLBACK_TYPE_CALIBRATION_SERVICE);
                        break;

                    case Constants.HANDLE_MESSAGE_TYPE_COMPLETE_CALIBRATION:
                        Log.d("MessengerCommunication", "Service receive 4");
                        mCalibrationService.CompleteCalibraton = true;
                        break;

                    case Constants.HANDLE_MESSAGE_TYPE_SEEKBAR_VALUE_CHANGED:
                        Log.d("MessengerCommunication", "Service receive 5");
                        mCalibrationService.mBLEServiceUtils.threshold_Calibration = msg.arg1;
                        break;
                }
                break;
        }
    }
}
