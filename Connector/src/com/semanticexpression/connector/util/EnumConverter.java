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

package com.semanticexpression.connector.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.semanticexpression.connector.server.repository.ConnectionPool;
import com.semanticexpression.connector.server.repository.Jdbc;

public class EnumConverter
{
  public static void main(String[] args) throws Exception
  {
    ConnectionPool connectionPool = new ConnectionPool("jdbc:derby://localhost:1527/Repository", 5);
    Connection connection = connectionPool.getConnection();
    try
    {
      StringBuilder s = new StringBuilder();
      s.append("select id, serializable_value from property where serializable_value is not null order by id");
      PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
      try
      {
        connection.setAutoCommit(false);
        ResultSet resultSet = Jdbc.executeQuery(preparedStatement);
        while (Jdbc.next(resultSet))
        {
          int id = Jdbc.getInt(resultSet, 1);
          Blob blob = Jdbc.getBlob(resultSet, 2);

          Enum<?> value = (Enum<?>)readSerializable(blob);
          String enumName = value.getClass().getName();
          
          enumName = enumName.substring(Math.max(enumName.lastIndexOf('$'), enumName.lastIndexOf('.')) + 1);
          String enumValue = value.name();
          writeEnum(connection, id, enumName, enumValue);
        }
        connection.setAutoCommit(true);
      }
      finally
      {
        preparedStatement.close();
      }
    }
    finally
    {
      connectionPool.putConnection(connection);
    }
  }

  public static Object readSerializable(Blob serializableValue)
  {
    Object rowValue;
    try
    {
      ObjectInputStream objectInputStream = new ObjectInputStream(serializableValue.getBinaryStream());
      try
      {
        rowValue = objectInputStream.readObject();
      }
      finally
      {
        objectInputStream.close();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
    catch (ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }
    return rowValue;
  }

  public static byte[] writeSerializable(Object value)
  {
    try
    {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(value);
      return byteArrayOutputStream.toByteArray();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static boolean writeBlob(Connection connection, int id, byte[] bytes)
  {
    StringBuilder s = new StringBuilder();
    s.append("update property set serializable_value = ? where id = ?");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, bytes, id);
      return updateCount == 1;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public static boolean writeEnum(Connection connection, int id, String enumName, String enumValue)
  {
    StringBuilder s = new StringBuilder();
    String enumSpec = enumName + ":" + enumValue;
    s.append("update property set enum_value = ? where id = ?");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, enumSpec, id);
      return updateCount == 1;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

}
