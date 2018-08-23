package club.tempvs.message;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @RequestMapping("/")
    public String index() {
        return "Hello world!";
    }
}
