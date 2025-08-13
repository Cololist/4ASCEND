package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class GameGUI extends JFrame {
    private BoardPanel boardPanel;
    private GameClient client;
    private int playerId;
    private int currentTurnId = 1;// 现在轮到的玩家
    private int hp1 = 5, hp2 = 5;
    private int attackCountP1 = 0;
    private int attackCountP2 = 0;
    private JTextArea leftArea;// 玩家1
    private JTextArea rightArea;// 玩家2

    public GameGUI(GameClient client) {
        this.client = client;
        this.setTitle("4ASCEND");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 关闭窗口时退出程序
        this.setSize(900, 640);// 窗口大小
        this.setLayout(new BorderLayout());// 设置边界布局

        this.setResizable(false);

        getContentPane().setBackground(new Color(245, 245, 255));

        // 显示状态栏
        leftArea = new JTextArea("玩家1等待中...");
        rightArea = new JTextArea("玩家2等待中...");
        leftArea.setFont(new Font("STCaiyun", Font.BOLD, 20));
        rightArea.setFont(new Font("STCaiyun", Font.BOLD, 20));
        leftArea.setPreferredSize(new Dimension(138, 200));
        rightArea.setPreferredSize(new Dimension(138, 200));
        leftArea.setLineWrap(true);// 自动换行
        rightArea.setLineWrap(true);
        leftArea.setWrapStyleWord(true);// 不截断单词
        rightArea.setWrapStyleWord(true);
        leftArea.setBackground(new Color(255, 255, 240));
        rightArea.setBackground(new Color(240, 255, 255));
        leftArea.setBorder(BorderFactory.createLineBorder(new Color(200, 150, 100), 3, true));
        rightArea.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 3, true));
        leftArea.setEditable(false);
        rightArea.setEditable(false);

        // 图片头像
        ImageIcon leftIcon = new ImageIcon(getClass().getResource("/images/Noelle.png"));
        ImageIcon rightIcon = new ImageIcon(getClass().getResource("/images/ishia.png"));
        JLabel leftHead = new JLabel(leftIcon);
        JLabel rightHead = new JLabel(rightIcon);
        leftHead.setBackground(new Color(255, 255, 240));
        rightHead.setBackground(new Color(240, 255, 255));
        leftHead.setOpaque(true);
        rightHead.setOpaque(true);
        leftHead.setHorizontalAlignment(JLabel.CENTER);
        rightHead.setHorizontalAlignment(JLabel.CENTER);

        // 状态栏和头像合并放置
        JPanel leftStatus = new JPanel();
        JPanel rightStatus = new JPanel();
        leftStatus.setLayout(new BorderLayout());
        rightStatus.setLayout(new BorderLayout());
        leftStatus.add(new JScrollPane(leftArea), BorderLayout.CENTER);
        rightStatus.add(new JScrollPane(rightArea), BorderLayout.CENTER);
        leftStatus.add(leftHead, BorderLayout.SOUTH);
        rightStatus.add(rightHead, BorderLayout.SOUTH);

        add(leftStatus, BorderLayout.WEST);
        add(rightStatus, BorderLayout.EAST);


        // 初始化棋盘
        boardPanel = new BoardPanel(10, 10 ,60);
        boardPanel.setClient(client);
        add(boardPanel, BorderLayout.CENTER);

        setVisible(true);
        startListening();
    }

    private void updateStatus(){
        StringBuilder left = new StringBuilder();
        StringBuilder right = new StringBuilder();

        String p1Name = (playerId==1?"--> 玩家 1\n":"玩家1\n");
        String p2Name = (playerId==2?"--> 玩家 2\n":"玩家2\n");

        left.append(p1Name);
        left.append("HP: ").append(hp1).append("\n");
        if(attackCountP1>=4){
            left.append("攻击数: ").append(attackCountP1).append("\n");
        }

        right.append(p2Name);
        right.append("HP: ").append(hp2).append("\n");
        if(attackCountP2>=4){
            right.append("攻击数: ").append(attackCountP2).append("\n");
        }

        if(currentTurnId==1){
            left.append("现在轮到你!").append("\n");
        }
        else{
            right.append("现在轮到你!").append("\n");
        }

        leftArea.setText(left.toString());
        rightArea.setText(right.toString());
    }

    // 新进程监听服务器信息，不阻塞
    private void startListening() {
        Thread listener = new Thread(() -> {
            try{
                String message;
                while((message = client.readMessage())!=null) {
                    if(message.startsWith("INIT:")) {
                        // 分配玩家编号
                        playerId = Integer.parseInt(message.split(":")[1]);
                        boardPanel.setPlayerId(playerId);
                        updateStatus();
                    }
                    else if(message.startsWith("TURN:")) {
                        // 更新轮次
                        currentTurnId = Integer.parseInt(message.split(":")[1]);
                        boardPanel.setCurrentTurnId(currentTurnId);
                        updateStatus();
                    }
                    else if(message.startsWith("UPDATE:")) {
                        String[] data = message.split(":");
                        String enhanceData = data[2];

                        // 更新棋盘状态和血量
                        String boardData = data[1];
                        String[] hp = data[3].split(",");
                        hp1 = Integer.parseInt(hp[0]);
                        hp2 = Integer.parseInt(hp[1]);

                        // 解析攻击
                        attackCountP1 = 0;
                        attackCountP2 = 0;
                        if(data.length>4&&!data[4].isEmpty()){
                            String[] attackCount = data[4].split(",");
                            // 玩家1的攻击
                            attackCountP1 = Integer.parseInt(attackCount[0]);
                            // 玩家2的攻击
                            attackCountP2 = Integer.parseInt(attackCount[1]);
                        }
                        boardPanel.clearAttack();

                        // 新的攻击标记
                        if(data.length>5&&!data[5].isEmpty()){
                            String[] attacks = data[5].split("\\|");
                            for(String attack: attacks){
                                String[] position = attack.split(",");
                                int r = Integer.parseInt(position[0]);
                                int c = Integer.parseInt(position[1]);
                                int attackerId = Integer.parseInt(position[2]);
                                boardPanel.addAttack(new Point(r,c), attackerId);
                            }
                        }

                        boardPanel.updateBoard(boardData);
                        boardPanel.updateEnhanceBoard(enhanceData);
                        updateStatus();
                    }
                    else if(message.startsWith("END:")) {
                        // 游戏结束
                        String result = message.split(":")[1];
                        JOptionPane.showMessageDialog(this, "游戏结束，你"+result);

                        break;
                    }
                }
            }
            catch(IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "连接断开，游戏已终止。");
                System.exit(0);
            }
        });

        listener.start();
    }
}
