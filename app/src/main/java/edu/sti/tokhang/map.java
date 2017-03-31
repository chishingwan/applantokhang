package edu.sti.tokhang;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.arsy.maps_library.MapRipple;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class map extends AppCompatActivity implements OnMapReadyCallback
                                                            ,LocationListener
                                                            ,GoogleApiClient.ConnectionCallbacks
                                                            ,GoogleApiClient.OnConnectionFailedListener
                                                            ,GoogleMap.OnMarkerClickListener{
    NotificationCompat.Builder notification;
    private  static final int uniqueID=45612;

    private GoogleMap mMap;
    private Marker myLocation;
    Double minDist = 200.0;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest    ;
    private long UPDATE_INTERVAL = 10 * 1000;
    private long FASTEST_INTERVAL = 2000;

    LatLngs coord;

    MapRipple mapRipple;

    private LatLng center;
    ArrayList<LatLng> markerList;
    ArrayList<String> accType;
    ArrayList<String> dateList;
    ArrayList<String> timeList;
    ArrayList<Double>distanceList;

    DistanceJava dj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        notif();
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            populateDb();


        }
            catch(Exception e){
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            showMessage("error",errors.toString());
        }
            finally {

        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerClickListener(this);
        getCoordinates();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(coord.getLatitude(), coord.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(coord.getLatitude(), coord.getLongitude()))      // Sets the center of the map to location user
                .zoom(20)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                //.tilt(40)                 // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        // Getting latitude of the current location


    }

    public void addMarkersToMap(){

        mMap.clear();
        getData();
        myLocation = mMap.addMarker(new MarkerOptions()
                .position(center)
                .title("Your Current Location")
                .snippet("")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .infoWindowAnchor(0.5f, 0.5f));

        for(int i =0;i<markerList.size();i++){

            int x=R.drawable.redholdup;
            switch(accType.get(i)){
                case "Hold-up":
                    x=R.drawable.redholdup;
                    break;
                case "Murder":
                    x=R.drawable.redmurder;
                    break;
                case "Rape":
                    x=R.drawable.yellowrape;
                    break;

            }
            mMap.addMarker(new MarkerOptions()
                    .position(markerList.get(i))
                    .snippet("Date : "+dateList.get(i).toString()+" Time : "+timeList.get(i).toString())
                    .title(accType.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(x))
                    .infoWindowAnchor(0.5f,0.5f));
        }

    }

    public void getCoordinates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                RationaleDialog.newInstance(MY_PERMISSION_ACCESS_FINE_LOCATION, true)
                        .show(this.getSupportFragmentManager(), "dialog");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_FINE_LOCATION);
            }

        }
        startLocationUpdates();
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mCurrentLocation != null) {
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            coord = new LatLngs(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }


        if (mapRipple.isAnimationRunning()) {
            mapRipple.stopRippleMapAnimation();
        }
    }


    public void onConnected(Bundle dataBundle) {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                RationaleDialog.newInstance(MY_PERMISSION_ACCESS_FINE_LOCATION, true)
                        .show(this.getSupportFragmentManager(), "dialog");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_FINE_LOCATION);
            }

        }
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    public void onLocationChanged(Location location) {
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        coord = new LatLngs(location.getLatitude(), location.getLongitude());
        center = new LatLng(location.getLatitude(), location.getLongitude());
        addMarkersToMap();

        dj = new DistanceJava(markerList,center);

        distanceList=dj.getDistance();

        if(mapRipple!=null){
            if (mapRipple.isAnimationRunning()) {
                mapRipple.stopRippleMapAnimation();
            }
        }
        mapRipple = new MapRipple(mMap, center, getApplicationContext());
        mapRipple.withFillColor(Color.parseColor("#FFA3D2E4"));
        mapRipple.withStrokewidth(0);
        mapRipple.withDistance(200);
        mapRipple.withRippleDuration(6000);

        // Start Animation again only if it is not running
        if (!mapRipple.isAnimationRunning()) {
            mapRipple.startRippleMapAnimation();
        }

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(
                            1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker.setAnchor(0.5f, 0.7f + 2 * t);

                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });

        return false;
    }


    public void showMessage(String title,String message){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

public static class RationaleDialog extends DialogFragment {

    private static final String ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode";

    private static final String ARGUMENT_FINISH_ACTIVITY = "finish";

    private boolean mFinishActivity = false;

    public static RationaleDialog newInstance(int requestCode, boolean finishActivity) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode);
        arguments.putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity);
        RationaleDialog dialog = new RationaleDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        final int requestCode = arguments.getInt(ARGUMENT_PERMISSION_REQUEST_CODE);
        mFinishActivity = arguments.getBoolean(ARGUMENT_FINISH_ACTIVITY);

        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.permission_rationale_location)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // After click on Ok, request the permission.
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                requestCode);
                        // Do not finish the Activity while requesting permission.
                        mFinishActivity = false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mFinishActivity) {
            Toast.makeText(getActivity(),
                    R.string.permission_required_toast,
                    Toast.LENGTH_SHORT)
                    .show();
            getActivity().finish();
        }
    }
}

    SQLiteDatabase db;
    public void populateDb(){
        try {

        }
        catch(Exception e){

        }

    }

    public void getData(){
        db = openOrCreateDatabase(
                "tokhangdb"
                , SQLiteDatabase.CREATE_IF_NECESSARY
                , null
        );
        Cursor c = db.rawQuery("Select * from markertbl", null);

        markerList = new ArrayList<>();
        accType = new ArrayList<>();
        dateList = new ArrayList<>();
        timeList = new ArrayList<>();
        while (c.moveToNext()) {
            markerList.add(new LatLng(Double.parseDouble(c.getString(1)),Double.parseDouble(c.getString(2))));
            accType.add(c.getString(0));
            dateList.add(c.getString(3));
            timeList.add(c.getString(4));

        }
        db.close();
    }


    public void notif(){
        notification.setSmallIcon(R.drawable.mic);
        notification.setTicker("Tokhang");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Tokhang Alert");
        notification.setContentText("You are in an area where a lot of snatching and hold-up happened please secure your belongings");
        Intent intent= new Intent(this,map.class);
        //PendingIntent pendingIntent = new PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID,notification.build());
        notification.setVibrate(new long[] { 1000, 1000});


    }

    public void sendSms(){

    }


    public ArrayList<Boolean> nearbyMark(ArrayList<Double> distanceList,double min){
        ArrayList<Boolean> nearMark = new ArrayList<>();
        for(int i=0;i<distanceList.size();i++){
            if(distanceList.get(i)<=minDist){
                nearMark.add(true);
            }
            else{
                nearMark.add(false);
            }
        }
        return nearMark;

    }
}
