package server;

import java.io.*;
import java.net.*;

public class GameServer {
    public static void main(String[] args) throws IOException {
        BGM bgm = new BGM();
        bgm.play("/sounds/NIMBUS.wav");

        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("服务器启动，等待两位玩家连接...");
        GameRoom room = new GameRoom();

        for(int i=1;i<=2;i++){
            Socket client = serverSocket.accept();
            ClientHandler handler = new ClientHandler(client, room, i);
            new Thread(handler).start();
            System.out.println("玩家"+i+"已连接");

        }
        System.out.println("Fight!");
    }
}
