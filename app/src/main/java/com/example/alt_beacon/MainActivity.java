package com.example.alt_beacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS=
            {Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN};
    private int GPS_ENABLE_REQUEST_CODE=1;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private static final int PERMISSIONS = 100;

    Vector<MyBeacon> beacon;


    BeaconAdapter beaconAdapter, tmpAdapter;
    ListView beaconListView;
    ScanSettings.Builder mScanSettings;
    List<ScanFilter> scanFilters;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREAN);
    private TextView mTextAddres;
    private Button btn_Ar;


    //float[] F1,F2,F3,L1,L2,L3;//?????? ????????????:F ????????? :L
    String B1 = "C2:01:9E:00:07:5A", B2 = "C2:01:9E:00:07:6C", B3 = "C2:01:9E:00:07:62", B4 = "C2:01:9E:00:07:6A";
    double dis1, dis2, dis3, dis4, dis42, dis43;
    Object tmpDis1, tmpDis2, tmpDis3;
    double[] F1 = new double [2];
    double[] F2 = new double [2];
    double[] F3 = new double [2];
    double[] F4 = new double [2];
    double[] F5 = new double [2];

    double[] Target1;
    double[] L1 = new double [2];
    double[] L2 = new double [2];
    double[] L3 = new double [2];
    double[] L4 = new double [2];
    int cnt=0, stpcnt = 0;

    public double mylot=0.0;
    public double mylat=0.0;
    public double beaconx=0.0;
    public double beacony=0.0;
    public int targetx=0;
    public int targety=0;
    ArrayList<String>   Scanlist;

    ArrayList<String>   list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS);
        beaconListView = (ListView) findViewById(R.id.beaconListView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mTextAddres=findViewById(R.id.tmp1);

        int cnt = 0;

        /**????????? ??????*/
        L1[0] = 0.0;
        L1[1] = 0.0;

        L2[0] = 0.0;
        L2[1] = 0.0;

        L3[0] = 0.0;
        L3[1] = 0.0;

        /** ?????? ?????? ??????*/
        F1[0] = -0;
        F1[1] = -0.5;

        F2[0] = -3.3;
        F2[1] = 4.1;

        F3[0] = 0.0;
        F3[1] = 4.1;

        F4[0] = 0.0;    //-2.1
        F4[1] = 0.0;    //2.1

        F5[0] = 0.4;
        F5[1] = 1.3;

        beacon = new Vector<>();
        mScanSettings = new ScanSettings.Builder();
        mScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        // ?????? ?????? ????????? 2?????? ???????????? Setting?????????.
        // ?????????????????? ??? ????????? ????????? ?????? ?????? ????????? ?????? ??????
        // ??? ????????? ???????????? ??????????????? ?????? ????????? ^^
        // ??? ????????? ????????? ???????????? ??? ?????? ??? 10??? ????????? ????????? ?????????.

        //Button btnScan = (Button) findViewById(R.id.btnscan);//?????? ??????

        Button btnScan=findViewById(R.id.btnScan);
        Button btnStop=findViewById(R.id.btnStop);
        btn_Ar = (Button)findViewById(R.id.btnAr);
        Button btnGPS=findViewById(R.id.bntGPS);

        scanFilters = new Vector<>();
      //  final ScanSettings scanSettings = mScanSettings.build();
        ScanFilter.Builder scanFilter = new ScanFilter.Builder();
        //scanFilter.setDeviceAddress("C2:01:9E:00:07:6C"); //ex) C2:01:9E:00:07:6C
        ScanFilter scan = scanFilter.build();
        scanFilters.add(scan);


        btn_Ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ArActivity.class);
                //gps?????????
                intent.putExtra("longitude",mylot);
                intent.putExtra("latitude",mylat);
                intent.putExtra("beaconx",beaconx);
                intent.putExtra("beacony",beacony);
                startActivity(intent);
            }
        });


        btnScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothLeScanner.startScan(/*scanFilters, scanSettings,*/mScanCallback);
                }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                mBluetoothLeScanner.stopScan(mScanCallback);

                stpcnt++;
            }
        });

        /*GPS??????*/
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnGPS.setOnClickListener(new View.OnClickListener()
         {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23 &&ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                            0 );
                }
                else{
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();

                    mylot=longitude;
                    mylat=latitude;

                    double LoD=DegreeToD(mylot);
                    double LoM=DegreeToM(mylot);
                    double LoS=DegreeToS(mylot);
                    double Loms=DegreeTomS(mylot);
                    double LaD=DegreeToD(mylat);
                    double LaM=DegreeToM(mylat);
                    double LaS=DegreeToS(mylat);
                    double Lams=DegreeTomS(mylat);
                    //DMSTolot(LoD,LoM,LoS,Loms);
                    double doublex= changex(L4[0]);
                    double doubley=changey(L4[1]);

                    String strx=doublex+"";
                    String stry=doubley+"";

                    int intx=Integer.parseInt(strx.substring(0,strx.lastIndexOf(".")));
                    int inty=Integer.parseInt(stry.substring(0,stry.lastIndexOf(".")));
                    //int inty=(int)doubley;

                    targetx=intx;
                    targety=inty;

                    beaconx=DMSTolot(LoD,LoM,LoS,Loms);
                    beacony=DMSTolot(LaD,LaM,LaS,Lams);

                    int targetlot=DegreeTomS(mylot)+intx;
                    int targetlat=DegreeTomS(mylat)+inty;

                    mTextAddres.setText(
                            "Lx : "+doublex+"\n"+
                            "Ly : "+doubley+"\n"+
                            "x??? : "+intx+"\n"+
                            "y??? : "+inty+"\n"+
                            "?????? : " + longitude + "\t" +
                            "?????? : " + latitude + "\n"+
                            "?????? ????????? : "+DegreeToD(mylot)+":"+ DegreeToM(mylot)+"."+DegreeToS(mylot)+"/"+DegreeTomS((mylot))+"\n"+
                            "?????? ????????? : "+DegreeToD(mylat)+":"+ DegreeToM(mylat)+"."+DegreeToS(mylat)+"/"+DegreeTomS((mylat))+"\n"+
                            "???????????? ????????? : "+DegreeToD(mylot)+":"+ DegreeToM(mylot)+"."+DegreeToS(mylot)+"/"+targetlot+"\n"+
                            "???????????? ????????? : "+DegreeToD(mylat)+":"+ DegreeToM(mylat)+"."+DegreeToS(mylat)+"/"+targetlat+"\n"+
                            "return lot" +beaconx+"\n"+
                            "return lat"+beacony+"\n");


                }


            }


        });
        /*
        btnSearch.setOnApplyWindowInsetsListener(new View.OnClickListener(){
            public void onClick(View v) {

            }
        });
        */
        //mBluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
        // filter??? settings ????????? ???????????? ?????? ??????
        //mBluetoothLeScanner.startScan(mScanCallback);?????? ??????????????? ??????.
    }




    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            try {
                final ScanRecord scanRecord = result.getScanRecord();
                Log.d("getTxPowerLevel()",scanRecord.getTxPowerLevel()+"");
                Log.d("onScanResult()", "MAC Address: " +result.getDevice().getAddress() + "\n" + "RSSI: " + result.getRssi()  + "\n" + "TxPower: " +  result.getTxPower() + "\n" + "Device Name: " + result.getDevice().getName()
                        + "\n" + "BondState: " + result.getDevice().getBondState() + "\n" + "Device Type: " + result.getDevice().getType());




                final ScanResult scanResult = result;


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /**  runOnUiThread??? ???????????? ??????  = UI ??? ???????????? ????????? ?????? ????????? ?????????
                         * ?????? runOnUiThread ?????? X    =   onClickListener ???????????? ????????? ???????????? ????????? ???????????? ????????? UI ??? ??????*/
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                int count, checked = 0;
                                count = beaconListView.getCount();
                                /**  Ready Adapter, beacon ??????????????? ????????????, ????????? ????????? ???????????? ??? ?????????????????? ????????? ?????????
                                 * layoutInflate   =   XML??? ????????? Resource?????? View ????????? ??????*/
                                beaconAdapter = new BeaconAdapter(beacon, getLayoutInflater());
                                beaconListView.setAdapter(beaconAdapter);
                                beaconAdapter.notifyDataSetChanged();

                                if (count > 0) {
                                    for(int i=0;i<beaconListView.getCount();i++) {
                                        if (beaconAdapter.getAddress(i).equals(scanResult.getDevice().getAddress())){
                                            // ????????? ??????
                                            beacon.set(i,new MyBeacon(scanResult.getDevice().getAddress(), scanResult.getRssi(), scanRecord.getTxPowerLevel(), simpleDateFormat.format(new Date()), F1, 0.0));
                                            //beacon.set(i, new MyBeacon(scanResult.getDevice().getAddress(), scanResult.getRssi(), scanResult.getTxPower(), simpleDateFormat.format(new Date())));

                                            if (beaconAdapter.getAddress(i).equals(B1)){

                                                dis1 = calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()) * 1000;
                                                Log.d("Beacon ??? #1 ","Beacon 1 ??? ?????? ????????? ???????????????....");
                                                Log.d("Beacon ??? #1 ","Beacon 1 ??? ?????? ????????? ???????????????...." + calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()));


                                            }
                                            else if(beaconAdapter.getAddress(i).equals(B2)){
                                                /**
                                                 beacon2 = beacon;

                                                 Object tmp;
                                                 tmp =   beacon2.get(2);
                                                 int tmpTx   =   (int)tmp;

                                                 tmp =   beacon2.get(1);
                                                 int tmpRssi =   (int)tmp;
                                                 calculateAccuracy(tmpTx,tmpRssi);

                                                 beacon2.set(i,new MyBeacon(scanResult.getDevice().getAddress(), scanResult.getRssi(), scanRecord.getTxPowerLevel(), simpleDateFormat.format(new Date()), F2, calculateAccuracy(tmpTx,tmpRssi)));
                                                 Log.d("Show me the Yong Yong ?????? 2","Beacon 2 ??? ?????? ????????? ???????????????....");
                                                 */

                                                dis2 = calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()) ;
                                                Log.d("Beacon ??? #2","Beacon 2 ??? ?????? ????????? ???????????????....");
                                                Log.d("Beacon ??? #2","Beacon 2 ??? ?????? ????????? ???????????????...." + calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()));

                                            }
                                            else if(beaconAdapter.getAddress(i).equals(B3)){

                                                dis3 = calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()) ;
                                                Log.d("Beacon ??? #3","Beacon 3 ??? ?????? ????????? ???????????????....");
                                                Log.d("Beacon ??? #3","Beacon 3 ??? ?????? ????????? ???????????????...." + calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()));
                                            }
                                            else if(beaconAdapter.getAddress(i).equals(B4)){

                                                dis4 = calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()) ;
                                                Log.d("Calculating  #4 Success","Beacon 4 ??? ?????? ????????? ???????????????....");
                                                Log.d("Show me the Yong Yong ?????? 4","Beacon 4 ??? ?????? ????????? ???????????????...." + calculateAccuracy(Txpower(scanResult.getTxPower()),scanResult.getRssi()));
                                            }
                                            else{
                                                Log.d("Beacon ???","Beacon ?????? ?????? ????????? ???????????????....");
                                            }
                                            // listview ??????
                                            beaconAdapter.notifyDataSetChanged();
                                            checked=1;
                                        }
                                    }

                                    /** ????????? ??? & ????????? ???????????? ???????????? ???????????? ??????*/
                                    //for(int i = 0; i < beaconListView.getCount(); i++) {
                                    Log.d("??????????????? For??? ??????...","Calculating Sequence initiate");

                                    /**
                                     if((L1[0] == 0.0))
                                     Log.d("?????? ?????? ???... \n Coordinate L1 is exist... \n","Let's go home..." + "X??????: " + L1[0] + "Y??????: " + L1[1]);
                                     L1[0] = position_calculate_x(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                     L1[1] = position_calculate_y(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                     Log.d("?????? ?????? ???.... \n Coordinate L1 is exist... \n", "Let's go home..." + "X??????: " + L1[0] + "Y??????: " + L1[1]);
                                     cnt++;


                                     if((L2[0] == 0.0)) {
                                     Log.d("?????? ?????? ???... \n Coordinate L2is exist... \n","Let's go home..." + "X??????: " + L2[0] + "Y??????: " + L2[1]);
                                     L2[0]  =   position_calculate_x(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1],  dis1, dis2, dis3);
                                     L2[1]  =   position_calculate_y(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1],  dis1, dis2, dis3);
                                     Log.d("?????? ?????? ???.... \n Coordinate L2 is exist... \n","Let's go home..." + "X??????: " + L2[0] + "Y??????: " + L2[1]);
                                     }
                                     if((L3[0] == 0.0)){
                                     Log.d("?????? ?????? ???... \n Coordinate L2 is exist... \n","Let's go home..." + "X??????: " + L3[0] + "Y??????: " + L3[1]);
                                     L3[0]  =   position_calculate_x(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1],  dis1, dis2, dis3);
                                     L3[1]  =   position_calculate_y(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1],  dis1, dis2, dis3);
                                     Log.d("?????? ?????? ???.... \n Coordinate L3 is exist... \n","Let's go home..." +"X??????: " + L3[0] + "Y??????: " + L3[1]);


                                     }
                                     */
                                }
                                //  ?????? ?????? ??????
                                if(checked==0) {
                                    if(scanRecord.getTxPowerLevel() > -77)       {
                                        beacon.add(0, new MyBeacon(scanResult.getDevice().getAddress(), scanResult.getRssi(),  scanRecord.getTxPowerLevel() , simpleDateFormat.format(new Date()),F1,0.0));
                                        //beacon.add(0, new MyBeacon(scanResult.getDevice().getAddress(), scanResult.getRssi(),  scanResult.getTxPower() , simpleDateFormat.format(new Date())));
                                        beaconAdapter.notifyDataSetChanged();
                                    }
                                }
                                if(stpcnt == 0){
                                    if((L1[0] == 0.0))
                                        Log.d("?????? ?????? ???... Coordinate L1 is exist... ","Let's go home..." + "X??????: " + L1[0] + "Y??????: " + L1[1]);
                                    L1[0] = position_calculate_x(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                    L1[1] = position_calculate_y(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                    Log.d("?????? ?????? ???.... Coordinate L1 is exist... ", "Let's go home..." + "X??????: " + L1[0] + "Y??????: " + L1[1]);
                                }
                                else if(stpcnt == 1) {

                                    Log.d("?????? ?????? ???... Coordinate L2is exist... ", "Let's go home..." + "X??????: " + L2[0] + "Y??????: " + L2[1]);
                                    L2[0] = position_calculate_x(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                    L2[1] = position_calculate_y(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                    Log.d("?????? ?????? ???....  Coordinate L2 is exist... ", "Let's go home..." + "X??????: " + L2[0] + "Y??????: " + L2[1]);

                                }
                                else if(stpcnt == 2) {

                                    Log.d("?????? ?????? ???...  Coordinate L2 is exist... ", "Let's go home..." + "X??????: " + L3[0] + "Y??????: " + L3[1]);
                                    L3[0] = position_calculate_x(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                    L3[1] = position_calculate_y(F1[0], F2[0], F3[0], F1[1], F2[1], F3[1], dis1, dis2, dis3);
                                    Log.d("?????? ?????? ???....  Coordinate L3 is exist... ", "Let's go home..." + "X??????: " + L3[0] + "Y??????: " + L3[1]);

                                }
                                else{
                                    L4[0] = position_calculate_x(L1[0], L2[0], L3[0], L1[1], L2[1], L3[1], dis1, dis2, dis3);
                                    L4[1] = position_calculate_y(F1[0], F2[0], L3[0], L1[1], L2[1], L3[1], dis1, dis2, dis3);
                                    Log.d("????????????","?????????!!!");
                                }
                            }

                        });
                    }
                }).start();

                //  ?????? ????????? ?????????
                double mdistance = 0.000;

                //  beacon??? ????????? ???????????? ???????????? ????????? ????????? Data ?????????  count??? ??????
                int count  =   beaconListView.getCount();

                /** TXpower, RSSI, distance ???????????? ??????*/
                /*StringBuilder tmp;
                for(int i = 0; i < count; i++) {
                   // mdistance = calculateAccuracy(Txpower(beaconAdapter.getTxPower(i)), beaconAdapter.getRssi(i));//????????????

                    tmp = new StringBuilder();
                    tmp.append("TxPower: " + Integer.toString(beaconAdapter.getTxPower(i)) + "\t");
                    tmp.append("RSSI:" + Integer.toString(beaconAdapter.getRssi(i)) + "\t");
                    tmp.append("distance: " + Double.toString(mdistance));

                    if (i == 0) {
                        mTextAddres = (TextView) findViewById(R.id.tmp1);
                        mTextAddres.setText(tmp);
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("onBatchScanResults", results.size() + "");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed()", errorCode+"");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeScanner.stopScan(mScanCallback);
    }


    public int Txpower(int level){//txpowerlevel??? txpower??? ??????
        switch (level){
            case -30:
                return -115;
            case -20:
                return -84;
            case -16:
                return -81;
            case -12:
                return -77;
            case -8:
                return -72;
            case -4:
                return -69;
            case 0:
                return -65;
            case 4:
                return -59;
                default:
                    return -1;
        }
    }

    static class KalmanFilter {

        private double Q = 0.00001;
        private double R = 0.001;
        private double X = 0, P = 1, K;

        //??????????????? ???????????? ????????? ??????. ??????????????? ???????????? ???????????? ???????????? ????????? ????????? ??????????????? ?????? ???????????????~

        KalmanFilter(double initValue) {
            X = initValue;

        }

        //??????????????? ???????????? ????????????

        private void measurementUpdate() {
            K = (P + Q) / (P + Q + R);
            P = R * (P + Q) / (R + P + Q);

        }

        //???????????? ?????? ????????? ????????? ???????????? ????????????
        public double update(double measurement) {
            measurementUpdate();
            X = X + (measurement - X) * K;
            return X;
        }
    }//????????????

    static public double calculateAccuracy(int txPower, int rssi) {

        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        Log.d(TAG, "calculating accuracy based on rssi of " + rssi);
        double ratio = rssi * 1.0 / txPower;

        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            Log.d(TAG, " avg rssi: " + rssi + " accuracy: " + accuracy);
            return accuracy;
        }
    }//rssi????????? ????????????

    //???????????? ??? ????????? triptx, tripty ??????????????? ???????????? ??????
    /*public double[] position_calculate(float[] P1, float[] P2, float[] P3, double d1, double d2, double d3)
    {
        double[] ex   = new double[2];
        double[] ey   = new double[2];
        double[] p3p1 = new double[2];





        double p2p1distance = Math.pow(Math.pow(P2[0] - P1[0] , 2) + Math.pow(P2[1] - P1[1] , 2) , 0.5);
        double exx = (P2[0] - P1[0])/p2p1distance;
        double exy = (P2[1] - P1[1])/p2p1distance;
        double auxx = (P3[0] - P1[0]);
        double auxy = (P3[1] - P1[1]);
        double i = exx * auxx + exy * auxy;
        double aux2x = P3[0] - P1[0] - (i * exx);
        double aux2y = P3[1] - P1[1] - (i * exy);
        double eyx = aux2x / (Math.pow(Math.pow(aux2x,2) + Math.pow(aux2y,2) , 0.5)) ;
        double eyy = aux2y / (Math.pow(Math.pow(aux2x,2) + Math.pow(aux2y,2) , 0.5)) ;
        double j = eyx * auxx + eyy * auxy;
        double x = (Math.pow(d1,2) - Math.pow(d2,2) + Math.pow(p2p1distance,2))/ (2 * p2p1distance);
        double y = (Math.pow(d1,2) - Math.pow(d3,2) + Math.pow(i,2) + Math.pow(j,2))/(2*j) - i*x/j;
        double triptx = (P1[0] + x * exx + y * eyx);
        double tripty = (P1[1] + x * exy + y * eyy);


        double result[]={triptx,tripty};
        return result;
        //return  triptx;
    }*/
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;


            // ?????? ???????????? ??????????????? ???????????????.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {
                Log.d("@@@", "start");
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    /*public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }*/

    /*public void saveLocation(double lon, double lat){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("lon", String.valueOf(lon));
        editor.putString("lat", String.valueOf(lat));
        editor.commit();
    }*/
    /*private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

                double longitude = location.getLongitude();    //??????

                double latitude = location.getLatitude();         //??????


            } else {

            }

        }


        public void onProviderDisabled(String provider) {

        }


        public void onProviderEnabled(String provider) {

        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };*/

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();

            mTextAddres.setText("?????? : " + longitude + "\t" +
                    "?????? : " + latitude + "\t");

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
        public double position_calculate_x(double X1, double X2, double X3, double Y1, double Y2, double Y3, double d1, double d2, double d3)
        {

            double p2p1distance = Math.pow(Math.pow(X2 - X1 , 2) + Math.pow(Y2 - Y1 , 2) , 0.5);
            double exx = (X2 - X1)/p2p1distance;
            double exy = (Y2 - Y1)/p2p1distance;
            double auxx = (X3 - X1);
            double auxy = (Y3 - Y1);
            double i = exx * auxx + exy * auxy;
            double aux2x = X3 - X1 - (i * exx);
            double aux2y = Y3 - Y1 - (i * exy);
            double eyx = aux2x / (Math.pow(Math.pow(aux2x,2) + Math.pow(aux2y,2) , 0.5)) ;
            double eyy = aux2y / (Math.pow(Math.pow(aux2x,2) + Math.pow(aux2y,2) , 0.5)) ;
            double j = eyx * auxx + eyy * auxy;
            double x = (Math.pow(d1,2) - Math.pow(d2,2) + Math.pow(p2p1distance,2))/ (2 * p2p1distance);
            double y = (Math.pow(d1,2) - Math.pow(d3,2) + Math.pow(i,2) + Math.pow(j,2))/(2*j) - i*x/j;
            double triptx = (X1 + x * exx + y * eyx);



            return triptx ;
            //return  triptx;
        }

        /**???????????? ??? ?????????  tripty ??????????????? ???????????? ?????? */
        public double position_calculate_y(double X1, double X2, double X3, double Y1, double Y2, double Y3, double d1, double d2, double d3) {

            double p2p1distance = Math.pow(Math.pow(X2 - X1 , 2) + Math.pow(Y2 - Y1 , 2) , 0.5);
            double exx = (X2 - X1)/p2p1distance;
            double exy = (Y2 - Y1)/p2p1distance;
            double auxx = (X3 - X1);
            double auxy = (Y3 - Y1);
            double i = exx * auxx + exy * auxy;
            double aux2x = X3 - X1 - (i * exx);
            double aux2y = Y3 - Y1 - (i * exy);
            double eyx = aux2x / (Math.pow(Math.pow(aux2x,2) + Math.pow(aux2y,2) , 0.5)) ;
            double eyy = aux2y / (Math.pow(Math.pow(aux2x,2) + Math.pow(aux2y,2) , 0.5)) ;
            double j = eyx * auxx + eyy * auxy;
            double x = (Math.pow(d1,2) - Math.pow(d2,2) + Math.pow(p2p1distance,2))/ (2 * p2p1distance);
            double y = (Math.pow(d1,2) - Math.pow(d3,2) + Math.pow(i,2) + Math.pow(j,2))/(2*j) - i*x/j;
            double tripty = (Y1 + x * exy + y * eyy);


            return tripty ;

        }
    public String DegreeToDMS(double degree){
        int hour = (int)degree;
        degree -= hour;
        int minute = (int)(degree*60);
        degree = degree*60 - minute;
        int second = (int)(degree*60);
        degree = degree*60 - second;
        int msecond = (int)(degree*1000);

        return hour+":"+minute+"."+second+"/"+msecond;
    }
    public int DegreeToD(double degree){
        int hour = (int)degree;
        return hour;
    }
    public int DegreeToM(double degree){
        int hour = (int)degree;
        degree -= hour;
        int minute = (int)(degree*60);
        return minute;
    }
    public int DegreeToS(double degree){
        int hour = (int)degree;
        degree -= hour;
        int minute = (int)(degree*60);
        degree = degree*60 - minute;
        int second = (int) (degree*60);
        return second;
    }
    public int DegreeTomS(double degree){
        int hour = (int)degree;
        degree -= hour;
        int minute = (int)(degree*60);
        degree = degree*60 - minute;
        int second = (int)(degree*60);
        degree = degree*60 - second;
        int msecond = (int)(degree*1000);
        return msecond;
    }
    public double DMSTolot(double D,double M,double S,double ms){

           return ms/1000+S/60+M/60+D;
    }
    public double DMSTolat(double D,double M,double S,double ms){
        return ms/1000+S/60+M/60+D;

    }
    /*public double targetlot(double mlot,double targetlot){

    }*/
    public int findmsx(int mlot,int targetlot){
        return mlot+targetlot;
    }
    public int findmsy(int mlat,int targetlat){
        return mlat+targetlat;
    }
    public double changex(double x){
        double vex=x/30*100;
                //int targetx=Integer.parseInt(String.valueOf(Math.round(vex)));
                return vex;


    }
    public double changey(double y){
        double vey=y/24*100;
            //int targety=Integer.parseInt(String.valueOf(Math.round(vey)));
            return vey;

    }



        /*public int DegreeToD(double degree){

        }*/
}

