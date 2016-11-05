package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.taek.commutingchecker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Awesometic on 2016-06-09.
 */
public class Other_attendFragment extends Fragment {

    private View rootView;
    ListView list;
    List<Other_listItem> data;
    Button[] add_btn;
    Other_customAdapt adapter;
    Other_listItem item;


    public static Other_attendFragment newInstance() {
        Other_attendFragment fragment = new Other_attendFragment();
        return fragment;
    }

    public Other_attendFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_otherattend, container, false);
        adapter = new Other_customAdapt();
        list = (ListView) rootView.findViewById(R.id.other_status);

        data = new ArrayList<Other_listItem>();



        for(int i=0; i<100; i++)
        {
            item = new Other_listItem();
            item.setIcon(ContextCompat.getDrawable(rootView.getContext(), R.drawable.commuting));
            item.setName("check: "+ i);

            adapter.addItem(item.getIcon(),item.getName(),item.getBtn());
            data.add(item);

        }
        list.setAdapter(adapter);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}