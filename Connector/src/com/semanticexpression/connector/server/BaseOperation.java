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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.server.repository.Repository.AccessAuthorization;
import com.semanticexpression.connector.server.repository.Repository.AccessRequested;
import com.semanticexpression.connector.server.repository.Repository.PropertyBlob;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.UpdateStatus;
import com.semanticexpression.connector.shared.Credential.AuthenticationType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;

public class BaseOperation
{
  public static final String ACTION_CHECK = "Checked";
  public static final String ACTION_CREATED = "Created";
  public static final String ACTION_PUBLISHED = "Published";
  public static final String ACTION_UPDATED = "Updated";

  protected Mailer mailer;
  protected Repository repository;
  protected SearchEngine searchEngine;
  protected ServerProperties serverProperties;
  protected StatusQueues statusQueues;
  protected File temporaryDirectory;

  protected BaseOperation(ServerContext serverContext)
  {
    this.serverProperties = serverContext.getServerProperties();
    this.repository = serverContext.getRepository();
    this.searchEngine = serverContext.getSearchEngine();
    this.mailer = serverContext.getMailer();
    this.statusQueues = serverContext.getStatusManager();
    this.temporaryDirectory = serverContext.getTemporaryDirectory();
  }

  private boolean canAccess(Connection connection, Id userId, Id contentId, AccessRequested accessRequested)
  {
    AccessAuthorization accessAuthorization;
    if (userId.equals(Repository.SYSTEM_USER_ID))
    {
      accessAuthorization = AccessAuthorization.GRANTED;
    }
    else
    {
      accessAuthorization = repository.checkEntityAccess(connection, userId, contentId, accessRequested);
      if (accessAuthorization == AccessAuthorization.INDETERMINATE)
      {
        accessAuthorization = repository.checkUserAuthority(connection, userId, contentId, accessRequested);
        if (accessAuthorization == AccessAuthorization.INDETERMINATE)
        {
          accessAuthorization = repository.checkGroupAuthority(connection, userId, contentId, accessRequested);
        }
      }
    }
    Log.debug("BaseOperation.canAccess: userId=%s, contentId=%s, %s %s", userId, contentId, accessRequested, accessAuthorization);
    return accessAuthorization == AccessAuthorization.GRANTED;
  }

  public boolean canRead(Connection connection, Id userId, Id contentId)
  {
    return canAccess(connection, userId, contentId, AccessRequested.READ);
  }

  public boolean canWrite(Connection connection, Id userId, Id contentId)
  {
    return canAccess(connection, userId, contentId, AccessRequested.WRITE);
  }

  protected Credential createCredential(Connection connection, Id userId, String userName)
  {
    String authenticationToken;

    do
    {
      authenticationToken = Utility.createRandomString("0123456789abcdef", serverProperties.getAuthenticationTokenLength());
    }
    while (repository.retrieveEntity(connection, EntityType.CREDENTIAL, authenticationToken) != null);

    Date date = new Date();
    Id credentialId = repository.createEntity(connection, EntityType.CREDENTIAL, authenticationToken, null, userId, date);
    createProperty(connection, credentialId, Keys.USER_ID, userId, date, userId);
    createProperty(connection, credentialId, Keys.ACCESS_TIMESTAMP, date, date, userId);
    int accountCreationOptions = serverProperties.getAccountCreationOptions();
    Credential credential = new Credential(AuthenticationType.AUTHENTICATED, authenticationToken, userName, accountCreationOptions);
    return credential;
  }

  protected void createProperty(Connection connection, Id identity, String name, Object value, Date validFrom, Id userId)
  {
    repository.createProperty(connection, identity, name, value, validFrom, userId);
  }

  protected Credential getCredential(Connection connection, String authenticationToken)
  {
    Credential credential;
    int accountCreationOptions = serverProperties.getAccountCreationOptions();
    Id userId = getCurrentUserId(connection, authenticationToken);
    if (userId == null)
    {
      boolean isAllowGuestAccess = serverProperties.isAllowGuestAccess();
      if (isAllowGuestAccess)
      {
        credential = new Credential(AuthenticationType.UNAUTHENTICATED, null, Credential.GUEST_USER, accountCreationOptions);
      }
      else
      {
        credential = new Credential(AuthenticationType.AUTHENTICATION_REQUIRED, null, null, accountCreationOptions);
      }
    }
    else
    {
      String userName = repository.retrieveEntityName(connection, userId);
      credential = new Credential(AuthenticationType.AUTHENTICATED, authenticationToken, userName, accountCreationOptions);
    }
    return credential;
  }

  private Id getCurrentUserId(Connection connection, String authenticationToken)
  {
    Id userId = null;
    if (authenticationToken != null)
    {
      Id credentialId = repository.retrieveEntity(connection, EntityType.CREDENTIAL, authenticationToken);
      if (credentialId != null)
      {
        if (isCredentialExpired(connection, credentialId))
        {
          logout(connection, credentialId);
        }
        else
        {
          userId = (Id)retrieveProperty(connection, credentialId, Keys.USER_ID);
          if (userId == null)
          {
            logout(connection, credentialId);
          }
        }
      }
    }
    return userId;
  }

  protected Id getCurrentUserId(String authenticationToken)
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      return getCurrentUserId(connection, authenticationToken);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  protected Id getCurrentUserIdWithGuest(String authenticationToken)
  {
    Id userId = getCurrentUserId(authenticationToken);
    if (userId == null)
    {
      userId = Repository.GUEST_USER_ID;
    }
    return userId;
  }

  protected String getUpdateStatusAction(boolean isNewEntity)
  {
    return isNewEntity ? ACTION_CREATED : ACTION_UPDATED;
  }

  boolean isCredentialExpired(Connection connection, Id session)
  {
    boolean isCredentialExpired = false;
    long credentialTimeoutInterval = serverProperties.getCredentialTimeoutMillis();
    if (credentialTimeoutInterval > 0)
    {
      Date accessTimestamp = (Date)retrieveProperty(connection, session, Keys.ACCESS_TIMESTAMP);
      if (accessTimestamp.getTime() + credentialTimeoutInterval < System.currentTimeMillis())
      {
        isCredentialExpired = true;
      }
    }
    return isCredentialExpired;
  }

  protected void logout(Connection connection, Id session)
  {
    repository.deleteEntity(connection, session);
  }

  protected String normalizeText(String text)
  {
    // (?s) - turn on DOTALL mode to match line separators
    // \\<.+?\\> - reluctant match of tag including line separators
    // \\s+ - multiple whitespace characters
    return text.replaceAll("(?s)\\<.+?\\>", " ").replaceAll("\\s+", " ");
  }

  public void postStatus(Connection connection, List<Status> statusList, Id sourceMonitorId)
  {
    int totalQueues = statusQueues.size();
    int deletedQueues = 0;
    int postedQueues = 0;
    int accessChecks = 0;
    int skippedQueues = 0;

    for (Status status : statusList)
    {
      int statusQueueTimeoutMillis = serverProperties.getStatusQueueTimeoutMillis();
      for (Entry<Id, StatusQueue> entry : statusQueues.entrySet())
      {
        Id monitorId = entry.getKey();
        StatusQueue statusQueue = entry.getValue();
        if (statusQueue.isClientTimeout(statusQueueTimeoutMillis))
        {
          deletedQueues++;
          statusQueues.remove(monitorId);
        }
        else if (monitorId.equals(sourceMonitorId))
        {
          skippedQueues++;
        }
        else if (status instanceof UpdateStatus)
        {
          accessChecks++;
          if (canRead(connection, statusQueue.getUserId(), ((UpdateStatus)status).getContentId()))
          {
            postedQueues++;
            statusQueue.add(status);
          }
          else if (statusQueue.isServerTimeout(statusQueueTimeoutMillis))
          {
            UpdateStatus s = new UpdateStatus(new Date(), new Id(0), Repository.SYSTEM_USER_NAME, "Client Health Check", ACTION_CHECK);
            statusQueue.add(s);
          }
        }
        else
        {
          throw new IllegalArgumentException();
        }
      }
    }

    Log.debug("BaseOperation.postStatus: totalQueues=%d, deletedQueues=%d, skippedQueues=%d, accessChecks=%d, postedQueues=%d, listSize=%s", totalQueues, deletedQueues, skippedQueues, accessChecks, postedQueues, statusList.size());
  }

  protected Object retrieveProperty(Connection connection, Content content, String name)
  {
    Object value = content.get(name);
    if (value == null)
    {
      Id contentId = content.getId();
      value = retrieveProperty(connection, contentId, name);
    }
    return value;
  }

  protected Object retrieveProperty(Connection connection, Id contentId, String name)
  {
    return repository.retrieveProperty(connection, contentId, name);
  }

  protected Object retrieveProperty(Connection connection, Id contentId, String name, Date presentAt)
  {
    return repository.retrieveProperty(connection, contentId, name, presentAt);
  }

  /**
   * Do not use client supplied MIME type, it is unreliable.
   */
  public void updateImageDimensions(Connection connection, Id contentId, Date validFrom, Id userId)
  {
    try
    {
      PropertyBlob imageBlob = repository.retrievePropertyBlob(connection, contentId, Keys.IMAGE, validFrom);
      try
      {
        InputStream imageInputStream = imageBlob.getBinaryStream();
        try
        {
          BufferedImage bufferedImage = ImageIO.read(imageInputStream);
          int width = bufferedImage.getWidth();
          int height = bufferedImage.getHeight();
          repository.updateProperty(connection, contentId, Keys.IMAGE_NATIVE_WIDTH, width, validFrom, userId);
          repository.updateProperty(connection, contentId, Keys.IMAGE_NATIVE_HEIGHT, height, validFrom, userId);
        }
        finally
        {
          imageInputStream.close();
        }
      }
      finally
      {
        imageBlob.close();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  protected void updateProperties(Connection connection, Id userId, Content content, Date date) throws AuthorizationException
  {
    Id contentId = content.getId();
    validateWriteAccess(connection, userId, contentId);
    updatePropertiesAuthorized(connection, userId, content, date);
  }

  protected void updatePropertiesAuthorized(Connection connection, Id userId, Content content, Date date)
  {
    Id contentId = content.getId();
    Map<String, Object> properties = content.getProperties();
    for (Entry<String, Object> property : properties.entrySet())
    {
      String propertyName = property.getKey();
      Object propertyValue = property.getValue();
      repository.updateProperty(connection, contentId, propertyName, propertyValue, date, userId);
      if (Keys.IMAGE.equals(propertyName) && (content.get(Keys.IMAGE_NATIVE_HEIGHT) == null || content.get(Keys.IMAGE_NATIVE_WIDTH) == null))
      {
        updateImageDimensions(connection, contentId, date, userId);
      }
    }
  }

  protected void updateStatusList(Connection connection, Id userId, Id contentId, Date date, String action, List<Status> statusList)
  {
    String title = (String)retrieveProperty(connection, contentId, Keys.TITLE);
    String userName = repository.retrieveEntityName(connection, userId);
    UpdateStatus updateStatus = new UpdateStatus(date, contentId, userName, title, action);
    statusList.add(updateStatus);
  }

  public void validateReadAccess(Connection connection, Id userId, Id contentId) throws AuthorizationException
  {
    if (!canRead(connection, userId, contentId))
    {
      throw new AuthorizationException(contentId);
    }
  }

  public Id validateReadAccess(Connection connection, String authenticationToken, Id contentId) throws AuthenticationException, AuthorizationException
  {
    Id userId = validateReadAuthentication(connection, authenticationToken);
    validateReadAccess(connection, userId, contentId);
    return userId;
  }

  protected Id validateReadAuthentication(Connection connection, String authenticationToken) throws AuthenticationException
  {
    Id userId = getCurrentUserId(connection, authenticationToken);
    if (userId == null)
    {
      boolean isAllowGuestAccess = serverProperties.isAllowGuestAccess();
      if (isAllowGuestAccess)
      {
        userId = Repository.GUEST_USER_ID;
      }
      else
      {
        throw new AuthenticationException();
      }
    }
    return userId;
  }

  public void validateWriteAccess(Connection connection, Id userId, Id contentId) throws AuthorizationException
  {
    if (!canWrite(connection, userId, contentId))
    {
      throw new AuthorizationException(contentId);
    }
  }

  protected Id validateWriteAuthentication(Connection connection, String authenticationToken) throws AuthenticationException
  {
    Id userId = getCurrentUserId(connection, authenticationToken);
    if (userId == null)
    {
      throw new AuthenticationException();
    }
    return userId;
  }

  protected void writeIndex(Connection connection, Content content)
  {
    HashMap<String, String> indexedProperties = new HashMap<String, String>();

    String title = (String)retrieveProperty(connection, content, Keys.TITLE);
    if (title != null)
    {
      indexedProperties.put(Keys.TITLE, title);
    }

    String text = (String)retrieveProperty(connection, content, Keys.TEXT);
    if (text != null)
    {
      String normalizedText = normalizeText(text);
      indexedProperties.put(Keys.TEXT, normalizedText);
    }

    String caption = (String)retrieveProperty(connection, content, Keys.CAPTION);
    if (caption != null)
    {
      indexedProperties.put(Keys.CAPTION, caption);
    }

    Id contentId = content.getId();
    String createdByName = repository.retrieveEntityCreatedByName(connection, contentId);
    indexedProperties.put(Keys.CREATED_BY, createdByName);

    String idValue = contentId.formatString();
    searchEngine.update(Keys.LUCENE_ID, idValue, indexedProperties.entrySet());
    searchEngine.commit();
  }

  protected void writeIndex(Connection connection, List<Content> contents)
  {
    for (Content content : contents)
    {
      writeIndex(connection, content);
    }
  }

}
