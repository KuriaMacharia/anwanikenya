package com.center.agiza;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.center.agiza.Helper.CheckNetWorkStatus;
import com.center.agiza.Helper.HttpJsonParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.content.Context.ACTIVITY_SERVICE;

public class MyView extends LinearLayout
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
        {

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_DATA = "data";
    private static final String KEY_ADDRESS_NAME = "name";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    private static final String KEY_MIN_LATITUDE = "min_lat";
    private static final String KEY_MIN_LONGITUDE = "min_long";
    private static final String KEY_MAX_LATITUDE = "max_lat";
    private static final String KEY_MAX_LONGITUDE = "max_long";
    private static final String KEY_PACKAGE = "package";
    private static final String KEY_SUCCESS = "success";

    private static final String BASE_URL = "http://anwani.net/seya/";

    private final String COLLECTION_KEY = "address";

    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    private int CALL_CODE = 3;
    private int LOCATION_CODE = 4;
    private static final int PERMISSION_REQUEST_CODE = 200;
    LocationManager locationManager;
    boolean GpsStatus = false;

    Double lat1, lat2, lon1, lon2;
    ArrayList<Double> listDistance;
    ArrayList<String> listAllDistance;
    ArrayList<Double> lisLat;
    ArrayList<Double> lisLon;
    int success;
    String Latitude, Longitude, absoluteLat, myDistance, myAddress, myAddressName, myLatitude, myLongitude, theName, theAddress,
            thePackage, startLat, startLong;
    Double angleRadius, maxLat, minLat, maxLong, minLong, curLat1, curLat, curLong;
    ArrayList<HashMap<String, String>> coordinatesListRe;
    private ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> distanceListRe;
    ArrayList <String> autoList;
    Double accuracySet;
    float disMtrs;

    private Location mylocation;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    TextView resultTxt, locListenTxt, nameTxt, accuracyTxt;
    int count, accuracyMtrs;
    ConstraintLayout findmeCons, resultCons, failedCons, helpCons, consAcuracy, consManual;
    Button saveBtn, shareBtn, directionBtn, findmeBtn;
    ImageView helpImg, closeImg, closeFoundImg, closeNotFoundImg, clearImg, searchImg, settingsImg, closeAccuracyImg;
    TextWatcher textwatcher;
    private AsyncTask searchtask, findtask;
    AutoCompleteTextView searchEdt;
    private AlertDialog alertDialog;
    RadioGroup autoGroup;

    public MyView(Context context) {
        super(context);
        initialize(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }
    private void initialize(Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.my_view, null);
        builder.setView(view);

        //setUpGClient();
        CheckGpsStatus();
        new AutoNames().execute();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        FetchPackageInfo();

        distanceListRe = new ArrayList<>();
        coordinatesListRe=new ArrayList<>();
        listDistance = new ArrayList<Double>();
        listAllDistance = new ArrayList<String>();
        lisLat =new ArrayList<Double>();
        lisLon =new ArrayList<Double>();
        autoList= new ArrayList<>();
        startLat="0.0";
        startLong="0.0";

        locListenTxt=(TextView) view. findViewById(R.id.txt_loc_listen);
        resultTxt=(TextView) view.findViewById(R.id.txt_address_result);
        nameTxt=(TextView) view.findViewById(R.id.txt_address_name);
        findmeCons=(ConstraintLayout) view.findViewById(R.id.cons_find_me);
        resultCons=(ConstraintLayout) view.findViewById(R.id.cons_result_found);
        failedCons=(ConstraintLayout) view.findViewById(R.id.cons_result_not_found);
        helpCons=(ConstraintLayout) view.findViewById(R.id.cons_tips);
        saveBtn=(Button) view.findViewById(R.id.btn_save);
        shareBtn=(Button) view.findViewById(R.id.btn_share);
        directionBtn=(Button) view.findViewById(R.id.btn_direction);
        helpImg=(ImageView) view.findViewById(R.id.img_help);
        closeImg=(ImageView) view.findViewById(R.id.img_close);
        closeAccuracyImg=(ImageView) view.findViewById(R.id.img_close_accuracy);
        searchEdt = (AutoCompleteTextView) view.findViewById(R.id.edt_search);
        closeFoundImg=(ImageView) view.findViewById(R.id.img_close_found);
        closeNotFoundImg=(ImageView) view.findViewById(R.id.img_close_not_found);
        clearImg=(ImageView) view.findViewById(R.id.img_clear_search);
        searchImg=(ImageView) view.findViewById(R.id.img_search_lib);
        settingsImg=(ImageView) view.findViewById(R.id.img_settings);
        consAcuracy=(ConstraintLayout) view.findViewById(R.id.cons_accuracy);
        consManual=(ConstraintLayout) view.findViewById(R.id.cons_manual_set);
        accuracyTxt=(TextView) view.findViewById(R.id.txt_accuracy_selected);
        autoGroup=(RadioGroup) view.findViewById(R.id.group_auto_set);
        findmeBtn=(Button) view.findViewById(R.id.btn_accuracy_continue);

        builder.setCancelable(true);
        alertDialog = builder.create();
        accuracyMtrs=1;
        disMtrs =(float)accuracyMtrs;

        pDialog = new ProgressDialog(getContext(), R.style.mydialog);
        findmeCons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetWorkStatus.isNetworkAvailable(getContext())) {
                    CheckGpsStatus();
                    if (GpsStatus) {
                        if(googleApiClient!=null) {
                            getMyLocation();
                        }else{
                            setUpGClient();
                        }
                        pDialog.setMessage("Initiating Search. Please wait...");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(true);
                        pDialog.show();

                        locListenTxt.addTextChangedListener(textwatcher);
                        resultCons.setVisibility(View.GONE);
                        failedCons.setVisibility(View.GONE);
                        consAcuracy.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Please Enable GPS First", Toast.LENGTH_LONG).show();
                    }
                }else{
                        Toast.makeText(getContext(), "Unable to connect to internet", Toast.LENGTH_LONG).show();
                    }
                }
        });

        autoGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.radio_low)
                {
                    accuracySet=10.0;
                    disMtrs =accuracySet.floatValue();
                    accuracyTxt.setText(String.valueOf(disMtrs) + " Metres (Fast)");
                }
                else if (checkedId == R.id.radio_medium)
                {
                    accuracySet=1.0;
                    disMtrs =accuracySet.floatValue();
                    accuracyTxt.setText(String.valueOf(disMtrs) + " Metres");
                }
                else if (checkedId == R.id.radio_high)
                {
                    accuracySet=0.03;
                    disMtrs =accuracySet.floatValue();
                    accuracyTxt.setText(String.valueOf(disMtrs) + " Metres (Slow)");
                }
                else{

                }
            }
        });

        findmeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetWorkStatus.isNetworkAvailable(getContext())) {
                    CheckGpsStatus();
                    if (GpsStatus) {
                        if(googleApiClient!=null) {
                            getMyLocation();
                        }else{
                            setUpGClient();

                        }

                        pDialog.setMessage("Initiating Search. Please wait...");
                        pDialog.setIndeterminate(false);
                        pDialog.setCancelable(true);
                        pDialog.show();

                        locListenTxt.addTextChangedListener(textwatcher);
                        resultCons.setVisibility(View.GONE);
                        failedCons.setVisibility(View.GONE);
                        consAcuracy.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Please Enable GPS First", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getContext(), "Unable to connect to internet", Toast.LENGTH_LONG).show();
                }

            }
        });

        searchEdt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (CheckNetWorkStatus.isNetworkAvailable(getContext())) {

                    pDialog.setMessage("Searching. Please wait...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(true);
                    pDialog.show();

                    InputMethodManager imm=(InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);

                    new SearchAddress().execute();
                    searchtask = new SearchAddress().execute();
                    resultCons.setVisibility(View.GONE);
                    failedCons.setVisibility(View.GONE);

                }else{
                    Toast.makeText(getContext(), "Unable to connect to internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length()>0){
                    clearImg.setVisibility(View.VISIBLE);
                }else{
                    clearImg.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchEdt.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus){
                if(hasFocus){
                }
            }
        });

        closeAccuracyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consAcuracy.setVisibility(View.GONE);
            }
        });


        clearImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEdt.setText("");
                clearImg.setVisibility(View.GONE);

                alertDialog.dismiss();
            }
        });

        searchImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (CheckNetWorkStatus.isNetworkAvailable(getContext())) {

                    pDialog.setMessage("Searching. Please wait...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(true);
                    pDialog.show();

                    new SearchAddress().execute();
                    searchtask = new SearchAddress().execute();
                    resultCons.setVisibility(View.GONE);
                    failedCons.setVisibility(View.GONE);

                }else{
                    Toast.makeText(getContext(), "Unable to connect to internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        settingsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(consAcuracy.getVisibility()==View.GONE) {
                    consAcuracy.setVisibility(View.VISIBLE);
                }else{
                    consAcuracy.setVisibility(View.GONE);
                }
            }
        });

        helpImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpCons.setVisibility(View.VISIBLE);
                closeImg.setVisibility(View.VISIBLE);
                helpImg.setVisibility(View.GONE);
            }
        });

        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpCons.setVisibility(View.GONE);
                closeImg.setVisibility(View.GONE);
                helpImg.setVisibility(View.VISIBLE);
            }
        });

        closeFoundImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultCons.setVisibility(View.GONE);
            }
        });

        closeNotFoundImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failedCons.setVisibility(View.GONE);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.NAME, "Home Address");
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, resultTxt.getText().toString());
                getContext().startActivity(intent);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                String uri = "http://maps.google.com/maps?daddr=" + String.valueOf(Double.parseDouble(myLatitude)+","+myLongitude);

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String ShareSub = resultTxt.getText().toString();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ShareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, resultTxt.getText().toString() + "\n" +uri);
                getContext().startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        directionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startLat.contentEquals("0.0") &&startLong.contentEquals("0.0")){
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(
                            Uri.parse("https://www.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" +
                                    (myLatitude + "," + myLongitude)));
                    getContext().startActivity(intent);
                }else {
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(
                            Uri.parse("https://www.google.com/maps?saddr=" + startLat + "," + startLong + "&daddr=" +
                                    (myLatitude + "," + myLongitude)));
                    getContext().startActivity(intent);
                }
            }
        });

        textwatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        locListenTxt.removeTextChangedListener(textwatcher);
                    }
                });

                lisLat.add((mylocation.getLatitude()));
                lisLon.add(mylocation.getLongitude());
                int a=lisLat.size();

                if(lisLat.size()>2) {

                    lat1 = lisLat.get(a-1);
                    lon1 = lisLon.get(a-1);
                    lat2 = lisLat.get(a - 2);
                    lon2 = lisLon.get(a - 2);

                    Location loc1 = new Location("");
                    loc1.setLatitude(lat1);
                    loc1.setLongitude(lon1);

                    Location loc2 = new Location("");
                    loc2.setLatitude(lat2);
                    loc2.setLongitude(lon2);

                    float distanceInMeters = loc1.distanceTo(loc2);

//Add the coordinates storage list
                    HashMap<String, String> map = new HashMap<>();
                    map.put("latitude", String.valueOf(Double.valueOf(lat1)));
                    map.put("longitude", String.valueOf(Double.valueOf(lon1)));
                    map.put("distance", String.valueOf(distanceInMeters));
                    coordinatesListRe.add(map);

                    //addressEdt.setText(String.valueOf(distanceInMeters));
                    if(1-distanceInMeters>10) {
                        pDialog.setMessage("Locating. Please wait... (10%)");
                    }else if(distanceInMeters>5 && distanceInMeters<10){
                        pDialog.setMessage("Locating. Please wait... (20%)");
                    }else if(distanceInMeters>3&& distanceInMeters<5){
                        pDialog.setMessage("Locating. Please wait... (30%)");
                    }else if(distanceInMeters>2&& distanceInMeters<3){
                        pDialog.setMessage("Locating. Please wait... (40%)");
                    }else if(distanceInMeters>1&& distanceInMeters<2){
                        pDialog.setMessage("Locating. Please wait... (50%)");
                    }else if(distanceInMeters>0.5&& distanceInMeters<1){
                        pDialog.setMessage("Locating. Please wait... (60%)");
                    }else if(distanceInMeters>0.3&& distanceInMeters<0.5){
                        pDialog.setMessage("Locating. Please wait... (70%)");
                    }else if(distanceInMeters>0.1&& distanceInMeters<0.3){
                        pDialog.setMessage("Locating. Please wait... (80%)");
                    }else if(distanceInMeters>0.05&& distanceInMeters<0.1){
                        pDialog.setMessage("Locating. Please wait... (90%)");
                    }else{
                        pDialog.setMessage("Locating. Please wait...");
                    }

//Compute distance list and process accuracy
                    listDistance.add(Double.parseDouble(String.valueOf(distanceInMeters)));
                    //ss.notifyDataSetChanged();

                    int j=listDistance.size();

                    if(listDistance.size()>15) {
                        /*if (listDistance.get(j - 1) < 0.03
                                && listDistance.get(j - 2) < 0.03
                                && listDistance.get(j - 3) < 0.03
                                && listDistance.get(j - 4) < 0.03
                                && listDistance.get(j - 5) < 0.03)*/
                        if (listDistance.get(j - 1) < disMtrs
                                && listDistance.get(j - 2) < disMtrs
                                && listDistance.get(j - 3) < disMtrs
                                && listDistance.get(j - 4) < disMtrs
                                && listDistance.get(j - 5) < disMtrs){

                            locListenTxt.removeTextChangedListener(textwatcher);
                            lisLat.clear();
                            lisLon.clear();
                            listDistance.clear();
//Coordinates to be set as address point

                            Latitude = coordinatesListRe.get(coordinatesListRe.size()-2).get("latitude");
                            Longitude = coordinatesListRe.get(coordinatesListRe.size()-2).get("longitude");

                            absoluteLat = String.valueOf(Double.parseDouble(coordinatesListRe.get(coordinatesListRe.size()-2).get("latitude"))+ 100);
                            myDistance= coordinatesListRe.get(coordinatesListRe.size()-2).get("distance");
                            coordinatesListRe.clear();

                            CalculateDomain();
                            listAllDistance.clear();

                        }
                    }


                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

    }

    public void show(){
        alertDialog.show();
    }


    public void hide(){
        alertDialog.hide();
    }

    private void FetchPackageInfo(){
        ActivityManager am = (ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        thePackage = componentInfo.getPackageName();
    }


    private void CalculateDomain(){

        curLat=Double.parseDouble(absoluteLat);
        curLat1=Double.parseDouble(Latitude);
        curLong=Double.parseDouble(Longitude);

        angleRadius= 0.002/ ( 111 * Math.cos(curLat1));

        minLat = curLat1 - angleRadius;
        maxLat = curLat1 + angleRadius;
        minLong = curLong - angleRadius;
        maxLong = curLong + angleRadius;

        new FindAddress().execute();
        findtask = new FindAddress().execute();
    }

    private class FindAddress extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    findtask.cancel(true);
                }
            });
        }
        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();

            httpParams.put(KEY_MIN_LATITUDE, String.valueOf(minLat));
            httpParams.put(KEY_MIN_LONGITUDE, String.valueOf(minLong));
            httpParams.put(KEY_MAX_LATITUDE, String.valueOf(maxLat));
            httpParams.put(KEY_MAX_LONGITUDE, String.valueOf(maxLong));
            httpParams.put(KEY_PACKAGE, String.valueOf(thePackage));

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "search_captured_point_api.php", "GET", httpParams);

            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject businesses;
                if (success == 1) {
                    businesses = jsonObject.getJSONObject(KEY_DATA);
                    myAddressName = businesses.getString(KEY_ADDRESS_NAME);
                    myAddress = businesses.getString(KEY_ADDRESS);
                    myLatitude = businesses.getString(KEY_LATITUDE);
                    myLongitude=businesses.getString(KEY_LONGITUDE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
                if (success == 1) {
                    resultTxt.setText(myAddress);
                    nameTxt.setText(myAddressName);
                    resultCons.setVisibility(View.VISIBLE);
                    pDialog.dismiss();
                } else {
                    failedCons.setVisibility(View.VISIBLE);
                    pDialog.dismiss();
                }
        }
    }

    private class SearchAddress extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    searchtask.cancel(true);
                }
            });
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_ADDRESS, searchEdt.getText().toString());
            httpParams.put(KEY_ADDRESS_NAME, searchEdt.getText().toString());
            httpParams.put(KEY_PACKAGE, thePackage);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "search_entered_address_api.php", "GET", httpParams);

            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject businesses;
                if (success == 1) {
                    //Parse the JSON response
                    businesses = jsonObject.getJSONObject(KEY_DATA);

                    myAddressName = businesses.getString(KEY_ADDRESS_NAME);
                    myAddress = businesses.getString(KEY_ADDRESS);
                    myLatitude = businesses.getString(KEY_LATITUDE);
                    myLongitude=businesses.getString(KEY_LONGITUDE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(String result) {
            if (success == 1) {
                resultTxt.setText(myAddress);
                nameTxt.setText(myAddressName);
                resultCons.setVisibility(View.VISIBLE);
                pDialog.dismiss();
            } else {
                failedCons.setVisibility(View.VISIBLE);
                pDialog.dismiss();
            }
        }
    }


    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                //.enableAutoManage((FragmentActivity) context, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        getMyLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        if (mylocation != null) {
            Double latitude1 = mylocation.getLatitude();
            Double longitude1 = mylocation.getLongitude();
            locListenTxt.setText(Double.toString(latitude1) + ", " + Double.toString(longitude1));
            startLat=String.valueOf(mylocation.getLatitude());
            startLong=String.valueOf(mylocation.getLongitude());
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void getMyLocation(){
        if(googleApiClient!=null) {

            if (googleApiClient.isConnected()) {

                int permissionLocation = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    locationRequest = new LocationRequest();
                    locationRequest.setInterval(20);
                    locationRequest.setFastestInterval(20);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(getContext(),
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;
                                /*case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;*/

                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    private void CheckGpsStatus() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private class AutoNames extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_address_names.php", "GET", null);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theAddress = incidence.getString(KEY_ADDRESS);
                        theName = incidence.getString(KEY_ADDRESS_NAME);
                        autoList.add(theAddress);
                        if(!theName.contentEquals("No Name")) {
                            autoList.add(theName);
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
                    if (success == 1) {
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, autoList);
                        searchEdt.setAdapter(dataAdapter);
                    } else {

                    }
        }
    }
}
