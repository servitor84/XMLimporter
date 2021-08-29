package de.di.elo.client;

//import de.di.dokinform.util.PWDHandler;
import de.di.xml.Archiving;
import de.di.xml.Importer;
import de.di.xml.IndexValue;
import de.elo.ix.client.ClientInfo;
import de.elo.ix.client.CopySordC;
import de.elo.ix.client.DocMask;
import de.elo.ix.client.DocMaskC;
import de.elo.ix.client.DocMaskLine;
import de.elo.ix.client.DocMaskZ;
import de.elo.ix.client.Document;
import de.elo.ix.client.EditInfo;
import de.elo.ix.client.EditInfoC;
import de.elo.ix.client.EditInfoZ;
import de.elo.ix.client.FindByIndex;
import de.elo.ix.client.FindByVersion;
import de.elo.ix.client.FindInfo;
import de.elo.ix.client.FindResult;
import de.elo.ix.client.IXClient;
import de.elo.ix.client.IXConnFactory;
import de.elo.ix.client.IXConnection;
import de.elo.ix.client.IXServicePortC;
import de.elo.ix.client.LockC;
import de.elo.ix.client.LockZ;
import de.elo.ix.client.LoginResult;
import de.elo.ix.client.ObjKey;
import de.elo.ix.client.ServerInfo;
import de.elo.ix.client.Sord;
import de.elo.ix.client.SordC;
import de.elo.ix.client.SordZ;
import de.elo.ix.client.WFTypeZ;
import de.elo.utils.net.RemoteException;
import de.elo.utils.sec.DesEncryption;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

//import de.di.dokinform.util.
//import de.arivato.dokinform.elo.ELOClientNG;
/**
 *
 * @author A. Sopicki
 */
public class ELOClient /*extends de.di.dokinform.elo.ELOClientNG*/ {
    public IXConnFactory indexClient = null;
    public IXConnection _connection = null;
    
    //private IXClient indexClient;
    private int objId;
    private int objPath;
    private static java.util.Properties settings;
    private static Logger logger;
    //    
    private LoginResult loginResult;
    private ClientInfo clientInfo;
    private IXServicePortC ixConstants;
    private String connectionUrl;
    private String userName;
    private String password;
    private boolean connected = false;
    private static int i = 1;
    // --- Encryption-Decryption module
    //PWDHandler decryptor;
    // ---    

    public ELOClient() {
        this(settings);
    }
    
    public ELOClient(Logger log, java.util.Properties set) {
        logger = log;
        settings = set;
    }

    public ELOClient(java.util.Properties set) {
        //super(set);
        settings = set;
        init();
    }

    public ELOClient(java.util.Properties set, Logger log) {        
        //super(set);        
        logger = log;
        settings = set;
        init();
    }
//
    private void init() throws IllegalStateException {
        while (true) {
            try {
                if (settings == null) {
                    settings = Importer.getSettings();
                }
                connectionUrl = settings.getProperty("IndexServer.URL");
                userName = settings.getProperty("IndexServer.User");
                password = settings.getProperty("IndexServer.Password");
                // PWD decription +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                String pattern = "^((\\d){1,})([-]{1}(\\d){1,}){1,}";
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher matcher = p.matcher(password);
                if(matcher.matches()) {
                    de.elo.utils.sec.DesEncryption des = new de.elo.utils.sec.DesEncryption();
                    password = des.decrypt(password);
                }
                // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

                if (clientInfo == null) {
                    clientInfo = new ClientInfo();
                }

                clientInfo.setTimeZone(getSystemTimeZone());

                if (indexClient == null) {                    
                    indexClient = new IXConnFactory(getConnectionUrl(), "", "");
                }
                break;
            } catch (IllegalStateException eex) {
                logger.error("Cannot connect to elo indexserver at the following addresses \'" + settings.getProperty("IndexServer.URL") + "\' ");
                logger.error("Please secure the elo indexserver is runing");
                logger.info("If the url, user or password is wrong, please correct it in config.properties file and restart XMLimporter");
                logger.info("Try to reconnect in " + Integer.valueOf(settings.getProperty("Importer.PollTime")) / 1000 + " seconds");
                // -----                        
                //Importer.reportException(Importer.getMailContent(eex));
                // -----
                try {
                    Thread.sleep(Integer.valueOf(settings.getProperty("Importer.PollTime")));
                } catch (InterruptedException iex) {
                    // -----                        
                    //Importer.reportException(Importer.getMailContent(iex));
                    // -----
                    iex.getStackTrace();
                }
            } catch (Exception ex) {
                logger.error("Decryption of the password failed: " + ex);
                // -----                        
                //Importer.reportException(Importer.getMailContent(ex));
                // -----
            }
        }
    }
    
    public String getConnectionUrl() {
      return connectionUrl;
    }
    public String getPassword() {
      return password;
    }
    public void logoff() {}
//
    public void alive() throws RemoteException {
        if (!connected) {
            login();
            return;
        }        
        _connection.ix().alive();
    }

    public ServerInfo getServerInfo() throws RemoteException {
        if (!connected) {
            login();
        }                        
        return _connection.ix().getServerInfo();
    } //
//
    public Document checkinDocBegin(Document doc) throws RemoteException {
        if (!connected) {
            login();
        }        
        return _connection.ix().checkinDocBegin(doc);
    }
//
    public Document checkinDocEnd(Sord docSord, SordZ sordInfo, Document doc, LockZ lockInfo) throws RemoteException {
        if (!connected) {
            login();
        }

        return _connection.ix().checkinDocEnd(docSord, sordInfo, doc, lockInfo);
    }
//
    public int checkinSord(Sord parent, SordZ sordInfo, LockZ lockInfo) throws RemoteException{
        if (!connected) {
            login();
        }

        return _connection.ix().checkinSord(parent, sordInfo, lockInfo);
    }
//
    public EditInfo checkoutSord(String path, EditInfoZ editInfo, LockZ lockInfo) throws RemoteException {
        if (!connected) {
            login();
        }

        return _connection.ix().checkoutSord(path, editInfo, lockInfo);
    }
//
    public EditInfo createDoc(String parent, String mask, String template, EditInfoZ editInfo) throws RemoteException {
        if (!connected) {
            login();
        }

        return _connection.ix().createSord(parent, mask, editInfo);
    }
//
    public EditInfo createSord(String parent, String mask, EditInfoZ editInfo) throws RemoteException {
        if (!connected) {
            login();
        }
        return _connection.ix().createSord(parent, mask, editInfo);
    }
//
    public Sord findDoc(String md5) throws RemoteException{ // parameter "name" deleted
        if (/*!connected*/!isConnected()) {
            login();
        }
        FindInfo info = new FindInfo();
        FindByVersion versionInfo = new FindByVersion();
        //FindByIndex indexInfo = new FindByIndex();//
        versionInfo.setVersionMD5(md5);        
        //indexInfo.setName(name);//
        info.setFindByVersion(versionInfo);
        //info.setFindByIndex(indexInfo);//

        //FindResult result = indexClient.ix.findFirstSords(clientInfo, info, 1, SordC.mbMinDocVersion);
        FindResult result = _connection.ix().findFirstSords(info, 1, SordC.mbMinDocVersion);
        Sord[] sords = result.getSords();

        if (sords != null && sords.length > 0) {
            return sords[0];
        }

        return null;
    }

    public void close() {
        if (!connected) {
            logoff();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public IXServicePortC getServicePort() {
        return ixConstants;
    }
//
    public int getTicketLifeTime() {
        if (!connected) {
            return 0;
        }

        return loginResult.getTicketLifetime();
    }
////
    public void refSord(String oldParentId, String newParentId, String objId, int manSortIdx) throws RemoteException {
        if (!connected) {
            login();
        }
        _connection.ix().refSord(oldParentId, newParentId, objId, manSortIdx);
    }
//
    public String upload(String url, java.io.File file) throws RemoteException {
        if (!connected) {
            login();
        }        
        return _connection.upload(url, file);
    }
//
    public int startWorkFlow(String template, String workflow, String objId) throws RemoteException {
        if (!connected) {
            login();
        }
        return _connection.ix().startWorkFlow(template, workflow, objId);
    }
//
    public void deleteWorkFlow(Integer workflowId, WFTypeZ type, LockZ lock) throws RemoteException {
        if (!connected) {
            login();
        }
        _connection.ix().deleteWorkFlow(workflowId.toString(), type, lock);
    }
////
    /*private String getPassword() {
        return password;
    }*/
    public void setPassword(String password)
    {
        this.password = password;
    }

    /*private String getConnectionUrl() {
        return connectionUrl;
    }*/
    public void setConnectionUrl(String connectionUrl)
    {
        this.connectionUrl = connectionUrl;
    }
//
    private String getSystemTimeZone() {
        return java.util.TimeZone.getDefault().getID();
    }

    private String getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

    public void login() throws RemoteException {
        if(indexClient == null) {
            init();
        }
        try {                                           
            //loginResult = indexClient.login(clientInfo, getUserName(), getPassword(), getLocalHost(), null);
            //ixConstants = indexClient.getCONST(clientInfo);            
            _connection = indexClient.create(getUserName(), getPassword(), getLocalHost(), null);            
            ixConstants = _connection.getCONST();
            loginResult = _connection.getLoginResult(); 
            clientInfo = this.loginResult.getClientInfo();
            //logger.debug("\t\t\t\t\tTicket number: " + clientInfo.getTicket());
        } catch (UnknownHostException uhex) {            
            throw new RemoteException("Unable to login due to a network problem", uhex);            
        } catch (Exception ex) {            
            throw new RemoteException("Unable to login due to a network problem", ex);
        }
        connected = true;
    }

    /*private void logoff() {
        try {
            indexClient.logout(clientInfo);
        } catch (RemoteException ex) {
        } finally {
            connected = false;
        }
    }*/

    @Override
    public void finalize() {
        if (connected) {
            try {
                //indexClient.logout(clientInfo);
                _connection.logout();
            } catch (Exception ex) {
            }
        }        
    }
////
    public java.util.Map<String, Integer> getMaskNamesAndMaxNumberOfIndexField() throws RemoteException{
       
        if (!connected) {
            login();
        }
        EditInfoZ editZ = new EditInfoZ(EditInfoC.mbMaskNames, new SordZ());
        EditInfo ed = _connection.ix().createSord(null, null, editZ);
        java.util.Map<String, Integer> map = new java.util.HashMap<String, Integer>();
        for (int i = 0; i < ed.getMaskNames().length; i++) {
//            if (ed.getMaskNames()[i].isDocumentMask()) {
            String maskName = ed.getMaskNames()[i].getName();
            DocMask ed2 = _connection.ix().checkoutDocMask(maskName, new DocMaskZ(), LockC.NO);
            int numberOfIndexFields = ed2.getLines().length;
            map.put(maskName, numberOfIndexFields);
//            }
        }
        return map;
    }
//
    public java.util.Map<String, Map<Integer, String>> getMasksLinesIdToName() throws RemoteException {
        if (!connected) {
            login();
        }
        DocMaskZ dmz = new DocMaskZ();
        dmz.add(DocMaskC.mbName);
        dmz.add(DocMaskC.mbLines);
        EditInfoZ editZ = new EditInfoZ(EditInfoC.mbMaskNames, new SordZ());
        EditInfo ed = _connection.ix().createSord(null, null, editZ);
        java.util.Map<String, Map<Integer, String>> map = new java.util.HashMap<String, Map<Integer, String>>();
        for (int i = 0; i < ed.getMaskNames().length; i++) {
            Map<Integer, String> lineIdToName = new java.util.HashMap<Integer, String>();
            String maskName = ed.getMaskNames()[i].getName();
            DocMask ed2 = _connection.ix().checkoutDocMask(maskName, new DocMaskZ(), LockC.NO);
            for (DocMaskLine dml : ed2.getLines()) {
                lineIdToName.put(dml.getId(), dml.getKey());
            }
            logger.debug("Adding : " + maskName + " with lines (id to name) : " + lineIdToName);
            map.put(maskName, lineIdToName);
        }
        return map;
    }
//
    public boolean findByIndex(Map<String, IndexValue> map, Logger logger) throws RemoteException {
        if (!connected) {
            login();
        }
        SordZ sordZ = new SordZ();
        sordZ.add(SordC.mbId);
        sordZ.add(SordC.mbPath);
        sordZ.add(SordC.mbName);
        sordZ.add(SordC.mbIDateIso);
        sordZ.add(SordC.mbMask);
        FindInfo findInfo = new FindInfo();
        findInfo.setFindByIndex(new FindByIndex());
        String[] arr = new String[52];
        IndexValue indexVersioning = map.get("versioning");
        IndexValue indexList = map.get("indexlist");
        String fields = "";
        Map<Integer, String> objKeysToSearch = new HashMap<Integer, String>();
        // --- SL: Versionierung mit Gruppennamen der Maske
        Map<String, String> objKeysToSearch_GroupName = new HashMap<String, String>();
        // Indexfelder in XML file sind Gruppennamen der Maske
        boolean groupName_flag = false;
        // ---
        while (indexList != null && indexVersioning != null) {
            if (indexVersioning.getAttribute().equals(indexList.getAttribute()) && indexVersioning.getValue().equals(indexList.getValue())) {
                if(StringUtils.isNumeric(indexVersioning.getValue()))
                {
                    objKeysToSearch.put(Integer.valueOf(indexVersioning.getValue()), indexList.getNext().getValue());
                }
                // --- SL
                else
                {
                    groupName_flag = true;
                    objKeysToSearch_GroupName.put(indexVersioning.getValue(), indexList.getNext().getValue());
                }
                // --- END
                if (fields.isEmpty()) {
                    fields = indexVersioning.getValue();
                } else {
                    fields = fields + "/" + indexVersioning.getValue();
                }
                indexVersioning = indexVersioning.getSibling();
                indexList = indexList.getSibling();
            } else {
                indexList = indexList.getSibling();
            }
        }
        logger.info("\t\t\tVersioning data found, fields : " + fields);
        /*OLD
         ObjKey[] objKeys = new ObjKey[50];
         for (int j = 0; j < arr.length; j++) {
         if (arr[j] != null) {
         objKeys[j] = new ObjKey();
         objKeys[j].setId(j);
         objKeys[j].setData(new String[]{arr[j]});
         }
         }
         */
        ObjKey[] objKeys = null;
        if(!groupName_flag)
        {
            objKeys = new ObjKey[objKeysToSearch.size()];
            int n = 0;
            for (Integer objkeyIndex : objKeysToSearch.keySet()) {
                ObjKey obKey = new ObjKey();
                String obKeyName = Archiving.maskLinesIdToName.get(map.get("type").getValue()).get(objkeyIndex - 1);
                obKey.setName(obKeyName);
                obKey.setData(new String[]{objKeysToSearch.get(objkeyIndex)});
                objKeys[n++] = obKey;
                logger.debug("Searching by objkey : " + obKey.getName() + " with value : " + obKey.getData()[0]);
            }
        }
        // --- SL
        else 
        {
           objKeys = new ObjKey[objKeysToSearch_GroupName.size()];
            int n = 0;
            for (String objkeyIndex : objKeysToSearch_GroupName.keySet()) {
                ObjKey obKey = new ObjKey();
                
                obKey.setName(objkeyIndex);
                obKey.setData(new String[]{objKeysToSearch_GroupName.get(objkeyIndex)});
                objKeys[n++] = obKey;
                logger.debug("Searching by objkey : " + obKey.getName() + " with value : " + obKey.getData()[0]);
            } 
        }
        // END
        findInfo.getFindByIndex().setObjKeys(objKeys);
        FindResult result = _connection.ix().findFirstSords(findInfo, 100, sordZ);
        Sord[] sords = result.getSords();
        if (sords != null && sords.length > 0 && map.get("type").getValue().equals(sords[0].getMaskName())) {
            logger.debug("\t\t\tSearch old version, found " + sords.length + " entries");
            try {
                Sord sordWithLargXDateIso = getSordWithLargXDateIso(sords);
                objId = sordWithLargXDateIso.getId();
                objPath = sordWithLargXDateIso.getPath();
            } catch (java.text.ParseException ex) {
                logger.debug("\t\t\tCannot detect the XDateIso of Sords from findFirstSords:  " + ex.getMessage());
                objId = sords[0].getId();
                objPath = sords[0].getPath();
                // -----                        
                //Importer.reportException(Importer.getMailContent(ex));
                // -----
            }
            return true;
        } else {
            return false;
        }
    }
//
    public void setIndexValueSpalte(ObjKey key, String value) {
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
//
    public FindResult findFirstSords(FindInfo findInfo) throws RemoteException {
        if (!connected) {
            login();
        }
        return _connection.ix().findFirstSords(findInfo, 1, SordC.mbMinDocVersion);
    }
//
    public EditInfo checkoutDoc() throws RemoteException {
        if (!connected) {
            login();
        }
        return _connection.ix().checkoutDoc(String.valueOf(objId), null, EditInfoC.mbSordDoc, LockC.NO);
    }
//
    public void copySord(String newParentId, String objId) throws RemoteException {
        if (!connected) {
            login();
        }
        _connection.ix().copySord(newParentId, objId, null, CopySordC.MOVE);
    }
//
    public void createReferenz(String newParentId, String objId) throws RemoteException {
        if (!connected) {
            login();
        }
        _connection.ix().copySord(newParentId, objId, null, CopySordC.REFERENCE);
    }
//
    public void deleteSord(String parentId, String objId) throws RemoteException {
        if (!connected) {
            login();
        }
        _connection.ix().deleteSord(parentId, objId, LockC.NO, null);
    }
//
    public Sord getSord(int sordId) throws RemoteException {
        if (!connected) {
            login();
        }
        return _connection.ix().checkoutSord(String.valueOf(sordId), EditInfoC.mbSord, LockC.NO).getSord();
    }
//
    public LinkedList<Integer> getParentSords(int sordId, LinkedList<Integer> parentSords) throws RemoteException {
        Sord sord = getSord(sordId);
        if (sord.getParentId() != 1) {
            Sord parentSord = getSord(sord.getParentId());
            parentSords.add(parentSord.getId());
            getParentSords(parentSord.getId(), parentSords);
        }
        return parentSords;
    }
//
    private Sord getSordWithLargXDateIso(Sord[] sords) throws java.text.ParseException {
        if (sords.length == 1) {
            return sords[0];
        } else {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            java.util.Date currentDate, largDate = formatter.parse(sords[0].getIDateIso());
            Sord resultSord = sords[0];
            for (int i = 0; i < sords.length; i++) {
                currentDate = formatter.parse(sords[i].getIDateIso());
                if (currentDate.after(largDate)) {
                    largDate = currentDate;
                    resultSord = sords[i];
                }
            }
            return resultSord;
        }
    }
//
    public String getIndexServerVersion() {
//        try {
//            return this.indexClient.getImplVersion();
//        } catch (de.elo.utils.net.RemoteException RE) {
//            return null;
//        }
        return _connection.getImplVersion();
    }
    
    // --- SL: Access the key of indexfield in teh mask
    public String getFieldNameByGroupName(String maskName, String groupname) throws RemoteException 
    {
        DocMask docMask = _connection.ix().checkoutDocMask(maskName, DocMaskC.mbAll, LockC.NO);
        DocMaskLine[] objLines = docMask.getLines();
        for ( int i = 0; i < objLines.length; i++){
            DocMaskLine obj = objLines[i];
            if(obj.getKey().equals(groupname))
            {
                return obj.getName();
            }
        }
        return "";
    }           
               
    
}
