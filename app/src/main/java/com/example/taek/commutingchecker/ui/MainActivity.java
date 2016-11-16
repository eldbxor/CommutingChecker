package com.example.taek.commutingchecker.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.example.taek.commutingchecker.services.BLEScanService;
import com.example.taek.commutingchecker.services.CalibrationService;
import com.example.taek.commutingchecker.utils.BackPressCloseHandler;
import com.example.taek.commutingchecker.R;
import com.example.taek.commutingchecker.utils.Constants;
import com.example.taek.commutingchecker.utils.IncomingHandler;
import com.example.taek.commutingchecker.utils.RegisterReceiver;
import com.example.taek.commutingchecker.utils.SocketIO;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    /** 2016. 6. 9
     * Migrating views from MainActivity to this SetupFragment
     * Reference: https://github.com/awesometic/facetalk_android
     */

    // Initialize it to exit from EntryActivity
    public static AppCompatActivity mainActivity;

    // Close the app when back button twice pressed
    private BackPressCloseHandler backPressCloseHandler;

    public static Context MainActivityContext;
    public static String ServiceTAG;
    public static SocketIO mSocket;
    public static String myMacAddress;
    public static Messenger messenger;
    public static Activity activity;
    public static ServiceConnection conn;
    public static ComponentName serviceComponentName;
    public static Handler unbindHandler;
    BroadcastReceiver ComeToWorkStateReceiver, LeaveWorkStateReceiver, StandByComeToWorkStateReceiver;

    // Target we publish for clients to send messages to IncomingHandler.
    public static Messenger incomingMessenger;

    // Employee information
    public static TextView tvNavHeadId;
    public static TextView tvNavHeadAddr;
    public static String employee_number;
    public static String employee_name;

    // String of Calibration's result
    public static String strOfCalibrationResult;

    public static void connectMessenger(){
        Log.d("MainActivity method", "call connectMessenger");
        ComponentName cn = new ComponentName(MainActivity.MainActivityContext, CalibrationService.class);
        Intent intent = new Intent();
        intent.setComponent(cn);

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("MessengerCommunication", "connected to service");
                MainActivity.messenger = new Messenger(service);
                CalibrationFragment.timerStart();

                CalibrationFragment.btnCalibrationStart.setText("RESET");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // MainActivity.mainActivity.finish();
            }
        };

        MainActivity.MainActivityContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** 2016. 7. 1
         * Add Fabric Crash Reporter by Awesometic
         * Official Site: https://www.fabric.io/home
         * Documents: https://docs.fabric.io/android/fabric/overview.html
         * If you want to see the reports, you should join Fabric and tell me your email address
         * To build this properly, you have to install the plugin named "Fabric for Android Studio" at Settings->Plugins
         */
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        incomingMessenger = new Messenger(new IncomingHandler(Constants.HANDLER_TYPE_ACTIVITY, MainActivityContext));

        /** 2016. 6. 9
         * Init UI Elements including navigation view
         */
        initUIElements();

        MainActivityContext = this;
        ServiceTAG = getResources().getString(R.string.scan_service);
        messenger = null;
        activity = MainActivity.this;
        unbindHandler = new Handler();

        // Go to EntryActivity, and check whether this smart phone is registered or not
        mainActivity = this;
        myMacAddress = android.provider.Settings.Secure.getString(this.getContentResolver(), "bluetooth_address");
        Log.d("myMacAddress", myMacAddress);

        Intent intent = new Intent(MainActivity.this.getApplicationContext(), EntryActivity.class);
        startActivity(intent);

        // 리시버 등록
        RegisterReceiver mRegisterReceiver = new RegisterReceiver(MainActivityContext);
        ComeToWorkStateReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_COME_TO_WORK_STATE);
        LeaveWorkStateReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_LEAVE_WORK_STATE);
        StandByComeToWorkStateReceiver = mRegisterReceiver.createReceiver(Constants.BROADCAST_RECEIVER_TYPE_STAND_BY_COME_TO_WORK_STATE);
        registerReceiver(ComeToWorkStateReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_COME_TO_WORK_STATE));
        registerReceiver(LeaveWorkStateReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_LEAVE_WORK_STATE));
        registerReceiver(StandByComeToWorkStateReceiver, mRegisterReceiver.createPackageFilter(Constants.BROADCAST_RECEIVER_TYPE_STAND_BY_COME_TO_WORK_STATE));

        // BLE 관련 Permission 주기
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Android M Permission check
            if(this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton("Ok", null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        // BLE를 지원하지 않는 디바이스 경우 강제 종료
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE를 지원하지 않는 디바이스입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            //Toast.makeText(this, "BLE를 지원하는 디바이스입니다.", Toast.LENGTH_SHORT).show();
        }

        // 서비스 실행
        //startService(intent);

//        try{
//            Thread.sleep(1000);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int reqeustCode, String permission[], int[] grantResults){
        switch (reqeustCode){
            case PERMISSION_REQUEST_COARSE_LOCATION:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("permission", "coarse location permission granted");
                }else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, " +
                            "this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton("Ok", null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }


    /** 2016. 6. 9
     * Member variables and methods comes with implementing navigation view
     */

    /* DrawerLayout object */
    private DrawerLayout drawerLayout;

    /* Fragments objects */
    private MainFragment fragMain;
    public SetupFragment fragSetup;
    private ChartFragment fragChart;
    private Other_attendFragment fragOtherattend;

    /* Navigation View object */
    private NavigationView navigationView;

    private void initUIElements() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fragments
        fragMain = MainFragment.newInstance();
        fragSetup = SetupFragment.newInstance();
        fragChart = ChartFragment.newInstance();
        fragOtherattend = Other_attendFragment.newInstance();

        // DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        // Navigation View
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set the header of the Navigation View
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header);
        tvNavHeadId = (TextView) navHeaderView.findViewById(R.id.nav_head_id);
        tvNavHeadAddr = (TextView) navHeaderView.findViewById(R.id.nav_head_bluetooth_addr);
        tvNavHeadId.setText("employee id here");
        tvNavHeadAddr.setText("employee bluetooth address here");

        // Set the menu of the Navigation View
        navigationView.inflateMenu(R.menu.nav_menu);
        int check = navigationView.getMenu().size();

        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragMain)
                .detach(fragMain).attach(fragMain)
                .commit();
    }

    /* Essential overriding methods */
    @Override
    public void onBackPressed() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // super.onBackPressed();
            backPressCloseHandler.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();

        ///noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            moveTaskToBack(true);
            finish();
        }else if(id == R.id.admin_setup){
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragSetup)
                    .detach(fragSetup).attach(fragSetup)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fragmentManager = getFragmentManager();
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_main:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragMain)
                        .detach(fragMain).attach(fragMain)
                        .commit();
                break;
            case R.id.nav_setup:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragSetup)
                        .detach(fragSetup).attach(fragSetup)
                        .commit();
                break;
            case R.id.nav_population_of_each_department:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragChart)
                        .detach(fragChart).attach(fragChart)
                        .commit();
                break;
            case R.id.nav_Otherattend:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragOtherattend)
                        .detach(fragOtherattend).attach(fragOtherattend)
                        .commit();
                break;
            case R.id.attendence_each_people:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragChart)
                        .detach(fragChart).attach(fragChart)
                        .commit();
                break;
            default:
                break;
        }

        // Change title on appbar
        if (id == R.id.nav_main)
            setTitle(R.string.app_name);
        else
            setTitle(item.getTitle());

        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(ComeToWorkStateReceiver);
        unregisterReceiver(LeaveWorkStateReceiver);
        unregisterReceiver(StandByComeToWorkStateReceiver);

        if (conn != null) {
            unbindService(conn);
            conn = null;
        }

        try {
            mSocket.close();
            // unbindService(conn);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
