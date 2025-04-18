package program.chatus.Model;

import java.time.LocalDateTime;

public class GroupMessage {
    private Long messageId;
    private Long groupId;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;

    public GroupMessage() {}

    public GroupMessage(Long messageId, Long groupId, Long senderId, String content, LocalDateTime timestamp) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
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
}
