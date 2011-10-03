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

package com.semanticexpression.connector.client.events;

import com.semanticexpression.connector.client.wiring.EventNotification;
import com.semanticexpression.connector.shared.Credential;

public class LoginEvent implements EventNotification
{
  private Credential credential;
  private boolean isRememberMe;

  public LoginEvent(Credential credential, boolean isRememberMe)
  {
    this.credential = credential;
    this.isRememberMe = isRememberMe;
  }

  public Credential getCredential()
  {
    return credential;
  }

  public boolean isRememberMe()
  {
    return isRememberMe;
  }

}
