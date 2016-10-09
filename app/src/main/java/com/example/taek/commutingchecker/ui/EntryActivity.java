package com.example.taek.commutingchecker.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.SocketIO;

/**
 * Created by Awesometic on 2016-07-02.
 */
public class EntryActivity extends AppCompatActivity {

    private SignupFragment fragSignup;
    private WaitFragment fragWait;

    public static boolean amIRegistered;
    public static boolean permitted;

    public static boolean isGettingEmployeeDataFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Fragments
        fragSignup = SignupFragment.newInstance();
        fragWait = WaitFragment.newInstance();

        FragmentManager fragmentManager = getFragmentManager();

        isGettingEmployeeDataFinished = false;

        try {
            MainActivity.mSocket = new SocketIO();
            MainActivity.mSocket.connect();

            do {
                Thread.sleep(100);
            } while (MainActivity.mSocket.connected() == false);

            // Getting a public key from server
            MainActivity.mSocket.getServersRsaPublicKey();
            do {
                Thread.sleep(100);
            } while (MainActivity.mSocket.isServersPublicKeyInitialized() == false);

            MainActivity.mSocket.amIRegistered(MainActivity.myMacAddress);

            // Wait for receiving "amIRegistered" data through socket.io
            while (true) {
                Thread.sleep(100);

                if (isGettingEmployeeDataFinished) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (amIRegistered == false) {
            // If not registered
            fragmentManager.beginTransaction()
                    .replace(R.id.content_entry_frame, fragSignup)
                    .detach(fragSignup).attach(fragSignup)
                    .commit();
            MainActivity.mainActivity.finish();

        } else if (permitted == false) {
            // If registered but not permitted
            fragmentManager.beginTransaction()
                    .replace(R.id.content_entry_frame, fragWait)
                    .detach(fragWait).attach(fragWait)
                    .commit();
            MainActivity.mainActivity.finish();

        } else {
            // If registered and permitted
            MainActivity.tvNavHeadId.setText(MainActivity.employee_name + " (" + MainActivity.employee_number + ")");
            MainActivity.tvNavHeadAddr.setText(MainActivity.myMacAddress);
            this.finish();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
