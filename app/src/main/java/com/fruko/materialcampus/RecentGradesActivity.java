package com.fruko.materialcampus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Activity;

/**
 * Created by mail929 on 5/1/15.
 */
public class RecentGradesActivity extends ActionBarActivity
{
    private ListView assignments;
    List<String[]> assignmentsArray = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        InfiniteCampusApi.refresh();
        setContentView(R.layout.activity_recentgrades);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        List<Activity> newAssignments = InfiniteCampusApi.userInfo.newAssignments;

        for (int j = 0; j < newAssignments.size(); j++)
                    {
                        us.plxhack.InfiniteCampus.api.course.Activity a = newAssignments.get(j);

                        String percent = "";
                        if (a.missing)
                            percent = "Missing";
                        else if (a.letterGrade.equals("N/A"))
                            percent = "Not Graded";
                        else if (a.percentage == 0)
                            percent = "0.00%";
                        else
                            percent = new DecimalFormat("#.00").format(a.percentage) + "%";

                        String[] assignment = {a.name, percent, a.letterGrade, a.className};
                        assignmentsArray.add(assignment);
                    }

        assignments = (ListView)findViewById( R.id.assignments );

        assignments.setAdapter(new ArrayAdapter<String[]>(this, R.layout.recentgrade_list_item, R.id.name, assignmentsArray)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.recentgrade_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                String[] data = assignmentsArray.get(position);

                SharedPreferences settings = getSharedPreferences("MaterialCampus", 0);

                TextView assignment = (TextView) view.findViewById(R.id.name);
                TextView className = (TextView) view.findViewById(R.id.className);
                TextView grade = (TextView) view.findViewById(R.id.grade);
                TextView percent = (TextView) view.findViewById(R.id.percent);

                assignment.setText(data[0]);
                className.setText(data[3]);
                grade.setText(data[2]);
                percent.setText(data[1]);

                    if(settings.getBoolean("highlightGrade", false))
                    {
                        switch(data[2]) {
                            case "A":
                                grade.setBackgroundColor(Color.GREEN);
                                break;
                            case "B":
                                grade.setBackgroundColor(Color.parseColor("#ADFF2F"));
                                break;
                            case "C":
                                grade.setBackgroundColor(Color.YELLOW);
                                break;
                            case "D":
                                grade.setBackgroundColor(Color.parseColor("#FFA500"));
                                break;
                            case "F":
                                grade.setBackgroundColor(Color.RED);
                                break;
                        }
                    }
                    if(grade.getText().toString().equals("Missing"))
                        grade.setTextColor(Color.RED);

                return view;
            }
        });

    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onRestart()
    {
        super.onRestart();
    }

    protected void onResume()
    {
        super.onResume();
    }

    protected void onPause()
    {
        super.onPause();
    }

    protected void onStop()
    {
        super.onStop();
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.accounts)
        {
            Intent go = new Intent(this, AccountListActivity.class);
            this.startActivity(go);
            return true;
        }
        /*else if (id == R.id.settings)
        {
            Intent go = new Intent(this, SettingsActivity.class);
            this.startActivity(go);
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
}
