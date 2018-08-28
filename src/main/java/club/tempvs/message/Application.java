package club.tempvs.message;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.*;

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

            long senderId = 27L;
            long receiverId = 29L;

            Participant sender = participantService.getParticipant(senderId);
            Participant receiver = participantService.getParticipant(receiverId);
            
            Set<Participant> receivers = new LinkedHashSet<>();
            receivers.add(receiver);

            conversationService.createConversation(sender, receivers, "message text", "conversation name");

            Participant participant = participantService.getParticipant(senderId);
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

            participant = participantService.getParticipant(receiverId);
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

            long conversationId = 3L;
            Participant newParticipant = participantService.createParticipant(666L);

            Conversation conversation = conversationService.getConversation(conversationId);

            System.out.println("Conversation #69 has " + conversation.getParticipants().size() + " participants");
            System.out.println("Adding one more...");
            Set<Participant> newParticipants = new LinkedHashSet<>();
            newParticipants.add(newParticipant);
            conversationService.addParticipants(conversation, newParticipants);
            System.out.println("Now conversation #69 has " + conversation.getParticipants().size() + " participants");
        };
    }
}
