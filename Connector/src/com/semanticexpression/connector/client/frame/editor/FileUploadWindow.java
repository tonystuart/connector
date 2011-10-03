// Copyright 2011 Semantic Expression, Inc. All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the GNU General Public License, either version 3 or (at your option)
// any later version. The terms of this license may be found at
// http://www.gnu.org/copyleft/gpl.html
//
// This program is made available on an "as is" basis, without warranties or
// conditions of any kind, either express or implied.
//
// Please contact us for other licensing options.
//
// Contributors:
//
// Anthony F. Stuart - Initial implementation
//
//
//

package com.semanticexpression.connector.client.frame.editor;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.semanticexpression.connector.client.ClientUrlBuilder;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.DefaultButtonWindow;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.UrlConstants;

public class FileUploadWindow extends DefaultButtonWindow
{
  private Button cancelButton;
  private FileUploadField fileNameFileUploadField;
  private FormPanel fileUploadFormPanel;
  private HiddenField<String> fileUploadIdHiddenField;
  private Button okayButton;
  private Listener<FormEvent> submitCallback;

  public FileUploadWindow()
  {
    setSize("400", "125");
    setHeading("File Upload");
    setLayout(new FitLayout());
    setModal(true);
    add(getFileUploadFormPanel());
    setFocusWidget(getFileNameFileUploadField());
    setDefaultButton(getOkayButton());
  }

  public void display(Component component, Id uploadContentId, Listener<FormEvent> submitCallback)
  {
    this.fileUploadIdHiddenField.setValue(uploadContentId.formatString());
    this.submitCallback = submitCallback;

    show();
    toFront();
    alignTo(component.getElement(), "c-c?", null);
  }

  public Button getCancelButton()
  {
    if (cancelButton == null)
    {
      cancelButton = new Button("Cancel");
      cancelButton.setIcon(Resources.CANCEL);
      cancelButton.addSelectionListener(new CancelButtonSelectionListener());
    }
    return cancelButton;
  }

  public FileUploadField getFileNameFileUploadField()
  {
    if (fileNameFileUploadField == null)
    {
      fileNameFileUploadField = new FileUploadField();
      fileNameFileUploadField.setAllowBlank(false);
      fileNameFileUploadField.setName(Keys.FILE_NAME);
      fileNameFileUploadField.setFieldLabel("File Name");
    }
    return fileNameFileUploadField;
  }

  public FormPanel getFileUploadFormPanel()
  {
    if (fileUploadFormPanel == null)
    {
      fileUploadFormPanel = new FormPanel();
      fileUploadFormPanel.setBodyBorder(false);
      fileUploadFormPanel.setMethod(Method.POST);
      fileUploadFormPanel.setEncoding(Encoding.MULTIPART);
      fileUploadFormPanel.setAction(ClientUrlBuilder.getContextRootQualifiedUrl(UrlConstants.URL_CONTENT));
      fileUploadFormPanel.setHeaderVisible(false);
      fileUploadFormPanel.setHeading("File Upload Form Panel");
      fileUploadFormPanel.setCollapsible(true);
      fileUploadFormPanel.add(getFileUploadIdHiddenField()); // must precede FileUploadField if ServletFileUpload is to process first
      fileUploadFormPanel.add(getFileNameFileUploadField(), new FormData("100%"));
      fileUploadFormPanel.addButton(getCancelButton());
      fileUploadFormPanel.addButton(getOkayButton());
      fileUploadFormPanel.addListener(Events.Submit, new SubmitListener());
    }
    return fileUploadFormPanel;
  }

  private HiddenField<String> getFileUploadIdHiddenField()
  {
    if (fileUploadIdHiddenField == null)
    {
      fileUploadIdHiddenField = new HiddenField<String>();
      fileUploadIdHiddenField.setName(Keys.CONTENT_ID);
    }
    return fileUploadIdHiddenField;
  }

  public Button getOkayButton()
  {
    if (okayButton == null)
    {
      okayButton = new Button("Okay");
      okayButton.setIcon(Resources.OKAY);
      okayButton.addSelectionListener(new SubmitButtonSelectionListener());
    }
    return okayButton;
  }

  private class CancelButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      hide();
    }
  }

  private class SubmitButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      getFileUploadFormPanel().submit();
    }
  }

  private final class SubmitListener implements Listener<FormEvent>
  {
    @Override
    public void handleEvent(FormEvent be)
    {
      hide();
      if (submitCallback != null)
      {
        submitCallback.handleEvent(be);
      }
    }
  }

}
