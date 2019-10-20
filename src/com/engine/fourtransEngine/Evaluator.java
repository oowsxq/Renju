package com.engine.fourtransEngine;

/**
 * 局面评估类，编写的时候应当考虑性能优化，尽可能实现多线程安全
 */
class Evaluator {
    /* 定义估值的最大值和最小值，无论如何估值结果应当在最大值和最小值之间 */
    public static final int VALUE_MAX = 268435455;  // 0x0FFFFFF
    public static final int VALUE_MIN = -VALUE_MAX;

    /**
     * 估值函数，
     * @apiNote 在此函数中 board 不应该被修改！推荐只使用 getValue 方法。
     *          如果必须使用修改后的状态请使用 Board 提供的复制构造方法构造临时量，避免污染输入引用
     *              举例: Board dst = new Board(src);
     * @param board 当前棋盘状态
     * @param side 从哪一方的视角评估最大值 side = { 'w', 'b' }
     * @return 估值
     */
    public int evaluate(final Board board, char side){
        //TODO:实现估值函数
        return 0;   //return a proper value here, not 0
    }
}
