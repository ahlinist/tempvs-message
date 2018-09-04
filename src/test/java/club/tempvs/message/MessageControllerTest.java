package club.tempvs.message;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MessageControllerTest {

    private MessageController messageController;

    @Before
    public void setup() {
        messageController = new MessageController();
    }

    @Test
    public void testGetPong() {
        assertEquals("getPong() method returns 'pong!' string", "pong!", messageController.getPong());
    }
}
