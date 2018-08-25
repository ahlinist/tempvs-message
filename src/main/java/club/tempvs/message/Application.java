package club.tempvs.message;

import club.tempvs.message.dao.MessageRepository;
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
    public CommandLineRunner commandLineRunner(
            MessageRepository messageRepository,
            ParticipantService participantService,
            ParticipantRepository participantRepository
    ) {

        return args -> {

            Participant participant1 = participantService.createParticipant(10L);
            Participant participant2 = participantService.createParticipant(12L);

            Conversation conversation = new Conversation();
            conversation.addParticipant(participant1);
            conversation.addParticipant(participant2);

            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(participant1);
            message.setText("qwe");

            conversation.addMessage(message);

            Message2Recipient message2Recipient = new Message2Recipient();
            message2Recipient.setMessage(message);
            message2Recipient.setRecipient(participant2);

            messageRepository.save(message);

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
