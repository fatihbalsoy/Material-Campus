package com.fruko.materialcampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;

/**
 * Created by mail929 on 3/22/15.
 * todo (make settings for)
 * color highlighting
 * ui color
 */
public class SettingsActivity extends ActionBarActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences settings = getSharedPreferences("MaterialCampus", 0);
        final SharedPreferences.Editor editor = settings.edit();

        LinearLayout scroll = (LinearLayout) findViewById(R.id.scroll);

        View gradeHighlight = getLayoutInflater().inflate(R.layout.checkbox_setting_item, null);
        TextView highlightLabel = (TextView) gradeHighlight.findViewById(R.id.label);
        final CheckBox highlightCheck = (CheckBox) gradeHighlight.findViewById(R.id.checkBox);
        highlightLabel.setText("Highlight Grades with Color?");
        highlightCheck.setChecked(settings.getBoolean("highlightGrade", false));
        highlightCheck.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editor.putBoolean("highlightGrade", highlightCheck.isChecked());
                editor.commit();
            }
        });
        scroll.addView(gradeHighlight);
    }
}
