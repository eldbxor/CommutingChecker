package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.taek.commutingchecker.R;

import org.w3c.dom.Text;

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
        View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
        itemList = new ArrayList<HashMap<String, String>>();
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        //하드코딩용 데이터
        for(int i=0; i<10; i++) {
            String title = "TAG_TITLE" + i;
            String content = "TAG_CONTENT" + i;

            //HashMap에 붙이기
            HashMap<String,String> posts = new HashMap<String,String>();
            posts.put(TAG_TITLE,title);
            posts.put(TAG_CONTENT, content);

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