package de.di.xml.gui.importers;

import de.di.xml.gui.Config;
import de.di.xml.gui.Config.Property;
import org.jdesktop.application.ResourceMap;

/**
 *
 * @author A. Sopicki
 */
public class ELOImporter extends Importer {

  public ELOImporter() {
    displayName = "Importer for ELO";

    ResourceMap map = org.jdesktop.application.Application.
        getInstance(de.di.xml.gui.ConfigApp.class).getContext().
        getResourceMap(Importer.class);

    try {
      displayName = map.getString("importer.ELO");
    } catch (Exception ex) {
    }
  }

  @Override
  public void doConfig(Config config) {
    config.setProperty(Property.ImporterFileExtension, ".sig");
    config.setProperty(Property.ImporterDocumentFileExtension, ".tif");
    config.setProperty(Property.ImporterIndexFileExtension, ".xml");
    config.setProperty(Property.ImporterMakeBackup, "TRUE");
    config.setProperty("Importer.className", "de.di.xml.importers.ELOImporter");
    config.setProperty(Property.CleanupDeleteByExtension, ".xml,.tif");
    config.setProperty(Property.BasicImporter, "de.di.xml.importers.gui.ELOImporter");
  }
}
