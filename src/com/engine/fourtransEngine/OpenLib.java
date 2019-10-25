package com.engine.fourtransEngine;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

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
     * @param hashkey
     * @return null 则开局库中无此开局
     */
    public static OpenPattern getPatternByName(long hashkey){
        initOpenPattern();
        OpenPattern result = null;
        Iterator<Map.Entry<Long, OpenPattern>> iter = lib.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<Long, OpenPattern> element = iter.next();
            if (element.getKey() == hashkey)
                result = element.getValue();
        }
        return result;
    }

    private static void utilInsertDirectPattern(String name, Point step3, double strength){
        Board board_tmp = new Board(basicDirectBoard);
        Long hashkey_tmp = null;
        Point[] steps = null;

        //插入模式

        //插入对称的等效模式
    }

    private static void utilInsertInclinedPattern(String name, Point step3, double strength){
        Board board_tmp = new Board(basicInclinedBoard);
        Long hashkey_tmp = null;
        Point[] steps = null;
    }

    class OpenPattern{
        public String name;             //局面名称
        public Board board;             //棋盘局面
        public long hashkey;            //哈希值
        public Point[] steps;            //三步的步法
        public double strength;          //强度 0 ~ 1，越小对黑越有利
        OpenPattern(String name, Board board, long hashkey, Point[] steps, double strength){
            this.name = name;
            this.board = new Board(board);
            this.hashkey = hashkey;
            this.steps = new Point[3];
            steps[0] = new Point(steps[0]);
            steps[1] = new Point(steps[1]);
            steps[2] = new Point(steps[2]);
            this.strength = strength;
        }
    }
}
