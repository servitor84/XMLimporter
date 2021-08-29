package de.di.xml.handlers;


/**
 *
 * @author A. Sopicki
 */
public interface SchedulableHandler {
  public void handleSchedulable(de.di.xml.Schedulable s) throws HandlerException;

  public void setNextHandler(SchedulableHandler next);

  public SchedulableHandler getNextHandler();
}
