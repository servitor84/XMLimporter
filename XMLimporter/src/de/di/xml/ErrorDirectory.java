
package de.di.xml;

import java.io.File;
import java.text.DecimalFormat;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class ErrorDirectory extends java.io.File {
    
    private Logger logger = null;
    
    public ErrorDirectory(String pathname) {
        super(pathname);
        
        logger = Logger.getLogger(getClass());
    }
    
    public boolean moveToDir(java.util.List<java.io.File> files) {
        
        if ( files.size() == 0 ) {
            return true;
        }
        
        java.io.File f = files.get(0);
        String extension = f.getName().substring(f.getName().
                lastIndexOf("."));
        String destFileName = f.getName().substring(0,
                f.getName().
                lastIndexOf("."));

        //check for files with the same name in the error output directory
        java.io.File[] fileList = listFiles(new ErrorDirectoryFilter(this,
                destFileName,
                extension));
        
        //add an index to the file to avoid overwriting existing files
        String index = "_0000";

        //get that last index used and increment it by one
        if (fileList != null && fileList.length > 0) {
            java.util.Arrays.sort(fileList, null);
            java.io.File lastIndexFile = fileList[fileList.length - 1];
            String temp = lastIndexFile.getName().substring(destFileName.length());
            temp = temp.substring(1, 5);

            try {
                int i = Integer.parseInt(temp);

                DecimalFormat dformat = new DecimalFormat("_0000");
                dformat.setMinimumIntegerDigits(4);
                index = dformat.format(i + 1);
            } catch (NumberFormatException nfe) {
            }
        }
        
         //try to move the files
        try {
            java.io.File destFile = null;
            
            for(java.io.File srcFile: files) {
                extension = srcFile.getName().substring(srcFile.getName().lastIndexOf("."));
                destFileName = srcFile.getName().substring(0, srcFile.getName().lastIndexOf("."));
                destFile = new java.io.File(this, destFileName + index + extension);
                
                if (srcFile.exists() && !moveFile(srcFile, destFile)) {
                    return false;
                }
            }
        } catch (Exception ex) {
            logger.log(Level.FATAL, "Unable to move file to  directory " + 
                    this.getAbsolutePath() + " because of an exception. Aborting.", ex);
            // -----                        
            //Importer.reportException(Importer.getMailContent(ex));
            // -----
            return false;
        }
        
        return true;
    }

    private boolean moveFile(File srcFile, File destFile) {
        logger.debug("Moving file " + srcFile.getAbsolutePath() +
                " to destination " + destFile.getAbsolutePath());

        return srcFile.renameTo(destFile);
    }
}
