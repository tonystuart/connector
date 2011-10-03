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

package com.semanticexpression.connector.client.frame.editor.chart;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.semanticexpression.connector.client.frame.editor.CaptionBaseEditor;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;

public class ChartEditor extends CaptionBaseEditor
{
  private ChartFramelet chartFramelet;
  private EditorFrame editorFrame;
  private ModificationContext modificationContext;

  public ChartEditor(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    this.editorFrame = editorFrame;
    this.modificationContext = modificationContext;
    add(getChartFramelet(), new BorderLayoutData(LayoutRegion.CENTER));
  }

  @Override
  public void display(ContentReference contentReference)
  {
    super.display(contentReference);
    getChartFramelet().display(contentReference);
  }

  public ChartFramelet getChartFramelet()
  {
    if (chartFramelet == null)
    {
      chartFramelet = new ChartFramelet(editorFrame, modificationContext);
    }
    return chartFramelet;
  }

}
