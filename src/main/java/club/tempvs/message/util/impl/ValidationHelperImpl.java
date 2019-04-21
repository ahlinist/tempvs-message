package club.tempvs.message.util.impl;

import club.tempvs.message.domain.Message;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.dto.ErrorsDto;
import club.tempvs.message.util.ValidationHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ValidationHelperImpl implements ValidationHelper {

    private static final String TEXT_FIELD = "text";
    private static final String TEXT_EMPTY = "message.empty.text";
    private static final String PARTICIPANTS_FIELD = "participants";
    private static final String PARTICIPANTS_WRONG_SIZE = "conversation.participant.wrong.size";
    private static final String TYPE_MISMATCH = "conversation.participant.type.mismatch";
    private static final String PERIOD_MISMATCH = "conversation.participant.period.mismatch";
    private static final String PARTICIPANTS_EMPTY = "conversation.participant.empty";

    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    public ErrorsDto getErrors() {
        return new ErrorsDto();
    }

    public void addError(ErrorsDto errorsDto, String field, String messageKey) {
        addError(errorsDto, field, messageKey, null);
    }

    public void addError(ErrorsDto errorsDto, String field, String messageKey, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        String value = messageSource.getMessage(messageKey, args, messageKey, locale);
        errorsDto.addError(field, value);
    }

    public void processErrors(ErrorsDto errorsDto) {
        if (!errorsDto.getErrors().isEmpty()) {
            try {
                throw new IllegalArgumentException(objectMapper.writeValueAsString(errorsDto));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public void validateConversationCreation(Participant author, Set<Participant> receivers, Message message) {
        String type = author.getType();
        String period = author.getPeriod();
        String text = message.getText();
        ErrorsDto errorsDto = getErrors();

        if (receivers == null || receivers.isEmpty() || receivers.size() > 19) {
            addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE);
        }

        if (text == null || text.isEmpty()) {
            addError(errorsDto, TEXT_FIELD, TEXT_EMPTY);
        }

        if (receivers.stream().anyMatch(subject -> !subject.getType().equals(type))) {
            addError(errorsDto, PARTICIPANTS_FIELD, TYPE_MISMATCH);
        } else if (receivers.stream().anyMatch(subject -> !subject.getPeriod().equals(period))) {
            addError(errorsDto, PARTICIPANTS_FIELD, PERIOD_MISMATCH);
        }

        processErrors(errorsDto);
    }

    public void validateParticipantsAddition(Participant adder, Set<Participant> added, Set<Participant> initial) {
        ErrorsDto errorsDto = getErrors();

        if (added == null || added.isEmpty()) {
            addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_EMPTY);
        }


        if (initial.size() + added.size() > 20) {
            addError(errorsDto, PARTICIPANTS_FIELD, PARTICIPANTS_WRONG_SIZE);
        }

        String type = adder.getType();
        String period = adder.getPeriod();

        if (added.stream().anyMatch(subject -> !subject.getType().equals(type))) {
            addError(errorsDto, PARTICIPANTS_FIELD, TYPE_MISMATCH);
        } else if (added.stream().anyMatch(subject -> !subject.getPeriod().equals(period))) {
            addError(errorsDto, PARTICIPANTS_FIELD, PERIOD_MISMATCH);
        }

        processErrors(errorsDto);
    }
}
