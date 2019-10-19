/*
This Panle support the view for chessboard
 */

package com.chessboard;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class ChessboardPanel extends JPanel implements MouseListener, MouseMotionListener {
    private static final String[] alphabetSeq = {"A","B","C","D",
        "E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    private static final String[] numberSeq = {"1","2","3","4","5","6","7","8","9",
        "10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26"};

    private Chessboard chessboard = null;
    private ChessboardListener chessboardListener = null;

    // data for paintComponent
    private int padding_min, padding_x, padding_y;  // data for layout
    private int board_size, cell_width, chess_radius; // data for layout
    private int font_size;  //data for font
    private Point origin = new Point(-1,-1);
    private Point cursor = new Point(-1,-1);    //cursor should be hilighted.

    /**
     * chessboard panel constructor
     *
     * @param chessboardSize size of chessboard, which will always be square
     */
    public ChessboardPanel(int chessboardSize, ChessboardListener listener){
        this(new Chessboard(chessboardSize), listener);
    }

    public ChessboardPanel(Chessboard chessboard, ChessboardListener listener){
        resetChessboard(chessboard);
        addMouseListener(this);
        addMouseMotionListener(this);
        chessboardListener = listener;
    }

    /**
     * replace chessboard by a new chessboard
     */
    public void resetChessboard(Chessboard chessboard){
        this.chessboard = chessboard;
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g.create();

        /* get current size of this panel */
        int curr_width = this.getSize().width;
        int curr_height = this.getSize().height;

        /* compute every detail's size */
        board_size = chessboard.getBoardSize();
        cell_width = (int)(Math.min(curr_width, curr_height) * 0.8 / (board_size - 1));
        chess_radius = (int)(cell_width * 0.40);


        /* compute padding */
        padding_min = (int)(Math.min(curr_width, curr_height) * 0.1);
        if (curr_width >= curr_height) {
            padding_y = padding_min;
            padding_x = (int) ((curr_width - curr_height) / 2 + padding_min);
        } else {
            padding_x = padding_min;
            padding_y = (int) ((curr_height - curr_width) / 2 + padding_min);
        }
        origin.setLocation(padding_x, padding_y);

        /* draw background */
        g2d.setColor(Color.lightGray);
        g2d.fillRect(0,0,curr_width,curr_height);

        /* draw alphabet and number */
        drawAlphabetAndNumber(g2d);

        /* draw the lines of chessboard*/
        drawLines(g2d);

        /* draw stars */
        drawStars(g2d);

        /* draw chess pieces */
        drawChess(g2d);

        /* draw cursor */
        drawCursor(g2d);

        g2d.dispose();
    }


    private void drawLines(Graphics2D g2d){
        int draw_line_x_start = origin.x;
        int draw_line_x_end = origin.x + (board_size - 1) * cell_width;
        int draw_line_y = origin.y + cell_width * (board_size - 1);
        for (int i = 0; i < board_size; i++) {
            if (i == cursor.y) g2d.setColor(Color.orange); else g2d.setColor(Color.darkGray);
            g2d.drawLine(draw_line_x_start, draw_line_y, draw_line_x_end, draw_line_y);
            draw_line_y -= cell_width;
        }
        int draw_line_y_start = origin.y;
        int draw_line_y_end = origin.y + (board_size - 1) * cell_width;
        int draw_line_x = origin.x;
        for (int i = 0; i < board_size; i++){
            if (i == cursor.x) g2d.setColor(Color.orange); else g2d.setColor(Color.darkGray);
            g2d.drawLine(draw_line_x, draw_line_y_start, draw_line_x, draw_line_y_end);
            draw_line_x += cell_width;
        }
    }

    private void drawChess(Graphics2D g2d){
        int draw_chess_x_start = origin.x;
        int draw_chess_y_start = origin.y + cell_width * (board_size - 1);

        /* draw chess */
        for (int i = 0; i < board_size; i++){
            for (int j = 0; j < board_size; j++){
                switch (chessboard.getChessValue(i,j)){
                    case BLACK:
                        g2d.setColor(Color.black);
                        break;
                    case WHITE:
                        g2d.setColor(Color.white);
                        break;
                    default:
                            continue;
                }
                g2d.fillOval(draw_chess_x_start + i * cell_width - chess_radius,
                        draw_chess_y_start - j * cell_width - chess_radius,
                        chess_radius * 2, chess_radius * 2);
            }
        }

        /* draw order */
        g2d.setFont(new Font(null,Font.BOLD, (int)(cell_width * 0.4)));
        int sixth_cell_width = (int)(cell_width / 6);
        int order_tmp;
        for (int i = 0; i < board_size; i++){
            for (int j = 0; j < board_size; j++) {
                switch (chessboard.getChessValue(i,j)){
                    case BLACK:
                        g2d.setColor(Color.lightGray);
                        break;
                    case WHITE:
                        g2d.setColor(Color.darkGray);
                        break;
                    default:
                        continue;
                }
                if ((order_tmp = chessboard.getChessOrder(i, j)) > 0) {
                    if (order_tmp >= 100)
                        g2d.drawString(String.valueOf(order_tmp),
                            origin.x + cell_width * i - 2 * sixth_cell_width,
                            origin.y + cell_width * (board_size - j - 1) + sixth_cell_width);
                    else if (order_tmp >= 10)
                        g2d.drawString(String.valueOf(order_tmp),
                                origin.x + cell_width * i - 1 * sixth_cell_width,
                                origin.y + cell_width * (board_size - j - 1) + sixth_cell_width);
                    else
                        g2d.drawString(String.valueOf(order_tmp),
                                origin.x + cell_width * i,
                                origin.y + cell_width * (board_size - j - 1) + sixth_cell_width);
                }
            }
        }
    }


    private void drawCursor(Graphics2D g2d){
        if (cursor.x >= 0 && cursor.y >= 0) {
            g2d.setColor(Color.red);
            g2d.drawRect(origin.x + cursor.x * cell_width - (int) (cell_width * 0.5),
                    origin.y + cell_width * (board_size - cursor.y - 1) - (int) (cell_width * 0.5),
                    cell_width, cell_width);
        }
    }


    private void drawAlphabetAndNumber(Graphics2D g2d){
        g2d.setFont(new Font(null,Font.BOLD, (int)(cell_width * 0.5)));
        int quarter_cell_width = (int)(cell_width * 0.25);
        for (int i = 0; i < board_size; i++){
            if (i == cursor.x) g2d.setColor(Color.orange); else g2d.setColor(Color.black);
            g2d.drawString(alphabetSeq[i], origin.x + cell_width * i - quarter_cell_width,
                    origin.y + cell_width * board_size);
        }
        for (int i = 0; i < board_size; i++){
            if (i == cursor.y) g2d.setColor(Color.orange); else g2d.setColor(Color.black);
            g2d.drawString(numberSeq[i], origin.x - cell_width,
                    origin.y + cell_width * (board_size - i - 1) + quarter_cell_width);
        }

    }


    private void drawStars(Graphics2D g2d){
        int[] star_x = { origin.x + cell_width * 3, origin.x + cell_width * 7, origin.x + cell_width * 11 };
        int[] star_y = { origin.y + cell_width * 3, origin.y + cell_width * 7, origin.y + cell_width * 11 };
        int star_radius = (int)(cell_width / 8);
        g2d.setColor(Color.black);
        g2d.fillOval(star_x[0] - star_radius, star_y[0] - star_radius, star_radius * 2, star_radius * 2);
        g2d.fillOval(star_x[2] - star_radius, star_y[0] - star_radius, star_radius * 2, star_radius * 2);
        g2d.fillOval(star_x[1] - star_radius, star_y[1] - star_radius, star_radius * 2, star_radius * 2);
        g2d.fillOval(star_x[0] - star_radius, star_y[2] - star_radius, star_radius * 2, star_radius * 2);
        g2d.fillOval(star_x[2] - star_radius, star_y[2] - star_radius, star_radius * 2, star_radius * 2);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (cursor.x >= 0 && cursor.y >= 0)
            chessboardListener.selectChessPosition(cursor.x, cursor.y);
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        cursor.setLocation(-1,-1);
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point curr_position = utilAdjustMouseLocation(e);
        if (curr_position != null)
            cursor.setLocation(curr_position);
        else
            cursor.setLocation(-1,-1);
        repaint();
    }

    /**
     * utility for find out which cell is mouse locate at
     * @param e
     * @return return null if there is not located
     */
    private Point utilAdjustMouseLocation(MouseEvent e){
        int mouse_x = e.getPoint().x;
        int mouse_y = e.getPoint().y;
        int result_x = 0;
        int result_y = 0;
        boolean is_cell_located_x = false;
        boolean is_cell_located_y = false;

        // find result of x
        for (int i = 0; i < board_size; i++){
            if (Math.abs(mouse_x - origin.x - i * cell_width) < chess_radius){
                result_x = i;
                is_cell_located_x = true;
                break;
            }
        }
        // find result of y
        for (int i = 0; i < board_size; i++){
            if (Math.abs(mouse_y - origin.y - i * cell_width) < chess_radius){
                result_y = i;
                result_y = board_size - result_y  - 1;
                is_cell_located_y = true;
                break;
            }
        }

        if (is_cell_located_x && is_cell_located_y)
            return new Point(result_x,result_y);
        else
            return null;
    }
}
