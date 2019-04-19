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

import java.util.*;

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

    @Test(expected = IllegalStateException.class)
    public void testGetParticipantForNullInput() {
        participantService.getParticipant(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetParticipantForNoneFound() {
        Long participantId = 1L;

        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        participantService.getParticipant(participantId);
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

    @Test(expected = IllegalStateException.class)
    public void testRefreshParticipantForNonExistent() {
        Long participantId = 1L;
        String name = "firstName lastName";
        String type = "type";
        String period = "period";

        when(participantRepository.findById(participantId)).thenReturn(Optional.empty());

        participantService.refreshParticipant(participantId, name, type, period);
    }

    @Test
    public void testGetParticipants() {
        Set<Long> participantIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        Set<Participant> participantSet = new HashSet<>(Arrays.asList(participant));
        List<Participant> participantList = Arrays.asList(participant);

        when(participantRepository.findAllById(participantIds)).thenReturn(participantList);

        Set<Participant> result = participantService.getParticipants(participantIds);

        verify(participantRepository).findAllById(participantIds);
        verifyNoMoreInteractions(participant, participantRepository);

        assertEquals("A set of participants is returned", participantSet, result);
    }
}
