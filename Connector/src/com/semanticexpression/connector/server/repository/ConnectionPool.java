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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionPool
{
  private int allocatedConnectionCount;
  private BlockingQueue<Connection> connections = new LinkedBlockingQueue<Connection>();
  private int maximumConnectionCount;
  private String url;

  public ConnectionPool(String url, int maximumConnectionCount)
  {
    this.url = url;
    this.maximumConnectionCount = maximumConnectionCount;
  }

  private synchronized Connection allocateConnection()
  {
    Connection connection = null;
    if (allocatedConnectionCount < maximumConnectionCount)
    {
      connection = Jdbc.open(url);
      allocatedConnectionCount++;
    }
    return connection;
  }

  public Connection getConnection()
  {
    Connection connection = connections.poll();
    if (connection == null)
    {
      connection = allocateConnection();
      if (connection == null)
      {
        try
        {
          connection = connections.take();
        }
        catch (InterruptedException e)
        {
          throw new RuntimeException(e);
        }
      }
    }
    return connection;
  }

  public void putConnection(Connection connection)
  {
    try
    {
      if (Jdbc.getAutoCommit(connection) == false)
      {
        Jdbc.rollback(connection);
        Jdbc.setAutoCommit(connection, true);
      }
      connections.put(connection);
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

}
