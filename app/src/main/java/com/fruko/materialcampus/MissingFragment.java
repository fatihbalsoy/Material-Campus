package com.fruko.materialcampus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Course;

/**
 * Created by mail929 on 5/7/15.
 */
public class MissingFragment extends Fragment
{
    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String SELECTED_ASSIGNMENT_ID = "com.fruko.materialcampus.SELECTED_ASSIGNMENT_ID";
    public final static String SELECTED_CATEGORY_ID = "com.fruko.materialcampus.SELECTED_CATEGORY_ID";
    public final static String SELECTED_TASK_ID = "com.fruko.materialcampus.SELECTED_TASK_ID";

    ListView list;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.activity_classgrades, container, false);
        getActivity().setTitle("Missing Assignments");
        list = (ListView) view.findViewById(R.id.class_grades);

        final List<Activity> missing = InfiniteCampusApi.getInstance().getAllMissingAssignments();

        list.setAdapter(new ArrayAdapter<Activity>(getActivity(), R.layout.missing_list_item, R.id.name, missing)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.missing_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                ((TextView) view.findViewById(R.id.name)).setText(missing.get(position).getName());
                ((TextView) view.findViewById(R.id.className)).setText(missing.get(position).getClassName());

                view.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Fragment fragment = new AssignmentFragment();
                        Bundle args = new Bundle();
                        List<Course> courses = InfiniteCampusApi.getInstance().getUserInfo().getCourses();
                        for (int i = 0; i < courses.size(); i++)
                        {
                            for (int j = 0; j < courses.get(i).tasks.size(); j++)
                            {
                                for (int k = 0; k < courses.get(i).tasks.get(j).getCategories().size(); k++)
                                {
                                    for (int l = 0; l < courses.get(i).tasks.get(j).getCategories().get(k).getActivites().size(); l++)
                                    {
                                        Activity activity = courses.get(i).tasks.get(j).getCategories().get(k).getActivites().get(l);
                                        if (activity.getId().equals(missing.get(position).getId()))
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
