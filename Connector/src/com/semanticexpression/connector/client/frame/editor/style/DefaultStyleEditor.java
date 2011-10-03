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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.shared.Keys;

public final class DefaultStyleEditor extends Framelet
{
  private ContentReference contentReference;
  private DefaultStyleEditorWindow defaultStyleEditorWindow;
  private Menu defaultStyleMenuContext;
  private Menu defaultStyleMenuFramelet;
  private SafeTextArea defaultStyleSafeTextArea;

  public DefaultStyleEditor()
  {
    super("Default Styles", Resources.STYLE_OMEGA);
    setFrameletMenu(getDefaultStyleMenuFramelet());
    setBodyBorder(false);
    add(getDefaultStyleSafeTextArea());
  }

  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;

    String defaultStyle = contentReference.get(Keys.STYLE_DEFAULT);
    getDefaultStyleSafeTextArea().setValue(defaultStyle);
    setReadOnly(contentReference.isReadOnly());
  }

  private DefaultStyleEditorWindow getDefaultStyleEditorWindow()
  {
    if (defaultStyleEditorWindow == null)
    {
      defaultStyleEditorWindow = new DefaultStyleEditorWindow(contentReference);
    }
    return defaultStyleEditorWindow;
  }

  private Menu getDefaultStyleMenuContext()
  {
    if (defaultStyleMenuContext == null)
    {
      defaultStyleMenuContext = new DefaultStyleMenu();
    }
    return defaultStyleMenuContext;
  }

  private Menu getDefaultStyleMenuFramelet()
  {
    if (defaultStyleMenuFramelet == null)
    {
      defaultStyleMenuFramelet = new DefaultStyleMenu();
    }
    return defaultStyleMenuFramelet;
  }

  public SafeTextArea getDefaultStyleSafeTextArea()
  {
    if (defaultStyleSafeTextArea == null)
    {
      defaultStyleSafeTextArea = new SafeTextArea();
      defaultStyleSafeTextArea.setEmptyText("Select Edit from context menu to define default CSS style rule(s)");
      defaultStyleSafeTextArea.setReadOnly(true);
      defaultStyleSafeTextArea.setContextMenu(getDefaultStyleMenuContext());
    }
    return defaultStyleSafeTextArea;
  }

  public class DefaultStyleMenu extends Menu
  {
    private MenuItem defaultStyleMenuItem;

    public DefaultStyleMenu()
    {
      add(getDefaultStyleMenuItem());
    }

    private MenuItem getDefaultStyleMenuItem()
    {
      if (defaultStyleMenuItem == null)
      {
        defaultStyleMenuItem = new DefaultStyleMenuItem();
      }
      return defaultStyleMenuItem;
    }

    public class DefaultStyleMenuItem extends MenuItem
    {
      public DefaultStyleMenuItem()
      {
        super("Edit Default Style");
        setIcon(Resources.STYLE_OMEGA);
        addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            getDefaultStyleEditorWindow().show();
            getDefaultStyleEditorWindow().toFront();
            getDefaultStyleEditorWindow().alignTo(EditorFrame.getEditorFrame(DefaultStyleEditor.this).getElement(), "c-c?", null);
            getDefaultStyleEditorWindow().display(contentReference, getDefaultStyleSafeTextArea());
          }
        });
      }
    }

  }

}
