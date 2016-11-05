package com.example.taek.commutingchecker.ui;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.SocketIO;

import org.json.JSONArray;

/**
 * Created by Awesometic on 2016-07-02.
 */
public class EntryActivity extends AppCompatActivity {

    private SignupFragment fragSignup;
    private WaitFragment fragWait;

    public static JSONArray departmentListJsonArr;
    public static JSONArray positionListJsonArr;

    public static boolean amIRegistered;
    public static boolean permitted;

    public static boolean isGettingEmployeeDataFinished;
    public static boolean isSignupRequestSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Fragments
        fragSignup = SignupFragment.newInstance();
        fragWait = WaitFragment.newInstance();

        isGettingEmployeeDataFinished = false;
        isSignupRequestSuccess = false;

        FragmentManager fragmentManager = getFragmentManager();

        try {
            MainActivity.mSocket = new SocketIO(this);
            MainActivity.mSocket.connect(Constants.CALLBACK_TYPE_MAIN_ACTIVITY);

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
            int timerCount = 0;
            while (true) {
                if (timerCount == 100) {
                    Toast.makeText(this.getApplicationContext(), "서버와 연결할 수 없습니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_LONG).show();

                    MainActivity.mSocket.close();
                    MainActivity.mainActivity.finish();
                    this.finish();
                } else if (isGettingEmployeeDataFinished) {
                    break;
                } else {
                    Thread.sleep(100);
                    timerCount++;
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
