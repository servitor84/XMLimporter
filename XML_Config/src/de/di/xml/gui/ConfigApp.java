package de.di.xml.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class ConfigApp extends SingleFrameApplication {

    private final static String bundleName = "de/di/xml/gui/resources/ConfigApp";
    Config config = null;
    private Encryption encryptor;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.WARN);
        config = loadConfig();
        show(new ConfigView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
        getMainFrame().setIconImage(getContext().getResourceMap(getClass()).getImageIcon("fav.icon").getImage());
        java.util.Properties prop = new java.util.Properties();
        try {
            InputStream in = getClass().getResourceAsStream("resources/product.properties");
            prop.load(in);
            in.close();
            String version = prop.getProperty("app.version");
            String buildNr = prop.getProperty("app.build");
            getMainFrame().setTitle(java.util.ResourceBundle.getBundle(bundleName).getString("Application.title") + " " + version + " (build " + buildNr + ")");
        } catch (java.io.IOException ex) {
            System.out.println("Exception " + ex.getMessage());
        }

    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of ERPGUIApp
     */
    public static ConfigApp getApplication() {
        return Application.getInstance(ConfigApp.class);
    }

    public Config getConfig() {
        return config;
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(ConfigApp.class, args);
    }

    void saveConfig() throws Exception {
        File configFile = null;

        OutputStream configStream = null;

        try {
            configFile = new File("../conf/config.properties");

            if (configFile.canWrite() == false) {
                throw new Exception(java.util.ResourceBundle.getBundle(
                        bundleName).
                        getString("noWriteAccess.text"));
            }
        } catch (Exception ex) {
            throw new Exception(java.util.ResourceBundle.getBundle(
                    bundleName).
                    getString("unableToWriteFile.text"));
        }

        try {
            configStream = new FileOutputStream(configFile);
        } catch (FileNotFoundException fex) {
            throw new Exception(java.util.ResourceBundle.getBundle(
                    bundleName).
                    getString("canNotWriteFile.text"));
        }

        try {
            this.config.store(configStream, "Configuration file");
        } catch (java.io.IOException ioex) {
            throw new Exception(java.util.ResourceBundle.getBundle(
                    bundleName).
                    getString("errorOnWritingFile.text"));
        }
    }

    private Config loadConfig() {
        Config props = new Config();
        Logger log = Logger.getAnonymousLogger();
        File configFile = null;
        InputStream configStream = null;
        try {
            configFile = new File("../conf/config.properties");
            if (configFile.canRead() == false) {
                log.info(java.util.ResourceBundle.getBundle(
                        bundleName).getString(
                                "fileUnreadable.text"));                
                return props;
            }
        } catch (Exception ex) {
            log.info(ex.getMessage());
            return props;
        }
        try {
            configStream = new FileInputStream(configFile);
        } catch (FileNotFoundException fex) {            
            return props;
        }
        try {
            props.load(configStream);
            // PWD: Decode ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            String password = props.getProperty("IndexServer.Password");
            String pattern = "^((\\d){1,})([-]{1}(\\d){1,}){1,}";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = p.matcher(password);
            if(matcher.matches()) {
                de.elo.utils.sec.DesEncryption des = new de.elo.utils.sec.DesEncryption();
                password = des.decrypt(password);
            }
            // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            props.setProperty("IndexServer.Password", password);           
        } catch (java.io.IOException ioex) {            
        } catch (Exception ex) {            
        }
        return props;
    }
}
