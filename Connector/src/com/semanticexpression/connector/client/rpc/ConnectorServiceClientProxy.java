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

package com.semanticexpression.connector.client.rpc;

import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.rpc.LoginRetryAsyncCallback.LoginCallbackAdapter;
import com.semanticexpression.connector.shared.AdminRequest;
import com.semanticexpression.connector.shared.AdminResult;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.HistoryItem;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Relationship;
import com.semanticexpression.connector.shared.SearchRequest;
import com.semanticexpression.connector.shared.SearchResult;
import com.semanticexpression.connector.shared.SerializableClasses;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.TagConstants.TagType;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;

public class ConnectorServiceClientProxy implements ConnectorServiceAsync
{
  private ConnectorServiceAsync connectorServiceAsync;

  public ConnectorServiceClientProxy(ConnectorServiceAsync connectorServiceAsync)
  {
    this.connectorServiceAsync = connectorServiceAsync;
  }

  @Override
  public void addSerializableClassesToWhiteList(SerializableClasses serializableClasses, AsyncCallback<SerializableClasses> callback)
  {
  }

  @Override
  public void addTag(final String authenticationToken, final List<Id> contentIds, final String tagName, final TagType tagType, final TagVisibility tagVisibility, final AsyncCallback<Void> callback)
  {
    connectorServiceAsync.addTag(authenticationToken, contentIds, tagName, tagType, tagVisibility, new LoginRetryAsyncCallback<Void>(callback, new LoginCallbackAdapter()
    {
      @Override
      public void onLogin(Credential credential)
      {
        connectorServiceAsync.addTag(authenticationToken, contentIds, tagName, tagType, tagVisibility, callback);
      }
    }));
  }

  @Override
  public void checkPersonalSecurityAnswer(String userName, String personalSecurityAnswer, AsyncCallback<Credential> callback)
  {
    connectorServiceAsync.checkPersonalSecurityAnswer(userName, personalSecurityAnswer, callback);
  }

  @Override
  public void createAccount(String userName, String password, String emailAddress, String personalSecurityQuestion, String personalSecurityAnswer, String captchaKey, AsyncCallback<Credential> callback)
  {
    connectorServiceAsync.createAccount(userName, password, emailAddress, personalSecurityQuestion, personalSecurityAnswer, captchaKey, callback);
  }

  @Override
  public void executeAdminRequest(String authenticationToken, AdminRequest adminRequest, AsyncCallback<AdminResult> callback)
  {
    connectorServiceAsync.executeAdminRequest(authenticationToken, adminRequest, callback);
  }

  @Override
  public void getCredential(String authenticationToken, AsyncCallback<Credential> callback)
  {
    connectorServiceAsync.getCredential(authenticationToken, callback);
  }

  @Override
  public void getHistory(String authenticationToken, Id contentId, PagingLoadConfig loadConfig, AsyncCallback<BasePagingLoadResult<HistoryItem>> callback)
  {
    connectorServiceAsync.getHistory(authenticationToken, contentId, loadConfig, callback);
  }

  @Override
  public void getMatchingNames(String authenticationToken, String wildcard, MatchingNameType type, PagingLoadConfig pagingLoadConfig, AsyncCallback<BasePagingLoadResult<Association>> callback)
  {
    connectorServiceAsync.getMatchingNames(authenticationToken, wildcard, type, pagingLoadConfig, callback);
  }

  @Override
  public void getPersonalSecurityQuestion(String userName, AsyncCallback<String> callback)
  {
    connectorServiceAsync.getPersonalSecurityQuestion(userName, callback);
  }

  @Override
  public void getRelationships(String authenticationToken, Id contentId, AsyncCallback<List<Relationship>> callback)
  {
    connectorServiceAsync.getRelationships(authenticationToken, contentId, callback);
  }

  @Override
  public void getStatus(String authenticationToken, Id monitorId, AsyncCallback<List<Status>> callback)
  {
    connectorServiceAsync.getStatus(authenticationToken, monitorId, callback);
  }

  @Override
  public void login(String userName, String password, AsyncCallback<Credential> callback)
  {
    connectorServiceAsync.login(userName, password, callback);
  }

  @Override
  public void logout(String authenticationToken, AsyncCallback<Void> callback)
  {
    connectorServiceAsync.logout(authenticationToken, callback);
  }

  @Override
  public void publishContent(final String authenticationToken, final List<Id> contentIds, final Id monitorId, final AsyncCallback<Void> callback)
  {
    connectorServiceAsync.publishContent(authenticationToken, contentIds, monitorId, new LoginRetryAsyncCallback<Void>(callback, new LoginCallbackAdapter()
    {
      @Override
      public void onLogin(Credential credential)
      {
        connectorServiceAsync.publishContent(authenticationToken, contentIds, Utility.getMonitorId(), callback);
      }
    }));
  }

  @Override
  public void retrieveContent(final String authenticationToken, final Id content, final Date presentAt, final boolean isDeep, final AsyncCallback<List<Content>> callback)
  {
    connectorServiceAsync.retrieveContent(authenticationToken, content, presentAt, isDeep, new ClearStatusCallback<List<Content>>("Opening...", new LoginRetryAsyncCallback<List<Content>>(callback, new LoginCallbackAdapter()
    {
      @Override
      public void onLogin(Credential credential)
      {
        connectorServiceAsync.retrieveContent(credential.getAuthenticationToken(), content, presentAt, isDeep, callback);
      }
    })));
  }

  @Override
  public void search(String authenticationToken, SearchRequest searchRequest, PagingLoadConfig pagingLoadConfig, AsyncCallback<BasePagingLoadResult<SearchResult>> callback)
  {
    connectorServiceAsync.search(authenticationToken, searchRequest, pagingLoadConfig, new ClearStatusCallback<BasePagingLoadResult<SearchResult>>("Searching...", callback));
  }

  @Override
  public void updateContent(String authenticationToken, final List<Content> contents, final Id monitorId, final AsyncCallback<Void> callback)
  {
    connectorServiceAsync.updateContent(authenticationToken, contents, monitorId, new LoginRetryAsyncCallback<Void>(callback, new LoginCallbackAdapter()
    {
      @Override
      public void onLogin(Credential credential)
      {
        connectorServiceAsync.updateContent(credential.getAuthenticationToken(), contents, Utility.getMonitorId(), callback);
      }
    }));
  }

  @Override
  public void updateWorkflowTask(final String authenticationToken, final Id workflowContentId, final Association workflowTask, final Id monitorId, final AsyncCallback<Void> callback)
  {
    connectorServiceAsync.updateWorkflowTask(authenticationToken, workflowContentId, workflowTask, monitorId, new LoginRetryAsyncCallback<Void>(callback, new LoginCallbackAdapter()
    {
      @Override
      public void onLogin(Credential credential)
      {
        connectorServiceAsync.updateWorkflowTask(authenticationToken, workflowContentId, workflowTask, Utility.getMonitorId(), callback);
      }
    }));
  }

}
