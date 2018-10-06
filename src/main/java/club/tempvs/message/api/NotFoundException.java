package club.tempvs.message.api;

/**
 * An exception to represent the 404 Http status.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {

    }

    public NotFoundException(String message) {
        super(message);
    }
}
