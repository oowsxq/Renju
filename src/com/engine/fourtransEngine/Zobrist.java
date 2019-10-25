package com.engine.fourtransEngine;


import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Random;

/**
 * Zobrist 哈希算法
 */
public class Zobrist {
    private static final int SIZE = 15;
    private static final int DEFAULT_SEED = 794357291;

    private static boolean initialized = false;
    private static long[] white = new long[SIZE * SIZE];
    private static long[] black = new long[SIZE * SIZE];


    private long code;                               //当前键值


    Zobrist(){
        if (!initialized) {
            Random rand = new Random(DEFAULT_SEED);
            for (int i = 0; i < SIZE * SIZE; i++) {
                white[i] = ((((long) rand.nextInt()) << 32) | (long) rand.nextInt());
                black[i] = ((((long) rand.nextInt()) << 32) | (long) rand.nextInt());
            }
            initialized = true;
        }
        Random rand = new Random(DEFAULT_SEED); //保证每次新构造的 Zobrist 都有相同的初始值
        code = ((((long)rand.nextInt()) << 32) | (long)rand.nextInt());
    }

    Zobrist(Zobrist src){
        if (!initialized) {
            Random rand = new Random(DEFAULT_SEED);
            for (int i = 0; i < SIZE * SIZE; i++) {
                white[i] = ((((long) rand.nextInt()) << 32) | (long) rand.nextInt());
                black[i] = ((((long) rand.nextInt()) << 32) | (long) rand.nextInt());
            }
            initialized = true;
        }
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
     * 获取当前的 code
     * @return
     */
    public long getCurrentCode(){
        return code;
    }

    /**
     * 输入一个棋局的走法序列，重建 Zobrist 对象，此方式开销较高，不应当被频繁调用，只用于入口操作
     * @param steps 棋局的全部走法序列，第一个元素对应第一手黑棋，第二个元素对应第二手的白棋，以此类推
     * @return 重建后的 Zobrist 实例
     */
    public static Zobrist getRebuildedZobrist(LinkedList<Point> steps){
        Zobrist result = new Zobrist();
        int counter = 1;
        for (Point step : steps){
            result.go(step.x, step.y, ((counter % 2 == 0) ? 'w' : 'b'));
            counter++;
        }
        return result;
    }

    /**
     * 输入一个棋局的走法序列，重建 Zobrist 对象，此方式开销较高，不应当被频繁调用，只用于入口操作
     * @param steps 棋局的全部走法序列，第一个元素对应第一手黑棋，第二个元素对应第二手的白棋，以此类推
     * @return 重建后的 Zobrist 实例
     */
    public static Zobrist getRebuildedZobrist(Point[] steps){
        Zobrist result = new Zobrist();
        int counter = 1;
        for (Point step : steps){
            result.go(step.x, step.y, ((counter % 2 == 0) ? 'w' : 'b'));
            counter++;
        }
        return result;
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
