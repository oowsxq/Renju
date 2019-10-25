package com.engine.fourtransEngine;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Zobrist 哈希算法
 */
public class Zobrist {
    private static final int SIZE = 15;
    private static final int DEFAULT_SEED = 794357291;



    private long[] white = new long[SIZE * SIZE];
    private long[] black = new long[SIZE * SIZE];
    private long code;                               //当前键值


    Zobrist(){
        Random rand = new Random(DEFAULT_SEED);
        for (int i = 0; i < SIZE * SIZE; i++) {
            white[i]    = ((((long)rand.nextInt()) << 32) | (long)rand.nextInt());
            black[i]    = ((((long)rand.nextInt()) << 32) | (long)rand.nextInt());
        }
        code = ((((long)rand.nextInt()) << 32) | (long)rand.nextInt());
    }

    Zobrist(Zobrist src){
        System.arraycopy(src.black, 0, this.black, 0, SIZE * SIZE);
        System.arraycopy(src.white, 0, this.white, 0, SIZE * SIZE);
        this.code = src.code;
    }

    /**
     *
     * @param row = {'b','w'}
     * @param col
     * @param side
     * @return
     */
    public long go(int row, int col, char side){
        int index = SIZE * row + col;
        code ^= (side == 'b' ? black[index] : white[index]);
        return code;
    }

    /**
     * Uses serialization to create a copy of the given Random, needed for
     * repeatability in some tests.
     */
    private Random utilCloneRandom(Random src) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bo);
        oos.writeObject(src);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bo.toByteArray()));
        return (Random)(ois.readObject());
    }
}
