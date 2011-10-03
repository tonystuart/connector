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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.semanticexpression.connector.client.ClientUrlBuilder;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.events.ContentManagerUpdateEvent;
import com.semanticexpression.connector.client.events.EditorFrameCommitEvent;
import com.semanticexpression.connector.client.events.EditorFrameModifyEvent;
import com.semanticexpression.connector.client.events.EditorFrameRollbackEvent;
import com.semanticexpression.connector.client.events.SaveAllEvent;
import com.semanticexpression.connector.client.events.TextEditorUpdateEvent;
import com.semanticexpression.connector.client.frame.editor.history.HistoryFramelet;
import com.semanticexpression.connector.client.frame.editor.outline.InsertLocationType;
import com.semanticexpression.connector.client.frame.editor.outline.OutlineFramelet;
import com.semanticexpression.connector.client.frame.editor.outline.OutlineTreeNode;
import com.semanticexpression.connector.client.frame.editor.outline.OutlineTreePanel;
import com.semanticexpression.connector.client.frame.editor.outline.OutlineTreeStore;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.rpc.LoginRetryAsyncCallback;
import com.semanticexpression.connector.client.rpc.LoginRetryAsyncCallback.LoginCallbackAdapter;
import com.semanticexpression.connector.client.widget.Frame;
import com.semanticexpression.connector.client.widget.FrameManager;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.client.wiring.StoreVisitor;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.UrlBuilder;
import com.semanticexpression.connector.shared.UrlConstants;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;

public final class EditorFrame extends Frame
{
  private static final String MODIFIED_HEADING = "*";

  public static Element getAlignToElement(Widget widget)
  {
    Widget bestWidget = EditorFrame.getEditorFrame(widget);
    if (bestWidget == null)
    {
      bestWidget = widget;
    }
    return bestWidget.getElement();
  }

  public static EditorFrame getEditorFrame(Widget widget)
  {
    while (widget != null && !(widget instanceof EditorFrame))
    {
      widget = widget.getParent();
    }
    return (EditorFrame)widget;
  }

  private AddContentButtonListener addContentButtonListener;
  private BorderLayout borderLayout;
  private Timer conflictTimer = new EditConflictTimer();
  private ContentDetails contentDetails;
  private ContentManagerUpdateEventListener contentManagerUpdateEventListener;
  private DetailsPanel detailsPanel;
  private Map<ContentReference, ContentManagerUpdateEvent> editConflicts;
  private EditorFrameCommitListener editorFrameCommitListener;
  private FileUploadWindow fileUploadWindow;
  private HistoryFramelet historyFramelet;
  private boolean isActive;
  private boolean isInitialAdd;
  private boolean isNew;
  private ModificationContext modificationContext;
  private OutlineFramelet outlineFramelet;
  private ToolButton outlineHideButton;
  private ContentPanel outlineHistoryContentPanel;
  private SaveAllListener saveAllListener;
  private SaveButton saveButton;
  private TextEditorUpdateListener textEditorUpdateListener;

  public EditorFrame(List<Content> contentList, boolean isNew)
  {
    this.isNew = isNew;

    setSize(800, 425);
    setLayout(getBorderLayout());
    setIcon(Resources.EDITOR);

    add(getOutlineHistoryContentPanel(), getOutlineHistoryLayoutData());
    add(getContentDetails(), getContentDetailsLayoutData());
    add(getDetailsPanel(), getDetailsPanelLayoutData());

    Directory.getEventBus().addListener(ContentManagerUpdateEvent.class, getContentManagerUpdateEventListener());
    Directory.getEventBus().addListener(EditorFrameCommitEvent.class, getEditorFrameCommitListener());
    Directory.getEventBus().addListener(SaveAllEvent.class, getSaveAllListener());
    Directory.getEventBus().addListener(TextEditorUpdateEvent.class, getTextEditorUpdateListener());

    addListener(FrameManager.Close, new CloseListener());
    addListener(FrameManager.Activate, new ActivateListener());

    addInitialContent(contentList);
    setFrameMenu(getDetailsPanel().getDetailsMenu());

    if (isNew)
    {
      modificationContext.onModify();
    }
  }

  public void addContent(ContentType desiredContentType, InsertLocationType insertLocationType)
  {
    ContentReference insertLocationReference = getOutlineTreePanel().getSelectionModel().getSelectedItem();
    int childCount = getOutlineTreeStore().getChildCount();
    if (childCount > 0)
    {
      if (insertLocationReference == null)
      {
        Utility.displaySelectionMessageBox();
        return;
      }

      ContentReference parent = getOutlineTreeStore().getParent(insertLocationReference);
      ContentType selectedContentType = insertLocationReference.get(Keys.CONTENT_TYPE);
      boolean hasDocumentRoot = getOutlineTreeStore().hasDocumentRoot();

      if (addContentLocationIsInvalid(desiredContentType, selectedContentType, insertLocationType, parent, hasDocumentRoot))
      {
        MessageBox.alert("Invalid Location", "You cannot insert the requested type of content at the selected location.", null);
        return;
      }
    }

    if (desiredContentType == ContentType.IMAGE)
    {
      String authenticationToken = Utility.getAuthenticationToken();
      if (authenticationToken == null)
      {
        showFileUploadWindowAfterLogin(insertLocationType, insertLocationReference);
      }
      else
      {
        showFileUploadWindow(insertLocationType, insertLocationReference);
      }
    }
    else
    {
      Content newContent = Directory.getContentManager().createContent(desiredContentType);
      insertContent(insertLocationReference, insertLocationType, newContent);
    }
  }

  private boolean addContentLocationIsInvalid(ContentType desiredContentType, ContentType selectedContentType, InsertLocationType insertLocationType, ContentReference parent, boolean hasDocumentRoot)
  {
    if (parent == null)
    {
      if (hasDocumentRoot)
      {
        if (insertLocationType == InsertLocationType.AFTER || insertLocationType == InsertLocationType.BEFORE)
        {
          return true;
        }
      }
    }

    if (selectedContentType != ContentType.DOCUMENT && insertLocationType == InsertLocationType.CHILD)
    {
      return true;
    }

    if (desiredContentType != ContentType.DOCUMENT && insertLocationType == InsertLocationType.PARENT)
    {
      return true;
    }

    return false;
  }

  private void addInitialContent(List<Content> contentList)
  {
    isInitialAdd = true;
    insertContentList(null, 0, contentList);
    isInitialAdd = false;
  }

  private void checkEditConflicts()
  {
    if (isActive && editConflicts != null)
    {
      processEditConflicts();
    }
  }

  private void cleanup()
  {
    hide();

    Directory.getEventBus().removeListener(ContentManagerUpdateEvent.class, getContentManagerUpdateEventListener());
    Directory.getEventBus().removeListener(EditorFrameCommitEvent.class, getEditorFrameCommitListener());
    Directory.getEventBus().removeListener(SaveAllEvent.class, getSaveAllListener());
    Directory.getEventBus().removeListener(TextEditorUpdateEvent.class, getTextEditorUpdateListener());

    getOutlineTreeStore().visit(new StoreVisitor<ContentReference>()
    {
      @Override
      public boolean visit(ContentReference contentReference)
      {
        Directory.getContentManager().unregister(contentReference);
        return true;
      }
    });
  }

  private void clearModifiedState()
  {
    getSaveButton().onSave();
    onCommitCleanup(getModificationContext().getModificationCount());
  }

  public void compareHistory(Date item1Date, Date item2Date)
  {
    getContentDetails().compareHistory(item1Date, item2Date);
  }

  private void display(ContentReference contentReference)
  {
    getContentDetails().display(contentReference);
    getDetailsPanel().display(contentReference);
  }

  public void displayCurrent(ContentReference contentReference)
  {
    getContentDetails().saveChanges(); // must occur before clearHistory() because editors cache contentReference
    contentReference.clearHistory();
    display(contentReference);
    getOutlineTreePanel().refresh(contentReference);
  }

  public void displayCurrentWithoutSavingChanges(ContentReference contentReference)
  {
    contentReference.clearHistory();
    display(contentReference);
    getOutlineTreePanel().refresh(contentReference);
  }

  public void displayHistory(ContentReference contentReference, Date historyDate, Content content)
  {
    getContentDetails().saveChanges(); // must occur before setHistory() because editors cache contentReference
    contentReference.setHistory(historyDate, content);
    display(contentReference);
    getOutlineTreePanel().refresh(contentReference);
  }

  private void displayOnCommit()
  {
    getHistoryFramelet().refresh();
    getContentDetails().onCommit();
  }

  public List<ContentReference> findContentReferences(Id contentId)
  {
    FindContentIdVisitor visitor = new FindContentIdVisitor(contentId);
    getOutlineTreeStore().visit(visitor);
    return visitor.getContentReferences();
  }

  public AddContentButtonListener getAddContentButtonListener()
  {
    if (addContentButtonListener == null)
    {
      addContentButtonListener = new AddContentButtonListener();
    }
    return addContentButtonListener;
  }

  public BorderLayout getBorderLayout()
  {
    if (borderLayout == null)
    {
      borderLayout = new BorderLayout();
    }
    return borderLayout;
  }

  public Content getContent(Date date)
  {
    Content content = getHistoryFramelet().getContent(date);
    return content;
  }

  public ContentDetails getContentDetails()
  {
    if (contentDetails == null)
    {
      contentDetails = new ContentDetails(this, getModificationContext());
    }
    return contentDetails;
  }

  private BorderLayoutData getContentDetailsLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
    layoutData.setMargins(new Margins(5, 5, 5, 5));
    return layoutData;
  }

  private ContentManagerUpdateEventListener getContentManagerUpdateEventListener()
  {
    if (contentManagerUpdateEventListener == null)
    {
      contentManagerUpdateEventListener = new ContentManagerUpdateEventListener();
    }
    return contentManagerUpdateEventListener;
  }

  private boolean getContext(ContentReference parent, ContentReference target, LinkedList<ContentReference> styles, ContentType desiredContentType)
  {
    int styleCount = styles.size();
    List<ContentReference> children = getOutlineTreeStore().getChildren(parent);
    for (ContentReference child : children)
    {
      if (child == target)
      {
        return true;
      }
      ContentType childContentType = child.get(Keys.CONTENT_TYPE);
      if (childContentType == desiredContentType)
      {
        styles.add(child);
      }
      else if (childContentType == ContentType.DOCUMENT)
      {
        boolean found = getContext(child, target, styles, desiredContentType);
        if (found)
        {
          return true;
        }
      }
    }
    while (styles.size() > styleCount)
    {
      styles.removeLast();
    }
    return false;
  }

  public List<ContentReference> getContext(ContentReference target, ContentType desiredContentType)
  {
    LinkedList<ContentReference> styles = new LinkedList<ContentReference>();
    ContentReference parent = getOutlineTreeStore().getChild(0);
    getContext(parent, target, styles, desiredContentType);
    return styles;
  }

  public DetailsPanel getDetailsPanel()
  {
    if (detailsPanel == null)
    {
      detailsPanel = new DetailsPanel(this, getModificationContext());
    }
    return detailsPanel;
  }

  private BorderLayoutData getDetailsPanelLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.25f, 0, 10000);
    layoutData.setMargins(new Margins(0, 5, 5, 5));
    layoutData.setSplit(true);
    layoutData.setFloatable(false);
    layoutData.setHidden(true);
    return layoutData;
  }

  private EditorFrameCommitListener getEditorFrameCommitListener()
  {
    if (editorFrameCommitListener == null)
    {
      editorFrameCommitListener = new EditorFrameCommitListener();
    }
    return editorFrameCommitListener;
  }

  private FileUploadWindow getFileUploadWindow()
  {
    if (fileUploadWindow == null)
    {
      fileUploadWindow = new FileUploadWindow();
    }
    return fileUploadWindow;
  }

  @Override
  public String getHeadingTitle()
  {
    String heading = getHeading();
    if (heading != null)
    {
      if (heading.startsWith(MODIFIED_HEADING))
      {
        heading = heading.substring(MODIFIED_HEADING.length());
      }
    }
    return heading;
  }

  public HistoryFramelet getHistoryFramelet()
  {
    if (historyFramelet == null)
    {
      historyFramelet = new HistoryFramelet(this);
    }
    return historyFramelet;
  }

  private BorderLayoutData getHistoryLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.SOUTH, 0.5f, 0, 10000);
    layoutData.setMargins(new Margins(5, 0, 0, 0));
    layoutData.setSplit(true);
    layoutData.setCollapsible(true);
    layoutData.setFloatable(false); // buggy
    return layoutData;
  }

  public ModificationContext getModificationContext()
  {
    if (modificationContext == null)
    {
      modificationContext = new ModificationContext();
    }
    return modificationContext;
  }

  public OutlineFramelet getOutlineFramelet()
  {
    if (outlineFramelet == null)
    {
      outlineFramelet = new OutlineFramelet(this);
      outlineFramelet.getHeader().addTool(getOutlineHideButton());
    }
    return outlineFramelet;
  }

  public ToolButton getOutlineHideButton()
  {
    if (outlineHideButton == null)
    {
      outlineHideButton = new ToolButton("x-tool-left");
      outlineHideButton.addListener(Events.Select, new Listener<BaseEvent>()
      {
        @Override
        public void handleEvent(BaseEvent be)
        {
          hideOutline();
        }
      });
    }
    return outlineHideButton;
  }

  private ContentPanel getOutlineHistoryContentPanel()
  {
    if (outlineHistoryContentPanel == null)
    {
      outlineHistoryContentPanel = new ContentPanel(); // provides expand support
      outlineHistoryContentPanel.setHeaderVisible(false);
      outlineHistoryContentPanel.setBodyBorder(false);
      outlineHistoryContentPanel.setLayout(new BorderLayout());
      outlineHistoryContentPanel.add(getOutlineFramelet(), getOutlineLayoutData());
      outlineHistoryContentPanel.add(getHistoryFramelet(), getHistoryLayoutData());
    }
    return outlineHistoryContentPanel;
  }

  private BorderLayoutData getOutlineHistoryLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.WEST, 0.25f, 0, 10000);
    layoutData.setMargins(new Margins(5, 0, 5, 5));
    layoutData.setFloatable(false);
    layoutData.setSplit(true);
    return layoutData;
  }

  private BorderLayoutData getOutlineLayoutData()
  {
    BorderLayoutData layoutData = new BorderLayoutData(LayoutRegion.CENTER);
    layoutData.setSplit(true);
    layoutData.setCollapsible(true);
    layoutData.setFloatable(false); // buggy
    return layoutData;
  }

  public OutlineTreePanel getOutlineTreePanel()
  {
    return getOutlineFramelet().getOutlineTreePanel();
  }

  public OutlineTreeStore getOutlineTreeStore()
  {
    return getOutlineFramelet().getOutlineTreeStore();
  }

  public ContentReference getRootContentReference()
  {
    ContentReference rootContentReference = null;

    int contentCount = getOutlineTreeStore().getChildCount();
    if (contentCount > 0)
    {
      rootContentReference = getOutlineTreeStore().getChild(0);
    }

    return rootContentReference;
  }

  private SaveAllListener getSaveAllListener()
  {
    if (saveAllListener == null)
    {
      saveAllListener = new SaveAllListener();
    }
    return saveAllListener;
  }

  public SaveButton getSaveButton()
  {
    if (saveButton == null)
    {
      saveButton = new SaveButton();
      saveButton.setToolTip("Save");
      saveButton.hide();
      saveButton.addSelectionListener(new SaveButtonSelectionListener());
    }
    return saveButton;
  }

  private TextEditorUpdateListener getTextEditorUpdateListener()
  {
    if (textEditorUpdateListener == null)
    {
      textEditorUpdateListener = new TextEditorUpdateListener();
    }
    return textEditorUpdateListener;
  }

  public boolean hasNext(Iterator<Content> contentIterator, Iterator<Id> partsIterator)
  {
    if (partsIterator == null)
    {
      return contentIterator.hasNext();
    }
    return partsIterator != null && contentIterator.hasNext();
  }

  public void hideOutline()
  {
    if (getOutlineHistoryContentPanel().isVisible())
    {
      getBorderLayout().collapse(LayoutRegion.WEST);
      // Nothing more to do... the OutlineHistoryContentPanel does the expand.
    }
  }

  @Override
  protected void initTools()
  {
    head.addTool(getSaveButton());

    super.initTools();
  }

  public void insertContent(ContentReference insertLocationReference, InsertLocationType insertLocationType, Content content)
  {
    ContentReference contentReference = null;
    ContentReference parent = getOutlineTreeStore().getParent(insertLocationReference);
    int index = getOutlineTreeStore().indexOf(insertLocationReference);

    switch (insertLocationType)
    {
      case AFTER:
        contentReference = insertContent(parent, index + 1, content);
        break;
      case BEFORE:
        contentReference = insertContent(parent, index, content);
        break;
      case CHILD:
        contentReference = insertContent(insertLocationReference, getOutlineTreeStore().getChildCount(insertLocationReference), content);
        break;
      case PARENT:
        contentReference = insertContentReparent(insertLocationReference, content, parent, index);
        break;
    }

    getOutlineTreePanel().getSelectionModel().select(contentReference, false);
  }

  private ContentReference insertContent(ContentReference parent, int index, Content content)
  {
    ContentReference contentReference = insertContentList(parent, index, Arrays.asList(content));
    return contentReference;
  }

  private ContentReference insertContent(ContentReference parentReference, int index, Map<Id, Content> contentIdMap, Id contentId)
  {
    Content content = contentIdMap.get(contentId); // replace with properties from first occurrence
    if (content == null)
    {
      throw new IllegalStateException("Content list does not contain item in parts list, contentId=" + contentId);
    }

    ContentReference contentReference = Directory.getContentManager().register(content); // harmlessly detects duplicates

    if (parentReference == null)
    {
      getOutlineTreeStore().insert(contentReference, index++, false);
      String title = contentReference.get(Keys.TITLE);
      setHeading(title);
    }
    else
    {
      getOutlineTreeStore().insert(parentReference, contentReference, index++, false);
    }

    List<Id> parts = contentReference.get(Keys.PARTS);
    if (parts != null)
    {
      int childIndex = 0;
      for (Id childContentId : parts)
      {
        insertContent(contentReference, childIndex++, contentIdMap, childContentId);
      }
    }

    return contentReference;
  }

  /**
   * The contentList contains either a single document and its parts, or a
   * single non-document content item. There is no way to retrieve anything else
   * because retrieve requires a single contentId. If in the future retrieve
   * were modified to retrieve more than one contentId, (e.g. a collection of
   * non document content) then this method would need to be modified to accept
   * a list of those requested root items and would then iterate through them,
   * invoking insertContent on each of their contentId's.
   */
  private ContentReference insertContentList(ContentReference parentReference, int index, List<Content> contentList)
  {
    HashMap<Id, Content> contentIdMap = new HashMap<Id, Content>();

    for (Content content : contentList)
    {
      Id contentId = content.getId();
      if (!contentIdMap.containsKey(contentId))
      {
        contentIdMap.put(contentId, content); // only first occurrence contains properties
      }
    }

    Content content = contentList.get(0);
    Id contentId = content.getId();
    ContentReference contentReference = insertContent(parentReference, index, contentIdMap, contentId);

    return contentReference;
  }

  private void insertContentReference(ContentReference parent, OutlineTreeNode subtree)
  {
    ContentReference contentReference = subtree.getContentReference();
    getOutlineTreeStore().add(parent, contentReference, false);
    for (OutlineTreeNode child : subtree.getChildren())
    {
      insertContentReference(contentReference, child);
    }
  }

  /**
   * Works around a limitation in TreeStore: re-parenting a root item doesn't
   * work because BaseTreeModel.adopt() doesn't have the information it needs to
   * remove the node from TreeStore.rootWrapper. Also note that the addChildren
   * flag only works for nodes that are an instance of TreeModel (i.e. it
   * doesn't use the store relationships).
   * 
   * @return
   */
  private ContentReference insertContentReparent(ContentReference insertLocationReference, Content content, ContentReference parent, int index)
  {
    OutlineTreeNode subtree = getOutlineTreeStore().getSubtree(insertLocationReference);

    if (parent == null)
    {
      getOutlineTreeStore().remove(insertLocationReference);
    }
    else
    {
      getOutlineTreeStore().remove(parent, insertLocationReference);
    }

    ContentReference contentReference = insertContent(parent, index, content);
    insertContentReference(contentReference, subtree);

    getOutlineTreePanel().setExpanded(contentReference, true, true);
    return contentReference;
  }

  private boolean isChanged(ContentReference contentReference)
  {
    if (contentReference.isChanged())
    {
      return true;
    }
    List<ContentReference> children = getOutlineTreeStore().getChildren(contentReference);
    for (ContentReference child : children)
    {
      if (isChanged(child))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isModified()
  {
    boolean isModified = false;
    getContentDetails().saveChanges();
    ContentReference rootContent = getRootContentReference();
    if (rootContent != null)
    {
      isModified = isChanged(rootContent);
    }
    return isModified;
  }

  private boolean isRootOnly(List<ContentReference> contentReferences)
  {
    for (ContentReference contentReference : contentReferences)
    {
      if (getOutlineTreeStore().getParent(contentReference) != null)
      {
        return false;
      }
    }
    return true;
  }

  public void onActivate(boolean isActive)
  {
    this.isActive = isActive;
    checkEditConflicts();
  }

  public void onClose(CloseEvent closeEvent)
  {
    if (isModified())
    {
      closeEvent.setDeferredPendingCallback(true);
      MessageBox.confirm("Unsaved Changes", "Close " + getHeadingTitle() + " without saving?", new CloseModifiedConfirmListener(closeEvent));
    }
    else
    {
      cleanup();
      Directory.getEventBus().post(new EditorFrameRollbackEvent(this));
    }
  }

  private void onCommit(int modificationCount)
  {
    Directory.getEventBus().post(new EditorFrameCommitEvent(this));
    onCommitCleanup(modificationCount);
  }

  private void onCommitCleanup(int modificationCount)
  {
    modificationContext.onCommit(modificationCount);
    refreshHeading();
    getSaveButton().onCommit();
    displayOnCommit();
  }

  private void onFirstModification()
  {
    refreshHeading();
    getSaveButton().onFirstModification();
    Directory.getEventBus().post(new EditorFrameModifyEvent(this));
  }

  public void onOutlineDragStart()
  {
    getContentDetails().saveChanges();
  }

  public void onOutlineSelectionChanged(ContentReference contentReference)
  {
    if (contentReference != null)
    {
      // Uncomment following line to have details view reset to content details on outline selection change
      // getDetailsPanel().displayContentDetails();
      displayCurrent(contentReference);
      getHistoryFramelet().onOutlineSelectionChanged(contentReference);
    }
  }

  public void onOutlineTreeStoreAdd(TreeStoreEvent<ContentReference> treeStoreEvent)
  {
    if (!isInitialAdd)
    {
      modificationContext.onModify();
      ContentReference parent = treeStoreEvent.getParent();
      if (parent != null)
      {
        refreshPartsList(parent);
      }
    }
  }

  public void onOutlineTreeStoreRemove(TreeStoreEvent<ContentReference> treeStoreEvent)
  {
    modificationContext.onModify();
    ContentReference parent = treeStoreEvent.getParent();
    if (parent != null)
    {
      refreshPartsList(parent);
    }
    display(null);
    getHistoryFramelet().display(null);
  }

  public void onOutlineTreeStoreUpdate(StoreEvent<ContentReference> storeEvent)
  {
    modificationContext.onModify(); // e.g. title of subordinate content
    ContentReference updatedContent = storeEvent.getModel();
    ContentReference rootContent = getRootContentReference();
    if (updatedContent == rootContent)
    {
      String newTitle = updatedContent.get(Keys.TITLE);
      String currentTitle = getHeadingTitle();
      if (!newTitle.equals(currentTitle))
      {
        setHeading(newTitle);
      }
    }
  }

  @Override
  protected void onRender(Element parent, int pos)
  {
    super.onRender(parent, pos);
    El.fly(head.el().getChildElement(0)).setStyleAttribute("cursor", "pointer");

    // Prevent GXT from invoking hide() when close button is pressed
    // Not only do we want to show the content when prompting to discard changes
    // But also SemanticTextArea requires DOM access for normalize() in isModified()
    Component closeButton = hookHeaderButton("x-tool-close");
    closeButton.setToolTip("Close");
    closeButton.removeAllListeners();
    closeButton.addListener(Events.Select, new EventPropagator(CloseButton));

    if (isNew)
    {
      getOutlineTreePanel().selectBestLeaf();
    }
    else
    {
      getOutlineTreePanel().selectRoot();
    }
  }

  public void openSelectedContent(OpenBrowserType openBrowserType)
  {
    final List<ContentReference> selectedContentReferences = getOutlineTreePanel().getSelectionModel().getSelectedItems();
    if (selectedContentReferences.size() == 0)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      if (isModified())
      {
        // Prompting to save would require not opening until the save is complete
        MessageBox.confirm("Unsaved Changes", "The selected item may contain unsaved changes. Would you like to open the most recently saved version?", new OpenUnsavedChangesListener(selectedContentReferences, openBrowserType));
      }
      else
      {
        openSelectedContentContinue(selectedContentReferences, openBrowserType);
      }
    }
  }

  protected void openSelectedContentContinue(List<ContentReference> selectedContentReferences, OpenBrowserType openBrowserType)
  {
    for (ContentReference selectedContentReference : selectedContentReferences)
    {
      String id = selectedContentReference.getId().formatString();
      UrlBuilder urlBuilder = new ClientUrlBuilder(UrlConstants.URL_CONTENT);
      switch (openBrowserType)
      {
        case DOCUMENT:
          urlBuilder.addParameter(UrlConstants.PARAMETER_ID, id);
          break;
        case WEB_PAGE:
          if (selectedContentReference != getRootContentReference())
          {
            MessageBox.alert("Browse as Pages", "Please select the uppermost parent document and try again.", null);
            return;
          }
          urlBuilder.addParameter(UrlConstants.PARAMETER_PATH, id);
          break;
      }
      String title = selectedContentReference.get(Keys.TITLE);
      urlBuilder.addTitle(title);
      String urlString = urlBuilder.toString();
      // See https://developer.mozilla.org/en/DOM/window.open
      Window.open(urlString, null, null);
    }
  }

  private void processContentManagerUpdateEvent(ContentManagerUpdateEvent contentManagerUpdateEvent)
  {
    Id contentId = contentManagerUpdateEvent.getContentId();
    List<ContentReference> contentReferences = findContentReferences(contentId);
    if (contentReferences != null)
    {
      getContentDetails().saveChanges();
      for (ContentReference contentReference : contentReferences)
      {
        if (isChanged(contentReference))
        {
          if (editConflicts == null)
          {
            editConflicts = new HashMap<ContentReference, ContentManagerUpdateEvent>();
          }
          editConflicts.put(contentReference, contentManagerUpdateEvent);
          if (isActive)
          {
            conflictTimer.schedule(5000); // gather all conflicts and ask just once
          }
        }
        else
        {
          replaceContent(contentReference, contentManagerUpdateEvent.getContentList());
        }
      }
    }
  }

  private void processEditConflicts()
  {
    // Make a copy for display to user. We can be preempted and more can arrive while we are displaying the message box. Also, each message box activation causes our activation handler to get invoked, which checks for pending edit conflicts. Otherwise the browser is single threaded so these two assignments are safe. 
    Map<ContentReference, ContentManagerUpdateEvent> editConflictSnapShot = editConflicts;
    editConflicts = null;

    StringBuilder s = new StringBuilder();
    s.append("The following content has been modified by other users while you were editing it:<br/><br/><b>");
    for (Entry<ContentReference, ContentManagerUpdateEvent> editConflict : editConflictSnapShot.entrySet())
    {
      ContentReference contentReference = editConflict.getKey();
      s.append(contentReference.get(Keys.TITLE));
      s.append("<br/>");
    }
    s.append("</b><br/>You can replace your changes with the updated content, or you can save your changes.<br/><br/>If you replace your changes with the updated content, your changes will be lost.<br/><br/>If you save your changes, the other user's changes will be available in the history.<br/><br/>Would you like to save your changes? Press YES to save them or NO to discard them.");
    MessageBox.confirm("Edit Conflict", s.toString(), new EditConflictMessageBoxListener(editConflictSnapShot));
  }

  public void publishSelectedContent()
  {
    if (isModified())
    {
      MessageBox.alert("Unsaved Changes", "Please save your changes before publishing.", null);
      return;
    }
    final List<ContentReference> selectedContentReferences = getOutlineTreePanel().getSelectionModel().getSelectedItems();
    if (selectedContentReferences.size() == 0)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      if (isRootOnly(selectedContentReferences))
      {
        publishSelectedContent(selectedContentReferences);
      }
      else
      {
        MessageBox.confirm("Publish", "Your selection includes items that are not top level content.<br/><br>Only the selected item(s) will be published.<br/><br/>Are you sure you want to continue?", new Listener<MessageBoxEvent>()
        {
          @Override
          public void handleEvent(MessageBoxEvent messageBoxEvent)
          {
            if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
            {
              publishSelectedContent(selectedContentReferences);
            }
          }
        });
      }
    }
  }

  private void publishSelectedContent(List<ContentReference> contentReferences)
  {
    List<Id> contentIds = new LinkedList<Id>();
    for (ContentReference contentReference : contentReferences)
    {
      contentIds.add(contentReference.getId());
    }
    String authenticationToken = Utility.getAuthenticationToken();
    Id monitorId = Utility.getMonitorId();
    Directory.getConnectorService().publishContent(authenticationToken, contentIds, monitorId, new FailureReportingAsyncCallback<Void>()
    {
      @Override
      public void onSuccess(Void result)
      {
        MessageBox.info("Publishing Complete", "The selected content has been published.", null);
      }
    });
  }

  private void refreshHeading()
  {
    String title = "";
    ContentReference rootContentReference = getRootContentReference();
    if (rootContentReference != null)
    {
      title = rootContentReference.get(Keys.TITLE);
    }
    setHeading(title);
  }

  private void refreshPartsList(ContentReference contentReference)
  {
    ContentType contentType = contentReference.get(Keys.CONTENT_TYPE);
    if (contentType == ContentType.DOCUMENT)
    {
      List<Id> parts = new LinkedList<Id>();
      List<ContentReference> children = getOutlineTreeStore().getChildren(contentReference);
      for (ContentReference child : children)
      {
        Id contentId = child.getId();
        parts.add(contentId);
      }
      contentReference.set(Keys.PARTS, parts);
    }
  }

  public void removeSelectedContent()
  {
    List<ContentReference> selectedContentReferences = getOutlineTreePanel().getSelectionModel().getSelectedItems();
    if (selectedContentReferences.size() == 0)
    {
      Utility.displaySelectionMessageBox();
    }
    else
    {
      for (ContentReference selectedContentReference : selectedContentReferences)
      {
        if (getOutlineTreeStore().getParent(selectedContentReference) == null)
        {
          MessageBox.alert("Delete Error", "You may not remove the top level content.", null);
          return;
        }
      }
      for (ContentReference selectedContentReference : selectedContentReferences)
      {
        unregister(selectedContentReference);
        getOutlineTreeStore().remove(selectedContentReference);
      }
    }
  }

  public void replaceContent(ContentReference contentReference, List<Content> contentList)
  {
    // Replacing a parent may result in subsequent deferred (conflicting) updates to children that no longer exist
    if (getOutlineTreeStore().indexOf(contentReference) != -1)
    {
      contentReference.clearHistory(); // Consider processContentManagerUpdateEvent -> replaceContent -> saveNodeState (don't want to save read only if history node is active)
      getModificationContext().setSuppressModificationTracking(true);
      try
      {
        ContentReference parent = getOutlineTreeStore().getParent(contentReference);
        int index = getOutlineTreeStore().indexOf(contentReference);

        Map<Id, NodeState> savedNodeState = saveNodeState();

        unregister(contentReference);
        getOutlineTreeStore().remove(contentReference);
        insertContentList(parent, index, contentList);

        restoreNodeState(savedNodeState);
      }
      finally
      {
        getModificationContext().setSuppressModificationTracking(false);
      }
    }
  }

  public void restoreNodeState(final Map<Id, NodeState> savedNodeState)
  {
    getOutlineTreeStore().visit(new StoreVisitor<ContentReference>()
    {
      @Override
      public boolean visit(ContentReference contentReference)
      {
        Id contentId = contentReference.getId();
        NodeState nodeState = savedNodeState.get(contentId);
        if (nodeState != null)
        {
          contentReference.setReadOnly(nodeState.isReadOnly());
          getOutlineTreePanel().setExpanded(contentReference, nodeState.isExpanded());
          if (nodeState.isSelected)
          {
            getOutlineTreePanel().getSelectionModel().select(contentReference, true);
          }
        }
        return true;
      }
    });
  }

  public void saveDocument()
  {
    getSaveButton().onSave();
    getContentDetails().saveChanges();

    List<Content> changedProperties = new LinkedList<Content>(); // changed properties only
    List<ContentReference> modifiedContentReferences = new LinkedList<ContentReference>(); // original content

    getOutlineTreeStore().visit(new SaveDocumentStoreVisitor(modifiedContentReferences, changedProperties));

    if (changedProperties.isEmpty())
    {
      clearModifiedState();
    }
    else
    {
      int modificationCount = modificationContext.getModificationCount();
      String authenticationToken = Utility.getAuthenticationToken();
      Id monitorId = Utility.getMonitorId();
      Directory.getConnectorService().updateContent(authenticationToken, changedProperties, monitorId, new SaveCallback(modifiedContentReferences, modificationCount));
    }
  }

  public Map<Id, NodeState> saveNodeState()
  {
    final Map<Id, NodeState> savedNodeState = new HashMap<Id, EditorFrame.NodeState>();

    getOutlineTreeStore().visit(new StoreVisitor<ContentReference>()
    {
      @Override
      public boolean visit(ContentReference content)
      {
        Id contentId = content.getId();
        boolean isReadOnly = content.isReadOnly();
        boolean isExpanded = getOutlineTreePanel().isExpanded(content);
        boolean isSelected = getOutlineTreePanel().getSelectionModel().isSelected(content);
        savedNodeState.put(contentId, new NodeState(isReadOnly, isExpanded, isSelected));
        content.setReadOnly(false);
        return true;
      }
    });
    return savedNodeState;
  }

  public void sendSelectedContent()
  {
  }

  @Override
  public void setHeading(String heading)
  {
    if (modificationContext.isModified())
    {
      heading = MODIFIED_HEADING + heading;
    }
    super.setHeading(heading);
  }

  private void showFileUploadWindow(final InsertLocationType insertLocationType, final ContentReference insertLocationReference)
  {
    Id uploadContentId = IdManager.createIdentifier();
    FileUploadSubmitCallback submitCallback = new FileUploadSubmitCallback(uploadContentId, insertLocationReference, insertLocationType);
    getFileUploadWindow().display(this, uploadContentId, submitCallback);
  }

  private void showFileUploadWindowAfterLogin(final InsertLocationType insertLocationType, final ContentReference insertLocationReference)
  {
    LoginRetryAsyncCallback<List<Content>> loginRetryAsyncCallback = new LoginRetryAsyncCallback<List<Content>>(null, new LoginCallbackAdapter()
    {
      @Override
      public void onLogin(Credential credential)
      {
        showFileUploadWindow(insertLocationType, insertLocationReference);
      }
    });
    loginRetryAsyncCallback.onFailure(new AuthenticationException());
  }

  public String toString()
  {
    return "[EditorFrame id=" + (getOutlineTreeStore().getChildCount() == 0 ? "n/a" : getOutlineTreeStore().getChild(0).getId()) + "]";
  }

  public void unregister(ContentReference contentReference)
  {
    System.out.println("EditorFrame.unregister: unregister contentReference=" + contentReference.getId());
    Directory.getContentManager().unregister(contentReference);

    List<ContentReference> children = getOutlineTreeStore().getChildren(contentReference, true);
    if (children != null) // children is null in nested deleted when parent has already been deleted
    {
      for (ContentReference child : children)
      {
        System.out.println("EditorFrame.unregister: unregister child=" + child.getId());
        Directory.getContentManager().unregister(child);
      }
    }
  }

  private final class ActivateListener implements Listener<ActivateEvent>
  {
    @Override
    public void handleEvent(ActivateEvent activateEvent)
    {
      onActivate(activateEvent.isActive());
    }
  }

  public class AddContentButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      Button button = ce.getButton();
      ContentType contentType = button.getData(Keys.CONTENT_TYPE);
      addContent(contentType, InsertLocationType.AFTER);
    }
  }

  private final class CloseListener implements Listener<CloseEvent>
  {
    @Override
    public void handleEvent(CloseEvent closeEvent)
    {
      onClose(closeEvent);
    }
  }

  private final class CloseModifiedConfirmListener implements Listener<MessageBoxEvent>
  {
    private final CloseEvent closeEvent;

    private CloseModifiedConfirmListener(CloseEvent closeEvent)
    {
      this.closeEvent = closeEvent;
    }

    @Override
    public void handleEvent(MessageBoxEvent messageBoxEvent)
    {
      if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
      {
        cleanup();
        closeEvent.getFrameManager().onCloseContinue(EditorFrame.this);
      }
    }
  }

  private final class ContentManagerUpdateEventListener implements EventListener<ContentManagerUpdateEvent>
  {
    @Override
    public void onEventNotification(ContentManagerUpdateEvent contentManagerUpdateEvent)
    {
      processContentManagerUpdateEvent(contentManagerUpdateEvent);
    }
  }

  private final class EditConflictMessageBoxListener implements Listener<MessageBoxEvent>
  {
    private Map<ContentReference, ContentManagerUpdateEvent> editConflictSnapShot;

    public EditConflictMessageBoxListener(Map<ContentReference, ContentManagerUpdateEvent> editConflictSnapShot)
    {
      this.editConflictSnapShot = editConflictSnapShot;
    }

    @Override
    public void handleEvent(MessageBoxEvent messageBoxEvent)
    {
      if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
      {
        saveDocument();
      }
      else
      {
        for (Entry<ContentReference, ContentManagerUpdateEvent> editConflict : editConflictSnapShot.entrySet())
        {
          ContentReference contentReference = editConflict.getKey();
          ContentManagerUpdateEvent contentManagerUpdateEvent = editConflict.getValue();
          replaceContent(contentReference, contentManagerUpdateEvent.getContentList());
        }
      }
    }
  }

  private final class EditConflictTimer extends Timer
  {
    @Override
    public void run()
    {
      checkEditConflicts();
    }
  }

  private final class EditorFrameCommitListener implements EventListener<EditorFrameCommitEvent>
  {
    @Override
    public void onEventNotification(EditorFrameCommitEvent eventNotification)
    {
      if (eventNotification.getEditorFrame() != EditorFrame.this)
      {
        // Another EditorFrame may have committed all of the modified content in this EditorFrame
        if (!isModified())
        {
          clearModifiedState();
        }
      }
    }
  }

  private final class FileUploadSubmitCallback implements Listener<FormEvent>
  {
    private ContentReference insertLocationReference;
    private InsertLocationType insertLocationType;
    private Id uploadContentId;

    public FileUploadSubmitCallback(Id uploadContentId, ContentReference insertLocationReference, InsertLocationType insertLocationType)
    {
      this.uploadContentId = uploadContentId;
      this.insertLocationReference = insertLocationReference;
      this.insertLocationType = insertLocationType;
    }

    @Override
    public void handleEvent(FormEvent be)
    {
      Directory.getConnectorService().retrieveContent(Utility.getAuthenticationToken(), uploadContentId, null, false, new RetrieveContentCallback());
    }

    private final class RetrieveContentCallback extends FailureReportingAsyncCallback<List<Content>>
    {
      @Override
      public void onSuccess(List<Content> result)
      {
        insertContent(insertLocationReference, insertLocationType, result.get(0));
      }
    }
  }

  private final class FindContentIdVisitor implements StoreVisitor<ContentReference>
  {
    private Id contentId;
    private List<ContentReference> contentReferences;

    public FindContentIdVisitor(Id contentId)
    {
      this.contentId = contentId;
    }

    public List<ContentReference> getContentReferences()
    {
      return contentReferences;
    }

    @Override
    public boolean visit(ContentReference contentReference)
    {
      if (contentReference.getId().equals(contentId))
      {
        if (contentReferences == null)
        {
          contentReferences = new LinkedList<ContentReference>();
        }
        contentReferences.add(contentReference);
      }
      return true;
    }
  }

  public class ModificationContext
  {
    private int commitPoint;
    private int modificationCount;
    private boolean suppressModificationTracking;

    public void checkModify()
    {
      if (!this.isModified() && EditorFrame.this.isModified())
      {
        onModify();
      }
    }

    public int getModificationCount()
    {
      return modificationCount;
    }

    public boolean isModified()
    {
      return modificationCount > commitPoint;
    }

    public boolean isSuppressModificationTracking()
    {
      return suppressModificationTracking;
    }

    public void onCommit(int modificationCount)
    {
      commitPoint = modificationCount;
    }

    public void onModify()
    {
      if (!suppressModificationTracking)
      {
        if (modificationCount++ == commitPoint)
        {
          onFirstModification();
        }
      }
    }

    public void setSuppressModificationTracking(boolean suppressModificationTracking)
    {
      this.suppressModificationTracking = suppressModificationTracking;
    }
  }

  public class NodeState
  {
    private boolean isExpanded;
    private boolean isReadOnly;
    private boolean isSelected;

    public NodeState(boolean isReadOnly, boolean isExpanded, boolean isSelected)
    {
      this.isReadOnly = isReadOnly;
      this.isExpanded = isExpanded;
      this.isSelected = isSelected;
    }

    public boolean isExpanded()
    {
      return isExpanded;
    }

    public boolean isReadOnly()
    {
      return isReadOnly;
    }

    public boolean isSelected()
    {
      return isSelected;
    }
  }

  public enum OpenBrowserType
  {
    DOCUMENT, WEB_PAGE
  }

  private final class OpenUnsavedChangesListener implements Listener<MessageBoxEvent>
  {
    private OpenBrowserType openBrowserType;
    private final List<ContentReference> selectedContentReferences;

    private OpenUnsavedChangesListener(List<ContentReference> selectedContentReferences, OpenBrowserType openBrowserType)
    {
      this.selectedContentReferences = selectedContentReferences;
      this.openBrowserType = openBrowserType;
    }

    @Override
    public void handleEvent(MessageBoxEvent messageBoxEvent)
    {
      if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
      {
        openSelectedContentContinue(selectedContentReferences, openBrowserType);
      }
    }
  }

  private final class SaveAllListener implements EventListener<SaveAllEvent>
  {
    @Override
    public void onEventNotification(SaveAllEvent saveAllEvent)
    {
      saveDocument();
    }
  }

  public class SaveButton extends ToolButton
  {
    private boolean isCommitPending;

    public SaveButton()
    {
      super("x-tool-save");
    }

    public void onCommit()
    {
      if (modificationContext.isModified())
      {
        // A modification occurred while we were waiting for confirmation
        show();
      }
      isCommitPending = false;
    }

    public void onFailure()
    {
      show();
      isCommitPending = false;
    }

    public void onFirstModification()
    {
      if (!isCommitPending)
      {
        show();
      }
    }

    public void onSave()
    {
      // Disable button to prevent multiple concurrent updates
      hide();
      isCommitPending = true;
    }
  }

  public class SaveButtonSelectionListener extends SelectionListener<IconButtonEvent>
  {
    @Override
    public void componentSelected(IconButtonEvent ce)
    {
      saveDocument();
    }
  }

  private final class SaveCallback extends FailureReportingAsyncCallback<Void>
  {
    private int modificationCount;
    private final List<ContentReference> modifiedContentReferences;

    private SaveCallback(List<ContentReference> modifiedContentReferences, int modificationCount)
    {
      this.modifiedContentReferences = modifiedContentReferences;
      this.modificationCount = modificationCount;
    }

    @Override
    public void onFailure(Throwable caught)
    {
      getSaveButton().onFailure();
      super.onFailure(caught);
    }

    @Override
    public void onSuccess(Void result)
    {
      for (ContentReference modifiedContentReference : modifiedContentReferences)
      {
        modifiedContentReference.commit();
      }
      onCommit(modificationCount);
    }
  }

  private final class SaveDocumentStoreVisitor implements StoreVisitor<ContentReference>
  {
    private final List<Content> changedProperties;
    private final List<ContentReference> modifiedContentReferences;

    private SaveDocumentStoreVisitor(List<ContentReference> modifiedContentReferences, List<Content> changedProperties)
    {
      this.modifiedContentReferences = modifiedContentReferences;
      this.changedProperties = changedProperties;
    }

    /**
     * Takes advantage of the modifiedContentReferences list to prevent multiple
     * references to a single content id from resulting in multiple appearances
     * of the underlying content in the save request. Note that this is not the
     * same as "already encountered" because it only detects content that has
     * been modified. This is fine for our purposes, but for large content lists
     * with few modifications and lots of duplicate references an alternative
     * approach (e.g. a HashMap) may be more efficient.
     */
    private boolean alreadyInSaveList(ContentReference contentReference)
    {
      for (ContentReference alreadyEncountered : modifiedContentReferences)
      {
        if (alreadyEncountered.getId().getId() == contentReference.getId().getId())
        {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean visit(ContentReference contentReference)
    {
      if (!alreadyInSaveList(contentReference))
      {
        Content changes = contentReference.getChanges();
        if (changes != null)
        {
          changedProperties.add(changes);
          modifiedContentReferences.add(contentReference);
        }
      }

      return true;
    }
  }

  private final class TextEditorUpdateListener implements EventListener<TextEditorUpdateEvent>
  {
    @Override
    public void onEventNotification(TextEditorUpdateEvent textEditorUpdateEvent)
    {
      getContentDetails().onTextEditorUpdate(textEditorUpdateEvent);
    }
  }

}
