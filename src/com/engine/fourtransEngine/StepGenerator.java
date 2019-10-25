package com.engine.fourtransEngine;

import java.util.LinkedList;

/**
 * 全静态设计，走法生成器
 */
public class StepGenerator {

    //最大搜索宽度 = MAX_SEARCH_WIDTH_BASE + 当前局面上落子数 * MAX_SEARCH_WIDTH_RATIO
    private static int MAX_SEARCH_WIDTH_BASE = 15;
    private static double MAX_SEARCH_WIDTH_RATIO = 0.5;

    private static int[][] basePriority =
            {
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                    {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
                    {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    {0,1,2,9,3,3,3,3,3,3,3,9,2,1,0},
                    {0,1,2,3,4,4,4,4,4,4,4,3,2,1,0},
                    {0,1,2,3,4,5,5,5,5,5,4,3,2,1,0},
                    {0,1,2,3,4,5,6,6,6,5,4,3,2,1,0},
                    {0,1,2,3,4,5,6,9,6,5,4,3,2,1,0},
                    {0,1,2,3,4,5,6,6,6,5,4,3,2,1,0},
                    {0,1,2,3,4,5,5,5,5,5,4,3,2,1,0},
                    {0,1,2,3,4,4,4,4,4,4,4,3,2,1,0},
                    {0,1,2,9,3,3,3,3,3,3,3,9,2,1,0},
                    {0,1,2,2,2,2,2,2,2,2,2,2,2,1,0},
                    {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
            };
    private static int surroundingPriority = 10;

    /**
     * 走法步骤创建，创建完毕存入走法步骤列表，并按自然序排序（升序）
     * @param board 需要生成走法的局面
     * @param side 从何方的角度判断 side = {'w','b'}
     */
    public static LinkedList<SearchElement> generateLegalMovements(Board board, char side){
        LinkedList<SearchElement> movementList = new LinkedList<SearchElement>();

        //使用 tmp 来创建走法序列，如果是黑方，则要判断禁手
        Board tmp = null;
        if (side == 'w')
            tmp = board;
        else {
            tmp = new Board(board);
            Judgementor.hasForbiddenStone(board, tmp);
        }

        //对棋盘上每个点计算权重
        int[][] priorities = new int[15][15];
        for (int i = 0; i < Board.SIZE; i++){
            System.arraycopy(basePriority[i],0,priorities[i],0,15);
        }
        /*
            使用位操作获得边界信息
            000     111     011     110
            1X1 1F  1X1 F8  0X1 6B  1X0 D6
            111     000     011     110
         */
        char tmp_stone; //用来暂存当前正在判断的从棋盘中取出的落子值
        int stone_counter = 0;  //当前局面的落子数
        for (int i = 0; i < Board.SIZE; i++)
            for (int j = 0; j < Board.SIZE; j++){
                tmp_stone = board.getValue(i,j);
                if (tmp_stone == (byte)'w' || tmp_stone == (byte)'b') {
                    stone_counter++;

                    int sign = 0x0FF;
                    if (i == 0) sign &= 0x01F;
                    if (i == 14) sign &= 0x0F8;
                    if (j == 0) sign &= 0x06B;
                    if (j == 14) sign &= 0x0D6;

                    /* 绝大多数坐标并非边界点，故直接赋值 */
                    if (sign == 0x0FF) {
                        priorities[i-1][j-1]        += surroundingPriority;
                        priorities[i-1][j]          += surroundingPriority;
                        priorities[i-1][j+1]        += surroundingPriority;
                        priorities[i][j-1]          += surroundingPriority;
                        priorities[i][j+1]          += surroundingPriority;
                        priorities[i+1][j-1]        += surroundingPriority;
                        priorities[i+1][j]          += surroundingPriority;
                        priorities[i+1][j+1]        += surroundingPriority;
                    } else {
                        /* 对边界点单独判断，挨个赋值 */
                        if ((sign & 0x080) != 0) priorities[i-1][j-1]      += surroundingPriority;
                        if ((sign & 0x040) != 0) priorities[i-1][j]        += surroundingPriority;
                        if ((sign & 0x020) != 0) priorities[i-1][j+1]      += surroundingPriority;
                        if ((sign & 0x010) != 0) priorities[i][j-1]        += surroundingPriority;
                        if ((sign & 0x08) != 0) priorities[i][j+1]         += surroundingPriority;
                        if ((sign & 0x04) != 0) priorities[i+1][j-1]       += surroundingPriority;
                        if ((sign & 0x02) != 0) priorities[i+1][j]         += surroundingPriority;
                        if ((sign & 0x01) != 0) priorities[i+1][j+1]       += surroundingPriority;
                    }
                }
            }

        //按权重插入走法列表，升序排序
        for (int i = 0; i < Board.SIZE; i++)
            for (int j = 0; j < Board.SIZE; j++)
                if (board.getValue(i,j) == (byte)'e')
                    movementList.add(new SearchElement(i,j,priorities[i][j]));
        movementList.sort(null);

        //计算搜索宽度，移除多余步骤，减少计算量
        int width = (int)(stone_counter * MAX_SEARCH_WIDTH_RATIO ) + MAX_SEARCH_WIDTH_BASE;
        if (movementList.size() > width){
            int extra_num = movementList.size() - width;
            for (int i = 0; i < extra_num; i++)
                movementList.remove();
        }

        return movementList;
    }
}
