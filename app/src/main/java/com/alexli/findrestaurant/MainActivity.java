package com.alexli.findrestaurant;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, LocationListener
{
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;

    private RequestQueue queue = null;
    private ImageLoader imageLoader = null;

    public RequestQueue getQueue(Context context)
    {
        if (queue == null)
        {
            queue = Volley.newRequestQueue(context);
        }

        return queue;
    }

    private class Restaurant
    {
        String latitude;
        String longitude;
        String name;
        String address;
        String cuisines;
        String costForTwo;
        String rating;

        public Restaurant(String latitude, String longitude, String name, String address, String cuisines, String costForTwo, String rating)
        {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
            this.address = address;
            this.cuisines = cuisines;
            this.costForTwo = costForTwo;
            this.rating = rating;
        }
    }

    ArrayList<Restaurant> nearestRestaurants = new ArrayList<Restaurant>();


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

        final ListView lstRestaurants = (ListView) findViewById(R.id.lstRestaurants);

        Button getRequest = (Button) findViewById(R.id.btnGet);

        getRequest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                if(lastLocation != null)
                {
                    final ArrayList<ItemView> arrayOfItemViews = new ArrayList<ItemView>();
                    mMap.clear();

                    LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));


                    for(Restaurant rest: nearestRestaurants)
                    {
                        LatLng restLatLng = new LatLng(Double.parseDouble(rest.latitude), Double.parseDouble(rest.longitude));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(restLatLng);
                        markerOptions.title(rest.name);
                        markerOptions.snippet(rest.address);
                        mMap.addMarker(markerOptions);

                        arrayOfItemViews.add(new ItemView(rest.name, "Cuisines: "+rest.cuisines, "Average cost for two: NZ$"+rest.costForTwo,
                                "Rating: "+rest.rating, rest.address));
                    }

                    UsersAdapter adapter = new UsersAdapter(getApplicationContext(), arrayOfItemViews);
                    lstRestaurants.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));

        String url = "https://developers.zomato.com/api/v2.1/geocode?lat="+ Double.toString(lastLocation.getLatitude()) +"&lon="+ Double.toString(lastLocation.getLongitude());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        nearestRestaurants.clear();
                        try
                        {
                            JSONArray data = response.getJSONArray("nearby_restaurants");

                            for (int i = 0; i<data.length(); i++)
                            {
                                JSONObject restaurant = data.getJSONObject(i);

                                JSONObject details = restaurant.getJSONObject("restaurant");
                                JSONObject location = details.getJSONObject("location");
                                JSONObject rating = details.getJSONObject("user_rating");

                                nearestRestaurants.add(new Restaurant(location.getString("latitude"), location.getString("longitude"),
                                        details.getString("name"), location.getString("address"), details.getString("cuisines"),
                                        details.getString("average_cost_for_two"), rating.getString("aggregate_rating")));
                            }
                            System.err.println(response);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
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
}
