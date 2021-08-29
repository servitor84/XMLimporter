package de.di.xml;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author A. Sopicki
 */
public class ErrorDirectoryFilter implements FilenameFilter
{

    private File directory = null;

    private String name = null;

    private String extension = null;

    public ErrorDirectoryFilter(File directory, String name, String extension)
    {
        this.directory = directory;
        this.name = name;
        this.extension = extension;
    }

    @Override
    public boolean accept(File dir, String filename)
    {
        if (dir.compareTo(directory) == 0 && filename.startsWith(name) && filename.endsWith(extension))
        {
            return true;
        }

        return false;
    }
}
