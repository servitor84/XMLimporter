package de.di.xml.handlers;

import de.di.dokinform.util.Registry;
import de.di.xml.Importer;
import de.di.xml.Schedulable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class CleanUpHandler extends BaseSchedulableHandler {

  private String[] cleanupExtensions = null;

  public CleanUpHandler(String[] cleanupExtensions) {
    super();

    this.cleanupExtensions = cleanupExtensions;
    logger = Logger.getLogger(getClass());
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    Properties settings = (Properties)Registry.getInstance().get("settings");
    File inputDir = inputDir = new File(settings.getProperty("Directories.Input"));

    //remove any files left
    String basename = s.getIndexFile().getName();
    basename = basename.substring(0, basename.lastIndexOf("."));

    //check that document file has been removed from the input directory
    if (s.getDocumentFile().exists()) {
      if (!s.getDocumentFile().delete()) {
        logger.info("Unable to delete file " +
            s.getDocumentFile().getAbsolutePath() +
            ".");
        throw new HandlerException();
      }
    }

    if (cleanupExtensions != null) {
      try {
        for (String ext : cleanupExtensions) {
          if (ext.length() == 0) {
            continue;
          }

          File removable = new File(inputDir, basename +
              ext);

          if (removable.exists() && !removable.delete()) {
            logger.info("Unable to delete file " +
                removable.getAbsolutePath() +
                ".");
            throw new IOException();
          } else {
            logger.trace("Removed file " +
                removable.getName() +
                " from input directory");
          }
        }
      } catch (IOException ioex) {
        logger.warn("IOException occured during cleanup!", ioex);
        // -----                        
            //Importer.reportException(Importer.getMailContent(ioex));
            // -----
        throw new HandlerException();
      }
    } else {
      logger.warn("No cleanup extensions defined!");
    }
    
    super.handleSchedulable(s);
  }
}
