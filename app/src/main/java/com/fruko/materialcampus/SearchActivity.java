package com.fruko.materialcampus;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Course;

/**
 * Created by mail929 on 5/6/15.
 */
public class SearchActivity extends ActionBarActivity
{
    EditText search;
    ListView list;
    Course course;
    boolean allClasses;
    List<Activity> results;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search = (EditText) findViewById(R.id.search);
        list = (ListView) findViewById(R.id.results);

        course = InfiniteCampusApi.userInfo.courses.get(getIntent().getIntExtra(ClassGradesActivity.SELECTED_COURSE_ID, 0));
        allClasses = getIntent().getBooleanExtra(ClassesActivity.ALL_CLASSES_ID, false);

        if(allClasses)
        {
            setTitle("Search: All Classes");
        }
        else
        {
            setTitle("Search: " + course.getCourseName());
        }

        final Context c = this;

        search.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(allClasses)
                {
                    results = InfiniteCampusApi.searchAllClasses(search.getText().toString());
                }
                else
                {
                    results = InfiniteCampusApi.searchClass(course, search.getText().toString());
                }

                list.setAdapter(new ArrayAdapter<Activity>(c, R.layout.assignment_search_item, R.id.name, results)
                {
                    public View getView(final int position, View convertView, ViewGroup parent)
                    {
                        View view;
                        if (convertView == null)
                        {
                            LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            convertView = infl.inflate(R.layout.assignment_search_item, parent, false);
                        }
                        view = super.getView(position, convertView, parent);

                        ((TextView) view.findViewById(R.id.name)).setText(results.get(position).name);
                        
                        String percent = "";
                        if (results.get(position).missing)
                            percent = "Missing";
                        else if (results.get(position).letterGrade.equals("N/A"))
                            percent = "Not Graded";
                        else if (results.get(position).percentage == 0)
                            percent = "0.00%";
                        else
                            percent = new DecimalFormat("#.00").format(results.get(position).percentage) + "%";

                        ((TextView) view.findViewById(R.id.grade)).setText(percent);
                        if(allClasses)
                        {
                            ((TextView) view.findViewById(R.id.className)).setText(results.get(position).className);
                        }
                        else
                        {

                            (view.findViewById(R.id.className)).setVisibility(View.GONE);
                        }

                        return view;
                    }
                });
            }
        });
    }
}
