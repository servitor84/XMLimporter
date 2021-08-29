
package de.di.xml.handlers;

import de.di.dokinform.util.Registry;
import de.di.xml.Importer;
import de.di.xml.Schedulable;
import java.io.File;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class CheckBackupHandler extends BaseSchedulableHandler {

  public CheckBackupHandler() {
    super();

    logger = Logger.getLogger(getClass());
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    try {
      checkBackup(s);
    } catch (FileCheckException bex) {     
      throw new HandlerException();
    } catch (Exception ex) {
        logger.error("Error while checking backup directory.", ex);
        // -----                        
        //Importer.reportException(Importer.getMailContent(ex));
        // -----
        throw new HandlerException();
    }

    //tell the schedulable that it's being worked on
    s.setStatus(Schedulable.Status.WORK_IN_PROGRESS);

    if (s.getProgressStatus().getValue() < Schedulable.Progress.CHECK_COMPLETE.getValue()) {
      s.setProgressStatus(Schedulable.Progress.CHECK_COMPLETE);
    }

    
    super.handleSchedulable(s);
  }

  private void checkBackup(Schedulable schedulable) throws FileCheckException {
    Properties settings = (Properties)Registry.getInstance().get("settings");
    boolean doBackup = false;
    File backupDir = null;
    try {
      doBackup = Boolean.parseBoolean(settings.getProperty("Importer.MakeBackup", "FALSE"));
    } catch (Exception e) {
        logger.warn("Configuration option Importer.MakeBackup not set" +
          " or not set to a boolean value. Using default value (false).");
        // -----                        
        //Importer.reportException(Importer.getMailContent(e));
        // -----
    }

    if (doBackup) {
      backupDir = new File(settings.getProperty("Directories.Backup"));
    }

    for (File file : schedulable.getFileList()) {
      /*
       * Conservative approach: Don't process the files if a backup already exists or a copy of the file
       * is in the output directory. We will do that only once for a Schedulable object
       * because otherwise a backup may already exist.
       */
      File backupFile = new File(backupDir, file.getName());

      if (schedulable.getProgressStatus().getValue() <
          Schedulable.Progress.CHECK_COMPLETE.getValue() && backupFile.exists()) {
        logger.warn("File '" + backupFile.getAbsolutePath() +
            "' found in backup directory blocks processing \n" +
            "of the file '" +
            file.getAbsolutePath() +
            "'. Skipping.");
        throw new FileCheckException();
      }
    }
  }
}
