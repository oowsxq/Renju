package com.engine;

import com.chessboard.Chessboard;

import java.util.Queue;

public interface Engine {

    /**
     * 用于表示引擎当前状态的常数
     */
    public static final int ENGINE_INITIALIZING     = 0x01; //正在初始化，不能接收新的计算通知，尚不能进行游戏
    public static final int ENGINE_STANDBY          = 0x10; //初始化完成，游戏未开始，可以接收新的计算通知，可以随时开始新对弈
    public static final int ENGINE_PAUSING          = 0x20; //暂停状态，游戏未结束，不能接收新的计算通知，保存有棋局状态
    public static final int ENGINE_READY            = 0x30; //就绪状态，游戏未结束，可以接收新的计算通知，保存有棋局状态
    public static final int ENGINE_COMPUTING        = 0x40; //正在计算，游戏未结束，不能接收新的计算通知

    /**
     * 平台向博弈引擎发送指令要求返回下一步落子位置
     *  @param chessboard 当前局面的棋盘
     * @param necessarySteps 需要返回的必要落子点数量，比如在五子棋中如果需要五手二打则需要返回哪两个点
     * @param seconds 剩余可用时间，如果为负值则表示剩余不考虑此项，剩余时间无穷多
     * @return 一个非null队列引用，引擎将结果写入其中，靠前的优先选择
     */
    public Queue<ResultUnit> move(Chessboard chessboard, int necessarySteps, int seconds);

    /**
     * 平台向博弈引擎发送指令要求返回保留一个第五手黑子的位置
     *  @param chessboard 当前局面的棋盘
     * @param seconds 剩余可用时间，如果为负值则表示剩余不考虑此项，剩余时间无穷多
     * @return 一个 ResultUnit 非null引用，引擎将结果写入其中
     */

    public ResultUnit reserveOneFifthStone(Chessboard chessboard, int seconds);

    /**
     * 平台向博弈引擎发送指令询问是否要三手交换
     * @param chessboard 当前局面的棋盘
     * @param seconds 剩余可用时间，如果为负值则表示剩余不考虑此项，剩余时间无穷多
     * @return 引擎返回的结果
     */
    public ResultUnit needExchange(Chessboard chessboard, int seconds);

    /**
     * 向引擎发送通知开始新对弈，
     * 期待引擎状态 ENGINE_STANDBY -> ENGINE_READY
     *
     * @param forbidDoubleThree 是否启用三三禁手
     * @param forbidDoubleFour 是否启用四四禁手
     * @param forbidOverline 是否启用长连禁手
     * @param openGameAsFree 是否为自由开局，若为 false 则为指定开局
     */
    public void startNewGame(boolean forbidDoubleThree,
                             boolean forbidDoubleFour,
                             boolean forbidOverline,
                             boolean openGameAsFree);

    /**
     * 向引擎发送通知上一局对弈已经结束
     * 期待引擎状态 (ANY STATUS) -> ENGINE_STANDBY
     *
     */
    public void endCurrentGame();

    /**
     * 向引擎发送通知要求继续计算，如果不是暂停状态则可以忽略此通知
     * 期待引擎状态 ENGINE_PAUSING -> ENGINE_COMPUTING
     *
     */
    public void computeContinue();

    /**
     * 向引擎发送通知要求暂停计算，如果不是计算中状态则可以忽略此通知
     * 期待引擎状态 ENGINE_COMPUTING -> ENGINE_PAUSING
     *
     */
    public void computePause();

    /**
     * 放弃当前计算的内容，如果当前不在计算状态则可以忽略此通知
     * 期待引擎状态 ENGINE_COMPUTING -> ENGINE_READY
     */
    public void computeEnd();

    /**
     * 获取引擎当前状态
     * PS: 此方法应当立即返回，必须是非阻塞的
     * @return magicNumber = { ENGINE_... }
     */
    public int getStatus();

    /**
     * 专门用于引擎返回结果给平台的数据结构，不考虑效率，功能优先
     * 要求返回给平台的数据里原点在左下角
     */
    class ResultUnit{
        public int row;                   //落子 y 坐标
        public int col;                   //落子 x 坐标
        public boolean needExchange;    //true 表示需要进行三手交换

        public ResultUnit(){}
        public ResultUnit(int row, int col){
            this.row = row;
            this.col = col;
        }
        public ResultUnit(int row, int col, boolean needExchange){
            this.row = row;
            this.col = col;
            this.needExchange =needExchange;
        }
    }
}

