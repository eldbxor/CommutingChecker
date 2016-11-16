package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taek.commutingchecker.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Awesometic on 2016-06-09.
 */
public class MainFragment2 extends Fragment {

    public static MainFragment2 newInstance() {
        MainFragment2 fragment = new MainFragment2();
        return fragment;
    }


    public MainFragment2() {   }

//    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main2, container, false);

        return rootView;
    }





    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}