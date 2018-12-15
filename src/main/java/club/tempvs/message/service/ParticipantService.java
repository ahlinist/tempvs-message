package club.tempvs.message.service;

import club.tempvs.message.domain.Participant;

public interface ParticipantService {
    Participant getParticipant(Long id);
    Participant createParticipant(Long id, String name, String type, String period);
    Participant refreshParticipant(Long id, String name, String type, String period);
}
