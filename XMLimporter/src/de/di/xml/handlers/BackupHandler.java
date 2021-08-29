package de.di.xml.handlers;

import de.di.dokinform.util.Registry;
import de.di.xml.BackupWriter;
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
public class BackupHandler extends BaseSchedulableHandler {

  private File backupDir = null;

  public BackupHandler() {
    super();

    logger = Logger.getLogger(getClass());
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    Properties settings = (Properties)Registry.getInstance().get("settings");
    boolean doBackup = false;

    try {
      doBackup = Boolean.parseBoolean(settings.getProperty("Importer.MakeBackup", "FALSE"));
    } catch (Exception e) {
      logger.warn("Configuration option Importer.MakeBackup not set" +
          " or not set to a boolean value. Using default value (false).");
        // -----                        
        //Importer.reportException(Importer.getMailContent(e));
        // -----
    }

    if (!doBackup) {
      super.handleSchedulable(s);
      return;
    }

    backupDir = new File(settings.getProperty("Directories.Backup"));

    //create backups if enabled and if necessary
    if (doBackup && s.getProgressStatus().getValue() <
        Schedulable.Progress.BACKUP_COMPLETE.getValue()) {

      logger.info("Making backup of input files.");
      try {
        for (File file : s.getFileList()) {
          makeBackup(file);
        }
      } catch (IOException ioex) {
        logger.fatal("Unable to create backup of input files\n" +
            "Backup directory should be writable and there should\n" +
            " be enough space left on the disk.");
        // -----                        
            //Importer.reportException(Importer.getMailContent(ioex));
            // -----
        throw new HandlerException();
      }
    }

    super.handleSchedulable(s);
  }

  /**
   * Creates a copy of the source file in the backup directory
   * @param srcFile the source file for the backup
   * @throws java.io.IOException
   */
  private void makeBackup(File srcFile) throws IOException {
    File destFile = null;
    destFile = new File(backupDir, srcFile.getName());

    //use the utility class BackupWriter for the actual backup process
    BackupWriter.makeBackup(srcFile, destFile, logger);
  }
}
