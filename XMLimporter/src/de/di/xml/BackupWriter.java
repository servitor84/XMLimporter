package de.di.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author A. Sopicki
 */
public class BackupWriter 
{
    public static void makeBackup(File srcFile, File destFile, Logger logger) throws IOException
    {
        java.io.FileInputStream ins = null;
        java.io.FileOutputStream outs = null;
        java.nio.channels.FileChannel iChannel = null;
        java.nio.channels.FileChannel oChannel = null;


        logger.debug("\t\t\t Making backup of file " + srcFile.toString());
        
        try
        {
            //if (!destFile.exists())
            //{
                logger.debug("\t\t\t\tCreating new file " + destFile.toString());
                
                // File Lock Facility                
                //FileChannel fileChannel = FileChannel.open(srcFile.toPath(), StandardOpenOption.READ);
                //FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, true);
                //System.out.println("Lock acquired: " + lock.isValid());
                //System.out.println("Lock is shared: " + lock.isShared());
                // +++
                destFile.createNewFile();  
                //fileChannel.close();
                              
            //}            
//            else
//            {                
//                /*
//                +++ STÜCK CODE VON SL - ANFANG +++
//                WENN DAS SELBE DOKUMENT SCHON IN BACKUP ORDNER EXISTIERT, DANN WIRD ES NOCHMAL DORT
//                VERSCHOBEN ABER DIESMAL MIT DEM ZEIITSTEMPEL BEIM KOPIEREN
//                -----------------------------------------------------------------------------------
//                */                                                 
//                String destFileTimestampStr = "";
//                File destFileTimestampFile = null;                
//                // Zeitstempel setzen
//                GregorianCalendar now = new GregorianCalendar();
//                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//                // xx.xx.xx xx:xx => xx.xx.xx_xxxx
//                String timeStamp = df.format(now.getTime()).replace(" ", "").replace(":", "");                                                              
//                // Umbennen des Dokumentes
//                String destFileStr = destFile.toString();
//                destFileTimestampStr = destFileStr.substring(0, destFileStr.lastIndexOf(".")) + 
//                        "_" + timeStamp + destFileStr.substring(destFileStr.lastIndexOf("."));  
//                
//                if(srcFile.getName().equals(destFile.getName())) {
//                    logger.info("\t\t\t\tWaiting for 60 seconds");
//                    try {
//                        Thread.sleep(60000);
//                    } catch (InterruptedException ex) {
//                        logger.info("\t\t\t\tException by sleep function");
//                    }
//                }
//                
//                Path source = Paths.get(destFile.toString());
//                Files.move(source, source.resolveSibling(destFileTimestampStr));                       
//                logger.debug("\t\t\t\tBackupfile " + destFile.toString() + " Creating new file with timestamp " + destFileTimestampStr);               
//            }

            ins = new java.io.FileInputStream(srcFile);
            outs = new java.io.FileOutputStream(destFile);
            
            iChannel = ins.getChannel();
            oChannel = outs.getChannel();

            oChannel.transferFrom(iChannel, 0, iChannel.size());

            iChannel.close();
            oChannel.close();
        }
        catch (IOException ioex)
        {
            if (iChannel != null)
            {
                iChannel.close();
            }
            if (oChannel != null)
            {
                oChannel.close();
            }
            throw ioex;
        }
    }
    
    private void save(File outputFile, String doc) throws IOException {
        Properties p = new Properties();
        p.setProperty(doc, "0");
        p.store(new FileOutputStream(outputFile), "DO NOT MODIFY PLEASE!!!");
    }
    
//            
//    private static String getCopyNr(String doc) throws FileNotFoundException, IOException {
//        FileReader fr = new FileReader("copynr.txt");
//        BufferedReader br = new BufferedReader(fr);
//        String line = "";
//        String fileName = "";
//        while ((line = br.readLine()) != null){          
//          fileName = line.substring(0, line.lastIndexOf("="));
//          if (fileName.equals(doc)) {
//              return line.substring(line.lastIndexOf("=")+1);
//          }
//        }
//        br.close();        
//        return null;
//    }
//    
//    private static void storeCopynr(String doc, int copynr) throws IOException {
//        FileWriter fw = new FileWriter("copynr.txt", true);
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write(doc);
//        bw.write("=");
//        bw.write(copynr++);
//        bw.write("\n");
//        bw.close();
//    }
}
