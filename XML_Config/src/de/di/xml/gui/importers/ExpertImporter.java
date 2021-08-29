package de.di.xml.gui.importers;

import de.di.xml.gui.Config;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author A. Sopicki
 */
public class ExpertImporter extends Importer {

  public ExpertImporter() {
    displayName = "Userdefined";

    ResourceMap map = org.jdesktop.application.Application.
        getInstance(de.di.xml.gui.ConfigApp.class).getContext().
        getResourceMap(Importer.class);

    try {
      displayName = map.getString("importer.Expert");
    } catch (Exception ex) {
    }
  }

  @Override
  public void doConfig(Config config) {
    //don't change the config in expert mode
    }
}
