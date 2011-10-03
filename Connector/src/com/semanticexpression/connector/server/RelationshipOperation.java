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
import java.util.List;

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Relationship;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;

public class RelationshipOperation extends BaseOperation
{

  protected RelationshipOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  private List<Relationship> getRelationships(Connection connection, String authenticationToken, Id contentId) throws AuthenticationException, InvalidContentIdException, AuthorizationException
  {
    List<Relationship> relationships = null;
    
    Id userId = validateReadAuthentication(connection, authenticationToken);

    if (repository.isExistingEntity(connection, contentId, EntityType.CONTENT)) // return null to indicate unsaved content
    {
      validateReadAccess(connection, userId, contentId);
      relationships = repository.getRelationships(connection, contentId);
    }
    
    return relationships;
  }

  public List<Relationship> getRelationships(String authenticationToken, Id contentId) throws AuthenticationException, InvalidContentIdException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      List<Relationship> relationships = getRelationships(connection, authenticationToken, contentId);
      return relationships;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}
