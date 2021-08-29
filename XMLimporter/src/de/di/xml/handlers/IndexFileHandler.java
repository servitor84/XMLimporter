
package de.di.xml.handlers;

import de.di.dokinform.app.IndexFileReader;
import de.di.dokinform.app.IndexValue;
import de.di.dokinform.util.Registry;
import java.util.Map;
import org.apache.log4j.Logger;

import de.di.xml.Schedulable;

/**
 *
 * @author A. Sopicki
 */
public class IndexFileHandler extends BaseSchedulableHandler {

  public IndexFileHandler() {
    super();

    logger = Logger.getLogger(getClass());
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {

    //tell the schedulable that it's being worked on
    s.setStatus(Schedulable.Status.WORK_IN_PROGRESS);

    IndexFileReader reader = new IndexFileReader(s.getIndexFile());
    reader.parse();

    Map<String, IndexValue> data = reader.getIndexData();

    if (data.size() == 0) {
      logger.error("Unable to parse index file " + s.getIndexFile());
      throw new HandlerException("Unable to parse index file");
    }

    Registry.getInstance().put(s.getIndexFile().toString(), data);

    super.handleSchedulable(s);
  }
}
