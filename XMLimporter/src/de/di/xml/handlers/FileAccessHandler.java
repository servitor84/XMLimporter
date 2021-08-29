package de.di.xml.handlers;

import de.di.xml.Importer;
import de.di.xml.Schedulable;
import java.io.File;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class FileAccessHandler extends BaseSchedulableHandler {

  public FileAccessHandler() {
    super();

    logger = Logger.getLogger(getClass());
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    try {
      checkInputFiles(s.getFileList());
    } catch (FileCheckException ex) {
    
      throw new HandlerException();
    } catch (Exception ex) {
      logger.error("File access check failed for input files!", ex);
      // -----                        
            //Importer.reportException(Importer.getMailContent(ex));
            // -----
      throw new HandlerException();
    }

    super.handleSchedulable(s);
  }

  private void checkInputFiles(List<File> fileList) throws FileCheckException {
    for (File file : fileList) {
      //make some checks for access writes
      if (!file.canRead()) {
        logger.log(Level.DEBUG, "File '" +
            file.getAbsolutePath() +
            "' is not readable. Skipping.");
        throw new FileCheckException();
      } else if (!file.canWrite()) {
        logger.log(Level.DEBUG, "File '" +
            file.getAbsolutePath() +
            "' is not writeable. Skipping.");
        throw new FileCheckException();
      }
    }
  }
}
