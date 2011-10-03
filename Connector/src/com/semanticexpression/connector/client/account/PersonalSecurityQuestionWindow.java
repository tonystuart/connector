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
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteData;
import com.extjs.gxt.ui.client.widget.layout.AbsoluteLayout;
import com.semanticexpression.connector.client.events.LoginEvent;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.widget.DefaultButtonWindow;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.exception.InvalidLoginCredentialsException;
import com.semanticexpression.connector.shared.exception.InvalidPersonalSecurityAnswer;

public class PersonalSecurityQuestionWindow extends DefaultButtonWindow
{
  private SafeTextField<String> answerSafeTextField;
  private Text answerText;
  private Button checkAnswerButton;
  private Button getQuestionButton;
  private Html ifYouRegisteredHtml;
  private boolean isRememberMe;
  private Text questionText;
  private SafeTextField<String> userNameSafeTextField;
  private Text userNameText;

  public PersonalSecurityQuestionWindow(boolean isRememberMe)
  {
    this.isRememberMe = isRememberMe;

    setSize("370px", "309px");
    setResizable(false);
    setHeading("Personal Security Question");
    setLayout(new AbsoluteLayout());
    add(getIfYouRegisteredHtml(), new AbsoluteData(6, 5));
    add(getUserNameText(), new AbsoluteData(14, 70));
    add(getUserNameSafeTextField(), new AbsoluteData(142, 65));
    add(getGetQuestionButton(), new AbsoluteData(227, 103));
    setDefaultButton(getGetQuestionButton());
    add(getQuestionText(), new AbsoluteData(6, 139));
    add(getAnswerText(), new AbsoluteData(14, 204));
    add(getAnswerSafeTextField(), new AbsoluteData(142, 199));
    add(getCheckAnswerButton(), new AbsoluteData(227, 237));
    setFocusWidget(getUserNameSafeTextField());
    Directory.getEventBus().addListener(LoginEvent.class, new EventListener<LoginEvent>()
    {
      @Override
      public void onEventNotification(LoginEvent eventNotification)
      {
        // Handle successful login, either from this window or from LoginWindow or CreateAccountWindow
        PersonalSecurityQuestionWindow.this.hide();
      }
    });
  }

  public SafeTextField<String> getAnswerSafeTextField()
  {
    if (answerSafeTextField == null)
    {
      answerSafeTextField = new SafeTextField<String>();
      answerSafeTextField.setEnabled(false);
      answerSafeTextField.setFieldLabel("User Name");
      answerSafeTextField.setSize("190px", "22px");
    }
    return answerSafeTextField;
  }

  public Text getAnswerText()
  {
    if (answerText == null)
    {
      answerText = new Text("Answer:");
      answerText.setEnabled(false);
      answerText.setStyleName("connector-FieldLabel");
      answerText.setSize("120px", "14px");
    }
    return answerText;
  }

  public Button getCheckAnswerButton()
  {
    if (checkAnswerButton == null)
    {
      checkAnswerButton = new Button("Check Answer");
      checkAnswerButton.setEnabled(false);
      checkAnswerButton.addSelectionListener(new CheckAnswerButtonSelectionListener());
      checkAnswerButton.setSize("105px", "22px");
    }
    return checkAnswerButton;
  }

  public Button getGetQuestionButton()
  {
    if (getQuestionButton == null)
    {
      getQuestionButton = new Button("Get Question");
      getQuestionButton.addSelectionListener(new GetQuestionButtonSelectionListener());
      getQuestionButton.setSize("105px", "22px");
    }
    return getQuestionButton;
  }

  public Html getIfYouRegisteredHtml()
  {
    if (ifYouRegisteredHtml == null)
    {
      ifYouRegisteredHtml = new Html("Your profile contains a question that you provided that only you should know the answer to. Enter your user name and press \"Get Question\" to display the question.");
      ifYouRegisteredHtml.setSize("344px", "45px");
    }
    return ifYouRegisteredHtml;
  }

  public Text getQuestionText()
  {
    if (questionText == null)
    {
      questionText = new Text("Your question will appear here when you press the \"Get Question\" button.");
      questionText.setEnabled(false);
      questionText.setSize("344px", "45px");
    }
    return questionText;
  }

  public SafeTextField<String> getUserNameSafeTextField()
  {
    if (userNameSafeTextField == null)
    {
      userNameSafeTextField = new SafeTextField<String>();
      userNameSafeTextField.setFieldLabel("User Name");
      userNameSafeTextField.setSize("190px", "22px");
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

  private class CheckAnswerButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      String userName = getUserNameSafeTextField().getValue();
      if (userName == null)
      {
        MessageBox.alert("Missing Field", "Please enter your user name and try again.", null);
        setFocusWidget(getUserNameSafeTextField());
        return;
      }
      String personalSecurityAnswer = getAnswerSafeTextField().getValue();
      if (personalSecurityAnswer == null)
      {
        MessageBox.alert("Missing Field", "Please enter your answer and try again.", null);
        setFocusWidget(getAnswerSafeTextField());
        return;
      }
      Directory.getConnectorService().checkPersonalSecurityAnswer(userName, personalSecurityAnswer, new FailureReportingAsyncCallback<Credential>()
      {
        @Override
        public void onFailure(Throwable throwable)
        {
          if (throwable instanceof InvalidLoginCredentialsException)
          {
            MessageBox.alert("Invalid User Name", "The user name you entered does not exist. Please try again.", null);
            setFocusWidget(getUserNameSafeTextField());
          }
          else if (throwable instanceof InvalidPersonalSecurityAnswer)
          {
            MessageBox.alert("Incorrect Answer", "The answer you supplied doesn't match the answer you originally provided. Please try again.", null);
            setFocusWidget(getAnswerSafeTextField());
          }
          else
          {
            super.onFailure(throwable);
          }
        }

        @Override
        public void onSuccess(Credential credential)
        {
          MessageBox.info("Correct Answer", "The answer you supplied matches the answer you originally provided and you are now logged in. Please change your password in your profile settings so that you do not have to use the personal security question in the future.", null);
          Directory.getEventBus().post(new LoginEvent(credential, isRememberMe));
        }
      });
    }
  }

  private class GetQuestionButtonSelectionListener extends SelectionListener<ButtonEvent>
  {
    public void componentSelected(ButtonEvent ce)
    {
      String userName = getUserNameSafeTextField().getValue();
      if (userName == null)
      {
        MessageBox.alert("Missing Field", "Please enter your user name and try again.", null);
        setFocusWidget(getUserNameSafeTextField());
        return;
      }
      Directory.getConnectorService().getPersonalSecurityQuestion(userName, new FailureReportingAsyncCallback<String>()
      {
        @Override
        public void onFailure(Throwable throwable)
        {
          if (throwable instanceof InvalidLoginCredentialsException)
          {
            MessageBox.alert("Invalid User Name", "The user name you entered does not exist. Please try again", null);
            setFocusWidget(getUserNameSafeTextField());
          }
          else
          {
            super.onFailure(throwable);
          }
        }

        @Override
        public void onSuccess(String personalSecurityQuestion)
        {
          getQuestionText().setText(personalSecurityQuestion);
          getQuestionText().setEnabled(true);
          getAnswerText().setEnabled(true);
          getAnswerSafeTextField().setEnabled(true);
          getCheckAnswerButton().setEnabled(true);
          setFocusWidget(getAnswerSafeTextField());
          setDefaultButton(getCheckAnswerButton());
          focus(); // sets focus to widget
        }
      });
    }
  }
}
