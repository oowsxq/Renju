package com.engine.fourtransEngine;

/**
 * 用于表示搜索节点的数据结构
 * board是用于表示棋盘状态的字符数组，每个元素具有四种可能 { 'b','w','e','f' }
 *      'b' 黑子
 *      ‘w' 白字
 *      'e' 空点
 *      'f' 黑方禁手点
 */
class Board {
    public static final int SIZE        = 15;  //五子棋边长 15
    public static final char EMPYT      = 'e';
    public static final char BLACK      = 'b';
    public static final char WHITE      = 'w';
    public static final char FORBID     = 'f';

    private char[] data = new char[SIZE * SIZE];

    Board(final Board src){
        System.arraycopy(src.data, 0, this.data, 0, SIZE*SIZE);
    }

    Board(final char[][] src){
        for (int i = 0; i < SIZE; i++){
            System.arraycopy(src[i], 0, this.data, i * SIZE, SIZE);
        }
    }

    public final char getValue(int row, int col){
        return data[row * SIZE + col];
    }

    public final void setValue(int row, int col, char c){
        data[row * SIZE  + col] = c;
    }

    public final char[] getValueRow(int row) {
        char[] tmp = new char[SIZE];
        System.arraycopy(data, row * SIZE, tmp, 0, SIZE);
        return tmp;
    }

    public final char[] getValueColumn(int col){
        char[] tmp = new char[SIZE];
        for (int i = 0; i < SIZE; i++)
            tmp[i] = data[i * SIZE + col];
        return tmp;
    }

    /**
     * 获取样本点所在主对角线元素，顺序从左到右
     * @param row
     * @param col
     * @return
     */
    public final char[] getMainDiag(int row, int col){
        //将输入点归一到顶侧的边界点
        int size = Board.SIZE - Math.abs(row - col);
        if (row >= col){
            row = row - col;
            col = 0;
        } else {
            col = col - row;
            row = 0;
        }
        char[] tmp = new char[size];

        int base = col * Board.SIZE + row;
        for (int pos = 0; pos < size; pos++){
            tmp[pos] = data[base + (Board.SIZE + 1) * pos];
        }

        return tmp;
    }

    /**
     * 获取样本点所在副对角线元素，顺序从左到右
     * @param row
     * @param col
     * @return
     */
    public final char[] getViceDiag(int row, int col){
        //将输入点归一到左侧或底边的边界点
        int size = row + col + 1;
        if (size > Board.SIZE) {
            size = 2 * Board.SIZE - size;
        }
        if (row + col < Board.SIZE){
            col = row + col;
            row = 0;
        } else {
            row = row - (Board.SIZE - col - 1);
            col = Board.SIZE - 1;
        }
        char[] tmp = new char[size];
        int base = col * Board.SIZE + row;
        for (int pos = 0; pos < size; pos++){
            tmp[pos] = data[base - (Board.SIZE - 1) * pos];
        }

        return tmp;
    }
}
