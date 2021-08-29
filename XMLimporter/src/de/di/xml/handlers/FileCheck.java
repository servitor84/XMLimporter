package de.di.xml.handlers;

import java.io.File;

/**
 * Class that associates a file with a given file size
 * which may be used for file checking.
 *
 * @author A. Sopicki
 */
public class FileCheck {

  private long size;
  private File file;

  public FileCheck(File f, long size) {
    file = f;
    this.size = size;
  }

  /**
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * @return the file
   */
  public File getFile() {
    return file;
  }
}
