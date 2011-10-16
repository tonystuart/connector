package com.semanticexpression.connector.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.shared.AdminRequest;
import com.semanticexpression.connector.shared.AdminResult;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.exception.AuthenticationException;

public class AdminOperation extends BaseOperation
{
  private static final String UPDATE_COUNT = "UPDATE_COUNT";

  protected AdminOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  private AdminResult executeAdminRequest(Connection connection, AdminRequest adminRequest)
  {
    String command = adminRequest.getCommand();
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, command);
    try
    {
      ArrayList<String> columnNames = new ArrayList<String>();
      List<ModelData> rows = new LinkedList<ModelData>();
      boolean isQuery = Jdbc.execute(preparedStatement);
      if (isQuery)
      {
        ResultSet resultSet = Jdbc.getResultSet(preparedStatement);
        ResultSetMetaData metaData = Jdbc.getMetaData(resultSet);
        int columnCount = Jdbc.getColumnCount(metaData);
        for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++)
        {
          String columnName = Jdbc.getColumnName(metaData, columnNumber);
          columnNames.add(columnName);
        }
        int rowOffset = 0;
        while (Jdbc.next(resultSet) && rowOffset < 50)
        {
          BaseModelData row = new BaseModelData();
          for (int columnOffset = 0, columnNumber = 1; columnOffset < columnCount; columnOffset++, columnNumber++)
          {
            String columnName = columnNames.get(columnOffset);
            Object value = Jdbc.getObject(resultSet, columnNumber);
            row.set(columnName, value);
          }
          rows.add(row);
          rowOffset++;
        }
      }
      else
      {
        int updateCount = Jdbc.executeUpdate(preparedStatement);
        columnNames.add(UPDATE_COUNT);
        BaseModelData row = new BaseModelData();
        row.set(UPDATE_COUNT, updateCount);
        rows.add(row);
      }
      AdminResult adminResult = new AdminResult(columnNames, rows);
      return adminResult;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public AdminResult executeAdminRequest(String authenticationToken, AdminRequest adminRequest) throws AuthenticationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateWriteAuthentication(connection, authenticationToken);
      Boolean isAdministrator = (Boolean)repository.retrieveProperty(connection, userId, Keys.IS_ADMINISTRATOR, null, false);
      if (!isAdministrator)
      {
        throw new AuthenticationException();
      }
      AdminResult adminResult = executeAdminRequest(connection, adminRequest);
      return adminResult;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}
