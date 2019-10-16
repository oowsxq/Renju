package com.controller;

import com.chessboard.ChessValue;
import com.chessboard.Chessboard;
import com.chessboard.ChessboardListener;
import com.chessboard.ChessboardPanel;
import com.controller.setting.SettingDialog;
import com.controller.setting.SettingModel;

import javax.swing.*;
import java.awt.*;

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
        if (chessboard.getChess(x,y) != ChessValue.EMPTY)
            return;
        System.out.println("set chess at" + "(" +  x + "," + y + ")");
        if (blackIsInTurnFlag) {
            chessboard.setChess(x, y, ChessValue.BLACK);
            blackIsInTurnFlag = false;
        } else {
            chessboard.setChess(x,y, ChessValue.WHITE);
            blackIsInTurnFlag = true;
        }
        chessboardPanel.resetChessboard(chessboard);
        chessboardPanel.repaint();
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

    /*(no-doc)
     * debug method
     */
    private static void debugPrompt(String message){
        System.out.println("[debug] " + "[time " + String.valueOf(System.currentTimeMillis()) + "] " + message);
    }
}
