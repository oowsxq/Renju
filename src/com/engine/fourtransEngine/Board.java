package com.engine.fourtransEngine;

/**
 * 用于表示搜索节点的数据结构
 * board是用于表示棋盘状态的字符数组，每个元素具有四种可能 { 'b','w','e','f' }
 */
class Board {
    public static final int SIZE = 15;  //五子棋边长 15
    public static final byte EMPYT = 'e';
    public static final byte BLACK = 'b';
    public static final byte WHITE = 'w';
    public static final byte FORBID = 'f';

    private byte[] data = new byte[SIZE * SIZE];

    Board(final Board src){
        System.arraycopy(src.data, 0, this.data, 0, SIZE*SIZE);
    }

    Board(final byte[][] src){
        for (int i = 0; i < SIZE; i++){
            System.arraycopy(src[i], 0, this.data, i * SIZE, SIZE);
        }
    }

    Board(final char[][] src){
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                data[i * SIZE  + j] = (byte)src[i][j];
    }

    public final byte getValue(int x, int y){
        return data[x * SIZE + y];
    }

    public final void setValue(int x, int y, byte c){
        data[x * SIZE  + y] = c;
    }
}
