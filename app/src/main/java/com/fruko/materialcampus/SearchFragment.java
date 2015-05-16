package com.fruko.materialcampus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
public class SearchFragment extends Fragment
{
    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String SELECTED_ASSIGNMENT_ID = "com.fruko.materialcampus.SELECTED_ASSIGNMENT_ID";
    public final static String SELECTED_CATEGORY_ID = "com.fruko.materialcampus.SELECTED_CATEGORY_ID";
    public final static String SELECTED_TASK_ID = "com.fruko.materialcampus.SELECTED_TASK_ID";

    EditText search;
    ListView list;
    Course course;
    boolean allClasses;
    List<Activity> results;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.activity_search, container, false);

        course = InfiniteCampusApi.getInstance().getUserInfo().getCourses().get(getArguments().getInt(ClassGradesFragment.SELECTED_COURSE_ID, 0));
        allClasses = getArguments().getBoolean(ClassesFragment.ALL_CLASSES_ID, false);

        search = (EditText) view.findViewById(R.id.search);
        list = (ListView) view.findViewById(R.id.results);

        if(allClasses)
        {
            getActivity().setTitle("Search: All Classes");
        }
        else
        {
            getActivity().setTitle("Search: " + course.getCourseName());
        }

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
                if (allClasses)
                {
                    results = InfiniteCampusApi.getInstance().searchAllClasses(search.getText().toString());
                } else
                {
                    results = InfiniteCampusApi.getInstance().searchClass(course, search.getText().toString());
                }

                list.setAdapter(new ArrayAdapter<Activity>(getActivity(), R.layout.assignment_search_item, R.id.name, results)
                {
                    public View getView(final int position, View convertView, ViewGroup parent)
                    {
                        View view;
                        if (convertView == null)
                        {
                            LayoutInflater infl = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                            convertView = infl.inflate(R.layout.assignment_search_item, parent, false);
                        }
                        view = super.getView(position, convertView, parent);

                        ((TextView) view.findViewById(R.id.name)).setText(results.get(position).getName());
                        TextView grade = (TextView) view.findViewById(R.id.grade);

                        String percent = "";
                        if (results.get(position).isMissing())
                        {
                            percent = "Missing";
                            grade.setTextColor(Color.RED);
                        } else if (results.get(position).getLetterGrade().equals("N/A"))
                            percent = "Not Graded";
                        else if (results.get(position).getPercentage() == 0)
                            percent = "0.00%";
                        else
                            percent = new DecimalFormat("#.00").format(results.get(position).getPercentage()) + "%";

                        grade.setText(percent);

                        if (allClasses)
                        {
                            ((TextView) view.findViewById(R.id.className)).setText(results.get(position).getClassName());
                        } else
                        {

                            (view.findViewById(R.id.className)).setVisibility(View.GONE);
                        }

                        view.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent intent = new Intent(getActivity(), AssignmentFragment.class);
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
                                                if (activity.getId().equals(results.get(position).getId()))
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
        });
        return view;
    }
}
