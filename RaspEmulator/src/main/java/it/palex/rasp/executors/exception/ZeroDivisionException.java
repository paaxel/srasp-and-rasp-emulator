package it.palex.rasp.executors.exception;

public class ZeroDivisionException extends RuntimeException {

	private static final long serialVersionUID = 4913337638692148091L;

	public ZeroDivisionException() {
        super();
    }

    public ZeroDivisionException(String message) {
        super(message);
    }
    
    public ZeroDivisionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZeroDivisionException(Throwable cause) {
        super(cause);
    }

}
