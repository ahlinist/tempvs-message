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
            ParticipantRepository participantRepository
    ) {

        return args -> {

            Participant participant1 = new Participant();
            participant1.setId(3L);
            Participant participant2 = new Participant();
            participant2.setId(4L);

            participantRepository.save(participant1);
            participantRepository.save(participant2);

            Conversation conversation = new Conversation();
            conversation.addParticipant(participant1);
            conversation.addParticipant(participant2);

            Message message = new Message();
            message.setConversation(conversation);
            message.setSender(participant1);
            message.setText("asd");

            conversation.addMessage(message);

            Message2Recipient message2Recipient = new Message2Recipient();
            message2Recipient.setMessage(message);
            message2Recipient.setRecipient(participant2);

            messageRepository.save(message);

            System.out.println("Participants:");

            for (Message messageFromList : messageRepository.findAll()) {
                System.out.println("Message #" + messageFromList.getId());
                System.out.println("Message text: " + messageFromList.getText());
            }
        };
    }
}
