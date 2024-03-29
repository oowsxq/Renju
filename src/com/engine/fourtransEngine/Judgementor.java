package com.engine.fourtransEngine;


import java.util.regex.Matcher;

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
        boolean flag = false;
        if (result != null) {
            result = new Board(board);
            if (forbidDoubleThree) {
                flag = flag || doubleThreeForbiddenStone(result);
            }
            if (forbidDoubleFour) {
                flag = flag || doubleFourForbiddenStone(result);
            }
            if (forbidOverline) {
                flag = flag || overlineForbiddenStone(result);
            }
        }
        return flag;
    }

    /**
     * 检查某一个点在当前棋局中是否是禁手点，不会污染输入board
     *
     * @param row
     * @param col
     * @return false 非禁手， true 是禁手
     */
    public static boolean checkOneStoneIsForbidden(final Board board, int row, int col) {
        Board tmp = new Board(board);
        boolean flag = false;
        if (forbidDoubleThree && !flag) {
            flag = isOneStoneForbiddenDoubleThree(tmp, row, col);
        }
        if (forbidDoubleFour && !flag) {
            flag = isOneStoneForbiddenDoubleFour(tmp, row, col);
        }
        if (forbidOverline && !flag) {
            flag = isOneStoneForbiddenOverline(tmp, row, col);
        }
        return flag;
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

    /**
     * 判断是否有三三禁手
     *
     * @param chessBoard
     * @return
     */
    private static boolean doubleThreeForbiddenStone(Board chessBoard) {
        boolean flag = false;
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (chessBoard.getValue(i, j) == Board.EMPYT) {
                    //下一颗黑子
                    chessBoard.setValue(i, j, Board.BLACK);
                    //判断是否有两个以上的活三
                    int count = 0;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(chessBoard.getValueRow(i));
                    Matcher matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(chessBoard.getValueColumn(j));
                    matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(chessBoard.getMainDiag(i, j));
                    matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(chessBoard.getViceDiag(i, j));
                    matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    if (count >= 2) {
                        chessBoard.setValue(i, j, Board.FORBID);
                        flag = true;
                    } else {
                        chessBoard.setValue(i, j, Board.EMPYT);
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 判断是否有四四禁手
     *
     * @param chessBoard
     * @return
     */
    private static boolean doubleFourForbiddenStone(Board chessBoard) {
        boolean flag = false;
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (chessBoard.getValue(i, j) == Board.EMPYT) {
                    //下一颗黑子
                    chessBoard.setValue(i, j, Board.BLACK);
                    //判断是否有两个以上的冲四
                    int count = 0;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(chessBoard.getValueRow(i));
                    Matcher matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(chessBoard.getValueColumn(j));
                    matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(chessBoard.getMainDiag(i, j));
                    matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(chessBoard.getViceDiag(i, j));
                    matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
                    if (matcher.find()) {
                        ++count;
                    }
                    if (count >= 2) {
                        chessBoard.setValue(i, j, Board.FORBID);
                        flag = true;
                    } else {
                        chessBoard.setValue(i, j, Board.EMPYT);
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 判断是否有长连禁手
     *
     * @param chessBoard
     * @return
     */
    private static boolean overlineForbiddenStone(Board chessBoard) {
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j) {
                if (chessBoard.getValue(i, j) == Board.EMPYT) {
                    chessBoard.setValue(i, j, Board.BLACK);
                    char[] strViceDiag = chessBoard.getViceDiag(i, j);
                    char[] strColumn = chessBoard.getValueColumn(j);
                    char[] strMainDiag = chessBoard.getMainDiag(i, j);
                    char[] strRow = chessBoard.getValueRow(i);
                    int cntRow = 0;
                    int cntColumn = 0;
                    int cntMainDiag = 0;
                    int cntViceDiag = 0;
                    for (int k = 0; k < strRow.length; ++k) {
                        if (strRow[k] == Board.BLACK) {
                            ++cntRow;
                        } else {
                            cntRow = 0;
                        }
                    }
                    if (cntRow > 5) {
                        return true;
                    }
                    for (int k = 0; k < strColumn.length; ++k) {
                        if (strColumn[k] == Board.BLACK) {
                            ++cntColumn;
                        } else {
                            cntColumn = 0;
                        }
                    }
                    if (cntColumn > 5) {
                        return true;
                    }
                    for (int k = 0; k < strMainDiag.length; ++k) {
                        if (strMainDiag[k] == Board.BLACK) {
                            ++cntMainDiag;
                        } else {
                            cntMainDiag = 0;
                        }
                    }
                    if (cntMainDiag > 5) {
                        return true;
                    }
                    for (int k = 0; k < strViceDiag.length; ++k) {
                        if (strViceDiag[k] == Board.BLACK) {
                            ++cntViceDiag;
                        } else {
                            cntViceDiag = 0;
                        }
                    }
                    if (cntViceDiag > 5) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 判断是否有三三禁手
     *
     * @param chessBoard
     * @return
     */
    private static boolean isOneStoneForbiddenDoubleThree(Board chessBoard, int row, int col) {
        boolean flag = false;
        if (chessBoard.getValue(row, col) == Board.EMPYT) {
            //下一颗黑子
            chessBoard.setValue(row, col, Board.BLACK);
            //判断是否有两个以上的活三
            int count = 0;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(chessBoard.getValueRow(row));
            Matcher matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(chessBoard.getValueColumn(col));
            matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(chessBoard.getMainDiag(row, col));
            matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(chessBoard.getViceDiag(row, col));
            matcher = Evaluator.PATTERN_HUO_SAN_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            if (count >= 2) {
                chessBoard.setValue(row, col, Board.FORBID);
                flag = true;
            } else {
                chessBoard.setValue(row, col, Board.EMPYT);
            }

        }
        return flag;
    }

    /**
     * 判断是否有四四禁手
     *
     * @param chessBoard
     * @return
     */
    private static boolean isOneStoneForbiddenDoubleFour(Board chessBoard, int row, int col) {
        boolean flag = false;

        if (chessBoard.getValue(row, col) == Board.EMPYT) {
            //下一颗黑子
            chessBoard.setValue(row, col, Board.BLACK);
            //判断是否有两个以上的冲四
            int count = 0;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(chessBoard.getValueRow(row));
            Matcher matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(chessBoard.getValueColumn(col));
            matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(chessBoard.getMainDiag(row, col));
            matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(chessBoard.getViceDiag(row, col));
            matcher = Evaluator.PATTERN_CHONG_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            matcher = Evaluator.PATTERN_HUO_SI_BLACK.matcher(stringBuilder);
            if (matcher.find()) {
                ++count;
            }
            if (count >= 2) {
                chessBoard.setValue(row, col, Board.FORBID);
                flag = true;
            } else {
                chessBoard.setValue(row, col, Board.EMPYT);
            }

        }
        return flag;
    }

    /**
     * 判断是否有长连禁手
     *
     * @param chessBoard
     * @return
     */
    private static boolean isOneStoneForbiddenOverline(Board chessBoard, int row, int col) {
        if (chessBoard.getValue(row, col) == Board.EMPYT) {
            chessBoard.setValue(row, col, Board.BLACK);
            char[] strViceDiag = chessBoard.getViceDiag(row, col);
            char[] strColumn = chessBoard.getValueColumn(col);
            char[] strMainDiag = chessBoard.getMainDiag(row, col);
            char[] strRow = chessBoard.getValueRow(row);
            int cntRow = 0;
            int cntColumn = 0;
            int cntMainDiag = 0;
            int cntViceDiag = 0;
            for (int k = 0; k < strRow.length; ++k) {
                if (strRow[k] == Board.BLACK) {
                    ++cntRow;
                } else {
                    cntRow = 0;
                }
            }
            if (cntRow > 5) {
                return true;
            }
            for (int k = 0; k < strColumn.length; ++k) {
                if (strColumn[k] == Board.BLACK) {
                    ++cntColumn;
                } else {
                    cntColumn = 0;
                }
            }
            if (cntColumn > 5) {
                return true;
            }
            for (int k = 0; k < strMainDiag.length; ++k) {
                if (strMainDiag[k] == Board.BLACK) {
                    ++cntMainDiag;
                } else {
                    cntMainDiag = 0;
                }
            }
            if (cntMainDiag > 5) {
                return true;
            }
            for (int k = 0; k < strViceDiag.length; ++k) {
                if (strViceDiag[k] == Board.BLACK) {
                    ++cntViceDiag;
                } else {
                    cntViceDiag = 0;
                }
            }
            if (cntViceDiag > 5) {
                return true;
            }

        }
        return false;
    }
}

