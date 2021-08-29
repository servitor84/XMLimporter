
package de.di.xml.gui;

import org.jdesktop.application.ResourceMap;

/**
 *
 * @author A. Sopicki
 */
public class LogLevel {
  private org.apache.log4j.Level level;

  private String displayName;

  enum Type {
    DEBUG, WARN, INFO, ALL, ERROR;
  }

  LogLevel(org.apache.log4j.Level l, Type type) {
    level = l;

    ResourceMap map = org.jdesktop.application.Application.
            getInstance(de.di.xml.gui.ConfigApp.class).getContext().
            getResourceMap(getClass());

    try {
      displayName = map.getString("level."+type.toString().toLowerCase());
    } catch (Exception ex) {
      displayName = l.toString();
    }
  }

  org.apache.log4j.Level getLevel() {
    return level;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
