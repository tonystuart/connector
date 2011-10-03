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

package com.semanticexpression.connector.client.wiring;

import com.google.gwt.core.client.GWT;
import com.semanticexpression.connector.client.StatusMonitor;
import com.semanticexpression.connector.client.frame.editor.ContentManager;
import com.semanticexpression.connector.client.rpc.ConnectorService;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync;
import com.semanticexpression.connector.client.rpc.ConnectorServiceClientProxy;

/**
 * Some people (me) consider singletons to be an anti-pattern. This class works
 * around some of the issues by providing a directory lookup service for
 * managing global instances.
 * <p/>
 * We do not use static initializers due to order-of-creation issues, e.g. if
 * ContentManager's constructor invokes getEventBus().
 */
public final class Directory
{
  private static ConnectorServiceAsync connectorService;
  private static ContentManager contentManager;
  private static EventBus eventBus;
  private static ServiceBus serviceBus;
  private static StatusMonitor statusMonitor;

  public synchronized static ConnectorServiceAsync getConnectorService()
  {
    if (connectorService == null)
    {
      connectorService = new ConnectorServiceClientProxy((ConnectorServiceAsync)GWT.create(ConnectorService.class));
    }
    return connectorService;
  }

  public synchronized static ContentManager getContentManager()
  {
    if (contentManager == null)
    {
      contentManager = new ContentManager();
    }
    return contentManager;
  }

  public synchronized static EventBus getEventBus()
  {
    if (eventBus == null)
    {
      eventBus = new EventBus();
    }
    return eventBus;
  }

  public synchronized static ServiceBus getServiceBus()
  {
    if (serviceBus == null)
    {
      serviceBus = new ServiceBus();
    }
    return serviceBus;
  }

  public synchronized static StatusMonitor getStatusMonitor()
  {
    if (statusMonitor == null)
    {
      statusMonitor = new StatusMonitor();
    }
    return statusMonitor;
  }

}
