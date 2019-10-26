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

    /* 定义各种棋型的正则表达式和估值 */
    private static final String HUO_YI_WHITE = "eewee";                                     // 白棋活一
    private static final Pattern PATTERN_HUO_YI_WHITE = Pattern.compile(HUO_YI_WHITE);
    private static final String HUO_YI_BLACK = "eebee";                                     // 黑棋活一
    private static final Pattern PATTERN_HUO_YI_BLACK = Pattern.compile(HUO_YI_BLACK);
    private static final String CHONG_YI_WHITE = "bweee";                                   // 白棋冲一
    private static final Pattern PATTERN_CHONG_YI_WHITE = Pattern.compile(CHONG_YI_WHITE);
    private static final String CHONG_YI_BLACK = "wbeee";                                   // 黑棋冲一
    private static final Pattern PATTERN_CHONG_YI_BLACK = Pattern.compile(CHONG_YI_BLACK);
    private static final String HUO_ER_WHITE = "eewwe[^w]?|[^w]?ewwee|ewewe";               // 白棋活二
    private static final Pattern PATTERN_HUO_ER_WHITE = Pattern.compile(HUO_ER_WHITE);
    private static final String HUO_ER_BLACK = "eebbe[^b]?|[^b]?ebbee|ebebe";               // 黑棋活二
    private static final Pattern PATTERN_HUO_ER_BLACK = Pattern.compile(HUO_ER_BLACK);
    private static final String CHONG_ER_WHITE = "bw(wee|ewe)|(eew|ewe)wb";                 // 白棋冲二
    private static final Pattern PATTERN_CHONG_ER_WHITE = Pattern.compile(CHONG_ER_WHITE);
    private static final String CHONG_ER_BLACK = "wb(be|eb)e|e(eb|be)bw";                   // 黑棋冲二
    private static final Pattern PATTERN_CHONG_ER_BLACK = Pattern.compile(CHONG_ER_BLACK);
    private static final String HUO_SAN_WHITE = "e(ww(we|ew)|(ew|we)ww)e";                  // 白棋活三
    private static final Pattern PATTERN_HUO_SAN_WHITE = Pattern.compile(HUO_SAN_WHITE);
    private static final String HUO_SAN_BLACK = "e(bb(be|eb)|(eb|be)bb)e";                  // 黑棋活三
    public static final Pattern PATTERN_HUO_SAN_BLACK = Pattern.compile(HUO_SAN_BLACK);
    private static final String CHONG_SAN_WHITE = "bw(wwe|wew|eww)e|e(eww|wew|wwe)wb|wewew";// 白棋冲三
    private static final Pattern PATTERN_CHONG_SAN_WHITE = Pattern.compile(CHONG_SAN_WHITE);
    private static final String CHONG_SAN_BLACK = "wb(bbe|beb|ebb)e|e(ebb|beb|bbe)bw|bebeb";// 黑棋冲三
    private static final Pattern PATTERN_CHONG_SAN_BLACK = Pattern.compile(CHONG_SAN_BLACK);
    private static final String HUO_SI_WHITE = "ewwwwe";                                    // 白棋活四
    private static final Pattern PATTERN_HUO_SI_WHITE = Pattern.compile(HUO_SI_WHITE);
    private static final String HUO_SI_BLACK = "ebbbbe";                                    // 黑棋活四
    public static final Pattern PATTERN_HUO_SI_BLACK = Pattern.compile(HUO_SI_BLACK);
    private static final String CHONG_SI_WHITE = "ewwwwb|w(eww|wew|wwe)w|bwwwwe";           // 白棋冲四
    private static final Pattern PATTERN_CHONG_SI_WHITE = Pattern.compile(CHONG_SI_WHITE);
    private static final String CHONG_SI_BLACK = "ebbbbw|b(ebb|beb|bbe)b|wbbbbe";           // 黑棋冲四
    public static final Pattern PATTERN_CHONG_SI_BLACK = Pattern.compile(CHONG_SI_BLACK);
    private static final String WU_WHITE = "wwwww";                                         // 白棋五（胜）
    private static final Pattern PATTERN_WU_WHITE = Pattern.compile(WU_WHITE);
    private static final String WU_BLACK = "bbbbb";                                         // 黑棋五（胜）
    private static final Pattern PATTERN_WU_BLACK = Pattern.compile(WU_BLACK);
    private static final int WU = 10000;                                                    // 连成五估值
    private static final int HUO_SI = 5000;                                                 // 活四估值
    private static final int CHONG_SI = 1000;                                               // 冲四估值
    private static final int HUO_SAN = 2500;                                                // 活三估值
    private static final int CHONG_SAN = 100;                                               // 冲三估值
    private static final int HUO_ER = 1000;                                                 // 活二估值
    private static final int CHONG_ER = 50;                                                 // 冲二估值
    private static final int HUO_YI = 200;                                                  // 活一估值
    private static final int CHONG_YI = 5;                                                  // 冲一估值
    /* 算法会创建许多临时String对象，使用公共缓冲区提高性能 */
    private StringBuffer chessForm;
//    private StringBuilder curRow;//临时存储当前点所在行
//    private StringBuilder curColumn;//临时存储当前点所在列
//    private StringBuilder curMainDiag;//临时存储当前点所在左上-右下方向的串
//    private StringBuilder curViceDiag;//临时存储当前点所在左下-右上方向的串

    public Evaluator() {
        chessForm = new StringBuffer(Board.SIZE);
//        curRow = new StringBuilder(Board.SIZE);
//        curColumn = new StringBuilder(Board.SIZE);
//        curMainDiag = new StringBuilder(Board.SIZE);
//        curViceDiag = new StringBuilder(Board.SIZE);
    }


    /**
     * 估计函数，给定一个点，估计这个点的分数， 如果它更有可能成三、四之类的棋则返回的分数更高，
     * 这个方法用于评估在某个点落子是否更有可能获得更高的估值，这将用于启发式搜索函数，且其实现应当是简单而快速的
     *
     * @param row   待估计点的坐标
     * @param col   待估计点的坐标
     * @param board 当前棋盘状态
     * @param side  从哪一方的视角评估最大值 side = { 'w', 'b' }
     * @return
     */
    public int fastEstimateOneStone(final Board board, int row, int col, char side) {
        int result = 0;
        Board chessBoard = new Board(board);
        if (side == Board.BLACK) {
            chessBoard.setValue(row, col, Board.BLACK);
            getRow(chessBoard, row, col);
            result += huoErBlack() - 8 * huoErWhite() + huoSanBlack() - 8 * huoSanWhite() + huoSiBlack() - 8 * huoSiWhite() + chongSiBlack() - 8 * chongSiWhite() + wuBlack();
            getColumn(chessBoard, row, col);
            result += huoErBlack() - 8 * huoErWhite() + huoSanBlack() - 8 * huoSanWhite() + huoSiBlack() - 8 * huoSiWhite() + chongSiBlack() - 8 * chongSiWhite() + wuBlack();
            getMainDiag(chessBoard, row, col);
            result += huoErBlack() - 8 * huoErWhite() + huoSanBlack() - 8 * huoSanWhite() + huoSiBlack() - 8 * huoSiWhite() + chongSiBlack() - 8 * chongSiWhite() + wuBlack();
            getViceDiag(chessBoard, row, col);
            result += huoErBlack() - 8 * huoErWhite() + huoSanBlack() - 8 * huoSanWhite() + huoSiBlack() - 8 * huoSiWhite() + chongSiBlack() - 8 * chongSiWhite() + wuBlack();
        } else {
            chessBoard.setValue(row, col, Board.WHITE);
            getRow(chessBoard, row, col);
            result += huoErWhite() - 8 * huoErBlack() + huoSanWhite() - 8 * huoSanBlack() + huoSiWhite() - 8 * huoSiBlack() + chongSiWhite() - 8 * chongSiBlack() + wuWhite();
            getColumn(chessBoard, row, col);
            result += huoErWhite() - 8 * huoErBlack() + huoSanWhite() - 8 * huoSanBlack() + huoSiWhite() - 8 * huoSiBlack() + chongSiWhite() - 8 * chongSiBlack() + wuWhite();
            getMainDiag(chessBoard, row, col);
            result += huoErWhite() - 8 * huoErBlack() + huoSanWhite() - 8 * huoSanBlack() + huoSiWhite() - 8 * huoSiBlack() + chongSiWhite() - 8 * chongSiBlack() + wuWhite();
            getViceDiag(chessBoard, row, col);
            result += huoErWhite() - 8 * huoErBlack() + huoSanWhite() - 8 * huoSanBlack() + huoSiWhite() - 8 * huoSiBlack() + chongSiWhite() - 8 * chongSiBlack() + wuWhite();
        }
        return result;     //实现后替换这个0
    }

    /**
     * 估计与这某个子有关的位置对整个棋局的影响，横、竖、主对角线、副对角线各核算一遍，合计后返回，这个方法应当尽量快
     * 这个方法是用于动态更新，减少计算量的。
     *
     * @param board 当前棋盘状态
     * @param row   待估计点的坐标
     * @param col   待估计点的坐标
     * @param side  从哪一方的视角评估最大值 side = { 'w', 'b' }
     * @return
     */
    public int evaluateOneStone(final Board board, int row, int col, char side) {
        int result = 0;
        Board chessBoard = new Board(board);
        if (side == Board.BLACK) {
            chessBoard.setValue(row, col, Board.BLACK);
            getRow(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
            getColumn(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
            getMainDiag(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
            getViceDiag(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
        } else {
            chessBoard.setValue(row, col, Board.WHITE);
            getRow(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
            getColumn(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
            getMainDiag(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
            getViceDiag(chessBoard, row, col);
            result += evalBlack() - 8 * evalWhite();
        }
        return result;     //实现后替换这个0
    }

    /**
     * 估值函数
     *
     * @param board 当前棋盘状态
     * @param side  从哪一方的视角评估最大值 side = { 'w', 'b' }
     * @return 估值
     * @apiNote 在此函数中 status 不应该被修改！推荐只使用 getValue 方法。 如果必须使用修改后的状态请使用 Board
     * 提供的复制构造方法构造临时量，避免污染输入引用 举例: Board dst = new Board(src);
     */
    public int evaluate(final Board board, char side) {
        // TODO:实现估值函数
//        counter++;
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
        chessForm.delete(0, chessForm.length());
        chessForm.append(chessBoard.getValueRow(x));
    }

    /**
     * 将待落子点所在的那一列载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getColumn(Board chessBoard, int x, int y) {
        chessForm.delete(0, chessForm.length());
        chessForm.append(chessBoard.getValueColumn(y));
    }

    /**
     * 将待落子点所在的左上-右下方向上的斜线载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getMainDiag(Board chessBoard, int x, int y) {
        chessForm.delete(0, chessForm.length());
        // int size = Board.SIZE - Math.abs(x - y);
        // if (x > y) {
        // for (int i = 0; i < size; ++i) {
        // chessForm.append(chessBoard.getValue(x - y + i, i));
        // }
        // } else {
        // for (int i = 0; i < size; ++i) {
        // chessForm.append(chessBoard.getValue(i, y - x + i));
        // }
        // }
        chessForm.append(chessBoard.getMainDiag(x, y));
    }

    /**
     * 将待落子点所在的左下-右上方向上的斜线载入公共缓冲区
     *
     * @param chessBoard 棋盘
     * @param x          当前遍历点的横坐标
     * @param y          当前遍历点的纵坐标
     */
    private void getViceDiag(Board chessBoard, int x, int y) {
        chessForm.delete(0, chessForm.length());
        // int size = x + y + 1;
        // if (size > Board.SIZE) {
        // size = Board.SIZE - size;
        // }
        // if (x + y + 1 <= Board.SIZE) {
        // for (int i = 0; i < size; ++i) {
        // chessForm.append(chessBoard.getValue(size - 1 - i, i));
        // }
        // } else {
        // for (int i = 0; i < size; ++i) {
        // chessForm.append(chessBoard.getValue(Board.SIZE - 1 - i, x + y - (Board.SIZE
        // - 1 - i)));
        // }
        // }
        chessForm.append(chessBoard.getViceDiag(x, y));
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
            result += evalBlack() - 8 * evalWhite();
            getViceDiag(chessBoard, x, y);
            result += evalBlack() - 8 * evalWhite();
        } else {
            getRow(chessBoard, x, y);
            result += evalWhite() - 8 * evalBlack();
            getViceDiag(chessBoard, x, y);
            result += evalWhite() - 8 * evalBlack();
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
            result += evalBlack() - 8 * evalWhite();
            getMainDiag(chessBoard, x, y);
            result += evalBlack() - 8 * evalWhite();
        } else {
            getColumn(chessBoard, x, y);
            result += evalWhite() - 8 * evalBlack();
            getMainDiag(chessBoard, x, y);
            result += evalWhite() - 8 * evalBlack();
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
        Matcher matcher = PATTERN_HUO_YI_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_YI;
    }

    /**
     * 白棋冲一
     *
     * @return 棋型数×估值
     */
    private int chongYiWhite() {
        Matcher matcher = PATTERN_CHONG_YI_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_YI;
    }

    /**
     * 白棋活二
     *
     * @return 棋型数×估值
     */
    private int huoErWhite() {
        Matcher matcher = PATTERN_HUO_ER_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_ER;
    }

    /**
     * 白棋冲二
     *
     * @return 棋型数×估值
     */
    private int chongErWhite() {
        Matcher matcher = PATTERN_CHONG_ER_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_ER;
    }

    /**
     * 白棋活三
     *
     * @return 棋型数×估值
     */
    private int huoSanWhite() {
        Matcher matcher = PATTERN_HUO_SAN_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_SAN;
    }

    /**
     * 白棋冲三
     *
     * @return 棋型数×估值
     */
    private int chongSanWhite() {
        Matcher matcher = PATTERN_CHONG_SAN_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_SAN;
    }

    /**
     * 白棋活四
     *
     * @return 棋型数×估值
     */
    private int huoSiWhite() {
        Matcher matcher = PATTERN_HUO_SI_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_SI;
    }

    /**
     * 白棋冲四
     *
     * @return 棋型数×估值
     */
    private int chongSiWhite() {
        Matcher matcher = PATTERN_CHONG_SI_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_SI;
    }

    /**
     * 白棋五（胜）
     *
     * @return 棋型数×估值
     */
    private int wuWhite() {
        Matcher matcher = PATTERN_WU_WHITE.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * WU;
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
        Matcher matcher = PATTERN_HUO_YI_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_YI;
    }

    /**
     * 黑棋冲一
     *
     * @return 棋型数×估值
     */
    private int chongYiBlack() {
        Matcher matcher = PATTERN_CHONG_YI_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_YI;
    }

    /**
     * 黑棋活二
     *
     * @return 棋型数×估值
     */
    private int huoErBlack() {
        Matcher matcher = PATTERN_HUO_ER_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_ER;
    }

    /**
     * 黑棋冲二
     *
     * @return 棋型数×估值
     */
    private int chongErBlack() {
        Matcher matcher = PATTERN_CHONG_ER_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_ER;
    }

    /**
     * 黑棋活三
     *
     * @return 棋型数×估值
     */
    private int huoSanBlack() {
        Matcher matcher = PATTERN_HUO_SAN_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_SAN;
    }

    /**
     * 黑棋冲三
     *
     * @return 棋型数×估值
     */
    private int chongSanBlack() {
        Matcher matcher = PATTERN_CHONG_SAN_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_SAN;
    }

    /**
     * 黑棋活四
     *
     * @return 棋型数×估值
     */
    private int huoSiBlack() {
        Matcher matcher = PATTERN_HUO_SI_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * HUO_SI;
    }

    /**
     * 黑棋冲四
     *
     * @return 棋型数×估值
     */
    private int chongSiBlack() {
        Matcher matcher = PATTERN_CHONG_SI_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * CHONG_SI;
    }

    /**
     * 黑棋五（胜）
     *
     * @return 棋型数×估值
     */
    private int wuBlack() {
        Matcher matcher = PATTERN_WU_BLACK.matcher(chessForm);
        int count = 0;
        while (matcher.find()) {
            ++count;
        }
        return count * WU;
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
