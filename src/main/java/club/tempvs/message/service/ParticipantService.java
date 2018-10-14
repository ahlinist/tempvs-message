package club.tempvs.message.service;

import club.tempvs.message.domain.Participant;

public interface ParticipantService {
    Participant createParticipant(Long id, String name);
    Participant getParticipant(Long id);
    Participant refreshParticipant(Long id, String name);
}
