package club.tempvs.message;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class Application {

    private static final String CLOUDAMQP_URL = System.getenv("CLOUDAMQP_URL");
    private static final int CLOUDAMQP_CONNECTION_TIMEOUT = 30000;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ConnectionFactory amqpConnectionFactory() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        if (CLOUDAMQP_URL != null) {
            connectionFactory.setUri(CLOUDAMQP_URL);
            connectionFactory.setConnectionTimeout(CLOUDAMQP_CONNECTION_TIMEOUT);
        }

        return connectionFactory;
    }
}
