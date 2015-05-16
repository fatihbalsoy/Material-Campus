package com.fruko.materialcampus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mc);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.navdrawer_list_item, drawerItemTitles));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new ClassesFragment())
                .commit();

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @SuppressLint("NewApi")
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

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
}
