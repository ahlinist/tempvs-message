package club.tempvs.message.service;

import club.tempvs.message.domain.Participant;

public interface ParticipantService {
    Participant createParticipant(Long id);
    Participant getParticipant(Long id);
}
