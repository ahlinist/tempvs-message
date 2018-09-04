package club.tempvs.message;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MessageController {

    @RequestMapping("/ping")
    public String getPong() {
        return "pong!";
    }
}
