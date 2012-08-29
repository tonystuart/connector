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

package com.semanticexpression.connector.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileUploadException;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.semanticexpression.connector.captcha.CaptchaServlet;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.rpc.ConnectorService;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.server.repository.RepositoryFactory;
import com.semanticexpression.connector.shared.AdminRequest;
import com.semanticexpression.connector.shared.AdminResult;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.HistoryItem;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Relationship;
import com.semanticexpression.connector.shared.SearchRequest;
import com.semanticexpression.connector.shared.SearchResult;
import com.semanticexpression.connector.shared.SerializableClasses;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.TagConstants.TagType;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.UrlConstants;
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

@SuppressWarnings("serial")
public class ConnectorServiceImpl extends RemoteServiceServlet implements ConnectorService
{
  private static String hostPageBaseURL;

  public static String getHostPageBaseURL()
  {
    return hostPageBaseURL;
  }

  private OperationFactory operationFactory;
  private ServerContext serverContext;

  @Override
  public SerializableClasses addSerializableClassesToWhiteList(SerializableClasses serializableClasses) throws ServerException
  {
    try
    {
      return serializableClasses;
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public void addTag(String authenticationToken, List<Id> contentIds, String tagName, TagType tagType, TagVisibility tagVisibility) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException, TagVisibilityException
  {
    try
    {
      TagOperation tagOperation = operationFactory.createTagOperation();
      tagOperation.addTag(authenticationToken, contentIds, tagName, tagType, tagVisibility);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public Credential checkPersonalSecurityAnswer(String userName, String personalSecurityAnswer) throws ServerException, InvalidLoginCredentialsException, InvalidPersonalSecurityAnswer
  {
    try
    {
      PersonalSecurityOperation personalSecurityOperation = operationFactory.createPersonalSecurityOperation();
      return personalSecurityOperation.checkPersonalSecurityAnswer(userName, personalSecurityAnswer);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public Credential createAccount(String userName, String password, String emailAddress, String personalSecurityQuestion, String personalSecurityAnswer, String captchaKey) throws ServerException, InvalidCaptchaException, DuplicateUserNameException, InvalidUserNameLengthException, InvalidPersonalSecurityAnswer
  {
    try
    {
      Credential credential = null;
      HttpServletRequest request = getThreadLocalRequest();
      HttpSession httpSession = request.getSession();
      String generatedCaptchaKey = (String)httpSession.getAttribute(CaptchaServlet.CAPTCHA_KEY);
      if (generatedCaptchaKey == null || !generatedCaptchaKey.equals(captchaKey))
      {
        throw new InvalidCaptchaException(captchaKey);
      }
      else
      {
        AccountOperation createAccountOperation = operationFactory.createAccountOperation();
        credential = createAccountOperation.createAccount(userName, password, emailAddress, personalSecurityQuestion, personalSecurityAnswer);
        httpSession.setAttribute(CaptchaServlet.CAPTCHA_KEY, null);
      }
      return credential;
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  private ServerContext createServerContext(ServletConfig servletConfig)
  {
    try
    {
      Log.info("ConnectorServiceImpl: initialization in progress");

      ServerProperties serverProperties = new ServerProperties(servletConfig);
      Log.setLogPropertyProvider(serverProperties);

      ServletContext servletContext = servletConfig.getServletContext();
      hostPageBaseURL = servletContext.getContextPath();

      String temporaryDirectoryPathName = serverProperties.getTemporaryDirectoryPathName();
      File temporaryDirectory = new File(temporaryDirectoryPathName);
      temporaryDirectory.mkdirs();

      String indexDirectoryPathName = serverProperties.getIndexDirectoryPathName();
      SearchEngine searchEngine = new SearchEngine(indexDirectoryPathName);
      searchEngine.open();

      String mailerSessionPropertiesPathName = serverProperties.getMailerSessionPropertiesPathName();
      Mailer mailer = new Mailer(mailerSessionPropertiesPathName);

      RepositoryFactory repositoryCreator = new RepositoryFactory(serverProperties);
      Repository repository = repositoryCreator.create();

      StatusQueues statusQueues = new StatusQueues();

      Random random = new Random();

      ServerContext serverContext = new ServerContext(serverProperties, repository, searchEngine, mailer, statusQueues, temporaryDirectory, random);
      Log.info("ConnectorServiceImpl: initialization complete");

      return serverContext;
    }
    catch (RuntimeException e)
    {
      Log.fatal("ConnectorServiceImpl: initialization failed, e=%s", e);
      throw e;
    }
  }

  private ServerException createServerException(RuntimeException e)
  {
    String stackTrace = Utility.getStackTrace(e);
    Log.error("ConnectorServiceImpl.createServerException: an exception occurred, stackTrace=%s", stackTrace);
    ServerException serverException = new ServerException(stackTrace);
    return serverException;
  }

  @Override
  public void destroy()
  {
    Log.info("ConnectorServiceImpl.terminate: server shutdown in progress");
    if (serverContext != null)
    {
      serverContext.getSearchEngine().close();
    }
    super.destroy();
  }

  private void disableCaching(HttpServletResponse response)
  {
    response.setHeader("Cache-Control", "no-cache");
  }

  protected void doGetContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      Cookie[] cookies = request.getCookies();
      String authenticationToken = getAuthenticationToken(cookies);

      String queryString = request.getQueryString();
      Parameters parameters = new Parameters(queryString);

      Date presentAt = parameters.getDate(UrlConstants.PARAMETER_PRESENT_AT, null);
      if (presentAt == null)
      {
        disableCaching(response);
      }

      RetrieveOperation retrieveOperation = operationFactory.createRetrieveOperation();
      retrieveOperation.retrieveHttpContent(authenticationToken, request, response, parameters);
    }
    catch (AuthenticationException e)
    {
      formatAccessException(response);
    }
    catch (AuthorizationException e)
    {
      formatAccessException(response);
    }
  }

  public void doPostContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      Cookie[] cookies = request.getCookies();
      String authenticationToken = getAuthenticationToken(cookies);

      ImageOperation imageOperation = operationFactory.createImageOperation();
      imageOperation.uploadImage(authenticationToken, request);
    }
    catch (FileUploadException e)
    {
      throw new ServletException(e);
    }
    catch (AuthenticationException e)
    {
      throw new ServletException(e);
    }
    catch (AuthorizationException e)
    {
      throw new ServletException(e);
    }
  }

  @Override
  protected void doUnexpectedFailure(Throwable throwable)
  {
    Log.fatal("******************* doUnexpectedFailure *******************");
    Log.fatal("e=%s", throwable);
    super.doUnexpectedFailure(throwable);
  }

  @Override
  public AdminResult executeAdminRequest(String authenticationToken, AdminRequest adminRequest) throws ServerException, AuthenticationException, AuthorizationException
  {
    try
    {
      AdminOperation adminOperation = operationFactory.createAdminOperation();
      return adminOperation.executeAdminRequest(authenticationToken, adminRequest);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  private void formatAccessException(HttpServletResponse response) throws ServletException
  {
    try
    {
      PrintWriter writer = response.getWriter();
      writer.write("<html><head><title>Access Exception</title></head><body>You do not have access to the requested content.<br/><br/>This may be due to one or more of the following reasons:<ul><li>The content identifier is incorrect</li><li>The content has been deleted</li><li>The content has not been published</li><li>You are not logged in as a user who has access to the content</li></ul>Please correct the problem and try again.</body></html>");
      writer.flush();
    }
    catch (IOException e)
    {
      throw new ServletException(e);
    }
  }

  private String getAuthenticationToken(Cookie[] cookies)
  {
    boolean found = false;
    String authenticationToken = null;
    if (cookies != null)
    {
      for (int i = 0; i < cookies.length && !found; i++)
      {
        Cookie cookie = cookies[i];
        String name = cookie.getName();
        if (name != null)
        {
          if (name.equals(Keys.AUTHENTICATION_TOKEN))
          {
            found = true;
            authenticationToken = cookie.getValue();
          }
        }
      }
    }
    return authenticationToken;
  }

  @Override
  public Credential getCredential(String authenticationToken) throws ServerException
  {
    try
    {
      CredentialOperation credentialOperation = operationFactory.createCredentialOperation();
      return credentialOperation.getCredential(authenticationToken);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public BasePagingLoadResult<HistoryItem> getHistory(String authenticationToken, Id contentId, PagingLoadConfig loadConfig) throws ServerException, AuthenticationException, AuthorizationException
  {
    try
    {
      HistoryOperation historyOperation = operationFactory.createHistoryOperation();
      return historyOperation.getHistory(authenticationToken, contentId, loadConfig);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public BasePagingLoadResult<Association> getMatchingNames(String authenticationToken, String wildcard, MatchingNameType type, PagingLoadConfig pagingLoadConfig) throws ServerException, AuthenticationException
  {
    try
    {
      MatchingOperation matchingOperation = operationFactory.createMatchingOperation();
      return matchingOperation.getMatchingNames(authenticationToken, wildcard, type, pagingLoadConfig);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public String getPersonalSecurityQuestion(String userName) throws ServerException, InvalidLoginCredentialsException
  {
    try
    {
      PersonalSecurityOperation personalSecurityOperation = operationFactory.createPersonalSecurityOperation();
      return personalSecurityOperation.getPersonalSecurityQuestion(userName);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public List<Relationship> getRelationships(String authenticationToken, Id contentId) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException
  {
    try
    {
      RelationshipOperation relationshipOperation = operationFactory.createRelationshipOperation();
      return relationshipOperation.getRelationships(authenticationToken, contentId);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public List<Status> getStatus(String authenticationToken, Id monitorId) throws ServerException
  {
    try
    {
      HttpServletRequest httpServletRequest = getThreadLocalRequest();
      String remoteAddr = httpServletRequest.getRemoteAddr();
      StatusOperation statusOperation = operationFactory.createStatusOperation();
      return statusOperation.getStatus(authenticationToken, monitorId, remoteAddr);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    serverContext = createServerContext(config);
    operationFactory = new OperationFactory(serverContext);
    super.init(config);
  }

  @Override
  public Credential login(String userName, String password) throws ServerException, InvalidLoginCredentialsException
  {
    try
    {
      LoginOperation loginOperation = operationFactory.createLoginOperation();
      return loginOperation.login(userName, password);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public void logout(String authenticationToken) throws ServerException
  {
    try
    {
      LogoutOperation logoutOperation = operationFactory.createLogoutOperation();
      logoutOperation.logout(authenticationToken);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public void publishContent(String authenticationToken, List<Id> contentIds, Id monitorId) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException, PublicationException
  {
    try
    {
      PublishOperation publishOperation = operationFactory.createPublishOperation();
      publishOperation.publishContent(authenticationToken, contentIds, monitorId);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public List<Content> retrieveContent(String authenticationToken, Id contentId, Date presentAt, boolean isDeep) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException
  {
    try
    {
      RetrieveOperation retrieveOperation = operationFactory.createRetrieveOperation();
      return retrieveOperation.retrieveContent(authenticationToken, contentId, presentAt, isDeep);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public BasePagingLoadResult<SearchResult> search(String authenticationToken, SearchRequest searchRequest, PagingLoadConfig pagingLoadConfig) throws ServerException, AuthenticationException
  {
    try
    {
      SearchOperation searchOperation = operationFactory.createSearchOperation();
      return searchOperation.search(authenticationToken, searchRequest, pagingLoadConfig);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException
  {
    if (servletRequest instanceof HttpServletRequest)
    {
      HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
      String method = httpServletRequest.getMethod();
      String servletPath = httpServletRequest.getServletPath();
      Log.debug("ConnectorServiceImpl.service: servletPath=%s", servletPath);
      if (servletPath.equals(UrlConstants.URL_CONTENT))
      {
        if ("POST".equals(method))
        {
          doPostContent(httpServletRequest, (HttpServletResponse)servletResponse);
          return;
        }
        if ("GET".equals(method))
        {
          doGetContent(httpServletRequest, (HttpServletResponse)servletResponse);
          return;
        }
      }
    }
    super.service(servletRequest, servletResponse);
  }

  @Override
  public void updateContent(String authenticationToken, List<Content> contents, Id monitorId) throws ServerException, AuthenticationException, InvalidContentException, InvalidContentIdException, AuthorizationException
  {
    try
    {
      UpdateOperation updateOperation = operationFactory.createUpdateOperation();
      updateOperation.updateContent(authenticationToken, contents, monitorId);

      WorkflowOperation workflowOperation = operationFactory.createWorkflowOperation();
      workflowOperation.updateWorkflow(contents);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

  @Override
  public void updateWorkflowTask(String authenticationToken, Id workflowContentId, Association newWorkflowTask, Id monitorId) throws ServerException, AuthenticationException, InvalidContentException, InvalidContentIdException, AuthorizationException
  {
    try
    {
      UpdateOperation updateOperation = operationFactory.createUpdateOperation();
      updateOperation.updateWorkflowTask(authenticationToken, workflowContentId, newWorkflowTask, monitorId);

      RetrieveOperation retrieveOperation = operationFactory.createRetrieveOperation();
      List<Content> updatedContent = retrieveOperation.retrieveContent(workflowContentId, null, false);

      WorkflowOperation workflowOperation = operationFactory.createWorkflowOperation();
      workflowOperation.updateWorkflow(updatedContent);
    }
    catch (RuntimeException e)
    {
      throw createServerException(e);
    }
  }

}
