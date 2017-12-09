package client;

import server.MyServer;

public class ClientMain {
    public static void main(String[] args) {
        new MyWindow(MyServer.PORT);
    }
}