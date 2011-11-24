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

import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ConnectorWindow;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Keys;

public final class TaskCompleteWindow extends ConnectorWindow implements OkayCancelHandler
{
  private Association association;
  private DateNamePanel completionNameDatePanel;
  private OkayCancelToolBar okayCancelToolBar;

  public TaskCompleteWindow()
  {
    setHeading("Update Task Completion");
    setIcon(Resources.WORKFLOW_UPDATE);
    setSize(300, 300);
    setLayout(new FitLayout());
    add(getCompletionDateNamePanel(), new FitData(5));
    setBottomComponent(getOkayCancelToolBar());
  }

  @Override
  public void cancel()
  {
    hide();
  }

  public void edit(Association association)
  {
    this.association = association;

    String task = association.get(Keys.WORKFLOW_TASK);
    setHeading(task);

    Date completionDate = association.get(Keys.WORKFLOW_COMPLETION_DATE);
    String completedByName = association.get(Keys.WORKFLOW_COMPLETED_BY_NAME);
    Integer completedByType = association.get(Keys.WORKFLOW_COMPLETED_BY_TYPE);
    getCompletionDateNamePanel().display(completionDate, completedByName, completedByType);

    setFocusWidget(getCompletionDateNamePanel().getDateField());
  }

  private DateNamePanel getCompletionDateNamePanel()
  {
    if (completionNameDatePanel == null)
    {
      completionNameDatePanel = new DateNamePanel("Completion Date:", "Completed By:", "completionRadioName");
    }
    return completionNameDatePanel;
  }

  private OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(this);
    }
    return okayCancelToolBar;
  }

  @Override
  public void okay()
  {
    Date completionDate = getCompletionDateNamePanel().getDateField().getValue();
    String completedByName = getCompletionDateNamePanel().getName();
    int completedByType = getCompletionDateNamePanel().getType();

    association.set(Keys.WORKFLOW_COMPLETION_DATE, completionDate);
    association.set(Keys.WORKFLOW_COMPLETED_BY_NAME, completedByName);
    association.set(Keys.WORKFLOW_COMPLETED_BY_TYPE, completedByType);

    hide();
  }
}
