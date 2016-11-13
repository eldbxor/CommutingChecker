package com.example.taek.commutingchecker.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.widget.Button;

/**
 * Created by TKs on 2016-07-15.
 */
public class Other_listItem {
    private Drawable iconDrawable ;
    private String textStr ,bntextStr;
    private Button btn;

    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setName(String text) {
        textStr = text ;
    }
    public void setBtn(Button button) {
        btn = button;
    }
    public void bsetName(String text) {bntextStr= text; }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getName() {
        return this.textStr ;
    }
    public String bgetName() { return this.bntextStr;}
    public Button getBtn() { return this.btn ;}

}
