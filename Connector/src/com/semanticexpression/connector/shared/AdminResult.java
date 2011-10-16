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

package com.semanticexpression.connector.shared;

import java.io.Serializable;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

public class AdminResult implements Serializable
{
  private static final long serialVersionUID = 1L;

  private List<String> columnNames;
  private List<ModelData> rows;

  public AdminResult()
  {
  }

  public AdminResult(List<String> columnNames, List<ModelData> rows)
  {
    this.columnNames = columnNames;
    this.rows = rows;
  }

  public List<String> getColumnNames()
  {
    return columnNames;
  }

  public List<ModelData> getRows()
  {
    return rows;
  }
}
