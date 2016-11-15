package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taek.commutingchecker.R;

import java.util.ArrayList;

/**
 * Created by TKs on 2016-07-15.
 */
public class Other_customAdapt extends BaseAdapter {

    private ArrayList<Other_listItem> listViewItemList = new ArrayList<Other_listItem>() ;

    // ListViewAdapter의 생성자
    public Other_customAdapt() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.other_status_list_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView icon_ImageView = (ImageView) convertView.findViewById(R.id.other_ico) ;
        final TextView name_TextView = (TextView) convertView.findViewById(R.id.other_name) ;


        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final Other_listItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        icon_ImageView.setImageDrawable(listViewItem.getIcon());
        name_TextView.setText(listViewItem.getName());

        Button add_btn = (Button) convertView.findViewById(R.id.other_button) ;
        add_btn.setText(listViewItem.bgetName());

        add_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent intent = new Intent( context.getApplicationContext(), ShowEachInfo.class );
                //context.startActivity(intent);

            }
        });

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(Drawable icon, String name, Button btn, String bname) {
        Other_listItem item = new Other_listItem();

        item.setIcon(icon);
        item.setName(name);
        item.setBtn(btn);
        item.bsetName(bname);

        listViewItemList.add(item);
    }

}
