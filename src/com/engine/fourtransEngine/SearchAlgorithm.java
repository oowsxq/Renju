package com.engine.fourtransEngine;

import java.awt.*;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 搜索算法，使用 MAX-MIN, alpha-beta 剪枝
 * 每展开一个节点，应当构造一个 SearchAlgorithm 对象
 */
class SearchAlgorithm {

    private Board board;
    private Evaluator evaluator;

    SearchAlgorithm() {
        evaluator = new Evaluator();
    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     *
     * @param input_board 需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth       搜索深度
     * @param side        从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x           评估的落子点
     * @param y           评估的落子点
     * @return 估值的结果
     */
    public int expand(Board input_board, int depth, char side, int x, int y) {
        return expand(input_board, depth, side, x, y, Evaluator.VALUE_MIN, Evaluator.VALUE_MAX);  //TODO: check min-max
    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     *
     * @param input_board   需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth         搜索深度
     * @param side          从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x             评估的落子点
     * @param y             评估的落子点
     * @param currentLBound 当前顶层节点的下界值
     * @return 估值的结果
     */
    public int expand(Board input_board, int depth, char side, int x, int y, int currentLBound) {
        return expand(input_board, depth, side, x, y, currentLBound, Evaluator.VALUE_MAX);  //TODO: check min-max
    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     *
     * @param input_board 需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth       搜索深度
     * @param side        从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x           评估的落子点
     * @param y           评估的落子点
     * @param alpha       输入的初始下界值
     * @param beta        输入的初始上界值
     * @return 估值的结果
     */
    public int expand(Board input_board, int depth, char side, int x, int y,
                      int alpha, int beta) {
        this.board = new Board(input_board);
        this.board.setValue(x, y, side);
        generator = new StepGenerator();
        return alphaBetaSearch(depth, side, alpha, beta);
    }

    /**
     * alpha-beta search algorithm
     *
     * @param depth 搜索深度
     * @param side  从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param alpha 此节点的下界值
     * @param beta  此节点的上界值
     * @return 此节点的估值
     */
    private int alphaBetaSearch(int depth, char side, int alpha, int beta) {
        //TODO
        //如果游戏结束 或到达指定深度 返回当前局面估值
        if (Judgementor.adjustGameOver(board) != null || depth <= 0)
            return evaluator.evaluate(board, side);


        //创建可能的走法序列
        LinkedList<SearchElement> movementList;
        int curr_score = 0;
        movementList = generator.generateLegalMovements(board, side);


        //遍历每一个候选步骤，如果发生剪枝则提前返回
        SearchElement movement = null;
        char ours = side;
        char opposite = ours == 'b' ? 'w' : 'b';
        if (depth % 2 == 1) {
            // 当前时对手节点，计算极小值
            for (ListIterator<SearchElement> iterator = movementList.listIterator(movementList.size());
                 iterator.hasPrevious(); ) {
                movement = iterator.previous();

                board.setValue(movement.row, movement.col, opposite);    //make move
                curr_score = alphaBetaSearch(depth - 1, side, alpha, beta); //搜索子节点
                board.setValue(movement.row, movement.col, 'e');     //unmake move

                if (curr_score < beta) {
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

                board.setValue(movement.row, movement.col, ours);                       //make move
                curr_score = alphaBetaSearch(depth - 1, side, alpha, beta);   //搜索子节点
                board.setValue(movement.row, movement.col, 'e');                    //unmake move

                if (curr_score > alpha) {
                    alpha = curr_score;
                    if (alpha >= beta)
                        return beta;           //beta 剪枝
                }
            }
            return alpha;    // 本节点遍历完毕，未发生剪枝
        }
    }





    /*
    NORMAL
    =========================================================================================
    EXPERIMENTAL
     */



    private Zobrist zobrist;
    private LinkedList<Point> steps;
    private StepGenerator generator;

//    /**
//     * 在某一点落子后展开搜索算法，返回一个值
//     *
//     * @param input_board 需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
//     * @param depth       搜索深度
//     * @param side        从哪一方的视角搜索最大值 side = { 'w', 'b' }
//     * @param x           评估的落子点
//     * @param y           评估的落子点
//     * @return 估值的结果
//     */
//    public int expandExperimetnal(Board input_board, int depth, char side, int x, int y) {
//        return expand(input_board, depth, side, x, y, Evaluator.VALUE_MIN, Evaluator.VALUE_MAX);
//    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     *
     * @param input_board   需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth         搜索深度
     * @param side          从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x             评估的落子点
     * @param y             评估的落子点
     * @param currentLBound 当前顶层节点的下界值
     * @return 估值的结果
     */
    public int expandExperimetnal(Board input_board, int depth, char side, int x, int y, int currentLBound, Zobrist zobrist) {
        this.zobrist = new Zobrist(zobrist);
        generator = new StepGenerator();
        return expandExperimental(input_board, depth, side, x, y, currentLBound, Evaluator.VALUE_MAX);
    }

    /**
     * 在某一点落子后展开搜索算法，返回一个值
     *
     * @param input_board 需要评估的棋局，输入后会复制构造，不会污染输入引用，数据上不会发生并行冲突
     * @param depth       搜索深度
     * @param side        从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param x           评估的落子点
     * @param y           评估的落子点
     * @param alpha       输入的初始下界值
     * @param beta        输入的初始上界值
     * @return 估值的结果
     */
    private int expandExperimental(Board input_board, int depth, char side, int x, int y,
                      int alpha, int beta) {
        this.board = new Board(input_board);
        this.board.setValue(x, y, side);
        return alphaBetaSearchExperimental(depth, side, alpha, beta, 0, null);
    }

    /**
     * alpha-beta search algorithm - enhanced with experimental methods
     *      - Zobrist hash cache
     *      - Surrounding Search
     *
     * @param depth 搜索深度
     * @param side  从哪一方的视角搜索最大值 side = { 'w', 'b' }
     * @param alpha 此节点的下界值
     * @param beta  此节点的上界值
     * @param lasEvaluation 上一层给定的估值
     * @param step 推迟移动，上层要走的落子点， Null表示为搜索展开的顶层，即次顶层
     * @return 此节点的估值
     */
    private int alphaBetaSearchExperimental(int depth, char side, int alpha, int beta, int lasEvaluation, SearchElement step) {
        char ours = side;
        char opposite = ours == 'b' ? 'w' : 'b';

        int current_evaluation = 0;
        if (step == null)
            current_evaluation = evaluator.evaluate(board, side);
        else {
            //当前不是算法的第一层（即整个搜索的次顶层），那么推迟移动，将移动子改到搜索算法入口处执行，使用局部更新的方法快速估值
            int delta_last = evaluator.evaluateOneStone(board, step.row, step.col, side);
            board.setValue(step.row, step.col, opposite);    //make move
            int delta_new = evaluator.evaluateOneStone(board, step.row, step.col, side);
            current_evaluation = lasEvaluation - delta_last + delta_new;
        }

        //如果游戏结束 或到达指定深度 返回当前局面估值
        if (Judgementor.adjustGameOver(board) != null || depth <= 0)
            return current_evaluation;

        //创建可能的走法序列
        LinkedList<SearchElement> movementList;
        int curr_score = 0;
        movementList = generator.generateLegalMovements(board, side);

        //遍历每一个候选步骤，如果发生剪枝则提前返回
        SearchElement movement = null;
        if (depth % 2 == 1) {
            // 当前时对手节点，计算极小值
            for (ListIterator<SearchElement> iterator = movementList.listIterator(movementList.size());
                 iterator.hasPrevious(); ) {
                movement = iterator.previous();


                curr_score = alphaBetaSearchExperimental(depth - 1, side, alpha, beta, current_evaluation, movement); //搜索子节点
                board.setValue(movement.row, movement.col, 'e');     //unmake move

                if (curr_score < beta) {
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

                curr_score = alphaBetaSearchExperimental(depth - 1, side, alpha, beta, current_evaluation, movement);   //搜索子节点
                board.setValue(movement.row, movement.col, 'e');                    //unmake move

                if (curr_score > alpha) {
                    alpha = curr_score;
                    if (alpha >= beta)
                        return beta;           //beta 剪枝
                }
            }
            return alpha;    // 本节点遍历完毕，未发生剪枝
        }
    }
}


