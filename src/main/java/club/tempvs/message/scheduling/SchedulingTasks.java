package club.tempvs.message.scheduling;

import club.tempvs.message.amqp.ParticipantSynchronizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true")
public class SchedulingTasks {

    private final ParticipantSynchronizer participantSynchronizer;

    @Autowired
    public SchedulingTasks(ParticipantSynchronizer participantSynchronizer) {
        this.participantSynchronizer = participantSynchronizer;
    }

    //runs every hour
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refreshParticipants() {
        participantSynchronizer.execute();
    }
}
