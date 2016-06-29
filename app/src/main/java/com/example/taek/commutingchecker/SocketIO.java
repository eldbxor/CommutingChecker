package com.example.taek.commutingchecker;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

/**
 * Created by Taek on 2016-04-15.
 */
public class SocketIO {
    //com.github.nkzawa.socketio.client.Socket mSocket;
    //io.socket.client.Socket mSocket;
    io.socket.client.Socket mSocket;

    // 생성자
    public SocketIO(){
        try{
            mSocket = IO.socket(Constants.SERVER_URL);
        }catch (URISyntaxException e){
            e.printStackTrace();
            Log.d("connectSocket", "Error");
        }
    }

    // 소켓 연결
    public void connect(){
        mSocket.connect();
    }

    // 이벤트 보내기
    public void sendEvent(Map<String, String> data) {
        JSONObject obj = new JSONObject();
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

                /*
                String str = "{ \"BeaconDeviceAddress1\":\"" + data.get("BeaconDeviceAddress1") + "\"," +
                        "\"BeaconDeviceAddress2\":\"" + data.get("BeaconDeviceAddress2") + "\"," +
                        "\"BeaconDeviceAddress3\":\"" + data.get("BeaconDeviceAddress3") + "\"," +
                        "\"BeaconData1\":\"" + data.get("BeaconData1") + "\"," +
                        "\"BeaconData2\":\"" + data.get("BeaconData2") + "\"," +
                        "\"BeaconData3\":\"" + data.get("BeaconData3") + "\"," +
                        "\"SmartphoneAddress\":\"" + data.get("SmartphoneAddress") + "\"," +
                        "\"DateTime\":\"" + data.get("DateTime") + "\" }";

                JSONObject obj_kitkat = new JSONObject(str); */

                mSocket.emit("circumstance", obj);
                Log.d("sendSocketData", "true");

                GenerateNotification.generateNotification(BLEScanService.ServiceContext, "서버에 데이터 전송", "서버에 데이터를 전송하였습니다.",
                        data.get("BeaconDeviceAddress1") + ", "
                        + data.get("BeaconDeviceAddress2") + ", "
                        + data.get("BeaconDeviceAddress3") + ", "
                        + data.get("BeaconData1") + ", "
                        + data.get("BeaconData2") + ", "
                        + data.get("BeaconData3") + ", "
                        + data.get("SmartphoneAddress"));
                        //+ ", " + data.get("DateTime"));

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
    public void requestEssentialData(){
        JSONObject obj = new JSONObject();
        try{
            if(mSocket.connected()){
                /*
                requestEssentialData
                {
                     SmartphoneAddress: '00:00:00:00:00:00',
                     DateTime: '0000/00/00 00:00:00'
                }
                 */
                obj.put("SmartphoneAddress", BLEScanService.myMacAddress);
                //obj.put("DateTime", CurrentTime.currentTime());
                mSocket.emit("requestEssentialData", obj);
                Log.d("requestEssentialData", "success");

                mSocket.on("data", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            final JSONArray jarray = new JSONArray(args[0].toString());
                            Log.d("jarray's length", String.valueOf(jarray.length()));
                            Log.d("request", args[0].toString());
                            for (int i = 0; i < jarray.length(); i++) {
                                JSONObject obj_listen = jarray.getJSONObject(i);
                                String str = obj_listen.getString("beacon_address");
                                String[] str_arr = str.split("-");
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("id_workplace", obj_listen.getString("id_workplace"));
                                map.put("coordinateX", obj_listen.getString("coordinateX"));
                                map.put("coordinateY", obj_listen.getString("coordinateY"));
                                map.put("coordinateZ", obj_listen.getString("coordinateZ"));
                                map.put("beacon_address1", str_arr[0]);
                                map.put("beacon_address2", str_arr[1]);
                                map.put("beacon_address3", str_arr[2]);
                                AddEssentialData.addEssentialData(map);
                                AddFilterList.addFilterList(str_arr[0]);
                                AddFilterList.addFilterList(str_arr[1]);
                                AddFilterList.addFilterList(str_arr[2]);
                                Log.d("addFilterList", str_arr[0] + ", " + str_arr[1] + ", " + str_arr[2]);
                                Log.d("FilterSizeInSocketIO", String.valueOf(BLEScanService.filterlist.size()));
                                if(Build.VERSION.SDK_INT >= 21) {
                                    BLEScanService.filters.clear();
                                    for (String deviceMacAddress : BLEScanService.filterlist) {
                                        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(deviceMacAddress).build();
                                        BLEScanService.filters.add(filter);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Receive Request_data", "fail");
                        }
                    }
                });
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void calibration(Map<String, String> data){
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
                //obj.put("DateTime", data.get("DateTime"));
                obj.put("CoordinateX", data.get("CoordinateX"));
                obj.put("CoordinateY", data.get("CoordinateY"));
                obj.put("CoordinateZ", data.get("CoordinateZ"));
                mSocket.emit("calibration", obj);
                Log.d("calibration", "success");

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

    public void amIRegisted(String dateTime, String smartphoneAddress){
        JSONObject obj = new JSONObject();
        try {
            if (mSocket.connected()) {
                Log.d("Socket", "connected");
                //obj.put("DateTime", dateTime);
                obj.put("SmartphoneAddress", smartphoneAddress);

                mSocket.emit("amIRegistered", obj);
                Log.d("amIRegistered", "success");

                mSocket.on("data", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject robj = (JSONObject) args[0];
                            String registered = String.valueOf(robj.get("registered"));
                            if(registered.equals("true")) {
                                String name = String.valueOf(robj.get("name"));
                                String employee_number = String.valueOf(robj.get("employee_number"));
                                Log.d("request", registered + ", " + name + ", " + employee_number);

                                MainActivity.amIRegistered = true;
                                MainActivity.employee_name = name;
                                MainActivity.employee_number = employee_number;
                            }else
                                MainActivity.amIRegistered = false;

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Receive Request_data", "fail");
                        }
                    }
                });
                //MainActivity.mScoket.close();
            } else {
                Log.d("amIRegistered", "fail");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("TestError(JSON)", e.getMessage());
        }
    }

    public void setValue(){
        JSONObject obj = new JSONObject();
        try {
            if (mSocket.connected()) {
                Log.d("Socket", "connected");

                obj.put("BeaconDeviceAddress1", "00:1A:7D:DA:71:07");
                obj.put("BeaconDeviceAddress2", "00:1A:7D:DA:71:03");
                obj.put("BeaconDeviceAddress3", "B8:27:EB:E1:47:EB");
                obj.put("BeaconData1", "f0 f4 c1 76 a6 23 42 ef ac 3a 66 f2 1a 11 99 3e 00 02 00 01");
                obj.put("BeaconData2", "bf 0c c4 a4 eb 9f 4f 06 b7 16 1f 5f f4 9a 8f 47 00 02 00 02");
                obj.put("BeaconData3", "70 9b d6 40 42 d1 4b 1a 99 0a 36 d4 a1 e5 27 d8 00 03 00 01");
                obj.put("SmartphoneAddress", BLEScanService.myMacAddress);
                //obj.put("DateTime", CurrentTime.currentTime());
                obj.put("CoordinateX", "-1");
                obj.put("CoordinateY", "-1");
                obj.put("CoordinateZ", "-1");
                mSocket.emit("calibration", obj);
                Log.d("test", "true");

            } else {
                Log.d("test", "false");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("TestError(JSON)", e.getMessage());
        }
    }

    // 소켓 닫기
    public void close(){
        mSocket.close();
    }
}