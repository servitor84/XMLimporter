package de.di.xml;

import de.elo.ix.client.EditInfo;
import de.elo.ix.client.EditInfoC;
import de.elo.ix.client.IXConnFactory;
import de.elo.ix.client.IXConnection;
import de.elo.ix.client.IXExceptionC;
import de.elo.ix.client.LockC;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author Rahman
 */
public class Check {
    
    public static boolean hasNoDuplicates(File docFile, Properties settings, Logger logger) throws IOException {
        String md5 = null;
        IXConnFactory connFact = new IXConnFactory(settings.getProperty("IndexServer.URL"), "XMLimporter", "8.0.0");
        IXConnection conn = connFact.create(settings.getProperty("IndexServer.User"), settings.getProperty("IndexServer.Password"), System.getProperty("user.name", "Unknown PC"), null);
        try {
            md5 = conn.getFileMd5(docFile);
            String objId = "MD5:" + md5;
            EditInfo ed = conn.ix().checkoutSord(objId, EditInfoC.mbOnlyId, LockC.NO);
            logger.warn("\t\t\t File " + docFile.getName() + " already exists in ELO with objId = " + ed.getSord().getId() + ". Skip");
            return true;
        } catch (IOException ex) {
            if (conn.ix().parseException(ex.toString()).getExceptionType() != IXExceptionC.NOT_FOUND) {                
                throw new IOException(ex.getMessage());
            }
            return false;
        }
    }
    
}
