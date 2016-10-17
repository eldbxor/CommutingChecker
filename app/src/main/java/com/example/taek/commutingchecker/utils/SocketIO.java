package com.example.taek.commutingchecker.utils;

import android.content.Context;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.services.CalibrationService;
import com.example.taek.commutingchecker.ui.ChartFragment;
import com.example.taek.commutingchecker.ui.EntryActivity;
import com.example.taek.commutingchecker.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

/**
 * Created by Taek on 2016-04-15.
 */
public class SocketIO {

    io.socket.client.Socket mSocket;

    /** 2016. 7. 27
     * Awesometic
     * Declaration Anaylzer instance
     * When socket.io data communicating,
     * It helps encrypt, decrypt JSON data and analyze received JSON from server
     */
    Analyzer analyzer;
    public Context mContext;
    private SocketDataQueue mSocketDataQueue;

    // 생성자
    public SocketIO(Context context) {
        try {
            mSocket = IO.socket(Constants.SERVER_URL);

            mContext = context;

            // Initialize anlayzer instance
            analyzer = new Analyzer();

            mSocketDataQueue = new SocketDataQueue();

        } catch (URISyntaxException e){
            e.printStackTrace();
            Log.d("connectSocket", "Error");
        }
    }

    // 소켓 연결
    public void connect() {
        if(!mSocket.connected()) {
            mSocket.connect();
        }else {
            Log.d("Service' onCreate(): ", "Socket is already connected");
        }
        Log.d("NetworkStatus", "connecting...");
    }

    // Get RSA public key from the server
    public void getServersRsaPublicKey(Object... args) {
        if (mSocket.connected()) {

            switch (args.length) {
                case 0:
                    mSocket.emit("requestRsaPublicKeyWithoutSmartphoneAddress");

                    break;
                case 1:
                    try {
                        JSONObject content = new JSONObject();
                        content.put("SmartphoneAddress", BLEScanService.myMacAddress);

                        mSocket.emit("requestRsaPublicKey", analyzer.encryptSendJson(content));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                default:
                    break;
            }

            mSocket.on("rsaPublicKey", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject receiveJsonObject = (JSONObject) args[0];
                        analyzer.serversPublicKey = RSACipher.stringToPublicKey(String.valueOf(receiveJsonObject.get("publicKey")));
                        Log.d("Awesometic", "getServerRsaPublicKey - receive success \n" + String.valueOf(receiveJsonObject.get("publicKey")));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Awesometic", "getServerRsaPublicKey - receive fail");
                    }
                }
            });

        } else {
            Log.d("Awesometic", "getServerRsaPublicKey - socket isn't connected");
        }
    }

    // 이벤트 보내기
    public void sendCommutingEvent(final Map<String, String> data, final boolean isComeToWork) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    JSONObject content = new JSONObject();
                    content.put("SmartphoneAddress", BLEScanService.myMacAddress);
                                   /*
                content.put("BeaconDeviceAddress1", mDeviceInfo1.Address);
                    content.put("BeaconDeviceAddress2", mDeviceInfo2.Address);
                    content.put("BeaconDeviceAddress3", mDeviceInfo3.Address);
                    content.put("BeaconData1", mDeviceInfo1.ScanRecord);
                    content.put("BeaconData2", mDeviceInfo2.ScanRecord);
                    content.put("BeaconData3", mDeviceInfo3.ScanRecord);
                    content.put("SmartphoneAddress", BLEScanService.myMacAddress);
                    content.put("DateTime", CurrentTime.currentTime());
                 */
                    content.put("BeaconDeviceAddress1", data.get("BeaconDeviceAddress1"));
                    content.put("BeaconDeviceAddress2", data.get("BeaconDeviceAddress2"));
                    content.put("BeaconDeviceAddress3", data.get("BeaconDeviceAddress3"));
                /*
                Gateway 1 (pi2): f0 f4 c1 76 a6 23 42 ef ac 3a 66 f2 1a 11 99 3e 00 02 00 01
Gateway 2 (pi2): bf 0c c4 a4 eb 9f 4f 06 b7 16 1f 5f f4 9a 8f 47 00 02 00 02
Gateway 3 (pi3): 70 9b d6 40 42 d1 4b 1a 99 0a 36 d4 a1 e5 27 d8 00 03 00 01
Gateway 4 (pi3): b1 2a 7a b6 d0 12 49 92 88 09 43 4d d1 34 30 19 00 03 00 02
                 */
                    content.put("BeaconData1", data.get("BeaconData1"));
                    content.put("BeaconData2", data.get("BeaconData2"));
                    content.put("BeaconData3", data.get("BeaconData3"));
                    content.put("SmartphoneAddress", data.get("SmartphoneAddress"));
                    if (isComeToWork) {
                        content.put("Commute", "true");
                    } else {
                        content.put("Commute", "false");
                    }
                    //content.put("DateTime", data.get("DateTime"));
                    Log.d("Awesometic", "sendCommutingEvent - send message to server");
                    mSocket.emit("circumstance", analyzer.encryptSendJson(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "sendCommutingEvent - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "sendCommutingEvent - server's public key is not initialized");
                getServersRsaPublicKey();
            }

            mSocket.on("circumstance_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONObject contentJson = new JSONObject(analyzer.extractContentFromReceivedJson(resultJson));

                        Boolean isSuccess = contentJson.getBoolean("requestSuccess");
                        if(isSuccess) {
                            BLEScanService.failureCount_SendEv = 0;
                            Log.d("SendEvent", "Success");
                            if(isComeToWork == true) {
                                Log.d("ComeToWork", "comeToWork is registered");
                                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "CommutingChecker", "출근 등록", "출근이 등록되었습니다.");
                            }else {
                                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "CommutingChecker", "퇴근 등록", "퇴근이 등록되었습니다.");
                            }
                        }else {
                            if(BLEScanService.failureCount_SendEv < 2) {
                                BLEScanService.failureCount_SendEv++;
                                sendCommutingEvent(data, isComeToWork);
                                return;
                            }
                            else{
                                Log.d("SendEvent", "failed");
                                BLEScanService.failureCount_SendEv = 0;
                                if(isComeToWork == true) {
                                    Log.d("ComeToWork", "comeToWork isn't registered");
                                    GenerateNotification.generateNotification(BLEScanService.ServiceContext, "CommutingChecker", "출근 등록 실패", "출근 등록을 실패하였습니다.");
                                }else{
                                    GenerateNotification.generateNotification(BLEScanService.ServiceContext, "CommutingChecker", "퇴근 등록 실패", "퇴근 등록을 실패하였습니다.");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Awesometic", "sendCommutingEvent - exception caught (result analyze)");
                    }
                }
            });

                            /*
                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "서버에 데이터 전송", "서버에 데이터를 전송하였습니다.",
                        data.get("BeaconDeviceAddress1") + ", "
                        + data.get("BeaconDeviceAddress2") + ", "
                        + data.get("BeaconDeviceAddress3") + ", "
                        + data.get("BeaconData1") + ", "
                        + data.get("BeaconData2") + ", "
                        + data.get("BeaconData3") + ", "
                        + data.get("SmartphoneAddress"));
                        //+ ", " + data.get("DateTime")); */

            //this.close();

        } else {
            Log.d("Awesometic", "sendCommutingEvent - socket isn't connected");
            mSocket.connect();
            data.put("isComeToWork", String.valueOf(isComeToWork));
            insertQueueData(data);
        }
    }

    // More than api ver.19
    //@SuppressLint("NewApi")
    public void requestEssentialData(final int callbackType) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    JSONObject content = new JSONObject();
                    switch (callbackType) {
                        case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                            content.put("SmartphoneAddress", BLEScanService.myMacAddress);
                            break;

                        case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                            content.put("SmartphoneAddress", CalibrationService.myMacAddress);
                            break;
                    }
                    Log.d("Awesometic", "requestEseentialData: send message to server");
                    mSocket.emit("requestEssentialData", analyzer.encryptSendJson(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "requestEssentialData - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "requestEssentialData - server's public key is not initialized");
            }

            mSocket.on("requestEssentialData_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONArray contentJsonArray = new JSONArray(analyzer.extractContentFromReceivedJson(resultJson));

                        // If workplace count is more than 1, is it can deal with properly?
                        for (int i = 0; i < contentJsonArray.length(); i++) {
                            String[] beaconAddress = {
                                    contentJsonArray.getJSONObject(i).getString("beacon_address").split("-")[0],
                                    contentJsonArray.getJSONObject(i).getString("beacon_address").split("-")[1],
                                    contentJsonArray.getJSONObject(i).getString("beacon_address").split("-")[2]
                            };
                            int[] threshold = {
                                    contentJsonArray.getJSONObject(i).getInt("thresholdX"),
                                    contentJsonArray.getJSONObject(i).getInt("thresholdY"),
                                    contentJsonArray.getJSONObject(i).getInt("thresholdZ")
                            };

                            Map<String, String> map = new HashMap<>();
                            map.put("id_workplace", contentJsonArray.getJSONObject(i).getString("id_workplace"));
                            map.put("coordinateX", contentJsonArray.getJSONObject(i).getString("coordinateX"));
                            map.put("coordinateY", contentJsonArray.getJSONObject(i).getString("coordinateY"));
                            map.put("coordinateZ", contentJsonArray.getJSONObject(i).getString("coordinateZ"));
                            map.put("beacon_address1", beaconAddress[0]);
                            map.put("beacon_address2", beaconAddress[1]);
                            map.put("beacon_address3", beaconAddress[2]);
                            map.put("thresholdX", String.valueOf(threshold[0]));
                            map.put("thresholdY", String.valueOf(threshold[1]));
                            map.put("thresholdZ", String.valueOf(threshold[2]));

                            switch (callbackType) {
                                case Constants.CALLBACK_TYPE_BLE_SCAN_SERVICE:
                                    ((BLEScanService) mContext).mBLEServiceUtils.addEssentialData(callbackType, map);
                                    for (int j = 0; j < 3; j++) {
                                        ((BLEScanService) mContext).mBLEServiceUtils.addFilterList(callbackType, beaconAddress[j]);
                                    }
                                    break;

                                case Constants.CALLBACK_TYPE_CALIBRATION_SERVICE:
                                    ((CalibrationService) mContext).mBLEServiceUtils.addEssentialData(callbackType, map);
                                    for (int j = 0; j < 3; j++) {
                                        ((CalibrationService) mContext).mBLEServiceUtils.addFilterList(callbackType, beaconAddress[j]);
                                    }
                                    break;

                                case Constants.CALLBACK_TYPE_MAIN_ACTIVITY:
                                    break;
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Awesometic", "requestEssentialData - exception caught (result analyze)");
                    }
                }
            });
        } else {
            Log.d("Awesometic", "requestEssentialData - socket isn't connected");
        }
    }

    public void calibration(final Map<String, String> data) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    JSONObject content = new JSONObject();
                    content.put("BeaconDeviceAddress1", data.get("BeaconDeviceAddress1"));
                    content.put("BeaconDeviceAddress2", data.get("BeaconDeviceAddress2"));
                    content.put("BeaconDeviceAddress3", data.get("BeaconDeviceAddress3"));
                    content.put("BeaconData1", data.get("BeaconData1"));
                    content.put("BeaconData2", data.get("BeaconData2"));
                    content.put("BeaconData3", data.get("BeaconData3"));
                    content.put("SmartphoneAddress", data.get("SmartphoneAddress"));
                    content.put("DateTime", data.get("DateTime"));
                    content.put("CoordinateX", data.get("CoordinateX"));
                    content.put("CoordinateY", data.get("CoordinateY"));
                    content.put("CoordinateZ", data.get("CoordinateZ"));
                    content.put("ThresholdX", data.get("ThresholdX"));
                    content.put("ThresholdY", data.get("ThresholdY"));
                    content.put("ThresholdZ", data.get("ThresholdZ"));

                    Log.d("Awesometic", "calibration: send message to server");
                    mSocket.emit("calibration", analyzer.encryptSendJson(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "calibration - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "calibration - server's public key is not initialized");
            }

            mSocket.on("calibration_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONObject contentJson = new JSONObject(analyzer.extractContentFromReceivedJson(resultJson));

                        Boolean isSuccess = contentJson.getBoolean("requestSuccess");
                        if(isSuccess) {
                            ((CalibrationService) mContext).failureCount_Cali = 0;
                            Log.d("Calibration", "Success");
                            // Toast.makeText(BLEScanService.ServiceContext, "Calibration: (" + data.get("CoordinateX") + ", "
                            //        + data.get("CoordinateY") + ", " + data.get("CoordinateZ"), Toast.LENGTH_SHORT).show();
//                            GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration 성공", "새로운 좌표 값이 등록되었습니다.", "");
                            /*
                            GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration", "Rssi 평균 값을 서버에 전송하였습니다." + ".",
                                    "rss1: " + data.get("CoordinateX") + ", " + "rssi2: " + data.get("CoordinateY") + ", " + "rss3: " + data.get("CoordinateZ")); */
                        } else {
                            if(((CalibrationService) mContext).failureCount_Cali < 2) {
                                ((CalibrationService) mContext).failureCount_Cali++;
                                calibration(data);
                                return;
                            }
                            else{
                                Log.d("Calibration", "failed");
                                ((CalibrationService) mContext).failureCount_Cali = 0;
                                // Toast.makeText(BLEScanService.ServiceContext, "새로운 좌표 값 등록을 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                // GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration 실패", "새로운 좌표 값 등록을 실패하였습니다.", "");
                            }
                        }

                        // 앱 종료
                        try {
                            Log.d("MessengerCommunication", "Service send 5");
                            ((CalibrationService) mContext).replyToActivityMessenger.send(Message.obtain(null, Constants.HANDLE_MESSAGE_TYPE_REGISTER_CALIBRATION));
                        }catch(RemoteException e){
                            Log.d("replyToActivity", e.toString());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Request answer(Cali)", "failed");
                    }
                }
            });
            //MainActivity.sendData = true;

            //this.close();
        } else {
            Log.d("Awesometic", "calibration - socket isn't connected");
        }
    }

    public void amIRegistered(String smartphoneAddress) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    JSONObject content = new JSONObject();
                    content.put("SmartphoneAddress", smartphoneAddress);

                    mSocket.emit("amIRegistered", analyzer.encryptSendJson(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "amIRegistered - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "amIRegistered - server's public key is not initialized");
            }

            mSocket.on("amIRegistered_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONObject contentJson = new JSONObject(analyzer.extractContentFromReceivedJson(resultJson));

                        boolean registered = Boolean.valueOf(String.valueOf(contentJson.get("registered")));
                        if (registered) {
                            String name = String.valueOf(contentJson.get("name"));
                            String employee_number = String.valueOf(contentJson.get("employee_number"));
                            boolean permitted = Boolean.valueOf(String.valueOf(contentJson.get("permitted")));

                            Log.d("Awesometic", "amIRegistered - true" + ", " + String.valueOf(permitted) + ", " + name + ", " + employee_number);
                            EntryActivity.amIRegistered = true;
                            EntryActivity.permitted = permitted;
                            MainActivity.employee_name = name;
                            MainActivity.employee_number = employee_number;

                            EntryActivity.isGettingEmployeeDataFinished = true;
                        } else {
                            Log.d("Awesometic", "amIRegistered - false");
                            EntryActivity.amIRegistered = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Awesometic", "amIRegistered - receive fail");
                    }
                }
            });

        } else {
            Log.d("Awesometic", "amIRegistered - socket isn't connected");
        }
    }

    public void signupRequest(String smartphoneAddress, String employee_number, String name, String password, String department, String position) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    JSONObject content = new JSONObject();
                    content.put("SmartphoneAddress", smartphoneAddress);
                    content.put("EmployeeNumber", employee_number);
                    content.put("Name", name);
                    content.put("Password", password);
                    content.put("Department", department);
                    content.put("Position", position);
                    content.put("Permission", 0);
                    content.put("Admin", 0);

                    mSocket.emit("signupRequest", analyzer.encryptSendJson(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "signupRequest - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "signupRequest - server's public key is not initialized");
            }

            mSocket.on("signupRequest_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONObject contentJson = new JSONObject(analyzer.extractContentFromReceivedJson(resultJson));

                        boolean requestSuccess = contentJson.getBoolean("requestSuccess");

                        if (requestSuccess)
                            Log.d("Awesometic", "signupRequest - Success");
                        else
                            Log.d("Awesometic", "signupRequest - Fail - Something Wrong");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Awesometic", "signupRequest - JSONException");
                    }
                }
            });

        } else {
            Log.d("Awesometic", "signupRequest - socket isn't connected");
        }
    }

    public enum ChartSignal {
        POPULATION
    }

    public void requestChartData(ChartSignal signal) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    switch (signal) {
                        case POPULATION:
                            JSONObject content = new JSONObject();
                            content.put("SmartphoneAddress", BLEScanService.myMacAddress);
                            content.put("Signal", signal.toString());

                            mSocket.emit("requestChartData", analyzer.encryptSendJson(content));

                            break;

                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "requestChartData - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "requestChartData - server's public key is not initialized");
            }

            mSocket.on("requestChartData_answer", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONArray contentJsonArray = new JSONArray(analyzer.extractContentFromReceivedJson(resultJson));
/*
                        Log.d("Awesometic", contentJson.toString());
                        StringBuilder result = new StringBuilder(contentJson.toString());
                        result.insert(0, "{ \"data\":");
                        result.insert(result.length(), "}");
*/

//                        Log.d("Awesometic", result.toString());

//                        JSONObject chartData = new JSONObject(result.toString());
                        ChartFragment.chartData = contentJsonArray;
                        ChartFragment.chartDataReceived = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            Log.d("Awesometic", "requestChartData - socket isn't connected");
        }
    }

    public void sendQueueData() {
        if (mSocketDataQueue.isEmpty())
            return;

        if(!mSocketDataQueue.isEmpty()) {
            Map<String, String> map = (HashMap<String, String>) mSocketDataQueue.remove();
            String isComeToWork = map.get("isComeToWork");
            if (map.containsKey("isComeToWork")) {
                map.remove("isComeToWork");
            }
            sendCommutingEvent(map, Boolean.valueOf(isComeToWork));
        }
        /*
        for (int i = 0; i < mSocketDataQueue.size(); i++) {
            Map<String, String> map = (HashMap<String, String>) mSocketDataQueue.remove();
            String isComeToWork = map.get("isComeToWork");
            if(map.containsKey("isComeToWork")) {
                map.remove("isComeToWork");
            }
            sendCommutingEvent(map, Boolean.valueOf(isComeToWork));
        } */
    }

    public void removePublicKey() {
        analyzer.serversPublicKey = null;
    }

    public boolean isServersPublicKeyInitialized() {
        if (analyzer.serversPublicKey != null)
            return true;
        else
            return false;
    }

    public boolean connected() {
        return mSocket.connected();
    }

    public void insertQueueData(Object obj) {
        if (!mSocketDataQueue.contains(obj)) {
            mSocketDataQueue.insert(obj);
        }
    }

    // 소켓 닫기
    public void close(){
        mSocket.close();
    }
}