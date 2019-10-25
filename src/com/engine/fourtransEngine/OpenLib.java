package com.engine.fourtransEngine;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 开局库，处理指定开局情况下的前五步走法
 */
public class OpenLib {
    private static boolean inited = false;
    private static char[][] basicDirectArray =
                    {{'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'w', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'b', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'}};

    private static char[][] basicInclinedArray =
                    {{'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'w', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'b', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'},
                    {'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e', 'e'}};

    private static Board basicDirectBoard = new Board(basicDirectArray);
    private static Board basicInclinedBoard = new Board(basicInclinedArray);
    private static LinkedHashMap<Long, OpenPattern> lib = new LinkedHashMap<Long, OpenPattern>();
           
    public static void initOpenPattern(){
        if (!inited) {

            //長星
            utilInsertInclinedPattern("1I", new Point(5,9), 0.6);
            //峡月
            utilInsertInclinedPattern("2I", new Point(6,9), 0.2);
            //恒星
            utilInsertInclinedPattern("3I", new Point(7,9), 0.2);
            //水月
            utilInsertInclinedPattern("4I", new Point(8,9), 0.2);
            //流星
            utilInsertInclinedPattern("5I", new Point(9,9), 0.6);
            //雲月
            utilInsertInclinedPattern("6I", new Point(7,8), 0.2);
            //浦月
            utilInsertInclinedPattern("7I", new Point(8,8), 0.1);
            //嵐月
            utilInsertInclinedPattern("8I", new Point(9,8), 0.2);
            //銀月
            utilInsertInclinedPattern("9I", new Point(8,7), 0.3);
            //明星
            utilInsertInclinedPattern("10I", new Point(9,7), 0.2);
            //斜月
            utilInsertInclinedPattern("11I", new Point(8,6), 0.4);
            //名月
            utilInsertInclinedPattern("12I", new Point(9,6), 0.3);
            //彗星
            utilInsertInclinedPattern("13I", new Point(9,5), 0.8);


            //寒星
            utilInsertDirectPattern("1D", new Point(5,7), 0.2);
            //渓月
            utilInsertDirectPattern("2D", new Point(5,8),0.2);
            //疎星
            utilInsertDirectPattern("3D", new Point(5,9),0.5);
            //花月
            utilInsertDirectPattern("4D", new Point(6,8),0.1);
            //残月
            utilInsertDirectPattern("5D", new Point(6,9),0.3);
            //雨月
            utilInsertDirectPattern("6D", new Point(7,8),0.2);
            //金星
            utilInsertDirectPattern("7D", new Point(7,9),0.2);
            //松月
            utilInsertDirectPattern("8D", new Point(8,7), 0.3);
            //丘月
            utilInsertDirectPattern("9D", new Point(8,8), 0.4);
            //新月
            utilInsertDirectPattern("10D", new Point(8,9), 0.3);
            //瑞星
            utilInsertDirectPattern("11D", new Point(9,7),0.5);
            //山月
            utilInsertDirectPattern("12D", new Point(9,8),0.3);
            //遊星
            utilInsertDirectPattern("13D", new Point(9,9),0.8);


            //初始化完成后
            inited = true;
        }
    }

    /**
     * 按照哈希值返回开局模式
     * @param hashkey
     * @return null 则开局库中无此开局
     */
    public static OpenPattern getPatternByHashkey(long hashkey){
        initOpenPattern();
        OpenPattern result = lib.get(hashkey);
        return result;
    }

    /**
     * 按照开局名称返回开局模式
     * @param name
     * @return null 则开局库中无此开局
     */
    public static OpenPattern getPatternByName(String name){
        initOpenPattern();
        OpenPattern result = null;
        Iterator<Map.Entry<Long, OpenPattern>> iter = lib.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<Long, OpenPattern> element = iter.next();
            if (element.getValue().name.equals(name)) {
                result = element.getValue();
                break;
            }
        }
        return result;
    }

    /**
     * 随机获取一个平衡开局 { 3D, 11D }
     * @return
     */
    public static OpenPattern getRandomPatternEqual(){
        if (Math.random() < 0.5)
            return getPatternByName("3D");
        else
            return getPatternByName("11D");
    }

//    /**
//     * 随机获取一个黑方绝对优势的开局 { 7I, 4D }
//     * @return
//     */
//    public static OpenPattern getRandomPatternBlackSureWin(){
//
//    }

    private static void utilInsertDirectPattern(String name, Point step3, double strength){
//        Board board_tmp = null; //暂时用不上，用null插入，以后有需要再改
        Point[] steps = new Point[3];
        steps[0] = new Point(7,7);
        steps[1] = new Point(6,7);
        steps[2] = new Point(step3);
        long hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();

        //插入模式
        utilInsertMutiple(name, null, hashkey, steps, strength);

    }

    private static void utilInsertInclinedPattern(String name, Point step3, double strength){
//        Board board_tmp = null; //暂时用不上，用null插入，以后有需要再改
        Point[] steps = new Point[3];
        steps[0] = new Point(7,7);
        steps[1] = new Point(6,8);
        steps[2] = new Point(step3);
        long hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();

        //插入模式
        utilInsertMutiple(name, null, hashkey, steps, strength);
    }

    /**
     * 向开局库中写入一种开局及其他7种等效排布
     * @param name
     * @param board
     * @param hashkey
     * @param steps
     * @param strength
     */
    private static void utilInsertMutiple(String name, Board board, long hashkey, Point[] steps, double strength){
        //插入模式
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        //插入等效模式
        steps = utilRotate90(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        steps = utilRotate90(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        steps = utilRotate90(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        steps = utilFlipViceDiagonal(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        steps = utilRotate90(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        steps = utilRotate90(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));

        steps = utilRotate90(steps);
        hashkey = Zobrist.getRebuildedZobrist(steps).getCurrentCode();
        lib.put(hashkey, new OpenPattern(name, null, hashkey, steps, strength));
    }

    private static Point[] utilFlipViceDiagonal(Point[] steps){
        Point[] result = new Point[steps.length];
        for (int i = 0; i < result.length; i++) result[i] = new Point();
        for (int i = 0; i < steps.length; i++){
            result[i].x = steps[i].y;
            result[i].y = steps[i].x;
        }
        return result;
    }

    private static Point[] utilRotate90(Point[] steps){
        Point[] result = new Point[steps.length];
        for (int i = 0; i < result.length; i++) result[i] = new Point();
        for (int i = 0; i < steps.length; i++){
            result[i].x = -steps[i].y + 14;
            result[i].y = steps[i].x;
        }
        return result;
    }
}

class OpenPattern{
    public String name;             //局面名称
//    public Board board;             //棋盘局面
    public long hashkey;            //哈希值
    public Point[] steps;            //三步的步法
    public double strength;          //强度 0 ~ 1，越小对黑越有利
    OpenPattern(String name, Board board, long hashkey, Point[] steps, double strength){
        this.name = name;
//        if (board != null)
//            this.board = new Board(board);
        this.hashkey = hashkey;
        this.steps = new Point[3];
        this.steps[0] = new Point(steps[0]);
        this.steps[1] = new Point(steps[1]);
        this.steps[2] = new Point(steps[2]);
        this.strength = strength;
    }
}