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

//    hardcording 용 배열
    static String[] h_name = new String[5];
    static String[] h_state = new String[5];

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

        h_name[0] = "양덕규";
        h_name[1] = "이유택";
        h_name[2] = "백소영";
        h_name[3] = "강은정";
        h_name[4] = "김선광";

        h_state[0] ="입실";
        h_state[1] ="입실";
        h_state[2] ="퇴실";
        h_state[3] ="퇴실";
        h_state[4] ="퇴실";

        for(int i=0; i<5; i++)
        {
            item = new Other_listItem();
            item.setIcon(ContextCompat.getDrawable(rootView.getContext(), R.drawable.logo));
            item.setName("이름: "+ h_name[i]);
            item.bsetName( h_state[i].toString());

            adapter.addItem(item.getIcon(),item.getName(),item.getBtn(),item.bgetName());

            data.add(item);

        }
        list.setAdapter(adapter);



        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public Button[] getAdd_btn() {
        return add_btn;
    }
}