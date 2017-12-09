package server;

import java.io.*;
import java.net.ServerSocket;
import java.util.Vector;

public class MyServer {
    public static final String AUTH = "/auth", AUTH_OK = "/authok", END = "/end", USERS_LIST = "/userslist", WHISPER = "/whisper";
    public static final int PORT = 8189;

    private ServerSocket serverSocket;
    private Vector<ClientHandler> clients;

    public MyServer(int port) {
        try {
            clients = new Vector<>();

            serverSocket = new ServerSocket(port);
            System.out.println("Server is on...");

            while (true) {
                Thread thread = new Thread(
                        new ClientHandler(this, serverSocket.accept()));
                thread.setDaemon(true);
                thread.start();
                System.out.println("Client connected");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isNickTaken(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick))
                return true;
        }

        return false;
    }

    public void broadcastUserList(){
        StringBuffer sb = new StringBuffer(USERS_LIST);
        for(ClientHandler c: clients){
            sb.append(" ");
            sb.append(c.getNick());
        }
        for(ClientHandler c: clients){
            c.sendMessage(sb.toString());
        }
    }
    public synchronized void sendMessageTo(ClientHandler from, String to, String msg){
        for(ClientHandler c: clients){
            if(c.getNick().equalsIgnoreCase(to)){
                c.sendMessage("from " + from.getNick() + ": " + msg);
                from.sendMessage("to " + to + " msg " + msg);
                break;
            }
        }
    }

    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastUserList();
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastUserList();
    }

    public synchronized void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }
}
