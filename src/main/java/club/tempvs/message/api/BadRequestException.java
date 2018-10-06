package club.tempvs.message.api;

/**
 * An exception to represent the 400 Http status.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException() {

    }

    public BadRequestException(String message) {
        super(message);
    }
}
