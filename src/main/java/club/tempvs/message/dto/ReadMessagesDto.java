package club.tempvs.message.dto;

import java.util.List;

public class ReadMessagesDto {

    private List<Long> messages;

    public ReadMessagesDto() {

    }

    public List<Long> getMessages() {
        return messages;
    }

    public void setMessages(List<Long> messages) {
        this.messages = messages;
    }
}
