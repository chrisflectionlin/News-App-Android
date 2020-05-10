package com.example.newsapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookmarkCardAdapter extends RecyclerView.Adapter<BookmarkCardAdapter.BookmarkCardViewHolder> {

    private Context mCtx;
    private List<Newscard> newsCardList;
    private View view;

    BookmarkCardAdapter(Context mCtx, List<Newscard> newsCardList,View view) {
        this.mCtx = mCtx;
        this.newsCardList = newsCardList;
        this.view = view;
    }

    @NonNull
    @Override
    public BookmarkCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.bookmark_card, null);
        return new BookmarkCardViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final BookmarkCardViewHolder holder, final int position) {
        final Newscard newscard = newsCardList.get(position);
        final String id = newscard.getNews_id();
        final BookmarkCardAdapter content = this;
        holder.news_title.setText(newscard.getNews_title());
        holder.news_time.setText(convertNewsDate(newscard.getNews_time()));
        holder.news_section.setText(newscard.getNews_section());
        Picasso.with(mCtx).load(newscard.getImage_url()).into(holder.news_image);

        //on click for bookmark icon
        final TextView noBookmark = view.findViewById(R.id.no_bookmark);
        final SharedPreferences pref = mCtx.getSharedPreferences("MyPref", 0);
        final SharedPreferences.Editor editor = pref.edit();
        holder.news_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.remove(id);
                editor.commit();
                newsCardList.remove(position);
                content.notifyDataSetChanged();
                if(newsCardList.size() == 0){
                    noBookmark.setVisibility(View.VISIBLE);
                }
                Toast.makeText(v.getContext(), "\"" + newscard.getNews_title() + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });

        //on click for card
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailArticlePage.class);
                intent.putExtra("id", id);
                v.getContext().startActivity(intent);
            }
        });

        //long click for bookmark card
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
                if (pref.getString(id, "false").equals("false")) {
                    dialog_bookmark.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    dialog_bookmark.setTag("notMarked");
                } else {
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
                        newsCardList.remove(position);
                        content.notifyDataSetChanged();
                        editor.remove(id);
                        editor.commit();
                        dialog.dismiss();
                        Toast.makeText(v.getContext(), "\"" + newscard.getNews_title() + "\" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (newsCardList != null && !newsCardList.isEmpty()) {
            return newsCardList.size();
        } else {
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String convertNewsDate(String news_date) {
        LocalDateTime time = LocalDateTime.parse(news_date, DateTimeFormatter.ISO_DATE_TIME);
        time = time.minusHours(7);
        String str_time = time.toString();
        String month = str_time.substring(5, 7);
        String date = str_time.substring(8, 10);
        String actual_month = "false";
        switch (month) {
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
        return date + " " + actual_month;
    }

    class BookmarkCardViewHolder extends RecyclerView.ViewHolder {
        ImageView news_image, news_bookmark;
        TextView news_title, news_time, news_section;
        CardView cardView;

        BookmarkCardViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            news_bookmark = itemView.findViewById(R.id.news_bookmark);
            news_title = itemView.findViewById(R.id.news_title);
            news_time = itemView.findViewById(R.id.news_time);
            news_section = itemView.findViewById(R.id.news_section);
            news_image = itemView.findViewById(R.id.news_image);
        }
    }
}
