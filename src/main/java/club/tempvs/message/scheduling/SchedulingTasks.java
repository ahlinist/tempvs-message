package club.tempvs.message.scheduling;

import club.tempvs.message.amqp.ParticipantSynchronizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true")
public class SchedulingTasks {

    private final ParticipantSynchronizer participantSynchronizer;

    //runs every 30 seconds
    @Scheduled(fixedRate = 30 * 1000)
    public void refreshParticipants() {
        participantSynchronizer.execute();
    }
}
