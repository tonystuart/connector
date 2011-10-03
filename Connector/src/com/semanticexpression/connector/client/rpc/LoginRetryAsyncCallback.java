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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.semanticexpression.connector.client.account.LoginWindow;
import com.semanticexpression.connector.client.events.LoginEvent;
import com.semanticexpression.connector.client.events.LoginWindowCloseEvent;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.exception.AuthenticationException;

public class LoginRetryAsyncCallback<T> implements AsyncCallback<T>
{
  private AsyncCallback<T> callback;
  private LoginCallback loginCallback;

  public LoginRetryAsyncCallback(AsyncCallback<T> callback, LoginCallback loginCallback)
  {
    this.callback = callback;
    this.loginCallback = loginCallback;
  }

  @Override
  public void onFailure(Throwable caught)
  {
    if (caught instanceof AuthenticationException)
    {
      MessageBox.confirm("Login Required", "You must be logged in to the server to create or update content. Would you like to log in?", new LoginPromptResponseHandler());
    }
    else
    {
      callback.onFailure(caught);
    }
  }

  @Override
  public void onSuccess(T result)
  {
    callback.onSuccess(result);
  }

  public interface LoginCallback
  {
    public void onClose();

    public void onLogin(Credential credential);
  }

  public static class LoginCallbackAdapter implements LoginCallback
  {
    @Override
    public void onClose()
    {
    }

    @Override
    public void onLogin(Credential credential)
    {
    }
  }

  private final class LoginPromptResponseHandler implements Listener<MessageBoxEvent>
  {
    private EventListener<LoginEvent> loginListener;
    private LoginWindowCloseListener loginWindowCloseListener;

    @Override
    public void handleEvent(MessageBoxEvent be)
    {
      if (Dialog.YES.equals(be.getButtonClicked().getItemId()))
      {
        loginListener = new LoginListener();
        loginWindowCloseListener = new LoginWindowCloseListener();

        Directory.getEventBus().addListener(LoginEvent.class, loginListener);
        Directory.getEventBus().addListener(LoginWindowCloseEvent.class, loginWindowCloseListener);
        
        LoginWindow loginWindow = new LoginWindow(true);
        loginWindow.show();
      }
    }

    private final class LoginListener implements EventListener<LoginEvent>
    {
      @Override
      public void onEventNotification(LoginEvent eventNotification)
      {
        Directory.getEventBus().removeListener(LoginEvent.class, loginListener);
        Directory.getEventBus().removeListener(LoginWindowCloseEvent.class, loginWindowCloseListener);
        loginCallback.onLogin(eventNotification.getCredential());
      }
    }

    private final class LoginWindowCloseListener implements EventListener<LoginWindowCloseEvent>
    {
      @Override
      public void onEventNotification(LoginWindowCloseEvent eventNotification)
      {
        Directory.getEventBus().removeListener(LoginEvent.class, loginListener);
        Directory.getEventBus().removeListener(LoginWindowCloseEvent.class, loginWindowCloseListener);
        loginCallback.onClose();
      }
    }
  }
}