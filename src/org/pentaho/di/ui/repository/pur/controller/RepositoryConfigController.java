package org.pentaho.di.ui.repository.pur.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.pur.PurRepositoryLocation;
import org.pentaho.di.repository.pur.PurRepositoryMeta;
import org.pentaho.di.ui.repository.pur.IRepositoryConfigDialogCallback;
import org.pentaho.di.ui.repository.pur.PurRepositoryDialog;
import org.pentaho.di.ui.repository.pur.model.RepositoryConfigModel;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import com.pentaho.repository.pur.ws.IUnifiedRepositoryWebService;

public class RepositoryConfigController extends AbstractXulEventHandler{
 
  public String getName() {
    return "repositoryConfigController"; //$NON-NLS-1$
  }

  public static final String PLUGIN_PROPERTIES_FILE = "plugins/repositories/pur-repository-plugin/plugin.properties"; //$NON-NLS-1$
  public static final String DEFAULT_URL = "default-url";  //$NON-NLS-1$
  private XulDialog repositoryConfigDialog;
  private XulTextbox url;
  private XulTextbox name;
  private XulTextbox description;
  private XulCheckbox modificationComments;
  private XulButton okButton;
  private RepositoryConfigModel model;
  private BindingFactory bf;
  private IRepositoryConfigDialogCallback callback;
  private RepositoryMeta repositoryMeta;
  private ResourceBundle messages;
  private XulMessageBox messageBox;
  public RepositoryConfigController() {
    
  }
  public void init() throws ControllerInitializationException {
    bf = new DefaultBindingFactory();
    bf.setDocument(this.getXulDomContainer().getDocumentRoot());
    model = new RepositoryConfigModel();
    try {
      messageBox = (XulMessageBox) document.createElement("messagebox"); //$NON-NLS-1$
    } catch (Throwable th) {
      throw new ControllerInitializationException(th);
    }
    if(bf != null) {
      createBindings();
    }
    initializeModel();
  }
  
  private void createBindings() {
    repositoryConfigDialog = (XulDialog) document.getElementById("repository-config-dialog");//$NON-NLS-1$
    url = (XulTextbox) document.getElementById("repository-url");//$NON-NLS-1$
    name = (XulTextbox) document.getElementById("repository-name");//$NON-NLS-1$
    description = (XulTextbox) document.getElementById("repository-description");//$NON-NLS-1$
    modificationComments = (XulCheckbox) document.getElementById("repository-modification-comments");//$NON-NLS-1$
    okButton = (XulButton) document.getElementById("repository-config-dialog_accept"); //$NON-NLS-1$
    bf.setBindingType(Type.BI_DIRECTIONAL);
    bf.createBinding(model, "url", url, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(model, "name", name, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(model, "description", description, "value");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(model, "modificationComments", modificationComments, "checked");//$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Type.ONE_WAY);
    bf.createBinding(model, "valid", okButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public void ok() {
    if(repositoryMeta instanceof PurRepositoryMeta) {
      repositoryMeta.setDescription(model.getDescription());
      repositoryMeta.setName(model.getName());
      ((PurRepositoryMeta)repositoryMeta).setRepositoryLocation(new PurRepositoryLocation(model.getUrl()));
      ((PurRepositoryMeta)repositoryMeta).setVersionCommentMandatory(model.isModificationComments());
      getCallback().onSuccess(((PurRepositoryMeta)repositoryMeta));
    } else {
      getCallback().onError(new IllegalStateException(BaseMessages.
          getString(PurRepositoryDialog.class, "RepositoryConfigDialog.ERROR_0001_NotAnInstanceOfPurRepositoryMeta"))); //$NON-NLS-1$
    }
  }
  
  public void cancel() {
    if(!repositoryConfigDialog.isHidden()) {
      repositoryConfigDialog.hide();
      getCallback().onCancel();
    }
  }
  
  public void test() {
    final String url = model.getUrl() + "/webservices/unifiedRepository?wsdl"; //$NON-NLS-1$
    Service service;
    try {
      service = Service.create(new URL(url), new QName("http://www.pentaho.org/ws/1.0", "unifiedRepository")); //$NON-NLS-1$ //$NON-NLS-2$
      if(service != null) {
        IUnifiedRepositoryWebService repoWebService = service.getPort(IUnifiedRepositoryWebService.class);
        if(repoWebService != null) {
          messageBox.setTitle(messages.getString("Dialog.Success"));//$NON-NLS-1$
          messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
          messageBox.setMessage(messages.getString("RepositoryConfigDialog.RepositoryUrlTestPassed"));//$NON-NLS-1$
          messageBox.open();

        } else {
          messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
          messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
          messageBox.setMessage(messages.getString("RepositoryConfigDialog.RepositoryUrlTestFailed"));//$NON-NLS-1$
          messageBox.open();
        }
      } else {
        messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
        messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
        messageBox.setMessage(messages.getString("RepositoryConfigDialog.RepositoryUrlTestFailed"));//$NON-NLS-1$
        messageBox.open();

      }
    } catch (Exception e) {
      messageBox.setTitle(messages.getString("Dialog.Error"));//$NON-NLS-1$
      messageBox.setAcceptLabel(messages.getString("Dialog.Ok"));//$NON-NLS-1$
      messageBox.setMessage(BaseMessages.getString(PurRepositoryDialog.class,
          "RepositoryConfigDialog.RepositoryUrlTestFailedMessage", e.getLocalizedMessage()));//$NON-NLS-1$
      messageBox.open();
    }
  }
  
  private String getDefaultUrl() {
    String returnValue = ""; //$NON-NLS-1$
    FileInputStream fis = null;
    Properties properties = null;
    try {
      File file = new File(PLUGIN_PROPERTIES_FILE);
      fis = new FileInputStream(file);
    } catch (IOException e1) {
      return returnValue;
    }
    if (null != fis) {
      properties = new Properties();
      try {
        properties.load(fis);
      } catch (IOException e) {
        return returnValue;
      }
    }
    if (properties != null) {
      returnValue = properties.getProperty(DEFAULT_URL, "");//$NON-NLS-1$
    }
    return returnValue;
 }
  
  public IRepositoryConfigDialogCallback getCallback() {
    return callback;
  }
  public void setCallback(IRepositoryConfigDialogCallback callback) {
    this.callback = callback;
  }
 
  public void setRepositoryMeta(RepositoryMeta repositoryMeta) {
    this.repositoryMeta = repositoryMeta;
  }

  public void updateModificationComments() {
    model.setModificationComments(modificationComments.isChecked());
  }
  private void initializeModel() {
    PurRepositoryMeta purRepositoryMeta = null;
    if (repositoryMeta != null && repositoryMeta instanceof PurRepositoryMeta) {
      purRepositoryMeta = (PurRepositoryMeta) repositoryMeta;
      model.setDescription(purRepositoryMeta.getDescription());
      model.setName(purRepositoryMeta.getName());
      PurRepositoryLocation location = purRepositoryMeta.getRepositoryLocation();
      if (location != null) {
        model.setUrl(location.getUrl());
      } else {
        model.setUrl(getDefaultUrl());
      }
    } else {
      model.setModificationComments(true);
    }
  }
  
  public ResourceBundle getMessages() {
    return messages;
  }
  public void setMessages(ResourceBundle messages) {
    this.messages = messages;
  }
}