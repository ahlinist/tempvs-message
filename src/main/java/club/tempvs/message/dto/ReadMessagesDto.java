package club.tempvs.message.dto;

import club.tempvs.message.api.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class ReadMessagesDto {

    private ParticipantDto participant;
    private List<Long> messageIds;

    public ReadMessagesDto() {

    }

    public ParticipantDto getParticipant() {
        return participant;
    }

    public void setParticipant(ParticipantDto participant) {
        this.participant = participant;
    }

    public List<Long> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<Long> messageIds) {
        this.messageIds = messageIds;
    }

    public void validate() {
        List<String> errors = new ArrayList<>();

        if (this.participant == null) {
            errors.add("Participant is missing.");
        }

        if (this.messageIds == null || this.messageIds.isEmpty()) {
            errors.add("Messages ids are missing.");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException(String.join("\n", errors));
        }
    }
}
