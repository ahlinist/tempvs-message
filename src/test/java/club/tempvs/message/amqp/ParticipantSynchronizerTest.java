package club.tempvs.message.amqp;

import club.tempvs.message.amqp.impl.ParticipantSynchronizerImpl;
import club.tempvs.message.dto.ParticipantDto;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParticipantSynchronizerTest {

    private static final String MESSAGE_PARTICIPANT_AMQP_QUEUE = "message.participant";
    private static final int DELIVERY_TIMEOUT = 30000; //30 seconds

    private ParticipantSynchronizer participantSynchronizer;

    @Mock
    private ConnectionFactory amqpConnectionFactory;
    @Mock
    private ObjectMapper jacksonObjectMapper;
    @Mock
    private ParticipantService participantService;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private Connection connection;
    @Mock
    private Channel channel;
    @Mock
    private QueueingConsumer consumer;
    @Mock
    private QueueingConsumer.Delivery delivery;
    @Mock
    private ParticipantDto participantDto;

    @Before
    public void setup() {
        participantSynchronizer = new ParticipantSynchronizerImpl(
                jacksonObjectMapper, participantService, amqpConnectionFactory, objectFactory);
    }

    @Test
    public void testParticipantSynchronizer() throws Exception {
        String jsonBodyAsString = "{}";
        byte[] jsonBodyAsByteArray = jsonBodyAsString.getBytes();
        Long participantId = 1L;
        String participantName = "name";

        when(amqpConnectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        when(objectFactory.getInstance(QueueingConsumer.class, channel)).thenReturn(consumer);
        when(consumer.nextDelivery(DELIVERY_TIMEOUT)).thenReturn(delivery);
        when(delivery.getBody()).thenReturn(jsonBodyAsByteArray);
        when(jacksonObjectMapper.readValue(jsonBodyAsString, ParticipantDto.class)).thenReturn(participantDto);
        when(participantDto.getId()).thenReturn(participantId);
        when(participantDto.getName()).thenReturn(participantName);
        //throws an exception only to break the infinite loop
        when(participantService.refreshParticipant(participantId, participantName)).thenThrow(new RuntimeException());

        participantSynchronizer.execute();

        verify(amqpConnectionFactory).newConnection();
        verify(connection).createChannel();
        verify(channel).queueDeclare(MESSAGE_PARTICIPANT_AMQP_QUEUE, false, false, false, null);
        verify(objectFactory).getInstance(QueueingConsumer.class, channel);
        verify(channel).basicConsume(MESSAGE_PARTICIPANT_AMQP_QUEUE, true, consumer);
        verify(consumer).nextDelivery(DELIVERY_TIMEOUT);
        verify(delivery).getBody();
        verify(jacksonObjectMapper).readValue(jsonBodyAsString, ParticipantDto.class);
        verify(participantDto).getId();
        verify(participantDto).getName();
        verify(channel).close();
        verify(connection).close();
        verify(participantService).refreshParticipant(participantId, participantName);
        verifyNoMoreInteractions(amqpConnectionFactory, connection, channel, objectFactory, consumer, delivery,
                jacksonObjectMapper, participantDto, participantService);
    }
}
