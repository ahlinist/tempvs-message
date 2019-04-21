package club.tempvs.message.util;

import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;

import java.util.Set;

public interface ValidationHelper {

    ErrorsDto getErrors();

    void addError(ErrorsDto errorsDto, String field, String messageKey);

    void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args);

    void processErrors(ErrorsDto errorsDto);

    void validateConversationCreation(Participant author, Set<Participant> receivers, Message message);

    void validateParticipantsAddition(Participant adder, Set<Participant> added, Set<Participant> initial);
}
