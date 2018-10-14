package club.tempvs.message.amqp.impl;

import club.tempvs.message.amqp.ParticipantSynchronizer;
import club.tempvs.message.dto.ParticipantDto;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ParticipantSynchronizerImpl extends AbstractAMQPConnector implements ParticipantSynchronizer {

    private static final String MESSAGE_PARTICIPANT_AMQP_QUEUE = "message.participant";

    private ObjectMapper jacksonObjectMapper;
    private final ParticipantService participantService;

    @Autowired
    public ParticipantSynchronizerImpl(ObjectMapper jacksonObjectMapper,
                                       ParticipantService participantService,
                                       ConnectionFactory amqpConnectionFactory,
                                       ObjectFactory objectFactory) {
        super(amqpConnectionFactory, objectFactory);
        this.participantService = participantService;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    public void execute() {
        super.execute(jsonMessage -> refreshParticipant(jsonMessage));
    }

    protected String getQueue() {
        return MESSAGE_PARTICIPANT_AMQP_QUEUE;
    }

    private void refreshParticipant(String json) {
        try {
            ParticipantDto participantDto = jacksonObjectMapper.readValue(json, ParticipantDto.class);
            participantService.refreshParticipant(participantDto.getId(), participantDto.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
