package com.fruko.materialcampus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import us.plxhack.InfiniteCampus.api.InfiniteCampusApi;

/**
 * Created by mail929 on 3/22/15.
 */
public class SettingsFragment extends Fragment
{
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.activity_settings, container, false);
        getActivity().setTitle("Settings");
        ((MCActivity) getActivity()).setUp(true);
        SharedPreferences settings = getActivity().getSharedPreferences("MaterialCampus", 0);
        final SharedPreferences.Editor editor = settings.edit();

        LinearLayout scroll = (LinearLayout) view.findViewById(R.id.scroll);


        View gradeHighlight = getActivity().getLayoutInflater().inflate(R.layout.checkbox_setting_item, null);
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

        View dontSync = getActivity().getLayoutInflater().inflate(R.layout.checkbox_setting_item, null);
        TextView dontSyncLabel = (TextView) dontSync.findViewById(R.id.label);
        final CheckBox dontSyncCheck = (CheckBox) dontSync.findViewById(R.id.checkBox);
        dontSyncLabel.setText("Dont sync automatically?");
        dontSyncCheck.setChecked(settings.getBoolean("dontSync", false));
        dontSyncCheck.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editor.putBoolean("dontSync", dontSyncCheck.isChecked());
                editor.commit();
            }
        });
        scroll.addView(dontSync);
        return view;
    }
}
