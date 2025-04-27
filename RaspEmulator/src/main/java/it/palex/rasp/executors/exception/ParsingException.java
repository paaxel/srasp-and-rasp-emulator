package it.palex.rasp.executors.exception;

public class ParsingException extends RuntimeException {

	private static final long serialVersionUID = 4469836573883187146L;

	public ParsingException() {
        super();
    }

    public ParsingException(String message) {
        super(message);
    }
    
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }


}
