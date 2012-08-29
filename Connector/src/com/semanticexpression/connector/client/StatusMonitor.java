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

package com.semanticexpression.connector.client;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.Timer;
import com.semanticexpression.connector.client.events.StatusEvent;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.ControlStatus;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Status;

public class StatusMonitor
{
  private FailureRetryTimer failureRetryTimer;
  private Id monitorId;

  public void connect()
  {
    monitorId = IdManager.createIdentifier();
    // Swap comment on following two lines to toggle StatusMonitor operation
    //System.err.println("StatusMonitor.connect: ******** NOT CONNECTING ********");
    getStatus();
  }

  private FailureRetryTimer getFailureRetryTimer()
  {
    if (failureRetryTimer == null)
    {
      failureRetryTimer = new FailureRetryTimer();
    }
    return failureRetryTimer;
  }

  public Id getMonitorId()
  {
    return monitorId;
  }

  private void getStatus()
  {
    Directory.getConnectorService().getStatus(Utility.getAuthenticationToken(), monitorId, new StatusCallback(monitorId));
  }

  private void processFailure(Throwable caught, Id subjectMonitorId)
  {
    sendControlStatus("Disconnect (T)", caught.getMessage(), subjectMonitorId);
    getFailureRetryTimer().schedule(5000);
  }

  private void processSuccess(List<Status> statusList)
  {
    if (statusList == null)
    {
      sendControlStatus("Connect (S)", "New Monitor", monitorId);
    }
    else if (statusList.size() == 0) {
      // server timeout
    }
    else
    {
      StatusEvent statusEvent = new StatusEvent(statusList);
      Directory.getEventBus().post(statusEvent);
    }
    getStatus();
  }

  private void sendControlStatus(String action, String title, Id subjectMonitorId)
  {
    List<Status> statusList = new LinkedList<Status>();
    Status status = new ControlStatus();
    status.set(Keys.CREATED_AT, new Date());
    status.set(Keys.CREATED_BY, "System");
    status.set(Keys.ACTION, action);
    status.set(Keys.TITLE, title);
    status.set(Keys.CONTENT_ID, subjectMonitorId);
    statusList.add(status);

    StatusEvent statusEvent = new StatusEvent(statusList);
    Directory.getEventBus().post(statusEvent);
  }

  private final class FailureRetryTimer extends Timer
  {
    @Override
    public void run()
    {
      getStatus();
    }
  }

  public class StatusCallback extends FailureReportingAsyncCallback<List<Status>>
  {
    private Id requestingMonitorId;

    public StatusCallback(Id monitorId)
    {
      this.requestingMonitorId = monitorId;
    }

    @Override
    public void onFailure(Throwable caught)
    {
      if (requestingMonitorId.equals(monitorId))
      {
        processFailure(caught, requestingMonitorId);
      }
      else
      {
        sendControlStatus("Disconnect (F)", "Old Monitor", requestingMonitorId); // a response to a request that was made prior to login
      }
    }

    @Override
    public void onSuccess(List<Status> statusList)
    {
      if (requestingMonitorId.equals(monitorId))
      {
        processSuccess(statusList);
      }
      else
      {
        sendControlStatus("Disconnect (S)", "Old Monitor", requestingMonitorId); // a response to a request that was made prior to login
      }
    }

  }
}