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

package com.semanticexpression.connector.client.account;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteData;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout;
import com.semanticexpression.connector.client.events.LoginEvent;
import com.semanticexpression.connector.client.events.LoginWindowCloseEvent;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.DefaultButtonWindow;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.exception.InvalidLoginCredentialsException;

public final class LoginWindow extends DefaultButtonWindow
{
  private Html createAccountHtml;
  private Button loginButton;
  private SafeTextField<String> passwordSafeTextField;
  private Text passwordText;
  private Html recoverPasswordHtml;
  private CheckBox rememberMeCheckBox;
  private Text rememberMeText;
  private SafeTextField<String> userNameSafeTextField;
  private Text userNameText;

  public LoginWindow(boolean isClosable)
  {
    setResizable(false);
    setSize("370", "250");
    setClosable(isClosable);
    setIcon(Resources.LOGIN);
    setHeading("Login to Connector");
    setLayout(new AbsoluteLayout());
    add(getUserNameText(), new AbsoluteData(11, 26));
    add(getUserNameSafeTextField(), new AbsoluteData(139, 21));
    add(getPasswordText(), new AbsoluteData(9, 63));
    add(getPasswordSafeTextField(), new AbsoluteData(139, 58));
    add(getLoginButton(), new AbsoluteData(289, 95));
    add(getCreateAccountHtml(), new AbsoluteData(8, 138));
    add(getRecoverPasswordHtml(), new AbsoluteData(8, 163));
    add(getRememberMeCheckBox(), new AbsoluteData(86, 185));
    add(getRememberMeText(), new AbsoluteData(107, 189));
    setFocusWidget(getUserNameSafeTextField());
    setDefaultButton(getLoginButton());
    addListener(Events.Close, new CloseListener());
    Directory.getEventBus().addListener(LoginEvent.class, new LoginListener());
  }

  private NewAccountWindow displayNewAccountWindow()
  {
    boolean isRememberMe = getRememberMeCheckBox().getValue();
    // Create new window to ensure it doesn't contain account info for previous user
    NewAccountWindow newAccountWindow = new NewAccountWindow(isRememberMe);
    newAccountWindow.show();
    return newAccountWindow;
  }

  public void doLogin()
  {
    final String userName = getUserNameSafeTextField().getValue();
    if (userName == null)
    {
      MessageBox.alert("Missing Field", "Please enter a user name and try again.", null);
      setFocusWidget(getUserNameSafeTextField());
      return;
    }
    String password = getPasswordSafeTextField().getValue();
    if (password == null)
    {
      MessageBox.alert("Missing Field", "Please enter a password and try again.", null);
      setFocusWidget(getPasswordSafeTextField());
      return;
    }
    Directory.getConnectorService().login(userName, password, new FailureReportingAsyncCallback<Credential>()
    {
      @Override
      public void onFailure(Throwable throwable)
      {
        if (throwable instanceof InvalidLoginCredentialsException)
        {
          MessageBox.alert("Login Failure", "The user name and password are not correct.", null);
        }
        else
        {
          super.onFailure(throwable);
        }
      }

      @Override
      public void onSuccess(Credential credential)
      {
        Boolean isRememberMe = getRememberMeCheckBox().getValue();
        Directory.getEventBus().post(new LoginEvent(credential, isRememberMe));
      }
    });
  }

  public Html getCreateAccountHtml()
  {
    if (createAccountHtml == null)
    {
      createAccountHtml = new Html("New to Connector? Click <span class=\"Connector-InlineHyperLink\">here</span> to create an account.");
      createAccountHtml.setSize("339px", "14px");
      createAccountHtml.setStyleName("connector-Centered");
      createAccountHtml.sinkEvents(Events.OnClick.getEventCode());
      createAccountHtml.addListener(Events.OnClick, new Listener<ComponentEvent>()
      {
        public void handleEvent(ComponentEvent be)
        {
          displayNewAccountWindow();
        }
      });
    }
    return createAccountHtml;
  }

  public Button getLoginButton()
  {
    if (loginButton == null)
    {
      loginButton = new Button("Login");
      loginButton.addSelectionListener(new LoginButtonSelectionListener());
    }
    return loginButton;
  }

  public SafeTextField<String> getPasswordSafeTextField()
  {
    if (passwordSafeTextField == null)
    {
      passwordSafeTextField = new SafeTextField<String>();
      passwordSafeTextField.setPassword(true);
      passwordSafeTextField.setSize("190px", "22px");
      passwordSafeTextField.setFieldLabel("Password");
    }
    return passwordSafeTextField;
  }

  public Text getPasswordText()
  {
    if (passwordText == null)
    {
      passwordText = new Text("Password:");
      passwordText.setStyleName("connector-FieldLabel");
      passwordText.setSize("120px", "14px");
    }
    return passwordText;
  }

  public Html getRecoverPasswordHtml()
  {
    if (recoverPasswordHtml == null)
    {
      recoverPasswordHtml = new Html("Forgot your password? Click <span class=\"Connector-InlineHyperLink\">here</span> to recover it.");
      recoverPasswordHtml.setStyleName("connector-Centered");
      recoverPasswordHtml.setSize("339px", "14px");
      recoverPasswordHtml.sinkEvents(Events.OnClick.getEventCode());
      recoverPasswordHtml.addListener(Events.OnClick, new Listener<ComponentEvent>()
      {
        public void handleEvent(ComponentEvent be)
        {
          boolean isRememberMe = getRememberMeCheckBox().getValue();
          PersonalSecurityQuestionWindow personalSecurityQuestionWindow = new PersonalSecurityQuestionWindow(isRememberMe);
          String userName = userNameSafeTextField.getValue();
          if (userName != null)
          {
            personalSecurityQuestionWindow.getUserNameSafeTextField().setValue(userName);
          }
          personalSecurityQuestionWindow.show();
        }
      });
    }
    return recoverPasswordHtml;
  }

  public CheckBox getRememberMeCheckBox()
  {
    if (rememberMeCheckBox == null)
    {
      rememberMeCheckBox = new CheckBox();
      rememberMeCheckBox.setHideLabel(true);
    }
    return rememberMeCheckBox;
  }

  public Text getRememberMeText()
  {
    if (rememberMeText == null)
    {
      rememberMeText = new Text("Remember me on this computer");
    }
    return rememberMeText;
  }

  public SafeTextField<String> getUserNameSafeTextField()
  {
    if (userNameSafeTextField == null)
    {
      userNameSafeTextField = new SafeTextField<String>();
      userNameSafeTextField.setSize("190px", "22px");
      userNameSafeTextField.setFieldLabel("User Name");
    }
    return userNameSafeTextField;
  }

  public Text getUserNameText()
  {
    if (userNameText == null)
    {
      userNameText = new Text("User Name:");
      userNameText.setStyleName("connector-FieldLabel");
      userNameText.setSize("120px", "14px");
    }
    return userNameText;
  }

  public void setRememberMe(boolean rememberMe)
  {
    getRememberMeCheckBox().setValue(rememberMe);
  }

  private final class CloseListener implements Listener<WindowEvent>
  {
    @Override
    public void handleEvent(WindowEvent be)
    {
      Directory.getEventBus().post(new LoginWindowCloseEvent());
    }
  }

  private class LoginButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      doLogin();
    }

  }

  private final class LoginListener implements EventListener<LoginEvent>
  {
    @Override
    public void onEventNotification(LoginEvent eventNotification)
    {
      // Handle successful login, either from this window or from CreateAccountWindow or PersonalSecurityQuestionWindow
      LoginWindow.this.hide();
    }
  }
}
