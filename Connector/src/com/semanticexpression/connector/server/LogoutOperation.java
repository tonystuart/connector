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

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.shared.Id;

public class LogoutOperation extends BaseOperation
{

  public LogoutOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public void logout(String authenticationToken)
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      if (authenticationToken != null)
      {
        Id session = repository.retrieveEntity(connection, EntityType.CREDENTIAL, authenticationToken);
        if (session != null)
        {
          logout(connection, session);
        }
      }
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }
}
