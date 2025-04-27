package it.palex.srasp.compiler;


public class UniqueIdentifierManager {

	private static UniqueIdentifierManager manager;
	
	public static final int STACK_MAX_LENGTH = 50000;
	private Integer identifierCounter;
	
	private UniqueIdentifierManager() {
		this.reset();
	} 
	 
    public static UniqueIdentifierManager getInstance() {
        if (manager == null) {
        	manager = new UniqueIdentifierManager();
        }
        return manager;
    }
    
    public void reset() {
		this.identifierCounter = STACK_MAX_LENGTH;
    }
    
    /**
     * 
     * @return a new unique identifier
     */
    public String getNewUniqueId() {
    	this.identifierCounter++;
    	return this.identifierCounter.toString();
    }

}
