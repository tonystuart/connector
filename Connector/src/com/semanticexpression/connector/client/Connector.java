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

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.semanticexpression.connector.client.account.LoginWindow;
import com.semanticexpression.connector.client.events.LoginEvent;
import com.semanticexpression.connector.client.events.LogoutEvent;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.services.GetCredentialServiceRequest;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.client.wiring.DefaultUncaughtExceptionHandler;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.client.wiring.ServiceProvider;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Credential.AuthenticationType;
import com.semanticexpression.connector.shared.Keys;

public class Connector implements EntryPoint
{
  private static final long ONE_YEAR_IN_MS = 365l * 24l * 60l * 60l * 1000l;

  public static final String TITLE = "Connector";

  private Credential credential;
  private LoginWindow loginWindow;

  private void displayLicenseWindow()
  {
    LicenseWindow licenseWindow = new LicenseWindow(new OkayCancelHandler()
    {
      @Override
      public void cancel()
      {
        // do nothing
      }

      @Override
      public void okay()
      {
        initialize();
      }
    });

    licenseWindow.show();
  }

  public String getAuthenticationTokenCookie()
  {
    return Cookies.getCookie(Keys.AUTHENTICATION_TOKEN);
  }

  public LoginWindow getLoginWindow()
  {
    if (loginWindow == null)
    {
      loginWindow = new LoginWindow(true);
    }
    return loginWindow;
  }

  private void initialize()
  {
    Directory.getEventBus().addListener(LoginEvent.class, new LoginEventListener());
    Directory.getEventBus().addListener(LogoutEvent.class, new LogoutEventListener());
    Directory.getServiceBus().addServiceProvider(GetCredentialServiceRequest.class, new GetCredentialServiceProvider());

    Viewport viewport = new Viewport();
    viewport.setLayout(new FitLayout());
    viewport.add(new MainPanel());
    RootPanel.get().add(viewport);

    Directory.getConnectorService().getCredential(getAuthenticationTokenCookie(), new CredentialCallback());
  }

  public boolean isRememberMe()
  {
    boolean isRememberMe = true; // default to true on first use on this computer
    String isRememberMeCookie = Cookies.getCookie(Keys.IS_REMEMBER_ME);
    if (isRememberMeCookie != null)
    {
      isRememberMe = Boolean.parseBoolean(isRememberMeCookie);
    }
    return isRememberMe;
  }

  private void onCredentialCallback(Credential credential)
  {
    if (credential.getAuthenticationType() == AuthenticationType.AUTHENTICATION_REQUIRED)
    {
      LoginWindow loginWindow = new LoginWindow(false);
      loginWindow.show();
    }
    else
    {
      Directory.getEventBus().post(new LoginEvent(credential, isRememberMe()));
    }
  }

  public void onLoginEvent(LoginEvent loginEvent)
  {
    credential = loginEvent.getCredential();
    String authenticationToken = credential.getAuthenticationToken();
    boolean isRememberMe = loginEvent.isRememberMe();
    storeAuthenticationToken(authenticationToken, isRememberMe);
    Directory.getStatusMonitor().connect();
  }

  public void onLogoutEvent()
  {
    switch (credential.getAuthenticationType())
    {
      case UNAUTHENTICATED:
        // User wants to login as an authenticated user. Leave guest as current user until login succeeds
        LoginWindow loginWindow = getLoginWindow();
        loginWindow.setRememberMe(isRememberMe());
        loginWindow.show();
        break;
      case AUTHENTICATED:
        // User wants to logout as an authenticated user. Clear everything and restart and let default startup handle it.
        Directory.getConnectorService().logout(getAuthenticationTokenCookie(), new FailureReportingAsyncCallback<Void>()
        {
          @Override
          public void onSuccess(Void result)
          {
            storeAuthenticationToken(null, isRememberMe());
            Window.Location.reload();
          }
        });
        break;
    }
  }

  public void onModuleLoad()
  {
    GWT.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    if (isRememberMe() || getAuthenticationTokenCookie() != null)
    {
      initialize();
    }
    else
    {
      displayLicenseWindow();
    }
  }

  public void storeAuthenticationToken(String authenticationToken, boolean isRememberMe)
  {
    if (authenticationToken == null)
    {
      Cookies.removeCookie(Keys.AUTHENTICATION_TOKEN);
    }
    else
    {
      Date expires = null;
      Date nextYear = new Date(System.currentTimeMillis() + ONE_YEAR_IN_MS);
      if (isRememberMe)
      {
        expires = nextYear;
      }
      Cookies.setCookie(Keys.AUTHENTICATION_TOKEN, authenticationToken, expires);
      Cookies.setCookie(Keys.IS_REMEMBER_ME, Boolean.toString(isRememberMe), nextYear); // remember this setting independent of user
    }
  }

  private final class CredentialCallback extends FailureReportingAsyncCallback<Credential>
  {
    @Override
    public void onSuccess(Credential credential)
    {
      onCredentialCallback(credential);
    }
  }

  private final class GetCredentialServiceProvider implements ServiceProvider<GetCredentialServiceRequest>
  {
    @Override
    public void onServiceRequest(GetCredentialServiceRequest getCredentialServiceRequest)
    {
      getCredentialServiceRequest.setCredential(credential);
    }
  }

  private final class LoginEventListener implements EventListener<LoginEvent>
  {
    @Override
    public void onEventNotification(LoginEvent loginEvent)
    {
      onLoginEvent(loginEvent);
    }
  }

  private final class LogoutEventListener implements EventListener<LogoutEvent>
  {
    @Override
    public void onEventNotification(LogoutEvent logoutEvent)
    {
      onLogoutEvent();
    }
  }

}
