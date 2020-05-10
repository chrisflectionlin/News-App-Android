package com.example.newsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookmarksFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<String> id_array;
    private BookmarkCardAdapter adapter;
    private boolean isBack;
    private List<Newscard> newscardList;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bookmarks,container,false);
        isBack=false;
        //set recyclerView
        recyclerView = view.findViewById(R.id.bookmark_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        populateBookmark();
        return view;
    }

    private void populateBookmark(){
        id_array = new ArrayList<>();
        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", 0);
        final SharedPreferences.Editor editor = pref.edit();
        Map<String,?> keys = pref.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            id_array.add(entry.getKey());
        }
        TextView noBookmark = view.findViewById(R.id.no_bookmark);
        if(id_array.size()==0){
            noBookmark.setVisibility(View.VISIBLE);
        }else{
            noBookmark.setVisibility(View.GONE);
        }
        //populate newscardList
        newscardList = new ArrayList<>();
        for(int i = 0; i< id_array.size(); i++){
            String cur_id = id_array.get(i);
            String jsonString =  pref.getString(cur_id,"false");
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                String news_url = jsonObject.getString("url");
                String news_date = jsonObject.getString("date");
                String news_title = jsonObject.getString("title");
                String news_section = jsonObject.getString("section");
                String news_image = jsonObject.getString("image");
                Newscard current = new Newscard(news_image,news_title,news_date,news_section,cur_id,news_url);
                newscardList.add(current);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter = new BookmarkCardAdapter(getActivity(), newscardList, view);
        recyclerView.setAdapter(adapter);
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
            populateBookmark();
        }
    }

}
