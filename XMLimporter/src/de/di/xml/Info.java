package de.di.xml;

import java.io.File;
import java.sql.Timestamp;
import java.util.Properties;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;

/**
 *
 * @author Rahman
 */
public class Info {

    private static String serviceStartup;
    private static Timestamp lastRun;
    private static int counterOK = 0;
    private static int counterError = 0;
    private static String configPath;
    private static String licenseowner = "alskdfjöasl";
    private static String product;
    private static String url;
    private static String errormsg;
    private static String profilePath;

    public static String getPollingIntervall() {
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            Properties conf = new Properties();
            conf.load(inputStream);
            inputStream.close();
            return conf.getProperty("Importer.PollTime");
        } catch (Exception ex) {            
            return ex.getMessage();
        }
    }

    public static int getPI() {
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            Properties conf = new Properties();
            conf.load(inputStream);
            inputStream.close();
            return Integer.valueOf(conf.getProperty("Importer.PollTime"));
        } catch (Exception ex) {
            return 0;
        }
    }

    public static String getErrormsg() {
        return errormsg;
    }

    public static void setErrormsg(String err) {
        errormsg = err + "\n" + errormsg;
    }

    public static String getLicenseowner() {
        return licenseowner;
    }

    public static void setProfilePath(String path) {
        profilePath = path;
    }

    public static String getProfilePath() {
        return profilePath;
    }

    public static void setProperties(File f, String prod) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(f.getAbsolutePath()));
            licenseowner = props.getProperty("client");
            product = prod;
        } catch (Exception ex) {
        }
    }

    public static String getProductName() {
        return product;
    }

    public static String getTimestamp() {
        return new Timestamp(new java.util.Date().getTime()).toString();
    }

    public static String getUrl() {
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            Properties conf = new Properties();
            conf.load(inputStream);
            inputStream.close();
            String TomcatURL = conf.getProperty("Basic.TomcatURL");
            String ServiceName = conf.getProperty("Basic.ServiceName");
            url = TomcatURL + ServiceName;
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return url;
    }

    public static void setConfigPath(String path) {
        configPath = path;
    }

    public static int getCounterOK() {
        return counterOK;
    }

    public static void incCounterOK() {
        counterOK++;
    }

    public static int getCounterError() {
        return counterError;
    }

    public static void incCounterError() {
        counterError++;
    }

    public static String getServiceStartup() {
        return serviceStartup.substring(0, serviceStartup.length() - 4);
    }

    public static void setServiceStartup() {
        serviceStartup = new Timestamp(new java.util.Date().getTime()).toString();
    }

    public static String getLogLevel() {
        Properties conf = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            conf.load(inputStream);
            inputStream.close();
        } catch (Exception ex) {
            ex.getStackTrace();
        }
        return conf.getProperty("Basic.LogLevel");
    }

    public static void setLastRun() {
        lastRun = new Timestamp(new java.util.Date().getTime());
    }

    public static String getLastRun() {
        if (lastRun == null) {
            setLastRun();
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastRun);
    }

    public static String getNextRun() {
        Properties conf = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            conf.load(inputStream);
            inputStream.close();
            if (lastRun == null) {
                setLastRun();
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(lastRun.getTime() + Integer.valueOf(conf.getProperty("Importer.PollTime"))));
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}
