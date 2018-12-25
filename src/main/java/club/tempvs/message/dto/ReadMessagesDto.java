package club.tempvs.message.dto;

import java.util.List;

public class ReadMessagesDto {

    private List<Long> messageIds;

    public ReadMessagesDto() {

    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }
}
