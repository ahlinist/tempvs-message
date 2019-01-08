package club.tempvs.message.service.impl;

import club.tempvs.message.domain.Participant;
import club.tempvs.message.dao.ParticipantRepository;
import club.tempvs.message.service.ParticipantService;
import club.tempvs.message.util.ObjectFactory;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ObjectFactory objectFactory;
    private final ParticipantRepository participantRepository;

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Participant createParticipant(Long id, String name, String type, String period) {
        Participant participant = objectFactory.getInstance(Participant.class, id, name, type, period);
        return participantRepository.save(participant);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Participant getParticipant(Long id) {
        return participantRepository.findById(id).orElse(null);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Set<Participant> getParticipants(Set<Long> ids) {
        List<Participant> participants = participantRepository.findAllById(ids);
        return new HashSet<>(participants);
    }

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public Participant refreshParticipant(Long id, String name, String type, String period) {
        Participant participant = getParticipant(id);

        if (participant != null) {
            participant.setName(name);
            participant.setType(type);
            participant.setPeriod(period);
        } else {
            participant = objectFactory.getInstance(Participant.class, id, name, type, period);
        }

        return participantRepository.save(participant);
    }
}
