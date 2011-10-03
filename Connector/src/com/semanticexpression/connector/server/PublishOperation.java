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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;
import com.semanticexpression.connector.shared.exception.PublicationException;

public class PublishOperation extends BaseOperation
{
  public PublishOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  private boolean isPublisher(Connection connection, Id userId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select boolean_value\n");
    s.append("from property as p\n");
    s.append("where p.entity_id = ?\n");
    s.append("and p.property_name = ?\n"); // Keys.IS_PUBLISHER

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Boolean booleanValue = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, userId.getId(), Keys.IS_PUBLISHER);
      if (Jdbc.next(resultSet))
      {
        booleanValue = Jdbc.getBoolean(resultSet, 1);
      }
      return booleanValue == null ? false : booleanValue;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private int publishContent(Connection connection, Id contentId, Date date)
  {
    StringBuilder s = new StringBuilder();
    s.append("update entity\n");
    s.append("set published_at = ?\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, date.getTime(), contentId.getId());
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public void publishContent(Connection connection, Id userId, List<Id> contentIds, Date date, List<Status> statusList) throws InvalidContentIdException, PublicationException
  {
    if (!isPublisher(connection, userId))
    {
      throw new PublicationException(contentIds.get(0));
    }

    for (Id contentId : contentIds)
    {
      @SuppressWarnings("unchecked")
      List<Id> parts = (List<Id>)retrieveProperty(connection, contentId, Keys.PARTS);
      if (parts != null)
      {
        for (Id part : parts)
        {
          publishContent(connection, part, date);
          updateStatusList(connection, userId, part, date, ACTION_PUBLISHED, statusList);
        }
      }
      publishContent(connection, contentId, date);
      updateStatusList(connection, userId, contentId, date, ACTION_PUBLISHED, statusList);
    }
  }

  public void publishContent(String authenticationToken, List<Id> contentIds, Id monitorId) throws InvalidContentIdException, AuthenticationException, AuthorizationException, PublicationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateWriteAuthentication(connection, authenticationToken);
      Jdbc.setAutoCommit(connection, false);

      List<Status> statusList = new LinkedList<Status>();
      publishContent(connection, userId, contentIds, new Date(), statusList);
      postStatus(connection, statusList, monitorId);

      Jdbc.setAutoCommit(connection, true);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}