package com.chessboard;

public interface ChessboardListener {
    /**
     * @brief set chess to chessboard, origin is bottom-left
     * @param row coordinate of chess
     * @param col coordinate of chess
     */
    public void selectChessPosition(int row, int col);
//
//    /**
//     * @brief set whole data of chessboard by a new chessboard
//     */
//    public void setChessboard(Chessboard chessboard);
}
