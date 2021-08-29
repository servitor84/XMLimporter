
package de.di.xml.handlers;

import de.di.xml.Schedulable;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class BaseSchedulableHandler implements SchedulableHandler {

  protected SchedulableHandler nextHandler;

  protected Logger logger;

  protected BaseSchedulableHandler() {
  }


  @Override
  public final void setNextHandler(SchedulableHandler next) {
    nextHandler = next;
  }

  @Override
  public final SchedulableHandler getNextHandler() {
    return nextHandler;
  }

  @Override
  public void handleSchedulable(Schedulable s) throws HandlerException {
    if ( nextHandler != null ) {
      nextHandler.handleSchedulable(s);
    }
  }

}
