package com.engine.fourtransEngine;

import java.util.LinkedList;
import java.util.TreeSet;

/**
 * 搜索算法，使用 MAX-MIN, alpha-beta 剪枝
 */
class SearchAlgorithm{

    public Board board;
    private LinkedList<SearchElement> movementList = new LinkedList<SearchElement>();

    SearchAlgorithm(Board board, int x, int y){

    }

    public int alphaBetaSearch(int depth, int side, int alpha, int beta){
        //TODO
        return 0;
    }

    /**
     * 走法步骤创建，创建完毕存入走法步骤集合
     */
    private void generateLegalMovement(){


        movementList.sort(null);
    }

    class SearchElement implements Comparable{
        int x;
        int y;
        int priority;

        @Override
        public int compareTo(Object o) {
            return this.priority - ((SearchElement)o).priority;
        }
    }
}