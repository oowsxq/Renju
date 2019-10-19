package com.controller.setting;

/**
 * 这个类定义了存储各种全局信息的模型
 * 一个专门的 SettingDialog 用来修改 settingModel
 * 其他的组件从这个 Model 中读取值
 */
public class SettingModel {

    /*=========================================================
        基本对弈信息
     */
    //玩家是否执黑，true则玩家为黑棋，false则玩家为白棋
    public boolean playerIsBlack;

    //黑方、白方 玩家名
    public String blackName;
    public String whiteName;

    //黑方、白方最大可用时间
    public int blackTotalTime;
    public int whiteTotalTime;

    //五手N打中N的值
    public int fifthMoveSteps;


    /*=========================================================
        禁手开关标志
     */
    public boolean doubleThree;     //三三禁手
    public boolean doubleFour;      //四四禁手
    public boolean overline;        //长连


    /*=========================================================
        特殊规则开关标志
     */
    public boolean thirdMoveExchange;   //三手交换开关
    public boolean fifthMoveMultiple;  //五手N打开关


    /*=========================================================
        开局设定
     */
    public boolean openGameAsFree;    //开局方式是否是自由开局，如果此项为假则为指定开局方式
    public String initGamePattern;      //指定开局时使用的开局模式 格式范例：B(H,8);W(H,7);B(G,9);W(I,7);B(G,7); ...


    /*=========================================================
        引擎设定
     */

}
