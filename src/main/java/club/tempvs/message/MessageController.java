package club.tempvs.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    CommandLineRunner commandLineRunner;

    @RequestMapping("/")
    public String index() throws Exception {
        commandLineRunner.run();
        return "Hello world!";
    }
}
