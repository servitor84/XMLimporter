
package de.di.xml.handlers;

import de.di.dokinform.util.Registry;
import de.di.xml.BackupWriter;
import de.di.xml.Importer;
import de.di.xml.Schedulable;
import java.io.File;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class RecoveryBackupHandler extends BaseSchedulableHandler {

  private Properties settings = null;

  public RecoveryBackupHandler() {
    super();

    logger = Logger.getLogger(getClass());
    settings = (Properties)Registry.getInstance().get("settings");
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    File backupDir = new File(
        settings.getProperty("Directories.Backup"));

    java.util.ArrayList<FileCheck> files = new java.util.ArrayList<FileCheck>();

    if ( s.getSignalFile() != null ) {
      files.add(new FileCheck(s.getSignalFile(), s.getSignalFileSize()));
    }

    if ( s.getSignatureFile() != null ) {
      files.add(new FileCheck(s.getSignatureFile(), s.getSignatureFileSize()));
    }

    if ( s.getProtocolFile() != null ) {
      files.add(new FileCheck(s.getProtocolFile(), s.getProtocolFileSize()));
    }

    files.add(new FileCheck(s.getDocumentFile(), s.getDocumentFileSize()));
    files.add(new FileCheck(s.getIndexFile(), s.getIndexFileSize()));

    for(FileCheck check: files) {
      File backupFile = new File(backupDir, check.getFile().getName());

      if ( backupFile.exists() && backupFile.length() != check.getSize() ) {
        throw new HandlerException("Backup file size missmatch!");
      } else {
        try {
          BackupWriter.makeBackup(check.getFile(), backupFile, logger);
        } catch(java.io.IOException ioex) {
          logger.error("Unable to complete backup for file "+check.getFile()+"!", ioex);
          // -----                        
            //Importer.reportException(Importer.getMailContent(ioex));
            // -----
          throw new HandlerException();
        }
      }
    }

    super.handleSchedulable(s);
  }
}
