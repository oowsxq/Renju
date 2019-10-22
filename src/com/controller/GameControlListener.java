package com.controller;

/**
 * 控制面板监听器，若存在返回值则 0 表示正常，非零表示操作不成功
 */
public interface GameControlListener {
    public void redoCommand();
    public void undoCommand();
    public void pauseCommand();
    public int startCommand();
    public void restartCommand();
    public void settingCommand();
}
