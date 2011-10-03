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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * See http://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-basic.html
 */
public class MySqlJdbcTest
{
  public static void main(String[] args)
  {
    try
    {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/connector?" + "user=connector&password=passw0rd");
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("select * from t1");
      while (resultSet.next())
      {
        String value = resultSet.getString(1);
        System.out.println(value);
      }
    }
    catch (InstantiationException e)
    {
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e)
    {
      throw new RuntimeException(e);
    }
    catch (ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }

  }
}
