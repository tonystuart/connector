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

import java.util.LinkedHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


public class ServerProperties implements LogPropertyProvider
{
  private Configuration logConfiguration;
  private Configuration repositoryConfiguration;
  private Configuration serverConfiguration;
  private ServletConfig servletConfig;
  private ServletContext servletContext;
  private Configuration workflowConfiguration;

  public ServerProperties(ServletConfig servletConfig)
  {
    this.servletConfig = servletConfig;
    this.servletContext = servletConfig.getServletContext();

    String serverConfigurationPathName = getServletConfigPathName("serverConfigurationPathName");
    String repositoryConfigurationPathName = getServletConfigPathName("repositoryConfigurationPathName");
    String workflowConfigurationPathName = getServletConfigPathName("workflowConfigurationPathName");
    String logConfigurationPathName = getServletConfigPathName("logConfigurationPathName");

    serverConfiguration = new Configuration(serverConfigurationPathName);
    repositoryConfiguration = new Configuration(repositoryConfigurationPathName);
    workflowConfiguration = new Configuration(workflowConfigurationPathName);
    logConfiguration = new Configuration(logConfigurationPathName);
  }

  public ServerProperties(String fileName)
  {
    serverConfiguration = new Configuration(fileName);
  }

  public int getAccountCreationOptions()
  {
    return serverConfiguration.getInt("accountCreationOptions");
  }

  public LinkedHashMap<String, String> getAllowedSqlStates()
  {
    return repositoryConfiguration.getAll("allowedSqlState.*");
  }

  public String getApplicationBaseUrl()
  {
    return serverConfiguration.get("applicationBaseUrl");
  }

  public int getAuthenticationTokenLength()
  {
    return serverConfiguration.getInt("authenticationTokenLength");
  }

  public int getCredentialTimeoutMillis()
  {
    return serverConfiguration.getInt("credentialTimeoutMillis");
  }

  public String getDatabaseUrl()
  {
    return serverConfiguration.get("databaseUrl");
  }

  public String getEncryptedAdministrationPassword()
  {
    return serverConfiguration.get("encryptedAdministrationPassword", null);
  }

  public String getIndexDirectoryPathName()
  {
    String pathName = serverConfiguration.get("indexDirectoryPathName");
    String realPathName = servletContext.getRealPath(pathName);
    return realPathName;
  }

  @Override
  public int getLogLevel()
  {
    return logConfiguration.getInt("logLevel");
  }

  @Override
  public String getLogPathName()
  {
    String pathName = logConfiguration.get("logPathName", null);
    String realPathName = servletContext.getRealPath(pathName);
    return realPathName;
  }

  @Override
  public LinkedHashMap<String, String> getLogPatterns()
  {
    return logConfiguration.getAll("logPattern.*");
  }

  public String getMailerSessionPropertiesPathName()
  {
    // Java Mail API requires Session Properties and cannot use a MailerPropertyProvider
    String pathName = serverConfiguration.get("mailerSessionPropertiesPathName", null);
    String realPathName = servletContext.getRealPath(pathName);
    return realPathName;
  }

  public int getMaximumConnections()
  {
    return serverConfiguration.getInt("maximumConnections");
  }

  public int getMaximumUploadSize()
  {
    return serverConfiguration.getInt("maximumUploadSize");
  }

  public LinkedHashMap<String, String> getRepositoryDefinitions()
  {
    return repositoryConfiguration.getAll("repositoryDefinition.*");
  }

  public String getRootPathName()
  {
    String rootPathName = servletContext.getRealPath("/");
    return rootPathName;
  }

  private String getServletConfigPathName(String name)
  {
    String value = servletConfig.getInitParameter(name);
    String path = servletContext.getRealPath(value);
    return path;
  }

  public int getStatusQueueTimeoutMillis()
  {
    return serverConfiguration.getInt("statusQueueTimeoutMillis");
  }

  public String getTemporaryDirectoryPathName()
  {
    String pathName = serverConfiguration.get("temporaryDirectoryPathName");
    String realPathName = servletContext.getRealPath(pathName);
    return realPathName;
  }

  public String getWorkflowCompletionBody()
  {
    return workflowConfiguration.get("workflowCompletionBody");
  }

  public String getWorkflowCompletionSubject()
  {
    return workflowConfiguration.get("workflowCompletionSubject");
  }

  public String getWorkflowReadyBody()
  {
    return workflowConfiguration.get("workflowReadyBody");
  }

  public String getWorkflowReadySubject()
  {
    return workflowConfiguration.get("workflowReadySubject");
  }

  public String getWorkflowRejectionBody()
  {
    return workflowConfiguration.get("workflowRejectionBody");
  }

  public String getWorkflowRejectionSubject()
  {
    return workflowConfiguration.get("workflowRejectionSubject");
  }

  public boolean isAllowGuestAccess()
  {
    return serverConfiguration.getBoolean("isAllowGuestAccess");
  }

  @Override
  public boolean isLogPropertyInitializationRequired()
  {
    boolean isReload = logConfiguration.validateCache();
    return isReload;
  }

}
