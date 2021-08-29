/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.di.xml.handlers;

/**
 *
 * @author alex
 */
public class HandlerException extends Exception {

    /**
     * Creates a new instance of <code>HandlerException</code> without detail message.
     */
    public HandlerException() {
      super();
    }


    /**
     * Constructs an instance of <code>HandlerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public HandlerException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>HandlerException</code> with the specified detail message
     * and the Throwable t (cause of the error).
     * @param msg the detail message.
     * @param t the cause of the error
     */
    public HandlerException(String msg, Throwable t) {
        super(msg, t);
    }
}
