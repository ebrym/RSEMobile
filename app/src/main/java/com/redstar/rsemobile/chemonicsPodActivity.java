package com.redstar.rsemobile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import barcodereader.BarcodeCaptureActivity;
import db.DataDB;
import me.dm7.barcodescanner.scanner.ScannerActivity;

public class chemonicsPodActivity extends AppCompatActivity {
    private EditText  txtCHMPODBy, txtCHMAwbno;
    private TextView txtTotalCHMPOD;

    private Button btnCHMSavePod,btnCHMSignature,btnCHMBarcode;
    private CheckBox chkCHMMultiplePOD;
    private ProgressDialog mProgressView;
    private TableLayout tbDetails;
    private Spinner spnServiceType;
    public int podCount = 0;
    DataDB db;

    // for camera barcode
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    // for ZBAR barcode
    private static final int ZBAR_SCANNER_REQUEST = 0;
    //private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private static final String[]serviceType = {"LHD", "LMD"};

    String currentDateandTime = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemonicspod);
        db=new DataDB();

        txtCHMAwbno = (EditText) findViewById(R.id.txtCHMPodAwbno);
        txtCHMPODBy = (EditText) findViewById(R.id.txtCHMRecievedBy);
        txtTotalCHMPOD = (TextView) findViewById(R.id.txtCHMTotalPOD);
        tbDetails = (TableLayout) findViewById(R.id.tbPodDetails);
        //for camera barcode
        btnCHMBarcode=(Button)findViewById(R.id.btnCHMbarcode);
        btnCHMSavePod = (Button) findViewById(R.id.btnCHMSavePod);
        btnCHMSignature = (Button)findViewById(R.id.btnCHMSignature);
        btnCHMSignature.setVisibility(View.INVISIBLE);
        chkCHMMultiplePOD = (CheckBox) findViewById(R.id.chkCHMMultiplePOD);
        spnServiceType = (Spinner) findViewById(R.id.spnServiceType);


        Global.globalMultiplePOD = "No";

        //load table

        loadAvailablePOD();
        loadSpnServiceType();

        // check airwaybill scanned
        txtCHMAwbno.setOnKeyListener(new View.OnKeyListener(){

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //((event.getAction()==KeyEvent.ACTION_DOWN) && (event.getKeyCode()==KeyEvent.KEYCODE_ENTER)) ||
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {


                    Global.globalCHMPodAwbno = txtCHMAwbno.getText().toString();
                    Global.globalCHMPodBy = txtCHMPODBy.getText().toString();
                    Global.globalCHMServiceType = String.valueOf(spnServiceType.getSelectedItem());

                    if(Global.globalCHMServiceType.toString().equals("")  || Global.globalCHMServiceType.toString().equals(" ")) {
                        Global.AssetDialog("Service Type is required!", chemonicsPodActivity.this).create().show();
                        spnServiceType.requestFocus();
                        return true;
                    }
                    if(Global.globalCHMPodBy.toString().equals("")  || Global.globalCHMPodBy.toString().equals(" ")) {
                        Global.AssetDialog("Received By is required!", chemonicsPodActivity.this).create().show();
                        txtCHMPODBy.requestFocus();
                        return true;
                    }
                    if(txtCHMAwbno.getText().toString().equals("") || txtCHMAwbno.getText().toString().equals(" "))
                    {
                        Global.AssetDialog("AirWay bill number is required!!", chemonicsPodActivity.this).create().show();
                        txtCHMAwbno.requestFocus();
                        return true;
                    }
                    if(db.checkCHMAWBNOPOD(chemonicsPodActivity.this,txtCHMAwbno.getText().toString()))
                    {
                        Global.AssetDialog("AirWay bill number already Exist.!!", chemonicsPodActivity.this).create().show();
                        txtCHMAwbno.setText("");
                        txtCHMAwbno.requestFocus();
                        return true;
                    }else{
                        if( Global.globalMultiplePOD.toString().equals("No")) {

                            btnCHMSavePod.setVisibility(View.VISIBLE);
                            btnCHMSignature.setVisibility(View.VISIBLE);
                        }else{
                            btnCHMSignature.setVisibility(View.INVISIBLE);
                            mProgressView = ProgressDialog.show(chemonicsPodActivity.this, "", "Please wait...", true);
                            String sql;
                            String currentDateandTime = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date());

                            sql = "INSERT INTO ChemonicsPOD" +
                                    "(WayBillNumber,ReceivedBy,DateReceived,UserID,ServiceType,TransferStatus" +
                                    ")VALUES('" + Global.globalCHMPodAwbno + "'," +
                                    "'" + Global.globalCHMPodBy + "'," +
                                    "'" + currentDateandTime.toString() + "'," +
                                    "'" + Global.globalUserName + "'," +
                                    "'" + Global.globalCHMServiceType + "'," +
                                    "'N')";
                            if (db.dynamicInsert(chemonicsPodActivity.this,sql)) {
                                //Log.d("Response: ", "> " + jsonStr);
                                // save signature for multiple pod
                                db.insertSignature(chemonicsPodActivity.this, Global.globalMultipleSignature);
                                handler.sendEmptyMessage(0);

                                appendDetailsPOD(tbDetails,Global.globalCHMPodAwbno,Global.globalCHMServiceType,Global.globalCHMPodBy, currentDateandTime.toString());

                                //loadAvailablePOD();
                                txtTotalCHMPOD.setText(String.valueOf(podCount+1));
                                txtCHMAwbno.setText("");
                                txtCHMAwbno.requestFocus();
                                //Global.AssetDialog("Record saved!!", podActivity.this).create().show();
                                // resetValues();
                            }else{
                                handler.sendEmptyMessage(0);
                                Global.AssetDialog("Error while trying to  save scan record. /n please try again.!!", chemonicsPodActivity.this).create().show();
                            }
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
    });
        // event for save

        btnCHMSavePod.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Global.globalCHMPodAwbno = txtCHMAwbno.getText().toString();
                Global.globalCHMPodBy = txtCHMPODBy.getText().toString();
                Global.globalCHMServiceType = String.valueOf(spnServiceType.getSelectedItem());

                if(Global.globalCHMServiceType.toString().equals("")  || Global.globalCHMServiceType.toString().equals(" ")) {
                    Global.AssetDialog("Received By is required!", chemonicsPodActivity.this).create().show();
                    spnServiceType.requestFocus();
                    return;
                }
                if(Global.globalCHMPodBy.toString().equals("")  || Global.globalCHMPodBy.toString().equals(" ")) {
                    Global.AssetDialog("Received By is required!", chemonicsPodActivity.this).create().show();
                    txtCHMPODBy.requestFocus();
                    return;
                }
                if(txtCHMAwbno.getText().toString().equals("") || txtCHMAwbno.getText().toString().equals(" "))
                {
                    Global.AssetDialog("AirWay bill number is required!!", chemonicsPodActivity.this).create().show();
                    txtCHMAwbno.requestFocus();
                    return ;
                }
                if(db.checkCHMAWBNOPOD(chemonicsPodActivity.this,txtCHMAwbno.getText().toString()))
                {
                    Global.AssetDialog("AirWay bill number already Exist.!!", chemonicsPodActivity.this).create().show();
                    txtCHMAwbno.setText("");
                    txtCHMAwbno.requestFocus();
                    return;
                }else{
                    mProgressView = ProgressDialog.show(chemonicsPodActivity.this, "", "Please wait...", true);
                    if (db.checkAWBNOSignature(chemonicsPodActivity.this,Global.globalPodAwbno) == false) {
                        handler.sendEmptyMessage(0);
                        Global.AssetDialog("Please capture Signature for this Waybill number.", chemonicsPodActivity.this).create().show();

                        btnCHMSignature.setVisibility(View.VISIBLE);
                        return;
                    }else {
                        String sql;
                        String currentDateandTime = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date());
                        sql = "INSERT INTO ChemonicsPOD" +
                                "(WayBillNumber,ReceivedBy,DateReceived,UserID,ServiceType,TransferStatus" +
                                ")VALUES('" + Global.globalCHMPodAwbno + "'," +
                                "'" + Global.globalCHMPodBy + "'," +
                                "'" + currentDateandTime.toString() + "'," +
                                "'" + Global.globalUserName + "'," +
                                "'" + Global.globalCHMServiceType + "'," +
                                "'N')";
                        if (db.dynamicInsert(chemonicsPodActivity.this,sql)) {
                            handler.sendEmptyMessage(0);
                            appendDetailsPOD(tbDetails,Global.globalCHMPodAwbno,Global.globalCHMServiceType,Global.globalCHMPodBy,currentDateandTime.toString());

                            txtTotalCHMPOD.setText(String.valueOf(podCount+1));
                            Global.AssetDialog("Record saved!!", chemonicsPodActivity.this).create().show();
                            btnCHMSignature.setVisibility(View.INVISIBLE);
                            resetValues();
                        }else{
                            handler.sendEmptyMessage(0);
                            Global.AssetDialog("Error while trying to  save scan record. /n please try again.!!", chemonicsPodActivity.this).create().show();
                        }
                    }

                }// end of aiwaybill check
               // handler.sendEmptyMessage(0);
            }
        });
        chkCHMMultiplePOD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                          if (isChecked) {
                              Global.globalMultiplePOD = "Yes";
                              Global.globalPodAwbno = Global.globalCHMPodAwbno.toString();
                              btnCHMSavePod.setVisibility(View.INVISIBLE);
                              btnCHMSignature.setVisibility(View.INVISIBLE);
                              mProgressView = ProgressDialog.show(chemonicsPodActivity.this, "", "Please wait...", true);
                              //Intent i = new Intent(podActivity.this, signatureActivity.class);
                              Intent i = new Intent(chemonicsPodActivity.this, CaptureSignature.class);
                              startActivity(i);
                              // clear progress
                              handler.sendEmptyMessage(0);
                          }
                          else {
                              Global.globalMultiplePOD = "No";
                              btnCHMSavePod.setVisibility(View.VISIBLE);
                              btnCHMSignature.setVisibility(View.VISIBLE);
                          }

                      }
              }
        );
        // event for signature button

        btnCHMSignature.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                validateControls();


                    if( Global.globalMultiplePOD.equals("No")) {
                        Global.globalPodAwbno = Global.globalCHMPodAwbno.toString();
                        if (db.checkAWBNOSignature(chemonicsPodActivity.this, Global.globalPodAwbno)) {
                            Global.AssetDialog("Signature already exist for the Waybill number.", chemonicsPodActivity.this).create().show();
                        } else {
                            mProgressView = ProgressDialog.show(chemonicsPodActivity.this, "", "Please wait...", true);
                            //Intent i = new Intent(podActivity.this, signatureActivity.class);
                            Intent i = new Intent(chemonicsPodActivity.this, CaptureSignature.class);
                            startActivity(i);
                            // clear progress
                            handler.sendEmptyMessage(0);
                        }
                    }else{


                    }


            }
        });

        // camera barcode reading
        btnCHMBarcode.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
               /* for GOOGLE BARCODE
                boolean autoFocus = true;
                boolean useFlash = true;
                Intent intent = new Intent(chemonicsPodActivity.this, BarcodeCaptureActivity.class);

                intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                */
                Intent intent = new Intent(chemonicsPodActivity.this, ScannerActivity.class);
                startActivityForResult(intent, ZBAR_SCANNER_REQUEST);


            }
        });
    }

    private void loadSpnServiceType() {
        // database handler
        //DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        db = new DataDB();
        // Spinner Drop down elements
        List<String> stationCodes = db.getStation(this);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, serviceType);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spnServiceType.setAdapter(dataAdapter);
    }


    private void loadAvailablePOD() {
        // database handler
        db = new DataDB();
        // Spinner Drop down elements

        String awbNo,  receivedBy, podDate, ServiceType;

        Cursor cursorPOD = db.getCHMPODNotUploaded(chemonicsPodActivity.this);
        if (cursorPOD != null ) {
            if (cursorPOD.moveToFirst()) {

                do {
                    awbNo = cursorPOD.getString(cursorPOD.getColumnIndex("WayBillNumber"));
                    receivedBy = cursorPOD.getString(cursorPOD.getColumnIndex("ReceivedBy"));
                    podDate = cursorPOD.getString(cursorPOD.getColumnIndex("DateReceived"));
                    ServiceType = cursorPOD.getString(cursorPOD.getColumnIndex("ServiceType"));

                    appendDetailsPOD(tbDetails,awbNo,ServiceType,receivedBy,podDate);
                    podCount+=1;


                }while (cursorPOD.moveToNext());
            }

        }
        txtTotalCHMPOD.setText(String.valueOf(podCount));
    }
    public void setValues(){
    Global.globalCHMPodAwbno = txtCHMAwbno.getText().toString();
    Global.globalCHMPodBy = txtCHMPODBy.getText().toString();
}
    public void resetValues(){
        txtCHMAwbno.setText("");
        txtCHMAwbno.requestFocus();
        txtCHMPODBy.setText("");

        Global.globalCHMPodAwbno = "";
        Global.globalCHMPodBy = "";

    }
    protected void validateControls(){
        // set controls values
        setValues();
        // validate controls


        if(Global.globalCHMPodBy.toString().equals("")) {
            Global.AssetDialog("Received By is required!", chemonicsPodActivity.this).create().show();
            txtCHMPODBy.requestFocus();
            return;
        }

    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {
            mProgressView.dismiss();
        }
    };

    private void appendDetailsPOD(TableLayout table, String Text1, String Text2, String Text3, String Text4) {

        View sep2 = new View(this);
        TableLayout.LayoutParams p2 = new TableLayout.LayoutParams();
        p2.height=1;
        p2.topMargin=10;
        sep2.setBackgroundColor(0xFF000000);
        sep2.setLayoutParams(p2);

        table.addView(sep2);

        TableRow row = new TableRow(this);

        TextView col1 = new TextView(this);
        TextView col2 = new TextView(this);
        TextView col3 = new TextView(this);
        TextView col4 = new TextView(this);

        col1.setText(Text1.trim());
        col1.setTextSize(9);
        col1.setPadding(3, 3, 3, 3);
        col1.setTextColor(Color.BLACK);

        col2.setText(Text2.trim());
        col2.setTextSize(9);
        col2.setPadding(3, 3, 3, 3);
        col2.setTextColor(Color.BLACK);

        col3.setText(Text3.trim());
        col3.setTextSize(9);
        col3.setPadding(3, 3, 3, 3);
        col3.setTextColor(Color.BLACK);

        col4.setText(Text4.trim());
        col4.setTextSize(9);
        col4.setPadding(3, 3, 3, 3);
        col4.setTextColor(Color.BLACK);

        TableRow.LayoutParams params1 = new TableRow.LayoutParams();
        params1.span = 2;
        params1.column = 0;


        row.addView(col1, new TableRow.LayoutParams());
        row.addView(col2, new TableRow.LayoutParams());
        row.addView(col3, new TableRow.LayoutParams());
        row.addView(col4, new TableRow.LayoutParams());

        table.addView(row, new TableLayout.LayoutParams());

        table.setColumnStretchable(0, true);

        View sep1 = new View(this);
        TableLayout.LayoutParams p1 = new TableLayout.LayoutParams();
        p1.height=1;
        sep1.setBackgroundColor(0xFF000000);
        sep1.setLayoutParams(p1);

        table.addView(sep1);

    }

    /**
     * Called when an activity you launched exits i.e. when BarcodeCaptureActivity exits
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* FOR GOOGLE VISION BARCODE
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Toast.makeText(this, R.string.barcode_success, Toast.LENGTH_SHORT).show();

                    txtCHMAwbno.setText(barcode.displayValue);

                   // Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    Toast.makeText(this, R.string.barcode_failure, Toast.LENGTH_SHORT).show();

                   // Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Toast.makeText(this, String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)), Toast.LENGTH_SHORT).show();

            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        */

        if (requestCode == ZBAR_SCANNER_REQUEST) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("BarcodeData");
                txtCHMAwbno.setText(result);
                // The value of type indicates one of the symbols listed in Advanced Options below.
            } else {
                Toast.makeText(this, "Invalid waybill!!!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Invalid waybill!!!", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
