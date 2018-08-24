package club.tempvs.message;

import club.tempvs.message.dao.ParticipantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ParticipantRepository participantRepository) {
        return args -> {

            Participant participant = new Participant();
            participant.setId(3L);
            participantRepository.save(participant);

            System.out.println("Participants:");

            for (Participant participant1 : participantRepository.findAll()) {
                System.out.println("Participant #" + participant1.getId());
            }
        };
    }
}
