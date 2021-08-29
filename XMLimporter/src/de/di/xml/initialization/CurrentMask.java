package de.di.xml.initialization;

import de.di.elo.client.ELOClient;
import de.di.xml.Archiving;
import de.di.xml.Importer;
import de.elo.utils.net.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Rahman
 */
public class CurrentMask {

    public static void pickUpFromIX(ELOClient client, Logger logger) {

        Map<String, Integer> maskNames = new HashMap<String, Integer>();
        Map<String, Map<Integer, String>> maskLinesIdToName = new HashMap();
        try {
            maskNames = client.getMaskNamesAndMaxNumberOfIndexField();
            maskLinesIdToName = client.getMasksLinesIdToName();
        } catch (java.rmi.RemoteException ex) {
            logger.error("Cannot read the name of current masks " + ex.getMessage());
            // -----                        
            //Importer.reportException(de.di.xml.Importer.getMailContent(ex));
            // -----
        }
        Archiving.map = maskNames;
        Archiving.maskLinesIdToName = maskLinesIdToName;
    }
}
