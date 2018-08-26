package club.tempvs.message;

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
            ParticipantService participantService
    ) {

        return args -> {

            Participant sender = participantService.getParticipant(20L);
            Participant receiver = participantService.getParticipant(21L);

            if (sender == null) {
                sender = participantService.createParticipant(20L);
            }

            if (receiver == null) {
                receiver = participantService.createParticipant(21L);
            }

            List<Participant> receivers = new ArrayList<>();
            receivers.add(receiver);

            conversationService.createConversation(sender, receivers, "message text", "conversation name");

            Participant participant = participantService.getParticipant(20L);
            List<Conversation> conversations = participant.getConversations();

            System.out.println("Participant #20 has " + conversations.size() + " converstations.");

            for (Conversation conversation : conversations) {
                System.out.println("Conversation #" + conversation.getId());
                List<Message> messages = conversation.getMessages();

                for (Message message : messages) {
                    System.out.println("Message #" + message.getId());
                    System.out.println("Message text: " + message.getText());
                    System.out.println("Message author #: " + message.getSender().getId());
                    System.out.println("----------------------");
                }
            }

            participant = participantService.getParticipant(21L);
            conversations = participant.getConversations();

            System.out.println("Participant #21 has " + conversations.size() + " converstations.");

            for (Conversation conversation : conversations) {
                System.out.println("Conversation #" + conversation.getId());
                List<Message> messages = conversation.getMessages();

                for (Message message : messages) {
                    System.out.println("Message #" + message.getId());
                    System.out.println("Message text: " + message.getText());
                    System.out.println("Message author #: " + message.getSender().getId());
                    System.out.println("----------------------");
                }
            }
        };
    }
}
