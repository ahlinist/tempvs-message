package club.tempvs.message;

import club.tempvs.message.dao.MessageRepository;
import club.tempvs.message.dao.ParticipantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            ConversationService conversationService,
            MessageRepository messageRepository,
            ParticipantService participantService,
            ParticipantRepository participantRepository
    ) {

        return args -> {

            Participant sender = participantService.createParticipant(20L);
            Participant receiver = participantService.createParticipant(21L);

            List<Participant> receivers = new ArrayList<>();
            receivers.add(receiver);

            conversationService.createConversation(sender, receivers, "message text", "conversation name");

            System.out.println("Participants:");

            for (Participant participantsFromList : participantRepository.findAll()) {
                System.out.println("Participant #" + participantsFromList.getId());
            }

            System.out.println("Messages:");

            for (Message messageFromList : messageRepository.findAll()) {
                System.out.println("Message #" + messageFromList.getId());
                System.out.println("Message text: " + messageFromList.getText());
            }
        };
    }
}
