package com.redstar.rsemobile;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import db.DataDB;

public class uploadActivity extends AppCompatActivity {
    private TextView lblTotalPOD, lblTotoalScans,lblTotalPickUpCount,lblSync,lblTotalSignatureCount,lblTotalCHMPOD;
    private Button btnUpload;
    private ImageView imgSyncing;
    private ProgressDialog mProgressView;
    manualUpload upData =   new manualUpload();
    String jsonStr;
    String sql;
    String retMsg,Uploadtype;
    Thread uploadThread;


    DataDB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        db=new DataDB();

        Global.UploadMessage = "Upload Complete";

        // show progress
        //mProgressView = ProgressDialog.show(uploadActivity.this, "", "Please wait...", true);
        // set controls
        lblTotalPickUpCount = (TextView) findViewById(R.id.txtUploadPickUpCount);
        lblTotoalScans = (TextView) findViewById(R.id.txtUploadScanCount);
        lblTotalPOD = (TextView) findViewById(R.id.txtUploadPODCount);
        lblTotalSignatureCount=(TextView) findViewById(R.id.txtUploadSignatureCount);
        lblTotalCHMPOD=(TextView) findViewById(R.id.txtCHMPOD);
        lblSync = (TextView) findViewById(R.id.lblSync);
        imgSyncing = (ImageView) findViewById(R.id.imgSync);
        // display available records to transfer



        //handler.sendEmptyMessage(0);
        imgSyncing.setVisibility(View.INVISIBLE);
        lblSync.setVisibility(View.INVISIBLE);
        btnUpload= (Button) findViewById(R.id.btnUpload);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mProgressView =  new ProgressDialog(uploadActivity.this);//ProgressDialog.show(uploadActivity.this, "", "Please wait...", true);

               if (isMyServiceRunning(SyncData.class)) {
                    //btnUpload.setEnabled(false);
                    btnUpload.setVisibility(View.INVISIBLE);
//                    imgSyncing.setVisibility(View.INVISIBLE);
//                    lblSync.setVisibility(View.INVISIBLE);





                   //final ProgressDialog dialog = new ProgressDialog(uploadActivity.this);
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           mProgressView.setTitle("Uploading");
                           mProgressView.setMessage("Please Wait!!");
                           mProgressView.setCancelable(true);
                           mProgressView.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                           mProgressView.show();
                           if(!mProgressView.isShowing()){
                               mProgressView.show();
                           }
                       }
                   });

                    //stopService(new Intent(uploadActivity.this, SyncData.class));
                   //uploadPOD();

                     uploadThread = new Thread() {

                       // @Override
                        public void run() {
                            db=new DataDB();
                            uploadPOD();
                            try {


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressView.dismiss();
                                        Global.AssetDialog(Global.UploadMessage, uploadActivity.this).create().show();

                                        btnUpload.setVisibility(View.VISIBLE);
                                    }
                                });
                                            // update TextView here!

//                                            uploadCHMPOD();
//                                            uploadPICKUP();
//                                            uploadSCANS();
//                                            uploadSignature();


                                //}
                            } catch (Exception e) {
                                //uploadThread.interrupt();
                                uploadThread.currentThread().interrupt();
                            }

                            //uploadThread.currentThread().interrupt();

                        }
                    };

                   uploadThread.start();



                   //mProgressView.dismiss();

                }else{
                    btnUpload.setVisibility(View.INVISIBLE);
//                    imgSyncing.setVisibility(View.INVISIBLE);
//                    lblSync.setVisibility(View.INVISIBLE);

                    //startService(new Intent(uploadActivity.this, SyncData.class));
                }

               //handler.sendEmptyMessage(0);
            }
        });

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(10000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update TextView here!
                                loadAvailableRecordsToUpload();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();



        // add click listener to all the value
        lblTotalPickUpCount.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (!lblTotalPickUpCount.getText().equals("0")) {
                    Global.globalDataListOpertionType = "PICKUP";
                    Global.globalDataListOpertionCode = "WEIGHT";
                    Intent i = new Intent(uploadActivity.this, DataListActivity.class);
                    i.putExtra("Operation", "PICKUP");
                    startActivity(i);
                }
                //
            }
        } );
        lblTotoalScans.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!lblTotoalScans.getText().equals("0")) {
                    Global.globalDataListOpertionType = "SCAN";
                    Global.globalDataListOpertionCode = "SCAN STATUS";
                    Intent i = new Intent(uploadActivity.this, DataListActivity.class);
                    i.putExtra("Operation", "SCAN");
                    startActivity(i);
                }
                //
            }
        } );
        lblTotalPOD.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (!lblTotalPOD.getText().equals("0")) {
                    Global.globalDataListOpertionType = "POD";
                    Global.globalDataListOpertionCode = "POD";
                    Intent i = new Intent(uploadActivity.this, DataListActivity.class);
                    i.putExtra("Operation", "POD");
                    startActivity(i);
                }
                //
            }
        } );
    }


    private void loadAvailableRecordsToUpload() {
        // database handler
        //DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db = new DataDB();
        // load avalable scans
        lblTotoalScans.setText(db.getAvailableScanCount(uploadActivity.this));
        lblTotalPOD.setText(db.getAvailablePODCount(uploadActivity.this));
        lblTotalPickUpCount.setText(db.getAvailablePickUpCount(uploadActivity.this));
        lblTotalSignatureCount.setText(db.getAvailableSignatureCount(uploadActivity.this));
        lblTotalCHMPOD.setText(db.getAvailableCHMPODCount(uploadActivity.this));
        //imgSyncing.setVisibility(View.VISIBLE);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    public void uploadPOD()
    {
        // SECTION POD SYNC
        Uploadtype = "";
        Uploadtype = "POD";

        Cursor cursor = db.getPODNotUploaded(uploadActivity.this);
        int cCount = 0;
        cCount = cursor.getCount();
        Log.d("POD COUNT : ", String.valueOf(cCount));
        if (cursor.getCount() > 0)
        {
            // POD RECORD EXIST
            if (cursor.moveToFirst()) {
                do {
                    // Get each items
                    String AwbNo = cursor.getString(cursor.getColumnIndex("waybillnumber"));
                    String pod = cursor.getString(cursor.getColumnIndex("pod"));
                    String podby = cursor.getString(cursor.getColumnIndex("podby"));
                    String dexcodeid = cursor.getString(cursor.getColumnIndex("dexcodeid"));
                    String first_date = cursor.getString(cursor.getColumnIndex("first_date"));
                    String poddate = cursor.getString(cursor.getColumnIndex("poddate"));
                    String OriginStation = cursor.getString(cursor.getColumnIndex("OriginStation"));
                    String pod_post_date = cursor.getString(cursor.getColumnIndex("pod_post_date"));
                    String DeliveryRemarks = cursor.getString(cursor.getColumnIndex("DeliveryRemarks"));
                    String Amount = cursor.getString(cursor.getColumnIndex("Amount"));


                    //if (upData==null){
                    upData = new manualUpload();
                    upData.execute(Uploadtype,Global.globalDeviceIMEI,Global.globalUserName,
                            AwbNo,pod,podby,dexcodeid,first_date,
                            poddate,OriginStation,pod_post_date,DeliveryRemarks,Amount);
                    //}

                    Global.globalSyncPOD = true;

                } while (cursor.moveToNext());

                cursor.close();
//

                Global.globalSyncPOD = false;
            }

        } else {
            cursor.close();
            Global.globalSyncPOD = false;
        }
        uploadCHMPOD();

    }
    // chemonics POD upload

    public void uploadCHMPOD()
    {
        // SECTION CHM POD SYNC
        Uploadtype = "";
        Uploadtype = "CHMPOD";

        Cursor cursor = db.getCHMPODNotUploaded(uploadActivity.this);
        int cCount = 0;
        cCount = cursor.getCount();
        Log.d("CHM POD COUNT : ", String.valueOf(cCount));
        if (cursor.getCount() > 0)
        {
            // POD RECORD EXIST
            if (cursor.moveToFirst()) {
                do {
                    // Get each items
                    String AwbNo = cursor.getString(cursor.getColumnIndex("WayBillNumber"));
                    String ReceivedBy = cursor.getString(cursor.getColumnIndex("ReceivedBy"));
                    String DateReceived = cursor.getString(cursor.getColumnIndex("DateReceived"));
                    String ServiceType = cursor.getString(cursor.getColumnIndex("ServiceType"));


                    //if (upData==null){
                    upData = new manualUpload();
                    upData.execute(Uploadtype,Global.globalDeviceIMEI,Global.globalUserName,AwbNo,ReceivedBy,DateReceived,ServiceType);
                    //}

                    Global.globalSyncCHMPOD = true;


                } while (cursor.moveToNext());

                cursor.close();
//

                Global.globalSyncCHMPOD = false;
            }

        } else {
            cursor.close();
            Global.globalSyncCHMPOD = false;
        }

        uploadSCANS();
    }
    //upload scans
    public void uploadSCANS()
    {
        // SECTION SCANS SYNC
        Uploadtype = "";
        Uploadtype = "SCANS";


        Cursor dcursor = db.getSCANSNotUploaded(uploadActivity.this);
        int cCount = 0;
        cCount = dcursor.getCount();
        Log.d("SCAN COUNT : ", String.valueOf(cCount));
        if (dcursor.getCount() > 0)
        {
            // SCAN RECORD EXIST
            if (dcursor.moveToFirst()) {
                do {
                    // Get each items

                    String AwbNo = dcursor.getString(dcursor.getColumnIndex("AWBNO"));
                    String Scan_Status = dcursor.getString(dcursor.getColumnIndex("SCAN_STATUS"));
                    String Origin = dcursor.getString(dcursor.getColumnIndex("ORIGIN"));
                    String Destination = dcursor.getString(dcursor.getColumnIndex("DESTINATION"));
                    String Date = dcursor.getString(dcursor.getColumnIndex("DATE"));
                    String Weight = dcursor.getString(dcursor.getColumnIndex("WEIGHT"));
                    String BatchNo = dcursor.getString(dcursor.getColumnIndex("BATCHNO"));
                    String Pieces = dcursor.getString(dcursor.getColumnIndex("PIECES"));
                    String SealNo = dcursor.getString(dcursor.getColumnIndex("SEALNO"));
                    String VehicleNo = dcursor.getString(dcursor.getColumnIndex("VEHICLENO"));
                    String Tag = dcursor.getString(dcursor.getColumnIndex("TAG"));
                    String Express_Centre_Code = dcursor.getString(dcursor.getColumnIndex("EXPRESS_CENTRE_CODE"));
                    String Content_Type = dcursor.getString(dcursor.getColumnIndex("CONTENT_TYPE"));
                    String Route = dcursor.getString(dcursor.getColumnIndex("ROUTE"));
                    String OpCode = dcursor.getString(dcursor.getColumnIndex("OpCode"));

                    upData = new manualUpload();
                    upData.execute(Uploadtype, Global.globalDeviceIMEI, Global.globalUserName,
                            AwbNo, Scan_Status, Origin, Destination, Date,
                            Weight, BatchNo, Pieces, SealNo,
                            VehicleNo, Tag, Express_Centre_Code, Content_Type, Route, OpCode);
                    Global.globalSyncScans=true;


                } while (dcursor.moveToNext());
            }
            dcursor.close();
//
            Global.globalSyncScans=false;
        } else {
            dcursor.close();
            Global.globalSyncScans=false;
        }
        uploadPICKUP();


    }
    public void uploadPICKUP()
    {
        // SECTION PICKUP SYNC
        Uploadtype = "";
        Uploadtype = "PICKUP";

        Cursor dcursor = db.getPickUPNotUploaded(uploadActivity.this);
        int cCount = 0;
        cCount = dcursor.getCount();
        Log.d("PICKUP COUNT : ", String.valueOf(cCount));
        if (dcursor.getCount() > 0)
        {
            // SCAN RECORD EXIST
            if (dcursor.moveToFirst()) {
                do {
                    // Get each items
                    String AcctNo = dcursor.getString(dcursor.getColumnIndex("ACCTNO"));
                    String AwbNo = dcursor.getString(dcursor.getColumnIndex("AWBNO"));
                    String COMPANY_NAME = dcursor.getString(dcursor.getColumnIndex("COMPANY_NAME"));
                    String ADDRESS = dcursor.getString(dcursor.getColumnIndex("ADDRESS"));
                    String Box_Crating = dcursor.getString(dcursor.getColumnIndex("BOX_CRATING"));
                    String Box_Crating_Value = dcursor.getString(dcursor.getColumnIndex("BOX_CRATING_VALUE"));
                    String Content_Description = dcursor.getString(dcursor.getColumnIndex("CONTENT_DESCRIPTION"));
                    String Delivery_Town = dcursor.getString(dcursor.getColumnIndex("DELIVERY_TOWN"));
                    String Delivery_Type = dcursor.getString(dcursor.getColumnIndex("DELIVERY_TYPE"));
                    String Department = dcursor.getString(dcursor.getColumnIndex("DEPARTMENT"));
                    String Destination = dcursor.getString(dcursor.getColumnIndex("DESTINATION"));
                    String Express_Centre = dcursor.getString(dcursor.getColumnIndex("EXPRESS_CENTRE"));
                    String INSURANCE_VALUE = dcursor.getString(dcursor.getColumnIndex("INSURANCE_VALUE"));
                    String Origin = dcursor.getString(dcursor.getColumnIndex("ORIGIN"));
                    String Packaging = dcursor.getString(dcursor.getColumnIndex("PACKAGING"));
                    String Pickup_Date = dcursor.getString(dcursor.getColumnIndex("PICKUP_DATE"));
                    String Pieces = dcursor.getString(dcursor.getColumnIndex("PIECES"));
                    String Recipient_Gsm = dcursor.getString(dcursor.getColumnIndex("RECIPIENT_GSM"));
                    String Senders_Gsm = dcursor.getString(dcursor.getColumnIndex("SENDERS_GSM"));
                    String UserID = dcursor.getString(dcursor.getColumnIndex("USERID"));
                    String Weight = dcursor.getString(dcursor.getColumnIndex("WEIGHT"));
                    String senders_name = dcursor.getString(dcursor.getColumnIndex("SENDERS_NAME"));
                    String SENDERS_EMAIL = dcursor.getString(dcursor.getColumnIndex("SENDERS_EMAIL"));
                    String RECIPIENTS_EMAIL = dcursor.getString(dcursor.getColumnIndex("RECIPIENTS_EMAIL"));
                    String AmountPaid = dcursor.getString(dcursor.getColumnIndex("AmountPaid"));
                    String DeliveryTownID = dcursor.getString(dcursor.getColumnIndex("DeliveryTownID"));
                    String DECLARED_VALUE = dcursor.getString(dcursor.getColumnIndex("DECLARED_VALUE"));
                    String WaybillEmailAlert = dcursor.getString(dcursor.getColumnIndex("WaybillEmailAlert"));
                    String ScansEmailAlert = dcursor.getString(dcursor.getColumnIndex("ScansEmailAlert"));
                    String PodEmailAlert = dcursor.getString(dcursor.getColumnIndex("PODEmailAlert"));
                    String Flyer_No = dcursor.getString(dcursor.getColumnIndex("FLYER_NO"));
                    String Prepaid = dcursor.getString(dcursor.getColumnIndex("Prepaid"));

                    upData = new manualUpload();
                    upData.execute(Uploadtype, Global.globalDeviceIMEI, AcctNo,
                            AwbNo, COMPANY_NAME, ADDRESS, Box_Crating, Box_Crating_Value, Content_Description, Delivery_Town,
                            Delivery_Type, Department, Destination, Express_Centre, INSURANCE_VALUE,
                            Origin, Packaging, Pickup_Date, Pieces, Recipient_Gsm, Senders_Gsm, UserID,
                            Weight, senders_name, SENDERS_EMAIL, RECIPIENTS_EMAIL, AmountPaid,
                            DeliveryTownID, DECLARED_VALUE, WaybillEmailAlert, ScansEmailAlert,
                            PodEmailAlert, Flyer_No,Prepaid);



                    Global.globalSyncPickUp = true;

                } while (dcursor.moveToNext());
            }
            dcursor.close();
//
            Global.globalSyncPickUp = false;
        } else {
            dcursor.close();
            Global.globalSyncPickUp = false;
        }

        uploadSignature();
    }
    public void uploadSignature()
    {
        // SECTION POD SYNC
        Uploadtype = "";
        Uploadtype = "SIGNATURE";

        Cursor cursor = db.getSignatureNotUploaded(uploadActivity.this);
        int cCount = 0;
        cCount = cursor.getCount();
        Log.d("SIGNATURE COUNT : ", String.valueOf(cCount));
        if (cursor.getCount() > 0)
        {
            // POD RECORD EXIST
            if (cursor.moveToFirst()) {
                do {
                    // Get each items
                    Bitmap bitmap = null;
                    String AwbNo = cursor.getString(cursor.getColumnIndex("AWBNO"));
                    byte[] bSignature = cursor.getBlob(cursor.getColumnIndex("Signature"));
                    String DateCreated = cursor.getString(cursor.getColumnIndex("DateCreated"));


                    String Signature = Base64.encodeToString(bSignature, Base64.DEFAULT);

                    upData = new manualUpload();
                    upData.execute(Uploadtype, Global.globalDeviceIMEI, Global.globalUserName, AwbNo, Signature, DateCreated);
                    Global.globalSyncSignature = true;


                    AwbNo = "";
                    Signature = "";
                    DateCreated = "";

                } while (cursor.moveToNext());
            }
            cursor.close();
//
            Global.globalSyncSignature = false;
        } else {
            cursor.close();
            Global.globalSyncSignature = false;
        }
       //

    }


    //SECTION TO SYNC DATA TO SERVER
    private class manualUpload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            // Creating service handler class instance
            WebRequest webreq = new WebRequest();
            // add parameter or query string
            // if task is canceled return null
            if (isCancelled()) return null;
            // create new instance of the DB class

            if(arg0[0] == "POD"){
                String DeviceID = db.getDeviceID(uploadActivity.this);//arg0[1];
                String UserID = db.getUserID(uploadActivity.this);//arg0[2];
                String AwbNo = arg0[3];
                String pod = arg0[4];
                String podby = arg0[5];
                String dexcodeid = arg0[6];
                String first_date = arg0[7];
                String poddate = arg0[8];
                String OriginStation = arg0[9];
                String pod_post_date = arg0[10];
                String CustomerFeedBack = arg0[11];

                String Amount = "";
                if(arg0[12] != null)
                {
                    Amount = arg0[12];
                }


                // Building Parameters


                HashMap<String, String> params = new HashMap<>();
                params.put("DeviceID", DeviceID);
                params.put("UserID", UserID);
                params.put("AwbNo", AwbNo);
                params.put("pod", pod);
                params.put("podby", podby);
                params.put("dexcodeid", dexcodeid);
                params.put("first_date", first_date);
                params.put("poddate", poddate);
                params.put("OriginStation", OriginStation);
                params.put("pod_post_date", pod_post_date);
                params.put("CustomerFeedBack", CustomerFeedBack);
                params.put("Amount", Amount);

                // Making a request to url and getting response
                jsonStr = webreq.makeWebServiceCall(Global.globalURLLocal + "rvcPOD_Insert", WebRequest.POST, params);

                if (jsonStr.contains("rCode : 0")) {
                    // FLAG THE WAYBILL AS UPDATED
                    sql = "UPDATE history_pod set outstation_Transfer='Y' where waybillnumber='" + AwbNo + "'";
                    db.dynamicInsert(uploadActivity.this, sql);
                    //DELETE THE RECORD
                    sql="DELETE FROM history_pod where outstation_Transfer='Y' and waybillnumber='"+ AwbNo.toString() + "'";
                    db.dynamicInsert(uploadActivity.this,sql);
                }

            }
            if(arg0[0] == "CHMPOD"){
                String DeviceID = db.getDeviceID(uploadActivity.this);//arg0[1];
                String UserID = db.getUserID(uploadActivity.this);//arg0[2];
                String AwbNo = arg0[3];
                String ReceivedBy = arg0[4];
                String DateReceived = arg0[5];
                String ServiceType = arg0[6];

                // Building Parameters
                HashMap<String, String> params = new HashMap<>();
                params.put("DeviceID", DeviceID);
                params.put("UserID", UserID);
                params.put("AwbNo", AwbNo);
                params.put("ReceivedBy", ReceivedBy);
                params.put("DateReceived", DateReceived);
                params.put("ServiceType", ServiceType);

                Log.d("CHOMENICS parameters: ", "> " + params);

                // Making a request to url and getting response
                jsonStr = webreq.makeWebServiceCall(Global.globalURLLocal + "rvcChemonicPODData_Insert", WebRequest.POST, params);
                Log.d("CHEMONIC response: ", "> " + jsonStr);
                if (jsonStr.contains("rCode : 0")) {
                    // FLAG THE WAYBILL AS UPDATED
                    sql = "UPDATE ChemonicsPOD set TransferStatus='Y' where WayBillNumber='" + AwbNo + "'";
                    db.dynamicInsert(uploadActivity.this, sql);
                    //DELETE THE RECORD
                    sql="DELETE FROM ChemonicsPOD where TransferStatus='Y' and WayBillNumber='"+ AwbNo.toString() + "'";
                    db.dynamicInsert(uploadActivity.this,sql);
                }
            }
            if(arg0[0] == "SCANS"){
                String DeviceID = db.getDeviceID(uploadActivity.this);//arg0[1];
                String UserID = db.getUserID(uploadActivity.this);//arg0[2];
                String AwbNo = arg0[3];
                String Scan_Status = arg0[4];
                String Origin = arg0[5];
                String Destination = arg0[6];
                String Date = arg0[7];
                String Weight = arg0[8];
                String BatchNo = arg0[9];
                String Pieces = arg0[10];
                String SealNo = arg0[11];
                String VehicleNo = arg0[12];
                String Tag = arg0[13];
                String Express_Centre_Code = arg0[14];
                String Content_Type = arg0[15];
                String Route = arg0[16];
                String OpCode = arg0[17];


                // Building Parameters


                HashMap<String, String> params = new HashMap<>();
                params.put("DeviceID", DeviceID);
                params.put("UserID", UserID);
                params.put("AwbNo", AwbNo);
                params.put("Scan_Status", Scan_Status);
                params.put("Origin", Origin);
                params.put("Destination", Destination);
                params.put("ScanDate", Date);
                params.put("Weight", Weight);
                params.put("BatchNo", BatchNo);
                params.put("Pieces", Pieces);
                params.put("SealNo", Global.convertNullToEmptyString(SealNo));
                params.put("VehicleNo", Global.convertNullToEmptyString(VehicleNo));
                params.put("Tag", Global.convertNullToEmptyString(Tag));
                params.put("Express_Centre_Code", Global.convertNullToEmptyString(Express_Centre_Code));
                params.put("Content_Type", Content_Type);
                params.put("Route", Global.convertNullToEmptyString(Route));
                params.put("OpCode", Global.convertNullToEmptyString(OpCode));

                // Making a request to url and getting response
                jsonStr = webreq.makeWebServiceCall(Global.globalURLLocal + "rvcScanData_Insert", WebRequest.POST, params);


                if (jsonStr.contains("rCode : 0")){
                    // FLAG THE WAYBILL AS UPDATED
                    sql="UPDATE SCANS set TransferStatus='Y' where AWBNO='" + AwbNo + "' and SCAN_STATUS='" + Scan_Status + "'";
                    db.dynamicInsert(uploadActivity.this,sql);
                    //DELETE ALL UPDATE SCANS
                    sql = "DELETE FROM SCANS where TransferStatus='Y' and AWBNO='" + AwbNo.toString() + "' and SCAN_STATUS='" + Scan_Status + "'";
                    db.dynamicInsert(uploadActivity.this, sql);

                }
            }
            if(arg0[0] == "SIGNATURE"){

                String DeviceID = db.getDeviceID(uploadActivity.this);//arg0[1];
                String UserID = db.getUserID(uploadActivity.this);//arg0[2];
                String AwbNo = arg0[3];
                String Signature = arg0[4];
                String DateCreated = arg0[5];



                // Building Parameters


                HashMap<String, String> params = new HashMap<>();
                params.put("DeviceID", DeviceID);
                params.put("UserID", UserID);
                params.put("AwbNo", AwbNo);
                params.put("Signature", Signature);
                params.put("DateCreated", DateCreated);

                //DeviceID=string&UserID=string&Longitude=string&Latitude=string
                // Making a request to url and getting response
                jsonStr = webreq.makeWebServiceCall(Global.globalURLLocal + "rvcSignatureData_Insert", WebRequest.POST, params);

                if (jsonStr.contains("rCode : 0")){
                    // FLAG THE WAYBILL AS UPDATED
                    sql="UPDATE signatures set Transferred='Y' where AWBNO='" + AwbNo + "'";
                    db.dynamicInsert(uploadActivity.this,sql);
                    //DELETE ALL UPDATE Scans
                    sql = "DELETE FROM signatures where Transferred='Y' and AWBNO='" + AwbNo.toString() + "'";
                    db.dynamicInsert(uploadActivity.this, sql);
                }
            }
            if(arg0[0] == "PICKUP"){

                String DeviceID = db.getDeviceID(uploadActivity.this);//arg0[1];
                String AcctNo = arg0[2];
                String AwbNo = arg0[3];
                String COMPANY_NAME = arg0[4];
                String ADDRESS = arg0[5];
                String Box_Crating = arg0[6];
                String Box_Crating_Value = arg0[7];
                String Content_Description = arg0[8];
                String Delivery_Town = arg0[9];
                String Delivery_Type = arg0[10];
                String Department = arg0[11];
                String Destination = arg0[12];
                String Express_Centre = arg0[13];
                String INSURANCE_VALUE = arg0[14];
                String Origin = arg0[15];
                String Packaging = arg0[16];
                String Pickup_Date = arg0[17];
                String Pieces = arg0[18];
                String Recipient_Gsm = arg0[19];
                String Senders_Gsm = arg0[20];

                String UserID = db.getUserID(uploadActivity.this);//arg0[2];
                String Weight = arg0[22];
                String senders_name = arg0[23];
                String SENDERS_EMAIL = arg0[24];
                String RECIPIENTS_EMAIL = arg0[25];
                String AmountPaid = arg0[26];
                String DeliveryTownID = arg0[27];
                String DECLARED_VALUE = arg0[28];
                String WaybillEmailAlert = arg0[29];
                String ScansEmailAlert = arg0[30];
                String PodEmailAlert = arg0[31];
                String Flyer_No =arg0[32];
                String Prepaid =arg0[33];




                // Building Parameters


                HashMap<String, String> params = new HashMap<>();
                params.put("DeviceID", DeviceID);
                params.put("AcctNo", AcctNo);
                params.put("AwbNo", AwbNo);
                params.put("COMPANY_NAME", COMPANY_NAME);
                params.put("ADDRESS", ADDRESS);
                params.put("Box_Crating", Box_Crating);
                params.put("Box_Crating_Value", Box_Crating_Value);
                params.put("Content_Description", Content_Description);
                params.put("Delivery_Town", Delivery_Town);
                params.put("Delivery_Type", Delivery_Type);
                params.put("Department", Department);
                params.put("Destination", Destination);
                params.put("Express_Centre", Express_Centre);
                params.put("INSURANCE_VALUE", INSURANCE_VALUE);
                params.put("Origin", Origin);
                params.put("Packaging", Packaging);
                params.put("Pickup_Date", Pickup_Date);
                params.put("Pieces", Pieces);
                params.put("Recipient_Gsm", Recipient_Gsm);
                params.put("Senders_Gsm", Senders_Gsm);
                params.put("UserID", UserID);
                params.put("Weight", Weight);
                params.put("senders_name", senders_name);
                params.put("SENDERS_EMAIL", SENDERS_EMAIL);
                params.put("RECIPIENTS_EMAIL", RECIPIENTS_EMAIL);
                params.put("AmountPaid", AmountPaid);
                params.put("DeliveryTownID", DeliveryTownID);
                params.put("DECLARED_VALUE", DECLARED_VALUE);
                params.put("WaybillEmailAlert", Global.convertNullToEmptyString(WaybillEmailAlert));
                params.put("ScansEmailAlert", Global.convertNullToEmptyString(ScansEmailAlert));
                params.put("PodEmailAlert", Global.convertNullToEmptyString(PodEmailAlert));
                params.put("Flyer_No", Flyer_No);
                params.put("Prepaid", Prepaid);

                // Log.d("HTTP parameters: ", "> " + params);
                // Making a request to url and getting response
                jsonStr = webreq.makeWebServiceCall(Global.globalURLLocal + "rvcPickUpDataNew_Insert", WebRequest.POST, params);


                if (jsonStr.contains("rCode : 0")){
                    // FLAG THE WAYBILL AS UPDATED
                    sql="UPDATE PICKUP_BILLING set CUSTOM_FIELD2='Y' where AWBNO='" + AwbNo + "'";
                    db.dynamicInsert(uploadActivity.this,sql);
                    //DELETE  PICKUP RECORD
                    sql = "DELETE FROM PICKUP_BILLING where CUSTOM_FIELD2='Y' and AWBNO='" + AwbNo.toString() + "'";
                    db.dynamicInsert(uploadActivity.this, sql);
                }

            }
            if (jsonStr.isEmpty() || jsonStr == "") {
                Global.UploadMessage = "Upload Terminated. Please check your internet connection.";
            }
            if (jsonStr.contains("rCode : 1")){
                // erro inserting record
                // do nothing try the next record.
            }
            if (jsonStr.contains("rCode : 2")){
                // FLAG THE WAYBILL AS UPDATED
                Global.UploadMessage = "Your device is not activated on the central server. /n Please contact administrator.";
            }
            Log.d("Response message : ", "> " + Global.UploadMessage);

            Log.d("Response: ", "> " + jsonStr);


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }
    // END OF SECTION SYNC DATA



    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {
            mProgressView.dismiss();
        }
    };
}
