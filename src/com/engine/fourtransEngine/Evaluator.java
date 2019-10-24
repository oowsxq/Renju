package com.engine.fourtransEngine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 局面评估类，编写的时候应当考虑性能优化，尽可能实现多线程安全
 */
class Evaluator {
    /* 定义估值的最大值和最小值，无论如何估值结果应当在最大值和最小值之间 */
    public static final int VALUE_MAX = 268435455; // 0x0FFFFFF
    public static final int VALUE_MIN = -VALUE_MAX;
    /* 算法会创建许多临时String对象，使用公共缓冲区提高性能 */
    private static final StringBuffer chessForm = new StringBuffer(Board.SIZE);
    /* 定义各种棋型的正则表达式和估值 */
    private static final String HUO_YI_WHITE = "eewee";                                             // 白棋活一
    private static final String HUO_YI_BLACK = "eebee";                                             // 黑棋活一
    private static final String CHONG_YI_WHITE = "bweee";                                           // 白棋冲一
    private static final String CHONG_YI_BLACK = "wbeee";                                           // 黑棋冲一
    private static final String HUO_ER_WHITE = "eewwe|ewwee|ewewe";                                 // 白棋活二
    private static final String HUO_ER_BLACK = "eebbe|ebbee|ebebe";                                 // 黑棋活二
    private static final String CHONG_ER_WHITE = "bwwee|bwewe|eewwb|ewewb";                         // 白棋冲二
    private static final String CHONG_ER_BLACK = "wbbee|wbebe|eebbw|ebebw";                         // 黑棋冲二
    private static final String HUO_SAN_WHITE = "ewwwe|wewew|eweww|ewwew|wewwe|wwewe";              // 白棋活三
    private static final String HUO_SAN_BLACK = "ebbbe|bebeb|ebebb|ebbeb|bebbe|bbebe";              // 黑棋活三
    private static final String CHONG_SAN_WHITE = "bwwwe|bwwew|bweww|ewwwb|wewwb|wwewb";            // 白棋冲三
    private static final String CHONG_SAN_BLACK = "wbbbe|wbbeb|wbebb|ebbbw|bebbw|bbebw";            // 黑棋冲三
    private static final String HUO_SI_WHITE = "ewwwwe";                                            // 白棋活四
    private static final String HUO_SI_BLACK = "ebbbbe";                                            // 黑棋活四
    private static final String CHONG_SI_WHITE = "ewwwwb|wewww|wweww|wwwew|bwwwwe";                 // 白棋冲四
    private static final String CHONG_SI_BLACK = "ebbbb|bebbb|bbebb|bbbeb|bbbbe";                   // 黑棋冲四
    private static final String WU_WHITE = "wwwww";                                                 // 白棋五（胜）
    private static final String WU_BLACK = "bbbbb";                                                 // 黑棋五（胜）
    private static final int WU = 1000000;                                                          // 连成五估值
    private static final int HUO_SI = 300000;                                                       // 活四估值
    private static final int CHONG_SI = 3000;                                                       // 冲四估值
    private static final int HUO_SAN = 5000;                                                        // 活三估值
    private static final int CHONG_SAN = 800;                                                       // 冲三估值
    private static final int HUO_ER = 1000;                                                         // 活二估值
    private static final int CHONG_ER = 100;                                                        // 冲二估值
    private static final int HUO_YI = 300;                                                          // 活一估值
    private static final int CHONG_YI = 50;                                                         // 冲一估值

    /**
     * 估值函数，
     *
     * @param board 当前棋盘状态
     * @param side  从哪一方的视角评估最大值 side = { 'w', 'b' }
     * @return 估值
     * @apiNote 在此函数中 status 不应该被修改！推荐只使用 getValue 方法。 如果必须使用修改后的状态请使用 Board
     * 提供的复制构造方法构造临时量，避免污染输入引用 举例: Board dst = new Board(src);
     */
    public int evaluate(final Board board, char side) {
        // TODO:实现估值函数
        int result = 0;
        for (int i = 0; i < Board.SIZE; ++i) {
            result += evalPointByMain(board, i, i, side);
            result += evalPointByVice(board, i, Board.SIZE - i - 1, side);
        }
        return result; // return a proper value here, not 0
    }

    /**
     * 将待落子点所在的那一行载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getRow(Board chessBoard, int x, int y) {
        chessForm.delete(0, Board.SIZE);
        for (int i = 0; i < Board.SIZE; ++i) {
            chessForm.append(chessBoard.getValue(x, i));
        }
    }

    /**
     * 将待落子点所在的那一列载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getColumn(Board chessBoard, int x, int y) {
        chessForm.delete(0, Board.SIZE);
        for (int i = 0; i < Board.SIZE; ++i) {
            chessForm.append(chessBoard.getValue(i, y));
        }
    }

    /**
     * 将待落子点所在的左上-右下方向上的斜线载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getMainDiag(Board chessBoard, int x, int y) {
        int size = Board.SIZE - Math.abs(x - y);
        if (x > y) {
            for (int i = 0; i < size; ++i) {
                chessForm.append(chessBoard.getValue(x - y + i, i));
            }
        } else {
            for (int i = 0; i < size; ++i) {
                chessForm.append(chessBoard.getValue(i, y - x + i));
            }
        }
    }

    /**
     * 将待落子点所在的左下-右上方向上的斜线载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getViceDiag(Board chessBoard, int x, int y) {
        int size = x + y + 1;
        if (size > Board.SIZE) {
            size = 2 * Board.SIZE - size;
        }
        if (x + y + 1 <= Board.SIZE) {
            for (int i = 0; i < size; ++i) {
                chessForm.append(chessBoard.getValue(size - 1 - i, i));
            }
        } else {
            for (int i = 0; i < size; ++i) {
                chessForm.append(chessBoard.getValue(Board.SIZE - 1 - i, x + y - (Board.SIZE - 1 - i)));
            }
        }
    }

    /**
     * 沿主对角线对每个点扫描，不重不漏地计算棋盘上的所有行和副对角线的估值
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     * @param side       评估方
     * @return 估值
     */
    private int evalPointByMain(Board chessBoard, int x, int y, char side) {
        int result = 0;
        if (side == Board.BLACK) {
            getRow(chessBoard, x, y);// 指向静态公共缓冲区
            result += evalBlack() - evalWhite();
            getViceDiag(chessBoard, x, y);
            result += evalBlack() - evalWhite();
        } else {
            getRow(chessBoard, x, y);
            result += evalWhite() - evalBlack();
            getViceDiag(chessBoard, x, y);
            result += evalWhite() - evalBlack();
        }
        return result;
    }

    /**
     * 沿副对角线对每个点扫描，不重不漏地计算棋盘上的所有列和主对角线的估值
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     * @param side       评估方
     * @return 估值
     */
    private int evalPointByVice(Board chessBoard, int x, int y, char side) {
        int result = 0;
        if (side == Board.BLACK) {
            getColumn(chessBoard, x, y);
            result += evalBlack() - evalWhite();
            getMainDiag(chessBoard, x, y);
            result += evalBlack() - evalWhite();
        } else {
            getColumn(chessBoard, x, y);
            result += evalWhite() - evalBlack();
            getMainDiag(chessBoard, x, y);
            result += evalWhite() - evalBlack();
        }
        return result;
    }

    // 白棋棋型判断与估值

    /**
     * 白棋活一
     *
     * @return 棋型数×估值
     */
    private int huoYiWhite() {
        Pattern pattern = Pattern.compile(HUO_YI_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_YI;
    }

    /**
     * 白棋冲一
     *
     * @return 棋型数×估值
     */
    private int chongYiWhite() {
        Pattern pattern = Pattern.compile(CHONG_YI_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_YI;
    }

    /**
     * 白棋活二
     *
     * @return 棋型数×估值
     */
    private int huoErWhite() {
        Pattern pattern = Pattern.compile(HUO_ER_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_ER;
    }

    /**
     * 白棋冲二
     *
     * @return 棋型数×估值
     */
    private int chongErWhite() {
        Pattern pattern = Pattern.compile(CHONG_ER_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_ER;
    }

    /**
     * 白棋活三
     *
     * @return 棋型数×估值
     */
    private int huoSanWhite() {
        Pattern pattern = Pattern.compile(HUO_SAN_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_SAN;
    }

    /**
     * 白棋冲三
     *
     * @return 棋型数×估值
     */
    private int chongSanWhite() {
        Pattern pattern = Pattern.compile(CHONG_SAN_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_SAN;
    }

    /**
     * 白棋活四
     *
     * @return 棋型数×估值
     */
    private int huoSiWhite() {
        Pattern pattern = Pattern.compile(HUO_SI_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_SI;
    }

    /**
     * 白棋冲四
     *
     * @return 棋型数×估值
     */
    private int chongSiWhite() {
        Pattern pattern = Pattern.compile(CHONG_SI_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_SI;
    }

    /**
     * 白棋五（胜）
     *
     * @return 棋型数×估值
     */
    private int wuWhite() {
        Pattern pattern = Pattern.compile(WU_WHITE);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * WU;
    }

    /**
     * 对一条线上的白棋进行估值
     *
     * @return 白棋估值
     */
    private int evalWhite() {
        return huoYiWhite() + huoErWhite() + huoSanWhite() + huoSiWhite() + chongYiWhite() + chongErWhite()
                + chongSanWhite() + chongSiWhite() + wuWhite();
    }

    // 黑棋棋型判断与估值

    /**
     * 黑棋活一
     *
     * @return 棋型数×估值
     */
    private int huoYiBlack() {
        Pattern pattern = Pattern.compile(HUO_YI_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_YI;
    }

    /**
     * 黑棋冲一
     *
     * @return 棋型数×估值
     */
    private int chongYiBlack() {
        Pattern pattern = Pattern.compile(CHONG_YI_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_YI;
    }

    /**
     * 黑棋活二
     *
     * @return 棋型数×估值
     */
    private int huoErBlack() {
        Pattern pattern = Pattern.compile(HUO_ER_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_ER;
    }

    /**
     * 黑棋冲二
     *
     * @return 棋型数×估值
     */
    private int chongErBlack() {
        Pattern pattern = Pattern.compile(CHONG_ER_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_ER;
    }

    /**
     * 黑棋活三
     *
     * @return 棋型数×估值
     */
    private int huoSanBlack() {
        Pattern pattern = Pattern.compile(HUO_SAN_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_SAN;
    }

    /**
     * 黑棋冲三
     *
     * @return 棋型数×估值
     */
    private int chongSanBlack() {
        Pattern pattern = Pattern.compile(CHONG_SAN_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_SAN;
    }

    /**
     * 黑棋活四
     *
     * @return 棋型数×估值
     */
    private int huoSiBlack() {
        Pattern pattern = Pattern.compile(HUO_SI_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * HUO_SI;
    }

    /**
     * 黑棋冲四
     *
     * @return 棋型数×估值
     */
    private int chongSiBlack() {
        Pattern pattern = Pattern.compile(CHONG_SI_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * CHONG_SI;
    }

    /**
     * 黑棋五（胜）
     *
     * @return 棋型数×估值
     */
    private int wuBlack() {
        Pattern pattern = Pattern.compile(WU_BLACK);
        Matcher matcher = pattern.matcher(chessForm);
        return matcher.groupCount() * WU;
    }

    /**
     * 对一条线上的黑棋进行估值
     *
     * @return 黑棋估值
     */
    private int evalBlack() {
        return huoYiBlack() + huoErBlack() + huoSanBlack() + huoSiBlack() + chongYiBlack() + chongErBlack()
                + chongSanBlack() + chongSiBlack() + wuBlack();
    }
}
