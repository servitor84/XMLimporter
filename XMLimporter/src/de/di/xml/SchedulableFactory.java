package de.di.xml;

import de.di.dokinform.recovery.Id;
import java.io.File;

/**
 *
 * @author A. Sopicki
 */
public class SchedulableFactory {

  private Id nextJobId = new Id();

  public Schedulable createSchedulable(File indexFile, File documentFile, File signalFile) {
    return new Job(nextJobId.nextId(), indexFile, documentFile, signalFile);
  }

  public void setNextJobId(int i) {
    nextJobId.setId(i);
  }

  public int getLastJobId() {
    return nextJobId.getLastId();
  }
}
