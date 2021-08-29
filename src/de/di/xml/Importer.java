package de.di.xml;

import de.di.elo.client.ELOClient;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import de.di.xml.initialization.Logging;
import de.di.xml.initialization.License;
//import de.di.xml.initialization.Recruitment;
import de.di.xml.initialization.Directories;
import org.apache.log4j.DailyRollingFileAppender;
import de.di.xml.initialization.CurrentMask;
import de.di.xml.initialization.BufferHandler;
import de.di.xml.initialization.StartUpException;
import java.util.Set;

public class Importer extends Thread {

    private static Logger logger = null;
    public static volatile boolean running = true;
    private BufferHandler logHandler = null;
    private InputStream licenseStream = null;
    private static Properties settings = null;
    private static Set<String> fArray;
    private DailyRollingFileAppender handler = null;
    private java.util.List<String> errorStatus = new java.util.ArrayList<String>();
    private java.util.Map<String, String> status = new java.util.Hashtable<String, String>();        
    private ELOClient eloClient;
    
    private static int numberOfAttemptsToMoveFile = 0;
    private static List<String> filesWhichNotCanBeEdited = new ArrayList<>();
    
    public static int getNumberOfAttemptsToMoveFile() {
        return numberOfAttemptsToMoveFile;
    }
    public static void setNumberOfAttemptsToMoveFile() {
        numberOfAttemptsToMoveFile++;
    }
    public static List<String> getFilesWhichNotCanBeEdited() {
        return filesWhichNotCanBeEdited;
    }
    public static void setFilesWhichNotCanBeEdited(String pfad) {
        filesWhichNotCanBeEdited.add(pfad);
    }
   
    
    public Importer() {
        this(null, null);
    }

    /**
     * Constructor for application start up in a J2EE server environment which
     * will pass in the InputStream object of the config file.
     *
     * @param config input stream holding the configuration information
     * @param license input stream holding the license information
     */
    public Importer(Properties config, InputStream license) {            
        settings = config;
        licenseStream = license;
       
        BasicConfigurator.configure();
        logger = Logger.getLogger(this.getClass().getPackage().getName());
        logHandler = new BufferHandler();
        logger.addAppender(logHandler);

        loadProductInfo();

        java.util.logging.Logger.getLogger("de.di.dokinform.recovery").setLevel(java.util.logging.Level.OFF);
    }

    public static Properties getSettings() {
        return settings;
    }

    private void loadProductInfo() {        
        String productName = "";
        if (settings.getProperty("Basic.ServiceName").startsWith("oi-") || settings.getProperty("Basic.ServiceName").toUpperCase().startsWith("OCKARAT")) {
            productName = "OCKARATimporter";
        } else {
            productName = "XMLimporter";
        }      
        InputStream in = getContextClassLoader().getResourceAsStream("de/di/xml/resources/product_" + productName + ".properties");
        if (in != null) {
            ProductInfo.readProductInfo(in, status);
//            status.put("pollTime", String.valueOf(Integer.valueOf(settings.getProperty("Importer.PollTime")) / 1000));
            logger.info("Productname: " + status.get("product"));
            logger.info("Version: " + status.get("version"));
        }
    }

    private void init() {
        logger.trace(getClass().getName() + ": startUp()");        
        try {            
            boolean newInstall = Boolean.parseBoolean(settings.getProperty("Basic.NewInstall", "TRUE"));
            if (newInstall) {
                logger.fatal("Service is currently deactivated. Aborting.");
                throw new StartUpException();
            }
           
            Logging.start(logger, handler, logHandler, settings);
            //connect to ELO IndexServer
            //ELOClient eloClient = ELOixIsReachable(settings);
            initELOClient(settings);
            //check all required directories
            Directories.check(settings, logger);
            //basic configuration checks
                //Recruitment.check(settings, logger);
            //check the license
            License.check(licenseStream, status, settings, logger);            
            //get the list of maskNames with max. number of index field for each mask
            CurrentMask.pickUpFromIX(eloClient, logger);
            //eloClient.close();
        } catch (StartUpException sex) {
            logger.error("Unable to start the service. " + sex.getMessage());            
            running = false;
            shutdown();
        }
    }

    @Override
    public void run() {
        logger.setLevel(Level.ALL);
        Info.setServiceStartup();
        logger.log(Level.INFO, "Starting XMLimporter");
        //initiate start up sequence
        init();
        while (running) {
            Info.setLastRun();
            new Archiving().start(settings, logger);
            try {
                File f = new File(settings.getProperty("Directories.Input"));                
//                if(settings.getProperty("Importer.CreateSignalFile").equalsIgnoreCase("true")) {
//                    //Archiving.addSignalFiles(settings, logger);
//                }
                File[] fileArray = f.listFiles();
                List<File> sigArray = new ArrayList<File>();

                for (int i = 0; i < fileArray.length; i++) {
                    String fName = fileArray[i].getName();
                    if (fName.substring(fName.length() - 4, fName.length()).equalsIgnoreCase(settings.getProperty("Importer.FileExtension"))) {
                        sigArray.add(fileArray[i]);
                    }
                }
                if (sigArray.size() == 0) {
                    logger.info(sigArray.size() + " " + settings.getProperty("Importer.FileExtension").toUpperCase() + " files found in " + settings.getProperty("Directories.Input") + " folder");
                    logger.info("Current total amount fo successfull / error actions: " + Info.getCounterOK() + " / " + Info.getCounterError());
                    int pollTime = Integer.valueOf(settings.getProperty("Importer.PollTime"));
                    logger.info("Importer is sleeping " + pollTime / 1000 + " seconds");
                    Thread.sleep(pollTime);
                }
            } catch (InterruptedException iex) {
                logger.debug("Importer is interrupted : " + iex.getMessage());                
            } catch (Exception e) {
                logger.error("Unhandled exception occured! Please"
                        + " report the bug \nif possible with all information available\n"
                        + "to reproduce the problem. ", e);               
            }
        }
    }

    public void shutdown() {
        if (logger != null) {
            logger.info("Shutting down...");
        }
        if (running == false) {
            return;
        }
        running = false;
        interrupt();
    }

    public static void setOpenFiles(Set<String> array) {
        fArray = array;
    }

    public static Set<String> getOpenFiles() {
        return fArray;
    }

    /**
     * Callback function for the status page to get some status information from
     * the running application.
     *
     * Currently the following information is available:
     *
     * Key: version, the version of the application Key: product_name, the
     * product name of the application Key: queue_size, the size of the queue
     * for incoming files Key: scheduler_status, status of the scheduler (true
     * if it is running, false otherwise) Key: queue_count, files currently
     * processed (only if scheduler is running!) Key: worker_count, number of
     * workers available (only if scheduler is running!) Key: dispatcher_status,
     * status of the dispatcher (true if it is running, false otherwise) Key:
     * active_job_count, number of open files (only if dispatcher is running!)
     *
     * @return java.util.Map<String, String> a map holding the status
     * information
     */
    public synchronized Map<String, String> getStatus() {
        return status;
    }

    public synchronized java.util.List<String> getErrorStatus() {
        return errorStatus;
    }

    /**
     * Inner class implementing the shutdown hook for the VM.
     */
    private class ShutdownHandler extends Thread {

        @Override
        public void run() {
            shutdown();
            try {
                try {
                    Importer.this.join();
                } catch (Exception e) {
                }

                //handler.flush();
            } catch (Exception e) {
            }
        }
    }

    public void outPut(Map<String, String> stat) {
        logger.info("Start with outPut function with status ");
        java.util.Iterator<Map.Entry<String, String>> it = stat.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            logger.info("entry.getKey() = " + entry.getKey() + " entry.getValue() = " + entry.getValue());

        }
        logger.info("End of outPut function");
    }

    private ELOClient ELOixIsReachable(java.util.Properties settings) throws IllegalStateException, StartUpException {
        running = false;
        ELOClient client = new ELOClient(settings, logger);
        if (!isCompatible(client.getIndexServerVersion(), status.get("version"))) {
            throw new StartUpException("This version of " + status.get("version") + " is not certified for ELO Indexserver " + client.getIndexServerVersion() + ", please contact DOKinform for an update www.dokinform.ch");
        }
        running = true;
        return client;
    }

    private static boolean isCompatible(String ixVersion, String xmlVersion) {
//        if (true) {
//            return true;
//        }
        if (ixVersion == null || ixVersion.isEmpty()) {
            logger.error("Could not check indexServer version. Version : " + ixVersion);
            return false;
        }
        if (xmlVersion == null || xmlVersion.isEmpty()) {
            logger.error("Could not check xml importer version. Version : " + xmlVersion);
            return false;
        }
        String ixMajor = ixVersion.split("\\.")[0];
        String xmlMajor = xmlVersion.split("\\.")[0];
        logger.info("Comparing sv : " + xmlMajor + " with ixv : " + ixMajor);
        return Integer.valueOf(xmlMajor) >= Integer.valueOf(ixMajor);
    }
    
    private void initELOClient(java.util.Properties settings) throws StartUpException {
        logger.trace(getClass().getName() + ": entering initELOClient()");

        String ix = settings.getProperty("IndexServer.URL");
        String user = settings.getProperty("IndexServer.User");
        String password = settings.getProperty("IndexServer.Password");
                      
        eloClient = new ELOClient(logger, settings);        
        eloClient.setConnectionUrl(ix);
        eloClient.setUserName(user);
        eloClient.setPassword(password);
        logger.info("Trying to connect. URL: " + ix + " ,user:" + user + " ,password: ****");
        
        int retryCount = 10000;

        while (!eloClient.isConnected() && retryCount > 0) {
            logger.debug("Is entering login loop");
            try {                         
                eloClient.login();                   
            } catch (de.elo.utils.net.RemoteException ex) {
                retryCount--;
                for (StackTraceElement elem : ex.getStackTrace()) {
                    logger.trace(elem.getFileName() + ":" + elem.getLineNumber() + " "
                            + elem.getClassName() + "." + elem.getMethodName());
                }
                logger.debug("An exception occured while connecting to the index server.");
                logger.warn("Connection to index server failed: ");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex1) {
                }
            } catch (IllegalStateException stateex) {
                retryCount--;
                logger.debug("An exception occured while connecting to the index server.");
                logger.warn("Connection to index server failed: ");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex1) {
                }
            }
        }                
        if (retryCount == 0) {
            throw new StartUpException("Connection to index server currently unavailable. Shutting down.");
        }        
        logger.trace(getClass().getName() + ": leaving initELOClient()");
    }        
}
