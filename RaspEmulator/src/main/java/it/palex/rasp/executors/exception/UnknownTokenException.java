package it.palex.rasp.executors.exception;

public class UnknownTokenException extends RuntimeException {

	private static final long serialVersionUID = 3171294602250045909L;

	public UnknownTokenException() {
        super();
    }

    public UnknownTokenException(String message) {
        super(message);
    }
    
    public UnknownTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownTokenException(Throwable cause) {
        super(cause);
    }
    
}
