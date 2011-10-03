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

import java.util.Date;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.semanticexpression.connector.client.events.TextEditorUpdateEvent;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.chart.ChartEditor;
import com.semanticexpression.connector.client.frame.editor.document.DocumentEditor;
import com.semanticexpression.connector.client.frame.editor.image.ImageEditor;
import com.semanticexpression.connector.client.frame.editor.style.StyleEditor;
import com.semanticexpression.connector.client.frame.editor.table.TableEditor;
import com.semanticexpression.connector.client.frame.editor.text.TextEditor;
import com.semanticexpression.connector.client.frame.editor.workflow.WorkflowEditor;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.enums.ContentType;

public final class ContentDetails extends ContentPanel
{
  private ChartEditor chartEditor;
  private DocumentEditor documentEditor;
  private EditorFrame editorFrame;
  private ImageEditor imageEditor;
  private ModificationContext modificationContext;
  private NullSelectionEditor nullSelectionEditor;
  private StyleEditor styleEditor;
  private TableEditor tableEditor;
  private TextEditor textEditor;
  private WorkflowEditor workflowEditor;

  public ContentDetails(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    this.editorFrame = editorFrame;
    this.modificationContext = modificationContext;

    setHeading("Content");
    setIcon(Resources.CONTENT);
    setHeaderVisible(false);
    setBodyBorder(false);
    setLayout(new FitLayout());
  }

  public void display(ContentReference contentReference)
  {
    DetailsPanelComponent currentEditor = (DetailsPanelComponent)getWidget(0);
    DetailsPanelComponent requiredEditor = getRequiredEditor(contentReference);

    if (requiredEditor != currentEditor)
    {
      removeAll(); // also handles null currentEditor on initial display
      add(requiredEditor.getComponent());
      layout();
      currentEditor = requiredEditor;
    }

    currentEditor.display(contentReference);
  }

  public ChartEditor getChartEditor()
  {
    if (chartEditor == null)
    {
      chartEditor = new ChartEditor(editorFrame, modificationContext);
    }
    return chartEditor;
  }

  public DocumentEditor getDocumentEditor()
  {
    if (documentEditor == null)
    {
      documentEditor = new DocumentEditor(editorFrame);
    }
    return documentEditor;
  }

  public ImageEditor getImageEditor()
  {
    if (imageEditor == null)
    {
      imageEditor = new ImageEditor();
    }
    return imageEditor;
  }

  private NullSelectionEditor getNullSelectionEditor()
  {
    if (nullSelectionEditor == null)
    {
      nullSelectionEditor = new NullSelectionEditor();
    }
    return nullSelectionEditor;
  }

  private DetailsPanelComponent getRequiredEditor(ContentReference contentReference)
  {
    DetailsPanelComponent requiredEditor;

    if (contentReference == null)
    {
      requiredEditor = getNullSelectionEditor();
    }
    else
    {
      ContentType contentType = contentReference.get(Keys.CONTENT_TYPE);
      requiredEditor = getRequiredEditor(contentType);
    }

    return requiredEditor;
  }

  private DetailsPanelComponent getRequiredEditor(ContentType contentType)
  {
    DetailsPanelComponent requiredEditor = null;

    if (contentType != null)
    {
      switch (contentType)
      {
        case CHART:
          requiredEditor = getChartEditor();
          break;
        case DOCUMENT:
          requiredEditor = getDocumentEditor();
          break;
        case IMAGE:
          requiredEditor = getImageEditor();
          break;
        case STYLE:
          requiredEditor = getStyleEditor();
          break;
        case TABLE:
          requiredEditor = getTableEditor();
          break;
        case TEXT:
          requiredEditor = getTextEditor();
          break;
        case WORKFLOW:
          requiredEditor = getWorkflowEditor();
          break;
      }
    }

    if (requiredEditor == null)
    {
      requiredEditor = getNullSelectionEditor();
    }

    return requiredEditor;
  }

  public StyleEditor getStyleEditor()
  {
    if (styleEditor == null)
    {
      styleEditor = new StyleEditor(modificationContext);
    }
    return styleEditor;
  }

  public TableEditor getTableEditor()
  {
    if (tableEditor == null)
    {
      tableEditor = new TableEditor(modificationContext);
    }
    return tableEditor;
  }

  public TextEditor getTextEditor()
  {
    if (textEditor == null)
    {
      textEditor = new TextEditor(editorFrame, modificationContext);
    }
    return textEditor;
  }

  public WorkflowEditor getWorkflowEditor()
  {
    if (workflowEditor == null)
    {
      workflowEditor = new WorkflowEditor(editorFrame, modificationContext);
    }
    return workflowEditor;
  }

  public void onCommit()
  {
    DetailsPanelComponent detailsPanelComponent = (DetailsPanelComponent)getWidget(0);
    if (detailsPanelComponent instanceof HasOnCommit)
    {
      ((HasOnCommit)detailsPanelComponent).onCommit();
    }
  }

  public void onTextEditorUpdate(TextEditorUpdateEvent textEditorUpdateEvent)
  {
    if (textEditor != null)
    {
      textEditor.onTextEditorUpdate(textEditorUpdateEvent);
    }
  }

  public void saveChanges()
  {
    DetailsPanelComponent detailsPanelComponent = (DetailsPanelComponent)getWidget(0);
    if (detailsPanelComponent != null) // contentEditor is null on initial outlineTreePanel select
    {
      detailsPanelComponent.saveChanges();
    }
  }

  public void compareHistory(Date leftDate, Date rightDate)
  {
    DetailsPanelComponent detailsPanelComponent = (DetailsPanelComponent)getWidget(0);
    if (detailsPanelComponent instanceof HasCompareHistory)
    {
      ((HasCompareHistory)detailsPanelComponent).compareHistory(leftDate, rightDate);
    }
    else
    {
      MessageBox.alert("Compare", "Please select an item in the Outline panel that supports compare history and try again.", null);
      return;
    }
  }

}