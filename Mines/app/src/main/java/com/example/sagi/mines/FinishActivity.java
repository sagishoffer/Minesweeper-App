package com.example.sagi.mines;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;

public class FinishActivity extends AppCompatActivity {

    public static final String finishBundleKey = "finishBundle";

    private TextView statusLabel;
    private TextView timeLabel;

    private int levelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        statusLabel = (TextView)findViewById(R.id.finishStatus);
        timeLabel = (TextView) findViewById(R.id.time);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(finishBundleKey);
        String status = bundle.getString(GameActivity.statusKey);
        String time = bundle.getString(GameActivity.timeKey);
        levelId = bundle.getInt(GameActivity.levelKey);

        if(status.equals("win"))
            statusLabel.setText(R.string.you_win);
        else
            statusLabel.setText(R.string.you_lose);

        timeLabel.setText(time);
    }

    public void onClickPlayAgain(View view) {

        Intent intent = new Intent(this, GameActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt(GameActivity.levelKey, levelId);

        intent.putExtra(GameActivity.gameBundleKey, bundle);
        startActivity(intent);
    }

    public void onClickFinish(View view) {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onClickFinish(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
