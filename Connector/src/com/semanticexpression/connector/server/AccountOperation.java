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

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.server.thirdparty.BCrypt;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.exception.DuplicateUserNameException;
import com.semanticexpression.connector.shared.exception.InvalidUserNameLengthException;

public class AccountOperation extends BaseOperation
{

  public AccountOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public Credential createAccount(String userName, String password, String emailAddress, String personalSecurityQuestion, String personalSecurityAnswer) throws DuplicateUserNameException, InvalidUserNameLengthException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Jdbc.setAutoCommit(connection, false);

      if (userName.length() > Repository.MAX_NAME_LENGTH)
      {
        throw new InvalidUserNameLengthException(userName, Repository.MAX_NAME_LENGTH);
      }

      Id userId = repository.retrieveEntityIgnoreCase(connection, EntityType.USER, userName);
      if (userId != null)
      {
        throw new DuplicateUserNameException(userName);
      }

      Date date = new Date();
      userId = repository.createEntity(connection, EntityType.USER, userName, null, Repository.SYSTEM_USER_ID, date);
      createProperty(connection, userId, Keys.ENCRYPTED_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()), date, userId);
      createProperty(connection, userId, Keys.EMAIL_ADDRESS, emailAddress, date, userId);
      createProperty(connection, userId, Keys.PERSONAL_SECURITY_QUESTION, personalSecurityQuestion, date, userId);
      createProperty(connection, userId, Keys.PERSONAL_SECURITY_ANSWER, personalSecurityAnswer, date, userId);

      Credential credential = createCredential(connection, userId, userName);
      Jdbc.setAutoCommit(connection, true);
      return credential;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}
