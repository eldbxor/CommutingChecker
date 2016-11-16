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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Awesometic on 2016-06-09.
 */
public class MainFragment extends Fragment {

    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rv;
    ArrayList<HashMap<String,String>> itemList;


    private static final String TAG_TITLE = "title";
    private static final String TAG_CONTENT = "content";

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
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
        itemList = new ArrayList<HashMap<String, String>>();
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        ArrayList<String> titles = new ArrayList<String>();
            titles.add(0,"금일 첫 입실");
            titles.add(1,"금일 마지막 퇴실");
            titles.add(2,"금일 근무시간");
            titles.add(3,"금일 초과근무");
            titles.add(4,"개인 정보 더 보기");



        //하드코딩용 데이터
        for(int i=0; i<5; i++) {
            String content = "TAG_CONTENT" + i;
            Log.d("check :" , titles.get(i));
            //HashMap에 붙이기
            HashMap<String,String> posts = new HashMap<String,String>();
            posts.put(TAG_TITLE,titles.get(i));
            if(i==4){
                posts.put(TAG_CONTENT,"클릭 시 이동");
            }else {
                posts.put(TAG_CONTENT, content);
            }
            //ArrayList에 HashMap 붙이기
            itemList.add(posts);
        }

        ItemAdapter adapter = new ItemAdapter(getActivity(),itemList);
        Log.e("onCreate[noticeList]", "" + itemList.size());


        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //TextView textview =/// (TextView) rootView.findViewById(R.id.cv_text);
        //textview.setText("hello?");

        return rootView;
    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}