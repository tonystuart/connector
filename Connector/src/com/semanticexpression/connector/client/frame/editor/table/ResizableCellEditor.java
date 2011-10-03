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

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EditorEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.google.gwt.user.client.Element;
import com.semanticexpression.connector.client.widget.SafeTextArea;

public class ResizableCellEditor extends CellEditor
{
  private El editorBoundEl;
  private String editorInnerHtmlStartValue;

  public ResizableCellEditor(Field<? extends Object> field)
  {
    super(field);

    addListener(Events.BeforeStartEdit, new Listener<EditorEvent>()
    {
      @Override
      public void handleEvent(EditorEvent editorEvent)
      {
        editorBoundEl = editorEvent.getBoundEl();
        editorInnerHtmlStartValue = editorBoundEl.getInnerHtml(); // for managing display only, not underlying content
      }
    });
    addListener(Events.BeforeComplete, new Listener<EditorEvent>()
    {
      @Override
      public void handleEvent(EditorEvent editorEvent)
      {
        editorBoundEl.setInnerHtml(editorInnerHtmlStartValue); // handles tab w/o change on empty field
      }
    });
    addListener(Events.CancelEdit, new Listener<EditorEvent>()
    {
      @Override
      public void handleEvent(EditorEvent editorEvent)
      {
        editorBoundEl.setInnerHtml(editorInnerHtmlStartValue);
      }
    });

    if (field instanceof SafeTextArea)
    {
      final SafeTextArea textArea = (SafeTextArea)field;
      textArea.setPreventScrollbars(true);
      textArea.addKeyListener(new KeyListener()
      {
        public void componentKeyUp(ComponentEvent event)
        {
          String value = textArea.getValue();
          resizeToValue(value);
        }
      });
    }

  }

  @Override
  public void startEdit(Element el, Object value)
  {
    Field<?> field = getField();
    if (field instanceof ConstrainedField)
    {
      ConstrainedField constrainedField = (ConstrainedField)field;
      value = constrainedField.normalizeValue(value);
    }
    super.startEdit(el, value);
  }

  public void resizeToValue(String value)
  {
    editorBoundEl.setInnerHtml(value);
    doAutoSize();
  }

}