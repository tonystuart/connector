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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.semanticexpression.connector.server.Log;
import com.semanticexpression.connector.server.ParameterSubstituter;
import com.semanticexpression.connector.server.ServerProperties;

public class RepositoryFactory
{
  private ServerProperties serverProperties;

  public RepositoryFactory(ServerProperties serverProperties)
  {
    this.serverProperties = serverProperties;
  }

  public Repository create()
  {
    String databaseUrl = serverProperties.getDatabaseUrl();
    String rootPathName = serverProperties.getRootPathName();
    int maximumConnections = serverProperties.getMaximumConnections();
    LinkedHashMap<String, String> repositoryDefinitions = serverProperties.getRepositoryDefinitions();
    LinkedHashMap<String, String> allowedSqlStates = serverProperties.getAllowedSqlStates();
    databaseUrl = databaseUrl.replace("{{ROOT}}", rootPathName);
    Repository repository = new Repository(databaseUrl, maximumConnections);
    initialize(repository, repositoryDefinitions, allowedSqlStates);
    return repository;
  }

  private String formatMessage(String sql)
  {
    int delimiter = sql.replaceAll("[\n\r]", "\n").indexOf('\n');
    if (delimiter == -1)
    {
      delimiter = sql.length();
    }
    String message = sql.substring(0, delimiter);
    return message;
  }

  private void initialize(Connection connection, LinkedHashMap<String, String> repositoryDefinitions, LinkedHashMap<String, String> allowedSqlStates)
  {
    ParameterSubstituter parameterSubstituter = new ParameterSubstituter();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("MAX_NAME_LENGTH", Repository.MAX_NAME_LENGTH);
    parameters.put("SYSTEM_USER_ID", Repository.SYSTEM_USER_ID.getId());
    parameters.put("USER_ENTITY_TYPE", EntityType.USER);
    parameters.put("SYSTEM_USER_NAME", quote(Repository.SYSTEM_USER_NAME));
    parameters.put("DATE_MILLIS", System.currentTimeMillis());

    for (String repositoryDefinition : repositoryDefinitions.values())
    {
      String sql = parameterSubstituter.substitute(parameters, repositoryDefinition);
      PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, sql);
      try
      {
        preparedStatement.executeUpdate();
        String message = formatMessage(sql);
        Log.info("RepositoryFactory.initialize: executed %s", message);
      }
      catch (SQLException e)
      {
        String sqlState = e.getSQLState();
        if (!allowedSqlStates.containsValue(sqlState))
        {
          throw new RuntimeException(e);
        }
      }
      finally
      {
        Jdbc.close(preparedStatement);
      }
    }
  }

  public void initialize(Repository repository, LinkedHashMap<String, String> repositoryDefinitions, LinkedHashMap<String, String> allowedSqlStates)
  {
    ConnectionPool connectionPool = repository.getConnectionPool();
    Connection connection = connectionPool.getConnection();
    try
    {
      initialize(connection, repositoryDefinitions, allowedSqlStates);
    }
    finally
    {
      connectionPool.putConnection(connection);
    }

  }

  private String quote(String value)
  {
    StringBuilder quotedValue = new StringBuilder("'");
    int length = value.length();
    for (int i = 0; i < length; i++)
    {
      char c = value.charAt(i);
      if (c == '\'')
      {
        quotedValue.append("'");
      }
      quotedValue.append(c);
    }
    quotedValue.append("'");
    return quotedValue.toString();
  }

}
