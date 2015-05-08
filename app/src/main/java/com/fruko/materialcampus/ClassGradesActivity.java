package com.fruko.materialcampus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

public class ClassGradesActivity extends ActionBarActivity
{
    private ListView gradesList;

    public final static String SELECTED_COURSE_ID = "com.fruko.materialcampus.SELECTED_COURSE_ID";
    public final static String SELECTED_ASSIGNMENT_ID = "com.fruko.materialcampus.SELECTED_ASSIGNMENT_ID";
    public final static String SELECTED_CATEGORY_ID = "com.fruko.materialcampus.SELECTED_CATEGORY_ID";
    public final static String SELECTED_TASK_ID = "com.fruko.materialcampus.SELECTED_TASK_ID";
    public final static String ALL_CLASSES_ID = "com.fruko.materialcampus.ALL_CLASSES_ID";

    private Course course;
    private int position;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classgrades);
        course = InfiniteCampusApi.getInstance().getUserInfo().getCourses().get(getIntent().getIntExtra(ClassesActivity.SELECTED_COURSE_ID, 0));
        position = getIntent().getIntExtra(ClassesActivity.SELECTED_COURSE_ID, 0);
        setTitle(course.getCourseName() + " - " + new DecimalFormat("#.00").format(course.getPercent()) + "%");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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

        gradesList = (ListView)findViewById( R.id.class_grades );

        final Context c = this;

        gradesList.setAdapter(new ArrayAdapter<List<String[]>>(this, R.layout.category_list_item, R.id.category, gradesArray)
        {
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view;
                if (convertView == null)
                {
                    LayoutInflater infl = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = infl.inflate(R.layout.category_list_item, parent, false);
                }
                view = super.getView(position, convertView, parent);

                SharedPreferences settings = getSharedPreferences("MaterialCampus", 0);
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
                        if(list.getVisibility() == View.GONE)
                        {
                            list.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            list.setVisibility(View.GONE);
                        }
                    }
                });

                list.removeAllViews();
                for(int i = 1; i < assignments.size(); i++)
                {
                    View child = getLayoutInflater().inflate(R.layout.assignment_list_item, null);
                    TextView assignName = (TextView) child.findViewById(R.id.name);
                    assignName.setText(assignments.get(i)[0]);
                    TextView grade = (TextView) child.findViewById(R.id.grade);
                    grade.setText(assignments.get(i)[1]);


                    if(settings.getBoolean("highlightGrade", false))
                    {
                        switch(assignments.get(i)[2]) {
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

                    final int a = i-1;
                    child.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(c, AssignmentActivity.class);
                            intent.putExtra(SELECTED_COURSE_ID, getIntent().getIntExtra(ClassesActivity.SELECTED_COURSE_ID, 0));
                            intent.putExtra(SELECTED_CATEGORY_ID, Integer.parseInt(assignments.get(a+1)[4]));
                            intent.putExtra(SELECTED_ASSIGNMENT_ID, a);
                            intent.putExtra(SELECTED_TASK_ID, Integer.parseInt(assignments.get(a+1)[3]));
                            startActivity(intent);
                        }
                    });
                    list.addView(child);
                }

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
        else if (id == R.id.settings)
        {
            Intent go = new Intent(this, SettingsActivity.class);
            this.startActivity(go);
            return true;
        }
        else if (id == R.id.recent)
        {
            Intent go = new Intent(this, RecentGradesActivity.class);
            this.startActivity(go);
            return true;
        }
        else if (id == R.id.missing)
        {
            Intent go = new Intent(this, MissingActivity.class);
            this.startActivity(go);
            return true;
        }
        else if (id == R.id.search)
        {
            Intent go = new Intent(this, SearchActivity.class);
            go.putExtra(SELECTED_COURSE_ID, position);
            go.putExtra(ALL_CLASSES_ID, false);
            this.startActivity(go);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
