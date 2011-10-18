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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
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
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.DuplicateUserNameException;
import com.semanticexpression.connector.shared.exception.InvalidCaptchaException;
import com.semanticexpression.connector.shared.exception.InvalidContentException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;
import com.semanticexpression.connector.shared.exception.InvalidLoginCredentialsException;
import com.semanticexpression.connector.shared.exception.InvalidPersonalSecurityAnswer;
import com.semanticexpression.connector.shared.exception.InvalidUserNameLengthException;
import com.semanticexpression.connector.shared.exception.PublicationException;
import com.semanticexpression.connector.shared.exception.ServerException;
import com.semanticexpression.connector.shared.exception.TagVisibilityException;

@RemoteServiceRelativePath("connectorService")
public interface ConnectorService extends RemoteService
{
  public SerializableClasses addSerializableClassesToWhiteList(SerializableClasses serializableClasses) throws ServerException;

  public void addTag(String authenticationToken, List<Id> contentIds, String tagName, TagType tagType, TagVisibility tagVisibility) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException, TagVisibilityException;

  public Credential checkPersonalSecurityAnswer(String userName, String personalSecurityAnswer) throws ServerException, InvalidLoginCredentialsException, InvalidPersonalSecurityAnswer;

  public Credential createAccount(String userName, String password, String emailAddress, String personalSecurityQuestion, String personalSecurityAnswer, String captchaKey) throws ServerException, InvalidCaptchaException, DuplicateUserNameException, InvalidUserNameLengthException, InvalidPersonalSecurityAnswer;

  public AdminResult executeAdminRequest(String authenticationToken, AdminRequest adminRequest) throws ServerException, AuthenticationException, AuthorizationException;

  public Credential getCredential(String authenticationToken) throws ServerException;

  public BasePagingLoadResult<HistoryItem> getHistory(String authenticationToken, Id contentId, PagingLoadConfig loadConfig) throws ServerException, AuthenticationException, AuthorizationException;

  public BasePagingLoadResult<Association> getMatchingNames(String authenticationToken, String wildcard, MatchingNameType type, PagingLoadConfig pagingLoadConfig) throws ServerException, AuthenticationException;

  public String getPersonalSecurityQuestion(String userName) throws ServerException, InvalidLoginCredentialsException;

  public List<Relationship> getRelationships(String authenticationToken, Id contentId) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException;

  public List<Status> getStatus(String authenticationToken, Id monitorId) throws ServerException;

  public Credential login(String userName, String password) throws ServerException, InvalidLoginCredentialsException;

  public void logout(String authenticationToken) throws ServerException;

  public void publishContent(String authenticationToken, List<Id> contentIds, Id monitorId) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException, PublicationException;

  public List<Content> retrieveContent(String authenticationToken, Id contentId, Date presentAt, boolean isDeep) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException;

  public BasePagingLoadResult<SearchResult> search(String authenticationToken, SearchRequest searchRequest, PagingLoadConfig pagingLoadConfig) throws ServerException, AuthenticationException;

  public void updateContent(String authenticationToken, List<Content> contents, Id monitorId) throws ServerException, AuthenticationException, InvalidContentException, InvalidContentIdException, AuthorizationException;

  public void updateWorkflowTask(String authenticationToken, Id workflowContentId, Association workflowTask, Id monitorId) throws ServerException, AuthenticationException, InvalidContentException, InvalidContentIdException, AuthorizationException;

}
