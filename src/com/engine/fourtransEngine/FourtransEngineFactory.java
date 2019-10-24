package com.engine.fourtransEngine;

import com.chessboard.Chessboard;
import com.engine.Engine;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class FourtransEngineFactory {
    public static int engineCounter = 0;
    public static FourtransEngine createEngine(){
        FourtransEngine result = new FourtransEngine();
        engineCounter++;
        return result;
    }
}

class FourtransEngine implements Engine, Runnable {

     /**
     * 进程同步用数据
     */
    /*
    引擎内部的信号表示和传递常数，原则：由 sender 置位和复位，receiver 只负责接收，如果 signal == 0 则当前无信号
    |SIGNAL                      |SENDER                          |RECEIVER                     |
    |:--------------------------:|:------------------------------:|:---------------------------:|
    |NEED_COMPUTE_SIGNAL         |需要 worker 计算的方法          |worker线程 run 方法的循环体中|

     */
    private int signal = 0x00;
    private static final int NEED_COMPUTE_SIGNAL = 0x01;
//    private static final int COMPUTE_PAUSE_SIGNAL       = 0x02;
//    private static final int NEED_CONTINUE_SIGNAL    = 0x04;
//    private static final int IN_GAMING_SIGNAL        = 0x08;
//    private static final int ENDGAME_SIGNAL     = 0x10;

    private final Object computeStartNotifier = new Object();
    private final Object computeDoneNotifier = new Object();
    private final Object computeEndedNotifier = new Object();

    private final Object expandUnitGetterLock = new Object();
    private final Object expandUnitSetterLock = new Object();

    /**
     * 全局参数
     */
    public static int SEARCH_DEPTH = 1;


    /**
     * 引擎计算用数据
     */
    private LinkedList<ExpandUnit> expandList = new LinkedList<ExpandUnit>();   //待展开节点列表
    private LinkedList<ExpandUnit> expandedList = new LinkedList<ExpandUnit>(); //已展开节点列表
    private int currentEngineStatus = Engine.ENGINE_INITIALIZING;;

    /**
     * 当前博弈设置
     */
    private boolean forbidDoubleThree = false;
    private boolean forbidDoubleFour = false;
    private boolean forbidOverline = false;
    private boolean openGameAsFree = false;

    public FourtransEngine(){
        //TODO:init
        new Thread(this, "Test-Worker-01").start();
        new Thread(this, "Test-Worker-02").start();
        new Thread(this, "Test-Worker-03").start();
        new Thread(this, "Test-Worker-04").start();
        currentEngineStatus = Engine.ENGINE_STANDBY;
    }

    @Override
    public Queue<ResultUnit> move(Chessboard chessboard, int necessarySteps, int seconds) {

        currentEngineStatus = Engine.ENGINE_COMPUTING;

        /* 计算当前是要下哪手棋子 */
        int max_order = 0;
        int tmp_order;
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++)
                if ((tmp_order = chessboard.getChessOrderEngineFriendly(i, j)) > max_order)
                    max_order = tmp_order;
        char side = (max_order + 1) % 2 == 0 ? 'w' : 'b';

        /* 如果当前是第一手棋，则总是下天元 */
        if (max_order == 0){
            LinkedList<ResultUnit> result = new LinkedList<ResultUnit>();
            result.add(new ResultUnit(8,8));
            currentEngineStatus = Engine.ENGINE_READY;
            return result;
        }

        /* 获取输入棋盘的内部表示 */
        Board board_tmp = new Board(chessboard.trans2EngineFriendlyCharArray());
        Board board = null;
        if (side == 'b') {
            //如果是黑方则要排除禁手点
            board = new Board(board_tmp);
            Judgementor.hasForbiddenStone(board_tmp, board);
        } else {
            board = board_tmp;
        }

        /* 初始化计算参数 */
        int depth = SEARCH_DEPTH;

        synchronized (computeDoneNotifier) {
            expandList.clear();        //清空搜素队列
            expandedList.clear();    //清空结果

            //向待展开节点列表装入数据
            for (int i = 0; i < Board.SIZE; i++)
                for (int j = 0; j < Board.SIZE; j++)
                    if (board.getValue(i, j) == 'e')
                        expandList.add(new ExpandUnit(new Board(board), i, j, side, depth));

            try {
                setSignal(NEED_COMPUTE_SIGNAL);
                synchronized (computeStartNotifier) {
                    computeStartNotifier.notifyAll();
                }
                computeDoneNotifier.wait();
                resetSignal(NEED_COMPUTE_SIGNAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        LinkedList<ResultUnit> result = new LinkedList<ResultUnit>();
        expandedList.sort(Comparator.comparingInt((ExpandUnit o) -> o.score));

        for (int i = 0; i < necessarySteps; i++){
            ExpandUnit tmp = expandedList.pollLast();
            result.add(new ResultUnit(tmp.x, tmp.y));
        }

        currentEngineStatus = Engine.ENGINE_READY;
        return result;
    }

    @Override
    public ResultUnit reserveOneFifthStone(Chessboard chessboard, int seconds) {
        //TODO: 创建一个进程任务等待计算完成后返回结果
        ResultUnit result = new ResultUnit();

        return result;
    }

    /**
     * 工作线程所在的运行空间
     */
    @Override
    public void run() {
        ExpandUnit unit;
        // producer Loop
        while(true) {
            //如果当前没有计算任务则等待
            if (!detectSignal(NEED_COMPUTE_SIGNAL)) {
                synchronized (computeStartNotifier) {
                    try {
                        computeStartNotifier.wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            while ((unit = workerGetTask()) != null) {
                //接受到展开任务，开始展开算法
                SearchAlgorithm algorithm = new SearchAlgorithm();
                unit.score = algorithm.expand(unit.board, unit.depth, unit.side, unit.x, unit.y);
                workerPutResult(unit);
            }

            synchronized (computeStartNotifier) {
                //没有展开任务了，通知结果已经产生
                try {
                    synchronized (computeDoneNotifier) {
                        computeDoneNotifier.notifyAll();
                    }
                    computeStartNotifier.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } //!while
    }

    /**
     * 从待展开列表中取第一个返回，如果没有元素则返回为 null
     * @return 待展开列表中第一个元素，若列表为空则返回 null
     */
    private ExpandUnit workerGetTask(){
        ExpandUnit result = null;
        synchronized (expandUnitGetterLock){
            if (!expandList.isEmpty())
                result = expandList.poll();
        }
        return result;
    }

    /**
     * 向已展开列表中放入一个结果
     * @param unit 已经展开的，有 score 估值的展开单元
     */
    private void workerPutResult(ExpandUnit unit){
        synchronized (expandUnitSetterLock){
            expandedList.add(unit);
        }
    }

    @Override
    public ResultUnit needExchange(Chessboard chessboard, int seconds) {
        //TODO: 计算完成后返回结果
        return new ResultUnit(true);        //暂时总是三手交换
    }

    @Override
    public void startNewGame(boolean forbidDoubleThree, boolean forbidDoubleFour, boolean forbidOverline, boolean openGameAsFree) {
        //TODO: 创建一个进程任务执行命令
        this.forbidDoubleThree = forbidDoubleThree;
        this.forbidDoubleFour = forbidDoubleFour;
        this.forbidOverline = forbidOverline;
        this.openGameAsFree = openGameAsFree;
        Judgementor.setRule(forbidDoubleThree, forbidDoubleFour, forbidOverline);
        currentEngineStatus = Engine.ENGINE_READY;
    }

    @Override
    public void endCurrentGame() {
        //TODO: 重置引擎状态
        currentEngineStatus = Engine.ENGINE_STANDBY;
    }

    @Override
    public void computeContinue() {
        //暂不实现
    }

    @Override
    public void computePause() {
        //暂不实现
    }

    @Override
    public void computeEnd() {
        //暂不实现
    }

    @Override
    public int getStatus() {
        return this.currentEngineStatus;
    }

    /**
     * 信号置位
     * @param type 需要置位的信号
     */
    private void setSignal(int type){
        signal |= type;
    }

    /**
     * 信号复位
     * @param type 需要复位的信号
     */
    private void resetSignal(int type){
        signal &= ~type;
    }

    /**
     * 信号检测
     * @param type 需要检测的信号
     * @return true 表示该信号已经置位， false 表示未置位
     */
    private boolean detectSignal(int type){
        return (signal & type) != 0;
    }
}

/**
 * 等待搜索的单元，用这个数据结构组成队列进入搜索，引擎不断调用
 */
class ExpandUnit {
    public Board board;     //局面数据
    public int score;       //最终得分
    public int x;           //落子的 x 坐标
    public int y;           //落子的 y 坐标
    public char side;       //搜索哪一方的极大值
    public int depth;       //搜索深度

    // 用于构造待展开单元
    public ExpandUnit(Board input_board, int x, int y, char side, int depth){
        this.board = new Board(input_board);
        this.x = x;
        this.y = y;
        this.side = side;
        this.depth = depth;
    }

    //用于构造展开结果
    public ExpandUnit(int x, int y, int score){

    }
}
