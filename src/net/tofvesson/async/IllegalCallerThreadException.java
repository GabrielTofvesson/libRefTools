package net.tofvesson.async;

public class IllegalCallerThreadException extends RuntimeException {
    public IllegalCallerThreadException() {
    }

    public IllegalCallerThreadException(String message) {
        super(message);
    }

    public IllegalCallerThreadException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalCallerThreadException(Throwable cause) {
        super(cause);
    }
}
