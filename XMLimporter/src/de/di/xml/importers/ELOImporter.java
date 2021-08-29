package de.di.xml.importers;

import de.di.elo.client.ELOClient;
import de.di.xml.Archiving;
import de.elo.ix.client.DocVersion;
import de.elo.ix.client.Document;
import de.elo.ix.client.EditInfo;
import de.elo.ix.client.EditInfoC;
import de.elo.ix.client.IXClient;
import de.elo.ix.client.LockC;
import de.elo.ix.client.ObjKey;
import de.elo.ix.client.Sord;
import de.elo.ix.client.SordC;
import de.elo.ix.client.WFTypeC;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import de.elo.utils.net.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import de.di.xml.Info;
import de.di.xml.IndexValue;
import de.elo.ix.client.ArcPath;
import de.elo.ix.client.DocMask;
import de.elo.ix.client.DocMaskLine;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author A. Sopicki
 */
public class ELOImporter implements Importer {

    private String connectionUrl = null;
    private String userName = null;
    private String password = null;
    private int objPath;
    private Logger logger = null;
    private SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private long bestBefore = 0;
    private ELOClient eloClient = null;
    private boolean checkDuplicates = true;
    private boolean first;

    ELOImporter() {
        this(null);
    }

    public ELOImporter(Logger logger) {
        this.logger = logger;

        if (logger == null) {
            this.logger = Logger.getLogger(getClass());
        }
    }

    public void importVersion(Map<String, IndexValue> indexData, File docFile, Properties settings, Logger logger) throws ImportException, IOException {
        try {
            if (eloClient == null) {
                eloClient = new ELOClient();
            }
            if (eloClient.findByIndex(indexData, logger)) {
                logger.info("Importing new version !");
                importVersionFinel(indexData, docFile, settings, logger);
            } else {
                importDocument(indexData);
            }
        } catch (ImportException | IOException ex) {            
            throw ex;
        } 
//        finally {
//            eloClient.close();
//        }
    }

    @Override
    public void importDocument(Map<String, IndexValue> indexData) throws ImportException {
        first = true;
        if (eloClient == null) {
            eloClient = new ELOClient();
        }
        try {
            String destination;
            String type;
            String dtype = null;
            String desc;
            String path;
            String memo = null;
            String idate;
            String xdate;
            String destinationType;
            IndexValue indexList;
            IndexValue destList;
            IndexValue workflowList;

            //get basic information to import document
            destList = indexData.get("destlist");

            if (destList == null) {
                logger.error("Missing destination for document!");
                throw new ImportException("Unknown destination for document");
            }
            //path, id or guid of the parent element for the document
            /*commented due to bug 17*/
 /*destination = removeWhiteSpace(destList.getValue()).replaceAll("[^a-zA-Z0-9\\[\\]\\¶\\däüöÄÜÖ\\(\\)\\,\\. ]", "");*/
            destination = removeWhiteSpace(destList.getValue());

            if (destList.getNext() == null) {
                destinationType = null;
            } else {
                destinationType = destList.getNext().getValue();
            }
            destList = destList.getSibling();

            IndexValue v = indexData.get("type");

            if (v == null) {
                logger.error("Missing type of the document!");
                throw new ImportException("Unknown type for document");
            }

            //mask or id of the mask for the Sord of the document
            type = v.getValue();

            v = indexData.get("desc");

            if (v == null) {
                logger.error("Missing description of the document!");
                throw new ImportException("Unknown description for document");
            }

            //short description of the element
            desc = v.getValue();
            /* 05.03.2019
            Die Länge der Zeichenkette in desc kontrollieren
            */
            if(desc.length() > 128) {
                desc = desc.substring(0, 127);
            }
            // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++SL

            java.util.Date d = new java.util.Date();
            v = indexData.get("idate");
            //internal document date (time of import)
            if (v == null) {
                idate = createShortIsoDate(d);
            } else if (isValidIsoDate(v.getValue())) {
                idate = v.getValue();
            } else {
                idate = createShortIsoDate(d);
            }

            v = indexData.get("xdate");
            //external document date (time of document creation)
            if (v == null) {
                xdate = createShortIsoDate(d);
            } else if (isValidIsoDate(v.getValue())) {
                xdate = v.getValue();
            } else {
                xdate = createShortIsoDate(d);
            }

            v = indexData.get("docfile");
            if (v == null) {
                logger.error("Missing document file path!");
                throw new ImportException("No document file path specified!");
            }

            //path for the document file
            path = v.getValue();

            v = indexData.get("dtype");

            //dtype is set
            if (v != null) {
                dtype = v.getValue();
            }                        

            indexList = indexData.get("indexlist");

            if (indexList == null) {
                logger.error("Missing index data for the document!");
                throw new ImportException("Unknown index data for document");
            }

            File file = new File(path);

            if (!file.exists()) {
                throw new ImportException("Can't import non exisiting file!");
            }

            if (!file.canRead()) {
                throw new ImportException("Can't import a file that is not readable!");
            }

            if (!file.canWrite()) {
                throw new ImportException("Can't import a file that is not writable!");
            }

            v = indexData.get("memo");

            //add memo data to document
            if (v != null) {
                memo = v.getValue().replace("\\n", "\n");
            }

            // --- SL: Parameter edi hinzugefügt. Wird benötigt - in der Meth. getIndexValue() - beim Vergleichen der Indexliste in der 
            // XML Datei mit den Gruppennamen der Maske
            EditInfo edi = eloClient.createSord(null, type, EditInfoC.mbSord);
            // --- END
            try {
                destination = buildPath(destination, indexData.get("indexlist"), idate, xdate, desc, edi);
            } catch (IllegalExpressionException ex) {               
                throw new ImportException("Can't import file due to illegal expression in destination path", ex);
            }

            EditInfo info = null;
            try {
                info = eloClient.checkoutSord("ARCPATH:" + destination, EditInfoC.mbSord, LockC.NO);
//                info = indexClient.ix.checkoutSord(clientInfo, "ARCPATH:" + destination, EditInfoC.mbSord, LockC.YES);
            } catch (IOException rex) {
                //path invalid
                if (!rex.getMessage().contains("[ELOIX:5023]")) {
//                    this.logger.error("Exception while checking out destination.", rex);
//                    throw rex;
                }
                this.logger.info("\t\t\tNon existing archive path: '" + destination + "'");                
            }

            Sord sord = null, docSord = null;

            if (info == null) {
                if (!Archiving.map.containsKey(destinationType)) {
                    logger.error("Mask with name: " + destinationType + " not found in ELO");
                    throw new Exception("Mask with name: " + destinationType + " not found in ELO");
                }
                ELOStructureBuilder builder = ELOStructureBuilder.getInstance();
                sord = builder.createPath(destination, destinationType, eloClient, getUserName());

//                sord = builder.createPath(destination, destinationType, clientInfo, lr.getUser().getName(), indexClient);
            } else {
                sord = info.getSord();
            }

            /**
             * Duplicate file checking
             */
            if (checkDuplicates) {
                MD5Digest digest = new MD5Digest();
                String md5 = null;
                try {
                    md5 = digest.digest(file);
                } catch (Exception ex) {                    
                    throw new ImportException("Unable to calculate MD5 sum", ex);                    
                }

                docSord = eloClient.findDoc(md5);

                /*
                 * we found a match for the md5 sum and the parent is the same and
                 * the name is the same
                 */
                if (docSord != null) {
                    if (sord.getLockId() != -1) {
                        eloClient.checkinSord(sord, SordC.mbOnlyUnlock, LockC.NO);
                    }
                    throw new DuplicateException("Duplicate file in archive found");
                }
            }

            info = eloClient.createDoc(sord.getGuid(), type, null, EditInfoC.mbSord);
//            info = indexClient.ix.createDoc(clientInfo, sord.getGuid(), type, userName, EditInfoC.mbSord);

            docSord = info.getSord();
            docSord.setName(desc);
            docSord.setXDateIso(xdate);
            docSord.setIDateIso(idate);
            if (dtype != null) {
                try {
                    int documentType = Integer.parseInt(dtype);

                    if (documentType < 254) {
                        throw new ImportException("Illegal dtype value '" + documentType
                                + "' for document.");
                    } else if (documentType > 283) {
                        throw new ImportException("Illegal dtype value '" + documentType + "' for document. "
                                + "Only build in document types supported.");
                    }
                    docSord.setType(documentType);
                } catch (NumberFormatException nfe) {
                    // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(nfe));
            // -----
                    throw new ImportException("Illegal value for dtype. Must be an integer.");
                }
            }

            ObjKey[] objKeys = docSord.getObjKeys();
            ObjKey[] objKeys2 = new ObjKey[objKeys.length + 1];
            System.arraycopy(objKeys, 0, objKeys2, 0, objKeys.length);
            objKeys = objKeys2;

            ObjKey key;
            String value;
            
            String fieldName;

            //set the index values            
            while (indexList != null) {             
                if(StringUtils.isNumeric(indexList.getValue()))
                {
                    int index = Integer.parseInt(indexList.getValue()) - 1;                    
                    value = indexList.getNext().getValue().trim();
                    key = null;

                    for (int i = 0; i < objKeys.length; i++) {
                        // +++
                        //fieldName = eloClient.getFieldNameByGroupName(docSord.getMaskName(), objKeys[i-1].getName());
                        // +++
                        if (objKeys[i] != null && index == objKeys[i].getId()) {
                            key = objKeys[i];
                            break;
                        }
                    }

                    if (key == null) {
                        logger.debug("\t\t\t\tIllegal index " + index + " for document ");
//                      throw new ImportException("Illegal index for document");
                    } else {
                        if (value.contains("¶") && value.length() > 254) {
                            logger.info("Value contains ¶ setIndexValue function will be called");
                            setIndexValueSpalte(key, String.valueOf(index), value);
                        } else if (value.length() > 254) {
                            logger.info("Value greater than 254 splitting: ");
                            setIndexValueSpalteNoSeparator(key, String.valueOf(index), value);
                        } else {
                            key.setData(new String[]{value});
                        }
                    }
                    indexList = indexList.getSibling();
                }
                
                // --- SL: attributes "name" in indexlist in xmlfile are the values of the groupenames in the mask                                 
                else
                {
                    String indexNameAttr_value = indexList.getValue();
                    value = indexList.getNext().getValue().trim();
                    key = null;
                    for (int i = 0; i < objKeys.length; i++) {                                                                       
                        if (objKeys[i] != null && indexNameAttr_value.substring(0, indexNameAttr_value.length()).equals(objKeys[i].getName())) 
                        {
                            key = objKeys[i];
                            break;
                        }
                    }
                    if (key == null) {
                        logger.debug("\t\t\t\tIllegal index " + indexNameAttr_value + " for document ");
//                      throw new ImportException("Illegal index for document");
                    } else {
                        if (value.contains("¶") && value.length() > 254) {
                            logger.info("Value contains ¶ setIndexValue function will be called");
                            setIndexValueSpalte(key, String.valueOf(indexNameAttr_value), value);
                        } else if (value.length() > 254) {
                            logger.info("Value greater than 254 splitting: ");
                            setIndexValueSpalteNoSeparator(key, String.valueOf(indexNameAttr_value), value);
                        } else {
                            key.setData(new String[]{value});
                        }
                    }
                    indexList = indexList.getSibling();
                }
                // --- End
            }

            ObjKey okeyFName = new ObjKey();
            okeyFName.setId(eloClient.getServicePort().getDOC_MASK_LINE().getID_FILENAME());
            okeyFName.setName(eloClient.getServicePort().getDOC_MASK_LINE().getNAME_FILENAME());
            okeyFName.setData(new String[]{file.getName()});

            objKeys[objKeys.length - 1] = okeyFName;
            docSord.setObjKeys(objKeys);

            //add memo text if available
            if (memo != null) {
                docSord.setDesc(memo);
            }
            int countCurrent = 4;
            Document doc = new Document();
            while (countCurrent != 0) {
                try {
                    countCurrent--;
                    DocVersion version = new DocVersion();
                    version.setExt(IXClient.getFileExt(file.getAbsolutePath()));
                    version.setPathId(docSord.getPath());
                    doc.setDocs(new DocVersion[]{version});
                    doc = eloClient.checkinDocBegin(doc);
                    version = doc.getDocs()[0];
                    version.setUploadResult(eloClient.upload(version.getUrl(), file));
                    if (first) {
                        Info.incCounterOK();
                        first = false;
                    }
                    logger.trace("\t\t\tDocument has been uploaded");
                    doc = eloClient.checkinDocEnd(docSord, SordC.mbAll, doc, LockC.NO);
                    version = doc.getDocs()[0];
                    version.setVersion("1.0");
                    version.setComment("Version 1.0");
                    countCurrent = 0;
                } catch (Exception ex) {
                    logger.error(ex, ex);
                    // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(ex));
            // -----
                }
            }
            if (sord.getLockId() != -1) {
                eloClient.checkinSord(sord, SordC.mbOnlyUnlock, LockC.NO);
            }
            
            while (destList != null) {                
                // ---
                destination = removeWhiteSpace(destList.getValue());
                // ---
                if (destList.getNext() == null) {
                    destinationType = null;
                } else {
                    destinationType = destList.getNext().getValue();
                }
                logger.info("\t\t\tCreating reference at " + destination);
                try {
                    destination = buildPath(destination, indexData.get("indexlist"), idate, xdate, desc, edi);
                } catch (IllegalExpressionException ex) {                   
                    throw new ImportException("Can't import file due to illegal expression in destination path", ex);
                }
                try {
                    info = eloClient.checkoutSord("ARCPATH:" + destination, EditInfoC.mbSord, LockC.NO);
                } catch (RemoteException rex) {
                    //path invalid
                    if (!rex.getMessage().contains("[ELOIX:5023]")) {
                        this.logger.error("Exception while checking out destination.", rex);
                        // -----                        
                        //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(rex));
                        // -----
                        throw rex;
                    }

                    this.logger.info("\t\t\tNon existing archive path: '" + destination + "'");
                }

                // info ist schon gesetzt und ist != null
                if (info == null) {
                    ELOStructureBuilder builder = ELOStructureBuilder.getInstance();
                    sord = builder.createPath(destination, destinationType, eloClient, getUserName());
                } else {
                    ELOStructureBuilder builder = ELOStructureBuilder.getInstance();
                    sord = builder.createPath(destination, destinationType, eloClient, getUserName());
                    //sord = info.getSord();
                }

                eloClient.refSord(null, sord.getGuid(), docSord.getGuid(), eloClient.getServicePort().getSORT_ORDER().getDEFAULT());

                if (sord.getLockId() != -1) {
                    eloClient.checkinSord(sord, SordC.mbOnlyUnlock, LockC.NO);
                }
                destList = destList.getSibling();
            }

            workflowList = indexData.get("workflowlist");

            java.util.ArrayList<Integer> workflowIds = new java.util.ArrayList<Integer>();
            java.util.ArrayList<String> workflows = new java.util.ArrayList<String>();
            try {
                //loop through the workflow list
                while (workflowList != null) {
                    v = workflowList.getNext();

                    String workflowName = workflowList.getValue();
                    String workflowTemplate = v.getValue();

                    int id = eloClient.startWorkFlow(workflowTemplate, workflowName, doc.getObjId());
                    workflowIds.add(id);
                    workflows.add(workflowName);
                    workflowList = workflowList.getSibling();
                }

            } catch (RemoteException rex) {
                //try to delete all workflows which have been started                
                for (Integer id : workflowIds) {
                    try {
                        eloClient.deleteWorkFlow(id, WFTypeC.ACTIVE, LockC.NO);
                    } catch (Exception ex) {
                    }
                }
                throw rex;
            }

            for (String workflowName : workflows) {
                logger.info("\t\t\t Workflow " + workflowName + " has been started");
            }
            if (first) {
                Info.incCounterOK();
                first = false;
            }
            logger.info("\t\t\tDocument import complete");
        } catch (RemoteException rex) {            
            if (rex.getMessage().contains("[ELOIX:5023]")) {
                logger.error("Import failed due to some missing element!", rex);                
                Info.incCounterError();
                throw new ImportException("Import failed!", rex);
            }

            if (rex.getMessage().contains("[ELOIX:2002]")) {
                logger.warn("Import failed because the server is busy.", rex);
                Info.incCounterError();
                throw new ImportException("Import failed!", rex);
            }

            logger.error("Import failed! Logging off.", rex);
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(rex));
            // -----
//            connected = false;
//            logoff();
            Info.incCounterError();
            throw new ImportException("Import failed!", rex);
        } catch (ImportException iex) {
//            logoff();            
            throw iex;
        } catch (Exception e) {
//            e.printStackTrace();
            logger.error("Import failed! Logging off.", e);
            // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(e));
            // -----
//            connected = false;
//            logoff();
            Info.incCounterError();
            throw new ImportException("Import failed!", e);
        } finally {
            logoff();
        }
    }

    private void setIndexValueSpalte(ObjKey key, String index, String value) {
        //Die Pilcrow am Anfang und Ende beseitigen
        if ((value != null) && (value.length() > 0)) {
            if (value.startsWith("¶")) {
                value = value.substring(1);
            }
            if (value.substring(value.length() - 1, value.length()).equals("¶")) {
                value = value.substring(0, value.length() - 1);
            }
        }

        //werte speichern                
        String[] data = value.split("¶");
        key.setData(data);
    }

    private void setIndexValueSpalteNoSeparator(ObjKey key, String index, String value) {

        key.setData(value.split("(?<=\\G.{254}"));
    }

    private void calculateBestBefore() {
        long us = eloClient.getTicketLifeTime() * 1000 * 1000;
        long ns100 = us * 10;

        bestBefore = System.nanoTime() * (ns100 * 9) / 10;
    }

    private String createIsoDate(Date d) {
        return isoFormatter.format(d);
    }

    private String createIsoDate(String value) {
        String ret = createIsoDate(new Date());
        if (value != null && value.length() > 15) {
            String subValue = value.substring(0, 16);
            if (subValue.matches("[0-9]")) {
                ret = subValue;
            }
        }
        return ret;
    }

    private String createShortIsoDate(Date d) {
        return isoFormatter.format(d);
    }

    private String createShortIsoDate(String value) {
        String ret = createShortIsoDate(new Date());
        if (value != null && value.length() > 9) {
            String subValue = value.substring(0, 10);
            if (subValue.matches("[0-9]")) {
                ret = subValue;
            }
        }
        return ret;
    }

    private boolean isValidIsoDate(String value) {
        return value != null && value.matches("^[0-9]{8}([0-9]{1,6})?$");
    }

    private String getSystemTimeZone() {
        return java.util.TimeZone.getDefault().getID();
    }

    @Override
    public void logoff() {

        if (eloClient != null) {
            eloClient.close();
            eloClient = null;
        }
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public void setConnectionUrl(String Url) {
        connectionUrl = Url;
    }

    private String getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public void setELOClient(ELOClient client) {
        this.eloClient = client;
    }

    public ELOClient getELOClient() {
        return this.eloClient;
    }

    @Override
    public void ping() {
        if (eloClient == null || !eloClient.isConnected()) {
            return;
        }
        //ticket about to expire
        if (bestBefore < System.nanoTime()) {
            try {
                eloClient.alive();
//                indexClient.ix.alive(clientInfo);
                calculateBestBefore();
            } catch (RemoteException ex) {
                logger.error("Unable to ping ELO server!", ex);
                // -----                        
            //de.di.xml.Importer.reportException(de.di.xml.Importer.getMailContent(ex));
            // -----
//                logoff();
                eloClient.close();
                eloClient = null;
            }
        }
    }

    /**
     * Replace any sequence of 2 or more space characters with one space and
     * replace any space character followed by the path seperator with the path
     * seperator.
     *
     * @param value the path to a structure element
     * @return the path with whitespace removed accordingly
     */
    private String removeWhiteSpace(String value) {
        String pattern1 = "[ ]{2,}";
        String pattern2 = "[ ]{1,}" + ELOStructureBuilder.PATH_SEP;

        value = value.replaceAll(pattern1, " ");
        value = value.replaceAll(pattern2, ELOStructureBuilder.PATH_SEP);
        return value;
    }

    @Override
    public boolean isCheckDuplicates() {
        return checkDuplicates;
    }

    @Override
    public void setCheckDuplicates(boolean checkDuplicates) {
        this.checkDuplicates = checkDuplicates;
    }

    protected String buildPath(String destination,
            IndexValue indexList,
            String aDate,
            String dDate,
            String desc,
            EditInfo edi) throws IllegalExpressionException {
        /*Modified due to bug 17 on ver*/
        String pattern = "(L(A|D|K|[0-9]{1,2})|L(A|D|K|(\\d{1,2}))\\((\\d{1,})\\,(\\d{1,})\\))";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        if (destination.contains("Â")) {
            destination = destination.replaceAll("Â", "");
        }
        String[] elements = destination.split(ELOStructureBuilder.PATH_SEP);

        StringBuffer buffer = new StringBuffer();

        for (String element : elements) {
            if (element.length() == 0) { //ignore empty text at the beginning of the destination path
                continue;
            }
            buffer.append(ELOStructureBuilder.PATH_SEP);

            java.util.List<String> parts = getParts(element);
            for (String part : parts) {

                if (part.startsWith("[") && part.endsWith("]")) {
                    buffer.append(part.substring(1, part.length() - 1));
                } else {
                    java.util.regex.Matcher matcher = p.matcher(part);

                    if (matcher.matches()) {

                        if (matcher.group(2) != null && matcher.group(2).equals("D")
                                || matcher.group(3) != null && matcher.group(3).equals("D")) {
                            String value = dDate;

                            if (matcher.group(5) != null && matcher.group(6) != null) {
                                try {
                                    value = getSubString(value, matcher.group(5), matcher.group(6));
                                } catch (Exception ex) {                                   
                                    throw new IllegalExpressionException("'" + part + "' is an illegal expression");
                                }
                            }

                            buffer.append(value);
                        } else if (matcher.group(2) != null && matcher.group(2).equals("A")
                                || matcher.group(3) != null && matcher.group(3).equals("A")) {
                            String value = aDate;

                            if (matcher.group(5) != null && matcher.group(6) != null) {
                                try {
                                    value = getSubString(value, matcher.group(5), matcher.group(6));
                                } catch (Exception ex) {                                   
                                    throw new IllegalExpressionException("'" + part + "' is an illegal expression");
                                }
                            }

                            buffer.append(value);
                        } else if (matcher.group(2) != null && matcher.group(2).equals("K")
                                || matcher.group(3) != null && matcher.group(3).equals("K")) {
                            String value = desc;

                            if (matcher.group(5) != null && matcher.group(6) != null) {
                                try {
                                    value = getSubString(value, matcher.group(5), matcher.group(6));
                                } catch (Exception ex) {                                    
                                    throw new IllegalExpressionException("'" + part + "' is an illegal expression");
                                }
                            }

                            buffer.append(value);
                        } else if (matcher.group(3) != null) {
                            // --- SL: zusätzlicher Parameter für den Zugriff auf die Gruppennamen/ObjKeys
                            String value = "";
                            if(StringUtils.isNumeric(indexList.getValue()))
                            {
                                value = getIndexValue(indexList, matcher.group(3));                                
                            }
                            else
                            {
                                value = getIndexValue(indexList, matcher.group(3), edi);                                
                            }
                            // --- END 
                            int startIndex = 0;
                            int endIndex = value.length();

                            try {
                                startIndex = Integer.parseInt(matcher.group(5)) - 1;
                                endIndex = startIndex + Integer.parseInt(matcher.group(6));

                                value = value.substring(startIndex, endIndex);
                            } catch (Exception ex) {                                
                                throw new IllegalExpressionException("'" + part + "' is an illegal expression");
                            }

                            buffer.append(value);
                        } else {
                            String value = "";
                            if(StringUtils.isNumeric(indexList.getValue()))
                            {
                                value = getIndexValue(indexList, matcher.group(2));
                                buffer.append(value);
                            }
                            else
                            {
                                value = getIndexValue(indexList, matcher.group(2), edi);
                                buffer.append(value);
                            }
//                            String value = getIndexValue(indexList, matcher.group(2), edi);
//                            buffer.append(value);
                        }
                    } else {
                        throw new IllegalExpressionException("'" + part + "' is an illegal expression");
                    }
                }
            }
        }

        return buffer.toString();
    }

    private String getSubString(String value, String startIndex, String length) throws NumberFormatException {
        int sIndex = 0;
        int eIndex = value.length();

        String substring = value;

        sIndex = Integer.parseInt(startIndex) - 1;
        eIndex = sIndex + Integer.parseInt(length);

        substring = value.substring(sIndex, eIndex);

        return substring;
    }

//    Version 1 of the function
    private String getIndexValue(IndexValue indexList, String index) {
        IndexValue current = indexList;        
        while (current != null) {                       
            if (current.getValue().equals(index)) {
                return current.getNext().getValue().trim();
            }     
            current = current.getSibling();
        }
        return null;
    }
    
//  Version 2 of the function
    // --- if the index tags in the xml file has entries with numeric characters at the begin of the string
    // Wird nicht benutzt - Im Fall, dass sie gebraucht wird
//    private String getIndexValue(IndexValue indexList, String index) {                  
//        while (indexList != null) {   
//            String index_field_nr = "";
//            int index_field_name_size = indexList.getValue().length();
//            if(index_field_name_size > 0 && index_field_name_size <= 2) // digit
//            {
//                if (indexList.getValue().equals(index)) 
//                {
//                    return indexList.getNext().getValue().trim();
//                }     
//                indexList = indexList.getSibling();
//            }
//            else
//            {
//                if(indexList.getValue().substring(0, 2).matches("\\d"))
//                {
//                    if (indexList.getValue().substring(0, 2).equals(index)) 
//                    {
//                        return indexList.getNext().getValue().substring(0, 2).trim();
//                    }     
//                    indexList = indexList.getSibling();
//                }
//                else if(indexList.getValue().substring(0, 1).matches("\\d") && !indexList.getValue().substring(1, 2).matches("\\d"))
//                {
//                    if (indexList.getValue().substring(0, 1).equals(index)) 
//                    {
//                        return indexList.getNext().getValue().trim();
//                    }     
//                    indexList = indexList.getSibling();
//                }
//                else 
//                {
//                    logger.info("No appropriate name attribute");
//                }
//            }         
//        }
//        return null;
//    }
    
    // --- SL:  Version 3 of the function for the Objectkeys
    private String getIndexValue(IndexValue indexList, String index, EditInfo edi) {
        IndexValue current = indexList;        
        ObjKey[] objkeys = edi.getSord().getObjKeys();        
        String groupName = objkeys[Integer.parseInt(index)-1].getName();
        while (current != null) {                       
            if (current.getValue().equals(groupName)) {
                return current.getNext().getValue().trim();
            }     
            current = current.getSibling();
        }
        return null;
    }
    // --- END of function
    
    private List<String> getParts(String element) {
        int pos = 0;
        java.util.LinkedList<String> parts = new java.util.LinkedList<String>();
        int brackets = 0;

        for (int i = 0; i < element.length(); i++) {
            if (element.charAt(i) == '[') {
                brackets++;
            } else if (element.charAt(i) == ']') {
                brackets--;
            } else if (element.charAt(i) == '+' && brackets == 0) {
                parts.add(element.substring(pos, i));
                pos = i + 1;
            }
        }

        if (pos < element.length()) {
            parts.add(element.substring(pos));
        }

        return parts;
    }

    public void importVersionFinel(Map<String, IndexValue> indexData, File docFile, Properties settings, Logger logger) throws ImportException, IOException {
        logger.info("\t\t\tArchiving as new Version");
        String destination;
        String type;
        String dtype = null;
        String desc;
        String path;
        String memo = null;
        String idate;
        String xdate;
        String destinationType;
        IndexValue indexList;
        IndexValue destList;

        //get basic information to import document
        destList = indexData.get("destlist");

        if (destList == null) {
            logger.error("Missing destination for document!");
            throw new ImportException("Unknown destination for document");
        }

        //path, id or guid of the parent element for the document
        //CHECK
        destination = removeWhiteSpace(destList.getValue());

        if (destList.getNext() == null) {
            destinationType = null;
        } else {
            destinationType = destList.getNext().getValue();
        }
        destList = destList.getSibling();

        IndexValue v = indexData.get("type");

        if (v == null) {
            logger.error("Missing type of the document!");
            throw new ImportException("Unknown type for document");
        }

        //mask or id of the mask for the Sord of the document
        type = v.getValue();

        v = indexData.get("desc");

        if (v == null) {
            logger.error("Missing description of the document!");
            throw new ImportException("Unknown description for document");
        }

        //short description of the element
        desc = v.getValue();

        java.util.Date d = new java.util.Date();
        v = indexData.get("idate");

        //internal document date (time of import)
        if (v == null) {
            idate = createShortIsoDate(d);
        } else if (isValidIsoDate(v.getValue())) {
            idate = v.getValue();
        } else {
            idate = createShortIsoDate(d);
        }

        v = indexData.get("xdate");
        //external document date (time of document creation)
        if (v == null) {
            xdate = createShortIsoDate(d);
        } else if (isValidIsoDate(v.getValue())) {
            xdate = v.getValue();
        } else {
            xdate = createShortIsoDate(d);
        }
        v = indexData.get("docfile");

        if (v == null) {
            logger.error("Missing document file path!");
            throw new ImportException("No document file path specified!");
        }

        //path for the document file
        path = v.getValue();

        v = indexData.get("dtype");

        //dtype is set
        if (v != null) {
            dtype = v.getValue();
        }

        indexList = indexData.get("indexlist");

        if (indexList == null) {
            logger.error("Missing index data for the document!");
            throw new ImportException("Unknown index data for document");
        }
        File file = new File(path);

        if (!file.exists()) {
            throw new ImportException("Can't import non exisiting file!");
        }

        if (!file.canRead()) {
            throw new ImportException("Can't import a file that is not readable!");
        }

        if (!file.canWrite()) {
            throw new ImportException("Can't import a file that is not writable!");
        }

        v = indexData.get("memo");

        //add memo data to document
        if (v != null) {
            memo = v.getValue().replace("\\n", "\n");
        }

        EditInfo ed = eloClient.checkoutDoc();

        ed.getSord().setName(desc);
        ed.getSord().setPath(objPath);
        ed.getSord().setXDateIso(xdate);
        ed.getSord().setIDateIso(idate);
        if (dtype != null) {
            try {
                int documentType = Integer.parseInt(dtype);

                if (documentType < 254) {
                    throw new ImportException("Illegal dtype value '" + documentType
                            + "' for document.");
                } else if (documentType > 283) {
                    throw new ImportException("Illegal dtype value '" + documentType + "' for document. "
                            + "Only build in document types supported.");
                }
                ed.getSord().setType(documentType);
            } catch (NumberFormatException nfe) {                
                throw new ImportException("Illegal value for dtype. Must be an integer.");
            }
        }

        ObjKey[] objKeys = ed.getSord().getObjKeys();
        ObjKey[] objKeys2 = new ObjKey[objKeys.length + 1];
        System.arraycopy(objKeys, 0, objKeys2, 0, objKeys.length);
        objKeys = objKeys2;

        ObjKey key;
        String value;

        //set the index values
        while (indexList != null) {
            // --- SL: Indexfield in XML file can ibe the groupname in the mask
            if(StringUtils.isNumeric(indexList.getValue()))
            {
                int index = Integer.parseInt(indexList.getValue()) - 1;
                value = indexList.getNext().getValue().trim();
                key = null;

                for (int i = 0; i < objKeys.length; i++) {

                    if (objKeys[i] != null && index == objKeys[i].getId()) {
                        key = objKeys[i];
                        break;
                    }
                }

                if (key == null) {
                    logger.error("Illegal index " + index + " for document ");
                    throw new ImportException("Illegal index for document");
                }
                if (value.contains("¶") && value.length() > 254) {
                    logger.info("Value contains ¶ setIndexValue function will be called");
                    eloClient.setIndexValueSpalte(key, value);
                } else {
                    key.setData(new String[]{value});
                }
                indexList = indexList.getSibling();
            }
            else
            {
                String indexNameAttr_value = indexList.getValue();
                value = indexList.getNext().getValue().trim();
                key = null;

                for (int i = 0; i < objKeys.length; i++) {

                    if ( objKeys[i] != null && indexNameAttr_value.equals(objKeys[i].getName()) ) {
                        key = objKeys[i];
                        break;
                    }
                }

                if (key == null) {
                    logger.error("Illegal index " + indexNameAttr_value + " for document ");
                    throw new ImportException("Illegal index for document");
                }
                if (value.contains("¶") && value.length() > 254) {
                    logger.info("Value contains ¶ setIndexValue function will be called");
                    eloClient.setIndexValueSpalte(key, value);
                } else {
                    key.setData(new String[]{value});
                }
                indexList = indexList.getSibling();
            }
        }

        ObjKey okeyFName = new ObjKey();
        okeyFName.setId(eloClient.getServicePort().getDOC_MASK_LINE().getID_FILENAME());
        okeyFName.setName(eloClient.getServicePort().getDOC_MASK_LINE().getNAME_FILENAME());
        okeyFName.setData(new String[]{file.getName()});

        objKeys[objKeys.length - 1] = okeyFName;
        ed.getSord().setObjKeys(objKeys);

        //add memo text if available
        if (memo != null) {
            ed.getSord().setDesc(memo);
        }

        Document doc = ed.getDocument();
        doc.getDocs()[0].setExt(docFile.getName().substring(docFile.getName().indexOf(".") + 1));
        doc.getDocs()[0].setPathId(ed.getSord().getPath());
        doc.getDocs()[0].setEncryptionSet(ed.getSord().getDetails().getEncryptionSet());
        doc = eloClient.checkinDocBegin(doc);

        doc.getDocs()[0].setUploadResult(eloClient.upload(doc.getDocs()[0].getUrl(), docFile));

        doc = eloClient.checkinDocEnd(ed.getSord(), SordC.mbAll, doc, LockC.NO);
        Sord sorda = eloClient.checkoutSord(ed.getSord().getId() + "", EditInfoC.mbAll, LockC.NO).getSord();
        sorda.setXDateIso(xdate);
        eloClient.checkinSord(sorda, SordC.mbAll, LockC.NO);
        logger.debug("\t\t\tObjct-ID = " + doc.getObjId() + ", Document-ID = " + doc.getDocs()[0].getId());

        //Referenzen von Archiv-Dokument ermitteln
        LinkedList<Integer> parentSords = new LinkedList<Integer>();
        parentSords = eloClient.getParentSords(ed.getSord().getId(), parentSords);

        String[] refPar = new String[10];
        ArcPath[] refPaths = ed.getSord().getRefPaths();
        for (int i = 0; i < refPaths.length; i++) {
            refPar[i] = String.valueOf(refPaths[i].getPath()[refPaths[i].getPath().length - 1].getId());
            if (i != 0) {
                Sord sord = eloClient.getSord(refPaths[i].getPath()[refPaths[i].getPath().length - 1].getId());
                parentSords.add(sord.getId());
                parentSords = eloClient.getParentSords(sord.getId(), parentSords);
            }
        }

        // --- SL         
        EditInfo edi = eloClient.createSord(null, type, EditInfoC.mbSord);
        // --- END       
        
        //Dokument in Path aus destList/destination verschieben
        shiftToNewDestination(destination, indexData, idate, xdate, desc, ed, destinationType, settings, edi);

        //Referenzerstellen
        createReferences(destList, destination, indexData, idate, xdate, desc, destinationType, settings, ed, edi);

        // Löschen von Referenzen
        deleteReferenz(refPar, ed.getSord());

        //Löschen von leeren Ordnern
        deleteEmptyFolder(parentSords);

        // Workfows starten
        startWorflows(indexData, doc.getObjId());

    }

    private void shiftToNewDestination(String destination, Map<String, IndexValue> indexData, String idate, String xdate, String desc, EditInfo ed, String type, Properties settings, EditInfo edi) throws ImportException, RemoteException {
        try {
            destination = buildPath(destination, indexData.get("indexlist"), idate, xdate, desc, edi);
            logger.info("\t\t\tMoving to new destination: " + destination);
        } catch (IllegalExpressionException ex) {            
            throw new ImportException("Can't import file due to illegal expression in destination path", ex);
        }
        ELOStructureBuilder builder = ELOStructureBuilder.getInstance();
        Sord sord = builder.createPath(destination, type, eloClient, settings.getProperty("IndexServer.User"));
        String newParentId = String.valueOf(sord.getId());
        String objId = String.valueOf(ed.getSord().getId());
        eloClient.copySord(newParentId, objId);
    }

    private void createReferences(IndexValue destList, String destination, Map<String, IndexValue> indexData, String idate, String xdate, String desc, String type, Properties settings, EditInfo ed, EditInfo edi) throws ImportException, RemoteException {
        while (destList != null) {
            destination = removeWhiteSpace(destList.getValue());
            try {
                destination = buildPath(destination, indexData.get("indexlist"), idate, xdate, desc, edi);
                logger.info("\t\t\tCreating reference to destination: " + destination);
            } catch (IllegalExpressionException ex) {                
                throw new ImportException("Can't import file due to illegal expression in destination path", ex);
            }
            ELOStructureBuilder builder2 = ELOStructureBuilder.getInstance();
            Sord sord2 = builder2.createPath(destination, type, eloClient, settings.getProperty("IndexServer.User"));
            String newParentId2 = String.valueOf(sord2.getId());
            String objId2 = String.valueOf(ed.getSord().getId());
            eloClient.createReferenz(newParentId2, objId2);
            destList = destList.getSibling();
        }
    }

    private void deleteEmptyFolder(LinkedList<Integer> parentSords) throws RemoteException {
        boolean log = false;
        for (int y = 0; y < parentSords.size(); y++) {
            Sord srd = eloClient.getSord(parentSords.get(y));
            if (srd.getChildCount() == 0) {
                String parentId = String.valueOf(srd.getParentId());
                String srdId = String.valueOf(srd.getId());
                logger.info("\t\t\tDelete old parent: " + srd.getName());
                eloClient.deleteSord(parentId, srdId);
                log = true;
            }
        }
        if (log) {
            logger.info("\t\t\tDelete done");
        }
    }

    private void deleteReferenz(String[] refPar, Sord sord) throws RemoteException {
        boolean log = false;
        for (int x = 1; x < refPar.length; x++) {
            if (refPar[x] != null) {
                logger.info("\t\t\tDelete old refernce in: " + sord.getRefPaths()[x].getPathAsString());
                eloClient.deleteSord(refPar[x], String.valueOf(sord.getId()));
                log = true;
            }
        }
        if (log) {
            logger.info("\t\t\tDelete done");
        }
    }

    private void startWorflows(Map<String, IndexValue> indexData, String objId) throws RemoteException {
        IndexValue workflowList = indexData.get("workflowlist");

        java.util.ArrayList<Integer> workflowIds = new java.util.ArrayList<Integer>();
        java.util.ArrayList<String> workflows = new java.util.ArrayList<String>();
        try {

            //loop through the workflow list
            while (workflowList != null) {
                IndexValue v = workflowList.getNext();

                String workflowName = workflowList.getValue();
                String workflowTemplate = v.getValue();

                int id = eloClient.startWorkFlow(workflowTemplate, workflowName, objId);
                workflowIds.add(id);
                workflows.add(workflowName);
                workflowList = workflowList.getSibling();
            }

        } catch (RemoteException rex) {
            //try to delete all workflows which have been started
            for (Integer id : workflowIds) {
                try {
                    eloClient.deleteWorkFlow(id, WFTypeC.ACTIVE, LockC.NO);
                } catch (Exception ex) {
                }
            }           
            throw rex;
        }

        for (String workflowName : workflows) {
            logger.info("\t\t\t Workflow " + workflowName + " has been started");
        }
    }        

}
