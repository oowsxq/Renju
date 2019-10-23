package com.engine.fourtransEngine;

import com.chessboard.ChessValue;
import com.chessboard.Chessboard;
import com.engine.Engine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * 简单随机引擎，用于平台测试
 */
public class SimpleRandomEngine implements Engine {
    Random rand = new Random();
    int curr_status = Engine.ENGINE_STANDBY;

    @Override
    public Queue<ResultUnit> move(Chessboard chessboard, int necessarySteps, int seconds) {
        curr_status = Engine.ENGINE_COMPUTING;
        Queue<ResultUnit> result = new LinkedList<ResultUnit>();
        for (int i = 0; i < necessarySteps; i++) {
            int x, y;
            x = (int) rand.nextDouble() * chessboard.getBoardSize();
            y = (int) rand.nextDouble() * chessboard.getBoardSize();
            while (chessboard.getChessValue(x, y) != ChessValue.EMPTY) {
                x = (int)(rand.nextDouble() * chessboard.getBoardSize());
                y = (int)(rand.nextDouble() * chessboard.getBoardSize());
            }
            result.add(new ResultUnit(x,y));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        curr_status = Engine.ENGINE_READY;
        return result;
    }

    @Override
    public ResultUnit reserveOneFifthStone(Chessboard chessboard, int seconds) {
        ResultUnit result = new ResultUnit();
        for (int i = 0; i < chessboard.getBoardSize(); i++)
            for (int j = 0; j < chessboard.getBoardSize(); j++)
                if (chessboard.getChessOrder(i,j) == 5){
                    result.row = i;
                    result.col = j;
                    return result;
                }
        return result;
    }

    @Override
    public ResultUnit needExchange(Chessboard chessboard, int seconds) {
        ResultUnit result = new ResultUnit();
        result.needExchange = rand.nextDouble() < 0.5;
        return result;
    }

    @Override
    public void startNewGame(boolean forbidDoubleThree, boolean forbidDoubleFour, boolean forbidOverline, boolean openGameAsFree) {
        System.out.println("[Engine get command]" + "start new game");
        curr_status = Engine.ENGINE_READY;
    }

    @Override
    public void endCurrentGame() {
        System.out.println("[Engine get command]" + "end current game");
        curr_status = Engine.ENGINE_STANDBY;
    }

    @Override
    public void computeContinue() {
        System.out.println("[Engine get command]" + "compute continue");
        curr_status = Engine.ENGINE_READY;
    }

    @Override
    public void computePause() {
        System.out.println("[Engine get command]" + "compute pause");
        curr_status = Engine.ENGINE_PAUSING;
    }

    @Override
    public void computeEnd() {
        System.out.println("[Engine get command]" + "compute end");
        curr_status = Engine.ENGINE_READY;
    }

    @Override
    public int getStatus() {
        return curr_status;
    }
}
