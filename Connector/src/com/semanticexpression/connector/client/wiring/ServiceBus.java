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

public class ServiceBus
{
  private HashMap<Object, Object> serviceProviders = new HashMap<Object, Object>();

  public ServiceBus()
  {
  }

  public <T extends ServiceRequest> void addServiceProvider(Class<T> serviceRequestClass, ServiceProvider<T> serviceProvider)
  {
    Object previousServiceProvider = serviceProviders.put(serviceRequestClass, serviceProvider);
    if (previousServiceProvider != null)
    {
      throw new IllegalStateException("Provider already defined, serviceRequest=" + serviceRequestClass.getName() + ", serviceProvider=" + serviceProvider.getClass().getName());
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends ServiceRequest> T invoke(T serviceRequest)
  {
    ServiceProvider<T> serviceProvider = (ServiceProvider<T>)serviceProviders.get(serviceRequest.getClass());
    if (serviceProvider == null)
    {
      throw new IllegalStateException("No service provider, serviceRequest=" + serviceRequest.getClass().getName());
    }
    serviceProvider.onServiceRequest(serviceRequest);
    return serviceRequest; // for one step access to getters
  }

  public <T extends ServiceRequest> void removeServiceProvider(Class<T> serviceRequestClass)
  {
    serviceProviders.remove(serviceRequestClass);
  }

}
