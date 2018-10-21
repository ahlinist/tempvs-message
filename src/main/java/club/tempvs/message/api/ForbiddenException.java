package club.tempvs.message.api;

/**
 * An exception to represent the 403 Http status.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {

    }

    public ForbiddenException(String message) {
        super(message);
    }
}
