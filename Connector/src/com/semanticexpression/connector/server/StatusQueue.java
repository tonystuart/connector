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

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Status;

public class StatusQueue extends LinkedBlockingQueue<Status> implements Comparable<StatusQueue>
{
  private static AtomicInteger count = new AtomicInteger();
  private long clientTime; // time server last returned status to client (i.e. the token is with the client)
  private int queueNumber;
  private long serverTime; // time client last requested status from server (i.e. the token is with the server)
  private Id userId;

  public StatusQueue(Id userId)
  {
    this.userId = userId;
    queueNumber = count.getAndAdd(1);
  }

  @Override
  public int compareTo(StatusQueue that)
  {
    return this.queueNumber - that.queueNumber;
  }

  @Override
  public int drainTo(Collection<? super Status> c)
  {
    int statusList = super.drainTo(c);
    clientTime = System.currentTimeMillis();
    return statusList;
  }

  public Id getUserId()
  {
    return userId;
  }

  public boolean isClientTimeout(int timeoutMillis)
  {
    return clientTime != 0 && ((clientTime + timeoutMillis) < System.currentTimeMillis());
  }

  public boolean isServerTimeout(int timeoutMillis)
  {
    return serverTime != 0 && ((serverTime + timeoutMillis) < System.currentTimeMillis());
  }

  @Override
  public Status take() throws InterruptedException
  {
    clientTime = 0;
    serverTime = System.currentTimeMillis();
    Status status = super.take();
    clientTime = System.currentTimeMillis();
    serverTime = 0;
    return status;
  }
}