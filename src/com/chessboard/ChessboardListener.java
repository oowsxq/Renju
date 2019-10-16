package com.chessboard;

public interface ChessboardListener {
    /**
     * @brief set chess to chessboard
     * @param x x coordinate of chess
     * @param y y coordinate of chess
     * origin locate in the top-left side of the chessboard
     */
    public void selectChessPosition(int x, int y);
//
//    /**
//     * @brief set whole data of chessboard by a new chessboard
//     */
//    public void setChessboard(Chessboard chessboard);
}
