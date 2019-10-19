package com.engine.fourtransEngine;

import com.chessboard.Chessboard;
import com.engine.Engine;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class FourtransEngineFactory {
    public static int engineCounter = 0;
    public static FourtransEngine createEngine(){
        FourtransEngine result = new FourtransEngine();
        Thread engingThread = new Thread(result, "Fourtrans-Engine-" + engineCounter);
        engineCounter++;
        engingThread.start();
        return result;
    }
}

class FourtransEngine implements Engine, Runnable {

    private LinkedList<Point> resultSteps = new LinkedList<Point>();
    private int currentStatus = Engine.ENGINE_INITIALIZING;

    /**
     * 引擎内部的信号表示和传递常数
     *
     *  SIGNAL                      SENDER                      RECEIVER
     *  ...
     */
    private int signal = 0x00;
    private static final int IN_COMPUTING_SIGNAL     = 0x10;
    private static final int NEED_PAUSE_SIGNAL       = 0x11;
    private static final int NEED_CONTINUE_SIGNAL    = 0x12;
    private static final int IN_GAMING_SIGNAL        = 0x20;
    private static final int NEED_ENDGAME_SIGNAL     = 0x21;


    private Chessboard chessboard = null;

    public FourtransEngine(){
        //TODO
    }

    public int[][] findForbidden(int[][] board){
        //TODO
        return null;
    }

    @Override
    public void move(Chessboard chessboard, int necessarySteps, int seconds, Queue<ResultUnit> result) {
        //TODO: 创建一个进程任务等待计算完成后返回结果
    }

    @Override
    public void removeFifthStone(Chessboard chessboard, int necessarySteps, int seconds, ResultUnit result) {
        //TODO: 创建一个进程任务等待计算完成后返回结果
    }

    @Override
    public void needExchange(Chessboard chessboard, int seconds, ResultUnit result) {
        //TODO: 创建一个进程任务等待计算完成后返回结果
    }

    @Override
    public void startNewGame(boolean forbidDoubleThree, boolean forbidDoubleFour, boolean forbidOverline, boolean openGameAsFree) {
        //TODO: 创建一个进程任务执行命令
    }

    @Override
    public void endCurrentGame() {
        //TODO: 创建一个进程任务执行命令
    }

    @Override
    public void computeContinue() {
        //TODO: 创建一个进程任务执行命令
    }

    @Override
    public void computePause() {
        //TODO: 创建一个进程任务执行命令
    }

    @Override
    public int getStatus() {
        return this.currentStatus;
    }

    /**
     * 引擎主线程运行所在的事件循环，不断监听来来自平台的信号
     */
    @Override
    public void run() {
        // Event Loop
        while(true){
            //TODO
        }
    }
}

/**
 * 等待搜索的单元，用这个数据结构组成队列进入搜索，引擎不断调用
 */
class WaittingForSearchUnit{
    Board board;
}
