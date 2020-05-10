package com.example.newsapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RelativeLayout spinner;
    private boolean isBack;
    private NewscardAdapter adapter;
    private String queryString;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isBack=false;
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        //set up tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //get query string
        Bundle bundle = getIntent().getExtras();
        queryString = bundle.getString("queryString");
        //set title of the page
        TextView searchTitle = findViewById(R.id.search_title);
        String title_string = "Search Results for " + queryString;
        searchTitle.setText(title_string);

        //set recycler view
        recyclerView = findViewById(R.id.search_news_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new NewscardAdapter(this, new ArrayList<Newscard>());
        recyclerView.setAdapter(adapter);

        //set up pull to refresh
        swipeRefreshLayout = findViewById(R.id.swiperefresh_items);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewsData();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });

        //set up spinner
        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);

        fetchNewsData();
    }

    private void fetchNewsData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String endpoint = "https://expressapp-android.wl.r.appspot.com/guardian/search/" + queryString;
        JsonObjectRequest news_request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        adapter = new NewscardAdapter(SearchPage.this,renderNewsCard(response));
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

    private List<Newscard> renderNewsCard(JSONObject response){
        List<Newscard> newscardList = new ArrayList<>();
        try {
            JSONArray articles = response
                    .getJSONObject("response")
                    .getJSONArray("results");
            for(int i=0; i<articles.length();i++){
                JSONObject article = articles.getJSONObject(i);
                String news_title = article.getString("webTitle");
                String news_section = article.getString("sectionName");
                String news_time = article.getString("webPublicationDate");
                String news_id = article.getString("id");
                String news_url = article.getString("webUrl");
                JSONObject blocks = article.getJSONObject("blocks");
                String news_image = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                if(blocks.has("main") && !blocks.isNull("main")) {
                    JSONArray assets = blocks.getJSONObject("main")
                            .getJSONArray("elements")
                            .getJSONObject(0)
                            .getJSONArray("assets");
                    if (assets.length() > 0) {
                        news_image = assets
                                .getJSONObject(0)
                                .getString("file");
                    }
                }
                newscardList.add(new Newscard(news_image,news_title,news_time,news_section,news_id,news_url));
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return newscardList;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isBack = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isBack){
            adapter.notifyDataSetChanged();
        }
    }
}
