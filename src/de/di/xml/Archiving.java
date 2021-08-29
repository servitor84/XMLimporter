package de.di.xml;

import de.di.xml.importers.ELOImporter;
import de.di.xml.importers.ImportException;
import de.di.xml.initialization.CurrentMask;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Rahman
 */
public class Archiving {

    private String errorFolder;
    private String duplicatesFolder;
    private String backupFolder;    
    private String isCheckingSigFileSet;
    //private String isCreateSignalFileSet;
    
    public static String docFilePath;
    public static String description;
    
    public static Map<String, Integer> map;
    public static Map<String, Map<Integer, String>> maskLinesIdToName;
    
    private final Set<String> filesName = new HashSet<String>();
    private static final Set<String> lastcheckedFiles = new HashSet<String>();
    private static final Set<String> lastCreated = new HashSet<String>();
            
    private boolean isInList = false;    

    public void start(Properties settings, Logger logger) {

        errorFolder = settings.getProperty("Directories.ErrorOutput");
        backupFolder = settings.getProperty("Directories.Backup");
        duplicatesFolder = settings.getProperty("Directories.DuplicatesOutput");
        //isCreateSignalFileSet = settings.getProperty("Importer.CreateSignalFile");
        isCheckingSigFileSet = settings.getProperty("Importer.CheckSignalFile");
                
        // Create an array of signal file
//        if(isCreateSignalFileSet.equalsIgnoreCase("true")) {
//            //addSignalFiles(settings, logger);
//        }
        
        // SIG Datei ist der Signal Datei
        if(isCheckingSigFileSet.equalsIgnoreCase("TRUE")) {
            List<File> sigArray = getSignalFiles(settings);
            Importer.setOpenFiles(filesName);
            if (sigArray.size() > 0) {                                
                // Deliver a set of file name to JSP
                deliverFilesName(sigArray);                                            
                // Start with handling            
                for (int j = 0; j < sigArray.size(); j++) {
                    if (Importer.running == false) {
                        logger.info("Shutdown command received. Stopping work");
                        return;
                    }
                    filesName.remove(sigArray.get(j).getName());
                    Importer.setOpenFiles(filesName);
                    String indexFilePath = sigArray.get(j).getAbsolutePath();
                    while (!indexFilePath.substring(indexFilePath.length() - 1, indexFilePath.length()).equals(".")) {
                        indexFilePath = indexFilePath.substring(0, indexFilePath.length() - 1);
                    }
                    indexFilePath = indexFilePath.substring(0, indexFilePath.length() - 1);
                    indexFilePath = indexFilePath + settings.getProperty("Importer.IndexFileExtension");
                    File indexFile = new File(indexFilePath);                   
                    File sigFile = null;
                    File docFile = null;
                    IndexFileReader fileReader = null;

                    if (indexFile.exists()) {
                        logger.info(sigArray.size() + " " + settings.getProperty("Importer.FileExtension").toUpperCase() + " files found in " + settings.getProperty("Directories.Input") + " folder");
                        logger.info("Start working");
                        try {                                                
                            // Parse xml file
                            String sigFilePath = sigArray.get(j).getAbsolutePath();
                            sigFile = new File(sigFilePath);
                            fileReader = parseXMLFile(logger, indexFile);
                            docFile = new File(docFilePath);                        
                            // Wenn PDF gerade von einem anderen Programm benutzt wird - 21.08.2019 09:39
                            FileChannel channel = null;
                            try {
                                channel = new RandomAccessFile(docFile, "rw").getChannel();
                            } catch(FileNotFoundException ex) {
                                logger.info(ex.toString());
                                continue;
                            } finally {
                                if(channel != null) {
                                    channel.close();
                                }
                            }
                            // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++                        
                            // Do backup 
                            if (settings.containsKey("Importer.MakeBackup") && settings.getProperty("Importer.MakeBackup").equalsIgnoreCase("true")) {
                                backUp(logger, docFile, indexFile, sigFile);
                            } else {
                                logger.info("\t\tBackup function is deactivated");
                            }
                            // Archive in ELO                        
                            archiveDocument(logger, docFile, fileReader, settings);

                            // Delete already archived file
                            deleteAlreadyArchivedFile(logger, sigFile, docFile, indexFile);

                        } catch (IOException ioex) {
                            logger.warn("\t\t\t\tException : " + ioex.getMessage());
                            // -----                        
                            //Importer.reportException(Importer.getMailContent(ioex));
                            // -----
                            logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
                            createErrFile(errorFolder, ioex, sigArray.get(j), logger);
                            moveToFolder(docFile, sigArray.get(j), indexFile, logger, errorFolder);
                            Info.incCounterError();                        
                        } catch (ImportException iex) {
                            logger.warn("\t\t\t\tCannot archive document : " + iex.getMessage());

                            // -------------
                            //Importer.reportException(Importer.getMailContent(iex));
                            // -------------

                            if (iex.getMessage().contains("Duplicate")) {
                                logger.info("\t\t\t\tMove files to duplicates directory: " + duplicatesFolder);
                                moveToFolder(docFile, sigArray.get(j), indexFile, logger, duplicatesFolder);
                                createErrFile(duplicatesFolder, iex, sigArray.get(j), logger);                            
                            } else if(iex.getMessage().contains("file is open")) {
                                logger.info("\t\t\t\tfiles remain in input directory");
                            } else {
                                logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
                                moveToFolder(docFile, sigArray.get(j), indexFile, logger, errorFolder);
                                createErrFile(errorFolder, iex, sigArray.get(j), logger);
                                Info.incCounterError();                           
                            }
                        } catch (ParserException pex) {
                            logger.warn(pex.getMessage());    

                            // -------------
                            //Importer.reportException(Importer.getMailContent(pex));
                            // -------------

                            createErrFile(errorFolder, pex, sigArray.get(j), logger);
                            logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
                            /*build 00956*/
                            if (docFilePath != null) {
                                docFile = new File(docFilePath);
                            }
                            // SL - 03.12.2018
                            // Bei falschem Maskenname wird die PDF Datei nicht in Error-Ordner verschoben
                            /*else {
                                docFile = new File(pdf_document);
                            }*/
                            // ++++ ++++ ++++
                            logger.error("Trying to parse ended in error + docFilePath:" + docFilePath);
                            try {
                                /*00948*/
                                moveToFolder(docFile, sigArray.get(j), indexFile, logger, errorFolder);
                            } catch (Exception ex) {

                                // -------------
                                //Importer.reportException(Importer.getMailContent(pex));
                                // -------------

                                logger.error("Ignored exception, to keep server up");
                                logger.error(ex);
                            }
                            docFilePath = null;
                            Info.incCounterError();
                        } catch (Exception ex) {
                            logger.warn(ex.getMessage());     

                            // -------------
                            //Importer.reportException(Importer.getMailContent(ex));
                            // -------------

                            createErrFile(errorFolder, ex, sigArray.get(j), logger);
                            logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
                            logger.info("Generic branch!! OK?!");                                 
                            moveToFolder(docFile, sigArray.get(j), indexFile, logger, errorFolder);
                            Info.incCounterError();
                        }
                    } else {                    
                        for(int k = 0; k < Importer.getFilesWhichNotCanBeEdited().size(); k++) {
                            if(Importer.getFilesWhichNotCanBeEdited().get(k).equals(sigArray.get(j).getAbsolutePath())) {
                                isInList = true;     
                                break;
                            }
                        }
                        if(!isInList && Importer.getNumberOfAttemptsToMoveFile() < 10) {
                            logger.warn(settings.getProperty("Importer.IndexFileExtension").toUpperCase() + " file for : " + sigArray.get(j).getName() + " not found");
                            logger.info("Try to move file : " + sigArray.get(j).getName() + " to " + errorFolder);
                            if (moveFile(sigArray.get(j).getAbsolutePath(), errorFolder + File.separator + sigArray.get(j).getName())) {
                                logger.info("Move done");
                            } else {
                                // Zieldatei ist möglicherweise bereits vorhanden => sie muss gelöscht werden, damit renameTo() funktioniert
                                // new file
                                File fileInErrorFolder = new File(errorFolder + File.separator + sigArray.get(j).getName());
                                // old file
                                File fileInInputFolder = new File(sigArray.get(j).getAbsolutePath());
                                if(fileInErrorFolder.exists()) {
                                    fileInErrorFolder.delete();
                                    // old file into new file
                                    fileInInputFolder.renameTo(fileInErrorFolder);
                                } else {
                                    // Kann nicht verschoben werden aus einem anderen Grund außer der Existenz der Datei in Zielort
                                    logger.warn("Cannot move file : " + sigArray.get(j).getName() + " to " + errorFolder + File.separator + sigArray.get(j).getName());
                                    Importer.setNumberOfAttemptsToMoveFile();                                                                
                                }
                            }
                        } else {
                            if(!isInList) {
                                Importer.setFilesWhichNotCanBeEdited(sigArray.get(j).getAbsolutePath());                        
                            }
                        }
                    }
                }
            }
        } else if(isCheckingSigFileSet.equalsIgnoreCase("FALSE")) { // XML Datei ist die Signal Datei
            startWithNoSigFile(settings, logger);
        }               
    }
    

    private void startWithNoSigFile(Properties settings, Logger logger) {
        List<File> sigArray = getXMLFiles(settings);
        Importer.setOpenFiles(filesName);
        if (sigArray.size() > 0) {                                            
            deliverFilesName(sigArray);
            for(int j = 0; j < sigArray.size(); j++) {
                if (Importer.running == false) {
                    logger.info("Shutdown command received. Stopping work");
                    return;
                }
                filesName.remove(sigArray.get(j).getName());
                Importer.setOpenFiles(filesName);
                String docFilePath = sigArray.get(j).getAbsolutePath();
                while (!docFilePath.substring(docFilePath.length() - 1, docFilePath.length()).equals(".")) {
			docFilePath = docFilePath.substring(0, docFilePath.length() - 1);
		}
                docFilePath = docFilePath.substring(0, docFilePath.length() - 1);
		docFilePath = docFilePath + settings.getProperty("Importer.DocumentFileExtension");
		File docFile = new File(docFilePath);
                File sigFile = null;
                IndexFileReader fileReader = null;
                if (docFile.exists()) {
                    try {                                                			
			String xmlFilePath = sigArray.get(j).getAbsolutePath();
			sigFile = new File(xmlFilePath);
			fileReader = parseXMLFile(logger, sigFile);			               
			// Wenn PDF gerade von einem anderen Programm benutzt wird - 21.08.2019 09:39
			FileChannel channel = null;
			try {
				channel = new RandomAccessFile(docFile, "rw").getChannel();
			} catch(FileNotFoundException ex) {
				logger.info(ex.toString());
				continue;
			} finally {
				if(channel != null) {
					channel.close();
				}
			}
			// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++                        
			// Do backup 
			if (settings.containsKey("Importer.MakeBackup") && settings.getProperty("Importer.MakeBackup").equalsIgnoreCase("true")) {
				//backUp(logger, docFile, indexFile, sigFile);
                                BackupWriter.makeBackup(sigArray.get(j), new File(backupFolder + "/" + sigArray.get(j).getName()), logger);
				BackupWriter.makeBackup(docFile, new File(backupFolder + "/" + docFile.getName()), logger);
			} else {
				logger.info("\t\tBackup function is deactivated");
			}
			// Archive in ELO                        
			archiveDocument(logger, docFile, fileReader, settings);
			// Delete already archived file
			//deleteAlreadyArchivedFile(logger, sigFile, docFile, indexFile);
                        if (!sigFile.delete()) {
                            logger.warn("\t\t\tCannot delete " + sigFile.getAbsolutePath());
                        }
                        if (!docFile.delete()) {
                            logger.warn("\t\t\tCannot delete " + sigFile.getAbsolutePath());
                        }
                    } catch (IOException ioex) {
			logger.warn("\t\t\t\tException : " + ioex.getMessage());			
			logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
			createErrFile(errorFolder, ioex, sigArray.get(j), logger);
			moveToFolder_2(docFile, sigArray.get(j), logger, errorFolder);
			Info.incCounterError();                        
                    } catch (ImportException iex) {
                        logger.warn("\t\t\t\tCannot archive document : " + iex.getMessage());
                        if (iex.getMessage().contains("Duplicate")) {
                                logger.info("\t\t\t\tMove files to duplicates directory: " + duplicatesFolder);
                                moveToFolder_2(docFile, sigArray.get(j), logger, duplicatesFolder);
                                createErrFile(duplicatesFolder, iex, sigArray.get(j), logger);                            
                        } else if(iex.getMessage().contains("file is open")) {
                                logger.info("\t\t\t\tfiles remain in input directory");
                        } else {
                                logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
                                moveToFolder_2(docFile, sigArray.get(j), logger, errorFolder);
                                createErrFile(errorFolder, iex, sigArray.get(j), logger);
                                Info.incCounterError();                           
                        }
                    } catch (ParserException pex) {
                        logger.warn(pex.getMessage());    
                        createErrFile(errorFolder, pex, sigArray.get(j), logger);
                        logger.info("\t\t\t\tMove files to error directory: " + errorFolder);                        
                        if (docFilePath != null) {
                                docFile = new File(docFilePath);
                        }                       
                        logger.error("Trying to parse ended in error + docFilePath:" + docFilePath);
                        try {                            
                            moveToFolder_2(docFile, sigArray.get(j), logger, errorFolder);
                        } catch (Exception ex) {
                                logger.error("Ignored exception, to keep server up");
                                logger.error(ex);
                        }
                        docFilePath = null;
                        Info.incCounterError();
                    } catch (Exception ex) {
                        logger.warn(ex.getMessage());     
                        createErrFile(errorFolder, ex, sigArray.get(j), logger);
                        logger.info("\t\t\t\tMove files to error directory: " + errorFolder);
                        logger.info("Generic branch!! OK?!");                                 
                        moveToFolder_2(docFile, sigArray.get(j), logger, errorFolder);
                        Info.incCounterError();
                    }
                }
            }            
        }
    }
    
    private void createErrFile(String folder, Exception ex, File signalFile, Logger logger) {
        try {
            String rawFileName = signalFile.getName().substring(0, signalFile.getName().lastIndexOf("."));
            File errorFile = new File(folder + "\\" + rawFileName + ".err");
            errorFile.createNewFile();
            Writer output = new BufferedWriter(new FileWriter(errorFile));
            output.write(ex.getMessage());
            ex.printStackTrace(new PrintWriter(output));
            output.flush();
            output.close();
        } catch (Exception newEx) {
            logger.error(newEx);
            // -----                        
            //Importer.reportException(Importer.getMailContent(newEx));
            // -----
        }
    }

    private boolean moveFile(String oldPath, String newPath) {
        File oldName = new File(oldPath);
        File newName = new File(newPath);       
        return oldName.exists() && oldName.renameTo(newName);        
    }

    private void moveToFolder(File docFile, File sigFile, File xmlFile, Logger logger, String folder) {
        //sigfile = .sig, xmlfile=.xml, docfile=.pdf
        if (Importer.running == false) {
            logger.debug("Ignoring request for move because importer is shutting down");
            return;
        }
        logger.debug("in movefolder");
        String xmlFileName = xmlFile.getName();
        String docFileName;
        if (docFile != null) {
            docFileName = docFile.getName();
        } else {
            docFileName = sigFile.getName().toLowerCase().replace(".sig", ".pdf"); // sig extention was lowercase and the replacement diden't work
            docFile = new File(sigFile.getAbsoluteFile().getParent() + File.separator + docFileName); // getParent() method was forgotten
        }
        String sigFileName = sigFile.getName();
        logger.debug("SigFileName: " + sigFileName);
        logger.debug("SigFile path : " + folder + File.separator + sigFileName);
        logger.debug("Sigfile exists : " + new File(folder + File.separator + sigFileName).exists());
        if (folder.equals(errorFolder) && new File(folder + File.separator + sigFileName).exists()) {
            logger.debug(" errorFolder: " + errorFolder + "folder: " + folder);
            int i = 0;
            String ext = "_00" + i;
            sigFileName = sigFileName.substring(0, sigFileName.length() - 4) + ext + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
            while (new File(folder + File.separator + sigFileName).exists()) {
                i++;
                if (i >= 10) {
                    ext = "_0" + i;
                } else if (i >= 100) {
                    ext = "_" + i;
                } else {
                    ext = "_00" + i;
                }
                sigFileName = sigFileName.substring(0, sigFileName.indexOf("_00")) + ext + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
            }
            docFileName = docFileName.substring(0, docFileName.length() - 4) + ext + docFileName.substring(docFileName.length() - 4, docFileName.length());
            xmlFileName = xmlFileName.substring(0, xmlFileName.length() - 4) + ext + xmlFileName.substring(xmlFileName.length() - 4, xmlFileName.length());
        }
        // ------------------------------------------------------------------------------------------------------------
        if (folder.equals(duplicatesFolder) && new File(folder + File.separator + sigFileName).exists()) {
            logger.debug(" duplicatesFolder: " + duplicatesFolder + "folder: " + folder);
//            int i = 0;
//            String ext = "_00" + i;
//            sigFileName = sigFileName.substring(0, sigFileName.length() - 4) + ext + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
//            while (new File(folder + File.separator + sigFileName).exists()) {
//                i++;
//                if (i >= 10) {
//                    ext = "_0" + i;
//                } else if (i >= 100) {
//                    ext = "_" + i;
//                } else {
//                    ext = "_00" + i;
//                }
//                sigFileName = sigFileName.substring(0, sigFileName.indexOf("_00")) + ext + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
//            }

            // 14.05.2019 - TimeStamp an den Namen der Datei anhängen, wenn die Datei schon in Duplicate-Ordner vorhanden ist
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String suffix = timestamp.toString().replace(" ", "__").replace(".", "__").replaceAll(":", "-");
            
            sigFileName = sigFileName.substring(0, sigFileName.indexOf(".")) + "_" + suffix + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
            docFileName = docFileName.substring(0, docFileName.length() - 4) + /*ext*/ "_" + suffix + docFileName.substring(docFileName.length() - 4, docFileName.length());
            xmlFileName = xmlFileName.substring(0, xmlFileName.length() - 4) + /*ext*/ "_" + suffix + xmlFileName.substring(xmlFileName.length() - 4, xmlFileName.length());
        }
        // ------------------------------------------------------------------------------------------------------------
        if (moveFile(sigFile.getAbsolutePath(), folder + File.separator + sigFileName)) {
            logger.debug("\t\t\t\tSigFile " + sigFileName + " is moved");
        } else {
            logger.warn("\t\t\t\tSigFile Cannot move " + sigFileName + " to " + folder + File.separator + sigFileName);
            // +++++++ 23.04.2019 +++++++
            // SIG-Datei aus Input-Ordner löschen
            if(folder.contains("duplicates") || folder.contains("Duplicates")) {
                if(sigFile.delete()) {
                    logger.info("\t\t\t\tSIG file is already existing in Input directory");
                    logger.info("\t\t\t\tdelete SIG file from Input");
                } else {
                    logger.info("\t\t\t\tdelete SIG file is failed");
                }
            }
            // ++++++++++++++++++++++++++++
        }
        if (xmlFile.exists()) {            
            if (moveFile(xmlFile.getAbsolutePath(), folder + File.separator + xmlFileName)) {
                logger.debug("\t\t\t\tXmlFile " + xmlFileName + " is moved");
            } else {                                                                        
                logger.debug("\t\t\t\tXmlFile Cannot move " + xmlFileName + " to " + folder + File.separator + xmlFileName);
                // +++++++ 23.04.2019 +++++++
                // XML-Datei aus Input-Ordner löschen
                if(folder.contains("duplicates") || folder.contains("Duplicates")) {
                    if(xmlFile.delete()) {
                        logger.info("\t\t\t\tXML file is already existing in Input directory");
                        logger.info("\t\t\t\tdelete XML file from Input");
                    } else {
                        logger.info("\t\t\t\tdelete XML file is failed");
                    }
                }
                // ++++++++++++++++++++++++++++
            }
        } else {
            logger.warn("\t\t\t\tXmlFile Cannot move " + xmlFileName + " to " + folder + File.separator + xmlFileName + " because it doesn't exist");
        }
        if (docFile.exists()) {
            if (moveFile(docFile.getAbsolutePath(), folder + File.separator + docFileName)) {
                logger.debug("\t\t\t\tDocFile " + docFileName + " is moved");
            } else {
                logger.warn("\t\t\t\tDocFile Cannot move " + docFileName + " to " + folder + File.separator + docFileName);
                // +++++++ 23.04.2019 +++++++
                // DOC-Datei aus Input-Ordner löschen
                if(folder.contains("duplicates") || folder.contains("Duplicates")) {
                    if(docFile.delete()) {
                        logger.info("\t\t\t\tDOC file is already existing in Input directory");
                        logger.info("\t\t\t\tdelete DOC file from Input");
                    } else {
                        logger.info("\t\t\t\tdelete DOC file is failed");
                    }
                }
                // ++++++++++++++++++++++++++++
            }
        } else {
            logger.warn("\t\t\t\tDocFile Cannot move " + docFileName + " to " + folder + File.separator + docFileName + " because it doesn't exist");
        }
    }
    
    private void moveToFolder_2(File docFile, File sigFile, Logger logger, String folder) {
        String orgFileName = docFile.getName();
        String sigFileName = sigFile.getName();
        if (folder.equals(errorFolder) && new File(folder + "/" + sigFileName).exists()) {
            int i = 0;
            String ext = "_00" + i;
            sigFileName = sigFileName.substring(0, sigFileName.length() - 4) + ext + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
            while (new File(folder + "/" + sigFileName).exists()) {
                i++;
                if (i >= 10) {
                    ext = "_0" + i;
                } else if (i >= 100) {
                    ext = "_" + i;
                } else {
                    ext = "_00" + i;
                }
                sigFileName = sigFileName.substring(0, sigFileName.indexOf("_00")) + ext + sigFileName.substring(sigFileName.length() - 4, sigFileName.length());
            }
            orgFileName = orgFileName.substring(0, orgFileName.length() - 4) + ext + orgFileName.substring(orgFileName.length() - 4, orgFileName.length());
        }
        if (moveFile(sigFile.getAbsolutePath(), folder + "/" + sigFileName)) {
            logger.debug("\t\t\t\t" + sigFileName + " is moved");
        } else {
            logger.warn("\t\t\t\tCannot move " + sigFileName + " to " + folder + "/" + sigFileName);
        }
        if (new File(docFile.getAbsolutePath()).exists()) {
            if (moveFile(docFile.getAbsolutePath(), folder + "/" + orgFileName)) {
                logger.debug("\t\t\t\t" + orgFileName + " is moved");
            } else {
                logger.warn("\t\t\t\tCannot move " + orgFileName + " to " + folder + "/" + orgFileName);
            }
        }
    }

    private void deliverFilesName(List<File> sigArray) {
        Iterator<File> it = sigArray.iterator();
        Set<String> setOfFileName = new TreeSet<>();
        while (it.hasNext()) {
            setOfFileName.add(it.next().getName());
        }
        Importer.setOpenFiles(setOfFileName);
    }

    private List<File> getSignalFiles(Properties settings) {
        File f = new File(settings.getProperty("Directories.Input"));
        File[] fileArray = f.listFiles();
        List<File> sigArray = new ArrayList<File>();
        for (int i = 0; i < fileArray.length; i++) {
            String fName = fileArray[i].getName();
            while (fName.contains(".")) {
                fName = fName.substring(fName.indexOf(".") + 1, fName.length());
            }
            fName = "." + fName;
            if (fName.equalsIgnoreCase(settings.getProperty("Importer.FileExtension"))) {
                sigArray.add(fileArray[i]);
                filesName.add(fileArray[i].getName());
            }
        }
        return sigArray;
    }
    
    /**
     * Füllt eine Liste mit XML Dateien, die sich in Input befinden
     * @param settings Konfiguration Daten
     * @return Liste der XML Dateien
     */
    private List<File> getXMLFiles(Properties settings) {
        File f = new File(settings.getProperty("Directories.Input"));
        File[] fileArray = f.listFiles();
        List<File> xmlArray = new ArrayList<File>();
        for (int i = 0; i < fileArray.length; i++) {
            String fName = fileArray[i].getName();
            while (fName.contains(".")) {
                fName = fName.substring(fName.indexOf(".") + 1, fName.length());
            }
            fName = "." + fName;
            if (fName.equalsIgnoreCase(settings.getProperty("Importer.IndexFileExtension"))) {
                xmlArray.add(fileArray[i]);
                filesName.add(fileArray[i].getName());
            }
        }
        return xmlArray;
    }

    private void backUp(Logger logger, File docFile, File indexFile, File sigFile) throws IOException {
        logger.info("\t\t Start to create backup");
        if (docFile.exists()) {
            BackupWriter.makeBackup(indexFile, new File(backupFolder + File.separator + indexFile.getName()), logger);
            BackupWriter.makeBackup(sigFile, new File(backupFolder + File.separator + sigFile.getName()), logger);
            BackupWriter.makeBackup(docFile, new File(backupFolder + File.separator + docFile.getName()), logger);
        } else {
            throw new IOException("Cannot find file : " + docFile.getName());
        }
        logger.info("\t\tBackup done.");
    }

    private IndexFileReader parseXMLFile(Logger logger, File indexFile) throws Exception, ParserException {
        logger.info("\t\tStart to parse : " + indexFile.getName());
        IndexFileReader fileReader = new IndexFileReader(indexFile, logger, map);
        fileReader.parse();
        logger.info("\t\t Parsing done.");
        return fileReader;
    }

    private void deleteAlreadyArchivedFile(Logger logger, File sigFile, File docFile, File indexFile) {
        logger.info("\t\t Start to delete archived files ");                                      
        
        if (!sigFile.delete()) {
            logger.warn("\t\t\tCannot delete " + sigFile.getAbsolutePath());
        }
        if (!docFile.delete()) {
            logger.warn("\t\t\tCannot delete " + docFile.getAbsolutePath());
        }
        if (!indexFile.delete()) {
            logger.warn("\t\t\tCannot delete " + indexFile.getAbsolutePath());
        }
        logger.info("\t\t Delete done.");
    }

    private void archiveDocument(Logger logger, File docFile, IndexFileReader fileReader, Properties settings) throws IOException, ImportException {
        logger.info("\t\t Start achiving ");
        if (!docFile.exists()) {
            throw new IOException("File \'" + docFile.getName() + "\' not found");
        }
        if (docFile.length() == 0) {
            throw new IOException("File \'" + docFile.getName() + "\' is empty");
        }
        
        /**
         * Wenn docFile schon geöffnet ist, dann die Archivierung abbrechen
         */        
        File sameFileName = new File(docFile.getAbsolutePath());
        if(docFile.renameTo(sameFileName)) { // Datei ist nicht geöffnet
            ELOImporter imp = new ELOImporter(logger);
            Map<String, IndexValue> map = fileReader.getIndexData();
            if (settings.containsKey("Importer.CheckDuplicates") && settings.getProperty("Importer.CheckDuplicates") != null && settings.getProperty("Importer.CheckDuplicates").equalsIgnoreCase("true")) {
                logger.debug("\t\t\t Check duplicates function is activated");
                imp.setCheckDuplicates(true);
                if (Check.hasNoDuplicates(docFile, settings, logger)) {
                    throw new ImportException("Duplicate");
                } else {
                    logger.debug("\t\t\t No duplicates in ELO");
                    if (map.containsKey("versioning")) {
                        imp.importVersion(map, docFile, settings, logger);
                    } else {
                        imp.importDocument(map);
                    }
                }
            } else {
                logger.info("\t\t\t Check duplicates function is deactivated");
                imp.setCheckDuplicates(false);
                if (map.containsKey("versioning")) {
                    imp.importVersion(map, docFile, settings, logger);
                } else {
                    imp.importDocument(map);
                }
            }
            logger.info("\t\t Archiving done.");
        } else {
            logger.debug("\t\t\t Archiving process can not be performed, file is open");
            throw new ImportException("Archiving process can not be performed, file is open");
        }                        
    }

//    public static void main(String[] args) throws FileNotFoundException, Exception {
//        Logger logger = Logger.getLogger(Archiving.class);
//        Archiving archiving = new Archiving();
//        Properties ixProps = new Properties();
//        ixProps.put("IndexServer.URL", "http://localhost:9090/ix-elo/ix");
//        ixProps.put("IndexServer.User", "Administrator");
//        ixProps.put("IndexServer.Password", "elo");
//        CurrentMask.pickUpFromIX(null, logger);
//        
//        File docFile = new File("C:\\DOKinform\\XMLimporter\\in\\ERPcon-000000000000086_TKOV_20170405191021.xml");
//        IndexFileReader idr = archiving.parseXMLFile(logger, docFile);
//        Properties importProperties = new Properties();
//        archiving.archiveDocument(logger, docFile, idr, importProperties);
//    }  
    
    private void createXMLInErrorFolder(File xmlFile, String folder)
    {
        FileInputStream fr = null;
        File xmlErrorFile = null;
        BufferedWriter bw = null;
        
        String line = "";                
        try {
             fr = new FileInputStream(xmlFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(fr, "UTF-16LE"));
             
             xmlErrorFile = new File(folder + File.separator + xmlFile.getName());
             FileOutputStream fw = new FileOutputStream(xmlErrorFile);
             bw = new BufferedWriter(new OutputStreamWriter(fw, "UTF-16LE"));
            while((line = br.readLine()) != null)
            {
                bw.write(line + '\n');                
            }
        } catch (Exception ex) {}  
        finally
        {
            try { bw.close(); } catch(Exception e) {}
        }
    }
}
