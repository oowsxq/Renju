package com.controller;

import com.utils.TimerLabel;

import javax.swing.*;
import java.awt.*;

/**
 * this class using for manage game layout of button, checkbox, etc.
 * this is just a view, not actual controller
 */
public class GameControlPanel extends JPanel {
    /* 当前面板的引用 */
    private JPanel currPanel = this;

    /* 外观常量 */
    private static final Font GENERAL_BTN_FONT = new Font(null,Font.BOLD,15);
    private static final Color GENERAL_BTN_COLOR = Color.black;
    private static final Font GENERAL_LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final Color GENERAL_LABEL_COLOR = Color.black;

    /* 用于响应本控制面板的接口 */
    private GameControlListener gameControlListener = null;

    /* 顶部状态显示 */
    private JPanel statusPanel;
    private JLabel currentPlayerLabel;
    private JLabel whiteNameLabel;          //白棋玩家名
    private TimerLabel whiteTimeLabel;      //白棋已用时间
    private TimerLabel whiteTotalTimeLabel; //白棋全部可用时间
    private JLabel blackNameLabel;          //黑棋玩家名
    private TimerLabel blackTimeLabel;      //黑棋已用时间
    private TimerLabel blackTotalTimeLabel; //黑棋全部可用时间

    /* 悔棋等历史纪录控制 */
    private JPanel historyPanel;
    private JButton undoBtn;
    private JButton redoBtn;

    /* 全局游戏控制：重新开始，选择参数等 */
    private JPanel globalPanel;
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton restartBtn;
    private JButton settingBtn;

//    private JToggleButton playerGoFirstTogglebtn;

    public void setWhiteName(String name){
        whiteNameLabel.setText(name);
    }

    public void setBlackName(String name){
        blackNameLabel.setText(name);
    }

    public void setWhiteUsedTime(long millis){
        whiteTimeLabel.setTime(millis);
    }

    public void setBlackUsedTime(long millis){
        blackTimeLabel.setTime(millis);
    }

    public void setWhiteTotalTime(long millis){
        whiteTotalTimeLabel.setTime(millis);
    }

    public void setBlackTotalTime(long millis){
        blackTotalTimeLabel.setTime(millis);
    }

    public void addWhiteTime(long millis){
        whiteTimeLabel.addTime(millis);
    }

    public void addBlackTime(long millis){
        blackTimeLabel.addTime(millis);
    }

    public void setCurrentPlayerLabel(String name) { currentPlayerLabel.setText(name); }

    public GameControlPanel(GameControlListener l){
        initStatusPanel();
        initHistoryPanel();
        initGlobalPanel();

        initActionListener();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(statusPanel);
        add(historyPanel);
        add(globalPanel);

        gameControlListener = l;
    }

    private void restartGame(){
        int result = JOptionPane.showConfirmDialog(
                currentPlayerLabel,
                "你是否确认重启游戏，当前所有的棋局内容将丢失。重启后你可以重新选择游戏设定。",
                "警告！",
                JOptionPane.YES_NO_OPTION);
        switch (result){
            case JOptionPane.YES_OPTION:
                settingBtn.setVisible(true);
                restartBtn.setVisible(false);
                gameControlListener.restartCommand();
                break;
            case JOptionPane.CANCEL_OPTION:
                break;
        }
    }

    private void startGame(){
        if (gameControlListener.startCommand() == 0){
            settingBtn.setVisible(false);
            restartBtn.setVisible(true);
        }
    }

    private void initActionListener(){
        redoBtn.addActionListener(e -> { gameControlListener.redoCommand(); });
        undoBtn.addActionListener(e -> { gameControlListener.undoCommand(); });
        pauseBtn.addActionListener(e -> { gameControlListener.pauseCommand(); });
        restartBtn.addActionListener(e -> { restartGame(); });
        startBtn.addActionListener(e -> { startGame(); });
        settingBtn.addActionListener(e -> { gameControlListener.settingCommand(); });
    }

    private void initStatusPanel(){
        statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));

        currentPlayerLabel = utilCreateGeneralLabel("<UNKNOWN>");

        //白棋状态子面板
        JPanel whiteSubPanel = new JPanel();
        whiteSubPanel.setBorder(BorderFactory.createTitledBorder("White Status"));
        whiteNameLabel = utilCreateGeneralLabel("WHITE");
        whiteTimeLabel = new TimerLabel(0); whiteTimeLabel.setFont(GENERAL_LABEL_FONT);
        whiteTotalTimeLabel = new TimerLabel(0); whiteTotalTimeLabel.setFont(GENERAL_LABEL_FONT);
        whiteSubPanel.setLayout(new BoxLayout(whiteSubPanel,BoxLayout.Y_AXIS));
        whiteSubPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("NAME : "), whiteNameLabel));
        whiteSubPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("USED TIME : "), whiteTimeLabel));
        whiteSubPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("TOTAL TIME : "), whiteTotalTimeLabel));

        //黑棋状态子面板
        JPanel blackSubPanel = new JPanel();
        blackSubPanel.setBorder(BorderFactory.createTitledBorder("Black Status"));
        blackNameLabel = utilCreateGeneralLabel("BLACK");
        blackTimeLabel = new TimerLabel(0); blackTimeLabel.setFont(GENERAL_LABEL_FONT);
        blackTotalTimeLabel = new TimerLabel(0); blackTotalTimeLabel.setFont(GENERAL_LABEL_FONT);
        blackSubPanel.setLayout(new BoxLayout(blackSubPanel,BoxLayout.Y_AXIS));
        blackSubPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("NAME : "), blackNameLabel));
        blackSubPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("USED TIME : "), blackTimeLabel));
        blackSubPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("TOTAL TIME : "), blackTotalTimeLabel));

        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER, currentPlayerLabel));
        statusPanel.add(blackSubPanel);
        statusPanel.add(whiteSubPanel);
    }

    private void initHistoryPanel(){
        historyPanel = new JPanel();
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));

        undoBtn = utilCreateGeneralBtn("UNDO");
        redoBtn = utilCreateGeneralBtn("REDO");

        historyPanel.setLayout(new BoxLayout(historyPanel,BoxLayout.Y_AXIS));
        historyPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                redoBtn, Box.createHorizontalStrut(10), undoBtn));
    }

    private void initGlobalPanel(){
        globalPanel = new JPanel();
        globalPanel.setBorder(BorderFactory.createTitledBorder("Global Settings"));

        startBtn = utilCreateGeneralBtn("START");
        restartBtn = utilCreateGeneralBtn("RESTART");
        pauseBtn = utilCreateGeneralBtn("PAUSE");
        settingBtn = utilCreateGeneralBtn("SETTING");

        globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.Y_AXIS));
        globalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER, pauseBtn));
        globalPanel.add(Box.createVerticalStrut(5));
        globalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER, startBtn));
        globalPanel.add(Box.createVerticalStrut(5));
        globalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER, restartBtn));
        globalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER, settingBtn));

        restartBtn.setVisible(false);
    }


    /**
     * 用于工具方法 utilGroupHorizontalBox, utilGroupVerticalBox 的一些常量
     * 用于设置对齐格式
     */
    private static final int UTIL_GROUP_ALIGH_NONE      = 0x00;    // 0000 0000
    private static final int UTIL_GROUP_ALIGH_LEFT      = 0x04;    // 0000 0100
    private static final int UTIL_GROUP_ALIGH_RIGHT     = 0x01;    // 0000 0001
    private static final int UTIL_GROUP_ALIGH_TOP       = 0x40;    // 0100 0000
    private static final int UTIL_GROUP_ALIGH_BOTTOM    = 0x10;    // 0001 0000
    private static final int UTIL_GROUP_ALIGH_CENTER    = 0x55;    // 0101 0101

    /**
     * 工具方法 将一系列的组件打包为一个 horizontal box
     * @param align 对齐格式
     * @param components 需要打包的组件
     * @return 打包结果，一个 Box
     */
    private Box utilGroupHorizontalBox(int align, Component ... components){
        Box tmp_hBox = Box.createHorizontalBox();
        if ((align & UTIL_GROUP_ALIGH_RIGHT) != 0) {
            tmp_hBox.add(Box.createHorizontalGlue());
        }
        for (Component component : components){
            tmp_hBox.add(component);
        }
        if ((align & UTIL_GROUP_ALIGH_LEFT) != 0) {
            tmp_hBox.add(Box.createHorizontalGlue());
        }
        return tmp_hBox;
    }

    /**
     * 工具方法 将一系列的组件打包为一个 vertical box
     * @param align 对齐格式
     * @param components 需要打包的组件
     * @return 打包结果，一个 Box
     */
    private Box utilGroupVerticalBox(int align, Component ... components) {
        Box tmp_vBox = Box.createVerticalBox();
        if ((align & UTIL_GROUP_ALIGH_BOTTOM) != 0) {
            tmp_vBox.add(Box.createVerticalGlue());
        }
        for (Component component : components) {
            tmp_vBox.add(component);
        }
        if ((align & UTIL_GROUP_ALIGH_TOP) != 0) {
            tmp_vBox.add(Box.createVerticalGlue());
        }
        return tmp_vBox;
    }

    /**
     * 工具方法 生成一个通用按钮
     * @param text
     * @return
     */
    private JButton utilCreateGeneralBtn(String text){
        JButton tmp = new JButton();
        tmp.setForeground(GENERAL_BTN_COLOR);
        tmp.setFont(GENERAL_BTN_FONT);
        tmp.setText(text);
        return tmp;
    }

    private JLabel utilCreateGeneralLabel(String text){
        JLabel label = new JLabel();
        label.setForeground(GENERAL_LABEL_COLOR);
        label.setFont(GENERAL_LABEL_FONT);
        label.setText(text);
        return label;
    }
}
