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

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.access.AccessDetails;
import com.semanticexpression.connector.client.frame.editor.keyword.KeywordEditor;
import com.semanticexpression.connector.client.frame.editor.property.PropertyEditor;
import com.semanticexpression.connector.client.frame.editor.relationship.RelationshipDetails;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.Framelet;

public final class DetailsPanel extends LayoutContainer
{
  private AccessDetails accessDetails;
  private CardLayout cardLayout;
  private ContentReference contentReference;
  private Menu detailsMenu;
  private EditorFrame editorFrame;
  private KeywordEditor keywordEditor;
  private ModificationContext modificationContext;
  private PropertyEditor propertyEditor;
  private RelationshipDetails relationshipDetails;

  public DetailsPanel(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    this.editorFrame = editorFrame;
    this.modificationContext = modificationContext;

    setLayout(getCardLayout());
  }

  public void display(ContentReference contentReference)
  {
    this.contentReference = contentReference;
    if (isVisible())
    {
      displayContentReference();
    }
  }

  private void displayContentReference()
  {
    if (contentReference == null)
    {
      hide();
    }
    else
    {
      show();
      DetailsPanelComponent activeItem = (DetailsPanelComponent)getCardLayout().getActiveItem();
      activeItem.display(contentReference);
    }
  }

  public AccessDetails getAccessDetails()
  {
    if (accessDetails == null)
    {
      accessDetails = new AccessDetails(editorFrame, modificationContext);
      prepareFramelet(accessDetails);
      add(accessDetails);
    }
    return accessDetails;
  }

  private CardLayout getCardLayout()
  {
    if (cardLayout == null)
    {
      cardLayout = new CardLayout();
    }
    return cardLayout;
  }

  public Menu getDetailsMenu()
  {
    if (detailsMenu == null)
    {
      detailsMenu = new DetailsMenu();
    }
    return detailsMenu;
  }

  public KeywordEditor getKeywordEditor()
  {
    if (keywordEditor == null)
    {
      keywordEditor = new KeywordEditor(modificationContext);
      prepareFramelet(keywordEditor);
      add(keywordEditor);
    }
    return keywordEditor;
  }

  private PropertyEditor getPropertyEditor()
  {
    if (propertyEditor == null)
    {
      propertyEditor = new PropertyEditor(modificationContext);
      prepareFramelet(propertyEditor);
      add(propertyEditor);
    }
    return propertyEditor;
  }

  public RelationshipDetails getReferencesDetails()
  {
    if (relationshipDetails == null)
    {
      relationshipDetails = new RelationshipDetails(editorFrame, modificationContext);
      prepareFramelet(relationshipDetails);
      add(relationshipDetails);
    }
    return relationshipDetails;
  }

  private void onSetActiveItem(ContentPanel newActiveItem)
  {
    getCardLayout().setActiveItem(newActiveItem);
    displayContentReference();
  }

  private void prepareFramelet(Framelet framelet)
  {
    framelet.removeDescriptionText();
    framelet.getHeader().addTool(new ToolButton("x-tool-close", new SelectionListener<IconButtonEvent>()
    {
      public void componentSelected(IconButtonEvent ce)
      {
        hide();
      }
    }));
  }

  public void saveChanges()
  {
    int itemCount = getItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      Component item = getItem(itemOffset);
      if (item instanceof DetailsPanelComponent)
      {
        DetailsPanelComponent detailsPanelComponent = (DetailsPanelComponent)item;
        detailsPanelComponent.saveChanges();
      }
    }
  }

  private final class DetailsMenu extends Menu
  {
    private MenuItem accessDetailsMenuItem;
    private MenuItem keywordEditorMenuItem;
    private MenuItem propertyEditorMenuItem;
    private MenuItem referencesDetailsMenuItem;

    private DetailsMenu()
    {
      add(getReferencesDetailsMenuItem());
      add(getAccessDetailsMenuItem());
      add(getPropertyEditorMenuItem());
      add(getKeywordEditorMenuItem());
    }

    private MenuItem getAccessDetailsMenuItem()
    {
      if (accessDetailsMenuItem == null)
      {
        accessDetailsMenuItem = new MenuItem("Access");
        accessDetailsMenuItem.setIcon(Resources.ACCESS);
        accessDetailsMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            onSetActiveItem(getAccessDetails());
          }
        });
      }
      return accessDetailsMenuItem;
    }

    private MenuItem getKeywordEditorMenuItem()
    {
      if (keywordEditorMenuItem == null)
      {
        keywordEditorMenuItem = new MenuItem("Keywords");
        keywordEditorMenuItem.setIcon(Resources.KEYWORD);
        keywordEditorMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            onSetActiveItem(getKeywordEditor());
          }
        });
      }
      return keywordEditorMenuItem;
    }

    private MenuItem getPropertyEditorMenuItem()
    {
      if (propertyEditorMenuItem == null)
      {
        propertyEditorMenuItem = new MenuItem("Properties");
        propertyEditorMenuItem.setIcon(Resources.PROPERTY);
        propertyEditorMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            onSetActiveItem(getPropertyEditor());
          }
        });
      }
      return propertyEditorMenuItem;
    }

    private MenuItem getReferencesDetailsMenuItem()
    {
      if (referencesDetailsMenuItem == null)
      {
        referencesDetailsMenuItem = new MenuItem("Relationships");
        referencesDetailsMenuItem.setIcon(Resources.RELATIONSHIP);
        referencesDetailsMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          @Override
          public void componentSelected(MenuEvent ce)
          {
            onSetActiveItem(getReferencesDetails());
          }
        });
      }
      return referencesDetailsMenuItem;
    }
  }

}