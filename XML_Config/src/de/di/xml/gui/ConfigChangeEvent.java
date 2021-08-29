package de.di.xml.gui;

/**
 *
 * @author A. Sopicki
 */
public class ConfigChangeEvent 
{   
    private String key = null;
    
    ConfigChangeEvent(String key)
    {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
