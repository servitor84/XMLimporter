package de.di.xml.importers;

import de.di.elo.client.ELOClient;
import de.di.xml.Archiving;
import de.elo.ix.client.AclItem;
import de.elo.ix.client.AclItemC;
import de.elo.ix.client.EditInfo;
import de.elo.ix.client.EditInfoC;
import de.elo.ix.client.LockC;
import de.elo.ix.client.Sord;
import de.elo.ix.client.SordC;
import java.io.IOException;
import de.elo.utils.net.RemoteException;

import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */


public class ELOStructureBuilder {

    public static final String PATH_SEP = "¶";
    private static ELOStructureBuilder instance = null;
    private Logger logger;

    private ELOStructureBuilder() {
        this.logger = Logger.getLogger(getClass());
    }

    public synchronized static ELOStructureBuilder getInstance() {
        if (instance == null) {
            instance = new ELOStructureBuilder();
        }

        return instance;
    }

    public synchronized Sord createPath(String destination, String mask, ELOClient eloClient, String userName) throws ImportException {
        String[] pathComponents = destination.substring(1).split(PATH_SEP);
        String path = "";
        EditInfo info = null;
        Sord parent = null;

       
        for (int i = 0; i < pathComponents.length; i++) {
            path += PATH_SEP + pathComponents[i].trim();

            info = null;
            try {
                info = eloClient.checkoutSord("ARCPATH:" + path, EditInfoC.mbSord, LockC.NO);
            } catch (IOException rex) {
                if (!rex.getMessage().contains("ELOIX:50")) {
                    logger.error("Unable to create structure path for document!", rex);
                    // -----                        
                    //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(rex));
                    // -----
//                    throw new ImportException("Unable to create structure path for document!");
                }
            }

            //create non-existing structure 
            if (info == null) {
                logger.debug("\t\t\tNon-existing-path: " + path);
                try {
                    if (parent == null) {
                        logger.info("\t\t\tCreating new root node.");

                        if (mask == null) {
                            logger.error("\t\t\tUsing default mask");

                            info = eloClient.createSord("1", "1", EditInfoC.mbSord);
                        } else {
                            logger.trace("\t\t\tUsing mask: '" + mask + "'");

                            info = eloClient.createSord("1", mask, EditInfoC.mbSord);
                        }
                    } else {
                        if (mask == null) {
                            info = eloClient.createSord(parent.getGuid(),Integer.toString(parent.getMask()), EditInfoC.mbSord);
                        } else {
                            info = eloClient.createSord(parent.getGuid(), mask, EditInfoC.mbSord);
                        }
                    }

                    parent = info.getSord();
                    parent.setName(pathComponents[i]);
                    AclItem[] aclItems = new AclItem[2];
                    aclItems[0] = new AclItem();
                    aclItems[0].setType(AclItemC.TYPE_INHERIT);
                    aclItems[1] = new AclItem();
                    aclItems[1].setName(userName);
                    aclItems[1].setAccess(eloClient.getServicePort().getACCESS().getLUR_ALL());

                    parent.setAclItems(aclItems);
                    eloClient.checkinSord(parent, SordC.mbAll, LockC.NO);

                } catch (RemoteException rex) {
                    logger.error("Unable to create structure path for document!", rex);
                    // -----                        
                    //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(rex));
                    // -----
                    throw new ImportException("Unable to create structure path for document!", rex);
                }
            } else {

                parent = info.getSord();
            }
        }
        try {
            info = eloClient.checkoutSord(parent.getGuid(), EditInfoC.mbAll, LockC.NO);
        } catch (RemoteException rex) {
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(rex));
            // -----
            logger.error("Unable to create structure path for document!", rex);
            throw new ImportException("Unable to create structure path for document!", rex);
        }

        if (info == null) {
            logger.error("Locking structure path failed!");
            throw new ImportException("Unable to lock structure path for document!");
        }

        parent = info.getSord();
        return parent;
    }

}
