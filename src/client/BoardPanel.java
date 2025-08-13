package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class BoardPanel extends JPanel {
    private int rows, cols, cellSize;
    private int[][] board;
    private boolean[][] enhanceBoard;
    private int playerId = 1;
    private int currentTurnId = 1;
    private GameClient client;

    private Map<Point, Integer> attackPoints = new HashMap<>();

    public BoardPanel(int rows, int cols, int cellSize) {
        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.board = new int[rows][cols];
        this.enhanceBoard = new boolean[rows][cols];

        setPreferredSize(new Dimension(rows * cellSize, cols * cellSize));

        // 鼠标点击监听落子
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(playerId != currentTurnId) {
                    return;
                }
                int x = e.getX() / cellSize;
                int y = e.getY() / cellSize;
                // 避免点击边界外
                if (x >= 0 && x < cols && y >= 0 && y < rows) {
                    client.sendMove(y, x);
                    Sound.play("/sounds/put.wav");// 点击音效
                }
            }
        });
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setCurrentTurnId(int currentTurnId) {
        this.currentTurnId = currentTurnId;
        repaint();
    }

    public void updateBoard(String data) {
        String[] cells = data.split(",");
        for(int i = 0; i < rows * cols; i++) {
            int r = i / cols;
            int c = i % cols;
            board[r][c] = Integer.parseInt(cells[i]);
        }
        repaint();
    }

    public void updateEnhanceBoard(String enhanceData) {
        String[] cells = enhanceData.split(",");
        for(int i = 0; i < rows * cols; i++) {
            int r = i / cols;
            int c = i % cols;
            enhanceBoard[r][c] = cells[i].equals("1");
        }
        repaint();
    }

    public void addAttack(Point p, int playerId) {
        attackPoints.put(p, playerId);
    }

    public void clearAttack(){
        attackPoints.clear();
    }

    // 初始化和repaint时调用
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 背景
        if(!attackPoints.isEmpty()) {
            g.setColor(new Color(220, 220, 220));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        else{
            GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 240),
                    getWidth(), getHeight(), new Color(240, 255, 255));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 绘制网络
        g.setColor(Color.GRAY);
        for(int i = 0; i <= rows; i++) {
            g.drawLine(0, i*cellSize, cols*cellSize, i*cellSize);
        }
        for(int j = 0; j <= cols; j++) {
            g.drawLine(j*cellSize, 0, j*cellSize, rows*cellSize);
        }

        // 绘制棋子
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                int value = board[r][c];

                // 增强地块
                if(enhanceBoard[r][c]){
                    g.setColor(new Color(200, 255, 150));
                    g.fillRect(c*cellSize+1, r*cellSize+1, cellSize-2, cellSize-2);
                }

                // 棋盘有棋子
                if(value == 1||value == 2) {
                    Point p = new Point(r, c);
                    boolean isAttack = attackPoints.containsKey(p);// 该棋子是否正在攻击

                    if(isAttack){
                        g.setColor(value == 1?new Color(0, 0, 0, 100):new Color(255, 255, 255, 100));
                    }
                    else{
                        g.setColor(value == 1?Color.BLACK:Color.WHITE);
                    }

                    g.fillOval(c * cellSize + 8, r * cellSize + 8, cellSize - 16, cellSize - 16);

                    // 给白子加黑框
                    if(value == 2){
                        g.setColor(Color.BLACK);
                        g.drawOval(c * cellSize + 8, r * cellSize + 8, cellSize - 16, cellSize - 16);
                    }
                }
            }
        }
    }
}
