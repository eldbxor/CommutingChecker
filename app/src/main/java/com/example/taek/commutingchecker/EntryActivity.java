package com.example.taek.commutingchecker;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.socket.client.SocketIOException;

/**
 * Created by Awesometic on 2016-07-02.
 */
public class EntryActivity extends AppCompatActivity {

    private SignupFragment fragSignup;
    private WaitFragment fragWait;

    public static SocketIO mSocket;

    public static String smartphoneAddr;
    public static boolean amIRegistered;
    public static boolean permitted;
    public static String employee_number;
    public static String employee_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Fragments
        fragSignup = SignupFragment.newInstance();
        fragWait = WaitFragment.newInstance();

        FragmentManager fragmentManager = getFragmentManager();

        try {
            mSocket = new SocketIO();
            mSocket.connect();
            Thread.sleep(500);

            smartphoneAddr = MainActivity.myMacAddress;
            mSocket.amIRegistered(smartphoneAddr);
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!amIRegistered) {
            // If not registered
            fragmentManager.beginTransaction()
                    .replace(R.id.content_entry_frame, fragSignup)
                    .detach(fragSignup).attach(fragSignup)
                    .commit();
            MainActivity.mainActivity.finish();

        } else if (!permitted) {
            // If registered but not permitted
            fragmentManager.beginTransaction()
                    .replace(R.id.content_entry_frame, fragWait)
                    .detach(fragWait).attach(fragWait)
                    .commit();
            MainActivity.mainActivity.finish();

        } else {
            // If registered and permitted
            this.finish();

        }
    }

    @Override
    protected void onDestroy(){
        if (mSocket.connected())
            mSocket.close();
        super.onDestroy();
    }
}
