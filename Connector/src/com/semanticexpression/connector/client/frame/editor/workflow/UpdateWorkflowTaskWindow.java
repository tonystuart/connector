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
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.events.WorkflowTaskUpdateEvent;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.ConnectorWindow;
import com.semanticexpression.connector.client.widget.DropDownComboBox;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.WorkflowConstants;

public final class UpdateWorkflowTaskWindow extends ConnectorWindow implements OkayCancelHandler
{
  private EditorFrame editorFrame;
  private OkayCancelToolBar okayCancelToolBar;
  private Text remarksText;
  private SafeTextArea remarksTextArea;
  private DropDownComboBox<ModelData> statusComboBox;
  private Text statusText;
  private Id workflowContentId;
  private Association workflowTask;

  public UpdateWorkflowTaskWindow(EditorFrame editorFrame)
  {
    this.editorFrame = editorFrame;

    setHeading("Update Task Status");
    setIcon(Resources.WORKFLOW_UPDATE);
    setSize(300, 200);
    setLayout(new RowLayout(Orientation.VERTICAL));
    add(getStatusText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(5, 5, 0, 5)));
    add(getStatusComboBox(), new RowData(1.0, Style.DEFAULT, new Margins(0, 5, 0, 5)));
    add(getRemarksText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(5, 5, 0, 5)));
    add(getRemarksTextArea(), new RowData(1.0, 1, new Margins(0, 5, 5, 5)));
    setBottomComponent(getOkayCancelToolBar());
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void display(Id contentId, Association workflowTask)
  {
    this.workflowContentId = contentId;
    this.workflowTask = workflowTask;

    String task = workflowTask.get(Keys.WORKFLOW_TASK);
    Integer status = workflowTask.get(Keys.WORKFLOW_STATUS);
    String remarks = workflowTask.get(Keys.WORKFLOW_REMARKS);

    setHeading(task);
    getStatusComboBox().setSimpleValue(status);
    getRemarksTextArea().setValue(remarks);
  }

  private OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

  public Text getRemarksText()
  {
    if (remarksText == null)
    {
      remarksText = new Text("Remarks:");
    }
    return remarksText;
  }

  public SafeTextArea getRemarksTextArea()
  {
    if (remarksTextArea == null)
    {
      remarksTextArea = new SafeTextArea();
      remarksTextArea.setEmptyText("Enter optional remarks pertaining to the status here.");
    }
    return remarksTextArea;
  }

  public DropDownComboBox<ModelData> getStatusComboBox()
  {
    if (statusComboBox == null)
    {
      statusComboBox = new DropDownComboBox<ModelData>();
      statusComboBox.add(WorkflowModelData.getStatusListShort());
    }
    return statusComboBox;
  }

  public Text getStatusText()
  {
    if (statusText == null)
    {
      statusText = new Text("Status:");
    }
    return statusText;
  }

  @Override
  public void okay()
  {
    Integer status = getStatusComboBox().getSimpleValue();
    String remarks = getRemarksTextArea().getValue();

    Association updatedWorkflowTask = new Association(workflowTask.getId(), false);
    updatedWorkflowTask.assignFrom(workflowTask);

    updatedWorkflowTask.set(Keys.WORKFLOW_STATUS, status);
    updatedWorkflowTask.set(Keys.WORKFLOW_REMARKS, remarks);

    switch (status)
    {
      case WorkflowConstants.STATUS_3_IN_PROGRESS:
      {
        String userName = Utility.getUserName();
        updatedWorkflowTask.set(Keys.WORKFLOW_ASSIGNED_TO_NAME, userName);
        updatedWorkflowTask.set(Keys.WORKFLOW_ASSIGNED_TO_TYPE, WorkflowConstants.TYPE_1_USER);
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETED_BY_NAME, null);
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETED_BY_TYPE, null);
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETION_DATE, null);
        break;
      }
      case WorkflowConstants.STATUS_4_COMPLETED:
      {
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETED_BY_NAME, Utility.getUserName());
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETED_BY_TYPE, WorkflowConstants.TYPE_1_USER);
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETION_DATE, new Date());
        break;
      }
      case WorkflowConstants.STATUS_5_REJECTED:
      {
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETED_BY_NAME, null);
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETED_BY_TYPE, null);
        updatedWorkflowTask.set(Keys.WORKFLOW_COMPLETION_DATE, null);
        break;
      }
    }

    Id monitorId = Directory.getStatusMonitor().getMonitorId();
    Directory.getConnectorService().updateWorkflowTask(Utility.getAuthenticationToken(), workflowContentId, updatedWorkflowTask, monitorId, new UpdateCallback(updatedWorkflowTask));
    hide();
  }

  private final class UpdateCallback extends FailureReportingAsyncCallback<Void>
  {
    private Association updatedWorkflowTask;

    public UpdateCallback(Association updatedWorkflowTask)
    {
      this.updatedWorkflowTask = updatedWorkflowTask;
    }

    @Override
    public void onSuccess(Void result)
    {
      boolean isFinished = false;
      int status = updatedWorkflowTask.get(Keys.WORKFLOW_STATUS);
      switch (status)
      {
        case WorkflowConstants.STATUS_3_IN_PROGRESS:
        {
          // We will receive ContentManagerUpdateEvent
          break;
        }
        case WorkflowConstants.STATUS_4_COMPLETED:
        case WorkflowConstants.STATUS_5_REJECTED:
        {
          // User may no longer have access to content
          isFinished = true;
          break;
        }
      }
      Directory.getEventBus().post(new WorkflowTaskUpdateEvent(editorFrame, isFinished));
    }
  }
}
