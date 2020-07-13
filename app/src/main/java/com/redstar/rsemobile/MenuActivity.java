package com.redstar.rsemobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.redstar.rsemobile.Tracking.TrackingActivity;

import db.DataDB;

public class MenuActivity extends Activity {
    Button btnScan, btnPOD, btnUpload, btnPickUp, btnTracking, btnCHMPOD ;
    TextView txtUserDetails;
    LinearLayout UserDetails;
    private ProgressDialog mProgressView;
    DataDB db;
    int pendingUploadCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        db=new DataDB();

        Global.globalBatchStatus = false;
        //start gps service
        //stopService(new Intent(MenuActivity.this, GPSService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            String[] per = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(per, 1);


            if (ContextCompat.checkSelfPermission(MenuActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {


                startService(new Intent(MenuActivity.this, GPSService.class));
            }

        }else{

            startService(new Intent(MenuActivity.this, GPSService.class));
        }

        //startService(new Intent(MenuActivity.this, LocationService.class));

        // start data sychronisation service
       // stopService(new Intent(MenuActivity.this, SyncData.class));
        startService(new Intent(MenuActivity.this, SyncData.class));

        // load Scans activity
        btnScan=(Button)findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, scanActivity.class);
                startActivity(i);
                //
            }
        } );
        // load POD activity
        btnPOD=(Button)findViewById(R.id.btnPod);
        btnPOD.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, podActivity.class);
                startActivity(i);
                //
            }
        } );
        // load upload activity
        btnUpload=(Button)findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, uploadActivity.class);
                startActivity(i);
                //
            }
        } );
        // load tracking activity
        btnTracking=(Button)findViewById(R.id.btnTracking);
        btnTracking.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, TrackingActivity.class);
                startActivity(i);
                //
            }
        } );
                // load Pickup activity
        btnPickUp=(Button)findViewById(R.id.btnPickUp);
        btnPickUp.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, PickupActivity.class);
                startActivity(i);
                //
            }
        } );

        // load CHEMONICS POD activity
        btnCHMPOD=(Button)findViewById(R.id.btnCHMPOD);
        btnCHMPOD.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(MenuActivity.this, chemonicsPodActivity.class);
                startActivity(i);
                //
            }
        } );

        // set layout controls
        UserDetails = (LinearLayout)findViewById(R.id.UserDetailsLayout);
        txtUserDetails = (TextView) findViewById(R.id.txtUserDetials);


            if(Global.globalUserName.toString().equals("admin")){
                Cursor dcursor = db.getUserList(MenuActivity.this);
                UserDetails.setVisibility(View.VISIBLE);
                if (dcursor.getCount() > 0) {
                    if (dcursor.moveToFirst()) {
                        do {
                            // Get each items
                            String username = dcursor.getString(dcursor.getColumnIndex("username"));
                            String Password = dcursor.getString(dcursor.getColumnIndex("password"));
                            String DeviceType = dcursor.getString(dcursor.getColumnIndex("DeviceType"));
                            txtUserDetails.setText("UserName : " + username.toString() +
                                                   " Password : " + Password.toString() +
                                                   " Device Type : " +  DeviceType.toString());
                        } while (dcursor.moveToNext());
                        dcursor.close();
                    }
                } else {
                    dcursor.close();
                }
            }else{
                UserDetails.setVisibility(View.INVISIBLE);
            }

            //get pending uploads
            pendingUploadCount();
            btnUpload.setText("Pending Upload (" + pendingUploadCount + ")");

    }

    public void showSettingsAlert() {

        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;


        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS Not Enabled!!");
            dialog.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                    Global.globalLocationEnabled=true;
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }

       /*  String provider = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
        sendBroadcast(intent);
        Global.globalLocationEnabled=true;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                Global.globalLocationEnabled=true;
            }
        });



        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
        */
    }

    public void pendingUploadCount()
    {
        int podCount = 0;
        int scanCount = 0;
        Cursor podCursor = db.getPODNotUploaded(MenuActivity.this);
        Cursor ScanCursor = db.getSCANSNotUploaded(MenuActivity.this);



        podCount = podCursor.getCount();

        scanCount = ScanCursor.getCount();

        pendingUploadCount = podCount + scanCount;

        podCursor.close();
        ScanCursor.close();


    }

/*
    public void showSettingsAlert() {
        //Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
        Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", true);
       sendBroadcast(intent);
        Global.globalLocationEnabled=true;

        String provider = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.sendBroadcast(poke);
        }

        if(provider.contains("network")){ //if gps is enabled and network location is available
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("0"));
            sendBroadcast(poke);
        }


    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MenuActivity.this);

    // Setting Dialog Title
        alertDialog.setTitle("GPS settings");
    // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

    // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            Global.globalLocationEnabled=true;
        }
    });



    // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    });

    // Showing Alert Message
        alertDialog.show();

}
*/
@Override
protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();

        Global.globalBatchStatus = false;
    //get pending uploads
    pendingUploadCount();
    btnUpload.setText("Pending Upload (" + pendingUploadCount + ")");
}
}
