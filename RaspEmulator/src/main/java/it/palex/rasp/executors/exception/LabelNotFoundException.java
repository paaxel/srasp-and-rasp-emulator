package it.palex.rasp.executors.exception;

public class LabelNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4469836573883187146L;

	public LabelNotFoundException() {
        super();
    }

    public LabelNotFoundException(String message) {
        super(message);
    }
    
    public LabelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LabelNotFoundException(Throwable cause) {
        super(cause);
    }

}