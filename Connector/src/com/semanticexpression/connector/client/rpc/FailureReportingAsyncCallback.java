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

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Credential.AuthenticationType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;
import com.semanticexpression.connector.shared.exception.PublicationException;
import com.semanticexpression.connector.shared.exception.TagVisibilityException;

public abstract class FailureReportingAsyncCallback<T> implements AsyncCallback<T>
{

  @Override
  public void onFailure(Throwable throwable)
  {
    if (throwable instanceof AuthenticationException)
    {
      MessageBox.alert("Unauthenticated Access", "You must be logged in to perform the requested operation.", null);
    }
    else if (throwable instanceof AuthorizationException)
    {
      Id contentId = ((AuthorizationException)throwable).getContentId();
      String message = "You do not have access to the requested content (" + contentId + ").";
      if (Utility.getCredential().getAuthenticationType() == AuthenticationType.UNAUTHENTICATED)
      {
        message += "<br/><br/>You currently have read only guest access. To access your own content, please login and try again.";
      }
      MessageBox.alert("Unauthorized Access", message, null);
    }
    else if (throwable instanceof InvalidContentIdException)
    {
      Id contentId = ((InvalidContentIdException)throwable).getContentId();
      MessageBox.alert("Invalid Content Request", "The requested content does not exist (" + contentId + ").", null);
    }
    else if (throwable instanceof TagVisibilityException)
    {
      TagVisibilityException tagVisibilityException = (TagVisibilityException)throwable;
      String tagName = tagVisibilityException.getTagName();
      boolean isPrivate = tagVisibilityException.isPrivate();
      MessageBox.alert("Tag Visibility", "You have already defined tag " + tagName + " as a " + (isPrivate ? "private" : "public") + " tag.", null);
    }
    else if (throwable instanceof PublicationException)
    {
      Id contentId = ((PublicationException)throwable).getContentId();
      MessageBox.alert("Unauthorized Publication", "You are not authorized to publish the requested content (" + contentId + ").", null);
    }
    else
    {
      Utility.displayStackTrace(throwable);
    }
  }
}
