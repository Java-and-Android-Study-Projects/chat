package server;

import authorization.BaseAuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static server.MyServer.*;

public class ClientHandler implements Runnable{
    private Storage storage;
    public static long MAX_LOGIN_TIME_MILLIS = 120000;

    private String nick;
    private BaseAuthService authService;
    private boolean isAuthorized;
    private MyServer server;
    Socket socket;

    DataInputStream in;
    DataOutputStream out;

    public ClientHandler(MyServer server, Socket socket, Storage storage) {
        this.storage = storage;
        this.socket = socket;
        this.server = server;
        authService = new BaseAuthService();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Couldn't build in/out data streams");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long startedTime = System.currentTimeMillis();

        try{
            Thread timer = new Thread(() -> {
                while (true) {
                    long timePassed = System.currentTimeMillis() - startedTime;
                    if (MAX_LOGIN_TIME_MILLIS < timePassed && !isAuthorized) {
                        sendMessage("Waited to long...");
                        sendMessage(END);
                        break;
                    }
                }
            });
            timer.start();

            //authorize client
            while(!socket.isClosed()){
                String str = in.readUTF();
                if(str.startsWith(AUTH)){
                    String[] elements = str.split("\\s");
                    String login = elements[1];
                    String pass = elements[2];
                    nick = authService.getNickByLoginPass(login, pass);

                    if (server.isNickTaken(nick)) {

                        sendMessage(nick + " is already logged in");

                    } else if(authService.correctPassword(login, pass)){
                        isAuthorized = true;
                        sendMessage(AUTH_OK);
                        server.subscribe(this);
                        System.out.println("Client " + nick + " is authorized");

                        //show previous messages
                        sendMessage(storage.getAllMessages().toString());
                        break;

                    } else {
                        sendMessage("Wrong password or username");
                    }

                } else sendMessage("Need to authorize first");
            }

            //chat
            while(true){
                String msg = in.readUTF();
                if(msg.equalsIgnoreCase(END)) {
                    break;
                }

                if(msg.startsWith(WHISPER)){
                    String[] string = msg.split("\\s", 3);
                    String nameTo = string[1];
//                    String message = str.substring(4 + nameTo.length());
                    String message = string[2];
                    server.sendMessageTo(this, nameTo, message);
                }else{
                    storage.save(System.currentTimeMillis(), nick, msg);
                    msg = nick + ": " + msg;
                    server.broadcast(msg);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            System.out.println("Client disconnected");
            server.unsubscribe(this);
            isAuthorized = false;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }

    public void sendMessage(String msg){
        try{
            out.writeUTF(msg);
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
