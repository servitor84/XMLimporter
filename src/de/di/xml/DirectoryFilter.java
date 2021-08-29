package de.di.xml;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author A. Sopicki
 */
public class DirectoryFilter implements FilenameFilter
{
    private File directory = null;
    
    private String extension = null;
    
    DirectoryFilter(File directory, String extension)
    {
        if ( directory == null )
            throw new IllegalArgumentException("Directory may not be null");
        
        if ( extension == null )
            throw new IllegalArgumentException("Extension may not be null");
        
        this.directory = directory;
        this.extension = extension;
    }

    @Override
    public boolean accept(File dir, String name)
    {
        if ( dir.compareTo(directory) == 0 && 
            name.endsWith(extension))
            return true;
        
        return false;
    }

}
