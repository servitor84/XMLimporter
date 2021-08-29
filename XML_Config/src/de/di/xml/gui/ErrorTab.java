package de.di.xml.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

public class ErrorTab extends JPanel implements ConfigTab
{
  private String title = "Error";
  private Config config = null;
  private ResourceMap resourceMap = null;
  private DefaultListModel mod = new DefaultListModel();
  private Properties prop = new Properties();
  private final String CONFIG_PATH = "../conf/config.properties";
  private JButton jButton1;
  private JButton jButton2;
  private JButton jButton3;
  private JButton jButton4;
  private JButton jButton5;
  private JLabel jLabel1;
  private JLabel jLabel2;
  private JLabel jLabel3;
  private JLabel jLabel4;
  private JList jList1;
  private JScrollPane jScrollPane1;
  private JTextField jTextField1;
  // +++ addition of more components V.2.0 +++ 
  private JTextArea errorOutput;
  private JScrollPane jScrollPane2;
  private JSplitPane splitPane;
  private JPanel topHalf;
  private JPanel bottomHalf;  
  int location = 0;
  // +++ END +++
  
  private String correctPath;
  
  public ErrorTab()
  {
    this.resourceMap = ((ConfigApp)Application.getInstance(ConfigApp.class)).getContext().getResourceMap(ErrorTab.class);
    
    this.title = this.resourceMap.getString("tabTitle.text", new Object[0]);
    initComponents();
    refreshJList();
  }
  
  private void initComponents()
  {
    this.jButton1 = new JButton();
    this.jButton2 = new JButton();
    this.jScrollPane1 = new JScrollPane();
    this.jList1 = new JList();
    this.jButton3 = new JButton();
    this.jButton4 = new JButton();
    this.jButton5 = new JButton();
    this.jLabel1 = new JLabel();
    this.jLabel2 = new JLabel();
    this.jLabel3 = new JLabel();
    this.jLabel4 = new JLabel();
    this.jTextField1 = new JTextField();
    // +++ instantiation of added components V.2.0 +++
    this.errorOutput = new JTextArea("No error message");
    this.jScrollPane2 = new JScrollPane();
    this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    this.topHalf = new JPanel(new BorderLayout());
    this.bottomHalf = new JPanel(new BorderLayout());
    // +++ END +++
    setName("Form");
    
    ResourceBundle bundle = ResourceBundle.getBundle("de/di/xml/gui/resources/ErrorTab");
    this.jButton1.setText(bundle.getString("launch.again"));
    this.jButton1.setName("jButton1");
    this.jButton1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        ErrorTab.this.jButton1ActionPerformed(evt);
      }
    });
    this.jButton2.setText(bundle.getString("open.in.text.editor"));
    this.jButton2.setName("jButton2");
    this.jButton2.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        ErrorTab.this.jButton2ActionPerformed(evt);
      }
    });
    this.jScrollPane1.setName("jScrollPane1");
    this.jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);    
    
    this.jList1.setModel(this.mod);
    this.jList1.setName("jList1");
    this.jScrollPane1.setViewportView(this.jList1);
    // +++ addition of a listener for the JList component +++
    this.jList1.addListSelectionListener
    (
        new ListSelectionListener() 
        {
            public void valueChanged(ListSelectionEvent e) {
                ErrorTab.this.showError(e);
            }
        }       
    );
    // +++ END +++
    
    // +++ addition of an area for errortext output V.2.0 +++           
    this.errorOutput.setEditable(false);  
    this.errorOutput.setForeground(Color.red);
    this.jScrollPane2.setName("jScrollPane2");    
    this.jScrollPane2.setViewportView(this.errorOutput);
    // +++ END +++
    
    // +++ addition of a splitcontainer for JScrollpanes (1 and 2) V.2.0 +++                
    topHalf.setBorder(BorderFactory.createTitledBorder("Faulty documents"));
    bottomHalf.setBorder(BorderFactory.createTitledBorder("Error text"));     
    //topHalf.add(tableContainer);        
    topHalf.setPreferredSize(new Dimension(-1, 300));    
    topHalf.add(jScrollPane1);    
    bottomHalf.add(jScrollPane2);          
    splitPane.add(topHalf);
    splitPane.add(bottomHalf);
    add(splitPane);        
    
    location = splitPane.getDividerLocation();    
    bottomHalf.setVisible(false);
    // +++ END +++
    
    this.jButton3.setText(bundle.getString("Error.Get.File"));
    this.jButton3.setName("jButton3");
    this.jButton3.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        ErrorTab.this.jButton3ActionPerformed(evt);
      }
    });
    this.jButton4.setText(bundle.getString("Error.Delete"));
    this.jButton4.setName("jButton4");
    this.jButton4.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        ErrorTab.this.jButton4ActionPerformed(evt);
      }
    });
    this.jButton5.setText(bundle.getString("Error.Refresh"));
    this.jButton5.setName("jButton5");
    this.jButton5.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        ErrorTab.this.jButton5ActionPerformed(evt);
      }
    });
    this.jLabel1.setText(bundle.getString("Error.Document.Number"));
    this.jLabel1.setName("jLabel1");
    
    this.jLabel2.setName("jLabel2");
    
    this.jLabel3.setText(bundle.getString("Error.Document.After.Filter"));
    this.jLabel3.setName("jLabel3");
    
    this.jLabel4.setName("jLabel4");
    
    this.jTextField1.setName("jTextField1");
    this.jTextField1.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent evt)
      {
        ErrorTab.this.jTextField1FocusLost(evt);
      }
    });
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.splitPane, -1, 617, 32767).addGap(31, 31, 31).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(this.jButton1, -2, 120, -2).addComponent(this.jButton2, -2, 120, -2).addComponent(this.jButton3, -2, 120, -2).addComponent(this.jButton4, -2, 120, -2)).addGap(19, 19, 19)).addGroup(layout.createSequentialGroup().addComponent(this.jLabel1).addGap(66, 66, 66).addComponent(this.jLabel2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jTextField1, -2, 149, -2).addPreferredGap(ComponentPlacement.RELATED, -1, 32767).addComponent(this.jButton5, -2, 110, -2).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.jLabel3).addGap(28, 28, 28).addComponent(this.jLabel4).addGap(182, 182, 182)))));
    
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(13, 32767).addComponent(this.jButton1).addGap(18, 18, 18).addComponent(this.jButton2).addGap(18, 18, 18).addComponent(this.jButton3).addGap(18, 18, 18).addComponent(this.jButton4).addGap(256, 256, 256)).addGroup(layout.createSequentialGroup().addComponent(this.splitPane, -1, 407, 32767).addGap(8, 8, 8))).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(this.jLabel2).addComponent(this.jLabel4).addComponent(this.jButton5).addComponent(this.jLabel1).addComponent(this.jTextField1, -2, -1, -2).addComponent(this.jLabel3)).addContainerGap()));
  }
  
  private void jButton2ActionPerformed(ActionEvent evt)
  {
    openTextFile();
  }
  
  private void jButton1ActionPerformed(ActionEvent evt)
  {
    retry();
  }
  
  private void jButton3ActionPerformed(ActionEvent evt)
  {
    openFile();
  }
  
  private void jButton4ActionPerformed(ActionEvent evt)
  {
    deleteFile();
  }
  
  private void jButton5ActionPerformed(ActionEvent evt)
  {
    if (!this.jTextField1.getText().equals(""))
    {
      if (this.jTextField1.getText().length() == 1)
      {
        ResourceBundle bundle = ResourceBundle.getBundle("de/di/xml/gui/resources/ErrorTab");
        ResourceBundle bundle1 = ResourceBundle.getBundle("de/di/xml/gui/resources/ConfigApp");
        JOptionPane.showConfirmDialog(this, bundle.getString("Error.Min"), bundle1.getString("Application.title"), -1, 2);
      }
      else
      {
        filtering();
      }
    }
    else {
      refreshJList();
    }
  }
  
  private void jTextField1FocusLost(MouseEvent evt) {}
  
  public JPanel getJPanel()
  {
    return this;
  }
  
  public String getTitle()
  {
    return this.title;
  }
  
  public void setConfig(Config c)
  {
    this.config = c;
  }
  
  // +++ addition of eventhandler for JList component+++
    private void showError(ListSelectionEvent e) 
    {        
        //if (!e.getValueIsAdjusting())        
        bottomHalf.setVisible(true);
        splitPane.setDividerLocation(location);
        errorOutput.setText("");
           
        BufferedReader reader = null;
        BufferedReader reader2 = null;
        String selection = null;
        String selection2 = null;
        try {
            selection = (String) jList1.getSelectedValue();
            selection2 = (String) jList1.getSelectedValue();
            //String selection = file.getName();                        
            
            File textFile = new File
                                (
                                    this.prop.getProperty("Directories.ErrorOutput") + "\\"
                                    + selection.substring(0, selection.length() - 4)
                                    + ".err"
                                );
            
            File textFile2 = new File
                                (
                                    this.prop.getProperty("Directories.DuplicatesOutput") + "\\"
                                    + selection2.substring(0, selection2.length() - 4)
                                    + ".err"
                                );
            
            StringBuilder bs = new StringBuilder();
            StringBuilder bs2 = new StringBuilder();
            if(textFile.exists()) { 
                reader = new BufferedReader(new FileReader(textFile)); 
                String line;
                
                while ((line = reader.readLine()) != null) {                
                    bs.append(line);
                    bs.append("\n");
                }
                correctPath = this.prop.getProperty("Directories.ErrorOutput") + "\\" + selection;
                errorOutput.setText(bs.toString());
            } else {            
                if(textFile2.exists()) { reader2 = new BufferedReader(new FileReader(textFile2)); 
                    String line2;

                    while ((line2 = reader2.readLine()) != null) {                
                        bs2.append(line2);
                        bs2.append("\n");
                    }
                }
                correctPath = this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + selection2;
                errorOutput.setText(bs2.toString());
            }            
//            errorOutput.setText(bs.toString());
//            errorOutput.setText(bs2.toString());
        } catch (Exception ex) {
            System.out.println("File reading failed: " + ex);
            //Logger.getLogger(ErrorTab.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // avoiding NPE by selecting entry but no error file exists in error dir
                if(selection != null && reader != null)
                {                    
                    reader.close();
                }                
                if(selection2 != null && reader2 != null)
                {
                    reader2.close();
                }                
            } catch (IOException ex) {
                //Logger.getLogger(ErrorTab.class.getName()).log(Level.ERROR, null, ex);
            }
        }                   
    }
  // +++ END +++
  
  public void refreshJList()
  {
    try
    {      
      this.prop.load(new FileInputStream(CONFIG_PATH));
      String var = this.prop.getProperty("Directories.ErrorOutput");
      String var2 = this.prop.getProperty("Directories.DuplicatesOutput");
      File folder = new File(var);
      File folder2 = new File(var2);
      File[] listOfFiles = folder.listFiles();
      File[] listOfFiles2 = folder2.listFiles();
      int ducumetnsNumber = 0;
      this.mod.clear();      
      for (File f : listOfFiles) {
        if (this.prop.getProperty("Trigger.DocumentFileExtension").contains(f.getName().substring(f.getName().length() - 4)))
        {
          this.mod.addElement(f.getName());
          ducumetnsNumber++;
        }        
      }
      for (File f2 : listOfFiles2) {
        if (this.prop.getProperty("Trigger.DocumentFileExtension").contains(f2.getName().substring(f2.getName().length() - 4)))
        {
          this.mod.addElement(f2.getName());
          ducumetnsNumber++;
        }        
      }
//      if(listOfFiles.length == 0)
//      {
//            JOptionPane.showMessageDialog(this, "Check out the error directory " + listOfFiles.length);
//      }
      this.jLabel2.setText(String.valueOf(ducumetnsNumber));
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Error.java cannot find config.properties " + ex.getMessage());
    }
    this.bottomHalf.setVisible(false);
    //this.jList1.setSelectedIndex(0);
  }
  
  private void openTextFile()
  {
    int position = 0;
    try
    {
      position = this.jList1.getSelectedIndex();
      if (position == -1)
      {
        JOptionPane.showMessageDialog(this, "Either the list of documents is empty or non ducument is selected");
      }
      else
      {
        File text = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".xml"/*this.prop.getProperty("File.Open")*/); // Cleanup.DeleteByExtension
        if(text.exists()) {
            Desktop.getDesktop().edit(text);
        } else {
            File text2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".xml"/*this.prop.getProperty("File.Open")*/);
            Desktop.getDesktop().edit(text2);
        }
        
        //Desktop.getDesktop().open(text);         
//        String notepadPlusPlusDir = "C:\\Program Files (x86)\\notepad++";
//        File path = new File(notepadPlusPlusDir);
//        if(path.exists())
//        {
//            Runtime.getRuntime().exec(path + "\\notepad++.exe " + text);  
//        }                  
//        else
//        {
//           Desktop.getDesktop().edit(text); 
//        }            
      }
    }
    catch (IOException ioex)
    {
      JOptionPane.showMessageDialog(this, "Error.openTextFile() cannot open " + 
              this.mod.elementAt(position).toString().substring(this.mod.elementAt(position).toString().length() - 4) + 
              ".xml" +
              ioex.getMessage());
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Excepiton in Error.openTextFile() " + ex.getMessage());
    }
  }
  
  private void openFile()
  {
    int position = 0;
    try
    {
      position = this.jList1.getSelectedIndex();
      if (position == -1)
      {
        JOptionPane.showMessageDialog(this, "Either the list of documents is empty or non ducument is selected");
      }
      else
      {
        File pdfFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position));
        if (pdfFile.exists() && Desktop.isDesktopSupported()) {
          Desktop.getDesktop().open(pdfFile);
        } else {
          //JOptionPane.showMessageDialog(this, "Awt Desktop is not supported!");
          File pdfFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position));
          Desktop.getDesktop().open(pdfFile2);
        }
      }
    }
    catch (IOException ioex)
    {
      JOptionPane.showMessageDialog(this, "Error.openTextFile() cannot open " + 
              this.mod.elementAt(position).toString().substring(this.mod.elementAt(position).toString().length() - 4) + 
              ".xml" +
              ioex.getMessage());
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Excepiton in Error.openTextFile() " + ex.getMessage());
    }
  }
  
  private void deleteFile()
  {
    ResourceBundle bundle = ResourceBundle.getBundle("de/di/xml/gui/resources/ErrorTab");
    int delete = JOptionPane.showConfirmDialog(this, bundle.getString("Error.Delete.Message"), bundle.getString("Error.Delete.Titel"), 0, 2);
    if (delete == 0)
    {
      int position = 0;
      try
      {
        int[] positions = this.jList1.getSelectedIndices();
        if (positions.length <= 1)
        {
          position = this.jList1.getSelectedIndex();
          if (position == -1)
          {
            JOptionPane.showMessageDialog(this, "Either the list of documents is empty or non ducument is selected");
          }
          else
          {
            File textFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/);
            File pdfFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position));
            File triggerFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + this.prop.getProperty("Importer.FileExtension"));
            File errFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/);
            if(textFile.exists() && pdfFile.exists() && errFile.exists()) {
                if (!pdfFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Document file cannot be removed");
                }
                if (!textFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Text file cannot be removed");
                }
                if (!triggerFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Trigger file cannot be removed");
                }
                                
                if (!errFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Error file cannot be removed");
                }
            } else {
                File textFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/);
                File pdfFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position));
                File triggerFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + this.prop.getProperty("Importer.FileExtension"));
                File errFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/);
                if (!pdfFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Document file cannot be removed");
                }
                if (!textFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Text file cannot be removed");
                }
                if (!triggerFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Trigger file cannot be removed");
                }
                if (!errFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Error file cannot be removed");
                }
            }
          }
        }
        else
        {
          for (int i = 0; i < positions.length; i++)
          {
            File textFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(i).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/);
            File pdfFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(i));
            File triggerFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(i).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + this.prop.getProperty("Importer.FileExtension"));
            File errFile = new File(this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/);
            if(textFile.exists() && pdfFile.exists() && errFile.exists()) {
                if (!pdfFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Document file cannot be removed");
                }
                if (!textFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Text file cannot be removed");
                }
                if (!triggerFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Trigger file cannot be removed");
                }
                if (!errFile.delete()) {
                  JOptionPane.showMessageDialog(this, "Error file cannot be removed");
                }
            } else {
                File textFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/);
                File pdfFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position));
                File triggerFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + this.prop.getProperty("Importer.FileExtension"));
                File errFile2 = new File(this.prop.getProperty("Directories.DuplicatesOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4) + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/);
                if (!pdfFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Document file cannot be removed");
                }
                if (!textFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Text file cannot be removed");
                }
                if (!triggerFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Trigger file cannot be removed");
                }
                if (!errFile2.delete()) {
                  JOptionPane.showMessageDialog(this, "Error file cannot be removed");
                }
            }
          }
        }
        refreshJList();
      }
      catch (Exception ex)
      {
        JOptionPane.showMessageDialog(this, "Excepiton in Error.deleteFile() " + ex.getMessage());
      }
    }
  }
  
  private void retry()
  {
    DefaultListModel modTemp = new DefaultListModel();
    try
    {          
      int[] positions = this.jList1.getSelectedIndices();      
      Arrays.sort(positions);
      int ducumetnsNumber = positions.length;
      for (int position : positions)
      {
        String path = correctPath.substring(0, correctPath.length()-4);
                //this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString().substring(0, this.mod.elementAt(position).toString().length() - 4);
        String doc = correctPath; 
                //this.prop.getProperty("Directories.ErrorOutput") + "\\" + this.mod.elementAt(position).toString();
        deleteFromBackUpDirectory(new File(path).getName(), doc);
        shiftToInputDirectory(path, position);
        modTemp.addElement(this.mod.elementAt(position));                  
      }
      for (int i = 0; i < modTemp.getSize(); i++) {
        for (int j = 0; j < this.mod.getSize(); j++) {
          if (modTemp.elementAt(i) == this.mod.elementAt(j)) {                
            this.mod.remove(j);   
            ducumetnsNumber--;
          }
        }
      }
      this.jList1.setModel(this.mod);      
      this.errorOutput.setText("");
      this.bottomHalf.setVisible(false);
      this.jLabel4.setText(String.valueOf(ducumetnsNumber));      
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Exception in Error.retry() " + ex.getMessage());
    }
    
  }
  
  private void shiftToInputDirectory(String path, int position)
  {
    try
    {
      String inputDirectory = this.prop.getProperty("Directories.Input");
      for (String ext : this.prop.getProperty("Trigger.DocumentFileExtension").split(","))
      {
        File originalFile = new File(path + ext);
        if (originalFile.exists())
        {
          String newOriginalFile = this.mod.elementAt(position).toString();
          originalFile.renameTo(new File(inputDirectory + "\\" + newOriginalFile));
          originalFile.delete();
        }
      }
      File xmlFile = new File(path + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/);
      File sigFile = new File(path + this.prop.getProperty("Importer.FileExtension"));
      File textFile = new File(path + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/);
      if (xmlFile.exists())
      {
        String newTextFile = xmlFile.getName().substring(0, xmlFile.getName().indexOf(".")) + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/;
        xmlFile.renameTo(new File(inputDirectory + "\\" + newTextFile));
        xmlFile.delete();
      }
      if (sigFile.exists())
      {
        String newSigFile = sigFile.getName().substring(0, sigFile.getName().indexOf(".")) + this.prop.getProperty("Importer.FileExtension");
        sigFile.renameTo(new File(inputDirectory + "\\" + newSigFile));
        sigFile.delete();
      }
      if (textFile.exists())
      {              
        String newTextFile = sigFile.getName().substring(0, sigFile.getName().indexOf(".")) + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/;
        sigFile.renameTo(new File(inputDirectory + "\\" + newTextFile));        
        textFile.delete();
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Exception in Error.shiftToInputDirectory() " + ex.getMessage());
    }
  }
  
  private void deleteFromBackUpDirectory(String name, String doc)
  {
    try
    {
      String inputDirectory = this.prop.getProperty("Directories.Backup", "");
      String ext[] = this.prop.getProperty("Trigger.DocumentFileExtension").split(",");
      boolean isThereOriginalDoc = false;
      for(int i = 0; i < ext.length; i++) {
          if(doc.substring(doc.length()-4).equals(ext[i])) {
              isThereOriginalDoc = true;
              break;
          }
      }
      
      File originalFile = null;
      if(isThereOriginalDoc) {
        //originalFile = new File(inputDirectory + "\\" + name + this.prop.getProperty("Trigger.DocumentFileExtension"));
        originalFile = new File(inputDirectory + "\\" + name + doc.substring(doc.length()-4));
      }
      File xmlFile = new File(inputDirectory + "\\" + name + ".xml" /*this.prop.getProperty("Cleanup.DeleteByExtension")*/);
      File textFile = new File(inputDirectory + "\\" + name + ".err" /*this.prop.getProperty("Cleanup.ErrorFile")*/);
      File sigFile = new File(inputDirectory + "\\" + name + this.prop.getProperty("Importer.FileExtension"));
      originalFile.delete();
      xmlFile.delete();
      textFile.delete();
      sigFile.delete();
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Exception in Error.deleteFromBackUpDirectory(). Cannot delete file " + 
              name + doc.substring(doc.length()-4) /*this.prop.getProperty("Trigger.DocumentFileExtension")*/ + " in Backup folder " + ex.getMessage());
    }
  }
  
  private void filtering()
  {      
    String pattern = this.jTextField1.getText();
    try
    {
      this.prop.load(new FileInputStream(CONFIG_PATH));
      String var = this.prop.getProperty("Directories.ErrorOutput");
      File folder = new File(var);
      File[] listOfFiles = folder.listFiles();
      int ducumetnsNumber = 0;      
      System.out.println(this.mod.elementAt(0));
      this.mod.clear();
      for (File f : listOfFiles) {
        if ((this.prop.getProperty("Trigger.DocumentFileExtension").contains(f.getName().substring(f.getName().length() - 4))) && 
          (f.getName().contains(pattern)))
        {
          this.mod.addElement(f.getName());
          ducumetnsNumber++;
        }
      }
      this.jLabel4.setText(String.valueOf(ducumetnsNumber));
      this.jList1.setModel(this.mod);
      
      this.errorOutput.setText("");
      this.bottomHalf.setVisible(false);
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, "Error.java cannot find config.properties " + ex.getMessage());
    }
    //this.jList1.setSelectedIndex(0);
  }
}
