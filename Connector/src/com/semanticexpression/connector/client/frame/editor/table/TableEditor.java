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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.semanticexpression.connector.client.frame.editor.CaptionBaseEditor;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;

public final class TableEditor extends CaptionBaseEditor
{
  private ModificationContext modificationContext;
  private TableFramelet tableFramelet;

  public TableEditor(ModificationContext modificationContext)
  {
    this.modificationContext = modificationContext;
    add(getTableFramelet(), new BorderLayoutData(LayoutRegion.CENTER));
  }

  @Override
  public void display(ContentReference contentReference)
  {
    super.display(contentReference);
    getTableFramelet().display(contentReference);
  }

  public TableFramelet getTableFramelet()
  {
    if (tableFramelet == null)
    {
      tableFramelet = new TableFramelet(modificationContext);
    }
    return tableFramelet;
  }
  @Override
  public void saveChanges()
  {
    getTableFramelet().saveChanges();
  }

}
