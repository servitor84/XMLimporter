package de.di.xml.handlers;

//import de.arivato.dokinform.app.IndexValue;
import de.di.dokinform.util.Registry;
import de.di.xml.Info;
import de.di.xml.Schedulable;
import de.di.xml.importers.DuplicateException;
import de.di.xml.importers.ImportException;
import de.di.xml.importers.Importer;
import java.util.Map;
import org.apache.log4j.Logger;
import de.di.xml.IndexValue;

/**
 *
 * @author A. Sopicki
 */
public class ImportHandler extends BaseSchedulableHandler {

    private Importer importer = null;

    public ImportHandler(Importer importer) {
        super();
        logger = Logger.getLogger(getClass());
        this.importer = importer;
    }

    @Override
    public void handleSchedulable(Schedulable s) throws HandlerException {

        //Check if import for this schedulable is already complete
        if (s.getProgressStatus().getValue() >= Schedulable.Progress.IMPORT_COMPLETE.getValue()) {
            super.handleSchedulable(s);
            return;
        }

        Map<String, IndexValue> data = (Map<String, IndexValue>) Registry.getInstance().get(s.getIndexFile().toString());
        try {
//            if (s.getSignatureFile() != null) {
//                importer.importDocument(data, s.getSignatureFile());
//            } else {
            importer.importDocument(data);
//            }
        } catch (DuplicateException dex) {
            logger.error("Duplicate file in archive found. Aborting import of file " + s.getDocumentFile());
            logger.debug("Details of exception: ", dex);
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(dex));
            // -----
            throw new HandlerException("Duplicate file in archive. Aborting import.", dex);
        } catch (ImportException iex) {
            logger.warn("Document import failed.", iex);
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(iex));
            // -----
            Info.incCounterError();
            throw new HandlerException();
        }

        super.handleSchedulable(s);
    }
}
