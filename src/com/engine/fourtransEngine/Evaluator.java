package com.engine.fourtransEngine;

/**
 * 局面评估类，编写的时候应当考虑性能优化，尽可能实现多线程安全
 */
class Evaluator {
    public static final int VALUE_MAX = 0x0FFFFFFF;
    public static final int VALUE_MIN = -VALUE_MAX;

    /**
     * 估值函数，
     * @apiNote 在此函数中 status 不应该被修改，推荐只使用 getValue 方法。
     *          如果使用修改后的状态请使用 Board 提供的复制构造方法，避免污染输入数据
     *              举例: Board dst = new Board(src);
     * @param board 当前棋盘状态
     * @return 估值
     */
    public int evaluate(final Board board){
        //TODO:实现估值函数
        return 0;   //return a proper value here, not 0
    }
}
