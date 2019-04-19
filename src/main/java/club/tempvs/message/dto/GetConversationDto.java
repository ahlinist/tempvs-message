package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import lombok.Data;

import java.util.*;

import static java.util.stream.Collectors.*;

@Data
public class GetConversationDto {

    private Long id;
    private String type;
    private String name;
    private ParticipantDto admin;
    private Set<ParticipantDto> participants;
    private List<MessageDtoBean> messages;

    public GetConversationDto(Conversation conversation, List<Message> messages, Participant self, String zoneId) {
        Participant admin = conversation.getAdmin();
        Collections.reverse(messages);

        this.id = conversation.getId();
        this.type = String.valueOf(conversation.getType());
        this.name = conversation.getName();
        this.admin = admin != null ? new ParticipantDto(admin) : null;
        this.participants = conversation.getParticipants().stream().map(ParticipantDto::new).collect(toSet());
        this.messages = messages.stream().map(message -> new MessageDtoBean(message, self, zoneId)).collect(toList());
    }
}
