package club.tempvs.message.service;

import club.tempvs.message.domain.Participant;

import java.util.Set;

public interface ParticipantService {
    Participant getParticipant(Long id);
    Set<Participant> getParticipants(Set<Long> ids);
    Participant createParticipant(Long id, String name, String type, String period);
    Participant refreshParticipant(Long id, String name, String type, String period);
}
