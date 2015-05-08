package com.fruko.materialcampus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
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
import us.plxhack.InfiniteCampus.api.course.Category;
import us.plxhack.InfiniteCampus.api.course.Course;

/**
 * Created by student on 3/18/2015.
 */
public class AssignmentActivity extends ActionBarActivity
{
    private ListView gradesList;

    private Course course;
    private us.plxhack.InfiniteCampus.api.course.Activity assignment;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);
        course = InfiniteCampusApi.getInstance().userInfo.courses.get(getIntent().getIntExtra(ClassGradesActivity.SELECTED_COURSE_ID, 0));
        int task = getIntent().getIntExtra(ClassGradesActivity.SELECTED_TASK_ID, 0);
        for(int i = 0; i < course.tasks.get(task).gradeCategories.size(); i++)
        {
            if(i == getIntent().getIntExtra(ClassGradesActivity.SELECTED_CATEGORY_ID, 0))
            {
                for (int j = 0; j < course.tasks.get(task).gradeCategories.get(i).activities.size(); j++)
                {
                    if (j == getIntent().getIntExtra(ClassGradesActivity.SELECTED_ASSIGNMENT_ID, 0))
                    {
                        assignment = InfiniteCampusApi.getInstance().userInfo.courses.get(getIntent().getIntExtra(ClassGradesActivity.SELECTED_COURSE_ID, 0)).tasks.get(task).gradeCategories.get(i).activities.get(j);
                    }
                }
            }
        }
        setTitle(assignment.name + " - " +  assignment.percentage + "%");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        List<String[]> dataPoints = new ArrayList<>();
        dataPoints.add(new String[]{"Title", assignment.name});
        dataPoints.add(new String[]{"Due Date", assignment.dueDate});
        dataPoints.add(new String[]{"Letter Grade", assignment.letterGrade});
        dataPoints.add(new String[]{"Percent", Float.toString(assignment.percentage) + "%"});
        dataPoints.add(new String[]{"Points Earned", assignment.earnedPoints});
        dataPoints.add(new String[]{"Points Possible", Float.toString(assignment.totalPoints)});

        LinearLayout list = (LinearLayout) findViewById(R.id.datapoints);
        for(int i = 1; i < dataPoints.size(); i++)
        {
            View child = getLayoutInflater().inflate(R.layout.assignment_data_item, null);
            TextView name = (TextView) child.findViewById(R.id.name);
            name.setText(dataPoints.get(i)[0]);
            TextView data = (TextView) child.findViewById(R.id.datapoint);
            data.setText(dataPoints.get(i)[1]);
            if(data.getText().toString().equals("M"))
                data.setTextColor(Color.RED);
            list.addView(child);
        }
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
}
