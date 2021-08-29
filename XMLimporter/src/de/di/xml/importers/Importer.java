package de.di.xml.importers;


import de.di.xml.IndexValue;

/**
 *
 * @author A. Sopicki
 */
public interface Importer {
    
    public void ping();
    
//    public void importDocument(java.util.Map<String, IndexValue> indexData) throws ImportException;

    public void importDocument(java.util.Map<String, IndexValue> indexData) throws ImportException;
    
    public void logoff();
    
    public String getConnectionUrl();

    public void setConnectionUrl(String Url);

    public String getUserName();

    public void setUserName(String userName);

    public String getPassword();

    public void setPassword(String password);
    
    public boolean isCheckDuplicates();
    
    public void setCheckDuplicates(boolean check);
}
