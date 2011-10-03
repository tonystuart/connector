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

package com.semanticexpression.connector.client.frame.editor.table;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.semanticexpression.connector.client.Utility;

public class ColumnTypeModelData
{
  private static LinkedList<ModelData> columnTypeList;

  public static List<ModelData> getColumnTypeList()
  {
    if (columnTypeList == null)
    {
      columnTypeList = new LinkedList<ModelData>();
      columnTypeList.add(Utility.createModel("Date", ColumnType.DATE));
      columnTypeList.add(Utility.createModel("Decimal", ColumnType.DECIMAL));
      columnTypeList.add(Utility.createModel("Integer", ColumnType.INTEGER));
      columnTypeList.add(Utility.createModel("Text", ColumnType.TEXT));
      columnTypeList.add(Utility.createModel("Yes/No", ColumnType.YESNO));
    }
    return columnTypeList;
  }

}
