package com.example.sagi.mines;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class HighScoreActivity extends AppCompatActivity {
    public static final String highScoreBundleKey = "highScoreBundle";
    public static final String levelKey = "level";

    private HighScoreTableFragment tableFragment;
    private HighScoreMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        tableFragment = new HighScoreTableFragment();
        mapFragment = new HighScoreMapFragment();

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(HighScoreActivity.highScoreBundleKey);
        int levelIdx = bundle.getInt(HighScoreActivity.levelKey);

        TextView title = (TextView)findViewById(R.id.highScoreTextView);
        switch (levelIdx) {
            case 0:
                title.setText(R.string.highScore_beginner_title);
                break;
            case 1:
                title.setText(R.string.highScore_normal_title);
                break;
            case 2:
                title.setText(R.string.highScore_expert_title);
                break;
        }
        DBHelper mydb = new DBHelper(this);
        ArrayList<HashMap<String, String>> data = mydb.getAllHighScores(levelIdx);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            Bundle tableBundle = new Bundle();
            tableBundle.putSerializable(HighScoreTableFragment.DATA_KEY, data);
            tableFragment.setArguments(tableBundle);

            Bundle mapBundle = new Bundle();
            mapBundle.putSerializable(HighScoreTableFragment.DATA_KEY, data);
            mapFragment.setArguments(mapBundle);

            getFragmentManager().beginTransaction().add(R.id.fragment_container, tableFragment).commit();
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.highScoreRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.mapRadioButton) {
                    onMapFragmentRBClicked();
                }
                else if (checkedId == R.id.tableRadioButton) {
                    onTableFragmentRBClicked();
                }
            }
        });
    }

    public void onTableFragmentRBClicked() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, tableFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onMapFragmentRBClicked() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
