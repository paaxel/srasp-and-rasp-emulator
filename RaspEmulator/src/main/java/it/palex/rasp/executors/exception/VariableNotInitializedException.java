package it.palex.rasp.executors.exception;

public class VariableNotInitializedException extends RuntimeException {

	private static final long serialVersionUID = 4469836573883187146L;

	public VariableNotInitializedException() {
        super();
    }

    public VariableNotInitializedException(String message) {
        super(message);
    }
    
    public VariableNotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VariableNotInitializedException(Throwable cause) {
        super(cause);
    }

}
