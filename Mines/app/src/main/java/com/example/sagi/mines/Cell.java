package com.example.sagi.mines;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Button;

public class Cell extends Button {
    private char token;
    private int numberOfNearMines;
    private boolean flag;

    public Cell(Context context) {
        super(context);

        setDefaultBackroundColor();
        this.setTypeface(null, Typeface.BOLD);
    }

    public void setFlagOn() {
        this.setBackgroundResource(R.drawable.flag);
        //this.setText("" + 'F');
        flag = true;
    }

    public void setFlagOff() {
        setDefaultBackroundColor();
        this.setText("");
        flag = false;
    }

    public int getNumberOfNearMines() {
        return numberOfNearMines;
    }

    public void setNumberOfNearMines(int numberOfNearMines) {
        this.numberOfNearMines = numberOfNearMines;

        if(!isMine()) {
            switch (numberOfNearMines) {
                case 1:
                    this.setTextColor(Color.BLUE);
                    break;
                case 2:
                    this.setTextColor(Color.rgb(0, 128, 0));
                    break;
                case 3:
                    this.setTextColor(Color.RED);
                    break;
                case 4:
                    this.setTextColor(Color.rgb(0, 0, 128));
                    break;
                case 5:
                    this.setTextColor(Color.rgb(130, 0, 0));
                    break;
                case 6:
                    this.setTextColor(Color.rgb(0, 128, 128));
                    break;
                case 7:
                    this.setTextColor(Color.BLACK);
                    break;
                case 8:
                    this.setTextColor(Color.rgb(128, 128, 128));
                    break;
            }
        }
        else
            this.setTextColor(Color.GRAY);
    }

    public boolean isFlagRaised() {
        return flag==true;
    }

    public boolean isMine() {
        return token == 'M';
    }

    public void putMine() {
        this.numberOfNearMines = 0;
        token = 'M';
    }

    public void setToken(char token) {
        this.token = token;
    }

    public void showContent() {
        //this.setTextSize(30);
        this.setBackgroundColor(Color.LTGRAY);
        if(!isMine()) {
            if(numberOfNearMines == 0)
                this.setText("");
            else
                this.setText("" + numberOfNearMines);
        }
        else
            this.setText("" + token);
        this.setEnabled(false);
    }

    public void resetCell() {
        this.setEnabled(true);
        this.setText("");
        flag = false;
        setDefaultBackroundColor();
    }

    private void setDefaultBackroundColor() {
        this.setBackgroundColor(Color.rgb(155, 155, 155));
    }
}
