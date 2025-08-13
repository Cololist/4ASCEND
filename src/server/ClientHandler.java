package server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameRoom room;
    private int playerId;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, GameRoom room, int playerId) throws IOException {
        this.socket = socket;
        this.room = room;
        this.playerId = playerId;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message){
        out.println(message);
    }

    @Override
    public void run() {
        room.clients.add(this);
        sendMessage("INIT:"+playerId);

        try{
            String line;
            while((line = in.readLine())!=null){
                if(line.startsWith("MOVE:")){
                    String[] data = line.substring("MOVE:".length()).split(",");
                    int x = Integer.parseInt(data[0]);
                    int y = Integer.parseInt(data[1]);
                    if(room.addMove(x, y, playerId)){
                        room.broadcastUpdate();
                    }
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
