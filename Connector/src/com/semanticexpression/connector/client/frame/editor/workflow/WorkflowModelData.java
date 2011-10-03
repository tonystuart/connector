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

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.widget.ListRenderer;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.WorkflowConstants;

public class WorkflowModelData
{
  private static ColumnConfig accessColumnConfig;
  private static List<ModelData> accessList;
  private static ColumnConfig orderColumnConfig;
  private static List<ModelData> orderList;
  private static ColumnConfig statusColumnConfig;
  private static List<ModelData> statusListLong;
  private static List<ModelData> statusListShort;
  private static List<ModelData> typeList;

  public static ColumnConfig getAccessColumnConfig()
  {
    if (accessColumnConfig == null)
    {
      accessColumnConfig = new ColumnConfig(Keys.WORKFLOW_ACCESS, "Access", 100);
      accessColumnConfig.setRenderer(new ListRenderer(getAccessList()));
    }
    return accessColumnConfig;
  }

  public static List<ModelData> getAccessList()
  {
    if (accessList == null)
    {
      accessList = new LinkedList<ModelData>();
      accessList.add(Utility.createModel("Read-Only", WorkflowConstants.ACCESS_1_READ_ONLY));
      accessList.add(Utility.createModel("Read/Write", WorkflowConstants.ACCESS_2_READ_WRITE));
    }
    return accessList;
  }

  public static ColumnConfig getOrderColumnConfig()
  {
    if (orderColumnConfig == null)
    {
      orderColumnConfig = new ColumnConfig(Keys.WORKFLOW_ORDER, "Order", 100);
      orderColumnConfig.setRenderer(new ListRenderer(getOrderList()));
    }
    return orderColumnConfig;
  }

  public static List<ModelData> getOrderList()
  {
    if (orderList == null)
    {
      orderList = new LinkedList<ModelData>();
      orderList.add(Utility.createModel("Sequential", WorkflowConstants.ORDER_1_SEQUENTIAL));
      orderList.add(Utility.createModel("Parallel", WorkflowConstants.ORDER_2_PARALLEL));
    }
    return orderList;
  }

  public static ColumnConfig getStatusColumnConfig()
  {
    if (statusColumnConfig == null)
    {
      statusColumnConfig = new ColumnConfig(Keys.WORKFLOW_STATUS, "Status", 100);
      statusColumnConfig.setRenderer(new ListRenderer(getStatusListLong()));
    }
    return statusColumnConfig;
  }

  public static List<ModelData> getStatusListLong()
  {
    if (statusListLong == null)
    {
      statusListLong = new LinkedList<ModelData>();
      statusListLong.add(Utility.createModel("Pending", WorkflowConstants.STATUS_1_PENDING));
      statusListLong.add(Utility.createModel("Ready", WorkflowConstants.STATUS_2_READY));
      statusListLong.add(Utility.createModel("In Progress", WorkflowConstants.STATUS_3_IN_PROGRESS));
      statusListLong.add(Utility.createModel("Completed", WorkflowConstants.STATUS_4_COMPLETED));
      statusListLong.add(Utility.createModel("Rejected", WorkflowConstants.STATUS_5_REJECTED));
    }
    return statusListLong;
  }

  public static List<ModelData> getStatusListShort()
  {
    if (statusListShort == null)
    {
      statusListShort = new LinkedList<ModelData>();
      statusListShort.add(Utility.createModel("In Progress", WorkflowConstants.STATUS_3_IN_PROGRESS));
      statusListShort.add(Utility.createModel("Completed", WorkflowConstants.STATUS_4_COMPLETED));
      statusListShort.add(Utility.createModel("Rejected", WorkflowConstants.STATUS_5_REJECTED));
    }
    return statusListShort;
  }

  public static List<ModelData> getTypeList()
  {
    if (typeList == null)
    {
      typeList = new LinkedList<ModelData>();
      typeList.add(Utility.createModel("User", WorkflowConstants.TYPE_1_USER));
      typeList.add(Utility.createModel("Group", WorkflowConstants.TYPE_2_GROUP));
    }
    return typeList;
  }
}