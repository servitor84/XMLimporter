package de.di.xml.initialization;

import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.DailyRollingFileAppender;

/**
 *
 * @author Rahman
 */
public class Logging {
    
     /**
     * Start logging to the log file     *
     * @param logger
     * @param handler
     * @param logHandler
     * @param settings
     * @throws de.di.xml.initialization.StartUpException
     * @throws de.arivato.erpconnect.ERPConnect.StartUpException
     */
    public static void start(org.apache.log4j.Logger logger, DailyRollingFileAppender handler, BufferHandler logHandler, java.util.Properties settings) throws StartUpException {
       
        File logDir = null;

        try {
            logDir = new File(settings.getProperty("Directories.Logging"));
        } catch (NullPointerException nex) {
            logger.log(Level.FATAL, "Logging directory not found!" + nex.getMessage());
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(nex));
            // -----
            throw new StartUpException();
        }

        if (logDir.canWrite() == false) {
            logger.log(Level.FATAL, "Unable to write to log directory");
            throw new StartUpException();
        }

        String logfile;

        try {
            logfile = logDir.getCanonicalPath()
                    + File.separator +  settings.getProperty("Basic.ServiceName").toLowerCase() + ".log"; // "xmlimporter.log";
            handler = new DailyRollingFileAppender();
            handler.setDatePattern("'.'yyyy-MM-dd");
            handler.setFile(logfile);
            handler.setImmediateFlush(true);
            handler.setName("XMLImporterAppender");

            String pattern = settings.getProperty("Basic.LogPattern",
                    "%d{dd.MM.yyyy HH:mm:ss} %-5p [%t]: %m%n");
            handler.setLayout(new PatternLayout(pattern));
            handler.activateOptions();

            Level logLevel = Level.toLevel(settings.getProperty("Basic.LogLevel", "WARN"), Level.WARN);

            logger.removeAppender(logHandler);
            Logger baseLogger = Logger.getLogger("de.di");
            baseLogger.addAppender(handler);
            baseLogger.setLevel(logLevel);

            for (LoggingEvent event : logHandler.getEvents()) {
                handler.doAppend(event);
            }

            logger = Logger.getLogger("de.di.xml.Mainthread.java");

            Logger.getRootLogger().setLevel(Level.WARN);
            Logger.getRootLogger().removeAllAppenders();

            logHandler.close();
        } catch (java.io.IOException ioex) {
            logger.log(Level.FATAL, "Unable to create log file" + ioex.getMessage());
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(ioex));
            // -----
            throw new StartUpException();
        } catch (IllegalArgumentException iaex) {
            logger.log(Level.FATAL, "Illegal log level set" + iaex.getMessage());
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(iaex));
            // -----
            throw new StartUpException();
        }
    }  
    
}
