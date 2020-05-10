package com.example.newsapp;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tabs extends Fragment {
    private String category;
    private NewscardAdapter adapter;
    private boolean isBack;
    private SwipeRefreshLayout swipeRefreshLayout;

    Tabs(String category) {
        this.category = category;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isBack=false;
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_tabs, container, false);

        //set recyclerview
        final RecyclerView recyclerView = view.findViewById(R.id.tabs_news_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new NewscardAdapter(getActivity(), new ArrayList<Newscard>());
        recyclerView.setAdapter(adapter);

        //set spinner
        final RelativeLayout spinner = view.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);

        //set swipe refresh layout
        swipeRefreshLayout = view.findViewById(R.id.swiperefresh_items);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewsData(recyclerView,spinner);
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

        //fetch news data
        fetchNewsData(recyclerView,spinner);

        return view;
    }

    private void fetchNewsData(final RecyclerView recyclerView, final RelativeLayout spinner){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String endpoint = "https://expressapp-android.wl.r.appspot.com/guardian/" + this.category;
        JsonObjectRequest news_request = new JsonObjectRequest(Request.Method.GET, endpoint, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        adapter = new NewscardAdapter(getActivity(),renderNewsCard(response));
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
            JSONArray articles = response.getJSONArray("articles");
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
