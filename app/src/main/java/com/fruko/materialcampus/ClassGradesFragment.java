package com.fruko.materialcampus;

import android.content.Context;
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
import android.view.Menu;
import android.view.MenuInflater;
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
import us.plxhack.InfiniteCampus.api.course.Category;
import us.plxhack.InfiniteCampus.api.course.Course;
import us.plxhack.InfiniteCampus.api.course.Task;

public class ClassGradesFragment extends Fragment
{
    private ListView gradesList;

    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String SELECTED_ASSIGNMENT_ID = "com.fruko.materialcampus.SELECTED_ASSIGNMENT_ID";
    public final static String SELECTED_CATEGORY_ID = "com.fruko.materialcampus.SELECTED_CATEGORY_ID";
    public final static String SELECTED_TASK_ID = "com.fruko.materialcampus.SELECTED_TASK_ID";
    public final static String ALL_CLASSES_ID = "com.fruko.materialcampus.ALL_CLASSES_ID";

    private Course course;
    private int position;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.activity_classgrades, container, false);

        position = getArguments().getInt(ClassesFragment.SELECTED_COURSE_ID, 0);
        course = InfiniteCampusApi.getInstance().getUserInfo().getCourses().get(position);

        getActivity().setTitle(course.getCourseName() + " - " + new DecimalFormat("#.00").format(course.getPercent()) + "%");

        final List<List<String[]>> gradesArray = new ArrayList<>();

        for (int i = 0; i < course.tasks.size(); i++)
        {
            Task task = course.tasks.get(i);

            for (int k = 0; k < task.getCategories().size();++k)
            {
                Category c = task.getCategories().get(k);

                String cpercent = "";
                if (c.getPercentage() == 0)
                    cpercent = "0.00%";
                else
                    cpercent = new DecimalFormat("#.00").format(c.getPercentage()) + "%";

                String[] title = {c.getName() + " - " + cpercent, ""};
                List<String[]> category = new ArrayList<>();
                category.add(title);

                for (int j = 0; j < c.getActivites().size(); j++) {
                    us.plxhack.InfiniteCampus.api.course.Activity a = c.getActivites().get(j);

                    String percent = "";
                    if (a.isMissing())
                        percent = "Missing";
                    else if (a.getLetterGrade().equals("N/A"))
                        percent = "Not Graded";
                    else if (a.getPercentage() == 0)
                        percent = "0.00%";
                    else
                        percent = new DecimalFormat("#.00").format(a.getPercentage()) + "%";

                    String[] assignment = {a.getName(), percent, a.getLetterGrade(), Integer.toString(i), Integer.toString(k)};
                    category.add(assignment);
                }

                gradesArray.add(category);
            }
        }

        gradesList = (ListView) view.findViewById(R.id.class_grades);

        gradesList.setAdapter(new ArrayAdapter<List<String[]>>(getActivity(), R.layout.category_list_item, R.id.category, gradesArray)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.category_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                SharedPreferences settings = getActivity().getSharedPreferences("MaterialCampus", 0);
                final List<String[]> assignments = gradesArray.get(position);

                final LinearLayout list = (LinearLayout) view.findViewById(R.id.assignments);

                TextView name = (TextView) view.findViewById(R.id.category);
                name.setBackgroundColor(getResources().getColor(R.color.accent));
                name.setTextColor(Color.WHITE);
                name.setText(assignments.get(0)[0]);
                name.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (list.getVisibility() == View.GONE)
                        {
                            list.setVisibility(View.VISIBLE);
                        } else
                        {
                            list.setVisibility(View.GONE);
                        }
                    }
                });

                list.removeAllViews();
                for (int i = 1; i < assignments.size(); i++)
                {
                    View child = getActivity().getLayoutInflater().inflate(R.layout.assignment_list_item, null);
                    TextView assignName = (TextView) child.findViewById(R.id.name);
                    assignName.setText(assignments.get(i)[0]);
                    TextView grade = (TextView) child.findViewById(R.id.grade);
                    grade.setText(assignments.get(i)[1]);


                    if (settings.getBoolean("highlightGrade", false))
                    {
                        switch (assignments.get(i)[2])
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

                    final int a = i - 1;
                    child.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Fragment fragment = new AssignmentFragment();
                            Bundle args = new Bundle();
                            args.putInt(SELECTED_COURSE_ID, getArguments().getInt(ClassesFragment.SELECTED_COURSE_ID, 0));
                            args.putInt(SELECTED_CATEGORY_ID, Integer.parseInt(assignments.get(a + 1)[4]));
                            args.putInt(SELECTED_ASSIGNMENT_ID, a);
                            args.putInt(SELECTED_TASK_ID, Integer.parseInt(assignments.get(a + 1)[3]));
                            fragment.setArguments(args);

                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    });
                    list.addView(child);
                }

                return view;
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.search)
        {
            Fragment fragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putInt(SELECTED_COURSE_ID, position);
            args.putBoolean(ALL_CLASSES_ID, false);
            fragment.setArguments(args);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
