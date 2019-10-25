package com.engine.fourtransEngine;


/**
 * 此类用于判断终局相关信息，全 static 设计
 * 功能：判断禁手、判断胜利
 */
public class Judgementor {
    /**
     * 三个全局禁手开关，用于表示是否禁手，在引擎创建游戏时初始化禁手标记
     */
    private static boolean forbidDoubleThree;
    private static boolean forbidDoubleFour;
    private static boolean forbidOverline;

    /**
     * 判断游戏是否结束
     *
     * @param board 当前局面的数组表示，原点左上角
     * @return null表示非终局，'b'表示黑方五连，'w'表示白方长连，'f'表示黑方禁手白方胜
     */
    public static Character adjustGameOver(final Board board) {
        boolean isend = false;
        Character result = null;

        /* 判断白方长连 */
        if (!isend) {
            //TODO:判断白方长连
            for (int i = 0; i < Board.SIZE; ++i) {
                char[] strViceDiag = board.getViceDiag(i, i);
                char[] strColumn = board.getValueColumn(i);
                char[] strMainDiag = board.getMainDiag(i, Board.SIZE - 1 - i);
                char[] strRow = board.getValueRow(i);
                int cntRow = 0;
                int cntColumn = 0;
                int cntMainDiag = 0;
                int cntViceDiag = 0;
                for (int j = 0; j < Board.SIZE; ++j) {
                    if (j < strViceDiag.length) {
                        if (strViceDiag[j] == Board.WHITE) {
                            ++cntViceDiag;
                            if (cntViceDiag >= 5) {
                                result = Board.WHITE;
                                isend = true;
                                break;
                            }
                        } else {
                            cntViceDiag = 0;
                        }
                    }
                    if (j < strColumn.length) {
                        if (strColumn[j] == Board.WHITE) {
                            ++cntColumn;
                            if (cntColumn >= 5) {
                                result = Board.WHITE;
                                isend = true;
                                break;
                            }
                        } else {
                            cntColumn = 0;
                        }
                    }
                    if (j < strMainDiag.length) {
                        if (strMainDiag[j] == Board.WHITE) {
                            ++cntMainDiag;
                            if (cntMainDiag >= 5) {
                                result = Board.WHITE;
                                isend = true;
                                break;
                            }
                        } else {
                            cntMainDiag = 0;
                        }
                    }
                    if (j < strRow.length) {
                        if (strRow[j] == Board.WHITE) {
                            ++cntRow;
                            if (cntRow >= 5) {
                                result = Board.WHITE;
                                isend = true;
                                break;
                            }
                        } else {
                            cntRow = 0;
                        }
                    }
                }
                if (isend) {
                    break;
                }
            }
            //如果有白方长连 result = 'w'; isend = true;
        }

        /* 判断黑方五连 */
        if (!isend) {
            //TODO:判断黑方五连
            for (int i = 0; i < Board.SIZE; ++i) {
                char[] strViceDiag = board.getViceDiag(i, i);
                char[] strColumn = board.getValueColumn(i);
                char[] strMainDiag = board.getMainDiag(i, Board.SIZE - 1 - i);
                char[] strRow = board.getValueRow(i);
                int cntRow = 0;
                int cntColumn = 0;
                int cntMainDiag = 0;
                int cntViceDiag = 0;
                for (int j = 0; j < Board.SIZE; ++j) {
                    if (j < strViceDiag.length) {
                        if (strViceDiag[j] == Board.WHITE) {
                            ++cntViceDiag;
                        } else {
                            cntViceDiag = 0;
                        }
                    }
                    if (j < strColumn.length) {
                        if (strColumn[j] == Board.WHITE) {
                            ++cntColumn;
                        } else {
                            cntColumn = 0;
                        }
                    }
                    if (j < strMainDiag.length) {
                        if (strMainDiag[j] == Board.WHITE) {
                            ++cntMainDiag;
                        } else {
                            cntMainDiag = 0;
                        }
                    }
                    if (j < strRow.length) {
                        if (strRow[j] == Board.WHITE) {
                            ++cntRow;
                        } else {
                            cntRow = 0;
                        }
                    }
                }
                if (cntRow == 5 || cntColumn == 5 || cntMainDiag == 5 || cntViceDiag == 5) {
                    result = Board.BLACK;
                    isend = true;
                    break;
                }

            }
            //如果有黑方五连 result = 'b'; isend = true;
        }

        /* 判断黑方禁手 */
        if (!isend && hasForbiddenStone(board, null)) {
            result = Board.FORBID;
            isend = true;
        }

        return result;
    }

    /**
     * 判断是否存在禁手
     *
     * @param board  需要判断的局面
     * @param result 如果不为 null, 则复制一份 board 并向其中写入带禁手点的位置，用'f'表示
     * @return true 存在禁手; false 不存在禁手点
     * 举例：
     * hasForbiddenStone (board, null)     //只关心是否存在禁手点，不关心在哪
     * hasForbiddenStone (board, result)   //不仅要知道结果，还要知道禁手点在哪，此函数将禁手点返回到 result 的棋盘里
     * result 棋盘 和输入棋盘一致，但在禁手的位置用 'f' 代替 'e'
     */
    public static boolean hasForbiddenStone(final Board board, Board result) {
        //TODO：判断禁手及禁手位置
        if (result != null) {
            result = new Board(board);
        }
        return false;
    }

    /**
     * 设定终局判断及其的判断规则
     *
     * @param forbidDoubleThree
     * @param forbidDoubleFour
     * @param forbidOverline
     */
    public static void setRule(boolean forbidDoubleThree, boolean forbidDoubleFour, boolean forbidOverline) {
        Judgementor.forbidDoubleThree = forbidDoubleThree;
        Judgementor.forbidDoubleFour = forbidDoubleFour;
        Judgementor.forbidOverline = forbidOverline;
    }
}
