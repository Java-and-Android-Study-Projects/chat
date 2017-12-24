package server;

import message.Message;
import message.SystemCommand;

import java.io.*;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class MyServer {
//    public static final String AUTH = "/auth", AUTH_OK = "/authok", END = "/end",
//                USERS_LIST = "/userslist", WHISPER = "/whisper";
    public static final int PORT = 8189;

    private ServerSocket serverSocket;
    private ConcurrentHashMap<String, ClientHandler> clients;

    public MyServer(int port) {
        Storage storage = new Storage();

        try {
            clients = new ConcurrentHashMap<>();

            serverSocket = new ServerSocket(port);
            System.out.println("Server is on...");

            while (true) {
                Thread thread = new Thread(
                        new ClientHandler(this, serverSocket.accept(), storage));
                thread.setDaemon(true);
                thread.start();
                System.out.println("Client connected");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                storage.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isNickTaken(String nick) {
        return clients.containsKey(nick);
    }

    public void broadcastUserList(){
        StringBuffer sb = new StringBuffer();
        clients.forEachKey(clients.size(), s -> {
            sb.append(" ");
            sb.append(s);
        });

        StringBuffer sb = new StringBuffer();
        for(ClientHandler c: clients){
            sb.append(" ");
            sb.append(c.getNick());
        }
        Message msg = new Message(sb.toString(), SystemCommand.USERS_LIST);
        for(ClientHandler c: clients){
            c.sendMessage(msg);
        }
    }
    public synchronized void sendMessageTo(Message msg){
        for(ClientHandler c: clients){
            if(c.getNick().equalsIgnoreCase(msg.getTo())){
                c.sendMessage(msg);
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
