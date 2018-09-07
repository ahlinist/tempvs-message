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
        when(objectFactory.getInstance(Participant.class)).thenReturn(participant);
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.createParticipant(1L);

        verify(objectFactory).getInstance(Participant.class);
        verify(participant).setId(1L);
        verify(participantRepository).save(participant);
        verifyNoMoreInteractions(participant);
        verifyNoMoreInteractions(objectFactory);
        verifyNoMoreInteractions(participantRepository);

        assertTrue("A participant instance is returned", result instanceof Participant);
    }

    @Test
    public void testGetParticipant() {
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));

        Participant result = participantService.getParticipant(1L);

        verify(participantRepository).findById(1L);
        verifyNoMoreInteractions(participantRepository);

        assertEquals("A participant instance is returned", result, participant);
    }

    @Test
    public void testGetParticipantForNoneFound() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());
        when(objectFactory.getInstance(Participant.class)).thenReturn(participant);
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = participantService.getParticipant(1L);

        verify(participantRepository).findById(1L);
        verify(objectFactory).getInstance(Participant.class);
        verify(participant).setId(1L);
        verify(participantRepository).save(participant);
        verifyNoMoreInteractions(participant);
        verifyNoMoreInteractions(objectFactory);
        verifyNoMoreInteractions(participantRepository);

        assertEquals("A participant instance is returned", result, participant);
    }
}
