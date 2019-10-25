package com.chessboard;

import java.util.Arrays;

/*
    Y
    ^
    |
    |
    |
    |------(row,col)
    |       |
    +--------------------> X
    原点左下角，坐标 row 优先 col 其次
 */
public class Chessboard {
    private ChessValue[][] chessValueArray = null;    //棋盘布局
    private int[][] orderArray = null;        //落子顺序编号, 从 1 开始编号，0 表示无效
    private int currNum = 0;                //当前最后一次落子的顺序编号
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

    public Chessboard(Chessboard src){
        this.size = src.size;
        this.currNum = src.currNum;
        this.chessValueArray = new ChessValue[size][size];
        this.orderArray = new int[size][size];

        for (int i = 0; i < size; i++){
            System.arraycopy(src.chessValueArray[i],0,this.chessValueArray[i],0,size);
            System.arraycopy(src.orderArray[i],0,this.orderArray[i],0,size);
        }
    }


    /**
     * 设置棋子
     * 原点 (0,0) 在左下角
     */
    public void setChessValue(int x, int y, ChessValue chessValue, int order){
        chessValueArray[x][y]= chessValue;
        if (chessValue == ChessValue.BLACK || chessValue == ChessValue.WHITE) {
            currNum = order;
            orderArray[x][y] = currNum;
        } else {
            orderArray[x][y] = 0;
        }
    }

    /**
     * 获取棋子值
     */
    public ChessValue getChessValue(int x, int y){
        return chessValueArray[x][y];
    }


    /**
     * 获取棋盘大小
     */
    public int getBoardSize(){ return size; }


    /**
     * 获取棋盘某个点的落子编号
     */
    public int getChessOrder(int x, int y) {
        return orderArray[x][y];
    }


    /**
     * @return 当前最后一次落子的顺序编号
     */
    public int getCurrNum() { return currNum;}

    /*
    一系列用于 getChessValueArray 的常数
     */
    public static final int CHESS_VALUE_AS_ENUM = 1;
    public static final int CHESS_VALUE_AS_CHAR = 2;

    /**
     * 获取整个棋盘的落子值
     * @return 一个复制构造的二维矩阵
     */
    public ChessValue[][] getChessValueArray() {
        ChessValue[][] result = new ChessValue[this.size][this.size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                result[i][j] = chessValueArray[i][j];
        return result;
    }

    /**
     * 以一个方便引擎计算的格式返回落子值的矩阵。
     * 在本 chessboard 类中使用的原点在左下角，通过垂直翻转选项可以切换为左上角，方便了引擎编写
     * 但是需要注意如果使用本方法返回的数据用来引擎计算，在返回结果的时候需要合理的映射
     * @param flip_vertical 是否垂直翻转
     * @return 以字符形二维数组返回落子数据 'b'->黑子 'w'->白子 'e'->空
     */
    public char[][] getEngineFriendlyArray(boolean flip_vertical) {
        char[][] result = new char[this.size][this.size];
        char tmp = 'e';
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                switch (chessValueArray[i][j]) {
                    case BLACK:
                        tmp = 'b';
                        break;
                    case WHITE:
                        tmp = 'w';
                        break;
                    case EMPTY:
                        tmp = 'e';
                        break;
                }
                if (flip_vertical)
                    result[size - i - 1][j] = tmp;
                else
                    result[i][j] = tmp;
            }
        }
        return result;
    }

    /**
     * 通过二维数组设定整个棋盘的值
     */
    public void setChessValueArray(ChessValue[][] chessboardArray){
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                this.chessValueArray[i][j] = chessboardArray[i][j];
    }

    /**
     * 工具方法，将棋盘的棋子落点数组改为对引擎处理友好的字符数组
     *      ChessValue.BLACK -> 'b'
     *      ChessValue.WHITE -> 'w'
     *      ChessValue.EMPTY -> 'e'
     *
     * @return 原点在左上角，行优先，的二维字符数组
     */
    /*
    坐标系也改变：
    O--------------------> col
    |       |
    |------(row,col) row first
    |
    |
    V
    row
     */
    public char[][] trans2EngineFriendlyCharArray(){
        int size = chessValueArray.length;
        char[][] results = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                switch (chessValueArray[i][j]){
                    case BLACK: results[size - 1 - j][i] = 'b'; break;
                    case WHITE: results[size - 1 - j][i] = 'w'; break;
                    default: results[size - 1 - j][i] = 'e'; break;
                }
            }
        }
//        System.out.println(Arrays.deepToString(results)); //debug
        return results;
    }

    /**
     * 按照引擎友好的坐标系，原点左上角，行优先的方式获取棋子值
     */
    public ChessValue getChessValueEngineFriendly(int row, int col){
        return chessValueArray[col][size - 1 - row];
    }

    /**
     * 按照引擎友好的坐标系，原点左上角，行优先的方式获取棋子编号
     */
    public int getChessOrderEngineFriendly(int row, int col){
        return orderArray[col][size - 1 - row];
    }
}
