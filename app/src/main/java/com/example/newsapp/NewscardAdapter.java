package com.example.newsapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NewscardAdapter extends RecyclerView.Adapter<NewscardAdapter.NewscardViewHolder> {

    private Context mCtx;
    private List<Newscard> newsCardList;

    NewscardAdapter(Context mCtx, List<Newscard> newsCardList){
        this.mCtx = mCtx;
        this.newsCardList = newsCardList;
    }

    @NonNull
    @Override
    public NewscardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.news_card, null);
        return new NewscardViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final NewscardViewHolder holder, int position) {
        final Newscard newscard = newsCardList.get(position);
        final String id = newscard.getNews_id();
        holder.news_title.setText(newscard.getNews_title());
        holder.news_time.setText(parseTime(newscard.getNews_time()));
        holder.news_section.setText(newscard.getNews_section());
        Picasso.with(mCtx).load(newscard.getImage_url()).into(holder.news_image);
        final NewscardAdapter content = this;

        //create a json object string to store the info
        JSONObject data = new JSONObject();
        try {
            data.put("url",newscard.getNews_url());
            data.put("title",newscard.getNews_title());
            data.put("date",newscard.getNews_time());
            data.put("section",newscard.getNews_section());
            data.put("image",newscard.getImage_url());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String jsonString = data.toString();

        //on click listener for bookmark
        final SharedPreferences pref = mCtx.getSharedPreferences("MyPref", 0);
        final SharedPreferences.Editor editor = pref.edit();

        if(pref.getString(id, "false").equals("false")){
            holder.news_bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
            holder.news_bookmark.setTag("notMarked");
        }else{
            holder.news_bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
            holder.news_bookmark.setTag("marked");
        }

        holder.news_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.news_bookmark.getTag() == "notMarked"){
                    holder.news_bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                    holder.news_bookmark.setTag("marked");
                    editor.putString(id,jsonString);
                    editor.commit();
                    Toast.makeText(v.getContext(), "\"" + newscard.getNews_title()+ "\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
                }else{
                    holder.news_bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    holder.news_bookmark.setTag("notMarked");
                    editor.remove(id);
                    editor.commit();
                    Toast.makeText(v.getContext(), "\"" + newscard.getNews_title()+ "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //onclick for card
        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),DetailArticlePage.class);
                intent.putExtra("id",id);
                v.getContext().startActivity(intent);
            }
        });

        //long click for card
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(mCtx);
                dialog.setContentView(R.layout.dialog);
                //get view
                TextView dialog_title = dialog.findViewById(R.id.dialog_title);
                ImageView dialog_image = dialog.findViewById(R.id.dialog_image);
                final ImageView dialog_bookmark = dialog.findViewById(R.id.dialog_bookmark);
                ImageView dialog_twitter = dialog.findViewById(R.id.dialog_twitter);
                //set view
                dialog_title.setText(newscard.getNews_title());
                Picasso.with(mCtx).load(newscard.getImage_url()).into(dialog_image);
                if(pref.getString(id, "false").equals("false")){
                    dialog_bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    dialog_bookmark.setTag("notMarked");
                }else{
                    dialog_bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                    dialog_bookmark.setTag("marked");
                }
                //set listener
                dialog_twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String intent_url = "https://twitter.com/intent/tweet?text=Check+out+this+Link&url=" + newscard.getNews_url() + "&hashtags=CSCI571NewsSearch";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(intent_url));
                        mCtx.startActivity(intent);
                    }
                });
                dialog_bookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(dialog_bookmark.getTag() == "notMarked"){
                            dialog_bookmark.setImageResource(R.drawable.ic_bookmark_black_24dp);
                            dialog_bookmark.setTag("marked");
                            content.notifyDataSetChanged();
                            editor.putString(id,jsonString);
                            editor.commit();
                            Toast.makeText(v.getContext(), "\"" + newscard.getNews_title()+ "\" was added to Bookmarks", Toast.LENGTH_SHORT).show();
                        }else{
                            dialog_bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                            dialog_bookmark.setTag("notMarked");
                            content.notifyDataSetChanged();
                            editor.remove(id);
                            editor.commit();
                            Toast.makeText(v.getContext(), "\"" + newscard.getNews_title()+ "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if(newsCardList != null && !newsCardList.isEmpty()){
            return newsCardList.size();
        }else{
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String parseTime(String news_time){
        LocalDateTime time = LocalDateTime.parse(news_time, DateTimeFormatter.ISO_DATE_TIME);
        time = time.minusHours(7);

        long hours = time.until(LocalDateTime.now(), ChronoUnit.HOURS);
        long minutes = time.until(LocalDateTime.now(), ChronoUnit.MINUTES);
        long seconds = time.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        if(hours==0 && minutes==0){
            return seconds + "s ago";
        }else if(hours==0){
            return minutes + "m ago";
        }else{
            return hours + "h ago";
        }
    }

    class NewscardViewHolder extends RecyclerView.ViewHolder {
        ImageView news_image, news_bookmark;
        TextView news_title, news_time, news_section;
        CardView cardView;

        NewscardViewHolder(View itemView){
            super(itemView);
            /*
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    System.out.println("youclicked!!!!");
                }
            });*/
            cardView = itemView.findViewById(R.id.card_view);
            news_bookmark = itemView.findViewById(R.id.news_bookmark);
            news_title = itemView.findViewById(R.id.news_title);
            news_time = itemView.findViewById(R.id.news_time);
            news_section = itemView.findViewById(R.id.news_section);
            news_image = itemView.findViewById(R.id.news_image);
        }
    }
}
