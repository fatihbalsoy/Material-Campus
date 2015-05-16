package com.fruko.materialcampus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Course;

/**
 * Created by student on 3/18/2015.
 */
public class AssignmentFragment extends Fragment
{
    private ListView gradesList;

    private Course course;
    private us.plxhack.InfiniteCampus.api.course.Activity assignment;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.activity_assignment, container, false);

        int courseId = getArguments().getInt(ClassGradesFragment.SELECTED_COURSE_ID, 0);
        course = InfiniteCampusApi.getInstance().getUserInfo().getCourses().get(courseId);
        int task = getArguments().getInt(ClassGradesFragment.SELECTED_TASK_ID, 0);
        for(int i = 0; i < course.tasks.get(task).getCategories().size(); i++)
        {
            if(i == getArguments().getInt(ClassGradesFragment.SELECTED_CATEGORY_ID, 0))
            {
                for (int j = 0; j < course.tasks.get(task).getCategories().get(i).getActivites().size(); j++)
                {
                    if (j == getArguments().getInt(ClassGradesFragment.SELECTED_ASSIGNMENT_ID, 0))
                    {
                        assignment = InfiniteCampusApi.getInstance().getUserInfo().getCourses().get(getArguments().getInt(ClassGradesFragment.SELECTED_COURSE_ID, 0)).tasks.get(task).getCategories().get(i).getActivites().get(j);
                    }
                }
            }
        }
        getActivity().setTitle(assignment.getName() + " - " + assignment.getPercentage() + "%");

        List<String[]> dataPoints = new ArrayList<>();
        dataPoints.add(new String[]{"Title", assignment.getName()});
        dataPoints.add(new String[]{"Due Date", assignment.getDueDate()});
        dataPoints.add(new String[]{"Letter Grade", assignment.getLetterGrade()});
        dataPoints.add(new String[]{"Percent", Float.toString(assignment.getPercentage()) + "%"});
        dataPoints.add(new String[]{"Points Earned", assignment.getEarnedPoints()});
        dataPoints.add(new String[]{"Points Possible", Float.toString(assignment.getTotalPoints())});

        LinearLayout list = (LinearLayout) view.findViewById(R.id.datapoints);
        for(int i = 1; i < dataPoints.size(); i++)
        {
            View child = getActivity().getLayoutInflater().inflate(R.layout.assignment_data_item, null);
            TextView name = (TextView) child.findViewById(R.id.name);
            name.setText(dataPoints.get(i)[0]);
            TextView data = (TextView) child.findViewById(R.id.datapoint);
            data.setText(dataPoints.get(i)[1]);
            if(data.getText().toString().equals("M"))
                data.setTextColor(Color.RED);
            list.addView(child);
        }

        return view;
    }
}
