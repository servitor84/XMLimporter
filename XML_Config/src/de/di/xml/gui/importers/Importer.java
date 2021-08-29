
package de.di.xml.gui.importers;

import de.di.xml.gui.Config;

/**
 *
 * @author A. Sopicki
 */
public abstract class Importer {
    protected String displayName;
    
    public abstract void doConfig(Config config);

    @Override
    public String toString() {
        return displayName;
    }
}
