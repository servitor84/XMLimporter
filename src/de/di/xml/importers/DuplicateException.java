package de.di.xml.importers;

/**
 *
 * @author A. Sopicki
 */
public class DuplicateException extends ImportException {

    /**
     * Constructs an instance of <code>DuplicateException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DuplicateException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>DuplicateException</code> with the specified detail message
     * and the cause of the exception.
     * @param msg the detail message.
     * @param cause the cause of the exception
     */
    public DuplicateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
