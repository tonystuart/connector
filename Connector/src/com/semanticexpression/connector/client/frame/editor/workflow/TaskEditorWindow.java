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

package com.semanticexpression.connector.client.frame.editor.workflow;

import java.util.Date;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;
import com.semanticexpression.connector.client.frame.editor.EditorStore;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.DropDownComboBox;
import com.semanticexpression.connector.client.widget.ListStoreEditorWindow;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.WorkflowConstants;

public final class TaskEditorWindow extends ListStoreEditorWindow
{
  private DropDownComboBox<ModelData> accessComboBox;
  private LayoutContainer accessLayoutContainer;
  private Text accessText;
  private DateNamePanel assignmentDateNamePanel;
  private Button completeButton;
  private LayoutContainer optionLayoutContainer;
  private DropDownComboBox<ModelData> orderComboBox;
  private LayoutContainer orderLayoutContainer;
  private Text orderText;
  private LayoutContainer remarksLayoutContainer;
  private Text remarksText;
  private SafeTextField<String> remarksTextField;
  private DropDownComboBox<ModelData> statusComboBox;
  private LayoutContainer statusLayoutContainer;
  private Text statusText;
  private TaskCompleteWindow taskCompleteWindow;
  private LayoutContainer taskLayoutContainer;
  private Text taskText;
  private SafeTextField<String> taskTextField;

  public TaskEditorWindow(EditorStore editorStore)
  {
    super(editorStore);
    setSize("400", "400");
    setHeading("Edit Task");
    setIcon(Resources.WORKFLOW_EDIT);
    setLayout(new RowLayout(Orientation.VERTICAL));
    add(getTaskLayoutContainer(), new RowData(1.0, Style.DEFAULT, new Margins(5, 5, 0, 5)));
    add(getAssignmentDateNamePanel(), new RowData(1.0, 1.0, new Margins(5, 5, 0, 5)));
    add(getOptionLayoutContainer(), new RowData(1.0, 50.0, new Margins(5, 5, 0, 5)));
    add(getRemarksLayoutContainer(), new RowData(1.0, Style.DEFAULT, new Margins(5, 5, 5, 5)));
    getOkayCancelToolBar().insert(getCompleteButton(), 0);
  }

  @Override
  protected void cleanup()
  {
    super.cleanup();

    getTaskTextField().setValue(null);
    getAssignmentDateNamePanel().clear();
    getAccessComboBox().setSimpleValue(WorkflowConstants.ACCESS_2_READ_WRITE);
    getOrderComboBox().setSimpleValue(WorkflowConstants.ORDER_1_SEQUENTIAL);
    getStatusComboBox().setSimpleValue(WorkflowConstants.STATUS_1_PENDING);

    if (taskCompleteWindow != null)
    {
      taskCompleteWindow.hide();
    }
  }

  protected void displayTaskCompleteWindow()
  {
    getTaskCompleteWindow().edit(association);
    getTaskCompleteWindow().show();
    getTaskCompleteWindow().toFront();
    getTaskCompleteWindow().alignTo(getElement(), "c-c?", null);
  }

  @Override
  public void edit(Association association, boolean isNew)
  {
    super.edit(association, isNew);

    String task = association.get(Keys.WORKFLOW_TASK);
    Date dueDate = association.get(Keys.WORKFLOW_DUE_DATE);
    String assignedToName = association.get(Keys.WORKFLOW_ASSIGNED_TO_NAME);
    Integer assignedToType = association.get(Keys.WORKFLOW_ASSIGNED_TO_TYPE);
    Integer access = association.get(Keys.WORKFLOW_ACCESS);
    Integer order = association.get(Keys.WORKFLOW_ORDER);
    Integer status = association.get(Keys.WORKFLOW_STATUS);
    String remarks = association.get(Keys.WORKFLOW_REMARKS);

    getTaskTextField().setValue(task);
    getAssignmentDateNamePanel().display(dueDate, assignedToName, assignedToType);
    getAccessComboBox().setSimpleValue(access);
    getOrderComboBox().setSimpleValue(order);
    getStatusComboBox().setSimpleValue(status);
    getRemarksTextField().setValue(remarks);

    setFocusWidget(getTaskTextField());
  }

  public DropDownComboBox<ModelData> getAccessComboBox()
  {
    if (accessComboBox == null)
    {
      accessComboBox = new DropDownComboBox<ModelData>();
      accessComboBox.add(WorkflowModelData.getAccessList());
    }
    return accessComboBox;
  }

  public LayoutContainer getAccessLayoutContainer()
  {
    if (accessLayoutContainer == null)
    {
      accessLayoutContainer = new LayoutContainer();
      accessLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      accessLayoutContainer.add(getAccessText());
      accessLayoutContainer.add(getAccessComboBox(), new RowData(1.0, Style.DEFAULT, new Margins()));
    }
    return accessLayoutContainer;
  }

  public Text getAccessText()
  {
    if (accessText == null)
    {
      accessText = new Text("Access:");
    }
    return accessText;
  }

  public DateNamePanel getAssignmentDateNamePanel()
  {
    if (assignmentDateNamePanel == null)
    {
      assignmentDateNamePanel = new DateNamePanel("Due Date:", "Assigned To:", "assignmentRadio");
      assignmentDateNamePanel.setLayout(new RowLayout(Orientation.VERTICAL));
    }
    return assignmentDateNamePanel;
  }

  public Button getCompleteButton()
  {
    if (completeButton == null)
    {
      completeButton = new Button("Complete");
      completeButton.setIcon(Resources.WORKFLOW_UPDATE);
      completeButton.addSelectionListener(new SelectionListener<ButtonEvent>()
      {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
          displayTaskCompleteWindow();
        }
      });
    }
    return completeButton;
  }

  public LayoutContainer getOptionLayoutContainer()
  {
    if (optionLayoutContainer == null)
    {
      optionLayoutContainer = new LayoutContainer();
      optionLayoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
      optionLayoutContainer.add(getAccessLayoutContainer(), new RowData(0.33, 1.0, new Margins()));
      optionLayoutContainer.add(getOrderLayoutContainer(), new RowData(0.33, 1.0, new Margins(0, 0, 0, 5)));
      optionLayoutContainer.add(getStatusLayoutContainer(), new RowData(0.34, 1.0, new Margins(0, 0, 0, 5)));
    }
    return optionLayoutContainer;
  }

  public DropDownComboBox<ModelData> getOrderComboBox()
  {
    if (orderComboBox == null)
    {
      orderComboBox = new DropDownComboBox<ModelData>();
      orderComboBox.add(WorkflowModelData.getOrderList());
    }
    return orderComboBox;
  }

  public LayoutContainer getOrderLayoutContainer()
  {
    if (orderLayoutContainer == null)
    {
      orderLayoutContainer = new LayoutContainer();
      orderLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      orderLayoutContainer.add(getOrderText());
      orderLayoutContainer.add(getOrderComboBox(), new RowData(1.0, Style.DEFAULT, new Margins()));
    }
    return orderLayoutContainer;
  }

  public Text getOrderText()
  {
    if (orderText == null)
    {
      orderText = new Text("Order:");
    }
    return orderText;
  }

  public LayoutContainer getRemarksLayoutContainer()
  {
    if (remarksLayoutContainer == null)
    {
      remarksLayoutContainer = new LayoutContainer();
      remarksLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      remarksLayoutContainer.add(getRemarksText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins()));
      remarksLayoutContainer.add(getRemarksTextField(), new RowData(1.0, Style.DEFAULT, new Margins()));
    }
    return remarksLayoutContainer;
  }

  public Text getRemarksText()
  {
    if (remarksText == null)
    {
      remarksText = new Text("Remarks:");
    }
    return remarksText;
  }

  public SafeTextField<String> getRemarksTextField()
  {
    if (remarksTextField == null)
    {
      remarksTextField = new SafeTextField<String>();
      remarksTextField.setFieldLabel("Remarks");
    }
    return remarksTextField;
  }

  public DropDownComboBox<ModelData> getStatusComboBox()
  {
    if (statusComboBox == null)
    {
      statusComboBox = new DropDownComboBox<ModelData>();
      statusComboBox.add(WorkflowModelData.getStatusListLong());
    }
    return statusComboBox;
  }

  public LayoutContainer getStatusLayoutContainer()
  {
    if (statusLayoutContainer == null)
    {
      statusLayoutContainer = new LayoutContainer();
      statusLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      statusLayoutContainer.add(getStatusText());
      statusLayoutContainer.add(getStatusComboBox(), new RowData(1.0, Style.DEFAULT, new Margins()));
    }
    return statusLayoutContainer;
  }

  public Text getStatusText()
  {
    if (statusText == null)
    {
      statusText = new Text("Status:");
    }
    return statusText;
  }

  private TaskCompleteWindow getTaskCompleteWindow()
  {
    if (taskCompleteWindow == null)
    {
      taskCompleteWindow = new TaskCompleteWindow();
    }
    return taskCompleteWindow;
  }

  public LayoutContainer getTaskLayoutContainer()
  {
    if (taskLayoutContainer == null)
    {
      taskLayoutContainer = new LayoutContainer();
      taskLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      taskLayoutContainer.add(getTaskText());
      taskLayoutContainer.add(getTaskTextField(), new RowData(1.0, Style.DEFAULT, new Margins()));
    }
    return taskLayoutContainer;
  }

  public Text getTaskText()
  {
    if (taskText == null)
    {
      taskText = new Text("Task:");
    }
    return taskText;
  }

  public SafeTextField<String> getTaskTextField()
  {
    if (taskTextField == null)
    {
      taskTextField = new SafeTextField<String>();
    }
    return taskTextField;
  }

  @Override
  public void okay()
  {
    String task = getTaskTextField().getValue();
    if (task == null)
    {
      MessageBox.alert("Missing Field", "Please enter a task.", null);
      setFocusWidget(getTaskTextField());
      return;
    }

    Widget invalidWidget = getAssignmentDateNamePanel().getInvalidWidget();
    if (invalidWidget != null)
    {
      setFocusWidget(invalidWidget);
      return;
    }

    Date dueDate = getAssignmentDateNamePanel().getDateField().getValue();
    String assignedToName = getAssignmentDateNamePanel().getName();
    int assignedToType = getAssignmentDateNamePanel().getType();

    Integer access = getAccessComboBox().getSimpleValue();
    Integer order = getOrderComboBox().getSimpleValue();
    Integer status = getStatusComboBox().getSimpleValue();

    String remarks = getRemarksTextField().getValue();

    association.set(Keys.WORKFLOW_TASK, task);
    association.set(Keys.WORKFLOW_DUE_DATE, dueDate);
    association.set(Keys.WORKFLOW_ASSIGNED_TO_NAME, assignedToName);
    association.set(Keys.WORKFLOW_ASSIGNED_TO_TYPE, assignedToType);
    association.set(Keys.WORKFLOW_ACCESS, access);
    association.set(Keys.WORKFLOW_ORDER, order);
    association.set(Keys.WORKFLOW_STATUS, status);
    association.set(Keys.WORKFLOW_REMARKS, remarks);

    if (isNew)
    {
      isNew = false; // prevent auto-delete on next cleanup
    }

    hide();
  }

}
