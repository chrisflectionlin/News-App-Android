package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class HeadlinesFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabItem world,business,politics,sports,technology,science;
    public TabAdapter tabAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View headlinesView = inflater.inflate(R.layout.fragment_headlines,container,false);

        tabLayout = headlinesView.findViewById(R.id.tabLayout);
        world = headlinesView.findViewById(R.id.tab_world);
        business = headlinesView.findViewById(R.id.tab_business);
        politics = headlinesView.findViewById(R.id.tab_politics);
        sports = headlinesView.findViewById(R.id.tab_sports);
        technology = headlinesView.findViewById(R.id.tab_technology);
        science = headlinesView.findViewById(R.id.tab_science);
        viewPager = headlinesView.findViewById(R.id.viewpager);

        tabAdapter = new TabAdapter(getChildFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(tabAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition() == 0){
                    tabAdapter.notifyDataSetChanged();
                }else if(tab.getPosition() == 1){
                    tabAdapter.notifyDataSetChanged();
                }else if(tab.getPosition() == 2){
                    tabAdapter.notifyDataSetChanged();
                }else if(tab.getPosition() == 3){
                    tabAdapter.notifyDataSetChanged();
                }else if(tab.getPosition() == 4){
                    tabAdapter.notifyDataSetChanged();
                }else if(tab.getPosition() == 5){
                    tabAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        return headlinesView;
    }

}
