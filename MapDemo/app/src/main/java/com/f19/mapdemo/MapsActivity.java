package com.f19.mapdemo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    public final static int REQUEST_CODE = 1;
    public static final String TAG = "MapsActivity";

    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Initializing the location request
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        getLocation();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // There is no permission needed when adding a marker on a map

        //Add marker for user location
        if(!checkPermission()) {
            requestPermission();
        } else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        // Get the last known location and from its coordinates add listener and create a marker for that
        // onMap Ready does this
//        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
//            @Override
//            public void onComplete(@NonNull Task<Location> task) {
//                if(task.isSuccessful() && task.getResult() != null) {
//                    Location lastlocation = task.getResult();
//
//                    //From the location add the marker
//                    LatLng userLocation = new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(userLocation).title("You were here!"));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
//                }
//            }
//        });



        /*  Commenting this for adding marker for user location
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        // Changing tp Toronto area
        //43.717899,-79.6582408
        LatLng toronto = new LatLng(43.717899,-79.6582408);

        //mMap.addMarker(new MarkerOptions().position(toronto).title("Marker in Toronto"));
        //Adding Item
        //Dragabble
        //mMap.addMarker(new MarkerOptions().position(toronto).title("Marker in Toronto").draggable(true));
        //Adding Icon with color
        mMap.addMarker(new MarkerOptions().position(toronto).title("Marker in Toronto").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 15));

        */
    }

    private boolean checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void getLocation() {
        // Let us initialize the locationcallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location: locationResult.getLocations()) {
                    // Clear the map
                    mMap.clear();

                    //From the location add the marker
                    LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("You were here!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {
                            Log.i(TAG,"onLocationResult: " + addresses.get(0));
                            String address = "";
                            if(addresses.get(0).getAdminArea() != null)
                                address += addresses.get(0).getAdminArea() + " ";
                            if(addresses.get(0).getCountryName() != null)
                                address += addresses.get(0).getCountryName() + " ";
                            if(addresses.get(0).getLocality() != null)
                                address += addresses.get(0).getLocality() + " ";
                            if(addresses.get(0).getPostalCode() != null)
                                address += addresses.get(0).getPostalCode() + " ";
                            if(addresses.get(0).getThoroughfare() != null)
                                address += addresses.get(0).getThoroughfare() + " ";

                            Toast.makeText(MapsActivity.this, address, Toast.LENGTH_SHORT).show();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE) {
            if(grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }
    }
}
