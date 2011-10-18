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

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.events.LoginEvent;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.client.widget.Wizard;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.exception.DuplicateUserNameException;
import com.semanticexpression.connector.shared.exception.InvalidCaptchaException;
import com.semanticexpression.connector.shared.exception.InvalidPersonalSecurityAnswer;
import com.semanticexpression.connector.shared.exception.InvalidUserNameLengthException;

public final class NewAccountWindow extends Wizard
{
  private int accountCreationOptions;
  private Html captchaHelp;
  private Html captchaImageHtml;
  private SafeTextField<String> captchaKeySafeTextField;
  private LayoutContainer captchaLayoutContainer;
  private Text captchKeyText;
  private Text confirmEmailText;
  private SafeTextField<String> confirmEmailTextField;
  private SafeTextField<String> confirmPasswordSafeTextField;
  private Text confirmPasswordText;
  private Html emailHelp;
  private LayoutContainer emailLayoutContainer;
  private Text emailText;
  private SafeTextField<String> emailTextField;
  private Margins fieldMargins;
  private Margins helpMargins;
  private boolean isRememberMe;
  private Margins labelMargins;
  private SafeTextField<String> passwordSafeTextField;
  private Text passwordText;
  private LayoutContainer personalQuestionLayoutContainer;
  private Text personalSecurityAnswerText;
  private SafeTextField<String> personalSecurityAnswerTextField;
  private Html personalSecurityHelp;
  private Text personalSecurityQuestionText;
  private SafeTextField<String> personalSecurityQuestionTextField;
  private Html userNameHelp;
  private LayoutContainer userNameLayoutContainer;
  private SafeTextField<String> userNameSafeTextField;
  private Text userNameText;

  public NewAccountWindow(boolean isRememberMe)
  {
    super("Create", Resources.ACCOUNT_CREATE);

    this.isRememberMe = isRememberMe;

    setSize("300px", "300px");
    setIcon(Resources.ACCOUNT);
    setHeading("Create Account");
    add(getUserNameLayoutContainer());

    accountCreationOptions = Utility.getAccountCreationOptions();
    if ((accountCreationOptions & Credential.OPTION_A_EMAIL) != 0)
    {
      add(getEmailLayoutContainer());
    }
    if ((accountCreationOptions & Credential.OPTION_B_PERSONAL_QUESTION) != 0)
    {
      add(getPersonalQuestionLayoutContainer());
    }
    if ((accountCreationOptions & Credential.OPTION_C_CAPTCHA) != 0)
    {
      add(getCaptchaLayoutContainer());
    }

    setFocusWidget(getUserNameSafeTextField());
    updateButtonState();
    Directory.getEventBus().addListener(LoginEvent.class, new LoginListener());
  }

  private void createAccount()
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
    String confirmPassword = getConfirmPasswordSafeTextField().getValue();
    if (confirmPassword == null)
    {
      MessageBox.alert("Missing Field", "Please confirm you password and try again.", null);
      setFocusWidget(getConfirmPasswordSafeTextField());
      return;
    }
    if (!password.equals(confirmPassword))
    {
      MessageBox.alert("Password Mismatch", "Your password and confirm password don't match.", null);
      setFocusWidget(getPasswordSafeTextField());
      return;
    }

    String emailAddress = null;

    if ((accountCreationOptions & Credential.OPTION_A_EMAIL) != 0)
    {
      emailAddress = getEmailTextField().getValue();
      if (emailAddress == null)
      {
        MessageBox.alert("Missing Field", "Please enter your email address and try again.", null);
        setFocusWidget(getEmailTextField());
        return;
      }
      String confirmEmailAddress = getConfirmEmailTextField().getValue();
      if (confirmEmailAddress == null)
      {
        MessageBox.alert("Missing Field", "Please confirm your email address and try again.", null);
        setFocusWidget(getConfirmEmailTextField());
        return;
      }
      if (!emailAddress.equals(confirmEmailAddress))
      {
        MessageBox.alert("Email Mismatch", "Your email address and confirm email address don't match.", null);
        setFocusWidget(getConfirmEmailTextField());
        return;
      }
    }

    String personalSecurityQuestion = null;
    String personalSecurityAnswer = null;

    if ((accountCreationOptions & Credential.OPTION_B_PERSONAL_QUESTION) != 0)
    {
      personalSecurityQuestion = getPersonalSecurityQuestionTextField().getValue();
      if (personalSecurityQuestion == null)
      {
        MessageBox.alert("Missing Field", "Please enter your personal question and try again.", null);
        setFocusWidget(getPersonalSecurityQuestionTextField());
        return;
      }
      personalSecurityAnswer = getPersonalSecurityAnswerTextField().getValue();
      if (personalSecurityAnswer == null)
      {
        MessageBox.alert("Missing Field", "Please enter your personal answer and try again.", null);
        setFocusWidget(getPersonalSecurityAnswerTextField());
        return;
      }
    }

    String captchaKey = null;
    if ((accountCreationOptions & Credential.OPTION_C_CAPTCHA) != 0)
    {
      captchaKey = getCaptchaKeySafeTextField().getValue();
      if (captchaKey == null)
      {
        MessageBox.alert("Missing Field", "Please enter the security key and try again.", null);
        setFocusWidget(getCaptchaKeySafeTextField());
        return;
      }
    }

    Directory.getConnectorService().createAccount(userName, password, emailAddress, personalSecurityQuestion, personalSecurityAnswer, captchaKey, new CreateAccountCallback(userName));
  }

  public Html getCaptchaHelp()
  {
    if (captchaHelp == null)
    {
      captchaHelp = new Html("<b>Security Key:</b> The image above contains a random collection of upper and lower case letters and digits. Please enter the value in the Security Key field. Doing so helps prevent malicious access.");
      captchaHelp.setHeight(100);
    }
    return captchaHelp;
  }

  public Html getCaptchaImageHtml()
  {
    if (captchaImageHtml == null)
    {
      captchaImageHtml = new Html();
      captchaImageHtml.setSize("190px", "50px");
      captchaImageHtml.sinkEvents(Events.OnClick.getEventCode());
      captchaImageHtml.addListener(Events.OnClick, new Listener<ComponentEvent>()
      {
        public void handleEvent(ComponentEvent be)
        {
          refreshCaptchaImageHtml();
        }
      });
      refreshCaptchaImageHtml();
    }
    return captchaImageHtml;
  }

  public SafeTextField<String> getCaptchaKeySafeTextField()
  {
    if (captchaKeySafeTextField == null)
    {
      captchaKeySafeTextField = addEnterKeyListener(new SafeTextField<String>());
    }
    return captchaKeySafeTextField;
  }

  private LayoutContainer getCaptchaLayoutContainer()
  {
    if (captchaLayoutContainer == null)
    {
      captchaLayoutContainer = new LayoutContainer();
      captchaLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      captchaLayoutContainer.add(getCaptchKeyText(), new RowData(1, -1, getLabelMargins()));
      captchaLayoutContainer.add(getCaptchaKeySafeTextField(), new RowData(1, -1, getFieldMargins()));
      captchaLayoutContainer.add(new Text(), new RowData(1, 0.5, getFieldMargins()));
      captchaLayoutContainer.add(getCaptchaImageHtml(), new RowData(1, -1, new Margins(0, 0, 0, 25)));
      captchaLayoutContainer.add(new Text(), new RowData(1, 0.5, getFieldMargins()));
      captchaLayoutContainer.add(getCaptchaHelp(), new RowData(1, -1, getHelpMargins()));
    }
    return captchaLayoutContainer;
  }

  public Text getCaptchKeyText()
  {
    if (captchKeyText == null)
    {
      captchKeyText = new Text("Security Key:");
    }
    return captchKeyText;
  }

  private Text getConfirmEmailText()
  {
    if (confirmEmailText == null)
    {
      confirmEmailText = new Text("Confirm Email Address:");
    }
    return confirmEmailText;
  }

  private SafeTextField<String> getConfirmEmailTextField()
  {
    if (confirmEmailTextField == null)
    {
      confirmEmailTextField = addEnterKeyListener(new SafeTextField<String>());
    }
    return confirmEmailTextField;
  }

  public SafeTextField<String> getConfirmPasswordSafeTextField()
  {
    if (confirmPasswordSafeTextField == null)
    {
      confirmPasswordSafeTextField = addEnterKeyListener(new SafeTextField<String>());
      confirmPasswordSafeTextField.setPassword(true);
    }
    return confirmPasswordSafeTextField;
  }

  public Text getConfirmPasswordText()
  {
    if (confirmPasswordText == null)
    {
      confirmPasswordText = new Text("Confirm Password:");
    }
    return confirmPasswordText;
  }

  private Html getEmailHelp()
  {
    if (emailHelp == null)
    {
      emailHelp = new Html("<b>Email Address:</b> Your email address is used to notify you when workflow tasks are ready for your attention.");
      emailHelp.setHeight(100);
    }
    return emailHelp;
  }

  private LayoutContainer getEmailLayoutContainer()
  {
    if (emailLayoutContainer == null)
    {
      emailLayoutContainer = new LayoutContainer();
      emailLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      emailLayoutContainer.add(getEmailText(), new RowData(1, -1, getLabelMargins()));
      emailLayoutContainer.add(getEmailTextField(), new RowData(1, -1, getFieldMargins()));
      emailLayoutContainer.add(getConfirmEmailText(), new RowData(1, -1, getLabelMargins()));
      emailLayoutContainer.add(getConfirmEmailTextField(), new RowData(1, -1, getFieldMargins()));
      emailLayoutContainer.add(new Text(), new RowData(1, 1, getFieldMargins()));
      emailLayoutContainer.add(getEmailHelp(), new RowData(1, -1, getHelpMargins()));
    }
    return emailLayoutContainer;
  }

  private Text getEmailText()
  {
    if (emailText == null)
    {
      emailText = new Text("Email Address:");
    }
    return emailText;
  }

  private SafeTextField<String> getEmailTextField()
  {
    if (emailTextField == null)
    {
      emailTextField = addEnterKeyListener(new SafeTextField<String>());
    }
    return emailTextField;
  }

  private Margins getFieldMargins()
  {
    if (fieldMargins == null)
    {
      fieldMargins = new Margins(0, 5, 0, 5);
    }
    return fieldMargins;
  }

  private Margins getHelpMargins()
  {
    if (helpMargins == null)
    {
      helpMargins = new Margins(5, 5, 5, 5);
    }
    return helpMargins;
  }

  private Margins getLabelMargins()
  {
    if (labelMargins == null)
    {
      labelMargins = new Margins(5, 5, 0, 5);
    }
    return labelMargins;
  }

  public SafeTextField<String> getPasswordSafeTextField()
  {
    if (passwordSafeTextField == null)
    {
      passwordSafeTextField = addEnterKeyListener(new SafeTextField<String>());
      passwordSafeTextField.setPassword(true);
    }
    return passwordSafeTextField;
  }

  public Text getPasswordText()
  {
    if (passwordText == null)
    {
      passwordText = new Text("Password:");
    }
    return passwordText;
  }

  private LayoutContainer getPersonalQuestionLayoutContainer()
  {
    if (personalQuestionLayoutContainer == null)
    {
      personalQuestionLayoutContainer = new LayoutContainer();
      personalQuestionLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      personalQuestionLayoutContainer.add(getPersonalSecurityQuestionText(), new RowData(1, -1, getLabelMargins()));
      personalQuestionLayoutContainer.add(getPersonalSecurityQuestionTextField(), new RowData(1, -1, getFieldMargins()));
      personalQuestionLayoutContainer.add(getPersonalSecurityAnswerText(), new RowData(1, -1, getLabelMargins()));
      personalQuestionLayoutContainer.add(getPersonalSecurityAnswerTextField(), new RowData(1, -1, getFieldMargins()));
      personalQuestionLayoutContainer.add(new Text(), new RowData(1, 1, getFieldMargins()));
      personalQuestionLayoutContainer.add(getPersonalSecurityHelp(), new RowData(1, -1, getHelpMargins()));
    }
    return personalQuestionLayoutContainer;
  }

  public Text getPersonalSecurityAnswerText()
  {
    if (personalSecurityAnswerText == null)
    {
      personalSecurityAnswerText = new Text("Personal Answer:");
    }
    return personalSecurityAnswerText;
  }

  public SafeTextField<String> getPersonalSecurityAnswerTextField()
  {
    if (personalSecurityAnswerTextField == null)
    {
      personalSecurityAnswerTextField = addEnterKeyListener(new SafeTextField<String>());
    }
    return personalSecurityAnswerTextField;
  }

  public Html getPersonalSecurityHelp()
  {
    if (personalSecurityHelp == null)
    {
      personalSecurityHelp = new Html("<b>Personal Security Question:</b> enter a question that only you know the answer to. Then enter the answer. If you ever forget your password, you can log on by requesting this question and providing the same answer.");
      personalSecurityHelp.setHeight(100);
    }
    return personalSecurityHelp;
  }

  public Text getPersonalSecurityQuestionText()
  {
    if (personalSecurityQuestionText == null)
    {
      personalSecurityQuestionText = new Text("Personal Question:");
    }
    return personalSecurityQuestionText;
  }

  public SafeTextField<String> getPersonalSecurityQuestionTextField()
  {
    if (personalSecurityQuestionTextField == null)
    {
      personalSecurityQuestionTextField = addEnterKeyListener(new SafeTextField<String>());
    }
    return personalSecurityQuestionTextField;
  }

  private Html getUserNameHelp()
  {
    if (userNameHelp == null)
    {
      userNameHelp = new Html("<b>User Name and Password:</b> User names and passwords are case sensitive. You can use special characters such as space. If you use upper case or special characters you must always enter these characters the same way, each time you log in.");
      userNameHelp.setHeight(100);
    }
    return userNameHelp;
  }

  private LayoutContainer getUserNameLayoutContainer()
  {
    if (userNameLayoutContainer == null)
    {
      userNameLayoutContainer = new LayoutContainer();
      userNameLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      userNameLayoutContainer.add(getUserNameText(), new RowData(1, -1, getLabelMargins()));
      userNameLayoutContainer.add(getUserNameSafeTextField(), new RowData(1, -1, getFieldMargins()));
      userNameLayoutContainer.add(getPasswordText(), new RowData(1, -1, getLabelMargins()));
      userNameLayoutContainer.add(getPasswordSafeTextField(), new RowData(1, -1, getFieldMargins()));
      userNameLayoutContainer.add(getConfirmPasswordText(), new RowData(1, -1, getLabelMargins()));
      userNameLayoutContainer.add(getConfirmPasswordSafeTextField(), new RowData(1, -1, getFieldMargins()));
      userNameLayoutContainer.add(new Text(), new RowData(1, 1, getFieldMargins()));
      userNameLayoutContainer.add(getUserNameHelp(), new RowData(1, -1, getHelpMargins()));
    }
    return userNameLayoutContainer;
  }

  public SafeTextField<String> getUserNameSafeTextField()
  {
    if (userNameSafeTextField == null)
    {
      userNameSafeTextField = addEnterKeyListener(new SafeTextField<String>());
    }
    return userNameSafeTextField;
  }

  public Text getUserNameText()
  {
    if (userNameText == null)
    {
      userNameText = new Text("User Name:");
    }
    return userNameText;
  }

  @Override
  public void onAction()
  {
    createAccount();
  }

  public void refreshCaptchaImageHtml()
  {
    getCaptchaImageHtml().setHtml("<img src='captchaServlet/" + System.currentTimeMillis() + ".jpg'/>");
  }

  private final class CreateAccountCallback extends FailureReportingAsyncCallback<Credential>
  {
    private final String userName;

    private CreateAccountCallback(String userName)
    {
      this.userName = userName;
    }

    @Override
    public void onFailure(Throwable throwable)
    {
      if (throwable instanceof DuplicateUserNameException)
      {
        MessageBox.alert("Duplicate User Name", "The user name you entered is already in use. Please select a new one and try again.", null);
        setFocusWidget(getUserNameSafeTextField());
      }
      else if (throwable instanceof InvalidUserNameLengthException)
      {
        MessageBox.alert("Invalid User Name", "The supplied user name is too long. The maximum length of a user name is " + ((InvalidUserNameLengthException)throwable).getMaximumLength() + " characters.", null);
        setFocusWidget(getUserNameSafeTextField());
      }
      else if (throwable instanceof InvalidPersonalSecurityAnswer)
      {
        MessageBox.alert("Invalid Personal Security Answer", "The personal security answer must contain at least one letter or digit.", null);
        setFocusWidget(getPersonalSecurityAnswerTextField());
      }
      else if (throwable instanceof InvalidCaptchaException)
      {
        MessageBox.alert("Invalid Security Key", "The security key does not match the characters in the image. If you are having trouble reading the characters, click on the image to generate a new one.", null);
        setFocusWidget(getCaptchaKeySafeTextField());
      }
      else
      {
        super.onFailure(throwable);
      }
    }

    @Override
    public void onSuccess(final Credential credential)
    {
      MessageBox.info("Account Created", "Welcome " + userName + ". Your account was successfully created.", new Listener<MessageBoxEvent>()
      {
        @Override
        public void handleEvent(MessageBoxEvent be)
        {
          // Don't post login event until the message is acknowledged... otherwise it can get overlaid by other windows that are waiting for the event.
          Directory.getEventBus().post(new LoginEvent(credential, isRememberMe));
        }
      });
    }
  }

  private final class LoginListener implements EventListener<LoginEvent>
  {
    @Override
    public void onEventNotification(LoginEvent eventNotification)
    {
      // Handle successful login, either from this window or from LoginWindow or ForgotPasswordWindow
      NewAccountWindow.this.hide();
    }
  }

}
