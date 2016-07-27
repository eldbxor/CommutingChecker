package com.example.taek.commutingchecker.utils;

import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.ui.ChartFragment;
import com.example.taek.commutingchecker.ui.EntryActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

/**
 * Created by Taek on 2016-04-15.
 */
public class SocketIO {
    public static final int SERVICE_CALLBACK = 1;
    public static final int ACTIVITY_CALLBACK = 2;

    io.socket.client.Socket mSocket;

    /** 2016. 7. 27
     * Awesometic
     * Declaration Anaylzer instance
     * When socket.io data communicating,
     * It helps encrypt, decrypt JSON data and analyze received JSON from server
     */
    Analyzer analyzer;

    // 생성자
    public SocketIO(){
        try {
            mSocket = IO.socket(Constants.SERVER_URL);

            // Initialize anlayzer instance
            analyzer = new Analyzer();

        } catch (URISyntaxException e){
            e.printStackTrace();
            Log.d("connectSocket", "Error");
        }
    }

    // 소켓 연결
    public void connect(){
        mSocket.connect();
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
    public void sendEvent(final Map<String, String> data, final boolean isComeToWork) {
        final JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            if (mSocket.connected()) {
                Log.d("Socket", "connected");

                /*
                data.put("BeaconDeviceAddress1", mDeviceInfo1.Address);
                    data.put("BeaconDeviceAddress2", mDeviceInfo2.Address);
                    data.put("BeaconDeviceAddress3", mDeviceInfo3.Address);
                    data.put("BeaconData1", mDeviceInfo1.ScanRecord);
                    data.put("BeaconData2", mDeviceInfo2.ScanRecord);
                    data.put("BeaconData3", mDeviceInfo3.ScanRecord);
                    data.put("SmartphoneAddress", BLEScanService.myMacAddress);
                    data.put("DateTime", CurrentTime.currentTime());
                 */
                obj.put("BeaconDeviceAddress1", data.get("BeaconDeviceAddress1"));
                obj.put("BeaconDeviceAddress2", data.get("BeaconDeviceAddress2"));
                obj.put("BeaconDeviceAddress3", data.get("BeaconDeviceAddress3"));
                /*
                Gateway 1 (pi2): f0 f4 c1 76 a6 23 42 ef ac 3a 66 f2 1a 11 99 3e 00 02 00 01
Gateway 2 (pi2): bf 0c c4 a4 eb 9f 4f 06 b7 16 1f 5f f4 9a 8f 47 00 02 00 02
Gateway 3 (pi3): 70 9b d6 40 42 d1 4b 1a 99 0a 36 d4 a1 e5 27 d8 00 03 00 01
Gateway 4 (pi3): b1 2a 7a b6 d0 12 49 92 88 09 43 4d d1 34 30 19 00 03 00 02
                 */
                obj.put("BeaconData1", data.get("BeaconData1"));
                obj.put("BeaconData2", data.get("BeaconData2"));
                obj.put("BeaconData3", data.get("BeaconData3"));
                obj.put("SmartphoneAddress", data.get("SmartphoneAddress"));
                //obj.put("DateTime", data.get("DateTime"));

                mSocket.emit("circumstance", obj);

                // 서버에 등록이 되었는지 확인
                mSocket.on("answer", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject robj = (JSONObject) args[0];
                            String isSuccess = String.valueOf(robj.get("isSuccess"));
                            if(isSuccess.equals("true")) {
                                BLEScanService.failureCount_SendEv = 0;
                                Log.d("SendEvent", "Success");
                                if(isComeToWork == true) {
                                    GenerateNotification.generateNotification(BLEScanService.ServiceContext, "출근 등록", "출근이 등록되었습니다.", "");
                                }else{
                                    GenerateNotification.generateNotification(BLEScanService.ServiceContext, "퇴근 등록", "퇴근이 등록되었습니다.", "");
                                }
                            }else{
                                if(BLEScanService.failureCount_SendEv < 2) {
                                    BLEScanService.failureCount_SendEv++;
                                    sendEvent(data, isComeToWork);
                                    return;
                                }
                                else{
                                    Log.d("SendEvent", "failed");
                                    BLEScanService.failureCount_SendEv = 0;
                                    if(isComeToWork == true) {
                                        GenerateNotification.generateNotification(BLEScanService.ServiceContext, "출근 등록 실패", "출근 등록을 실패하였습니다.", "");
                                    }else{
                                        GenerateNotification.generateNotification(BLEScanService.ServiceContext, "퇴근 등록 실패", "퇴근 등록을 실패하였습니다.", "");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Request answer(sendEv)", "failed");
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
                Log.d("sendSocketData", "false");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("sendEventError(JSON)", e.getMessage());
        }
    }

    // More than api ver.19
    //@SuppressLint("NewApi")
    public void requestEssentialData(final int callbackType) {
        if (mSocket.connected()) {
            if (analyzer.serversPublicKey != null) {
                try {
                    JSONObject content = new JSONObject();
                    content.put("SmartphoneAddress", BLEScanService.myMacAddress);

                    mSocket.emit("requestEssentialData", analyzer.encryptSendJson(content));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("Awesometic", "requestEssentialData - exception caught (JSON envelopment)");
                }

            } else {
                Log.d("Awesometic", "requestEssentialData - server's public key is not initialized");
            }

            mSocket.on("data", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject resultJson = (JSONObject) args[0];
                        JSONObject contentJson = analyzer.extractContentFromReceivedJson(resultJson);

                        String[] beaconAddress = {
                                contentJson.getString("beacon_address").split("-")[0],
                                contentJson.getString("beacon_address").split("-")[1],
                                contentJson.getString("beacon_address").split("-")[2]
                        };

                        Map<String, String> map = new HashMap<>();
                        map.put("id_workplace", contentJson.getString("id_workplace"));
                        map.put("coordinateX", contentJson.getString("coordinateX"));
                        map.put("coordinateY", contentJson.getString("coordinateY"));
                        map.put("coordinateZ", contentJson.getString("coordinateZ"));
                        map.put("beacon_address1", beaconAddress[0]);
                        map.put("beacon_address2", beaconAddress[1]);
                        map.put("beacon_address3", beaconAddress[2]);

                        switch (callbackType) {
                            case SERVICE_CALLBACK:
                                BLEServiceUtils.addEssentialData(map);
                                for (int i = 0; i < 3; i++) {
                                    BLEServiceUtils.addFilterList(beaconAddress[i]);
                                }
                                break;

                            case ACTIVITY_CALLBACK:
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Awesometic", "requestEssentialData - exception caught (result analyze)");
                    }
                }
            });
        }
    }

    public void calibration(final Map<String, String> data){
        JSONObject obj = new JSONObject();
        try {
            if (mSocket.connected()) {
                Log.d("Socket", "connected");

                obj.put("BeaconDeviceAddress1", data.get("BeaconDeviceAddress1"));
                obj.put("BeaconDeviceAddress2", data.get("BeaconDeviceAddress2"));
                obj.put("BeaconDeviceAddress3", data.get("BeaconDeviceAddress3"));
                obj.put("BeaconData1", data.get("BeaconData1"));
                obj.put("BeaconData2", data.get("BeaconData2"));
                obj.put("BeaconData3", data.get("BeaconData3"));
                obj.put("SmartphoneAddress", data.get("SmartphoneAddress"));
                obj.put("DateTime", data.get("DateTime"));
                obj.put("CoordinateX", data.get("CoordinateX"));
                obj.put("CoordinateY", data.get("CoordinateY"));
                obj.put("CoordinateZ", data.get("CoordinateZ"));
                mSocket.emit("calibration", obj);

                // 서버에 등록이 되었는지 확인
                mSocket.on("answer", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject robj = (JSONObject) args[0];
                            String isSuccess = String.valueOf(robj.get("isSuccess"));
                            if(isSuccess.equals("true")) {
                                BLEScanService.failureCount_Cali = 0;
                                Log.d("Calibration", "Success");
                                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration 성공", "새로운 좌표 값이 등록되었습니다.", "");
                            }else{
                                if(BLEScanService.failureCount_Cali < 2) {
                                    BLEScanService.failureCount_Cali++;
                                    calibration(data);
                                    return;
                                }
                                else{
                                    Log.d("Calibration", "failed");
                                    BLEScanService.failureCount_Cali = 0;
                                    GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration 실패", "새로운 좌표 값 등록을 실패하였습니다.", "");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Request answer(Cali)", "failed");
                        }
                    }
                });

                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "Calibration", "Rssi 평균 값을 서버에 전송하였습니다." +
                                ".",
                        "rss1: " + data.get("CoordinateX") + ", " + "rssi2: " + data.get("CoordinateY") + ", " + "rss3: " + data.get("CoordinateZ"));
                Log.d("calibration data", data.get("BeaconDeviceAddress1") + ", " +
                        data.get("BeaconDeviceAddress2") + ", " +
                        data.get("BeaconDeviceAddress3") + ", " +
                        data.get("BeaconData1") + ", " +
                        data.get("BeaconData2") + ", " +
                        data.get("BeaconData3") + ", " +
                        data.get("SmartphoneAddress") + ", " +
                        //data.get("DateTime") + ", " +
                        data.get("CoordinateX") + ", " +
                        data.get("CoordinateY") + ", " +
                        data.get("CoordinateZ"));
                //MainActivity.sendData = true;

                //this.close();
            } else {
                Log.d("calibration", "false");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("CalibrationError(JSON)", e.getMessage());
        }
    }

    public void amIRegistered(String smartphoneAddress) {
        JSONObject obj = new JSONObject();

        try {
            if (mSocket.connected()) {
                obj.put("SmartphoneAddress", smartphoneAddress);

                mSocket.emit("amIRegistered", obj);
                mSocket.on("data", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject robj = (JSONObject) args[0];

                            boolean registered = Boolean.valueOf(String.valueOf(robj.get("registered")));
                            if (registered) {
                                String name = String.valueOf(robj.get("name"));
                                String employee_number = String.valueOf(robj.get("employee_number"));
                                boolean permitted = Boolean.valueOf(String.valueOf(robj.get("permitted")));

                                Log.d("Awesometic", "amIRegistered - true" + ", " + String.valueOf(permitted) + ", " + name + ", " + employee_number);
                                EntryActivity.amIRegistered = true;
                                EntryActivity.permitted = permitted;
                                EntryActivity.employee_name = name;
                                EntryActivity.employee_number = employee_number;
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
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("TestError(JSON)", e.getMessage());
        }
    }

    public void signupRequest(String smartphoneAddr, String employee_number, String name, String password, String department, String position) {
        JSONObject obj = new JSONObject();

        try {
            if (mSocket.connected()) {
                obj.put("SmartphoneAddress", smartphoneAddr);
                obj.put("EmployeeNumber", employee_number);
                obj.put("Name", name);
                obj.put("Password", password);
                obj.put("Department", department);
                obj.put("Position", position);
                obj.put("Permission", 0);
                obj.put("Admin", 0);

                mSocket.emit("signupRequest", obj);
                mSocket.on("answer", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject result = (JSONObject) args[0];
                            boolean requestSuccess = result.getBoolean("requestSuccess");

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public enum ChartSignal {
        POPULATION
    }

    public void requestChartData(ChartSignal signal) {
        try {
            JSONObject obj = new JSONObject();

            if (mSocket.connected()) {
                switch (signal) {
                    case POPULATION:
                        obj.put("SmartphoneAddress", BLEScanService.myMacAddress);
                        obj.put("Signal", signal.toString());
                        mSocket.emit("requestChartData", obj);

                        mSocket.on("data", new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                try {
                                    StringBuilder result = new StringBuilder(args[0].toString());
                                    result.toString().replace("\"", "\\\"");
                                    result.insert(0, "{ \"data\":");
                                    result.insert(result.length(), "}");

                                    Log.d("Awesometic", result.toString());
                                    JSONObject chartData = new JSONObject(result.toString());
                                    ChartFragment.chartData = chartData.getJSONArray("data");
                                    ChartFragment.chartDataReceived = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        break;
                    default:
                        break;
                }
            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isServersPublicKeyInitialized() {
        if (analyzer.serversPublicKey != null)
            return true;
        else
            return false;
    }

    public boolean connected() {
        if (mSocket.connected())
            return true;
        else
            return false;
    }

    // 소켓 닫기
    public void close(){
        mSocket.close();
    }
}