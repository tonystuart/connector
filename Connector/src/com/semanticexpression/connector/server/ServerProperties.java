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

public class ServerProperties
{
  private Configuration configuration;
  
  public ServerProperties(String fileName)
  {
    configuration = new Configuration(fileName);
  }

  public String getApplicationBaseUrl()
  {
    return configuration.get("applicationBaseUrl");
  }

  public int getAuthenticationTokenLength()
  {
    return configuration.getInt("authenticationTokenLength");
  }

  public int getCredentialTimeoutMillis()
  {
    return configuration.getInt("credentialTimeoutMillis");
  }

  public String getDatabaseUrl()
  {
    return configuration.get("databaseUrl");
  }

  public String getIndexDirectory()
  {
    return configuration.get("indexDirectory");
  }

  public String getLogFileName()
  {
    return configuration.get("logFileName", null);
  }

  public int getLogLevel()
  {
    return configuration.getInt("logLevel");
  }

  public LinkedHashMap<String, String> getLogPatterns()
  {
    return configuration.getAll("logPattern.*");
  }

  public String getMailerConfigurationFileName()
  {
    return configuration.get("mailerConfigurationFileName", null);
  }

  public int getMaximumConnections()
  {
    return configuration.getInt("maximumConnections");
  }

  public int getMaximumUploadSize()
  {
    return configuration.getInt("maximumUploadSize");
  }

  public int getStatusQueueTimeoutMillis()
  {
    return configuration.getInt("statusQueueTimeoutMillis");
  }

  public String getTemporaryDirectory()
  {
    return configuration.get("temporaryDirectory");
  }

  public String getWorkflowCompletionBody()
  {
    return configuration.get("workflowCompletionBody");
  }

  public String getWorkflowCompletionSubject()
  {
    return configuration.get("workflowCompletionSubject");
  }

  public String getWorkflowRejectionBody()
  {
    return configuration.get("workflowRejectionBody");
  }

  public String getWorkflowRejectionSubject()
  {
    return configuration.get("workflowRejectionSubject");
  }

  public String getWorkflowReadyBody()
  {
    return configuration.get("workflowReadyBody");
  }

  public String getWorkflowReadySubject()
  {
    return configuration.get("workflowReadySubject");
  }

  public boolean isAllowGuestAccess()
  {
    return configuration.getBoolean("isAllowGuestAccess");
  }

  public int getAccountCreationOptions()
  {
    return configuration.getInt("accountCreationOptions");
  }
}
