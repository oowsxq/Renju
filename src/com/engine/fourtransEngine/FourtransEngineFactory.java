package com.engine.fourtransEngine;

import com.chessboard.Chessboard;
import com.engine.Engine;
import com.sun.scenario.effect.ZoomRadialBlur;

import java.awt.*;
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
    |ENABLE_TOP_CUT              |move方法的necessary==1时发送    |worker线程在顶层启用剪枝     |
    |EXPERIMENTAL_SEARCH         |move方法                        |worker线程使用实验性搜索算法 |

     */
    private int signal = 0x00;
    private static final int NEED_COMPUTE_SIGNAL = 0x01;
//    private static final int COMPUTE_PAUSE_SIGNAL       = 0x02;
//    private static final int NEED_CONTINUE_SIGNAL    = 0x04;
//    private static final int IN_GAMING_SIGNAL        = 0x08;
//    private static final int ENDGAME_SIGNAL     = 0x10;
    private static final int ENABLE_TOP_CUT = 0x20;
    private static final int EXPERIMENTAL_SEARCH = 0x40;

    private final Object computeStartNotifier = new Object();
    private final Object computeDoneNotifier = new Object();
    private int computingCounter = 0;
//    private final Object computeEndedNotifier = new Object();

    private final Object expandUnitGetterLock = new Object();
    private final Object expandUnitSetterLock = new Object();

    private final Object topCutLBoundLock = new Object();

    /**
     * 全局参数
     */
    private static int HALF_SEARCH_DEPTH = 3;   //this should be odd number
    private static int SEARCH_DEPTH = 3;        //this should be odd number
    private static int NUM_OF_WORKER = 2;


    /**
     * 引擎计算用数据
     */
    private LinkedList<ExpandUnit> expandList = new LinkedList<ExpandUnit>();   //待展开节点列表
    private LinkedList<ExpandUnit> expandedList = new LinkedList<ExpandUnit>(); //已展开节点列表
    private int currentEngineStatus = Engine.ENGINE_INITIALIZING;
    private int currentLBound = Evaluator.VALUE_MIN;

    /**
     * 当前博弈设置
     */
    private boolean forbidDoubleThree = false;
    private boolean forbidDoubleFour = false;
    private boolean forbidOverline = false;
    private boolean openGameAsFree = false;

    public FourtransEngine(){
        //init components
        OpenLib.initOpenPattern();

        //init worker
        for (int i = 0; i < NUM_OF_WORKER; i++) {
            new Thread(this, "Test-Worker-" + i).start();
        }
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


        /* 如果开启指定开局 则 前三手直接从开局库中取结果 */
        if (!openGameAsFree && max_order <= 2){
            LinkedList<Engine.ResultUnit> result = new LinkedList<ResultUnit>();

            //指定开局，取一个比较平衡的开局
            OpenPattern pattern = OpenLib.getRandomPatternEqual();
            result.add(new Engine.ResultUnit(pattern.steps[max_order].x, pattern.steps[max_order].y));

            currentEngineStatus = Engine.ENGINE_READY;
            return result;
        }

        /* 如果当前是第一手棋，则总是下天元 */
        if (max_order == 0){
            LinkedList<ResultUnit> result = new LinkedList<ResultUnit>();
            result.add(new ResultUnit(7,7));
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



        /* 生成有效步骤序列，按照权重高低升序排列 */
        LinkedList<SearchElement> initList = StepGenerator.generateTopSearchElements(board, side);



        /* 初始化计算参数 */
        int depth;
        if (max_order > 4)
            depth = SEARCH_DEPTH;
        else
            depth = HALF_SEARCH_DEPTH;

        /* 初始化当前的 Zobrist */
        LinkedList<ChessValueWithOrder> orders = getOrderListFromChessboard(chessboard);
        Zobrist zobrist = new Zobrist();
        for (ChessValueWithOrder order : orders){
            zobrist.go(order.x, order.y, (order.order % 2 == 1) ? Board.BLACK : Board.WHITE);
        }

        synchronized (computeDoneNotifier) {
            expandList.clear();        //清空搜素队列
            expandedList.clear();       //清空结果

            //向待展开节点列表装入数据
//            for (int i = 0; i < Board.SIZE; i++)
//                for (int j = 0; j < Board.SIZE; j++)
//                    if (board.getValue(i, j) == 'e')
//                        expandList.add(new ExpandUnit(new Board(board), i, j, side, depth));
            while (!initList.isEmpty()){
                SearchElement tmp = initList.poll();
                expandList.add(new ExpandUnit(new Board(board), tmp.row, tmp.col, side, depth, zobrist));
            }

            //如果只需要走一步则开启顶层剪枝、试验性搜索
            if (necessarySteps == 1){
                setLBound(Evaluator.VALUE_MIN);
                setSignal(ENABLE_TOP_CUT);
                setSignal(EXPERIMENTAL_SEARCH);
            }

            try {
                setSignal(NEED_COMPUTE_SIGNAL);
                synchronized (computeStartNotifier) {
                    computeStartNotifier.notifyAll();
                    computingCounter = NUM_OF_WORKER;
                }
                while (computingCounter != 0)
                    computeDoneNotifier.wait();
                resetSignal(NEED_COMPUTE_SIGNAL);
                resetSignal(ENABLE_TOP_CUT);
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

        //新建一个没有第五手的棋盘
        Board board = new Board(chessboard.trans2EngineFriendlyCharArray());
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++){
                if (chessboard.getChessOrderEngineFriendly(i,j) == 5)
                    board.setValue(i, j, Board.EMPYT);
            }

        //判定各个五手落子的值
        synchronized (computeDoneNotifier) {
            expandList.clear();        //清空搜素队列
            expandedList.clear();       //清空结果

            //向待展开节点列表装入数据
            for (int i = 0; i < 15; i++)
                for (int j = 0; j < 15; j++){
                    if (chessboard.getChessOrderEngineFriendly(i,j) == 5)
                        expandList.add(new ExpandUnit(new Board(board), i, j, 'b', 3));
                }

            try {
                setSignal(NEED_COMPUTE_SIGNAL);
                synchronized (computeStartNotifier) {
                    computeStartNotifier.notifyAll();
                    computingCounter = NUM_OF_WORKER;
                }
                while (computingCounter != 0)
                    computeDoneNotifier.wait();
                resetSignal(NEED_COMPUTE_SIGNAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //选取最小的返回
        expandedList.sort(Comparator.comparingInt((ExpandUnit o) -> o.score));
        ExpandUnit tmp = expandedList.getFirst();
        return new ResultUnit(tmp.x, tmp.y);
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
                if (! detectSignal(EXPERIMENTAL_SEARCH)) {
                    if (detectSignal(ENABLE_TOP_CUT))
                        unit.score = algorithm.expand(unit.board, unit.depth, unit.side, unit.x, unit.y, getLBound());
                    else
                        unit.score = algorithm.expand(unit.board, unit.depth, unit.side, unit.x, unit.y);
                } else {
                    unit.score = algorithm.expandExperimetnal(unit.board, unit.depth, unit.side, unit.x, unit.y, getLBound(), unit.zobrist);
                }
                if (getLBound() < unit.score){
                    setLBound(unit.score);
                }
                workerPutResult(unit);
            }

            synchronized (computeStartNotifier) {
                //没有展开任务了，通知结果已经产生
                try {
                    synchronized (computeDoneNotifier) {
                        computingCounter--;
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

    private int getLBound(){
        synchronized (topCutLBoundLock){
            return currentLBound;
        }
    }

    private void setLBound(int new_LBound){
        synchronized (topCutLBoundLock){
            currentLBound = new_LBound;
        }
    }

    @Override
    public ResultUnit needExchange(Chessboard chessboard, int seconds) {
        LinkedList<ChessValueWithOrder> stepslist =  getOrderListFromChessboard(chessboard);
        Point[] steps = new Point[stepslist.size()];
        for (int i = 0; i < steps.length; i++) steps[i] = new Point();
        for (int i = 0; i < steps.length; i++){
            steps[i].x = stepslist.get(i).x;
            steps[i].y = stepslist.get(i).y;
        }

        //索引开局库，获得开局判定是否有利于黑方的程度
        double strength = OpenLib.getPatternByHashkey(Zobrist.getRebuildedZobrist(steps).getCurrentCode()).strength;
        return new ResultUnit(strength < 0.5);    // 如果有利于黑方则交换
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

    /**
     * 从平台给定的棋盘局面中提取落子顺序序列
     * @param chessboard
     * @return
     */
    private LinkedList<ChessValueWithOrder> getOrderListFromChessboard(Chessboard chessboard) {
        LinkedList<ChessValueWithOrder> result = new LinkedList<ChessValueWithOrder>();
        int tmp_order = 0;
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++) {
                if ((tmp_order = chessboard.getChessOrderEngineFriendly(i, j)) != 0) {
                    result.add(new ChessValueWithOrder(i, j, tmp_order));
                }
            }
        result.sort(Comparator.comparingInt((chess) -> chess.order));
        return result;
    }
}

class ChessValueWithOrder{
    public int x;
    public int y;
    public int order;
    ChessValueWithOrder(int x, int y, int order){
        this.x = x;
        this.y = y;
        this.order = order;
    }
}

/**
 * 等待搜索的单元，用这个数据结构组成队列进入搜索，引擎不断调用
 */
class ExpandUnit {
    public Board board;     //局面数据
    public int score;       //最终得分
    public int x;           //落子的 row 坐标
    public int y;           //落子的 col 坐标
    public char side;       //搜索哪一方的极大值
    public int depth;       //搜索深度
    public Zobrist zobrist;

    // 用于构造待展开单元
    public ExpandUnit(Board input_board, int x, int y, char side, int depth){
        this.board = new Board(input_board);
        this.x = x;
        this.y = y;
        this.side = side;
        this.depth = depth;
    }

    // 用于构造待展开单元
    public ExpandUnit(Board input_board, int x, int y, char side, int depth, Zobrist zobrist){
        this.board = new Board(input_board);
        this.x = x;
        this.y = y;
        this.side = side;
        this.depth = depth;
        this.zobrist = zobrist;
    }

    //用于构造展开结果
    public ExpandUnit(int x, int y, int score){

    }
}
