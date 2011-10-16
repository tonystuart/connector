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

package com.semanticexpression.connector.client;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.semanticexpression.connector.client.events.EditorFrameCommitEvent;
import com.semanticexpression.connector.client.events.EditorFrameModifyEvent;
import com.semanticexpression.connector.client.events.EditorFrameRollbackEvent;
import com.semanticexpression.connector.client.events.LoginEvent;
import com.semanticexpression.connector.client.events.LogoutEvent;
import com.semanticexpression.connector.client.events.SaveAllEvent;
import com.semanticexpression.connector.client.events.WorkflowTaskUpdateEvent;
import com.semanticexpression.connector.client.frame.admin.AdminFrame;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.monitor.MonitorFrame;
import com.semanticexpression.connector.client.frame.search.SearchFrame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.services.ClearStatusMessageServiceRequest;
import com.semanticexpression.connector.client.services.DisplayStatusMessageServiceRequest;
import com.semanticexpression.connector.client.services.OpenExistingContentServiceRequest;
import com.semanticexpression.connector.client.widget.Frame;
import com.semanticexpression.connector.client.widget.FrameManager;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.client.wiring.ServiceProvider;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Credential.AuthenticationType;
import com.semanticexpression.connector.shared.Id;

public class MainPanel extends FrameManager
{
  private Button adminButton;
  private Button createButton;
  private Html currentUserHtml;
  private String initialState;
  private Button monitorButton;
  private MonitorFrame monitorFrame;
  private SaveAllButton saveAllButton;
  private Button searchButton;
  private Status status;
  private FillToolItem topFillToolItem;
  private ToolBar topToolBar;

  public MainPanel()
  {
    setLayout(new FitLayout());
    setHeaderVisible(false);
    setTopComponent(getTopToolBar());

    Directory.getEventBus().addListener(LoginEvent.class, new LoginEventListener());
    Directory.getEventBus().addListener(EditorFrameModifyEvent.class, new EditorFrameModifyEventListener());
    Directory.getEventBus().addListener(EditorFrameCommitEvent.class, new EditorFrameCommitEventListener());
    Directory.getEventBus().addListener(EditorFrameRollbackEvent.class, new EditorFrameRollbackEventListener());
    Directory.getEventBus().addListener(WorkflowTaskUpdateEvent.class, new WorkflowTaskUpdateEventListener());

    Directory.getServiceBus().addServiceProvider(OpenExistingContentServiceRequest.class, new OpenExistingContentServiceProvider());
    Directory.getServiceBus().addServiceProvider(DisplayStatusMessageServiceRequest.class, new DisplayStatusMessageServiceProvider());
    Directory.getServiceBus().addServiceProvider(ClearStatusMessageServiceRequest.class, new ClearStatusMessageServiceProvider());

    History.addValueChangeHandler(new UrlChangeListener());

    initialState = History.getToken();

    getMonitorFrame(); // create to monitor events, but don't show until requested
  }

  @Override
  public void addFrame(Frame frame)
  {
    super.addFrame(frame);

    saveBrowserHistory();
  }

  private void create()
  {
    List<Content> contentList = Directory.getContentManager().createDocument();
    EditorFrame editorFrame = new EditorFrame(contentList, true);
    addFrame(editorFrame);
  }

  public Button getAdminButton()
  {
    if (adminButton == null)
    {
      adminButton = new Button("Admin");
      adminButton.setIcon(Resources.ADMIN);
      adminButton.setToolTip("Open administrator window to manage system");
      adminButton.setVisible(false);
      adminButton.addSelectionListener(new AdminListener());
    }
    return adminButton;
  }

  public Button getCreateButton()
  {
    if (createButton == null)
    {
      createButton = new Button("Create");
      createButton.setIcon(Resources.EDITOR);
      createButton.setToolTip("Create a new document");
      createButton.setEnabled(false);
      createButton.addSelectionListener(new CreateListener());
    }
    return createButton;
  }

  protected String getCurrentState()
  {
    StringBuilder s = new StringBuilder();

    Set<Frame> frames = getFrames();
    for (Frame frame : frames)
    {
      if (s.length() > 0)
      {
        s.append(",");
      }
      if (frame instanceof SearchFrame)
      {
        s.append("s");
      }
      else if (frame instanceof MonitorFrame)
      {
        s.append("m");
      }
      else if (frame instanceof EditorFrame)
      {
        EditorFrame editorFrame = (EditorFrame)frame;
        ContentReference rootContentReference = editorFrame.getRootContentReference();
        String formattedId = rootContentReference.getId().formatString();
        s.append(formattedId);
      }
    }
    return s.toString();
  }

  public Html getCurrentUserHtml()
  {
    if (currentUserHtml == null)
    {
      currentUserHtml = new Html();
      currentUserHtml.sinkEvents(Events.OnClick.getEventCode());
      currentUserHtml.addListener(Events.OnClick, new LogoutListener());
    }
    return currentUserHtml;
  }

  private EditorFrame getFirstEditorFrame(Id contentId)
  {
    for (Frame frame : getFrames())
    {
      if (frame instanceof EditorFrame)
      {
        EditorFrame editorFrame = (EditorFrame)frame;
        ContentReference rootContentReference = editorFrame.getRootContentReference();
        if (rootContentReference != null && rootContentReference.getId().equals(contentId))
        {
          return editorFrame;
        }
      }
    }
    return null;
  }

  public Button getMonitorButton()
  {
    if (monitorButton == null)
    {
      monitorButton = new Button("Monitor");
      monitorButton.setIcon(Resources.MONITOR);
      monitorButton.setToolTip("Open monitor window for content updates");
      monitorButton.setEnabled(false);
      monitorButton.addSelectionListener(new MonitorListener());
    }
    return monitorButton;
  }

  private MonitorFrame getMonitorFrame()
  {
    if (monitorFrame == null)
    {
      monitorFrame = new MonitorFrame();
    }
    return monitorFrame;
  }

  private SaveAllButton getSaveAllButton()
  {
    if (saveAllButton == null)
    {
      saveAllButton = new SaveAllButton("Save All");
      saveAllButton.setIcon(Resources.SAVE_ALL);
      saveAllButton.addSelectionListener(new SaveAllListener());
    }
    return saveAllButton;
  }

  public Button getSearchButton()
  {
    if (searchButton == null)
    {
      searchButton = new Button("Search");
      searchButton.setIcon(Resources.SEARCH);
      searchButton.setToolTip("Search for content");
      searchButton.setEnabled(false);
      searchButton.addSelectionListener(new SearchListener());
    }
    return searchButton;
  }

  public Status getStatus()
  {
    if (status == null)
    {
      status = new Status();
      status.setStyleAttribute("leftPadding", "30px");
      status.setWidth(150);
    }
    return status;
  }

  public FillToolItem getTopFillToolItem()
  {
    if (topFillToolItem == null)
    {
      topFillToolItem = new FillToolItem();
    }
    return topFillToolItem;
  }

  public ToolBar getTopToolBar()
  {
    if (topToolBar == null)
    {
      topToolBar = new ToolBar();
      topToolBar.setSpacing(3);
      topToolBar.add(getSearchButton());
      topToolBar.add(getCreateButton());
      topToolBar.add(getMonitorButton());
      topToolBar.add(getAdminButton());
      topToolBar.add(getSaveAllButton());
      topToolBar.add(getStatus());
      topToolBar.add(getTopFillToolItem());
      topToolBar.add(getCurrentUserHtml());
    }
    return topToolBar;
  }

  private void loadBrowserHistory(String desiredState)
  {
    Id contentId;
    String[] tokens = desiredState.split(",");
    for (String token : tokens)
    {
      if (token.equals("s"))
      {
        search();
      }
      else if (token.startsWith("s:"))
      {
        String terms = token.substring(2);
        SearchFrame searchFrame = search();
        searchFrame.search(terms);
      }
      else if (token.equals("m"))
      {
        showMonitorPanel();
      }
      else if ((contentId = new Id()).parseString(token))
      {
        EditorFrame editorFrame = getFirstEditorFrame(contentId);
        if (editorFrame == null) // only open if not open (e.g. unauth user, new doc, logs in, gets auto refresh)
        {
          Directory.getConnectorService().retrieveContent(Utility.getAuthenticationToken(), contentId, null, true, new RetrieveCallback());
        }
      }
    }
  }

  @Override
  public void onCloseContinue(Frame frame)
  {
    super.onCloseContinue(frame);

    saveBrowserHistory();
  }

  /**
   * Handles all types of session activation and deactivation including initial
   * startup, login and credential upgrade (e.g. guest to user), logout, refresh
   * and changes to the URL fragment (which currently results in a refresh).
   * <p/>
   * We can't restore the history state requested at startup until we get the
   * credential information back from the server, so we cache the requested
   * state at startup and restore it here.
   * @param isAdministrator 
   */
  private void onCredentialChange(boolean isAccessPermitted, boolean isAuthenticated, boolean isAdministrator)
  {
    getSearchButton().setEnabled(isAccessPermitted);
    getCreateButton().setEnabled(isAccessPermitted);
    getMonitorButton().setEnabled(isAccessPermitted);
    getAdminButton().setVisible(isAdministrator);

    if (isAccessPermitted)
    {
      if (initialState != null)
      {
        loadBrowserHistory(initialState);
        initialState = null;
      }
      else if (isAuthenticated) // e.g. user went from guest to authenticated access
      {
        loadBrowserHistory(History.getToken());
      }
    }
  }

  private void onOpenExistingContentServiceRequest(OpenExistingContentServiceRequest serviceRequest)
  {
    Id contentId = serviceRequest.getContentId();
    EditorFrame editorFrame = getFirstEditorFrame(contentId);
    if (editorFrame == null)
    {
      open(contentId);
    }
    else
    {
      String title = serviceRequest.getTitle();
      MessageBox.confirm("Already Open", "The content your requested (" + title + ") is already open.<br/><br/>Would you like to open an additional instance?<br/><br/>Select Yes to open an additional instance or No to activate the open instance.", new AlreadyOpenMessageBoxListener(editorFrame, contentId));
    }
  }

  @Override
  protected void onRender(Element parent, int pos)
  {
    super.onRender(parent, pos);
  }

  private void onWorkflowTaskUpdate(WorkflowTaskUpdateEvent workflowTaskUpdateEvent)
  {
    if (workflowTaskUpdateEvent.isFinished())
    {
      EditorFrame editorFrame = workflowTaskUpdateEvent.getEditorFrame();
      close(editorFrame);
    }
  }

  private void open(Id contentId)
  {
    Directory.getConnectorService().retrieveContent(Utility.getAuthenticationToken(), contentId, null, true, new RetrieveCallback());
  }

  protected void saveAll()
  {
    Directory.getEventBus().post(new SaveAllEvent());
  }

  protected void saveBrowserHistory()
  {
    String currentState = getCurrentState();
    History.newItem(currentState, false);
  }

  private SearchFrame search()
  {
    SearchFrame searchFrame = new SearchFrame();
    addFrame(searchFrame);
    return searchFrame;
  }

  public void showAdminPanel()
  {
    addFrame(new AdminFrame());
  }

  public void showMonitorPanel()
  {
    if (!getMonitorFrame().isVisible())
    {
      addFrame(getMonitorFrame());
    }
    else
    {
      activate(getMonitorFrame());
    }
  }

  public void updateCurrentUserHtml(Credential credential)
  {
    AuthenticationType authenticationType = credential.getAuthenticationType();
    String userName = credential.getUserName();
    switch (authenticationType)
    {
      case AUTHENTICATION_REQUIRED:
        currentUserHtml.setHtml("<span class='connector-InlineHtml'>You are not currently logged in. Click <span class='connector-InlineHyperLink'>here</span> to login.</span>");
        break;
      case UNAUTHENTICATED:
        currentUserHtml.setHtml("<span class='connector-InlineHtml'>You have read only guest access. Click <span class='connector-InlineHyperLink'>here</span> to login.</span>");
        onCredentialChange(true, false, false);
        break;
      case AUTHENTICATED:
        currentUserHtml.setHtml("<span class='connector-InlineHtml'>You are logged in as " + userName + ". Click <span class='connector-InlineHyperLink'>here</span> to logout.</span>");
        boolean isAdministrator = credential.isAdministrator();
        onCredentialChange(true, true, isAdministrator);
        break;
    }
  }

  private class AdminListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      showAdminPanel();
    }
  }

  private final class AlreadyOpenMessageBoxListener implements Listener<MessageBoxEvent>
  {
    private final Id contentId;
    private final EditorFrame editorFrame;

    private AlreadyOpenMessageBoxListener(EditorFrame editorFrame, Id contentId)
    {
      this.editorFrame = editorFrame;
      this.contentId = contentId;
    }

    @Override
    public void handleEvent(MessageBoxEvent messageBoxEvent)
    {
      if (Dialog.YES.equals(messageBoxEvent.getButtonClicked().getItemId()))
      {
        open(contentId);
      }
      else
      {
        activate(editorFrame);
      }
    }
  }

  public class ClearStatusMessageServiceProvider implements ServiceProvider<ClearStatusMessageServiceRequest>
  {
    @Override
    public void onServiceRequest(ClearStatusMessageServiceRequest serviceRequest)
    {
      getStatus().clearStatus(null);
    }
  }

  private class CreateListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      create();
    }
  }

  public class DisplayStatusMessageServiceProvider implements ServiceProvider<DisplayStatusMessageServiceRequest>
  {
    @Override
    public void onServiceRequest(DisplayStatusMessageServiceRequest serviceRequest)
    {
      getStatus().setIconStyle("icon-wait");
      getStatus().setText(serviceRequest.getMessage());
    }
  }

  private final class EditorFrameCommitEventListener implements EventListener<EditorFrameCommitEvent>
  {
    @Override
    public void onEventNotification(EditorFrameCommitEvent editorFrameCommitEvent)
    {
      getSaveAllButton().onCommit();
      saveBrowserHistory(); // for permanent content id
    }
  }

  private final class EditorFrameModifyEventListener implements EventListener<EditorFrameModifyEvent>
  {
    @Override
    public void onEventNotification(EditorFrameModifyEvent editorFrameModifyEvent)
    {
      getSaveAllButton().onModify();
    }
  }

  private final class EditorFrameRollbackEventListener implements EventListener<EditorFrameRollbackEvent>
  {
    @Override
    public void onEventNotification(EditorFrameRollbackEvent editorFrameRollbackEvent)
    {
      getSaveAllButton().onRollback();
    }
  }

  private final class LoginEventListener implements EventListener<LoginEvent>
  {
    @Override
    public void onEventNotification(LoginEvent loginEvent)
    {
      Credential credential = loginEvent.getCredential();
      updateCurrentUserHtml(credential);
    }
  }

  private final class LogoutListener implements Listener<ComponentEvent>
  {
    public void handleEvent(ComponentEvent be)
    {
      onCredentialChange(false, false, false);
      Directory.getEventBus().post(new LogoutEvent());
    }
  }

  private class MonitorListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      showMonitorPanel();
    }
  }

  public class OpenExistingContentServiceProvider implements ServiceProvider<OpenExistingContentServiceRequest>
  {
    @Override
    public void onServiceRequest(OpenExistingContentServiceRequest serviceRequest)
    {
      onOpenExistingContentServiceRequest(serviceRequest);
    }
  }

  private final class RetrieveCallback extends FailureReportingAsyncCallback<List<Content>>
  {
    @Override
    public void onSuccess(List<Content> contentList)
    {
      EditorFrame editorFrame = new EditorFrame(contentList, false);
      addFrame(editorFrame);
    }
  }

  private final class SaveAllListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      saveAll();
    }
  }

  private class SearchListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      search();
    }
  }

  /**
   * Listens for changes in the browser URL fragment (the part following the
   * hash mark) while the application is running. These changes can be processed
   * without requiring a reload.
   * <p/>
   * At present, we lack the ability to merge the requested URL fragment with
   * the current state. There are a number of complex special cases, such as:
   * <ul>
   * <li>prompting to close windows with unsaved changes</li>
   * <li>ensuring that the asynchronously opened windows end up in the correct
   * sequence</li>
   * <li>restoring offset and extent for manually positioned windows</li>
   * <li>ensuring that we do not process a load request while waiting at a login
   * prompt.
   * </ul>
   * For now we just let refresh load the new URL from scratch.
   */
  private final class UrlChangeListener implements ValueChangeHandler<String>
  {
    public void onValueChange(ValueChangeEvent<String> event)
    {
      Window.Location.reload();
    }
  }

  private final class WorkflowTaskUpdateEventListener implements EventListener<WorkflowTaskUpdateEvent>
  {
    @Override
    public void onEventNotification(WorkflowTaskUpdateEvent workflowTaskUpdateEvent)
    {
      onWorkflowTaskUpdate(workflowTaskUpdateEvent);
    }
  }
}
