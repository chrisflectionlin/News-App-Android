package com.example.newsapp;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment implements LocationListener {
    private List<Newscard> newscardList;
    private NewscardAdapter adapter;
    private boolean isBack;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View homeView;
    private RelativeLayout spinner;
    private LocationManager locationManager;
    private CardView weatherCard;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeView = inflater.inflate(R.layout.fragment_home, container, false);
        isBack = false;
        weatherCard = homeView.findViewById(R.id.weather_card);
        weatherCard.setVisibility(View.GONE);
        //location
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //check permission
        checkLocationPermission();

        //set spinner
        spinner = homeView.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);

        //set recyclerView
        recyclerView = homeView.findViewById(R.id.news_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new NewscardAdapter(getActivity(), new ArrayList<Newscard>());
        recyclerView.setAdapter(adapter);

        //set up swipe refresh
        swipeRefreshLayout = homeView.findViewById(R.id.swiperefresh_items);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewsData(recyclerView, spinner);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });

        //render stuff if permission is granted
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation("gps");
            assert location != null;
            //fetch weather data
            fetchWeatherData(location.getLongitude(),location.getLatitude());
            //fetch news data
            fetchNewsData(recyclerView, spinner);
        }



        return homeView;
    }

    private void fetchWeatherData(double lng, double lat){
        //show card
        weatherCard.setVisibility(View.VISIBLE);

        //get city and state name
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
            final String cityName = addresses.get(0).getLocality();
            final String stateName = addresses.get(0).getAdminArea();

            //request weather data
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String weather_apiKey = "6153811fdd2c8806d074abb818ffada5";
            String weather_url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName +","+ stateName + "&units=metric&appid=" + weather_apiKey;
            JsonObjectRequest weather_request = new JsonObjectRequest(Request.Method.GET, weather_url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            setWeatherCard(response, homeView,cityName,stateName);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            queue.add(weather_request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setWeatherCard(JSONObject response, View homeView,String cityName, String stateName) {
        try {
            JSONArray weatherArray = response.getJSONArray("weather");
            String condition_string = weatherArray.getJSONObject(0).getString("main");

            JSONObject degreeObj = new JSONObject(response.getString("main"));
            String degree_string = degreeObj.getString("temp");
            int degree_int = (int) Math.round(Double.parseDouble(degree_string));

            TextView city = homeView.findViewById(R.id.weather_city);
            TextView state = homeView.findViewById(R.id.weather_state);
            TextView degree = homeView.findViewById(R.id.weather_degree);
            TextView condition = homeView.findViewById(R.id.weather_condition);
            ConstraintLayout background = homeView.findViewById(R.id.weather_background);

            switch (condition_string) {
                case "Clouds":
                    background.setBackgroundResource(R.drawable.cloudy_weather);
                    break;
                case "Clear":
                    background.setBackgroundResource(R.drawable.clear_weather);
                    break;
                case "Snow":
                    background.setBackgroundResource(R.drawable.snowy_weather);
                    break;
                case "Rain":
                    background.setBackgroundResource(R.drawable.rainy_weather);
                    break;
                case "Drizzle":
                    background.setBackgroundResource(R.drawable.rainy_weather);
                    break;
                case "Thunderstorm":
                    background.setBackgroundResource(R.drawable.thunder_weather);
                    break;
                default:
                    background.setBackgroundResource(R.drawable.sunny_weather);
            }
            String degreeText = degree_int + " \u2103";

            condition.setText(condition_string);
            degree.setText(degreeText);
            city.setText(cityName);
            state.setText(stateName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void fetchNewsData(final RecyclerView recyclerView, final RelativeLayout spinner) {
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        newscardList = new ArrayList<>();

        String home_endpoint = "https://expressapp-android.wl.r.appspot.com/guardian";
        JsonObjectRequest news_request = new JsonObjectRequest(Request.Method.GET, home_endpoint, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        renderNewsCard(response);
                        adapter = new NewscardAdapter(getActivity(), newscardList);
                        recyclerView.setAdapter(adapter);
                        spinner.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(news_request);
    }

    private void renderNewsCard(JSONObject response) {
        try {
            JSONObject responseObj = new JSONObject(response.getString("response"));
            JSONArray articles = responseObj.getJSONArray("results");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject temp = articles.getJSONObject(i);
                String news_title = temp.getString("webTitle");
                String news_section = temp.getString("sectionName");
                String news_time = temp.getString("webPublicationDate");
                String news_id = temp.getString("id");
                String news_url = temp.getString("webUrl");
                String news_image = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                if (temp.getJSONObject("fields").has("thumbnail") && !temp.getJSONObject("fields").isNull("thumbnail")) {
                    news_image = temp.getJSONObject("fields").getString("thumbnail");
                }
                newscardList.add(new Newscard(news_image, news_title, news_time, news_section, news_id, news_url));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isBack = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isBack) {
            adapter.notifyDataSetChanged();
        }
    }

    //permission
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    //Request location updates:
                    locationManager.requestLocationUpdates("gps", 400, 1, this);
                }
                Location location = locationManager.getLastKnownLocation("gps");
                assert location != null;
                //fetch weather data
                fetchWeatherData(location.getLongitude(),location.getLatitude());
                //fetch news data
                fetchNewsData(recyclerView, spinner);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }
}
