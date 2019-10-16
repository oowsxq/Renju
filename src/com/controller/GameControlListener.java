package com.controller;

public interface GameControlListener {
    public void redoCommand();
    public void undoCommand();
    public void pauseCommand();
    public void startCommand();
    public void restartCommand();
    public void settingCommand();
}
