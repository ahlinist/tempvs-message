package club.tempvs.message.service.impl;

import club.tempvs.message.domain.Participant;
import club.tempvs.message.dao.ParticipantRepository;
import club.tempvs.message.service.ParticipantService;
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

    public Participant createParticipant(Long id, String name) {
        Participant participant = objectFactory.getInstance(Participant.class, id, name);
        return participantRepository.save(participant);
    }

    public Participant getParticipant(Long id) {
        return participantRepository.findById(id).orElse(null);
    }

    public Participant refreshParticipant(Long id, String name) {
        Participant participant = getParticipant(id);

        if (participant != null) {
            participant.setName(name);
        } else {
            participant = objectFactory.getInstance(Participant.class, id, name);
        }

        return participantRepository.save(participant);
    }
}
