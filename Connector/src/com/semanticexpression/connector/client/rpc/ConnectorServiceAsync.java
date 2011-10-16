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

public interface ConnectorServiceAsync
{
  public void addSerializableClassesToWhiteList(SerializableClasses serializableClasses, AsyncCallback<SerializableClasses> callback);

  public void addTag(String authenticationToken, List<Id> contentIds, String tagName, TagType tagType, TagVisibility tagVisibility, AsyncCallback<Void> callback);

  public void checkPersonalSecurityAnswer(String userName, String personalSecurityAnswer, AsyncCallback<Credential> callback);

  public void createAccount(String userName, String password, String emailAddress, String personalSecurityQuestion, String personalSecurityAnswer, String captchaKey, AsyncCallback<Credential> callback);

  public void executeAdminRequest(String authenticationToken, AdminRequest adminRequest, AsyncCallback<AdminResult> callback);

  public void getCredential(String authenticationToken, AsyncCallback<Credential> callback);

  public void getHistory(String authenticationToken, Id contentId, PagingLoadConfig loadConfig, AsyncCallback<BasePagingLoadResult<HistoryItem>> callback);

  public void getMatchingNames(String authenticationToken, String wildcard, MatchingNameType type, PagingLoadConfig pagingLoadConfig, AsyncCallback<BasePagingLoadResult<Association>> callback);

  public void getPersonalSecurityQuestion(String userName, AsyncCallback<String> callback);

  public void getRelationships(String authenticationToken, Id contentId, AsyncCallback<List<Relationship>> callback);

  public void getStatus(String authenticationToken, Id monitorId, AsyncCallback<List<Status>> callback);

  public void login(String userName, String password, AsyncCallback<Credential> callback);

  public void logout(String authenticationToken, AsyncCallback<Void> callback);

  public void publishContent(String authenticationToken, List<Id> contentIds, Id monitorId, AsyncCallback<Void> callback);

  public void retrieveContent(String authenticationToken, Id contentId, Date presentAt, boolean isDeep, AsyncCallback<List<Content>> callback);

  public void search(String authenticationToken, SearchRequest searchRequest, PagingLoadConfig pagingLoadConfig, AsyncCallback<BasePagingLoadResult<SearchResult>> callback);

  public void updateContent(String authenticationToken, List<Content> contents, Id monitorId, AsyncCallback<Void> callback);

  public void updateWorkflowTask(String authenticationToken, Id workflowContentId, Association workflowTask, Id monitorId, AsyncCallback<Void> callback);

  public enum MatchingNameType
  {
    GROUP, KEYWORD, PRIVATE_TAG, PUBLIC_TAG, USER
  }

}
