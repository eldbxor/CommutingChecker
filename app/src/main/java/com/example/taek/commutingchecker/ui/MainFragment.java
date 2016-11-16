package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.SocketIO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Awesometic on 2016-06-09.
 */
public class MainFragment extends Fragment {

    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rv;
    ArrayList<HashMap<String,String>> itemList;

    public static JSONObject todayCommuteInfoJson = new JSONObject();
    public static boolean todayCommuteInfoReceived;

    private static final String TAG_TITLE = "title";
    private static final String TAG_CONTENT = "content";

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        todayCommuteInfoReceived = false;
        return fragment;
    }


    public MainFragment() {   }

    //    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main1, container, false);
        itemList = new ArrayList<>();
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        try {
            if (MainActivity.mSocket == null) {
                MainActivity.mSocket = new SocketIO(getActivity());
            }
            if (MainActivity.mSocket.connected() == false) {
                MainActivity.mSocket.connect(Constants.CALLBACK_TYPE_MAIN_ACTIVITY);
                do {
                    Thread.sleep(100);
                } while (MainActivity.mSocket.connected() == false);
            }

            if (MainActivity.mSocket.isServersPublicKeyInitialized() == false) {
                MainActivity.mSocket.getServersRsaPublicKey();
                do {
                    Thread.sleep(100);
                } while (MainActivity.mSocket.isServersPublicKeyInitialized() == false);
            }

            MainActivity.mSocket.requestTodayCommuteInfo();
            do {
                Thread.sleep(100);
            } while (todayCommuteInfoReceived == false);
            todayCommuteInfoReceived = false;

            ArrayList<String> titles = new ArrayList<>();
            titles.add(0,"오늘 첫 입실");
            titles.add(1,"오늘 마지막 퇴실");
            titles.add(2,"오늘 근무시간");
            titles.add(3,"오늘 초과근무");
            titles.add(4,"사용자 정보 더 보기");

            ArrayList<String> contents = new ArrayList<>();

            // userList, workplaceList, departmentList, positionList
            JSONArray userListJsonArray = todayCommuteInfoJson.getJSONArray("userList");

            for (int i = 0; i < userListJsonArray.length(); i++) {
                if (userListJsonArray.getJSONObject(i).getString("smartphoneAddress").equals(MainActivity.myMacAddress)) {
                    JSONObject currentUserCommuteJson = userListJsonArray.getJSONObject(i);

                    contents.add(0, convertMsecToReadableFormat(Integer.parseInt(currentUserCommuteJson.getString("firstComeInTime"))));
                    contents.add(1, convertMsecToReadableFormat(Integer.parseInt(currentUserCommuteJson.getString("lastComeOutTime"))));
                    contents.add(2, convertMsecToReadableFormat(Integer.parseInt(currentUserCommuteJson.getString("validWorkingTime"))));
                    contents.add(3, convertMsecToReadableFormat(Integer.parseInt(currentUserCommuteJson.getString("validOverWorkingTime"))));

                    break;
                }
            }

            for(int i = 0; i < 5; i++) {
                HashMap<String,String> posts = new HashMap<>();

                posts.put(TAG_TITLE,titles.get(i));
                if(i == 4){
                    posts.put(TAG_CONTENT,"클릭 시 이동");
                } else {
                    posts.put(TAG_CONTENT, contents.get(i));
                }

                itemList.add(posts);
            }

            ItemAdapter adapter = new ItemAdapter(getActivity(),itemList);
            Log.e("onCreate[noticeList]", "" + itemList.size());

            rv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    // TODO: function below may be quite used at other source files, need to move into other class file used as shared library
    private String convertMsecToReadableFormat(int msec) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(msec),
                TimeUnit.MILLISECONDS.toMinutes(msec) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(msec) % TimeUnit.MINUTES.toSeconds(1));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}