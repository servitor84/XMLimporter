
package de.di.xml.initialization;

/**
     * Inner class for an exception during start up sequence.
     */
    public class StartUpException extends Exception {

        public StartUpException() {
            super();
        }

        public StartUpException(String msg) {
            super(msg);
        }
    }
