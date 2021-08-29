package de.di.license.check;

import de.di.elo.client.ELOClient;
import de.di.license.check.BasicLicenseChecker;
import de.di.license.check.LicenseAttribute;
import de.di.license.check.LicenseException;
import de.di.license.check.LicenseExpiredException;
import de.di.license.check.LicenseKey;
import de.di.xml.Importer;
import de.elo.ix.client.License;
import de.elo.ix.client.ServerInfo;
import org.apache.log4j.Logger;
import java.util.Properties;

/**
 *
 * @author A. Sopicki
 */
public class ELOLicenseChecker extends BasicLicenseChecker {

    private Logger logger = null;
    private Properties settings = null;
    private String product = null;

    public ELOLicenseChecker(String product) throws java.security.NoSuchAlgorithmException {
        super();
        this.product = product;        
        logger = Logger.getLogger("de.di.xml.Importer.java");
    }

    @Override
    public void check(LicenseKey key, int rounds) throws LicenseException {
        ELOClient eloClient = new ELOClient(settings);        
        LicenseAttribute client = key.getAttribute("client");
        LicenseAttribute eloVersion = key.getAttribute("ELO-version");

        if (client == null) {
            logger.error("Client attribute missing");
            throw new LicenseException("client attribute missing");
        }

        if (eloVersion == null) {
            logger.error("ELO-version attribute missing");
            throw new LicenseException("ELO-version attribute missing");
        }

        if (key.getAttribute("ERP-system") == null) {
            logger.error("XMLimporter system attribute missing");
            throw new LicenseException("ERP-system attribute missing");
        }

        if (key.getAttribute("license-type") == null) {
            logger.error("License-type attribute missing");
            throw new LicenseException("license-type attribute missing");
        }

        if (!eloVersion.getValue().equals("Office")) {
            if (!eloVersion.getValue().equals("Professional")
                    && !eloVersion.getValue().equals("Enterprise")) {
                logger.error("Illegal ELO-version");
                throw new LicenseException("Illegal ELO-version");
            }

            License license;
            ServerInfo serverInfo = null;
            int count = 0;

            try {
                // 1000
                while (serverInfo == null && count < 10000) {
                    try {
                        serverInfo = eloClient.getServerInfo();
                    } catch (de.elo.utils.net.RemoteException rex) {                        
                        if (count == 10000) {                           
                            throw rex;
                        } else {
                            count++;
                            try {
                                logger.error("ELOix is not available, XMLimporter thread sleep 10 seconds");
                                logger.error(rex,rex);
                                Thread.sleep(10000);
                            } catch (InterruptedException iex) {
                            }
                        }
                    }
                }
                license = serverInfo.getLicense();
            } catch (Exception ex) {                
                throw new ELOException("ELO not available");
            }

            String serno = license.getSerno();

            if (serno.indexOf("\\n") != -1) {
                serno = license.getSerno().substring(0, license.getSerno().indexOf("\\n"));
            }

            serno = serno.trim();

//      System.out.println("Serno: "+serno);

            if (!serno.contains(client.getValue())) { // 09-05-2019: equals() mit contains() ersetzt
                throw new LicenseException("client attribute missmatch (got '"
                        + serno + "' from ELO instead of '" + client.getValue() + "')");
            }

//      if (!license.isProfessional()) {
//        throw new LicenseException("ELO-version missmatch");
//      }
            super.check(key, rounds);

            String version = serverInfo.getVersion();

            if (version != null) {
                eloVersion.setValue("ELO " + eloVersion.getValue() + " " + version);
//                System.out.println(eloVersion.getValue());
            }

            //ELO Office not supported
            try {
                if (key.getAttribute("ELO-version").getValue().toLowerCase().contains("office")) {
                    logger.fatal("ELO Office not supported!");
                    throw new LicenseException(("ELO Office not supported!"));
                }

                if (!key.getAttribute("product").getValue().equals(product)) {
                    logger.fatal("License violation detected! Product mismatch");
                    throw new LicenseException("Product not supported");
                }
            } catch (NullPointerException ex) {
                logger.fatal("Missing license attribute. License not valid!");
                // -----                        
                //Importer.reportException(Importer.getMailContent(ex));
                // -----
                throw new LicenseException("Missing license attribute. License not valid!");
            }

//              System.out.println(version.substring(0, version.length()));

            serverInfo = null;
            license = null;
        } else {

//            super.check(key, rounds);

            eloVersion.setValue("ELO " + eloVersion.getValue());

            logger.fatal("ELO Office not supported!");
            eloClient.close();
            eloClient = null;
            throw new LicenseException(("ELO Office not supported!"));
        }

        eloClient.close();
        eloClient = null;

        if (key.getExpirationDate() == null) {
            throw new LicenseException("Illegal license expiration date!");
        }

        if (key.getExpirationDate().compareTo(new java.util.Date()) < 0) {
            throw new LicenseExpiredException("License expired on: "
                    + String.format(java.util.Locale.getDefault(), "%1$tc", key.getExpirationDate()));
        }
    }

    public Properties getSettings() {
        return settings;
    }

    public void setSettings(Properties settings) {
        this.settings = settings;
    }
}
