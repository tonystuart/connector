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

import java.util.Set;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.shared.exception.HistoryCacheMissException;
import com.semanticexpression.connector.shared.exception.ReadOnlyUpdateException;

public final class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler
{
  /**
   * Displays an exception-specific MessageBox or a general exception stack
   * trace.
   */
  public void onUncaughtException(Throwable throwable)
  {
    if (throwable instanceof UmbrellaException)
    {
      UmbrellaException umbrellaException = (UmbrellaException)throwable;
      Set<Throwable> causes = umbrellaException.getCauses();
      if (causes.size() == 0)
      {
        displayException(throwable);
      }
      else
      {
        for (Throwable cause : causes)
        {
          displayException(cause);
        }
      }
    }
    else
    {
      displayException(throwable);
    }
  }

  private void displayException(Throwable throwable)
  {
    if (throwable instanceof ReadOnlyUpdateException)
    {
      MessageBox.alert("Read Only", "The content you are attempting to update is read-only. This could be due to several reasons:<br/><br/>" + //
          "1. You selected a previous version in your history view. If this is the case and you want to return to the current version, click on Current in the history view. " + //
          "If you want to replace the current version with a previous version, select Replace from the History context menu.<br/><br/>" + //
          "2. You are viewing published content. If you want to derive a new version from published content, drag and drop the content from the Outline panel to a new Editor frame.<br/><br/>" + //
          "3. You are viewing content created by another author who has given you read-only access. If you need write access, contact the author. " + //
          "If you would like to update a workflow task, click on the task and select Update Status from the context menu.", null);
    }
    else if (throwable instanceof HistoryCacheMissException)
    {
      MessageBox.alert("History", "Selected item is not in cache, please clear selection and try again.", null);
    }
    else
    {
      Utility.displayStackTrace(throwable);
    }
  }

}
