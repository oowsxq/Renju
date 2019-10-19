package com.controller.setting;

import com.utils.MyTextPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 这个类用来提供一个方便修改 settingModel 的面板
 */
public class SettingDialog extends JDialog {
    /* 本设置界面引用 */
    JDialog currDialog = this;

    /* 外观常量 */
    private static final Font GENERAL_BTN_FONT = new Font(null,Font.BOLD,15);
    private static final Color GENERAL_BTN_COLOR = Color.black;
    private static final Font GENERAL_LABEL_FONT = new Font(null, Font.PLAIN, 14);
    private static final Color GENERAL_LABEL_COLOR = Color.black;
    private static final Font GENERAL_CHECKBOX_FONT = new Font(null, Font.PLAIN, 14);
    private static final Color GENERAL_CHECKBOX_COLOR = Color.black;
    private static final Font GENERAL_TEXTFIELD_FONT = new Font(null, Font.PLAIN, 14);
    private static final Color GENERAL_TEXTFIELD_COLOR = Color.black;

    private JButton loadBtn = utilCreateGeneralBtn("Load Settings");
    private MyTextPanel myTextPanel = new MyTextPanel();
    private SettingModel settingModel;

    /* 主面板 */
    private JPanel mainPanel;

    /* 若干子面板 */
    private JPanel generalPanel = utilCreateGeneralPanel("通用设置");
    private JPanel forbiddenPanel = utilCreateGeneralPanel("禁手设置");
    private JPanel specialRulePanel = utilCreateGeneralPanel("特殊规则开关");
    private JPanel openGamePanel = utilCreateGeneralPanel("开局方式设定");
    private JPanel enginePanel = utilCreateGeneralPanel("引擎设置");

    /* 子组件 */
    private JCheckBox playerIsBlack = utilCreateGeneralCheckBox("玩家执黑棋（此项不选则玩家执白）", true);
    private JTextField blackName = utilCreateGeneralTextField("",20);
    private JTextField whiteName = utilCreateGeneralTextField("",20);
    private JTextField whiteTotalTime = utilCreateGeneralTextField("",10);
    private JTextField blackTotalTime = utilCreateGeneralTextField("",10);
    private JTextField fifthMoveSteps = utilCreateGeneralTextField("",5);

    private JCheckBox doubleThree = utilCreateGeneralCheckBox("三三禁手",true);
    private JCheckBox doubleFour = utilCreateGeneralCheckBox("四四禁手",true);
    private JCheckBox overline = utilCreateGeneralCheckBox("长连禁手",true);

    private JCheckBox thirdMoveExchange = utilCreateGeneralCheckBox("三手交换", false);
    private JCheckBox fifthMoveMultiple = utilCreateGeneralCheckBox("五手N打",false);

    private JCheckBox openGameAsFree = utilCreateGeneralCheckBox("自由开局（若此项不选则为指定开局)", false);
    private JTextField openGamePattern = utilCreateGeneralTextField("",20);

    /* 最后的按钮 */
    private JButton confirmBtn = utilCreateGeneralBtn("CONFIRM");
    private JButton cancelBtn = utilCreateGeneralBtn("CANCEL");

    public SettingDialog(Frame owner, String title, SettingModel settingModel) {
        super(owner, title, true);
        this.settingModel = settingModel;

        initGeneralPanel();
        initForbiddenPanel();
        initSpecialRulePanel();
        initOpenGamePanel();
        initEnginePanel();

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(generalPanel);
        mainPanel.add(forbiddenPanel);
        mainPanel.add(specialRulePanel);
        mainPanel.add(openGamePanel);
//        mainPanel.add(enginePanel);

        mainPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER, confirmBtn, cancelBtn));
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeIntoSettingModel();
                currDialog.setVisible(false);
            }
        });

        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currDialog.setVisible(false);
            }
        });

        setContentPane(mainPanel);
    }

    @Override
    public void setVisible(boolean b){
        if (b){
            loadFromSettingModel();
        }
        super.setVisible(b);
    }

    private void initGeneralPanel(){
        generalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                playerIsBlack
        ));
        generalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("黑方玩家名:"),blackName
        ));
        generalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("白方玩家名:"),whiteName
        ));
        generalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("黑方最大可用时间"),blackTotalTime,utilCreateGeneralLabel("秒")
        ));
        generalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("白方最大可用时间"),whiteTotalTime,utilCreateGeneralLabel("秒")
        ));
        generalPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                utilCreateGeneralLabel("五手N打中N的值"),fifthMoveSteps
        ));
    }

    private void initForbiddenPanel(){
        forbiddenPanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                doubleThree, Box.createHorizontalStrut(20),
                doubleFour, Box.createHorizontalStrut(20),
                overline
        ));
    }

    private void initSpecialRulePanel(){
        specialRulePanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                thirdMoveExchange, Box.createHorizontalStrut(20),
                fifthMoveMultiple
        ));
    }

    private void initOpenGamePanel(){
        openGamePanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                openGameAsFree
        ));
        openGamePanel.add(utilGroupHorizontalBox(UTIL_GROUP_ALIGH_CENTER,
                openGamePattern
        ));
    }

    private void initEnginePanel(){

    }

    private void loadFromSettingModel(){
        playerIsBlack.setSelected(settingModel.playerIsBlack);
        blackName.setText(settingModel.blackName);
        whiteName.setText(settingModel.whiteName);
        blackTotalTime.setText(String.valueOf(settingModel.blackTotalTime));
        whiteTotalTime.setText(String.valueOf(settingModel.whiteTotalTime));
        fifthMoveSteps.setText(String.valueOf(settingModel.fifthMoveSteps));

        doubleThree.setSelected(settingModel.doubleThree);
        doubleFour.setSelected(settingModel.doubleFour);
        overline.setSelected(settingModel.overline);

        thirdMoveExchange.setSelected(settingModel.thirdMoveExchange);
        fifthMoveMultiple.setSelected(settingModel.fifthMoveMultiple);

        openGameAsFree.setSelected(settingModel.openGameAsFree);
        openGamePattern.setText(settingModel.initGamePattern);
    }

    private void writeIntoSettingModel(){
        try {
            settingModel.playerIsBlack = playerIsBlack.isSelected();
            settingModel.blackName = blackName.getText();
            settingModel.whiteName = whiteName.getText();
            settingModel.blackTotalTime = Integer.valueOf(blackTotalTime.getText());
            settingModel.whiteTotalTime = Integer.valueOf(whiteTotalTime.getText());
            settingModel.fifthMoveSteps = Integer.valueOf(fifthMoveSteps.getText());

            settingModel.doubleThree = doubleThree.isSelected();
            settingModel.doubleFour = doubleFour.isSelected();
            settingModel.overline = overline.isSelected();

            settingModel.thirdMoveExchange = thirdMoveExchange.isSelected();
            settingModel.fifthMoveMultiple = fifthMoveMultiple.isSelected();

            settingModel.openGameAsFree = openGameAsFree.isSelected();
            settingModel.initGamePattern = openGamePattern.getText();
        }catch (Exception e){
            System.out.print(e.getStackTrace().toString());
        }
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

    private JPanel utilCreateGeneralPanel(String title){
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

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

    private JCheckBox utilCreateGeneralCheckBox(String text, boolean defaultSelected){
        JCheckBox checkBox = new JCheckBox(text, defaultSelected);
        checkBox.setForeground(GENERAL_CHECKBOX_COLOR);
        checkBox.setFont(GENERAL_CHECKBOX_FONT);
        return checkBox;
    }

    private JTextField utilCreateGeneralTextField(String text, int columns){
        JTextField textField = new JTextField(text);
        textField.setForeground(GENERAL_TEXTFIELD_COLOR);
        textField.setFont(GENERAL_TEXTFIELD_FONT);
        textField.setEditable(true);
        textField.setColumns(columns);
        return textField;
    }

}
