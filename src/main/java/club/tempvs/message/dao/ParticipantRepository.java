package club.tempvs.message.dao;

import club.tempvs.message.domain.Participant;
import org.springframework.data.repository.CrudRepository;

public interface ParticipantRepository extends CrudRepository<Participant, Long> {
}
