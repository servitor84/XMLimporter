package de.di.xml.importers;

/**
 *
 * @author alex
 */
public class IllegalExpressionException extends Exception {

    /**
     * Creates a new instance of <code>IllegalExpressionException</code> without detail message.
     */
    public IllegalExpressionException() {
    }


    /**
     * Constructs an instance of <code>IllegalExpressionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public IllegalExpressionException(String msg) {
        super(msg);
    }
}
