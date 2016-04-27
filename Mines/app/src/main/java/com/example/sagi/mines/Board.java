package com.example.sagi.mines;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

public class Board  {
    private Context context;
    private Cell[][] cells;
    private int numOfFreeCells;
    private int numOfMines;
    private int widthSize;
    private int heightSize;
    private ArrayList<Integer> minesIndexes;
    private int numOfRaisedFlags = 0;

    public Board(Context context, int widthSize, int heightSize) {
        this.context = context;
        this.widthSize = widthSize;
        this.heightSize = heightSize;
        numOfFreeCells = widthSize * heightSize;
        initMinesIndexes();
        initBoard();
    }

    private void initBoard() {
        cells = new Cell[heightSize][widthSize];

        // init board
        for(int i = 0 ; i < heightSize; i++) {
            for(int j = 0 ; j < widthSize; j++) {
                cells[i][j] = new Cell(context);
            }
        }

        // fill board with mines
        for (int i = 0; i < minesIndexes.size(); i++) {
            int row = minesIndexes.get(i)/widthSize;
            int col = minesIndexes.get(i)%widthSize;

            cells[row][col].putMine();
        }

        // fill board with numbers
        for(int i = 0; i < heightSize; i++) {
            for(int j = 0; j < widthSize; j++) {
                int counter = getNumberOfNearMines(i,j);
                cells[i][j].setNumberOfNearMines(counter);
            }
        }

//        // Show all content - for checking
//        for(int i = 0 ; i < heightSize; i++) {
//            for(int j = 0 ; j < widthSize; j++) {
//                cells[i][j].showContent();
//            }
//        }
    }

    private void refreshBoard(int indexMine) {
        // add board with mine
        int row = indexMine/widthSize;
        int col = indexMine%widthSize;
        cells[row][col].putMine();
        cells[row][col].setEnabled(true);

        closeNearbyCells(row, col);

        // fill board with numbers
        for(int i = 0; i < heightSize; i++) {
            for(int j = 0; j < widthSize; j++) {
                int counter = getNumberOfNearMines(i,j);
                cells[i][j].setNumberOfNearMines(counter);
            }
        }

//        // Show all content - for checking
//        for(int i = 0 ; i < heightSize; i++) {
//            for(int j = 0 ; j < widthSize; j++) {
//                cells[i][j].showContent();
//            }
//        }
    }
    
    private int getNumberOfNearMines(int row, int col) {
        int counter = 0;

        if (row - 1 >= 0 && col - 1 >= 0 && cells[row - 1][col - 1].isMine())
            counter++;
        if (row - 1 >= 0 && cells[row - 1][col].isMine())
            counter++;
        if (row - 1 >= 0 && col + 1 < widthSize && cells[row - 1][col + 1].isMine())
            counter++;
        if (col - 1 >= 0 && cells[row][col - 1].isMine())
            counter++;
        if (col + 1 < widthSize && cells[row][col + 1].isMine())
            counter++;
        if (row + 1 < heightSize && col - 1 >= 0 && cells[row + 1][col - 1].isMine())
            counter++;
        if (row + 1 < heightSize && cells[row + 1][col].isMine())
            counter++;
        if (row + 1 < heightSize && col + 1 < widthSize && cells[row + 1][col + 1].isMine())
            counter++;
        
        return counter;
    }

    private void initMinesIndexes() {
        minesIndexes = new ArrayList<Integer>();
        addRandomMine((int)Math.round(widthSize * heightSize * 0.2));
    }

    public void clearBoard() {

    }

    public int getWidthSize() {
        return widthSize;
    }

    public int getHeightSize() {
        return heightSize;
    }

    public int getNumOfMines() {
        return numOfMines;
    }


    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public void longClickOnCell(int row, int col) {
        if(cells[row][col].isFlagRaised())
            cells[row][col].setFlagOff();
        else
            cells[row][col].setFlagOn();
    }

    public boolean clickOnCell(int row, int col) {
        if(!cells[row][col].isFlagRaised()) {
            if (cells[row][col].isMine()) {
                showAllMines(R.drawable.mine);
                cells[row][col].setBackgroundResource(R.drawable.red_mine);
                return true;
            } else {
                openAllCellNeighbors(row, col);
                return false;
            }
        }

        return false;
    }

    public int addRandomMine(int numOfMines) {
        int randIndex = -1;

        if(minesIndexes.size() < getMaxNumOfMines())  {
            int count = numOfMines;
            while (count > 0) {
                randIndex = (int) (Math.random() * widthSize * heightSize);
                if (!minesIndexes.contains(randIndex)) {
                    minesIndexes.add(randIndex);
                    addNumOfMines();
                    count--;
                }
            }
        }

        return randIndex;
    }

    public void addRandomMineAndRefreshBoard() {
        Log.i("Board", "addRandomMine");
        int index = addRandomMine(1);

        if(index != -1) {
            refreshBoard(index);
        }
    }

    public boolean isFull() {
        Log.i("Board", "numOfFreeCells - " + numOfFreeCells);
        Log.i("Board", "numOfMines - " + numOfMines);
        return numOfFreeCells-numOfMines == 0;
    }

    public void openAllCellNeighbors(int row, int col) {

        cells[row][col].showContent();
        --numOfFreeCells;

        if (cells[row][col].getNumberOfNearMines() != 0 )
            return;

        if(row-1 >= 0 && cells[row-1][col].isEnabled() && !cells[row-1][col].isFlagRaised() && !cells[row-1][col].isMine())
            openAllCellNeighbors(row-1, col);
        if(row+1 < heightSize && cells[row+1][col].isEnabled() && !cells[row+1][col].isFlagRaised() && !cells[row+1][col].isMine())
            openAllCellNeighbors(row+1, col);
        if(col-1 >= 0 && cells[row][col-1].isEnabled() && !cells[row][col-1].isFlagRaised() && !cells[row][col-1].isMine())
            openAllCellNeighbors(row, col-1);
        if(col+1 < widthSize && cells[row][col+1].isEnabled() && !cells[row][col+1].isFlagRaised() && !cells[row][col+1].isMine())
            openAllCellNeighbors(row, col+1);
    }

    private void closeNearbyCells(int row, int col) {

        cells[row][col].resetCell();

        if (row - 1 >= 0 && col - 1 >= 0) {
            if(cells[row - 1][col - 1].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row - 1][col - 1].isEnabled())
                ++numOfFreeCells;
            cells[row - 1][col - 1].resetCell();
        }

        if (row - 1 >= 0) {
            if(cells[row - 1][col].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row - 1][col].isEnabled())
                ++numOfFreeCells;
            cells[row - 1][col].resetCell();
        }

        if (row - 1 >= 0 && col + 1 < widthSize) {
            if(cells[row - 1][col + 1].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row - 1][col + 1].isEnabled())
                ++numOfFreeCells;
            cells[row - 1][col + 1].resetCell();
        }

        if (col - 1 >= 0) {
            if(cells[row][col - 1].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row][col - 1].isEnabled())
                ++numOfFreeCells;
            cells[row][col - 1].resetCell();
        }

        if (col + 1 < widthSize) {
            if(cells[row][col + 1].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row][col + 1].isEnabled())
                ++numOfFreeCells;
            cells[row][col + 1].resetCell();
        }

        if (row + 1 < heightSize && col - 1 >= 0) {
            if(cells[row + 1][col - 1].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row + 1][col - 1].isEnabled())
                ++numOfFreeCells;
            cells[row + 1][col - 1].resetCell();
        }

        if (row + 1 < heightSize) {
            if(cells[row + 1][col].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row + 1][col].isEnabled())
                ++numOfFreeCells;
            cells[row + 1][col].resetCell();
        }

        if (row + 1 < heightSize && col + 1 < widthSize) {
            if(cells[row + 1][col + 1].isFlagRaised())
                subNumOfRaisedFlag();
            if(!cells[row + 1][col + 1].isEnabled())
                ++numOfFreeCells;
            cells[row + 1][col + 1].resetCell();
        }
    }

    public void showAllMines(int imageId) {
        for (int mineIndex: minesIndexes) {
            int row = mineIndex/widthSize;
            int col = mineIndex%widthSize;
            cells[row][col].setBackgroundResource(imageId);

            if(!cells[row][col].isFlagRaised())
                addNumOfRaisedFlag();
        }
        disableAllBoard();
    }

    public void disableAllBoard() {
        for(int i=0; i<heightSize ; i++) {
            for(int j=0; j<widthSize; j++) {
                cells[i][j].setEnabled(false);
            }
        }
    }

    public void addNumOfMines() {
        this.numOfMines++;
    }

    public int getNumOfRaisedFlag() {
        return this.numOfRaisedFlags;
    }

    public void addNumOfRaisedFlag() {
        this.numOfRaisedFlags++;
    }

    public void subNumOfRaisedFlag() {
        this.numOfRaisedFlags--;
    }

    public int getMaxNumOfMines() {
        return widthSize*heightSize;
    }


}
