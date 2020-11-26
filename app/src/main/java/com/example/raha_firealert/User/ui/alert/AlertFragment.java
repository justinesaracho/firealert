package com.example.raha_firealert.User.ui.alert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;

import androidx.fragment.app.DialogFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.raha_firealert.Login;
import com.example.raha_firealert.MyConfig;
import com.example.raha_firealert.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;


public class AlertFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private static long START_TIME_IN_MILLIS = 60000;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis;
    private long mEndTime;


    private MapView mapView;
    protected GoogleMap gMap;
    public Timer timer;
    private boolean mLocationPermissionGranted = false;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private Location mLastKnownLocation;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    Double coorLat, coorLng;
    LatLng current,mylatlng;
    Button btn_sendalert;
    String str_address;
    Circle circleRelocate;
    Marker myMarker;

    private FusedLocationProviderClient fusedLocationClient;
    boolean place_search = false;
    float radius = 500;
    private SharedPreferences location_sharedpref;
    String LOCATIONPEF_NAME = "locationprefname";
    SharedPreferences.Editor location_sharedpref_editor;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        location_sharedpref = getActivity().getSharedPreferences(LOCATIONPEF_NAME,Context.MODE_PRIVATE);
        location_sharedpref_editor = location_sharedpref.edit();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.gps_api_key));
        }
        PlacesClient placesClient = Places.createClient(getActivity());
//        MapsInitializer.initialize(this.getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setCountries("PH");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                coorLat = place.getLatLng().latitude;
                coorLng = place.getLatLng().longitude;
                putLocation(coorLat,coorLng);

                LatLng place_latlng = new LatLng(coorLat, coorLng);
                checkInsideCircle(place_latlng);

                setMarker(coorLat,coorLng);

                place_search = true;

//                Toast.makeText(getActivity(),place.getLatLng().toString(),Toast.LENGTH_LONG).show();

                Log.d("new_check","Place Selected: " + place_latlng + " , My Location: " + mylatlng);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.d("check", "Place Error : " + status);
            }
        });


        btn_sendalert = view.findViewById(R.id.btn_sendalert_id);
        btn_sendalert.setOnClickListener(v -> {
            new getGeocoding().execute();
        });
        return view;
    }

    private void putLocation(double loclat,double loclng){
        location_sharedpref_editor.putBoolean("has_location", true);
        location_sharedpref_editor.putString("coorlat", String.valueOf(loclat));
        location_sharedpref_editor.putString("coorlng", String.valueOf(loclng));
    }


    private void startTimer() {
        btn_sendalert.setClickable(false);
        btn_sendalert.setEnabled(false);

        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDown();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDown();
                updateSendButton();
                if (!mTimerRunning) {
                    btn_sendalert.setClickable(true);
                    btn_sendalert.setEnabled(true);
                    btn_sendalert.setText("Send Alert");
                    location_sharedpref_editor = location_sharedpref.edit();
                    location_sharedpref_editor.clear();
                    location_sharedpref_editor.commit();
                    location_sharedpref_editor.apply();
                }

            }
        }.start();

        mTimerRunning = true;
        updateSendButton();
    }

    private void updateCountDown() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        Log.d("check", "Time : " + timeLeftFormatted);

        btn_sendalert.setText(timeLeftFormatted);
    }

    private void updateSendButton() {
        Log.d("check", "UpdateSendButton:" + mTimerRunning);
        if (mTimerRunning) {
            btn_sendalert.setClickable(false);
            btn_sendalert.setEnabled(false);
        } else {
            btn_sendalert.setText("Send Alert");
            location_sharedpref_editor.clear();
            location_sharedpref_editor.commit();
            location_sharedpref_editor.apply();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }


    private void Started() {
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);

        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDown();
        updateSendButton();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            Log.d("check", "Time Left Millis" + mTimeLeftInMillis);

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDown();
                updateSendButton();
                Log.d("check", "mTimeLeftInMillis < 0 = " + false);

            } else {
                Log.d("check", "mTimeLeftInMillis < 0 = " + true);
                startTimer();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }


    private class getGeocoding extends AsyncTask<String, Void, String> {
        ProgressDialog pd;

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            String latlng = coorLat + "," + coorLng + "&key=" + getResources().getString(R.string.gps_api_key);
            Request request = new Request.Builder()
                    .url(MyConfig.geocoding + latlng)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return null;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(getContext());
            pd.setMessage("Getting Address...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }


        @Override
        protected void onPostExecute(String s) {
            try {
                if (!s.isEmpty() || s != null) {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray geocode_result = jsonObject.getJSONArray("results");
                    if (geocode_result != null) {
                        JSONObject geocode_address = geocode_result.getJSONObject(0);
                        str_address = geocode_address.getString("formatted_address");
                    }

                    Log.d("check", "Address : " + str_address);
                }

                pd.dismiss();
                new sendAlert().execute();
                SharedPreferences myprofile = getActivity().getSharedPreferences(Login.PROFILEPREF_NAME, MODE_PRIVATE);
                String id = myprofile.getString("id", "");

                Log.d("check", "ID: " + id + "\n" + "LATITUDE: " + String.valueOf(coorLat) + "\n" + "LONGITUDE: " + coorLng + "\n" + "ADDRESS: " + str_address);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private class sendAlert extends AsyncTask<String, Void, String> {
        ProgressDialog pd;
        private SharedPreferences myprofile = getActivity().getSharedPreferences(Login.PROFILEPREF_NAME, MODE_PRIVATE);
        String id = myprofile.getString("id", "");

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id", id)
                    .addFormDataPart("latitude", String.valueOf(coorLat))
                    .addFormDataPart("longitude", String.valueOf(coorLng))
                    .addFormDataPart("address", str_address)
                    .build();

            Request request = new Request.Builder()
                    .url(MyConfig.base_url + "/alert")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()) {
                    return response.body().string();
                } else {
                    return null;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(getContext());
            pd.setMessage("Sending Alert...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("check", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    startTimer();
                    location_sharedpref_editor.commit();
                    location_sharedpref_editor.apply();
                    Toast.makeText(getActivity(), "Alert Sent Successfully", Toast.LENGTH_LONG).show();
                    Log.d("check", "Alert Sent Successfully");
                } else {
                    Toast.makeText(getActivity(), "Alert Failed", Toast.LENGTH_LONG).show();
                    Log.d("check", "Alert Failed");
                }
                pd.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.setOnMyLocationButtonClickListener(this);
        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        gMap.setOnMapClickListener(this);
        updateLocationUI();

        boolean is_contains_location = location_sharedpref.contains("has_location");
        if (is_contains_location){
            boolean has_location = location_sharedpref.getBoolean("has_location",false);
            if (has_location){

            }
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

            if (mLocationPermissionGranted) {

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {

                    if (location != null) {
                        if (!mTimerRunning) {
                            if (!btn_sendalert.isEnabled()){
                                btn_sendalert.setClickable(true);
                                btn_sendalert.setEnabled(true);
                            }
                            location_sharedpref_editor.clear();
                            location_sharedpref_editor.commit();
                            location_sharedpref_editor.apply();
                            setMyLocation(location);
                        }
                        else{
                            boolean has_location = location_sharedpref.getBoolean("has_location",false);
                            if (has_location){
                                double location_lat = Double.parseDouble(location_sharedpref.getString("coorlat",""));
                                double location_lng = Double.parseDouble(location_sharedpref.getString("coorlng",""));
                                setMarker(location_lat,location_lng);
                                Log.d("new_check","has location: " + has_location);
                            }
                            else{
                                setMyLocation(location);
                            }
                        }


                    } else {
                        requestNewLocationData();
                        Log.d("new_check", "Location: No Location Found");

                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setMyLocation(Location location){
        coorLat = location.getLatitude();
        coorLng = location.getLongitude();
        putLocation(coorLat,coorLng);
        LatLng latLng = new LatLng(coorLat, coorLng);
        mylatlng = latLng;
        addCircleGeofence(latLng);
        setMarker(coorLat,coorLng);
        updateLocationUI();
        Log.d("new_check", "Location: Location Found On getDeviceLocation " + latLng);
    }

    public void addCircleGeofence(LatLng latLanggeofence){
        CircleOptions circleOptionsNearby = new CircleOptions();
        circleOptionsNearby.center( latLanggeofence );
        circleOptionsNearby.radius( 200 );
        circleOptionsNearby.strokeColor(Color.TRANSPARENT);
        circleRelocate = gMap.addCircle(circleOptionsNearby);
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            coorLat = mLastLocation.getLatitude();
            coorLng = mLastLocation.getLongitude();
            putLocation(coorLat,coorLng);
            LatLng latLng = new LatLng(coorLat, coorLng);
            mylatlng = latLng;
            addCircleGeofence(latLng);
            setMarker(coorLat,coorLng);
             Log.d("new_check","Location: Location Found LocationCallback");
            btn_sendalert.setClickable(true);
            btn_sendalert.setEnabled(true);
            updateLocationUI();
        }
    };

    private void updateLocationUI() {
        if (gMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                gMap.setMyLocationEnabled(true);
                gMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                gMap.setMyLocationEnabled(false);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("check","MAP CLICKED");
        checkInsideCircle(latLng);
        coorLat = latLng.latitude;
        coorLng = latLng.longitude;
        putLocation(coorLat,coorLng);
        setMarker(coorLat,coorLng);
        updateLocationUI();


        current = new LatLng(coorLat, coorLng);
//        Toast.makeText(getActivity(), current.toString(), Toast.LENGTH_LONG).show();
    }

    public void checkInsideCircle(LatLng markerlatlng){
        double my_latitude = markerlatlng.latitude;
        double my_longitude = markerlatlng.longitude;
        double markerlat = mylatlng.latitude;
        double markerlng = mylatlng.longitude;
        float[] distance = new float[2];
        Location.distanceBetween(markerlat,markerlng,my_latitude,my_longitude,distance);
        if (distance[0] < radius){
            if (!btn_sendalert.isEnabled()){
                btn_sendalert.setEnabled(true);
            }
            Log.d("new_check","Inside Circle");
        }
        else{
            if (btn_sendalert.isEnabled()){
                btn_sendalert.setEnabled(false);
            }
            Log.d("new_check","Outside Circle");
        }
    }

    private void setMarker(double location_lat,double location_lng){
        LatLng location_latlng = new LatLng(location_lat,location_lng);
        if(myMarker == null){
            myMarker = gMap.addMarker(new MarkerOptions().title("Location").position(location_latlng));
        }else{
            myMarker.setPosition(location_latlng);
        }
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location_latlng, 18));
    }


    @Override
    public void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                mLocationPermissionGranted = true;
                boolean gps_status = isGPSEnabled();
                if (gps_status){
                    if (!place_search){
                        getDeviceLocation();
                        Log.d("new_check","Place not Searched");
                    }
                    else{
                        Log.d("new_check","Place Searched");
                    }
                }
            }
            else{
                getLocationPermission();
            }
        }
        Started();
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            return true;
        }
        return false;
    }


    public boolean isGPSEnabled(){
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    });
            final AlertDialog alert = builder.create();
            if (!alert.isShowing()){
                alert.show();
            }
            Log.d("check","Check is GPS Enabled?");
            return false;
        }
        else{
            Log.d("check","Check is GPS Enabled?");
            return true;
        }
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mLocationPermissionGranted = true;
            boolean gps_status = isGPSEnabled();
            if (gps_status){
                getDeviceLocation();
            }
            Log.d("check","GPS STATUS: "+gps_status);
            Log.d("check","Permission: " + mLocationPermissionGranted);

        } else {
            Log.d("check","Permission: " + shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION));

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Show permission explanation dialog...
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }else{
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//                Snackbar.make(getView(), R.string.snackbar_content,
//                        Snackbar.LENGTH_INDEFINITE)
//                        .setAction(R.string.ok, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
//                                intent.setData(uri);
//                                startActivity(intent);
//                            }
//                        })
//                        .show();
                //Never ask again selected, or device policy prohibits the app from having that permission.
                //So, disable that feature, or fall back to another situation...
            }
        }
    }

    public boolean isServicesOK(){
        Log.d("check", "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d("check", "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d("check", "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(getActivity(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        Log.d("check","PERMISSION: " + permission);
        return super.shouldShowRequestPermissionRationale(permission);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("check","Permission");
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
//                getDeviceLocation();
            }
        }
    }



}
