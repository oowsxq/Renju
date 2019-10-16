package com.engine;

import com.chessboard.Chessboard;

import java.util.LinkedList;

public interface Engine {
    /**
     * 平台向博弈引擎发送计算指令，引擎返回结果给平台
     * @param chessboard 当前局面的棋盘
     * @param necessarySteps 需要返回的必要落子点数量，比如在五子棋中如果需要五手二打则需要返回哪两个点
     * @param seconds 剩余可用时间，如果为负值则表示剩余不考虑此项，剩余时间无穷多
     * @return 引擎的决策结果，使用一个链表表示，如果返回的点数多于 necessarySteps 则会优先选择链表靠前的内容
     */
    public LinkedList<Step> move(Chessboard chessboard, int necessarySteps, int seconds);


    /**
     * 向引擎发送通知要求继续计算，如果已经在计算则可以忽略此通知
     */
    public void computeContinue();

    /**
     * 向引擎发送通知要求暂停计算，如果已经暂停则可以忽略此通知
     */
    public void computePause();

    /**
     * 用于表示引擎当前状态的常数
     */
    public static final int ENGINE_INITIALIZING     = 0;
    public static final int ENGINE_PAUSING          = 1;
    public static final int ENGINE_IDLE             = 2;
    public static final int ENGINE_COMPUTING        = 3;

    /**
     * 获取引擎当前状态
     * @return magicNumber = { ENGINE_INITIALIZING, ENGINE_PAUSING, ENGINE_IDLE, ENGINE_COMPUTING }
     */
    public int getStatus();

    /**
     * 引擎返回给平台的落点数据
     */
    public class Step{
        int x;
        int y;
    }
}
