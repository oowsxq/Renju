package com.engine.fourtransEngine;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 搜索算法，使用 MAX-MIN, alpha-beta 剪枝
 * 每展开一个节点，应当构造一个 SearchAlgorithm 对象
 */
class SearchAlgorithm{

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

    private Board board;
    private Evaluator evaluator;

    SearchAlgorithm(){
        evaluator = new Evaluator();
    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     * @param input_board 需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth 搜索深度
     * @param side 从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x 评估的落子点
     * @param y 评估的落子点
     * @return 估值的结果
     */
    public int expand(Board input_board, int depth, char side, int x, int y){
        return expand(input_board, depth, side, x, y, Evaluator.VALUE_MIN, Evaluator.VALUE_MAX);  //TODO: check min-max
    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     * @param input_board 需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth 搜索深度
     * @param side 从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x 评估的落子点
     * @param y 评估的落子点
     * @param alpha 输入的初始下界值
     * @param beta 输入的初始上界值
     * @return 估值的结果
     */
    public int expand(Board input_board, int depth, char side, int x, int y,
                      int alpha, int beta){
        this.board = new Board(input_board);
        this.board.setValue(x,y,side);
        return alphaBetaSearch(depth, side, alpha, beta);
    }

    /**
     * alpha-beta search algorithm
     * @param depth 搜索深度
     * @param side 从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param alpha 此节点的下界值
     * @param beta 此节点的上界值
     * @return 此节点的估值
     */
    private int alphaBetaSearch(int depth, char side, int alpha, int beta){
        //TODO
        //如果游戏结束 或到达指定深度 返回当前局面估值
        if (Judgementor.adjustGameOver(board) != null || depth <= 0)
            return evaluator.evaluate(board, side);


        //创建可能的走法序列
        LinkedList<SearchElement> movementList = new LinkedList<SearchElement>();
        int curr_score = 0;
        generateLegalMovements(movementList, side);


        //遍历每一个候选步骤，如果发生剪枝则提前返回
        SearchElement movement = null;
        char ours = side;
        char opposite = ours == 'b' ? 'w' :  'b';
        if (depth % 2 == 0) {
            // 当前时对手节点，计算极小值
            for (ListIterator<SearchElement> iterator = movementList.listIterator(movementList.size());
                 iterator.hasPrevious(); ) {
                movement = iterator.previous();

                board.setValue(movement.x, movement.y, opposite);    //make move
                curr_score = alphaBetaSearch(depth-1, side, alpha, beta); //搜索子节点
                board.setValue(movement.x, movement.y, 'e');     //unmake move

                if (curr_score < beta){
                    beta = curr_score;
                    if (alpha >= beta)
                        return alpha;           //alpha 剪枝
                }
            }
            return beta;    // 本节点遍历完毕，未发生剪枝
        } else {
            //当前是我方节点，计算极大值
            for (ListIterator<SearchElement> iterator = movementList.listIterator(movementList.size());
                 iterator.hasPrevious(); ) {
                movement = iterator.previous();

                board.setValue(movement.x, movement.y, ours);                       //make move
                curr_score = alphaBetaSearch(depth-1, side, alpha, beta);   //搜索子节点
                board.setValue(movement.x, movement.y, 'e');                    //unmake move

                if (curr_score > alpha){
                    alpha = curr_score;
                    if (alpha >= beta)
                        return beta;           //beta 剪枝
                }
            }
            return alpha;    // 本节点遍历完毕，未发生剪枝
        }
    }

    /**
     * 走法步骤创建，创建完毕存入走法步骤列表，并按自然序排序（升序）
     * @param movementList 走法步骤列表，必须非 null
     * @param side 从何方的角度判断 side = {'w','b'}
     */
    private void generateLegalMovements(LinkedList<SearchElement> movementList, char side){
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
    }

    class SearchElement implements Comparable{
        public int x;
        public int y;
        public int priority;

        SearchElement(int x, int y, int priority){ this.x = x; this.y = y; this.priority = priority; }

        @Override
        public int compareTo(Object o) {
            return this.priority - ((SearchElement)o).priority;
        }
    }
}