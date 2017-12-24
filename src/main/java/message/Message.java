package message;

import java.io.Serializable;

/**
 * Created by Vlad on 24.12.2017.
 */

public class Message implements Serializable {

    private MessageType messageType;
    private String message;

    public Message(){}

    public Message(MessageType messageType, String message) {
        this.messageType = messageType;
        this.message = message;
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
