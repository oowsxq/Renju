package com.engine;

import com.chessboard.Chessboard;
import com.engine.Engine.Step;

import java.util.LinkedList;

public class FourtransEngine implements Engine {

    private LinkedList<Step> resultSteps = new LinkedList<Step>();
    private int current_status = Engine.ENGINE_INITIALIZING;

    private Chessboard chessboard = null;

    public FourtransEngine(){
        //TODO
    }

    public int[][] findForbidden(int[][] board){
        //TODO
        return null;
    }

    @Override
    public LinkedList<Step> move(Chessboard chessboard, int necessarySteps, int seconds) {
        return null;
    }

    @Override
    public void computeContinue() {
        //TODO
    }

    @Override
    public void computePause() {
        //TODO
    }

    @Override
    public int getStatus() {
        //TODO
        return 0;
    }
}
