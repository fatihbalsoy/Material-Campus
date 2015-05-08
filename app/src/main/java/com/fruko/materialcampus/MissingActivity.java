package com.fruko.materialcampus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Course;

/**
 * Created by mail929 on 5/7/15.
 */
public class MissingActivity extends ActionBarActivity
{
    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String SELECTED_ASSIGNMENT_ID = "com.fruko.materialcampus.SELECTED_ASSIGNMENT_ID";
    public final static String SELECTED_CATEGORY_ID = "com.fruko.materialcampus.SELECTED_CATEGORY_ID";
    public final static String SELECTED_TASK_ID = "com.fruko.materialcampus.SELECTED_TASK_ID";

    ListView list;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classgrades);

        setTitle("Missing Assignments");
        list = (ListView) findViewById(R.id.class_grades);

        final List<Activity> missing = InfiniteCampusApi.getInstance().getAllMissingAssignments();

        list.setAdapter(new ArrayAdapter<Activity>(this, R.layout.missing_list_item, R.id.name, missing)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.missing_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                ((TextView) view.findViewById(R.id.name)).setText(missing.get(position).getName());
                ((TextView) view.findViewById(R.id.className)).setText(missing.get(position).getClassName());

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), AssignmentActivity.class);
                        List<Course> courses = InfiniteCampusApi.getInstance().getUserInfo().getCourses();
                        for(int i = 0; i < courses.size(); i++)
                        {
                            for(int j = 0; j < courses.get(i).tasks.size(); j++)
                            {
                                for(int k = 0; k < courses.get(i).tasks.get(j).getCategories().size(); k++)
                                {
                                    for(int l = 0; l < courses.get(i).tasks.get(j).getCategories().get(k).getActivites().size(); l++)
                                    {
                                        Activity activity = courses.get(i).tasks.get(j).getCategories().get(k).getActivites().get(l);
                                        if(activity.getId().equals(missing.get(position).getId()))
                                        {
                                            intent.putExtra(SELECTED_COURSE_ID, i);
                                            intent.putExtra(SELECTED_CATEGORY_ID, k);
                                            intent.putExtra(SELECTED_ASSIGNMENT_ID, l);
                                            intent.putExtra(SELECTED_TASK_ID, j);
                                        }
                                    }
                                }
                            }
                        }
                        startActivity(intent);
                    }
                });
                return view;
            }
        });
    }
}
