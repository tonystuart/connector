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

package com.semanticexpression.connector.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.semanticexpression.connector.client.services.ClearStatusMessageServiceRequest;
import com.semanticexpression.connector.client.services.DisplayStatusMessageServiceRequest;
import com.semanticexpression.connector.client.wiring.Directory;

public final class ClearStatusCallback<T> implements AsyncCallback<T>
{
  private AsyncCallback<T> callback;

  public ClearStatusCallback(String message, AsyncCallback<T> callback)
  {
    this.callback = callback;
    Directory.getServiceBus().invoke(new DisplayStatusMessageServiceRequest(message));
  }

  private void clearStatus()
  {
    Directory.getServiceBus().invoke(new ClearStatusMessageServiceRequest());
  }

  @Override
  public void onFailure(Throwable caught)
  {
    clearStatus();
    callback.onFailure(caught);
  }

  @Override
  public void onSuccess(T result)
  {
    clearStatus();
    callback.onSuccess(result);
  }
  
}
