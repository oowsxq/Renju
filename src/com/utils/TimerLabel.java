package com.utils;

import javax.swing.*;

/**
 * 一个专门用于显示计时时间的类，拓展自 swing 中的 JLabel
 */
public class TimerLabel extends JLabel {

    private long milliseconds;

    public TimerLabel(){
        this(0);
    }

    public TimerLabel(int initMilliseconds){
        setTime(initMilliseconds);
    }

    public void setTime(long milliseconds){
        this.milliseconds = milliseconds;
        refreshLabel();
    }

    public void addTime(long milliseconds){
        this.milliseconds += milliseconds;
        refreshLabel();
    }

    /**
     * 每次修改时间后调用这个工具方法来更新标签的内容
     */
    private void refreshLabel(){
        int hour = (int)(milliseconds / 3600000);
        int minute = (int)((milliseconds - 3600000 * hour) / 60000);
        int second = (int)((milliseconds - 3600000 * hour - 60000 * minute) / 1000);
//        int millis = (int)(milliseconds - 3600000 * hour - 60000 * minute - 1000 * second);
        setText((hour == 0 ? "0" : String.valueOf(hour)) + " : " +
                (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute))
                + " : " + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second))
//                + " . " + (millis < 100 ? (millis < 10 ? "00" + String.valueOf(millis) : "0" + String.valueOf(millis))
//                                        : String.valueOf(millis))
        );
        repaint();
    }
}
