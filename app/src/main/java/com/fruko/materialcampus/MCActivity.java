package com.fruko.materialcampus;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
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
        transaction.setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_fade_out, R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void changeFragmentSearch(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom, R.anim.abc_slide_in_bottom, R.anim.abc_fade_out);
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

    public static class AdFragment extends Fragment
    {

        private AdView mAdView;

        public AdFragment()
        {
        }

        @Override
        public void onActivityCreated(Bundle bundle)
        {
            super.onActivityCreated(bundle);

            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    /*.addTestDevice("8F0E678B4BFECC3DF4FDBB0BC93BC803")*/.addTestDevice("ABCDEF012345")
                    .build();

            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        @Override
        public void onPause()
        {
            if (mAdView != null)
            {
                mAdView.pause();
            }
            super.onPause();
        }

        @Override
        public void onResume()
        {
            super.onResume();
            if (mAdView != null)
            {
                mAdView.resume();
            }
        }

        @Override
        public void onDestroy()
        {
            if (mAdView != null)
            {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }
}
