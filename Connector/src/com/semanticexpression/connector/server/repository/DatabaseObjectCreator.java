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

package com.semanticexpression.connector.server.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.semanticexpression.connector.server.Log;

public abstract class DatabaseObjectCreator
{
  private String objectName;
  private StringBuilder sqlBuilder;

  public DatabaseObjectCreator(String objectName)
  {
    this.objectName = objectName.toUpperCase();
    this.sqlBuilder = new StringBuilder();
  }

  public void append(String definition)
  {
    sqlBuilder.append(definition);
    sqlBuilder.append("\n");
  }

  public boolean create(Connection connection)
  {
    boolean isCreated = false;
    
    String createSql = sqlBuilder.toString();

    if (!objectExists(connection))
    {
      Log.info("DatabaseObjectCreator.create: creating objectName=%s", objectName);
      PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, createSql);
      Jdbc.executeUpdate(preparedStatement);
      isCreated = true;
    }
    
    return isCreated;
  }

  protected abstract String getObjectExistenceTestSql();

  public String getObjectName()
  {
    return objectName;
  }

  protected boolean objectExists(Connection connection)
  {
    // TODO: Consider using Connection.getDatabaseMetaData as an alternative
    String objectExistenceTestSql = getObjectExistenceTestSql();
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, objectExistenceTestSql, objectName);
    ResultSet resultSet = Jdbc.executeQuery(preparedStatement);
    boolean objectExists = Jdbc.next(resultSet);
    return objectExists;
  }

}