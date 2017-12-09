package client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static server.MyServer.*;

public class MyWindow extends JFrame {
    //views
    private JTextArea allMessagesTextArea;
    private JTextField inputMessageTextField, userNameTextField;
    private JPasswordField passwordField;
    private JList<String> usersList;

    //adaptive panels
    private JPanel sendMessagePanel, loginPanel;
    private boolean isAuthorized;
    private JScrollPane usersListScrollPane;

    //connection
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    //data
    private String[] usersOnline;

    public MyWindow (int port) {
        //set up the frame
        setTitle ( "Messages" );
        setBounds ( 300 ,     300 ,     500 ,     400 );
        setLayout ( new BorderLayout());
        addWindowListener(new MyWindowClosingAdapter());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //declare layouts and elements ------------------------------
        //messages text area
        allMessagesTextArea = new JTextArea();
        allMessagesTextArea.setEditable(false);
        allMessagesTextArea.setLineWrap(true);
        JScrollPane allMessagesScrollPane = new JScrollPane(allMessagesTextArea);

        //show who's online
        usersOnline = new String[0];
        usersList = new JList<>(usersOnline);
        usersList.setLayoutOrientation(JList.VERTICAL);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersList.addListSelectionListener(new UserSelectedListener());
        usersList.addFocusListener(new usersListFocusListener());
        usersListScrollPane = new JScrollPane(usersList);
        usersListScrollPane.setPreferredSize(new Dimension(150, 200));

        //send message panel
        sendMessagePanel = new JPanel(new BorderLayout());
        inputMessageTextField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputMessageTextField.addActionListener((ActionEvent e) -> sendMessage());
        sendButton.addActionListener((ActionEvent e) -> sendMessage());
        sendMessagePanel.add(inputMessageTextField, BorderLayout.CENTER);
        sendMessagePanel.add(sendButton, BorderLayout.EAST);

        //login panel
        loginPanel = new JPanel(new GridLayout(1, 3));
        userNameTextField = new JTextField();
        passwordField = new JPasswordField();
        passwordField.addActionListener((ActionEvent e)->sendAuthorizationRequest());
        JButton authorizeButton = new JButton("log in");
        authorizeButton.addActionListener((ActionEvent e)->sendAuthorizationRequest());
        loginPanel.add(userNameTextField);
        loginPanel.add(passwordField);
        loginPanel.add(authorizeButton);

        //locate elements in the frame
        add(allMessagesScrollPane, BorderLayout.CENTER);
        add(sendMessagePanel, BorderLayout.SOUTH);
        add(loginPanel, BorderLayout.NORTH);
        add(usersListScrollPane, BorderLayout.EAST);
        //------------------------- end declaring and adding elements

        //when a new window is created, user is not yet logged in by default
        setAuthorized(false);

        //try to set up connection
        try {
            socket = new Socket("5.128.36.163", port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            allMessagesTextArea.append("Couldn't connect to server");
            e.printStackTrace();
        }

        //chat in a separate thread
        Thread thread = new Thread(this::chat);
        thread.setDaemon(true);
        thread.start();

        //show everything
        setVisible(true);
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        //show log in panel if not yet logged in
        loginPanel.setVisible(!isAuthorized);
        //show send message panel if already logged in
        sendMessagePanel.setVisible(isAuthorized);
        usersListScrollPane.setVisible(isAuthorized);
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    private void send(String msg) {
        try {
            outputStream.writeUTF(msg);
            System.out.println("wrote " + msg);
            outputStream.flush();

            //end chat if user decides to
            if (msg.equals(END)) {
                setAuthorized(false);
                System.exit(0);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void sendAuthorizationRequest() {
        if (!socket.isClosed()) {
            String name = userNameTextField.getText();
            String password = passwordField.getText();
            send(AUTH + " " + name + " " + password);
        }
    }

    public void sendMessage() {
        String msg = inputMessageTextField.getText();
        inputMessageTextField.setText("");
        inputMessageTextField.grabFocus();

        if (msg.length() > 0) {
            send(msg);
        }
    }

    private void chat() {
        try {
            while (true) {
                //receive answer
                String msg = inputStream.readUTF();
                if (msg.startsWith(AUTH_OK)) {
                    //in case of successful authorization
                    //show the send message panel
                    setAuthorized(true);
                    break;
                } else if (msg.startsWith(END)) {
                    showText("Connection denied");
                    break;
                }

                //in case we couldn't log in, print error message for user
                showText(msg);
            }

            //read incoming messages
            while (isAuthorized) {
                String msg = inputStream.readUTF();

                if (msg.startsWith(USERS_LIST)) {
                    msg = msg.substring(USERS_LIST.length());
                    usersOnline = msg.split("\\s");
                    usersList.setListData(usersOnline);
                } else {
                    showText(msg);
                }
            }
        } catch (IOException e) {
            allMessagesTextArea.append("Connection difficulties");
            setAuthorized(false);
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showText(String msg) {
        allMessagesTextArea.append(msg + "\n");
        allMessagesTextArea.setCaretPosition(allMessagesTextArea.getDocument().getLength());
    }

    private class usersListFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) { }

        @Override
        public void focusLost(FocusEvent e) {
            usersList.clearSelection();
        }
    }

    private class UserSelectedListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = usersList.getSelectedIndex();

            if (index != -1) {
                inputMessageTextField.setText(WHISPER + " " + usersOnline[index] + " ");
            }
            inputMessageTextField.grabFocus();
        }
    }

    public class MyWindowClosingAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);

            send(END);
            //just remove authorization and the thread with chat messages will end
            setAuthorized(false);
        }
    }

}