
package de.di.xml.gui;

import org.jdesktop.application.ResourceMap;

/**
 *
 * @author A. Sopicki
 */
class PerformanceLevel {

  private Level level;

  private String displayName;

  PerformanceLevel(Level l) {
    level = l;

    ResourceMap map = org.jdesktop.application.Application.
            getInstance(de.di.xml.gui.ConfigApp.class).getContext().
            getResourceMap(getClass());

    try {
      displayName = map.getString("level."+l.toString().toLowerCase());
    } catch (Exception ex) {
      displayName = l.toString();
    }
  }

  enum Level {
    STANDARD, MINIMUM, FAST, MAXIMUM, EXPERT;
  }

  Level getLevel() {
    return level;
  }

  @Override
  public String toString() {
    return displayName;
  }
}
