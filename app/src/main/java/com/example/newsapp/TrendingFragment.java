package com.example.newsapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrendingFragment extends Fragment {

    private ArrayList<Entry> values;
    private LineChart lineChart;
    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trending, container, false);

        //setting up line chart
        lineChart = view.findViewById(R.id.line_chart);
        lineChart.setDragEnabled(false);
        lineChart.getLegend().setTextSize(15);
        lineChart.getLegend().setTextColor(Color.BLACK);
        lineChart.getLegend().setFormSize(15);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        //setting up edit text
        editText = view.findViewById(R.id.input_text);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    createGraph();
                    return true;
                }
                return false;
            }
        });

        //create the graph
        createGraph();

        return view;
    }


    private void createGraph() {
        values = new ArrayList<>();
        String temp_keyword = editText.getText().toString();
        if(temp_keyword.equals("")){
            temp_keyword = editText.getHint().toString();
        }
        final String keyword = temp_keyword;
        String trending_endpoint = "https://expressapp-android.wl.r.appspot.com/guardian/trending/" + keyword;
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, trending_endpoint, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LineDataSet lineDataSet = null;
                        try {
                            JSONArray timelineData = response.getJSONObject("default").getJSONArray("timelineData");
                            for (int i = 0; i < timelineData.length(); i++) {
                                JSONObject current = timelineData.getJSONObject(i);
                                int value = current.getJSONArray("value").getInt(0);
                                values.add(new Entry(i, value));
                                lineDataSet = new LineDataSet(values,"Trending Chart for " + keyword);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayList<ILineDataSet> dataSet = new ArrayList<>();
                        if(lineDataSet != null){
                            lineDataSet.setCircleColor(getResources().getColor(R.color.graphColor));
                            lineDataSet.setCircleHoleColor(getResources().getColor(R.color.graphColor));
                            lineDataSet.setValueTextColor(getResources().getColor(R.color.graphColor));
                            lineDataSet.setColor(getResources().getColor(R.color.graphColor));
                            dataSet.add(lineDataSet);
                            lineChart.setData(new LineData(dataSet));
                            lineChart.invalidate();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(request);
    }
}
