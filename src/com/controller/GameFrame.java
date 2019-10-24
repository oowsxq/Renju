package com.controller;

import com.chessboard.ChessValue;
import com.chessboard.Chessboard;
import com.chessboard.ChessboardListener;
import com.chessboard.ChessboardPanel;
import com.controller.setting.SettingDialog;
import com.controller.setting.SettingModel;
import com.engine.Engine;
import com.engine.SimpleRandomEngine;
import com.engine.fourtransEngine.FourtransEngineFactory;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */
public class GameFrame extends JFrame implements
        ChessboardListener, GameControlListener {
    /**
     * 多线程同步用
     */
    private final Object putChessSynchronizer = new Object();

    /**==========================
     * 常量参数
     */
    private static final int TIME_STEP = 500;               //计时间隔 ms
    private static final int AGENT_PERFORM_PERIOD = 20;    //引擎代理执行间隔 ms

    /**==========================
     * flags 用于控制全局状态
     */
    private boolean gamePlayingFlag = false;                //当前博弈是否在进行中，如果为 false 则相关游戏控制事件忽略
    private boolean gamePausingFlag = false;                //当前是否在暂停状态，如果为 true 则停止计时
    private boolean waitingToChooseFifthFlag = false;       //当前是否在等待需要保留的第五手内容
    private boolean waitingEngineResponseFlag = false;      //等待引擎返回结果，使用上先引擎发送计算指令后置位，获取结果后复位，此标记 true 则相关冲突操作会被拒绝
    private boolean thirdSwapedFlag = false;                //发生三手交换后此标志置位，便于悔棋组件、棋谱组件读取

    /**==========================
     * 各种图形化组件
     */
    ChessboardPanel chessboardPanel;
    GameControlPanel gameControlPanel;
    SettingDialog settingDialog;

    /**===========================
     * 计时器组件
     */
    Timer timeCounter = new Timer();    //更新时间

    /**===========================
     * 数据组件
     */
    SettingModel settingModel = new SettingModel();
    Engine.ResultUnit resultFromEngine = new Engine.ResultUnit();                           //从引擎接受的单个结果单元引用，重复使用
    Queue<Engine.ResultUnit> resultsFromEngine = new LinkedList<Engine.ResultUnit>();  //从引擎接收的多个结果单元引用，每次使用后 clear
    HistoryRecorder historyRecorder = new HistoryRecorder();

    /**===========================
     * 引擎组件
     */
    Engine engine = FourtransEngineFactory.createEngine();
//    Engine engine = new SimpleRandomEngine();

    /**===========================
     * 当前局面数据，悔棋后必须修改这些数据
     */
    int  currOrder      = 1;                    //当前正在准备下的下一子编号
    int  fifthCounter   = 1;                    //当前正在下第几个五手N打，每下一个 +1 ,当 fifthCounter == settingModel.fifthMoveSteps 时先落子后向白方要求移子
    long blackUsedTimeMillis = 0;
    long whiteUsedTimeMillis = 0;
    boolean playerPlayBlack = false;            //初始化使用 settingModel.playerIsBlack，如果发生三手交换则修改这个标记
    Chessboard curr_chessboard = null;

    /**
     * 游戏核心窗口构造方法
     */
    public GameFrame(String title){

        //set title
        if (title == null)
            this.setTitle("Renju Game");
        else
            this.setTitle(title);

        initComponent();
        initFrame();

        initTimer();

        setVisible(true);
    }

    /**
     * 初始化各种必要图形化组件
     */
    private void initComponent(){
        curr_chessboard = new Chessboard(15);
        chessboardPanel = new ChessboardPanel(curr_chessboard, this);
        settingDialog = new SettingDialog(this, "settings", settingModel);

        gameControlPanel = new GameControlPanel(this);

        getContentPane().add(chessboardPanel, BorderLayout.CENTER);
        getContentPane().add(gameControlPanel, BorderLayout.EAST);
    }

    /**
     * 初始化 JFrame 内容
     */
    private void initFrame(){
        setBounds(200,200,800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * 初始化定时器相关内容
     */
    private void initTimer() {
        //用于时间更新的线程
        timeCounter.schedule(new TimerTask() {
            @Override
            public void run() {
                refreshTime();
            }
        }, 0, TIME_STEP);
    }

    /**
     * 时间更新方法
     */
    private void refreshTime(){
        if (gamePlayingFlag && !gamePausingFlag) {
            if (currOrder % 2 == 1) {
                /* 当前是黑方在下棋 */
                if (playerPlayBlack || waitingEngineResponseFlag) {
                    if (settingModel.fifthMoveMultiple && currOrder == 5 && fifthCounter > settingModel.fifthMoveSteps)
                        return; //如果是五手交换等待白方选择保留字，则此时不记时
                    blackUsedTimeMillis += TIME_STEP;
                    gameControlPanel.setBlackUsedTime(blackUsedTimeMillis);
                    gameControlPanel.setCurrentPlayerLabel(thirdSwapedFlag ? settingModel.whiteName : settingModel.blackName);
                    SwingUtilities.invokeLater(() -> gameControlPanel.repaint());
                }
            } else {
                /* 当前是白方在下棋 */
                if (!playerPlayBlack || waitingEngineResponseFlag) {
                    whiteUsedTimeMillis += TIME_STEP;
                    gameControlPanel.setWhiteUsedTime(whiteUsedTimeMillis);
                    gameControlPanel.setCurrentPlayerLabel(thirdSwapedFlag ? settingModel.blackName : settingModel.whiteName);
                    SwingUtilities.invokeLater(() -> gameControlPanel.repaint());
                }
            }
        }
    }

    /**
     * 落子，接收来自玩家的落子和引擎的落子，依据当前的全局 flags 等信息做出判断
     * 请保证输入的落子点都是合法的
     */
    private void putChess(int x, int y){
        synchronized(putChessSynchronizer) {
            boolean thisTurnDone = false;   //本次落子已经结束，如果此标记置位则后续不判断

            //三手交换
            if (settingModel.thirdMoveExchange && currOrder == 3) {
                curr_chessboard.setChessValue(x, y, ChessValue.BLACK, currOrder);
                SwingUtilities.invokeLater(() -> chessboardPanel.repaint());
                currOrder++;

                if (playerPlayBlack) {
                    //询问引擎是否要交换
                    commandEngineToChooseThirdSwap();
                    waitingEngineResponseFlag = true;
                } else {
                    SwingUtilities.invokeLater(() -> {
                        //询问玩家是否要交换
                        int result = JOptionPane.showConfirmDialog(
                                this, "你是否要执行三手交换，交换后你将执黑棋。", "三手交换",
                                JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            thirdSwap();
                            //如果玩家选择三手交换后，则向引擎发出行棋通知
                            if (playerPlayBlack && currOrder % 2 == 0 || !playerPlayBlack && currOrder % 2 == 1) {
                                commandEngineToMove(1);
                            }
                        }
                    });
                }

                thisTurnDone = true;
                historyRecorder.recordCurrentStatus();
            }

            //五手 N 打
            if (!thisTurnDone && currOrder == 5 && settingModel.fifthMoveMultiple) {
                if (fifthCounter < settingModel.fifthMoveSteps) {
                    curr_chessboard.setChessValue(x, y, ChessValue.BLACK, currOrder);
                    SwingUtilities.invokeLater(() -> chessboardPanel.repaint());
                    fifthCounter++;
                } else if (!waitingToChooseFifthFlag) {
                    curr_chessboard.setChessValue(x, y, ChessValue.BLACK, currOrder);
                    SwingUtilities.invokeLater(() -> chessboardPanel.repaint());
                    fifthCounter++;     //五手N子全部落下时 fifthCounter 应当是设定数+1

                    if (playerPlayBlack) {
                        //向引擎询问保留哪个子，置标记 waitingToChooseFifthFlag，等待下次再次到达此方法进行处理
                        commandEngineToReserveOneFifthStone();
                        waitingToChooseFifthFlag = true;
                    } else {
                        //向玩家询问保留哪个子，置标记 waitingToChooseFifthFlag，等待下次再次到达此方法进行处理
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "请选择一个有效的第五手落子点", "五手N打", JOptionPane.INFORMATION_MESSAGE));
                        waitingToChooseFifthFlag = true;
                    }
                } else {
                    //玩家或引擎给出了要保留的子，把其他五手子剔除，这里只有在 watingToChooseFifth 标记置位的情况下才会到达这里

                    //先确认输入的点是否是一个五手点，若不是则直接返回，等待重新输入，默认引擎总是正确，这里只会玩家出错
                    if (curr_chessboard.getChessValue(x, y) != ChessValue.BLACK
                            || curr_chessboard.getChessOrder(x, y) != 5) {
                        if (playerPlayBlack){
                            System.out.println("[fatal] Engine choose a wrong 5th stone to reserve!");
                        } else {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "这不是一个五手点，非法！请重新选择。"));
                            return;
                        }
                    }

                    waitingToChooseFifthFlag = false; //复位标志

                    //遍历棋盘，将其他非保留的序号为5的落子消去
                    for (int i = 0; i < curr_chessboard.getBoardSize(); i++)
                        for (int j = 0; j < curr_chessboard.getBoardSize(); j++) {
                            if (curr_chessboard.getChessOrder(i, j) == 5 && (i != x || j != y))
                                curr_chessboard.setChessValue(i, j, ChessValue.EMPTY, 0);
                        }
                    SwingUtilities.invokeLater(() -> chessboardPanel.repaint());
                    currOrder++;
                    historyRecorder.recordCurrentStatus();

                    //五手N打完成后，如果刚才落子的是玩家，即是引擎选保留子，则向引擎发出行棋通知
                    if (playerPlayBlack && currOrder % 2 == 0 || !playerPlayBlack && currOrder % 2 == 1) {
                        commandEngineToMove(1);
                    }
                }

                thisTurnDone = true;
            }

            //正常落子
            if (!thisTurnDone) {
                curr_chessboard.setChessValue(x, y, (currOrder % 2 == 0 ? ChessValue.WHITE : ChessValue.BLACK), currOrder);
                SwingUtilities.invokeLater(() -> chessboardPanel.repaint());
                currOrder++;
                historyRecorder.recordCurrentStatus();

                //如果刚才落子的是玩家，则向引擎发出行棋通知
                if (playerPlayBlack && currOrder % 2 == 0 || !playerPlayBlack && currOrder % 2 == 1) {
                    if (settingModel.fifthMoveMultiple && currOrder == 5)
                        commandEngineToMove(settingModel.fifthMoveSteps);
                    else
                        commandEngineToMove(1);
                }
            }

            //终局判断
            GameResult gameResult = new GameResult();
            if ((gameResult = isGameEnd(curr_chessboard)).status != GameResult.Status.NOT_END){
                if (gameResult.status == GameResult.Status.WHITE_WIN){
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "白方长连，获得胜利！"));
                } else if (gameResult.status == GameResult.Status.BLACK_WIN){
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "黑方五连，获得胜利！"));
                } else if (gameResult.status == GameResult.Status.BLACK_FORBIDDEN){
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "黑方禁着，白方胜利！"));
                }
            }
        }
    }

    /**
     * 异步方法，向引擎发送行棋通知
     */
    private void commandEngineToMove(int necessarySteps){
        waitingEngineResponseFlag = true;
        new Thread(() -> {
            resultsFromEngine = engine.move(curr_chessboard, necessarySteps,
                    settingModel.blackTotalTime - (int) (blackUsedTimeMillis / 1000));

            if (!settingModel.fifthMoveMultiple || currOrder != 5) {
                //非特殊规则正常落子
                Engine.ResultUnit tmp = resultsFromEngine.poll();
                putChess(tmp.x, tmp.y);
            } else {
                //五手 N 打要落下多个子
                for (int i = 0; i < settingModel.fifthMoveSteps; i++){
                    Engine.ResultUnit tmp = resultsFromEngine.poll();
                    putChess(tmp.x, tmp.y);
                }
            }
            resultsFromEngine.clear();

            waitingEngineResponseFlag = false;
        }).start();
    }

    /**
     * 异步方法，向引擎发送是否执行三手交换的通知
     */
    private void commandEngineToChooseThirdSwap(){
        waitingEngineResponseFlag = true;
        new Thread(() -> {
            resultFromEngine = engine.needExchange(curr_chessboard,
                    settingModel.blackTotalTime - (int)(blackUsedTimeMillis / 1000));

            if (resultFromEngine.needExchange) {
                thirdSwap();
                JOptionPane.showMessageDialog(this, "发生三手交换，现在你执白棋。");
            } else {
                //如果不三手交换，则当前执白的引擎应当走子
                commandEngineToMove(1);
            }

            waitingEngineResponseFlag = false;
        }).start();
    }

    /**
     * 异步方法，向引擎发送五手N打保留某个子的通知
     */
    private void commandEngineToReserveOneFifthStone(){
        waitingEngineResponseFlag = true;
        new Thread(() -> {
            resultFromEngine = engine.reserveOneFifthStone(curr_chessboard,
                    settingModel.blackTotalTime - (int)(blackUsedTimeMillis / 1000));

            putChess(resultFromEngine.x, resultFromEngine.y);

            waitingEngineResponseFlag = false;
        }).start();

    }

    /**
     * 异步方法，向引擎发送开始一个新对弈的通知
     */
    private void commandEngineToStartNewGame(){
        new Thread(() -> engine.startNewGame(settingModel.doubleThree,
                settingModel.doubleFour, settingModel.overline, settingModel.openGameAsFree)).start();
    }

    /**
     * 异步方法，向引擎发送暂停当前计算的通知
     */
    private void commandEngineToComputePause(){
        new Thread(() -> engine.computePause()).start();
    }

    /**
     * 异步方法，向引擎发送继续计算的通知
     */
    private void commandEngineToComputeContinue(){
        new Thread(() -> engine.computeContinue()).start();
    }

    /**
     * 异步方法，向引擎发送终止本局对弈的通知
     */
    private void commandEngineToEndCurrentGame(){
        new Thread(() -> engine.endCurrentGame()).start();
    }

//    /**
//     * 异步方法，向引擎发送放弃当前计算任务的通知
//     */
//    private void commandEngineToComputeEnd(){
//
//    }


    /**
     * 执行三手交换
     */
    private void thirdSwap(){
        debugPrompt("三手交换");
        //执棋交换
        playerPlayBlack = !playerPlayBlack;

        //时间交换
        long tmp_blackUsedTimeMillis = blackUsedTimeMillis;
        long tmp_whiteUsedTimeMillis = whiteUsedTimeMillis;
        blackUsedTimeMillis = tmp_whiteUsedTimeMillis;
        whiteUsedTimeMillis = tmp_blackUsedTimeMillis;

        //面板显示的标签交换
        gameControlPanel.setBlackTotalTime(settingModel.whiteTotalTime * 1000);
        gameControlPanel.setWhiteTotalTime(settingModel.blackTotalTime * 1000);
        gameControlPanel.setBlackUsedTime(blackUsedTimeMillis);
        gameControlPanel.setWhiteUsedTime(whiteUsedTimeMillis);
        gameControlPanel.setBlackName(settingModel.whiteName);
        gameControlPanel.setWhiteName(settingModel.blackName);

        //置位标志
        thirdSwapedFlag = true;
    }

    /**
     * 面板上选点后的回调方法
     * @param x coordinate of chess
     * @param y coordinate of chess
     */
    @Override
    public void selectChessPosition(int x, int y) {
        //出现玩家的落子点选择后，只有在游戏开始、引擎为就绪态（即已经给出上次结果）、游戏不在暂停状态同时满足才接收玩家落子
        if (gamePlayingFlag && !waitingEngineResponseFlag && !gamePausingFlag) {

            /*
            玩家向平台发出落子请求，只有在以下情形是合法的：
                - 玩家执白 且 当前是白方落子
                - 玩家执黑 且 当前是黑方落子
                - 五手N打启用 且 玩家执白 且 当前是第五手 且 当前五手N子落子完毕
            */
            if (playerPlayBlack && currOrder % 2 == 1 ||
                !playerPlayBlack && currOrder % 2 == 0 ||
                settingModel.fifthMoveMultiple && !playerPlayBlack && currOrder == 5 &&
                        fifthCounter > settingModel.fifthMoveSteps){
                if (curr_chessboard.getChessValue(x, y) == ChessValue.EMPTY || waitingToChooseFifthFlag)
                    putChess(x, y);
            }

        } else if (!gamePlayingFlag) {
            JOptionPane.showMessageDialog(this, "请开始游戏后落子", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else if (gamePausingFlag) {
            JOptionPane.showMessageDialog(this, "请继续游戏后落子", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            return; //还在等待引擎结果返回，故不接受玩家落子数据
        }
    }

    /**
     * 来自 gameControlPanel 的重做命令回调方法
     */
    @Override
    public void redoCommand() {
        debugPrompt("redo command");
        //只有在玩家的回合才可以调用历史操作
        if (playerPlayBlack && currOrder % 2 == 1 || !playerPlayBlack && currOrder % 2 == 0) {
            if (historyRecorder.canRedu()) {
                //通过 historyRecorder 恢复局面数据
                historyRecorder.goNextStatus();
            } else {
                JOptionPane.showMessageDialog(this, "已经到达最新的历史纪录");
            }
        }
    }

    /**
     * 来自 gameControlPanel 的悔棋命令回调方法
     */
    @Override
    public void undoCommand() {
        debugPrompt("undo command");
        //只有在玩家的回合才可以调用历史操作
        if (playerPlayBlack && currOrder % 2 == 1 || !playerPlayBlack && currOrder % 2 == 0) {
            if (historyRecorder.canUndo()) {
                //通过 historyRecorder 恢复局面数据
                historyRecorder.goPreviousStatus();
            } else {
                JOptionPane.showMessageDialog(this, "已经到达最早的可回退历史纪录");
            }
        }
    }

    /**
     * 来自 gameControlPanel 的暂停命令回调方法
     */
    @Override
    public void pauseCommand() {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Pause option dose not support yet."));
//        debugPrompt("pause command");
//        //通知引擎
//        if (waitingEngineResponseFlag){
//            commandEngineToComputePause();
//        }
//
//        //置标记
//        gamePausingFlag = true;
    }

    /**
     * 来自 gameControlPanel 的开始命令回调方法
     *         如果游戏尚未开始，则开始游戏
     *         如果游戏已经开始但在暂停状态则恢复游戏
     *         如果不是以上两者则忽略
     * @return 结果如果为 0 表示成功执行（开始或继续） 非 0 表示失败（比如程序正在初始化则此操作会被忽略）
     */
    @Override
    public int startCommand() {
        debugPrompt("start command");

        if (!gamePlayingFlag /* 如果游戏尚未开始，则开始游戏 */) {
            //向引擎发送游戏开始通知
            int tmp_engine_status = engine.getStatus();
            if (tmp_engine_status == Engine.ENGINE_INITIALIZING) {
                JOptionPane.showMessageDialog(this, "引擎正在初始化，请稍后。");
                return -1;  //引擎正在初始化，开始操作失败
            }

            //如果在引擎初始化完成但却不是就绪的情况下，只可能是上局的游戏没有结束
            if (tmp_engine_status != Engine.ENGINE_STANDBY){
                commandEngineToEndCurrentGame();
                while(engine.getStatus() != Engine.ENGINE_STANDBY){
                    try{
                        Thread.sleep(100);  //一直阻塞再此直到引擎重新就绪
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            //此时 engine 必然状态为 STANDBY
            commandEngineToStartNewGame();

            //初始化游戏数据
            curr_chessboard = new Chessboard(15);
            currOrder = 1;
            fifthCounter = 1;
            blackUsedTimeMillis = 0;
            whiteUsedTimeMillis = 0;
            playerPlayBlack = settingModel.playerIsBlack;

            //初始化控件的数据
            chessboardPanel.resetChessboard(curr_chessboard);
            gameControlPanel.setWhiteUsedTime(blackUsedTimeMillis);
            gameControlPanel.setBlackUsedTime(blackUsedTimeMillis);
            gameControlPanel.setBlackName(settingModel.blackName);
            gameControlPanel.setWhiteName(settingModel.whiteName);
            gameControlPanel.setBlackTotalTime(settingModel.blackTotalTime * 1000);
            gameControlPanel.setWhiteTotalTime(settingModel.whiteTotalTime * 1000);

            //历史记录清空
            historyRecorder.clearHistory();

            //置标志
            gamePlayingFlag = true;
            gamePausingFlag = false;
            thirdSwapedFlag = false;

            //如果引擎执黑先手则发出通知要求行棋
            if (!playerPlayBlack){
                commandEngineToMove(1);
                waitingEngineResponseFlag = true;
            }

            return 0;

        } else if (gamePausingFlag /* 如果游戏已经开始但在暂停状态则恢复游戏 */) {
            //置标志
            gamePausingFlag = false;

            //如果引擎还需要计算则通知引擎
            if (waitingEngineResponseFlag){
                commandEngineToComputeContinue();
            }

            return 0;

        }
        return -1;  //不是合法的情况，忽略开始操作
    }

    /**
     * 来自 gameControlPanel 的重新开始命令回调方法，只有当前在玩家回合才可以执行
     * @return 结果如果为 0 表示成功执行（开始或继续）
     */
    @Override
    public int restartCommand() {
        debugPrompt("restart command");
        if (gamePlayingFlag) {
            commandEngineToEndCurrentGame();
            gamePlayingFlag = false;
            return 0;
        }
        return -1;
    }

    /**
     * 来自 gameControlPanel 的设置命令回调方法
     */
    @Override
    public void settingCommand() {
        debugPrompt("setting command");
        if (!gamePlayingFlag) {
            settingDialog.setBounds(200, 100, 500, 550);
            settingDialog.setVisible(true);
        }
    }

    /**
     * 判断游戏是否结束，通过 GameResult 返回结果
     * @param chessboard
     * @return
     */
    public GameResult isGameEnd(Chessboard chessboard){
        GameResult gameResult = new GameResult();
        LinkedList<Point> adjustResult = null;

        char[][] board = transChessValueArray2CharArray(chessboard.getChessValueArray());
        int size = chessboard.getBoardSize();

        /* 判断黑方 连5 */
        if ((adjustResult = Adjuster.adjust5(board, 'b')) != null)
            return new GameResult(GameResult.Status.BLACK_FORBIDDEN, adjustResult);

        /* 判断白方 连5 */
        if ((adjustResult = Adjuster.adjust5(board, 'w')) != null)
            return new GameResult(GameResult.Status.WHITE_WIN, adjustResult);

        /* 判断黑方禁手 */
        if ((adjustResult = Adjuster.adjustForbidden(board, 'b',
            settingModel.overline, settingModel.doubleThree, settingModel.doubleFour
        )) != null)
            return new GameResult(GameResult.Status.BLACK_FORBIDDEN,adjustResult);

        return new GameResult(GameResult.Status.NOT_END, null);
    }

    /**
     * 工具方法，将棋盘的棋子落点数组改为字符数组
     *      ChessValue.BLACK -> 'b'
     *      ChessValue.WHITE -> 'w'
     *      ChessValue.EMPTY -> 'e'
     * @param board
     * @return
     */
    private char[][] transChessValueArray2CharArray(ChessValue[][] board){
        int size = board.length;
        char[][] results = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++){
                switch (board[i][j]){
                    case BLACK: results[i][j] = 'b'; break;
                    case WHITE: results[i][j] = 'w'; break;
                    default: results[i][j] = 'e'; break;
                }
            }
        }
//        System.out.println(Arrays.deepToString(results)); //debug
        return results;
    }

    /*(no-doc)
     * debug method
     */
    private static void debugPrompt(String message){
        System.out.println("[debug] " + "[time " + String.valueOf(System.currentTimeMillis()) + "] " + message);
    }


    /**
     * Recorder
     * 历史记录类，可以用于悔棋、重做等操作
     * PS:  一次回退/前进两步，一黑一白
     *      只有在玩家的回合才可以调用历史操作，否则不保证正确性
     *      记录所有历史状态，但不允许回退到第三手及第三手之前的记录
     */
    class HistoryRecorder {
        private boolean peekModeFlag = false;       //悔棋后此标记打开，暂不清空历史队列，下次记录时此标记关闭并清空当前局面后的废弃历史队列
        private LinkedList<StatusRecord> history = new LinkedList<StatusRecord>();
        private ListIterator<StatusRecord> iterator; // iterator always point to the pass of last element in history

        public HistoryRecorder() {
            iterator = history.listIterator(history.size());
        }

        public boolean canUndo(){
            if (GameFrame.this.playerPlayBlack){
                return iterator.nextIndex() > 6;
            } else {
                return iterator.nextIndex() > 5;
            }
        }

        public boolean canRedu(){
            return (history.size() - iterator.nextIndex()) >= 2;
        }

        /**
         * 记录当前局面数据
         */
        public void recordCurrentStatus(){
            if (peekModeFlag){
                while(iterator.hasNext()){
                    iterator.next();
                    iterator.remove();
                }
                peekModeFlag = false;
            }
            StatusRecord tmp = new StatusRecord(curr_chessboard,
                    blackUsedTimeMillis, whiteUsedTimeMillis, currOrder, playerPlayBlack, fifthCounter);
            iterator.add(tmp);
        }

        /**
         * 悔棋，回退两步，从 history 中选取上上个内容恢复局面数据
         * @return true 恢复成功 false 不存在上一个局面
         */
        public boolean goPreviousStatus(){
            if (canUndo()){
                peekModeFlag = true;
                iterator.previous();
                iterator.previous();
                StatusRecord tmp = iterator.previous();
                iterator.next();
                executeTravelHistory(tmp);
                return true;
            } else {
                return false;
            }
        }

        /**
         * 悔棋逆操作，重做两部，从 history 中选取下下个内容恢复局面数据
         * @return
         */
        public boolean goNextStatus() {
            if (canRedu()) {
                peekModeFlag = true;
                iterator.next();
                StatusRecord tmp = iterator.next();
                executeTravelHistory(tmp);
                return true;
            } else {
                return false;
            }
        }

        /**
         * 输入一个历史节点，将当前游戏状态返回到这个历史节点的局面数据
         * @param statusRecord 输入的历史记录
         */
        private void executeTravelHistory(StatusRecord statusRecord){
            GameFrame.this.curr_chessboard = new Chessboard(statusRecord.chessboard);
            GameFrame.this.blackUsedTimeMillis = statusRecord.blackUsedTimeMillis;
            GameFrame.this.whiteUsedTimeMillis = statusRecord.whiteUsedTimeMillis ;
            GameFrame.this.currOrder = statusRecord.currOrder;
            GameFrame.this.playerPlayBlack = statusRecord.playerPlayBlack;
            GameFrame.this.fifthCounter = statusRecord.fifthCounter;

            //让当前棋盘面板为新的棋盘数据提供视图
            GameFrame.this.chessboardPanel.resetChessboard(GameFrame.this.curr_chessboard);
            SwingUtilities.invokeLater(() -> GameFrame.this.chessboardPanel.repaint());
        }

        /**
         * 清空历史记录，一般开始新游戏
         */
        public void clearHistory(){
            history.clear();
            iterator = history.listIterator(0);
        }

        /**
         * 状态记录类
         * 用于保存当前的棋局状态，用于实现悔棋步骤
         */
        class StatusRecord {
            public Chessboard chessboard;
            public long blackUsedTimeMillis;
            public long whiteUsedTimeMillis;
            public int currOrder;
            public boolean playerPlayBlack;
            public int fifthCounter;

            StatusRecord(Chessboard chessboard, long blackUsedTimeMillis, long whiteUsedTimeMillis, int currOrder,
                         boolean playerPlayBlack, int fifthCounter){
                this.chessboard = new Chessboard(chessboard);
                this.blackUsedTimeMillis = blackUsedTimeMillis;
                this.whiteUsedTimeMillis = whiteUsedTimeMillis;
                this.currOrder = currOrder;
                this.playerPlayBlack = playerPlayBlack;
                this.fifthCounter = fifthCounter;
            }
        }
    }
}


class GameResult {
    public enum Status { BLACK_WIN, WHITE_WIN, BLACK_FORBIDDEN, NOT_END };
    public GameResult.Status status = Status.NOT_END;
    public LinkedList<Point> path = new LinkedList<Point>();
    public GameResult(){}
    public GameResult(GameResult.Status status, LinkedList<Point> path){
        this.status = status;
        this.path = path;
    }
}

class Adjuster{
    /**
     * 判断 target 在棋盘中哪里五连
     * @param board
     * @param target
     * @return 若发现五连则返回五连的相关点 否则只返回 null
     */
    static LinkedList<Point> adjust5(char[][] board, char target){
        //TODO
        return null;
    }

    /**
     * 判断 target 在棋盘中是否出现禁手
     * @param board
     * @param target
     * @param forbidOverline        是否判断长连禁手
     * @param forbidDoubleThree     是否判断三三禁手
     * @param forbidDoubleFour      是否判断四四禁手
     * @return 若发现禁手则返回禁手相关点 否则只返回 null
     */
    static LinkedList<Point> adjustForbidden(char[][] board, char target,
                                             boolean forbidOverline,
                                             boolean forbidDoubleThree,
                                             boolean forbidDoubleFour){
        //TODO
        return null;
    }
}