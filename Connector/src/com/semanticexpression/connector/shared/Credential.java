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

package com.semanticexpression.connector.shared;

import java.io.Serializable;

public class Credential implements Serializable
{
  public static final String GUEST_USER = "Guest";

  public static final int OPTION_A_EMAIL = 0x01;
  public static final int OPTION_B_PERSONAL_QUESTION = 0x02;
  public static final int OPTION_C_CAPTCHA = 0x04;

  private static final long serialVersionUID = 1L;

  private int accountCreationOptions;
  private String authenticationToken;
  private AuthenticationType authenticationType;
  private String userName;

  public Credential()
  {
  }

  public Credential(AuthenticationType authenticationType, String authenticationToken, String userName, int accountCreationOptions)
  {
    this.authenticationType = authenticationType;
    this.authenticationToken = authenticationToken;
    this.userName = userName;
    this.accountCreationOptions = accountCreationOptions;
  }

  public int getAccountCreationOptions()
  {
    return accountCreationOptions;
  }

  public String getAuthenticationToken()
  {
    return authenticationToken;
  }

  public AuthenticationType getAuthenticationType()
  {
    return authenticationType;
  }

  public String getUserName()
  {
    return userName;
  }

  public void setAuthenticationToken(String authenticationToken)
  {
    this.authenticationToken = authenticationToken;
  }

  public void setAuthenticationType(AuthenticationType authenticationType)
  {
    this.authenticationType = authenticationType;
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  public enum AuthenticationType
  {
    AUTHENTICATED, AUTHENTICATION_REQUIRED, UNAUTHENTICATED
  }

}
