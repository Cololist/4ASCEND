package client;

import java.io.*;
import java.net.*;

// 客户端的处理
public class GameClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public GameClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    // 向服务器端发送玩家选择的位置
    public void sendMove(int x, int y) {
        out.println("MOVE:" + x + "," + y);
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public static void main(String[] args) throws IOException {
        GameClient client = new GameClient("localhost", 8888);
        new GameGUI(client);
    }
}
