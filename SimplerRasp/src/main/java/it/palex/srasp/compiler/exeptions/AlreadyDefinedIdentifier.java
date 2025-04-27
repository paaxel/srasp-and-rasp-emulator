package it.palex.srasp.compiler.exeptions;

public class AlreadyDefinedIdentifier extends RuntimeException {

	private static final long serialVersionUID = -6047434753038347486L;

	private int line;
	
	public AlreadyDefinedIdentifier(int line) {
        super();
        this.line = line;
	}
	
    
    public AlreadyDefinedIdentifier(String message, int line) {
        super(message);
        this.line = line;
    }
    
    public AlreadyDefinedIdentifier(String message, Throwable cause, int line) {
        super(message, cause);
        this.line = line;
    }

    public AlreadyDefinedIdentifier(Throwable cause, int line) {
        super(cause);
        this.line = line;
    }
    
    public String getMessage() {
        return super.getMessage()+". Line:"+this.line;
    }
    
    public String getMessageWithoutLine() {
        return super.getMessage();
    }
    
    public int getLine() {
    	return this.line;
    }
}
