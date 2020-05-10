package com.example.newsapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DetailArticlePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_article_page);
        final Context context = this;
        //set up action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //spinner
        final RelativeLayout spinner = findViewById(R.id.spinner);
        final CardView cardView = findViewById(R.id.news_card);
        spinner.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.GONE);

        //get the id of the article
        Bundle bundle = getIntent().getExtras();
        final String id = bundle.getString("id");

        //find views
        final TextView toolbar_title = findViewById(R.id.toolbar_title);
        final ImageView news_img = findViewById(R.id.news_image);
        final TextView title = findViewById(R.id.news_title);
        final TextView section = findViewById(R.id.news_section);
        final TextView date = findViewById(R.id.news_date);
        final TextView desc = findViewById(R.id.news_description);
        final TextView url = findViewById(R.id.news_url);
        desc.setMovementMethod(LinkMovementMethod.getInstance());
        url.setMovementMethod(LinkMovementMethod.getInstance());
        final ImageView twitter = findViewById(R.id.twitter_icon);

        //onclick for bookmark
        final ImageView bookmark = findViewById(R.id.bookmark_icon);
        final SharedPreferences pref = this.getSharedPreferences("MyPref", 0);
        final SharedPreferences.Editor editor = pref.edit();

        //request
        RequestQueue queue = Volley.newRequestQueue(this);
        String article ="https://expressapp-android.wl.r.appspot.com/guardian/article?id=" + id;
        JsonObjectRequest news_request = new JsonObjectRequest(Request.Method.GET, article, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject content = response.getJSONObject("response").getJSONObject("content");
                            final String news_title = content.getString("webTitle");
                            String news_section = content.getString("sectionName");
                            final String news_url = content.getString("webUrl");
                            String viewFull = "<a href=\"" + news_url + "\">View Full Article</a>";
                            String news_date = convertNewsDate(content.getString("webPublicationDate"));
                            //image
                            JSONObject blocks = content.getJSONObject("blocks");
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
                            //news description
                            StringBuilder news_desc_builder = new StringBuilder();
                            JSONArray body = blocks.getJSONArray("body");
                            for(int i=0;i<body.length();i++){
                                JSONObject item = body.getJSONObject(i);
                                news_desc_builder.append(item.getString("bodyHtml"));
                            }
                            String news_desc = news_desc_builder.toString();

                            //setting up the view
                            toolbar_title.setText(news_title);
                            Picasso.with(context).load(news_image).into(news_img);
                            title.setText(news_title);
                            section.setText(news_section);
                            date.setText(news_date);
                            desc.setText(HtmlCompat.fromHtml(news_desc,HtmlCompat.FROM_HTML_MODE_LEGACY));
                            url.setText(HtmlCompat.fromHtml(viewFull,HtmlCompat.FROM_HTML_MODE_LEGACY));

                            //json object
                            JSONObject data = new JSONObject();
                            try {
                                data.put("url",news_url);
                                data.put("title",news_title);
                                data.put("date",content.getString("webPublicationDate"));
                                data.put("section",news_section);
                                data.put("image",news_image);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            final String jsonString = data.toString();

                            //onclick listener for bookmark
                            if(pref.getString(id, "false").equals("false")){
                                bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                                bookmark.setTag("notMarked");
                            }else{
                                bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                                bookmark.setTag("marked");
                            }
                            bookmark.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(bookmark.getTag() == "notMarked"){
                                        bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                                        bookmark.setTag("marked");
                                        editor.putString(id,jsonString);
                                        editor.commit();
                                        Toast.makeText(v.getContext(), "\"" + news_title+ "\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
                                    }else{
                                        bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                                        bookmark.setTag("notMarked");
                                        editor.remove(id);
                                        editor.commit();
                                        Toast.makeText(v.getContext(), "\"" + news_title + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            //onclick listener for twitter
                            twitter.setImageResource(R.drawable.bluetwitter);
                            twitter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String intent_url = "https://twitter.com/intent/tweet?text=Check+out+this+Link&url=" + news_url + "&hashtags=CSCI571NewsSearch";
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(intent_url));
                                    startActivity(intent);
                                }
                            });
                            spinner.setVisibility(View.GONE);
                            cardView.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        queue.add(news_request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String convertNewsDate(String news_date){
        LocalDateTime time = LocalDateTime.parse(news_date, DateTimeFormatter.ISO_DATE_TIME);
        time = time.minusHours(7);
        String str_time = time.toString();
        String year = str_time.substring(0,4);
        String month = str_time.substring(5,7);
        String date = str_time.substring(8,10);
        String actual_month="false";
        switch (month){
            case "01":
                actual_month = "Jan";
                break;
            case "02":
                actual_month = "Feb";
                break;
            case "03":
                actual_month = "Mar";
                break;
            case "04":
                actual_month = "Apr";
                break;
            case "05":
                actual_month = "May";
                break;
            case "06":
                actual_month = "Jun";
                break;
            case "07":
                actual_month = "Jul";
                break;
            case "08":
                actual_month = "Aug";
                break;
            case "09":
                actual_month = "Sep";
                break;
            case "10":
                actual_month = "Oct";
                break;
            case "11":
                actual_month = "Nov";
                break;
            case "12":
                actual_month = "Dec";
                break;
        }
        return date + " " + actual_month + " " + year;
    }
}
