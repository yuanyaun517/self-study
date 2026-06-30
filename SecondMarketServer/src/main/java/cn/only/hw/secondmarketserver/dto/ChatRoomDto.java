package cn.only.hw.secondmarketserver.dto;

import java.io.Serializable;
import java.util.List;

public class ChatRoomDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private ChatSessionDto session;
    private List<ChatMessageDto> messages;

    public ChatSessionDto getSession() {
        return session;
    }

    public void setSession(ChatSessionDto session) {
        this.session = session;
    }

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}
