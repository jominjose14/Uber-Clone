package com.parse.starter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    ListView requestListView;
    ArrayList<String> requests = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    ArrayList<Double> requestLatitudes = new ArrayList<Double>();
    ArrayList<Double> requestLongitudes = new ArrayList<Double>();

    public void updateListView(Location location) {
        if(location != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            final ParseGeoPoint geoPointLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            query.whereNear("location", geoPointLocation);
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null && objects != null && objects.size() > 0) {
                        requests.clear();
                        requestLongitudes.clear();;
                        requestLatitudes.clear();
                        for(ParseObject object : objects) {
                            ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");
                            if(requestLocation != null) {
                                Double distanceInMiles = geoPointLocation.distanceInMilesTo(requestLocation);
                                Double distanceOneDP = (double) Math.round(distanceInMiles * 10) / 10;
                                requests.add(distanceOneDP.toString() + " miles");
                                requestLatitudes.add(requestLocation.getLatitude());
                                requestLongitudes.add(requestLocation.getLongitude());
                            }
                        }
                    } else if(e == null){
                        requests.add("No active requests nearby...");
                    } else {
                        requests.add("- Error -");
                    }
                    arrayAdapter.notifyDataSetChanged();
                }
            });

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        setTitle("Nearby Requests");

        //Set up location service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateListView(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation != null) {
                updateListView(lastKnownLocation);
            } else {
                LatLng sydney = new LatLng(-34, 151);
            }
        }

        //Set up list view
        requestListView = (ListView) findViewById(R.id.requestListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);
        requestListView.setAdapter(arrayAdapter);
        requests.clear();
        requests.add("Getting nearby requests...");

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Location lastKnownLocation = null;
                if(ActivityCompat.checkSelfPermission(ViewRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(requestLatitudes.size() > i && requestLongitudes.size() > i && lastKnownLocation != null) {
                    Intent intent = new Intent(getApplicationContext(), DriverLocationActivity.class);
                    intent.putExtra("requestLatitude", requestLatitudes.get(i));
                    intent.putExtra("requestLongitude", requestLongitudes.get(i));
                    intent.putExtra("driverLatitude", lastKnownLocation.getLatitude());
                    intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation != null) {
                    updateListView(lastKnownLocation);
                } else {
                    LatLng sydney = new LatLng(-34, 151);
                }
            }
        }
    }
}
