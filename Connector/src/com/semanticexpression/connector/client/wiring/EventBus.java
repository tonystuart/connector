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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class EventBus
{
  private HashMap<Object, Object> listenerMap = new HashMap<Object, Object>();

  public EventBus()
  {
  }

  @SuppressWarnings("unchecked")
  public <T extends EventNotification> void addListener(Class<T> eventNotificationClass, EventListener<T> eventListener)
  {
    List<EventListener<T>> listeners = (List<EventListener<T>>)listenerMap.get(eventNotificationClass);
    if (listeners == null)
    {
      listeners = new LinkedList<EventListener<T>>();
      listenerMap.put(eventNotificationClass, listeners);
    }
    listeners.add(eventListener);
  }

  @SuppressWarnings("unchecked")
  public <T extends EventNotification> void post(T eventNotification)
  {
    List<EventListener<T>> listeners = (List<EventListener<T>>)listenerMap.get(eventNotification.getClass());
    if (listeners != null)
    {
      LinkedList<EventListener<T>> listenersSnapshot = new LinkedList<EventListener<T>>(listeners);
      for (EventListener<T> listener : listenersSnapshot)
      {
        listener.onEventNotification(eventNotification);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends EventNotification> void removeListener(Class<T> eventNotificationClass, EventListener<T> eventListener)
  {
    List<EventListener<T>> listeners = (List<EventListener<T>>)listenerMap.get(eventNotificationClass);
    if (listeners != null)
    {
      listeners.remove(eventListener);
    }
  }

}
