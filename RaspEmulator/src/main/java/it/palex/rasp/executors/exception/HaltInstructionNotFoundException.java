package it.palex.rasp.executors.exception;

public class HaltInstructionNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -6047434753038347486L;

	public HaltInstructionNotFoundException() {
        super();
    }

    public HaltInstructionNotFoundException(String message) {
        super(message);
    }
    
    public HaltInstructionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public HaltInstructionNotFoundException(Throwable cause) {
        super(cause);
    }
}
