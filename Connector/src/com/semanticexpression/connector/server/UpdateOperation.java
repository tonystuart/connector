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

import java.sql.Connection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;

public class UpdateOperation extends BaseOperation
{
  public UpdateOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public void updateContent(Connection connection, Id userId, List<Content> contents, Date date, List<Status> statusList) throws InvalidContentException, InvalidContentIdException, AuthorizationException
  {
    for (Content content : contents)
    {
      Id contentId = content.getId();
      boolean isNewEntity = repository.realiseEntity(connection, contentId, EntityType.CONTENT, null, null, userId, date);
      String action = getUpdateStatusAction(isNewEntity);
      updateStatusList(connection, userId, contentId, date, action, statusList);
    }

    for (Content content : contents)
    {
      Id contentId = content.getId();
      ContentType contentType = (ContentType)retrieveProperty(connection, content, Keys.CONTENT_TYPE);
      if (contentType == null)
      {
        throw new InvalidContentException(contentId, Keys.CONTENT_TYPE);
      }
      updateProperties(connection, userId, content, date);
    }
  }

  public void updateContent(String authenticationToken, List<Content> contents, Id monitorId) throws InvalidContentException, InvalidContentIdException, AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateWriteAuthentication(connection, authenticationToken);
      Jdbc.setAutoCommit(connection, false);
      List<Status> statusList = new LinkedList<Status>();
      updateContent(connection, userId, contents, new Date(), statusList);
      writeIndex(connection, contents);
      postStatus(connection, statusList, monitorId);
      Jdbc.setAutoCommit(connection, true);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  public void updateWorkflowTask(String authenticationToken, Id workflowContentId, Association workflowTask, Id monitorId) throws InvalidContentException, InvalidContentIdException, AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateWriteAuthentication(connection, authenticationToken);
      Jdbc.setAutoCommit(connection, false);
      Date date = new Date();
      Content workflowTaskContent = new Content(workflowTask.getId(), false);
      workflowTaskContent.assignFrom(workflowTask);
      updateProperties(connection, userId, workflowTaskContent, date);
      // Do not need to generate a Status update because the system generates one immediately after this
      Jdbc.setAutoCommit(connection, true);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}