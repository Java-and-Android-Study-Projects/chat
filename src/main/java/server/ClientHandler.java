package server;

import authorization.BaseAuthService;
import message.Message;
import message.SystemCommand;

import java.io.*;
import java.net.Socket;

import static message.SystemCommand.*;

public class ClientHandler implements Runnable {
    private Storage storage;
    public static long MAX_LOGIN_TIME_MILLIS = 120000;

    private String nick;
    private BaseAuthService authService;
    private boolean isAuthorized;
    private MyServer server;
    private Socket socket;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(MyServer server, Socket socket, Storage storage) {
        this.storage = storage;
        this.socket = socket;
        this.server = server;
        authService = new BaseAuthService();
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Couldn't build in/out data streams");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long startedTime = System.currentTimeMillis();

        try {
            Thread timer = new Thread(() -> {
                while (true) {
                    long timePassed = System.currentTimeMillis() - startedTime;
                    if (MAX_LOGIN_TIME_MILLIS < timePassed && !isAuthorized) {
                        sendMessage(new Message("Waited too long...", SystemCommand.END));
                        break;
                    }
                }
            });
            timer.start();

            Message message;

            //authorize client
            while (!socket.isClosed()) {
                try {
                    message = (Message) in.readObject();

                    if (message.getSystemCommand().equals(AUTH)) {
                        String login = message.getLogin();
                        String pass = message.getPassword();

                        // TODO: 1/15/18 check for null
                        nick = authService.getNickByLoginPass(login, pass);

                        //check if the user has already logged in
                        if (server.isNickTaken(nick)) {
                            sendMessage(new Message(nick + " is already logged in", NOTIFICATION));

                        }

                        //check if the password is correct
                        else if (authService.isPasswordCorrect(login, pass)) {
                            isAuthorized = true;
                            sendMessage(new Message(AUTH_OK));
                            server.subscribe(this);
                            System.out.println("Client " + nick + " is authorized");

                            //show previous messages
                            sendMessage(new Message(storage.getAllMessages().toString(), MESSAGE_HISTORY));
                            break;

                        }

                        //password is not correct or no such login
                        else {
                            sendMessage(new Message("Wrong password or username", NOTIFICATION));
                        }

                    }

                    //if not auth message
                    else sendMessage(new Message("Need to authorize first", NOTIFICATION));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            //chat
            while (true) {
                try {
                    message = (Message) in.readObject();

                    if (message.getSystemCommand() != null) {
                        if (message.getSystemCommand().equals(END)) {
                            break;
                        }

                        if (message.getSystemCommand().equals(WHISPER)) {
                            server.sendMessageTo(message);
                        }
                    } else {
                        if (message.getMessage() != null)
                            storage.save(System.currentTimeMillis(), nick, message.getMessage());
                        server.broadcast(message);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client disconnected");
            server.unsubscribe(this);
            isAuthorized = false;

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
