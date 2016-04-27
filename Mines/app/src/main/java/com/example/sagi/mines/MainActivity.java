package com.example.sagi.mines;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private long lastPress;
    private RadioGroup levelRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        levelRadioGroup = (RadioGroup)findViewById(R.id.levelRadioGroup);
    }

    public void onClickStart(View view) {
        int radioButtonID = levelRadioGroup.getCheckedRadioButtonId();
        View radioButton = levelRadioGroup.findViewById(radioButtonID);
        int levelIdx = levelRadioGroup.indexOfChild(radioButton);

        Intent intent = new Intent(this, GameActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt(GameActivity.levelKey, levelIdx);

        intent.putExtra(GameActivity.gameBundleKey, bundle);
        startActivity(intent);
    }

    public void onClickHighScore(View view) {
        int radioButtonID = levelRadioGroup.getCheckedRadioButtonId();
        View radioButton = levelRadioGroup.findViewById(radioButtonID);
        int levelIdx = levelRadioGroup.indexOfChild(radioButton);

        Intent intent = new Intent(this, HighScoreActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt(HighScoreActivity.levelKey, levelIdx);

        intent.putExtra(HighScoreActivity.highScoreBundleKey, bundle);
        startActivity(intent);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)  {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            long currentTime = System.currentTimeMillis();

            if(currentTime - lastPress > 5000){
                Toast.makeText(getApplicationContext(), R.string.main_press_back_alert, Toast.LENGTH_SHORT).show();
                lastPress = currentTime;
            }
            else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
