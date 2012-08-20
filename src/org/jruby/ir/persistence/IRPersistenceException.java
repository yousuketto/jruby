package org.jruby.ir.persistence;

public class IRPersistenceException extends Exception {
	
	private static final long serialVersionUID = -3867320224908765830L;

	public IRPersistenceException(String message) {
		super(message);
	}
	
	public IRPersistenceException() {
		super();
	}
	
	public IRPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public IRPersistenceException(Throwable cause) {
        super(cause);
    }
}
