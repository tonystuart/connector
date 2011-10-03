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
import com.semanticexpression.connector.server.thirdparty.BCrypt;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.exception.InvalidLoginCredentialsException;

public class LoginOperation extends BaseOperation
{

  public LoginOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public Credential login(String userName, String password) throws InvalidLoginCredentialsException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = repository.retrieveEntity(connection, EntityType.USER, userName);
      if (userId == null)
      {
        throw new InvalidLoginCredentialsException();
      }

      String encryptedPassword = (String)retrieveProperty(connection, userId, Keys.ENCRYPTED_PASSWORD);
      if (!BCrypt.checkpw(password, encryptedPassword))
      {
        throw new InvalidLoginCredentialsException();
      }

      Credential credential = createCredential(connection, userId, userName);
      return credential;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}
