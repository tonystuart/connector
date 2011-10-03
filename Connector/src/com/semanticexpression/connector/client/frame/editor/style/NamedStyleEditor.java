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

package com.semanticexpression.connector.client.frame.editor.style;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.GridEditor;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ListStoreEditorWindow;
import com.semanticexpression.connector.shared.Keys;

public class NamedStyleEditor extends GridEditor
{
  public NamedStyleEditor(ModificationContext modificationContext)
  {
    super("Named Styles", Resources.STYLE, Resources.STYLE_ADD, Resources.STYLE_EDIT, Resources.STYLE_DELETE, Keys.STYLES, modificationContext);
  }

  @Override
  public void display(ContentReference contentReference)
  {
    super.display(contentReference);
    setDescription(contentReference.getId().formatString());
  }

  @Override
  public ColumnModel getColumnModel()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(new ColumnConfig(Keys.NAME, "Name", 100));
      columnConfigs.add(new ColumnConfig(Keys.STYLE_ELEMENT_NAME, "Element", 100));
      columnConfigs.add(new ColumnConfig(Keys.STYLE_SELECTOR, "Selector", 100));
      columnConfigs.add(Utility.createColumnConfigYesNo(Keys.STYLE_IS_COMMENT_ENABLED, "Comment Style", 100));
      columnConfigs.add(Utility.createColumnConfigStyleExample(Keys.VALUE, "Example", 100));
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  @Override
  public ListStoreEditorWindow getEditorWindow()
  {
    if (editorWindow == null)
    {
      editorWindow = new NamedStyleEditorWindow(getEditorStore());
    }
    return editorWindow;
  }

}
