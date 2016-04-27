package com.example.sagi.mines;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {
    private final double SECONDS_FOR_WAIT = 2 + 13*85/1000;
    private long lastPress;

    public static final String gameBundleKey = "gameBundle";
    public static final String levelKey = "level";
    public static final String statusKey = "status";
    public static final String timeKey = "time";

    private GridLayout table;

    private Handler timer = new Handler();
    private boolean timerStarted;
    private int secondsPassed = 0;
    private Board board;
    private boolean gameEnd;
    private int level;

    private TextView timerLabel;
    private TextView minesLabel;
    private ImageView smile;

    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;

    /** Handler of incoming messages from service */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MovementService.MSG_ADD_RANDOM_MINE:
                    board.addRandomMineAndRefreshBoard();
                    updateMinesLabel();

                    if(board.getNumOfMines() == board.getMaxNumOfMines()) {
                        loseGame(null);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /** Target we publish for clients to send messages to IncomingHandler */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /** Class for interacting with the main interface of the service */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            Log.i("GameAcivity", "Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, MovementService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

                Log.i("GameAcivity", "client registerSensorListener");
                Message msg2 = Message.obtain(null, MovementService.MSG_REGISTER_SENSOR_LISTENER);
                msg2.replyTo = mMessenger;
                mService.send(msg2);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Log.i("GameAcivity", "client service connected");
            //Toast.makeText(GameActivity.this, "R.string.remote_service_connected", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            Log.i("GameAcivity", "Disconnected.");

            // As part of the sample, tell the user what happened.
            //Toast.makeText(GameActivity.this, "R.string.remote_service_disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(GameActivity.this, MovementService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.i("GameAcivity", "Binding.");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, MovementService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            Log.i("GameAcivity", "Unbinding.");
        }
    }

    //********************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        minesLabel = (TextView)findViewById(R.id.minesText);
        timerLabel = (TextView)findViewById(R.id.timerText);
        table = (GridLayout) findViewById(R.id.boardTable);
        smile = (ImageView) findViewById(R.id.smileImage);

        // set font style for timer and mine count to LCD style
        Typeface lcdFont = Typeface.createFromAsset(getAssets(), "fonts/lcd2mono.ttf");
        minesLabel.setTypeface(lcdFont);
        timerLabel.setTypeface(lcdFont);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(GameActivity.gameBundleKey);

        level = bundle.getInt(GameActivity.levelKey);
        switch (level) {
            case 0:
                board = new Board(this, 3,5);
                break;
            case 1:
                board = new Board(this, 5,7);
                break;
            case 2:
                board = new Board(this, 7,9);
                break;
        }

        startTime();
        updateMinesLabel();
        drawBoard(board);
    }

    @Override
    protected void onStart() {
        Log.i("GameAcivity", "onStart");
        super.onStart();
        doBindService();
    }

    protected void onResume() {
        Log.i("GameAcivity", "onResume");
        super.onResume();
        startTime();
    }

    protected void onPause() {
        Log.i("GameAcivity", "onPause");
        super.onPause();
        stopTime();
    }

    @Override
    protected void onStop() {
        Log.i("GameAcivity", "onStop");
        super.onStop();

        if (mIsBound) {
            if (mService != null) {
                try {
                    Log.i("GameAcivity", "client unregisterSensorListener");
                    Message msg = Message.obtain(null, MovementService.MSG_UNREGISTER_SENSOR_LISTENER);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        doUnbindService();
    }

    public boolean isGameEnd() {
        return gameEnd;
    }

    public boolean isWin() {
        return board.isFull();
    }

    private void drawBoard(final Board board) {
        int numOfcolumns = board.getWidthSize();
        int numOfrows = board.getHeightSize();
        table.setColumnCount(numOfcolumns);
        table.setRowCount(numOfrows);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        RelativeLayout statusLayout = (RelativeLayout) findViewById(R.id.statusLayout);
        statusLayout.measure(0, 0);

        int heightMargin = (int)getResources().getDimension(R.dimen.activity_vertical_margin);
        int widthMargin = (int)getResources().getDimension(R.dimen.activity_horizontal_margin);

        int screenHeight = size.y - dpToPx(statusLayout.getMeasuredHeight()) - heightMargin*2 - (numOfrows*2)*2;
        int screenWidth = size.x - widthMargin*2 - (numOfcolumns*2)*2;
        int minSize = Math.min(screenHeight/numOfrows, screenWidth/numOfcolumns);

        for (int row = 0; row < numOfrows; row++)
        {
            for (int column = 0; column < numOfcolumns; column++) {
                GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(column));
                cellParams.width = minSize;
                cellParams.height = minSize;
                cellParams.setMargins(2,2,2,2);
                board.getCell(row, column).setLayoutParams(cellParams);
                table.addView(board.getCell(row, column), cellParams);

                final int currentRow = row;
                final int currentColumn = column;
                board.getCell(row, column).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeSmileOnClick();
                        boolean clickOnMine = board.clickOnCell(currentRow, currentColumn);
                        Cell c = board.getCell(currentRow, currentColumn);
                        if (!clickOnMine && !c.isFlagRaised()) {
                            if (isWin())
                                winGame();
                        }
                        else if (clickOnMine)
                            loseGame(c);
                    }
                });

                board.getCell(row, column).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        changeSmileOnClick();
                        board.longClickOnCell(currentRow, currentColumn);
                        Cell c = board.getCell(currentRow, currentColumn);
                        if (c.isFlagRaised()) {
                            board.addNumOfRaisedFlag();
                            updateMinesLabel();
                        }
                        else {
                            board.subNumOfRaisedFlag();
                            updateMinesLabel();
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void changeSmileOnClick() {
        smile.setImageResource(R.drawable.surprise);
        new CountDownTimer((int)(0.1*1000), 1000) {
            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                if(isWin())
                    smile.setImageResource(R.drawable.cool);
                else if(isGameEnd())
                    smile.setImageResource(R.drawable.sad);
                else
                    smile.setImageResource(R.drawable.smile);
            }
        }.start();
    }

    private void gotoRegisterActivity() {
        final Intent intent = new Intent(this, RegisterActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(statusKey, "win");
        bundle.putString(timeKey, paddingWithZero(secondsPassed));
        bundle.putInt(levelKey, level);

        intent.putExtra(RegisterActivity.registerBundleKey, bundle);

        new CountDownTimer((int)(SECONDS_FOR_WAIT*1000), 1000) {

            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                startActivity(intent);
            }
        }.start();
    }

    private void gotoFinishActivity(String status) {
        final Intent intent = new Intent(this, FinishActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(statusKey, status);
        bundle.putString(timeKey, paddingWithZero(secondsPassed));
        bundle.putInt(levelKey, level);

        intent.putExtra(FinishActivity.finishBundleKey, bundle);

        new CountDownTimer((int)(SECONDS_FOR_WAIT*1000), 1000) {

            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                startActivity(intent);
            }
        }.start();
    }

    public void setGameEnd() {
        gameEnd = true;
    }

    private void winGame(){
        stopTime();
        setGameEnd();
        board.showAllMines(R.drawable.flag);
        smile.setImageResource(R.drawable.cool);
        updateMinesLabel();
        winAnimation();

        gotoRegisterActivity();
    }

    private void loseGame(Cell clickedCell){
        stopTime();
        setGameEnd();
        board.disableAllBoard();
        smile.setImageResource(R.drawable.sad);
        loseAnimation(clickedCell);

        gotoFinishActivity("lose");
    }

    void loseAnimation(Cell clickedCell) {
        int startDelay = 0;

        if(clickedCell != null) {
            startDelay = 500;

            clickedCell.setBackgroundResource(R.drawable.bomb);
            AnimationDrawable bombAnim = (AnimationDrawable) clickedCell.getBackground();
            bombAnim.start();

            clickedCell.bringToFront();
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(clickedCell, "scaleX", 2.25f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(clickedCell, "scaleY", 2.25f);
            scaleDownX.setDuration(200);
            scaleDownY.setDuration(200);

            AnimatorSet scale = new AnimatorSet();
            scale.play(scaleDownX).with(scaleDownY).after(startDelay);
            scale.start();
        }

        for (int i = 0; i <board.getWidthSize(); i++) {
            for (int j = 0; j <board.getHeightSize(); j++) {
                Cell c = board.getCell(j, i);

                Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
                shakeAnim.setStartOffset(startDelay + 85);
                c.startAnimation(shakeAnim);

                ObjectAnimator moveAnim = ObjectAnimator.ofFloat(c,"y",2000);
                moveAnim.setDuration((int) (Math.random() * 1000 * SECONDS_FOR_WAIT));

                ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(c,"rotation",0f,360f);
                rotateAnim.setDuration((int) (Math.random() * 1000 * SECONDS_FOR_WAIT));


                if(c!=clickedCell) {
                    AnimatorSet set = new AnimatorSet();
                    set.play(moveAnim).with(rotateAnim).after(10*85);

                    set.start();
                }
            }
        }
    }

    void winAnimation() {
        ImageView imageView = (ImageView) findViewById(R.id.imageFire);
        imageView.setVisibility(View.VISIBLE);
        ((AnimationDrawable) imageView.getBackground()).start();

        ImageView imageView2 = (ImageView) findViewById(R.id.imageFire2);
        imageView2.setVisibility(View.VISIBLE);
        ((AnimationDrawable) imageView2.getBackground()).start();

        ImageView imageView3 = (ImageView) findViewById(R.id.imageDance);
        imageView3.setVisibility(View.VISIBLE);
        ((AnimationDrawable) imageView3.getBackground()).start();
    }

    public void startTime() {
        if (!timerStarted) {
            timer.postDelayed(updateTimeElasped, 1000);
            timerStarted = true;
        }
    }

    public void stopTime() {
        timer.removeCallbacks(updateTimeElasped);
        timerStarted = false;
    }

    private void updateMinesLabel() {
        minesLabel.setText(paddingWithZero(board.getNumOfMines() - board.getNumOfRaisedFlag()));
    }

    // timer call back when timer is ticked
    private Runnable updateTimeElasped = new Runnable()
    {
        public void run()
        {
            ++secondsPassed;
            timerLabel.setText(paddingWithZero(secondsPassed));

            long currentMilliseconds = System.currentTimeMillis();
            timer.postAtTime(this, currentMilliseconds);
            timer.postDelayed(updateTimeElasped, 1000);
        }
    };

    private int dpToPx(int dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float densityDpi = metrics.density;
        Log.i("GameAcivity", "densityDpi = " + densityDpi);

        return (int)densityDpi*dp;
    }

    private String paddingWithZero(int x) {
        if (x >= 0) {
            if (x < 10) {
                return "00" + Integer.toString(x);
            } else if (x < 100) {
                return "0" + Integer.toString(x);
            } else {
                return Integer.toString(x);
            }
        }
        else {
            return Integer.toString(x);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            long currentTime = System.currentTimeMillis();

            if(currentTime - lastPress > 5000){
                Toast.makeText(getApplicationContext(), R.string.game_press_back_alert, Toast.LENGTH_SHORT).show();
                lastPress = currentTime;
            }
            else {
                gotoMainActivity();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
