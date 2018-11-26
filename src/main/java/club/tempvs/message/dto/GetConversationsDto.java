package club.tempvs.message.dto;

import club.tempvs.message.domain.Conversation;
import club.tempvs.message.domain.Participant;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.*;

public class GetConversationsDto {

    private List<ConversationDtoBean> conversations;

    public GetConversationsDto() {

    }

    public GetConversationsDto(List<Conversation> conversations, Participant self, String zoneId, Locale locale) {
        this.conversations = conversations.stream()
                .map(conversation -> new ConversationDtoBean(conversation, self, zoneId, locale)).collect(toList());
    }

    public List<ConversationDtoBean> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationDtoBean> conversations) {
        this.conversations = conversations;
    }
}
