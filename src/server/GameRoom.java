package server;

import java.util.*;
import java.awt.Point;

// 控制游戏状态
public class GameRoom {
    private final int rows = 10;
    private final int cols = 10;
    public final BoardCheck board = new BoardCheck(rows, cols);
    public final List<ClientHandler> clients = new ArrayList<>();
    public int currentPlayerId = 1;
    public int[] hp = {5, 5};
    public boolean[][] enhancementBoard = new boolean[rows][cols];

    public List<Point> attackP1 = new ArrayList<>();
    public List<Point> attackP2 = new ArrayList<>();

    public synchronized boolean addMove(int row, int col, int playerId){
        if(playerId!=currentPlayerId){
            return false;
        }

        Point movePoint = new Point(row, col);

        // 是否占据对方已经成为攻击棋子的位置
        if(!board.isEmptyPoint(row,col)){
            if(playerId == 1&&attackP2.contains(movePoint)){
                attackP2.remove(movePoint);
            }
            else if(playerId == 2&&attackP1.contains(movePoint)){
                attackP1.remove(movePoint);
            }
            else{
                return false;
            }
        }

        board.setPointValue(row, col, playerId);

        List<Point> currAttack = board.getStraightPoints(row, col, playerId);

        // 保存新的攻击棋子
        if(currAttack.size()>=4){
            if(playerId==1){
                attackP1.clear();
                attackP1.addAll(currAttack);
            }
            else{
                attackP2.clear();
                attackP2.addAll(currAttack);
            }
        }
        else{// 本轮未形成攻击，检查对方是否攻击
            if(playerId==1&&!attackP2.isEmpty()){
                hp[0] -= (attackP2.size()+countEnhancement(attackP2));
                clearAttackPoints(attackP2);
                attackP2.clear();
                randomEnhancement();
            }
            if(playerId==2&&!attackP1.isEmpty()){
                hp[1] -= (attackP1.size()+countEnhancement(attackP1));
                clearAttackPoints(attackP1);
                attackP1.clear();
                randomEnhancement();
            }
        }

        // 双方都有攻击时相互抵消
        if(!attackP1.isEmpty()&&!attackP2.isEmpty()){
            int diff = (attackP1.size()+countEnhancement(attackP1))-(attackP2.size()+countEnhancement(attackP2));
            hp[0] -= Math.max(-diff,0);
            hp[1] -= Math.max(diff,0);
            clearAttackPoints(attackP1);
            clearAttackPoints(attackP2);
            attackP1.clear();
            attackP2.clear();
            randomEnhancement();// 每次攻击结束生成增强地块
        }

        currentPlayerId = 3 - currentPlayerId;
        return true;
    }

    // 清空棋盘上已经攻击的棋子
    private void clearAttackPoints(List<Point> attackPoints){
        for(Point p:attackPoints){
            board.setPointValue(p.x, p.y, 0);
            enhancementBoard[p.x][p.y] = false;
        }
    }

    // 随机生成增强的地块
    private void randomEnhancement(){
        Random rand = new Random();
        int times = 0;
        int count = 0;
        while(count<3){
            times++;
            if(times>100){
                break;
            }
            int ex = rand.nextInt(rows);
            int ey = rand.nextInt(cols);

            // 不生成在有棋子的地块或已生长的地块
            if(board.getPointValue(ex, ey) == 0&&!enhancementBoard[ex][ey]){
                enhancementBoard[ex][ey] = true;
                count++;
            }
        }
    }

    private int countEnhancement(List<Point> attack){
        int count = 0;
        for(Point p:attack){
            if(enhancementBoard[p.x][p.y]){
                count++;
            }
        }
        return count;
    }

    private String toStringEnhancement(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<rows; i++){
            for(int j=0; j<cols; j++){
                int value = enhancementBoard[i][j] ? 1 : 0;
                sb.append(value).append(",");
            }
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    // 向客户端发送信息更新
    public synchronized void broadcastUpdate(){
        String boardData = board.toStringData();
        String enhancementData = toStringEnhancement();
        String attackCounts = attackP1.size() + "," + attackP2.size();
        StringBuilder attackPositions = new StringBuilder();// P1和P2各自的攻击坐标

        for(Point p:attackP1){
            attackPositions.append(p.x).append(",").append(p.y).append(",1|");
        }
        for(Point p:attackP2){
            attackPositions.append(p.x).append(",").append(p.y).append(",2|");
        }

        for(ClientHandler c:clients){
            // 行为:棋盘数据:增强地块数据:血量:攻击棋子数:攻击棋子坐标
            c.sendMessage("UPDATE:"+boardData+":"+enhancementData+":"+hp[0]+","+hp[1]+":"+attackCounts+":"+attackPositions);
            c.sendMessage("TURN:"+currentPlayerId);
        }

        if(hp[0]<=0||hp[1]<=0){
            if(hp[0]<=0){
                clients.get(0).sendMessage("END:失败");
                clients.get(1).sendMessage("END:胜利");
            }
            else{
                clients.get(1).sendMessage("END:失败");
                clients.get(0).sendMessage("END:胜利");
            }
        }
    }

}
