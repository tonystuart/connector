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

package com.semanticexpression.connector.client.frame.editor.comment;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.DomIterable;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.GridEditor;
import com.semanticexpression.connector.client.frame.editor.text.CommandProcessor;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ListStoreEditorWindow;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;

public final class CommentEditor extends GridEditor
{
  private static final String BASIC_INFO = "To create a comment, select text in the Text panel and a comment style in the Style panel, then click on the Style menu and select Apply.<br/><br/>To add a reply to an existing comment, select a comment in the Comment panel, then click on the Comment menu and select Add.";

  private CommandProcessor commandProcessor;
  private RowExpander rowExpander;

  public CommentEditor(CommandProcessor commandProcessor, ModificationContext modificationContext)
  {
    super("Comment", Resources.COMMENT, Resources.COMMENT_ADD, Resources.COMMENT_EDIT, Resources.COMMENT_REMOVE, Keys.COMMENTS, modificationContext);

    this.commandProcessor = commandProcessor;

    getGrid().addPlugin(getRowExpander());
    getGrid().addListener(Events.RowClick, new RowClickListener());
    addCommentItems(getFrameletMenu());
    addCommentItems(getGridContextMenu());
    getGrid().getView().setEmptyText(BASIC_INFO);
  }

  private void addCommentItems(Menu menu)
  {
    menu.add(new SeparatorMenuItem());
    menu.add(createExpandAllMenuItem());
    menu.add(createCollapseAllMenuItem());
  }

  @Override
  protected void addItem()
  {
    Association selectedItem = getGrid().getSelectionModel().getSelectedItem();
    if (selectedItem == null)
    {
      MessageBox.info("Nothing Selected", BASIC_INFO, null);
    }
    else
    {
      String title = selectedItem.get(Keys.NAME);
      String styleElementId = selectedItem.get(Keys.STYLE_ELEMENT_ID);
      Association association = new Association(IdManager.createIdentifier());
      association.setTrackChanges(true);
      association.set(Keys.NAME, title);
      association.set(Keys.STYLE_ELEMENT_ID, styleElementId);
      addItem(association);
    }
  }

  @Override
  protected void addItem(Association association)
  {
    super.addItem(association);
    synchronizeCommentsAndText();
  }

  public void addItem(String styleElementId)
  {
    Association association = new Association(IdManager.createIdentifier());
    association.setTrackChanges(true);
    association.set(Keys.STYLE_ELEMENT_ID, styleElementId);
    addItem(association);
  }

  protected void collapseAllComments()
  {
    int rowCount = getEditorStore().getCount();
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      getRowExpander().collapseRow(rowOffset);
    }
  }

  private MenuItem createCollapseAllMenuItem()
  {
    MenuItem collapseAllMenuItem = new MenuItem("Collapse All");
    collapseAllMenuItem.setIcon(Resources.COLLAPSE_ALL);
    collapseAllMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
    {
      @Override
      public void componentSelected(MenuEvent ce)
      {
        collapseAllComments();
      }
    });
    return collapseAllMenuItem;
  }

  private MenuItem createExpandAllMenuItem()
  {
    MenuItem expandAllMenuItem = new MenuItem("Expand All");
    expandAllMenuItem.setIcon(Resources.EXPAND_ALL);
    expandAllMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
    {
      @Override
      public void componentSelected(MenuEvent ce)
      {
        expandAllComments();
      }
    });
    return expandAllMenuItem;
  }

  @Override
  protected void deleteItem(Association association)
  {
    if (isCreator(association))
    {
      super.deleteItem(association);
    }
    else
    {
      MessageBox.alert("Not Owner", "You can only delete comments that you created.", null);
    }
  }

  @Override
  protected void deleteSelectedItems()
  {
    super.deleteSelectedItems();
    synchronizeCommentsAndText();
  }

  public void deselectAll()
  {
    getGrid().getSelectionModel().deselectAll();
  }

  @Override
  public void display(ContentReference contentReference)
  {
    super.display(contentReference);
    synchronizeCommentsAndText();
  }

  @Override
  protected void editItem(Association association)
  {
    if (isCreator(association))
    {
      super.editItem(association);
    }
    else
    {
      MessageBox.alert("Not Owner", "You can only edit comments that you created.", null);
    }
  }

  protected void expandAllComments()
  {
    int rowCount = getEditorStore().getCount();
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      getRowExpander().expandRow(rowOffset);
    }
  }

  @Override
  protected ColumnModel getColumnModel()
  {
    if (columnModel == null)
    {
      List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
      columnConfigs.add(getRowExpander());
      columnConfigs.add(new ColumnConfig(Keys.COMMENT_SEQUENCE_NUMBER, "Number", 100));
      columnConfigs.add(new ColumnConfig(Keys.NAME, "Title", 100));
      columnConfigs.add(new ColumnConfig(Keys.CREATED_BY, "User", 100));
      columnConfigs.add(Utility.createDateTimeColumnConfig(Keys.CREATED_AT, "Date", 100));
      columnModel = new ColumnModel(columnConfigs);
    }
    return columnModel;
  }

  @Override
  public ListStoreEditorWindow getEditorWindow()
  {
    if (editorWindow == null)
    {
      editorWindow = new CommentEditorWindow(getEditorStore());
    }
    return editorWindow;
  }

  public RowExpander getRowExpander()
  {
    if (rowExpander == null)
    {
      rowExpander = new RowExpander();
      XTemplate template = XTemplate.create("<div style='margin: 5px;'>{" + Keys.VALUE + "}</div>");
      rowExpander.setTemplate(template);
    }
    return rowExpander;
  }

  private boolean isCreator(Association association)
  {
    String userName = Utility.getUserName();
    String creator = association.get(Keys.CREATED_BY);
    return Utility.equalsWithNull(creator, Credential.GUEST_USER) || Utility.equalsWithNull(creator, userName);
  }

  public void selectCommentByStyleElementId(String styleElementId)
  {
    List<Association> comments = getEditorStore().getModels();
    for (Association comment : comments)
    {
      if (styleElementId.equals(comment.get(Keys.STYLE_ELEMENT_ID)))
      {
        getGrid().getSelectionModel().select(comment, true);
        int rowOffset = getEditorStore().indexOf(comment);
        getGrid().getView().ensureVisible(rowOffset, 0, false);
      }
    }
  }

  protected void setCommentSequenceNumber(Association comment, int sequenceNumber)
  {
    getEditorStore().setSuppressModifyEvent(true);
    comment.setTransient(Keys.COMMENT_SEQUENCE_NUMBER, sequenceNumber);
    getEditorStore().setSuppressModifyEvent(false);
  }

  public void synchronizeCommentsAndText()
  {

    SortDir sortDir = getEditorStore().getSortDir();
    String sortField = getEditorStore().getSortField();
    List<Association> comments = getEditorStore().getModels();

    for (Association comment : comments)
    {
      setCommentSequenceNumber(comment, -1);
    }

    int sequenceNumber = 1;
    List<Element> unreferencedCommentStyleElements = new LinkedList<Element>();
    DomIterable domIterable = commandProcessor.getDocumentDomIterable(true);

    for (Node node : domIterable)
    {
      if (node instanceof Element)
      {
        Element element = (Element)node;
        String id = element.getId();
        if (id != null && id.length() > 0)
        {
          boolean found = false;
          for (Association comment : comments)
          {
            if (id.equals(comment.get(Keys.STYLE_ELEMENT_ID)))
            {
              setCommentSequenceNumber(comment, sequenceNumber++);
              found = true;
            }
          }
          if (!found)
          {
            String styleClassName = element.getClassName();
            Association style = commandProcessor.findStyleByName(styleClassName);
            if (style != null && !style.get(Keys.STYLE_IS_UNDEFINED, false) && style.get(Keys.STYLE_IS_COMMENT_ENABLED, false))
            {
              unreferencedCommentStyleElements.add(element);
            }
          }
        }
      }
    }

    for (Association comment : comments)
    {
      sequenceNumber = comment.get(Keys.COMMENT_SEQUENCE_NUMBER);
      if (sequenceNumber == -1)
      {
        getEditorStore().remove(comment);
      }
    }

    for (Element element : unreferencedCommentStyleElements)
    {
      commandProcessor.removeElementAndReparentChildren(element);
    }

    if (sortDir != SortDir.NONE)
    {
      getEditorStore().sort(sortField, sortDir);
    }
  }

  private final class RowClickListener implements Listener<GridEvent<Association>>
  {
    @Override
    public void handleEvent(GridEvent<Association> gridEvent)
    {
      int rowIndex = gridEvent.getRowIndex();
      if (rowIndex != -1)
      {
        Association currentComment = getEditorStore().getAt(rowIndex);
        String styleElementId = currentComment.get(Keys.STYLE_ELEMENT_ID);
        if (styleElementId != null)
        {
          getGrid().getSelectionModel().select(currentComment, false);
          commandProcessor.select(styleElementId);
        }
      }
    }
  }

}
