package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;
import lombok.Data;

import java.util.List;

import static java.util.stream.Collectors.*;

@Data
public class GetConversationsDto {

    private List<ConversationDtoBean> conversations;

    public GetConversationsDto(List<Conversation> conversations, Participant self, String zoneId) {
        this.conversations = conversations.stream()
                .map(conversation -> new ConversationDtoBean(conversation, self, zoneId))
                .collect(toList());
    }
}
