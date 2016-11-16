package com.example.taek.commutingchecker.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.taek.commutingchecker.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by TKs on 2016-11-15.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    Context context;
    ArrayList<HashMap<String,String>> noticeList; //공지사항 정보 담겨있음
    private MainFragment2 fragMain;

    public ItemAdapter(Context context, ArrayList<HashMap<String,String>> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cv_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        HashMap<String,String> noticeItem = noticeList.get(position);
        holder.tv_title.setText(noticeItem.get("title")); //제목
        holder.tv_content.setText(noticeItem.get("content")); //내용
        Log.e("onCreate1[title]", "" + noticeItem.get("title"));
        Log.e("onCreate1[content]", "" + noticeItem.get("content"));
        if(position==4) {
            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragMain = MainFragment2.newInstance();
                    AppCompatActivity activity = (AppCompatActivity) v.getContext();
                    FragmentManager fragmentManager = activity.getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragMain)
                            .detach(fragMain).attach(fragMain)
                            .addToBackStack(null).commit();

                    //MainFragment2 fragMain2 = new MainFragment2();
                    //activity.getSupportFragmentManager().beginTransaction()
                    //        .replace(R.id.EP_user_name, fragMain2)
                    //        .addToBackStack(null).commit();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return this.noticeList.size();
    }
    /** item layout 불러오기 **/
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_content;
        CardView cv;

        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_content = (TextView) v.findViewById(R.id.tv_content);
            cv = (CardView) v.findViewById(R.id.cv);

        }
    }
}