package com.controller;

import com.chessboard.ChessValue;
import com.chessboard.Chessboard;
import com.chessboard.ChessboardListener;
import com.chessboard.ChessboardPanel;
import com.controller.setting.SettingDialog;
import com.controller.setting.SettingModel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 */
public class GameFrame extends JFrame implements
        ChessboardListener, GameControlListener {

    /* 这个组件的自身引用 */
    JFrame currFrame = this;

    /**==========================
     * flags 用于控制全局状态
     */
    private boolean gamePlayingFlag = false;        //当前有戏是否在进行中
    boolean blackIsInTurnFlag = true;               //当前是黑方思考落子
    boolean inWaittingFlag = false;                 //当前平台正在等待引擎的落子数据

    /**==========================
     * 各种图形化组件
     */
    ChessboardPanel chessboardPanel;
    GameControlPanel gameControlPanel;
    SettingDialog settingDialog;


    /**===========================
     * 数据组件
     */
    Chessboard chessboard = null;
    SettingModel settingModel = new SettingModel();

    /**===========================
     * 其他的一些全局用数据
     */
    int currOrder = 1;  //当前正在准备下的下一子编号

    /**
     * constructor of main frame
     */
    public GameFrame(String title){

        //set title
        if (title == null)
            this.setTitle("Game");
        else
            this.setTitle(title);

        chessboard = new Chessboard(15);
        chessboardPanel = new ChessboardPanel(chessboard,this);
        settingDialog = new SettingDialog(this, "settings", settingModel);

        gameControlPanel = new GameControlPanel(this);

        getContentPane().add(chessboardPanel, BorderLayout.CENTER);
        getContentPane().add(gameControlPanel,BorderLayout.EAST);

        setBounds(200,200,800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void selectChessPosition(int x, int y) {
        // TODO
        if (chessboard.getChessValue(x,y) != ChessValue.EMPTY)
            return;
        System.out.println("set chess at" + "(" +  x + "," + y + ")");
        if (blackIsInTurnFlag) {
            chessboard.setChessValue(x, y, ChessValue.BLACK, currOrder);
            blackIsInTurnFlag = false;
        } else {
            chessboard.setChessValue(x,y, ChessValue.WHITE, currOrder);
            blackIsInTurnFlag = true;
        }
        currOrder ++;
        chessboardPanel.repaint();
        transChessValueArray2CharArray(chessboard.getChessValueArray());
    }

    @Override
    public void redoCommand() {
        debugPrompt("redo command");
        // TODO
    }

    @Override
    public void undoCommand() {
        debugPrompt("undo command");
        // TODO
    }

    @Override
    public void pauseCommand() {
        debugPrompt("pause command");
        // TODO
    }

    @Override
    public void startCommand() {
        debugPrompt("start command");
        // TODO
    }

    @Override
    public void restartCommand() {
        debugPrompt("restart command");
        // TODO
    }

    @Override
    public void settingCommand() {
        debugPrompt("setting command");
        // TODO
        if (!gamePlayingFlag) {
            settingDialog.setBounds(200, 100, 500, 550);
            settingDialog.setVisible(true);
        }
    }

    /**
     * 判断游戏是否结束，通过 GameResult 返回结果
     * @param chessboard
     * @return
     */
    public GameResult Adjuster(Chessboard chessboard){
        class Adjuster{
            /**
             * 判断 target 在棋盘中哪里五连
             * @param board
             * @param target
             * @return 若发现五连则返回五连的相关点 否则只返回 null
             */
            public LinkedList<Point> adjust5(char[][] board, char target){
                //TODO
                return null;
            }

            /**
             * 判断 target 在棋盘中是否出现禁手
             * @param board
             * @param target
             * @param forbidOverline        是否判断长连禁手
             * @param forbidDoubleThree     是否判断三三禁手
             * @param forbidDoubleFour      是否判断四四禁手
             * @return 若发现禁手则返回禁手相关点 否则只返回 null
             */
            public LinkedList<Point> adjustForbidden(char[][] board, char target,
                                                     boolean forbidOverline,
                                                     boolean forbidDoubleThree,
                                                     boolean forbidDoubleFour){
                //TODO
                return null;
            }
        }

        GameResult gameResult = new GameResult();
        Adjuster adjuster = new Adjuster();
        LinkedList<Point> adjustResult = null;

        char[][] board = transChessValueArray2CharArray(chessboard.getChessValueArray());
        int size = chessboard.getBoardSize();

        /* 判断黑方 连5 */
        if ((adjustResult = adjuster.adjust5(board, 'b')) != null)
            return new GameResult(0x10, adjustResult);

        /* 判断白方 连5 */
        if ((adjustResult = adjuster.adjust5(board, 'w')) != null)
            return new GameResult(0x20, adjustResult);

        /* 判断黑方禁手 */
        if ((adjustResult = adjuster.adjustForbidden(board, 'b',
            settingModel.overline, settingModel.doubleThree, settingModel.doubleFour
        )) != null)
            return new GameResult(0x21,adjustResult);

        return new GameResult(0, null);
    }

    /**
     * 工具方法，将棋盘的棋子落点数组改为字符数组
     *      ChessValue.BLACK -> 'b'
     *      ChessValue.WHITE -> 'w'
     *      ChessValue.EMPTY -> 'e'
     * @param board
     * @return
     */
    private char[][] transChessValueArray2CharArray(ChessValue[][] board){
        int size = chessboard.getBoardSize();
        char[][] results = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                switch (chessboard.getChessValue(i,j)){
                    case BLACK: results[i][j] = 'b'; break;
                    case WHITE: results[i][j] = 'w'; break;
                    default: results[i][j] = 'e'; break;
                }
            }
        }
//        System.out.println(Arrays.deepToString(results)); //debug
        return results;
    }

    /*(no-doc)
     * debug method
     */
    private static void debugPrompt(String message){
        System.out.println("[debug] " + "[time " + String.valueOf(System.currentTimeMillis()) + "] " + message);
    }

    private class GameResult {
        public int result = 0; // 0 未终局; 0x10 黑方胜，黑方5子 ; 0x20 白方胜，白方5子; 0x21 白方胜，黑方禁手
        public LinkedList<Point> path = new LinkedList<Point>();
        public GameResult(){}
        public GameResult(int result, LinkedList<Point> path){
            this.result = result;
            this.path = path;
        }
    }
}
