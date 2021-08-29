/*
 * Created on 28.04.2006
 *
 */
package de.di.xml;

//import de.arivato.xml.error.Profile;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Job implements Schedulable
{

    private Logger logger = null;
//    private Profile profile;
    /**
     * Id of the job
     */
    private int id = 0;

    /**
     * Creation time of the job for timeout checks
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * Return code of the job
     */
    private Status returnCode = Status.CREATED;
    
    private File signalFile = null;
    
    private File indexFile = null;
    
    private File documentFile = null;

    private File signatureFile = null;

    private File protocolFile = null;
    
    private String uid = null;
    
    private ArrayList<SchedulableListener> listeners = new ArrayList<SchedulableListener>();

    private int errorCount = 0;

    private long executionTime = System.currentTimeMillis();
    
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    
    private long signalFileSize = -1;
    
    private long indexFileSize = -1;
    
    private long documentFileSize = -1;

    private long signatureFileSize = -1;

    private long protocolFileSize = -1;
    
    private Progress progress = Schedulable.Progress.NEW;
    
    private long startTime = 0L;
    
    public Job(int id, File indexFile, File documentFile, File signalFile)
    {        
        logger = Logger.getLogger(getClass());
        this.id = id;
        this.signalFile = signalFile;
        this.indexFile = indexFile;
        this.documentFile = documentFile;
        
        uid = (new Integer(id).toString())+"_"+indexFile.getName();
        
        if ( signalFile == null )
            this.signalFileSize = 0;
    }

    /**
     * Returns true if every switch of the job has been
     * updated or the job has been aborted and false otherwise.
     * @return true if the job has been aborted or finished
     */
    @Override
    public boolean isFinished()
    {
        if (returnCode != Schedulable.Status.CREATED)
        {
            return true;
        }

        return false;
    }

    @Override
    public Status getStatus()
    {
        return returnCode;
    }
    
    @Override
    public void setStatus(Status status)
    {
        returnCode = status;
    }

    /**
     * Returns the id of the job
     * @return the id of the job
     */
    @Override
    public int getId()
    {
        return id;
    }
    
    public String getUID()
    {
        return uid;
    }

    /**
     * Returns the creation time of the job
     * @return the creation time of the job
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    @Override
    public void finished()
    {
        returnCode = Status.DONE;
        for(SchedulableListener l: listeners)
        {
            l.finished(this);
        }
        
        listeners.clear();
    }

    @Override
    public void addSchedulableListener(SchedulableListener listener)
    {
        logger.log(Level.INFO, "Add listener");
        listeners.add(listener);
        logger.log(Level.INFO, "Listener is added");
    }

    @Override
    public void removeSchedulableListener(SchedulableListener listener)
    {
        listeners.remove(listener);
    }
    
    @Override
    public String toString()
    {
        return getUID();
    }

    @Override
    public File getSignalFile()
    {
        return signalFile;
    }

    @Override
    public File getIndexFile()
    {
        return indexFile;
    }

    @Override
    public File getDocumentFile()
    {
        return documentFile;
    }

    @Override
    public File getSignatureFile() {
      return signatureFile;
    }
     
    @Override
    public int getErrorCount()
    {
        return errorCount;
    }

    @Override
    public void increaseErrorCount()
    {
        errorCount++;
    }

    @Override
    public void abort()
    {
        setStatus(Schedulable.Status.ABORTED);
                
        for(SchedulableListener l: listeners)
        {
            l.finished(this);
        }
        
        listeners.clear();
    }

    @Override
    public long getSignalFileSize() {
        if ( signalFileSize == -1 && signalFile != null )
        {
            signalFileSize = signalFile.length();
        }
        
        return signalFileSize;
    }

    @Override
    public void setSignalFileSize(long size) {
        signalFileSize = size;
    }

    @Override
    public long getIndexFileSize() {
        if ( indexFileSize == -1)
            indexFileSize = indexFile.length();
        
        return indexFileSize;
    }

    @Override
    public void setIndexFileSize(long size) {
        indexFileSize = size;
    }

    @Override
    public long getDocumentFileSize() {
        if ( documentFileSize == -1)
            documentFileSize = documentFile.length();
        
        return documentFileSize;
    }

    @Override
    public void setDocumentFileSize(long size) {
        documentFileSize = size;
    }

    @Override
    public long getSignatureFileSize() {
        if ( signatureFileSize == -1 && getSignatureFile() != null )
            signatureFileSize = signatureFile.length();

        return signatureFileSize;
    }

    @Override
    public void setSignatureFileSize(long size) {
        signatureFileSize = size;
    }

    @Override
    public Progress getProgressStatus() {
        return progress;
    }

    @Override
    public void setProgressStatus(Progress p) {
        progress = p;
    }

    @Override
    public long getDelay(TimeUnit u) {
        return u.convert(executionTime-System.currentTimeMillis(), unit);
    }

    @Override
    public int compareTo(Delayed other) {
        long d = other.getDelay(unit);
        long delay = executionTime-System.currentTimeMillis();
        
        if ( d < delay )
            return 1;
        
        if ( d > delay)
            return -1;
        
        return 0;
    }

    @Override
    public void setDelay(long d, TimeUnit tu) {
        executionTime = System.currentTimeMillis()+ unit.convert(d, tu);
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(long time) {
        startTime = time;
    }

  @Override
  public ArrayList<File> getFileList() {
    ArrayList<File> fileList = new ArrayList<File>(5);


    fileList.add(getIndexFile());
    fileList.add(getDocumentFile());

    if (getSignalFile() != null ) {
      fileList.add(getSignalFile());
    }

    if (getSignatureFile() != null) {
      fileList.add(getSignatureFile());
    }

    if ( getProtocolFile() != null ) {
      fileList.add(getProtocolFile());
    }

    return fileList;
  }

  /**
   * @return the protocolFile
   */
  @Override
  public File getProtocolFile() {
    return protocolFile;
  }

  /**
   * @param protocolFile the protocolFile to set
   */
  public void setProtocolFile(File protocolFile) {
    this.protocolFile = protocolFile;

    if ( protocolFile == null ) {
      setProtocolFileSize(0);
    }
  }

  /**
   * @param signatureFile the signatureFile to set
   */
  public void setSignatureFile(File signatureFile) {
    this.signatureFile = signatureFile;

    if ( signatureFile == null ) {
      signalFileSize = 0;
    }
  }

  /**
   * @return the protocolFileSize
   */
  public long getProtocolFileSize() {
    if ( protocolFileSize < 0 && protocolFile != null) {
      protocolFileSize = protocolFile.length();
    }

    return protocolFileSize;
  }

  /**
   * @param protocolFileSize the protocolFileSize to set
   */
  public void setProtocolFileSize(long protocolFileSize) {
    this.protocolFileSize = protocolFileSize;
  }

  public void setSignalFile(File f) {
    signalFile = f;
  }

  public void setIndexFile(File f) {
    indexFile = f;
  }

  public void setDocumentFile(File f) {
    documentFile = f;
  }
  
//  public void setProfile(Profile p){
//      profile = p;
//  }
//
//  public Profile getProfile(){
//      return profile;
//  }

}
