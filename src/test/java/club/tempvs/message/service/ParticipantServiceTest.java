package club.tempvs.message.service;

import club.tempvs.message.dao.ParticipantRepository;
import club.tempvs.message.domain.Participant;
import club.tempvs.message.service.impl.ParticipantServiceImpl;
import club.tempvs.message.util.ObjectFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ParticipantServiceTest {

    private ParticipantService participantService;

    @Mock
    private Participant participant;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ParticipantRepository participantRepository;

    @Before
    public void setup() {
        this.participantService = new ParticipantServiceImpl(objectFactory, participantRepository);
    }

    @Test
    public void testCreateParticipant() {
        Long participantId = 1L;
        String name = "firstName lastName";
        String type = "type";
        String period = "period";

        when(objectFactory.getInstance(Participant.class, participantId, name, type, period)).thenReturn(participant);
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.createParticipant(participantId, name, type, period);

        verify(objectFactory).getInstance(Participant.class, participantId, name, type, period);
        verify(participantRepository).save(participant);
        verifyNoMoreInteractions(participant, objectFactory, participantRepository);

        assertTrue("A participant instance is returned", result instanceof Participant);
    }

    @Test
    public void testGetParticipant() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));

        Participant result = participantService.getParticipant(1L);

        verify(participantRepository).findById(1L);
        verifyNoMoreInteractions(participant, objectFactory, participantRepository);

        assertEquals("A participant instance is returned", result, participant);
    }

    @Test
    public void testGetParticipantForNoneFound() {
        Long participantId = 1L;

        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        Participant result = participantService.getParticipant(participantId);

        verify(participantRepository).findById(participantId);
        verifyNoMoreInteractions(participant, objectFactory, participantRepository);

        assertEquals("Null is returned", result, null);
    }

    @Test
    public void testRefreshParticipant() {
        Long participantId = 1L;
        String name = "firstName lastName";
        String type = "type";
        String period = "period";

        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.refreshParticipant(participantId, name, type, period);

        verify(participantRepository).findById(participantId);
        verify(participant).setName(name);
        verify(participant).setType(type);
        verify(participant).setPeriod(period);
        verify(participantRepository).save(participant);
        verifyNoMoreInteractions(participant, objectFactory, participantRepository);

        assertEquals("A participant instance is returned", result, participant);
    }

    @Test
    public void testRefreshParticipantForNonExistent() {
        Long participantId = 1L;
        String name = "firstName lastName";
        String type = "type";
        String period = "period";

        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());
        when(objectFactory.getInstance(Participant.class, participantId, name, type, period)).thenReturn(participant);
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.refreshParticipant(participantId, name, type, period);

        verify(participantRepository).findById(participantId);
        verify(objectFactory).getInstance(Participant.class, participantId, name, type, period);
        verify(participantRepository).save(participant);
        verifyNoMoreInteractions(participant, objectFactory, participantRepository);

        assertEquals("A participant instance is returned", result, participant);
    }
}
