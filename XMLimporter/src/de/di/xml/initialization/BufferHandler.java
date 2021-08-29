package de.di.xml.initialization;

import java.util.ArrayList;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Rahman
 */
public class BufferHandler extends org.apache.log4j.AppenderSkeleton {

        private ArrayList<LoggingEvent> events = new ArrayList<LoggingEvent>(30);

        public BufferHandler() {
            super();
            super.setName(getClass().getName());
        }

        @Override
        protected void append(LoggingEvent event) {
            events.add(event);
        }

        @Override
        public void close() {
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
        public java.util.List<LoggingEvent> getEvents() {
            return events;
        }
    }