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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EmbeddedDerbyTest
{
  public static void main(String[] args)
  {
      try
      {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        boolean isNew = !new File("EmbeddedDatabase").exists();
        Connection connection1 = DriverManager.getConnection("jdbc:derby:EmbeddedDatabase;create=true");
        if (isNew)
        {
          Statement statement0 = connection1.createStatement();
          statement0.executeUpdate("create table test1(a varchar(32))");
          statement0.close();
        }
        Statement statement1 = connection1.createStatement();
        statement1.executeUpdate("insert into test1 values('hello world')");
        Connection connection2 = DriverManager.getConnection("jdbc:derby:EmbeddedDatabase;create=true");
        Statement statement2 = connection2.createStatement();
        ResultSet resultSet = statement2.executeQuery("select * from test1");
        while (resultSet.next())
        {
          System.out.println(resultSet.getString(1));
        }
        statement1.close();
        statement2.close();
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