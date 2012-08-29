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

import java.util.LinkedList;
import java.util.List;


import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Status;

public class StatusOperation extends BaseOperation
{

  protected StatusOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public List<Status> getStatus(String authenticationToken, Id monitorId, String remoteAddr)
  {
    try
    {
      List<Status> statusList = null;
      StatusQueue statusQueue = statusQueues.get(monitorId);
      if (statusQueue == null)
      {
        Log.info("StatusOperation.getStatus: creating queue, monitorId=%s, remoteAddr=%s", monitorId, remoteAddr);
        Id userId = getCurrentUserIdWithGuest(authenticationToken);
        statusQueue = new StatusQueue(userId);
        statusQueues.put(monitorId, statusQueue);
        // return null to indicate new queue
      }
      else
      {
        statusList = new LinkedList<Status>();
        if (statusQueue.size() > 0)
        {
          statusQueue.drainTo(statusList);
        }
        else
        {
          Status status = statusQueue.take();
          if (status != null) // return empty list to indicate server timeout
          {
            statusList.add(status);
          }
        }
      }
      return statusList;
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

}
