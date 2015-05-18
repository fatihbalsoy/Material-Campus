package com.fruko.materialcampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Course;

/**
 * Created by mail929 on 5/1/15.
 */
public class RecentGradesFragment extends Fragment
{
    private ListView assignments;
    List<String[]> assignmentsArray = new ArrayList<>();
    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String SELECTED_ASSIGNMENT_ID = "com.fruko.materialcampus.SELECTED_ASSIGNMENT_ID";
    public final static String SELECTED_CATEGORY_ID = "com.fruko.materialcampus.SELECTED_CATEGORY_ID";
    public final static String SELECTED_TASK_ID = "com.fruko.materialcampus.SELECTED_TASK_ID";

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.activity_recentgrades, container, false);

        getActivity().setTitle("Recently Updated Grades");

        List<Activity> newAssignments = InfiniteCampusApi.getInstance().getUserInfo().getNewAssignments();

        for (int j = 0; j < newAssignments.size(); j++)
        {
            us.plxhack.InfiniteCampus.api.course.Activity a = newAssignments.get(j);

            String percent = "";
            if (a.isMissing())
                percent = "Missing";
            else if (a.getLetterGrade().equals("N/A"))
                percent = "Not Graded";
            else if (a.getPercentage() == 0)
                percent = "0.00%";
            else
                percent = new DecimalFormat("#.00").format(a.getPercentage()) + "%";

            String[] assignment = {a.getName(), percent, a.getLetterGrade(), a.getClassName(), a.getId()};
            assignmentsArray.add(assignment);
        }

        assignments = (ListView) view.findViewById(R.id.assignments);

        assignments.setAdapter(new ArrayAdapter<String[]>(getActivity(), R.layout.recentgrade_list_item, R.id.name, assignmentsArray)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.recentgrade_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                String[] data = assignmentsArray.get(position);

                SharedPreferences settings = getActivity().getSharedPreferences("MaterialCampus", 0);

                TextView assignment = (TextView) view.findViewById(R.id.name);
                TextView className = (TextView) view.findViewById(R.id.className);
                TextView grade = (TextView) view.findViewById(R.id.grade);
                TextView percent = (TextView) view.findViewById(R.id.percent);

                assignment.setText(data[0]);
                className.setText(data[3]);
                grade.setText(data[2]);
                percent.setText(data[1]);

                if (settings.getBoolean("highlightGrade", false))
                {
                    switch (data[2])
                    {
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
                if (grade.getText().toString().equals("Missing"))
                    grade.setTextColor(Color.RED);

                view.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Fragment fragment = new AssignmentFragment();
                        Bundle args = new Bundle();
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
                                        if(activity.getId().equals(assignmentsArray.get(position)[4]))
                                        {
                                            args.putInt(SELECTED_COURSE_ID, i);
                                            args.putInt(SELECTED_CATEGORY_ID, k);
                                            args.putInt(SELECTED_ASSIGNMENT_ID, l);
                                            args.putInt(SELECTED_TASK_ID, j);
                                        }
                                    }
                                }
                            }
                        }
                        fragment.setArguments(args);
                        ((MCActivity) getActivity()).changeFragment(fragment);
                    }
                });
                return view;
            }
        });
        return view;
    }
}
