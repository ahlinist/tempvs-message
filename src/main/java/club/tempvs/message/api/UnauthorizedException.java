package club.tempvs.message.api;

/**
 * An exception to represent the 401 Http status.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {

    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
