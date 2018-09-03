package club.tempvs.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    CommandLineRunner commandLineRunner;

    @RequestMapping("/ping")
    public String getPong() {
        return "pong!";
    }
}
