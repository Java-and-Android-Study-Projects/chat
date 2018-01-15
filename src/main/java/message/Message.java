package message;

import com.sun.scenario.effect.impl.prism.PrImage;

import java.io.Serializable;

/**
 * Created by Vlad on 24.12.2017.
 */

public class Message implements Serializable {

    private MessageType messageType;
    private SystemCommand systemCommand;
    private String message;
    private String from, to;
    private String login, password;

    public Message(){}

    public Message(String message, String from, String to) {
        this.messageType = MessageType.COMMON;
        this.systemCommand = SystemCommand.WHISPER;
        this.message = message;
        this.from = from;
        this.to = to;
    }

    public Message(String message, SystemCommand systemCommand) {
        this.messageType = MessageType.SYSTEM;
        this.systemCommand = systemCommand;
        this.message = message;
    }

    public Message(SystemCommand systemCommand) {
        this.messageType = MessageType.SYSTEM;
        this.systemCommand = systemCommand;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public SystemCommand getSystemCommand() {
        return systemCommand;
    }

    public void setSystemCommand(SystemCommand systemCommand) {
        this.systemCommand = systemCommand;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
