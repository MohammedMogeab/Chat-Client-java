package program.chatus.Model;

import java.time.LocalDateTime;

public class ChatMessage {
    private String sender;
    private String receiver;
    private String message;
    private String content;
    private LocalDateTime timestamp;
    private String status;

    public ChatMessage() {
    }

    public ChatMessage(String sender, String receiver, String message, String content, LocalDateTime timestamp, String status) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
