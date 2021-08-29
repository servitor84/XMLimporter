/*
 * Schedulable.java
 *
 * Created on 18. Januar 2007, 11:54
 *
 */
package de.di.xml;

import java.io.File;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
//import de.arivato.xml.error.Profile;

/**
 *
 * @author A. Sopicki
 */
public interface Schedulable extends Delayed
{

    /**
     *
     * Enumeration for the status code of the job
     *
     * @author A. Sopicki
     */
    public enum Status
    {

        DONE(0), CREATED(1), QUEUED(2), WORK_IN_PROGRESS(3), DEFERRED(80),
        ABORTED(100), FAILED(101);

        private int _value;

        Status(int value)
        {
            _value = value;
        }

        public int getValue()
        {
            return _value;
        }
    }
    
    public enum Progress
    {
        NEW(0), IN_PROGRESS(1), CHECK_COMPLETE(2), BACKUP_COMPLETE(3), IMPORT_COMPLETE(4),
        MAIL_QUEUE_COMPLETE(5), COMPLETE(100);
        
        private int _value;
        
        Progress(int value)
        {
            _value = value;
        }
        
        public int getValue()
        {
            return _value;
        }
    }

    public int getId();

    public void finished();
    
    public void abort();

    public boolean isFinished();

    public java.util.List<File> getFileList();

    public File getSignalFile();

    public void setSignalFile(File f);

    public File getIndexFile();

    public void setIndexFile(File f);

    public File getDocumentFile();

    public void setDocumentFile(File f);

    public File getSignatureFile();

    public File getProtocolFile();

    /**
     * @param signatureFile the signatureFile to set
    */
    public void setSignatureFile(File signatureFile);

    /**
      * @param protocolFile the protocolFile to set
      */
    public void setProtocolFile(File protocolFile);
    
    public Status getStatus();

    public void setDelay(long d, TimeUnit unit);
        
    public void setStatus(Status status);
    
    public int getErrorCount();
        
    public void increaseErrorCount();
        
    public long getSignalFileSize();
    
    public void setSignalFileSize(long size);
    
    public long getIndexFileSize();
    
    public void setIndexFileSize(long size);
    
    public long getDocumentFileSize();
    
    public void setDocumentFileSize(long size);

    public long getSignatureFileSize();

    public void setSignatureFileSize(long size);

    public long getProtocolFileSize();

    public void setProtocolFileSize(long size);
    
    public Progress getProgressStatus();
    
    public void setProgressStatus(Progress p);
    
    public long getStartTime();
    
    public void setStartTime(long time);

    public void addSchedulableListener(SchedulableListener listener);

    public void removeSchedulableListener(SchedulableListener listener);

    public String getUID();
    
//    public Profile getProfile();
//    
//    public void setProfile(Profile p);
}
