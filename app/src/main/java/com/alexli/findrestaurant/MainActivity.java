package com.alexli.findrestaurant;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;

    RequestQueue queue = null;

    String latitude = "-36.813940";
    String longitude = "174.728377";

    public RequestQueue getQueue(Context context)
    {
        if (queue == null)
        {
            queue = Volley.newRequestQueue(this);
        }

        return queue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Find The Restaurant");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = getQueue(getApplicationContext());

        final TextView text = (TextView) findViewById(R.id.txtJSON);

        Button getRequest = (Button) findViewById(R.id.btnGet);
//
//        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
//
//        try {
//            Task<Location> location = client.getLastLocation();
//
//            location.addOnCompleteListener(new OnCompleteListener<Location>() {
//                @Override
//                public void onComplete(@NonNull Task<Location> task) {
//
//                    latitude = Double.toString(task.getResult().getLatitude());
//                    longitude = Double.toString(task.getResult().getLongitude());
//                    System.err.println(task.getResult().getLatitude());
//                }
//            });
//        }
//        catch(SecurityException ex)
//        {
//            ex.printStackTrace();
//        }

        getRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {

                String url = "https://developers.zomato.com/api/v2.1/geocode?lat="+ latitude +"&lon="+ longitude;

                JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response)
                            {
                                StringBuilder restaurants = new StringBuilder();

                                try
                                {
                                    JSONArray data = response.getJSONArray("nearby_restaurants");

                                    for (int i = 0; i<data.length(); i++)
                                    {
                                        JSONObject restaurant = data.getJSONObject(i);

                                        JSONObject details = restaurant.getJSONObject("restaurant");
                                        JSONObject location = details.getJSONObject("location");

                                        restaurants.append(details.getString("name")+" ");
                                        restaurants.append(location.getString("address") + "\n");
                                    }
                                    System.err.println(response);
                                }
                                catch (JSONException e)
                                {
                                    e.printStackTrace();
                                }
                                text.setText(restaurants.toString());
                            }

                        }, new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error)
                                {

                                }
                            }
                        )
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("user-key", "69c36c060025607a4e61602530353882");
                        params.put("Accept", "application/json");

                        return params;
                    }
                };
                queue.add(jsObjRequest);
            }
        });

//        LocationRequest req = new LocationRequest();
//        req.setInterval(10000); // 10 seconds
//        req.setFastestInterval(5000); // 5 seconds
//        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        client.requestLocationUpdates(req,new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                latitude = Double.toString(locationResult.getLastLocation().getLatitude());
//                longitude = Double.toString(locationResult.getLastLocation().getLongitude());
//                Log.e("location:",locationResult.getLastLocation().toString());
//            }
//        },null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;


        final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task<Location> location = client.getLastLocation();

            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    latitude = Double.toString(task.getResult().getLatitude());
                    longitude = Double.toString(task.getResult().getLongitude());
                    System.err.println(task.getResult().getLatitude());
                    // Add a marker in Sydney and move the camera
                    LatLng sydney = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,13));

                }
            });
        }
        catch(SecurityException ex)
        {
            ex.printStackTrace();
        }

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,13));

        LocationRequest req = new LocationRequest();
        req.setInterval(10000); // 10 seconds
        req.setFastestInterval(5000); // 5 seconds
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client.requestLocationUpdates(req,new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                latitude = Double.toString(locationResult.getLastLocation().getLatitude());
                longitude = Double.toString(locationResult.getLastLocation().getLongitude());
                Log.e("location:",locationResult.getLastLocation().toString());
            }
        },null);
    }
}
