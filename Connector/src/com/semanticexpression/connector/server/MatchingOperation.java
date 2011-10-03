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

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.ServerException;

public class MatchingOperation extends BaseOperation
{

  public MatchingOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public BasePagingLoadResult<Association> getMatchingNames(String authenticationToken, String wildcard, MatchingNameType type, PagingLoadConfig pagingLoadConfig) throws ServerException, AuthenticationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateReadAuthentication(connection, authenticationToken);

      BasePagingLoadResult<Association> pagingLoadResult;

      switch (type)
      {
        case GROUP:
          pagingLoadResult = repository.getMatchingEntityNames(connection, wildcard, EntityType.GROUP, pagingLoadConfig);
          break;
        case KEYWORD:
          pagingLoadResult = repository.getMatchingKeywords(connection, wildcard, pagingLoadConfig);
          break;
        case PRIVATE_TAG:
          pagingLoadResult = repository.getMatchingTags(connection, userId, wildcard, MatchingNameType.PRIVATE_TAG, pagingLoadConfig);
          break;
        case PUBLIC_TAG:
          pagingLoadResult = repository.getMatchingTags(connection, userId, wildcard, MatchingNameType.PUBLIC_TAG, pagingLoadConfig);
          break;
        case USER:
          pagingLoadResult = repository.getMatchingEntityNames(connection, wildcard, EntityType.USER, pagingLoadConfig);
          break;
        default:
          throw new IllegalArgumentException("type=" + type);
      }

      return pagingLoadResult;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }
}
