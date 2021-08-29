/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.di.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.apache.log4j.*;

/**
 *
 * @author samir.lebaal
 */
public class IndexFileFilter {    
    private String result = null;
    private final InputStreamReader tmpReader = null;

    private static Logger logger;
    private File temp;
    
    public IndexFileFilter() {
        logger = Logger.getLogger(this.getClass().getPackage().getName());
    }

    public static String xml_replaceInvalidCharakters(String xmlFile) {
        String docfile = "";
        
        IndexFileFilter iff = new IndexFileFilter();
        try {
            iff.setTemp(new File(xmlFile));
            logger.debug("\t\t\t\t -- Start writing data into xml file -" + iff.getTemp().getName()+"- ");
        } catch (Exception ex) {
                logger.log(Level.FATAL, "\t\t\t\t -- Unable to locate template file.", ex);
        }
        FileInputStream inputStream = null;
        InputStreamReader isr = null;
        BufferedReader topBr = null;
        StringBuilder topSb = null;        
        try {
            // Reading from xml file 
            inputStream = new FileInputStream(iff.getTemp());            
            isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            topBr = new BufferedReader(isr);
            topSb = new StringBuilder();
            docfile = iff.replaceParamsForIndexFile(topBr, topSb);
            
            // Writing in xml file
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile),StandardCharsets.UTF_8));
            logger.debug("\t\t\t\t\t - Writing results ");
            writer.write(iff.getResult());
            writer.close();
        } catch (IOException ex) {            
            logger.error("\t\t\t\t\t -- Writing data to file Failed: ", ex);
        } finally {
            try {
                inputStream.close();
                topBr.close();
                isr.close();                
                logger.debug("\t\t\t\t -- End writing data into xml file  " + iff.getTemp().getName());
            } catch (IOException ex) {
                    logger.error("\t\t\t\t -- Closing Stream Buffer Failed: ", ex);
            }
        }
        return docfile;
    }

    private String replaceParamsForIndexFile(BufferedReader topBr, StringBuilder topSb) {
        String docfile = "";
        
        String line;
        
        String[] special_charakter = {"\'", "\"", "<", ">", "&"};
        String[] special_charakter_encode = {"&apos;", "&quot;", "&lt;", "&gt;", "&amp;"};
        try {
            line = topBr.readLine();            
            while (line != null) {
                // "name=\"7\"" => Subject
                if(line.contains("desc") || /*line.contains("name=\"7\"") ||*/ line.contains("destination")) {
                    // String to analize is in VALUE attribute
                    String temp_line = line.substring(line.indexOf("value"), line.indexOf("/>"));
                    
                    for(int i = 0; i < special_charakter.length; i++) {                                                
                        if(temp_line.substring(temp_line.indexOf("\"")+1, temp_line.lastIndexOf("\"")).contains(special_charakter[i])) {
//                            if(special_charakter[i].equals("&") ) {
//                                if(!Character.toString(line.charAt(line.indexOf(special_charakter[i])+1)).equals("a") && 
//                                   !Character.toString(line.charAt(line.indexOf(special_charakter[i])+2)).equals("m") &&
//                                   !Character.toString(line.charAt(line.indexOf(special_charakter[i])+3)).equals("p") &&
//                                   !Character.toString(line.charAt(line.indexOf(special_charakter[i])+4)).equals(";") ) {
//                                    line = line.replace((Character.toString(line.charAt(line.indexOf(special_charakter[i])))), special_charakter_encode[i]);
//                                }
//                            } else {
                                line = line.replace((Character.toString(line.charAt(line.indexOf(special_charakter[i])))), special_charakter_encode[i]);
//                            }                       
                        }
                    }                                      
                } else if(line.contains("docfile")) {
                    // String to analize is in NAME attribute
                    String temp_line = line.substring(line.indexOf("name"), line.indexOf("/>"));
                    docfile = temp_line.substring(temp_line.indexOf("\"")+1, temp_line.lastIndexOf("\""));
                    
                    for(int j = 0; j < special_charakter.length; j++) {                        
                        if(docfile.contains(special_charakter[j])) {
                            if(special_charakter[j].equals("&") ) {
                                if(!Character.toString(line.charAt(line.indexOf(special_charakter[j])+1)).equals("a") && 
                                   !Character.toString(line.charAt(line.indexOf(special_charakter[j])+2)).equals("m") &&
                                   !Character.toString(line.charAt(line.indexOf(special_charakter[j])+3)).equals("p") &&
                                   !Character.toString(line.charAt(line.indexOf(special_charakter[j])+4)).equals(";") ) {
                                    line = line.replace((Character.toString(line.charAt(line.indexOf(special_charakter[j])))), special_charakter_encode[j]);
                                }
                            } else {
                                line = line.replace((Character.toString(line.charAt(line.indexOf(special_charakter[j])))), special_charakter_encode[j]);
                            }                       
                        }
                    }
                }
                topSb.append(line).append("\n");
                line = topBr.readLine();
            }
            result = topSb.toString();
            logger.debug("\t\t\t\t\t - Replacing special charakters into index file ");           
        } catch (IOException ex) {
            logger.error("\t\t\t\t -- Error by replacing special charakters into index file failed", ex);
        }
        return docfile;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public File getTemp() {
        return temp;
    }

    public void setTemp(File temp) {
        this.temp = temp;
    }



}
