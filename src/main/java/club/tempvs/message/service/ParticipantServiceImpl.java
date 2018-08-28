package club.tempvs.message.service;

import club.tempvs.message.Participant;
import club.tempvs.message.ParticipantService;
import club.tempvs.message.dao.ParticipantRepository;
import club.tempvs.message.util.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final ObjectFactory objectFactory;
    private final ParticipantRepository participantRepository;

    @Autowired
    public ParticipantServiceImpl (ObjectFactory objectFactory, ParticipantRepository participantRepository) {
        this.objectFactory = objectFactory;
        this.participantRepository = participantRepository;
    }

    public Participant createParticipant(Long id) {
        Participant participant = objectFactory.getInstance(Participant.class);
        participant.setId(id);
        return participantRepository.save(participant);
    }

    public Participant getParticipant(Long id) {
        return participantRepository.findById(id).orElseGet(() -> createParticipant(id));
    }
}
