package com.chessboard;

public class Chessboard {
    private ChessValue[][] chessValueArray = null;    //棋盘布局
    private int[][] orderArray = null;        //落子顺序编号, 从 1 开始编号，0 表示无效
    private int currNum = 0;               //当前最后一次落子的顺序编号
    private int size=0;                     //棋盘边长

    /**
     * @brief constructor of Chessboard
     * @param size
     */
    public Chessboard(int size){
        this.size = size;
        chessValueArray = new ChessValue[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                chessValueArray[i][j] = ChessValue.EMPTY;
        orderArray = new int[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                orderArray[i][j] = 0;
    }


    /**
     * @brief set chessValue by coordinate
     *
     * origin's coordinate is (x=0,y=0) and located at left-bottom side of the chessboard
     */
    public void setChess(int x, int y, ChessValue chessValue){
        chessValueArray[x][y]= chessValue;
        if (chessValue == ChessValue.BLACK || chessValue == ChessValue.WHITE) currNum++;
        orderArray[x][y]=currNum;
    }


    /**
     * @brief get board information by coordinate
     */
    public ChessValue getChess(int x, int y){
        return chessValueArray[x][y];
    }


    /**
     * @brief get the size of chessboard
     */
    public int getSize(){ return size; }


    /**
     * @brief get the whole chessboard data through array
     */
    public int getChessOrder(int x, int y) { return orderArray[x][y]; }


    /**
     * @return 当前最后一次落子的顺序编号
     */
    public int getCurrNum() { return currNum;}

    public ChessValue[][] getChessboardArray() {
        ChessValue[][] tmp = new ChessValue[this.size][this.size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                tmp[i][j] = chessValueArray[i][j];
        return tmp;
    }

    /**
     * @brief set the whole chessboard data
     */
    public void setChessboardArray(ChessValue[][] chessboardArray){
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                this.chessValueArray[i][j] = chessboardArray[i][j];
    }
}
