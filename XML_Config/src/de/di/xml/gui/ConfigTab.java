package de.di.xml.gui;

/**
 *
 * @author A. Sopicki
 */
public interface ConfigTab 
{
    javax.swing.JPanel getJPanel();
    
    String getTitle();
    
    void setConfig(Config c);
}
