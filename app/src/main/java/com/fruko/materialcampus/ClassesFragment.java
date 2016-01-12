package com.fruko.materialcampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;
import us.plxhack.InfiniteCampus.api.course.Course;

public class ClassesFragment extends Fragment
{
    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String ALL_CLASSES_ID = "com.fruko.materialcampus.ALL_CLASSES_ID";

    private ListView classList;
    private boolean canViewGrades = true;

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_classes, container, false);

        ((MCActivity) getActivity()).setUp(false);
        getActivity().setTitle(InfiniteCampusApi.getInstance().getUserInfo().getFirstName() + ' ' + InfiniteCampusApi.getInstance().getUserInfo().getLastName());
        getCourseList();

        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) view.findViewById( R.id.class_swipe );
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                System.out.println("Refreshing");
                new UserRefreshTask(swipeView).execute();
            }
        });

        return view;
    }

    private void getCourseList()
    {
        final ArrayList<String[]> classNameArray = new ArrayList<>();
        for (int i=0;i < InfiniteCampusApi.getInstance().getUserInfo().getCourses().size(); ++i)
        {
            Course course = InfiniteCampusApi.getInstance().getUserInfo().getCourses().get(i);
            String[] newArray = {course.getCourseName(), new DecimalFormat("#.00").format(course.getPercent()) + "%", course.getTeacherName(), course.getLetterGrade() };
            classNameArray.add(newArray);
        }

        classList = (ListView) view.findViewById( R.id.class_list );

        classList.setAdapter(new ArrayAdapter<String[]>(getActivity(), R.layout.class_list_item, R.id.name, classNameArray)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.class_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                TextView name = (TextView) view.findViewById(R.id.name);
                name.setText(classNameArray.get(position)[0]);
                TextView percent = (TextView) view.findViewById(R.id.percent);
                percent.setText(classNameArray.get(position)[1]);
                TextView teacher = (TextView) view.findViewById(R.id.teacher);
                teacher.setText(classNameArray.get(position)[2]);
                TextView grade = (TextView) view.findViewById(R.id.grade);
                grade.setText(classNameArray.get(position)[3]);


                SharedPreferences settings = getActivity().getSharedPreferences("MaterialCampus", 0);

                if (settings.getBoolean("highlightGrade", false))
                {
                    switch (grade.getText().toString())
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
                        case "P":
                            grade.setBackgroundColor(Color.GREEN);
                            break;
                    }
                }
                return view;
            }
        });


        classList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!canViewGrades)
                    return;

                Fragment fragment = new ClassGradesFragment();
                Bundle args = new Bundle();
                args.putInt(SELECTED_COURSE_ID, position);
                fragment.setArguments(args);

                ((MCActivity) getActivity()).changeFragment(fragment);
            }
        });

        canViewGrades = true;
    }

    private class UserRefreshTask extends AsyncTask<Void, Void, Boolean>
    {
        private SwipeRefreshLayout swipeLayout;

        public UserRefreshTask(SwipeRefreshLayout swipelayout)
        {
            swipeLayout = swipelayout;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            canViewGrades = false;
            return InfiniteCampusApi.getInstance().relogin();
        }

        protected void onPostExecute(Boolean result)
        {
            getCourseList();
            swipeLayout.setRefreshing(false);
            canViewGrades = true;
        }
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
            args.putInt(SELECTED_COURSE_ID, 0);
            args.putBoolean(ALL_CLASSES_ID, true);
            fragment.setArguments(args);

            ((MCActivity) getActivity()).changeFragmentSearch(fragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
