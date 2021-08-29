package de.di.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class Main {

    private static Logger logger = null;
    private BufferHandler logHandler = null;

    public Main() {
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        logHandler = new BufferHandler();
        logger.addAppender(logHandler);
        java.util.logging.Logger.getLogger("de.di.dokinform.recovery").setLevel(java.util.logging.Level.OFF);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        //C:\\Users\\ELO1\\Projects\\Uwe\\Projects\\XMLimporter
        try {
            File confFile = new File("conf/config.properties");
            String confFileAbsPath = confFile.getAbsolutePath();
            Properties confPr = new Properties();
            FileInputStream cis = new FileInputStream(confFile);
            confPr.load(cis);
            cis.close();

           // File licenseFile = new File("C:\\Users\\ELO1\\Projects\\Uwe\\Projects\\XMLimporter\\conf\\license.txt");
            
            //FileInputStream lis = new FileInputStream(licenseFile);
            
            Importer importer = new Importer(confPr, null);
            
            //start the application
            importer.start();

            //wait till the end of the application
            importer.join();

            //show error messages if start up fails
            java.util.List<String> status = importer.getErrorStatus();
            if (status != null && status.size() > 0) {
                for (String msg : status) {
                    logger.info(msg);
                }
            }
        } catch (InterruptedException ex) {
            logger.info("XMLimporter is in main class interrupted " + ex.getMessage());
        }
    }

    private class BufferHandler extends org.apache.log4j.AppenderSkeleton {

        private ArrayList<LoggingEvent> events = new ArrayList<LoggingEvent>(30);

        BufferHandler() {
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

}
