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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.frame.editor.DetailsPanelComponent;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.Keys;

public class StyleEditor extends LayoutContainer implements DetailsPanelComponent
{
  private BorderLayout borderLayout;
  private DefaultStyleEditor defaultStyleEditor;
  private ModificationContext modificationContext;
  private NamedStyleEditor namedStyleEditor;

  public StyleEditor(ModificationContext modificationContext)
  {
    this.modificationContext = modificationContext;
    
    setLayout(getBorderLayout());
    add(getNamedStyleEditor(), getNamedStyleEditorLayoutData());
    add(getDefaultStyleEditor(), getDefaultStyleEditorLayoutData());
    getNamedStyleEditor().getFrameletMenu().add(createDefaultStyleMenuItem());
    getNamedStyleEditor().getGridContextMenu().add(createDefaultStyleMenuItem());
  }

  protected MenuItem createDefaultStyleMenuItem()
  {
    MenuItem defaultStyleMenuItem = new MenuItem("Default Style");
    defaultStyleMenuItem.addSelectionListener(new DefaultStyleListener());
    defaultStyleMenuItem.setIcon(Resources.STYLE_OMEGA);
    return defaultStyleMenuItem;
  }

  @Override
  public void display(ContentReference contentReference)
  {
    String defaultStyle = contentReference.get(Keys.STYLE_DEFAULT);
    if (defaultStyle == null || defaultStyle.length() == 0)
    {
      getBorderLayout().hide(LayoutRegion.SOUTH);
    }
    else
    {
      getBorderLayout().show(LayoutRegion.SOUTH);
    }

    getNamedStyleEditor().display(contentReference);
    getDefaultStyleEditor().display(contentReference);
  }

  protected BorderLayout getBorderLayout()
  {
    if (borderLayout == null)
    {
      borderLayout = new BorderLayout();
    }
    return borderLayout;
  }

  protected BorderLayoutData getNamedStyleEditorLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
    layoutData.setMargins(new Margins(0, 0, 5, 0));
    return layoutData;
  }

  private BorderLayoutData getDefaultStyleEditorLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.5f, 0, 10000);
    layoutData.setSplit(true);
    layoutData.setFloatable(false);
    layoutData.setHidden(false); // workaround Ext-GWT problem: initial hide doesn't work if style is initial outline selection because show only works if rendered
    return layoutData;
  }

  public DefaultStyleEditor getDefaultStyleEditor()
  {
    if (defaultStyleEditor == null)
    {
      defaultStyleEditor = new DefaultStyleEditor();
    }
    return defaultStyleEditor;
  }

  public NamedStyleEditor getNamedStyleEditor()
  {
    if (namedStyleEditor == null)
    {
      namedStyleEditor = new NamedStyleEditor(modificationContext);
    }
    return namedStyleEditor;
  }

  @Override
  public Component getComponent()
  {
    return this;
  }

  @Override
  public void saveChanges()
  {
  }

  private final class DefaultStyleListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      if (getDefaultStyleEditor().isVisible())
      {
        getBorderLayout().hide(LayoutRegion.SOUTH);
      }
      else
      {
        getBorderLayout().show(LayoutRegion.SOUTH);
      }
    }
  }

}
