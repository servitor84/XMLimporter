package de.di.xml.initialization;

import java.io.File;

/**
 *
 * @author Rahman
 */
public class Directories {

    /**
     * Checks the directories for read and write access.
     *
     *
     * @throws de.arivato.erpconnect.ERPConnect.StartUpException
     */
    public static void check(java.util.Properties settings, org.apache.log4j.Logger logger) throws StartUpException {
        File dir = new File(settings.getProperty("Directories.Input", ""));
        
        if (!dir.canWrite()) {
            logger.fatal("Input directory not configured or not writable!\n"
                    + "Please edit config.properties accordingly or \nmake the "
                    + " directory writeable for the application.");
            throw new StartUpException();
        }
        
        if (!dir.canRead()) {
            logger.fatal("Input directory not readable! Please make the\n"
                    + "directory readable for the application.");
            throw new StartUpException();
        }
        
        dir = new File(settings.getProperty("Directories.Backup", ""));
        
        if (!dir.canWrite()) {
            logger.fatal("Backup directory not configured or not writable!"
                    + "Please edit config.properties accordingly or \nmake the "
                    + " directory writeable for the application.");
            throw new StartUpException();
        }
        
        dir = new File(settings.getProperty("Directories.ErrorOutput", ""));
        
        if (!dir.canWrite()) {
            logger.fatal("Error output directory not configured or not writable!"
                    + "Please edit config.properties accordingly or \nmake the "
                    + " directory writeable for the application.");
            throw new StartUpException();
        }
        
        dir = new File(settings.getProperty("Directories.Logging", ""));
        
        if (!dir.canWrite()) {
            logger.fatal("Logging directory not configured or not writable!\n"
                    + "Please edit config.properties accordingly or \nmake the "
                    + " directory writeable for the application.");
            throw new StartUpException();
        }
        
        
        dir = new File(settings.getProperty("Directories.DuplicatesOutput", ""));
        
        if (!dir.canWrite()) {
            logger.fatal("Duplicates output directory not configured or not writable!\n"
                    + "Please edit config.properties accordingly  or \nmake the "
                    + " directory writeable for the application.");
            throw new StartUpException();
        }
    }
}
