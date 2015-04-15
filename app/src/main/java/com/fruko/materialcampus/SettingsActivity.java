package com.fruko.materialcampus;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.widget.ScrollView;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;

/**
 * Created by mail929 on 3/22/15.
 * todo (make settings for)
 * color highlighting
 * ui color
 */
public class SettingsActivity extends ActionBarActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
    }
}
