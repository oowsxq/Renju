package com.engine.fourtransEngine;


import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 此类用于判断终局相关信息，全 static 设计
 * 功能：判断禁手、判断胜利
 */
public class Judgementor {
    /**
     * 三个全局禁手开关，用于表示是否禁手，在引擎创建游戏时初始化禁手标记
     */
    public static boolean forbidDoubleThree;
    public static boolean forbidDoubleFour;
    public static boolean forbidOverline;

    /**
     * 判断游戏是否结束
     * @param board 当前局面的数组表示，原点左上角
     * @return null表示非终局，'b'表示黑方五连，'w'表示白方长连，'f'表示黑方禁手白方胜
     */
    public static Character adjustGameOver(final Board board){
        boolean isend = false;
        Character result = null;

        /* 判断白方长连 */
        if (!isend){
            //TODO:判断白方长连

            //如果有白方长连 result = 'w'; isend = true;
        }

        /* 判断黑方五连 */
        if (!isend) {
            //TODO:判断黑方五连

            //如果有黑方五连 result = 'b'; isend = true;
        }

        /* 判断黑方禁手 */
        if (!isend && hasForbiddenStone(board, null)) {
            result = 'f';
            isend = true;
        }

        return result;
    }

    /**
     * 判断是否存在禁手
     * @param board 需要判断段的局面
     * @param result 如果不为 null, 则复制一份 board 并向其中写入带禁手点的位置，用'f'表示
     * @return  true 存在禁手; false 不存在
     */
    public static boolean hasForbiddenStone(final Board board, Board result){
        //TODO：判断禁手及禁手位置
        return false;
    }
}
