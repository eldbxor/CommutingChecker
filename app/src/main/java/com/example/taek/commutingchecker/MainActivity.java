package com.example.taek.commutingchecker;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    /** 2016. 6. 9
     * Migrating views from MainActivity to this SetupFragment
     * Reference: https://github.com/awesometic/facetalk_android
     */

    public static String ServiceTAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ServiceTAG = getResources().getString(R.string.scan_service);

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

        /** 2016. 6. 9
         * Init UI Elements including navigation view
         */
        initUiElements();
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

        Log.d("connect in mainactivity", "true");
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


    /** 2016. 06. 09
     * Member variables and methods comes with implementing navigation view
     */

    /* DrawerLayout object */
    private DrawerLayout drawerLayout;

    /* Fragments objects */
    private MainFragment fragMain;
    private WebFragment fragWeb;
    private SetupFragment fragSetup;

    /* Navigation View object */
    private NavigationView navigationView;

    private void initUiElements() {
        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fragments
        fragMain = MainFragment.newInstance();
        fragWeb = WebFragment.newInstance();
        fragSetup = SetupFragment.newInstance();

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
        TextView tvNavHeadId = (TextView) navHeaderView.findViewById(R.id.nav_head_id);
        TextView tvNavHeadAddr = (TextView) navHeaderView.findViewById(R.id.nav_head_bluetooth_addr);
        tvNavHeadId.setText("employee id here");
        tvNavHeadAddr.setText("employee bluetooth address here");

        // Set the menu of the Navigation View
        navigationView.inflateMenu(R.menu.nav_menu);

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
            super.onBackPressed();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            moveTaskToBack(true);
            finish();
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
            case R.id.nav_web1:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragWeb)
                        .detach(fragWeb).attach(fragWeb)
                        .commit();
                break;
            case R.id.nav_web2:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragWeb)
                        .detach(fragWeb).attach(fragWeb)
                        .commit();
                break;
            case R.id.nav_web3:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragWeb)
                        .detach(fragWeb).attach(fragWeb)
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
}
