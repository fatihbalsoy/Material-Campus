package com.fruko.materialcampus;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;

/**
 * Created by mail929 on 5/16/15.
 */
public class MCActivity extends ActionBarActivity
{
    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String ALL_CLASSES_ID = "com.fruko.materialcampus.ALL_CLASSES_ID";

    private String[] drawerItemTitles =
    {
            "Class Grades",
            "Missing Assignments",
            "Recently Updated",
            "Settings"
    };
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private RelativeLayout image;
    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    private boolean upEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mc);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        image = (RelativeLayout) findViewById(R.id.image);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.navdrawer_list_item, drawerItemTitles));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        changeFragment(new ClassesFragment());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.closed)
        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(!upEnabled)
        {
            if (mDrawerToggle.onOptionsItemSelected(item))
            {
                return true;
            }
        }
        else if(item.getItemId() == android.R.id.home)
        {
            getSupportFragmentManager().popBackStack();
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            Fragment fragment = new Fragment();
            switch(drawerItemTitles[position])
            {
                case "Class Grades":
                    fragment = new ClassesFragment();
                    break;
                case "Missing Assignments":
                    fragment = new MissingFragment();
                    break;
                case "Recently Updated":
                    fragment = new RecentGradesFragment();
                    break;
                case "Settings":
                    fragment = new SettingsFragment();
                    break;
            }

            changeFragment(fragment);

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer((LinearLayout) findViewById(R.id.drawer));
        }
    }

    public void changeFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setUp(boolean displayUp)
    {
        upEnabled = displayUp;
        getSupportActionBar().setDisplayHomeAsUpEnabled(displayUp);
        mDrawerToggle.setDrawerIndicatorEnabled(!displayUp);
    }
}
