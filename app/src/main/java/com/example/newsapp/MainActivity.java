package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private AutoSuggestAdapter autoSuggestAdapter;
    private SearchView.SearchAutoComplete searchAutoComplete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up bottom navigation
        BottomNavigationView navBar = findViewById(R.id.navbar);
        navBar.setOnNavigationItemSelectedListener(navListener);
        navBar.setOnNavigationItemReselectedListener(emptyListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    private BottomNavigationView.OnNavigationItemReselectedListener emptyListener =
            new BottomNavigationView.OnNavigationItemReselectedListener() {
                @Override
                public void onNavigationItemReselected(@NonNull MenuItem item) {

                }
            };

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selected = null;

                    switch(item.getItemId()){
                        case R.id.nav_home:
                            selected = new HomeFragment();
                            break;
                        case R.id.nav_headlines:
                            selected = new HeadlinesFragment();
                            break;
                        case R.id.nav_trending:
                            selected = new TrendingFragment();
                            break;
                        case R.id.nav_bookmark:
                            selected = new BookmarksFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selected).commit();
                    return true;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        final MenuItem searchItem = menu.findItem(R.id.search_view);

        //set search view, height
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);


        searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setAdapter(autoSuggestAdapter);
        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText(queryString);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this,SearchPage.class);
                intent.putExtra("queryString",query);
                MainActivity.this.startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>2){
                    makeApiCall(newText);
                }else{
                    autoSuggestAdapter.setData(new ArrayList<String>());
                    autoSuggestAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        return true;
    }

    private void makeApiCall(String text){
        //"Ocp-Apim-Subscription-Key": "20438b581e8b4b7b995afcc2f48ea2ff"
        RequestQueue queue = Volley.newRequestQueue(this);
        String autoSuggestApi = "https://csci571-beingautosuggestion.cognitiveservices.azure.com/bing/v7.0/suggestions?mkt=en-US&q=" + text;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, autoSuggestApi, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        List<String> stringList = new ArrayList<>();
                        try {
                            JSONArray searchSuggestions = response
                                    .getJSONArray("suggestionGroups")
                                    .getJSONObject(0)
                                    .getJSONArray("searchSuggestions");
                            int limit = 5;
                            if(searchSuggestions.length()<5){
                                limit = searchSuggestions.length();
                            }
                            for(int i=0;i<limit;i++){
                                JSONObject current = searchSuggestions.getJSONObject(i);
                                String displayText = current.getString("displayText");
                                stringList.add(displayText);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        autoSuggestAdapter.setData(stringList);
                        autoSuggestAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String,String> params =  new HashMap<>();
                params.put("Ocp-Apim-Subscription-Key","20438b581e8b4b7b995afcc2f48ea2ff");
                return params;
            }
        };

        queue.add(request);
    }
}
