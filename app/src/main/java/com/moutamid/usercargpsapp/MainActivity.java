package com.moutamid.usercargpsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference db;
    double currentLat, currentLng = 0;
    double startLat, startLng = 0;
    private static final int REQUEST_LOCATION = 1;
    private GoogleApiClient client;
    private String time = "";
    private String speed = "";
    private String consumption = "2.04";
    private String distance = "0.0";
    private String location = "";
    private LocationRequest locationRequest;
    RadioButton stopBtn,moveBtn;
    int selectedRadioId;
    RadioGroup radioGroup1;
    private SharedPreferencesManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        radioGroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
        stopBtn=findViewById(R.id.stop);
        moveBtn = findViewById(R.id.moving);
        db = FirebaseDatabase.getInstance().getReference().child("Car");
        manager = new SharedPreferencesManager(this);

      //  Toast.makeText(MainActivity.this,""+startLat,Toast.LENGTH_LONG).show();
        checkInternetAndGPSConnection();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            showGPSDialogBox();
        }
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (stopBtn.isChecked()){
                    //status = "parked";
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("status","parked");
                    db.child(user.getUid()).updateChildren(hashMap);
                }else if (moveBtn.isChecked()){
                   // status = "moving";
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("status","moving");
                    db.child(user.getUid()).updateChildren(hashMap);
                    manager.storeDouble("lat", (float) currentLat);
                    manager.storeDouble("lng", (float) currentLng);
                }
            }
        });
        checkDataExists();
    }

    private void checkDataExists() {
        db.child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            CarDetails model = snapshot.getValue(CarDetails.class);
                            if (model.getStatus().equals("moving")){
                                moveBtn.setChecked(true);

                            }else {
                                stopBtn.setChecked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();

    }


    private void showGPSDialogBox() {
        LocationManager enable_gps = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!enable_gps.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder gps = new AlertDialog.Builder(this);
            gps.setMessage("Turn on GPS to find Location").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "No location updation without GPS", Toast.LENGTH_SHORT).show();
                }
            }).show();
        }
    }


    private void checkInternetAndGPSConnection() {
        ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!connect.isActiveNetworkMetered() && !info.isConnected()) {
            AlertDialog.Builder internet = new AlertDialog.Builder(MainActivity.this);
            internet.setMessage("Turn on Internet to see rider location")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "No route description without Internet", Toast.LENGTH_SHORT).show();
                        }
                    }).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bulidGoogleApiClient();
                // Toast.makeText(MainScreen.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainScreen.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
        getCompleteAddressString(currentLat,currentLng);
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date resultdate = new Date(timestamp);
        time = sdf.format(resultdate);
        speed = String.valueOf(location.getSpeed());
        calculateDist();
        Log.d("lat",""+ currentLat);
        updateDetails();
    }

    private void calculateDist() {
        startLat = manager.retrieveFloat("lat",0);
        startLng = manager.retrieveFloat("lng",0);
        Location location1 = new Location("");
        location1.setLatitude(startLat);
        location1.setLongitude(startLng);

        Location location2 = new Location("");
        location2.setLatitude(currentLat);
        location2.setLongitude(currentLng);

        float distanceInMeters = location1.distanceTo(location2);
        distance = String.valueOf(distanceInMeters);
    }

    private void getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                strAdd = returnedAddress.getLocality() + " , " +returnedAddress.getCountryName();
                //      HashMap<String,Object> hashMap = new HashMap<>();
                //    hashMap.put("location",strAdd);
                //  db.updateChildren(hashMap);
                location = strAdd;
            } else {
                Log.w("My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current", "Canont get Address!");
        }
    }


    private void updateDetails(){
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("speed",speed);
        hashMap.put("location",location);
        hashMap.put("lat",currentLat);
        hashMap.put("lng",currentLng);
        hashMap.put("distance",distance);
        hashMap.put("time",time);
        hashMap.put("consumption",consumption);
        db.child(user.getUid()).updateChildren(hashMap);
    }
}