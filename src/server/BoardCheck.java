package server;

import java.util.*;
import java.awt.Point;

// 检查棋盘连线状态
public class BoardCheck {
    private int rows, cols;
    private int[][] board;

    public BoardCheck(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        board = new int[rows][cols];
    }

    public int getPointValue(int row, int col) {
        return board[row][col];
    }

    public void setPointValue(int row, int col, int value) {
        board[row][col] = value;
    }

    public boolean isEmptyPoint(int row, int col) {
        return board[row][col] == 0;
    }

    public String toStringData(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<rows; i++){
            for(int j = 0; j<cols; j++){
                sb.append(board[i][j]).append(",");
            }
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    // 判断连通的逻辑
    public List<Point> getStraightPoints(int row, int col, int playerId){
        List<Point> result = new ArrayList<>();
        boolean[][] added = new boolean[rows][cols];

        // 判断四个方向
        int[][] directions = {
                {0, 1}, // 右
                {1, 0}, // 下
                {1, 1}, // 右下
                {1, -1} // 左下
        };

        for(int[] direction : directions){
            List<Point> line = new ArrayList<>();
            line.add(new Point(row, col));

            int dx = direction[0];
            int dy = direction[1];

            // 正方向延伸
            int nx = row + dx;
            int ny = col + dy;
            while(isInBoard(nx, ny)&&board[nx][ny]==playerId){
                line.add(new Point(nx, ny));
                nx = nx + dx;
                ny = ny + dy;
            }
            // 反方向延伸
            nx = row - dx;
            ny = col - dy;
            while(isInBoard(nx, ny)&&board[nx][ny]==playerId){
                line.add(new Point(nx, ny));
                nx = nx - dx;
                ny = ny - dy;
            }

            if(line.size()>=4){
                for(Point p : line){
                    if(!added[p.x][p.y]){
                        result.add(p);
                        added[p.x][p.y] = true;
                    }
                }
            }
        }
        return result;
    }

    public boolean isInBoard(int x, int y){
        return x>=0 && x<rows && y>=0 && y<cols;
    }
}
