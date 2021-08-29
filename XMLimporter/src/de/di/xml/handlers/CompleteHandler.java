package de.di.xml.handlers;

import de.di.dokinform.util.Registry;
import de.di.xml.Schedulable;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class CompleteHandler extends BaseSchedulableHandler {

  public CompleteHandler() {
    super();

    logger = Logger.getLogger(getClass());
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    
    //mark schedulable as finished
    s.setStatus(Schedulable.Status.DONE);
    s.setProgressStatus(Schedulable.Progress.COMPLETE);
    s.finished();
    logger.info("Finished processing of file.");

    //remove index data for this schedulable if any
    Registry.getInstance().remove(s.getIndexFile().toString());

    super.handleSchedulable(s);
  }
}
