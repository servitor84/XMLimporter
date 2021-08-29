
package de.di.elo.client;


import de.elo.ix.client.ClientInfo;
import de.elo.ix.client.Document;
import de.elo.ix.client.EditInfo;
import de.elo.ix.client.EditInfoZ;
import de.elo.ix.client.FindByIndex;
import de.elo.ix.client.FindByVersion;
import de.elo.ix.client.FindInfo;
import de.elo.ix.client.FindResult;
import de.elo.ix.client.IXClient;
import de.elo.ix.client.IXServicePortC;
import de.elo.ix.client.LockZ;
import de.elo.ix.client.LoginResult;
import de.elo.ix.client.ServerInfo;
import de.elo.ix.client.Sord;
import de.elo.ix.client.SordC;
import de.elo.ix.client.SordZ;
import de.elo.ix.client.WFTypeZ;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import de.elo.utils.net.RemoteException;

import de.di.xml.gui.Encryption;
import de.elo.ix.client.IXConnFactory;
import de.elo.ix.client.IXConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author A. Sopicki
 */
public class ELOClient {
    //private IXClient indexClient;
    private ClientInfo clientInfo;
    private String connectionUrl;
    private String userName;
    private String password;
    private IXServicePortC ixConstants;
    private LoginResult loginResult = null;
    
    private Encryption encryptor;
    
    private boolean connected = false;
    
    // SL - 20.07.2021  -   ELO20
    public IXConnFactory indexClient = null;
    public IXConnection _connection = null;
       
    public ELOClient() {
       this(null);
    }
    
    public ELOClient(java.util.Properties settings) {
        init(settings);
    }
    
    private void init(java.util.Properties settings) {
        if ( settings == null ) {
            settings = new java.util.Properties();
            try {
            settings.load(new FileInputStream("../conf/config.properties"));
            } catch (FileNotFoundException fnfe){
                System.out.println("Cannot found setting.properties " + fnfe.getMessage());
            } catch (IOException ioe){
                System.out.println("IOExcetpion : " + ioe.getMessage());
            }
        }
        connectionUrl = settings.getProperty("IndexServer.URL");
        userName = settings.getProperty("IndexServer.User");
        password = settings.getProperty("IndexServer.Password");
        // PWD decription ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        String pattern = "^((\\d){1,})([-]{1}(\\d){1,}){1,}";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = p.matcher(password);
        if(matcher.matches()) {
            try {
                de.elo.utils.sec.DesEncryption des = new de.elo.utils.sec.DesEncryption();
                password = des.decrypt(password);
            } catch (Exception ex) {
                Logger.getLogger(ELOClient.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        
        if (clientInfo == null) {
            clientInfo = new ClientInfo();
        }

        clientInfo.setTimeZone(getSystemTimeZone());
        
        if (indexClient == null) {
            // indexClient = new de.elo.ix.client.IXClient(getConnectionUrl());
            indexClient = new IXConnFactory(getConnectionUrl(), "", "");
        }
    }
    
    public void alive() throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
            
            return;
        }        
        // indexClient.ix.alive(clientInfo);
        _connection.ix().alive();
    }
    
    public ServerInfo getServerInfo() throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }        
        // return indexClient.ix.getServerInfo(clientInfo);
        return _connection.ix().getServerInfo();
    }
    
    public Document checkinDocBegin(Document doc) throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }
        
        // return indexClient.ix.checkinDocBegin(clientInfo, doc);
        return _connection.ix().checkinDocBegin(doc);
    }
    
    public Document checkinDocEnd(Sord docSord, SordZ sordInfo, Document doc, LockZ lockInfo) throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }
        
        // return indexClient.ix.checkinDocEnd(clientInfo, docSord, sordInfo, doc, lockInfo);
        return _connection.ix().checkinDocEnd(docSord, sordInfo, doc, lockInfo);
    }
    
    public int checkinSord(Sord parent, SordZ sordInfo, LockZ lockInfo) throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }        
        // return indexClient.ix.checkinSord(clientInfo, parent, sordInfo, lockInfo);
        return _connection.ix().checkinSord(parent, sordInfo, lockInfo);
    }
    
    public EditInfo checkoutSord(String path, EditInfoZ editInfo, LockZ lockInfo) throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }        
        // return indexClient.ix.checkoutSord(clientInfo, path, editInfo, lockInfo);
        return _connection.ix().checkoutSord(path, editInfo, lockInfo);
    }
    
    public EditInfo createDoc(String parent, String mask, String template, EditInfoZ editInfo) throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }        
        // return indexClient.ix.createSord(clientInfo, parent, mask, editInfo);
        return _connection.ix().createSord(parent, mask, editInfo);
    }
    
    public  EditInfo createSord(String parent, String mask, EditInfoZ editInfo) throws RemoteException, java.rmi.RemoteException {
        if ( !connected ) {
            login();
        }        
        // return indexClient.ix.createSord(clientInfo, parent, mask, editInfo);
        return _connection.ix().createSord(parent, mask, editInfo);
    }
    
    public Sord findDoc(String md5, String name) throws RemoteException, java.rmi.RemoteException {
        FindInfo info = new FindInfo();
        FindByVersion versionInfo = new FindByVersion();
        FindByIndex indexInfo = new FindByIndex();
        versionInfo.setVersionMD5(md5);
        indexInfo.setName(name);
        info.setFindByVersion(versionInfo);
        info.setFindByIndex(indexInfo);
        
        // FindResult result = indexClient.ix.findFirstSords(clientInfo, info, 1, SordC.mbMinDocVersion);
        FindResult result = _connection.ix().findFirstSords(info, 1, SordC.mbMinDocVersion);
        Sord[] sords = result.getSords();
        
        if ( sords != null && sords.length > 0 ) {
            return sords[0];
        }
        
        return null;
    }
    
    public void close() {
        if ( connected ) {
           logoff(); 
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public IXServicePortC getServicePort() {
        return ixConstants;
    }
    
    public int getTicketLifeTime() {
        if ( !connected ) {
            return 0;
        }
        
        return loginResult.getTicketLifetime();
    }
    
    public void refSord(String oldParentId, String newParentId, String objId, int manSortIdx) throws RemoteException, java.rmi.RemoteException {
        // indexClient.ix.refSord(clientInfo, oldParentId, newParentId, objId, manSortIdx);
        _connection.ix().refSord(oldParentId, newParentId, objId, manSortIdx);
    }
    
    public String upload(String url, java.io.File file) throws RemoteException, java.rmi.RemoteException {
        // return indexClient.upload(url, file);
        return _connection.upload(url, file);
    }

    public int startWorkFlow(String template, String workflow, String objId) throws RemoteException, java.rmi.RemoteException {
      // return indexClient.ix.startWorkFlow(clientInfo, template, workflow, objId);
        return _connection.ix().startWorkFlow(template, workflow, objId);
    }

    public void deleteWorkFlow(Integer workflowId, WFTypeZ type, LockZ lock) throws RemoteException, java.rmi.RemoteException {
      // indexClient.ix.deleteWorkFlow(clientInfo, workflowId.toString(), type, lock);
        _connection.ix().deleteWorkFlow(workflowId.toString(), type, lock);
    }

    private String getPassword() {
        return password;
    }

    private String getConnectionUrl() {
        return connectionUrl;
    }

    private String getSystemTimeZone() {
        return java.util.TimeZone.getDefault().getID();
    }
    
    private String getLocalHost() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }
        
    private void login() throws RemoteException, java.rmi.RemoteException {
        try {
            // loginResult = indexClient.login(clientInfo, getUserName(), getPassword(), getLocalHost(), null);
            // ixConstants = indexClient.getCONST(clientInfo);
            _connection = indexClient.create(getUserName(), getPassword(), getLocalHost(), null);            
            ixConstants = _connection.getCONST();
            loginResult = _connection.getLoginResult(); 
            clientInfo = this.loginResult.getClientInfo();            
        } catch (UnknownHostException uhex) {
            throw new RemoteException("Unable to login due to a network problem", uhex);
        } catch( Exception ex ) {
            throw new RemoteException("Unable to login due to a network problem", ex);
        }
                
        
        connected = true;
    }
    
    private void logoff() {
        try {
            // indexClient.logout(clientInfo);
            _connection.logout();
        } catch (Exception ex) {

        } finally {
            connected = false;
        }
    }
    
    @Override
    public void finalize() {
        if ( connected ) {
            try {
                // indexClient.logout(clientInfo);
                _connection.logout();
            } catch (Exception ex) {
                
            }
        }
    }
}
