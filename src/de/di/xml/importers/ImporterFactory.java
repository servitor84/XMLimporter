package de.di.xml.importers;

/**
 *
 * @author A. Sopicki
 */
public class ImporterFactory {

    public static Importer getInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        Importer imp = (Importer) Class.forName(className).newInstance();

        return imp;
    }
    
    public static Importer getInstance(Class c) throws InstantiationException, IllegalAccessException {

        Importer imp = (Importer) c.newInstance();

        return imp;
    }
}
