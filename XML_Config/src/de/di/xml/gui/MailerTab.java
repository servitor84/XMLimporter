/*
 * MailerTab.java
 *
 * Created on 06.09.2009, 11:42:33
 */
package de.di.xml.gui;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author A. Sopicki
 */
public class MailerTab extends javax.swing.JPanel implements ConfigTab {

  private Config config = null;

  private boolean setup = false;

  private ResourceBundle bundle = null;

  private String title = "Mailsender";

  private static final String bundleName = "de/di/xml/gui/resources/MailerTab";

  /** Creates new form MailerTab */
  public MailerTab() {
    initComponents();

    bundle = ResourceBundle.getBundle(bundleName);

    title = bundle.getString("tabTitle.text");
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        useMailsenderCheckBox = new javax.swing.JCheckBox();
        mailQueueDirTextField = new javax.swing.JTextField();
        fileChooserButton = new javax.swing.JButton();
        mailQueueLabel = new javax.swing.JLabel();
        attachProtocolFileCheckBox = new javax.swing.JCheckBox();
        attachImportFileCheckBox = new javax.swing.JCheckBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("MAILsender"));
        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(MailerTab.class);
        useMailsenderCheckBox.setText(resourceMap.getString("useMailsenderCheckBox.text")); // NOI18N
        useMailsenderCheckBox.setName("useMailsenderCheckBox"); // NOI18N
        useMailsenderCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMailsenderCheckBoxActionPerformed(evt);
            }
        });

        mailQueueDirTextField.setName("mailQueueDirTextField"); // NOI18N

        fileChooserButton.setText(resourceMap.getString("fileChooserButton.text")); // NOI18N
        fileChooserButton.setName("fileChooserButton"); // NOI18N
        fileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserButtonActionPerformed(evt);
            }
        });

        mailQueueLabel.setText(resourceMap.getString("mailQueueLabel.text")); // NOI18N
        mailQueueLabel.setName("mailQueueLabel"); // NOI18N

        attachProtocolFileCheckBox.setText(resourceMap.getString("attachProtocolFileCheckBox.text")); // NOI18N
        attachProtocolFileCheckBox.setName("attachProtocolFileCheckBox"); // NOI18N
        attachProtocolFileCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachProtocolFileCheckBoxActionPerformed(evt);
            }
        });

        attachImportFileCheckBox.setText(resourceMap.getString("importFileCheckBox.text")); // NOI18N
        attachImportFileCheckBox.setName("attachImportFileCheckBox"); // NOI18N
        attachImportFileCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attachImportFileCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attachImportFileCheckBox)
                    .addComponent(attachProtocolFileCheckBox)
                    .addComponent(useMailsenderCheckBox)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(mailQueueLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mailQueueDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(fileChooserButton)))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useMailsenderCheckBox)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mailQueueLabel)
                    .addComponent(mailQueueDirTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileChooserButton))
                .addGap(18, 18, 18)
                .addComponent(attachProtocolFileCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(attachImportFileCheckBox)
                .addContainerGap(133, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

  private void useMailsenderCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMailsenderCheckBoxActionPerformed
    if (setup) {
      return;
    }

    config.setProperty(
        Config.Property.MailsenderUseMailQueue,
        Boolean.toString(useMailsenderCheckBox.isSelected()));

    toggleUseMailsender(useMailsenderCheckBox.isSelected());
  }//GEN-LAST:event_useMailsenderCheckBoxActionPerformed

  private void fileChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserButtonActionPerformed
    File f = getSelectedDirectory(config.getProperty(
        Config.Property.DirectoriesMailQueue));

    if (f != null) {
      try {
        if (!f.exists()) {
          throw new Exception(bundle.getString("missingDirectory.text"));
        }

        if (!f.canWrite()) {
          throw new Exception(bundle.getString("noWriteAccess.text"));
        }

        if (!f.canRead()) {
          throw new Exception(bundle.getString("noReadAccess.text"));
        }
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(),
            bundle.getString("errorDialogTitle.text"),
            JOptionPane.ERROR_MESSAGE);
      }
      mailQueueDirTextField.setText(f.getAbsolutePath());
      config.setProperty(Config.Property.DirectoriesMailQueue,
          f.getAbsolutePath());
    }
  }//GEN-LAST:event_fileChooserButtonActionPerformed

  private void attachProtocolFileCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachProtocolFileCheckBoxActionPerformed
    if ( setup ) {
      return;
    }
    
    config.setProperty(Config.Property.MailsenderAttachProtocolFile,
        Boolean.toString(attachProtocolFileCheckBox.isSelected()));
  }//GEN-LAST:event_attachProtocolFileCheckBoxActionPerformed

  private void attachImportFileCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachImportFileCheckBoxActionPerformed
    if ( setup ) {
      return;
    }

    config.setProperty(Config.Property.MailsenderAttachImportFile,
        Boolean.toString(attachImportFileCheckBox.isSelected()));
  }//GEN-LAST:event_attachImportFileCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox attachImportFileCheckBox;
    private javax.swing.JCheckBox attachProtocolFileCheckBox;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField mailQueueDirTextField;
    private javax.swing.JLabel mailQueueLabel;
    private javax.swing.JCheckBox useMailsenderCheckBox;
    // End of variables declaration//GEN-END:variables

  public JPanel getJPanel() {
    return this;
  }

  public String getTitle() {
    return title;
  }

  public void setConfig(Config c) {
    setup = true;
    config = c;

    boolean useMailsender = Boolean.parseBoolean(
        config.getProperty(Config.Property.MailsenderUseMailQueue));
    boolean attachProtocolFile = Boolean.parseBoolean(
        config.getProperty(Config.Property.MailsenderAttachProtocolFile));
    boolean attachImportFile = Boolean.parseBoolean(
        config.getProperty(Config.Property.MailsenderAttachImportFile));

    mailQueueDirTextField.setText(config.getProperty(Config.Property.DirectoriesMailQueue, ""));

    useMailsenderCheckBox.setSelected(useMailsender);
    attachProtocolFileCheckBox.setSelected(attachProtocolFile);
    attachImportFileCheckBox.setSelected(attachImportFile);

    toggleUseMailsender(useMailsender);

    setup = false;
  }

  private void toggleUseMailsender(boolean enabled) {
    mailQueueDirTextField.setEnabled(enabled);
    fileChooserButton.setEnabled(enabled);
    attachImportFileCheckBox.setEnabled(enabled);
    attachProtocolFileCheckBox.setEnabled(enabled);
  }

  private File getSelectedDirectory(String filename) {
    File f = null;

    if (filename != null) {
      f = new File(filename);
    }
    JFileChooser fileChooser = new JFileChooser();

    if (f != null && f.exists()) {
      fileChooser.setCurrentDirectory(f);
    }
    
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    }

    return null;
  }
}