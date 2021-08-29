package de.di.xml.importers;

/**
 *
 * @author A. Sopicki
 */
public class ImportException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs an instance of <code>ImportException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ImportException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ImportException</code> with the specified detail message
     * and the cause of the exception.
     * @param msg the detail message.
     * @param cause the cause of the exception
     */
    public ImportException(String msg, Throwable cause) {
        super(msg,cause);
    }

}
