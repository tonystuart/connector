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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Frame;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.ConnectorWindow;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.shared.UrlConstants;

public final class LicenseWindow extends ConnectorWindow
{
  private CheckBox acceptLicenseCheckBox;

  private Frame frame;
  private OkayCancelHandler okayCancelHandler;
  private OkayCancelToolBar okayCancelToolBar;

  public LicenseWindow(OkayCancelHandler okayCancelHandler)
  {
    this.okayCancelHandler = okayCancelHandler;

    setHeading("License Agreement");
    setIcon(Resources.LICENSE_AGREEMENT);
    setSize(600, 400);
    setLayout(new FitLayout());
    setClosable(false);
    add(getLicenseFrame());
    setBottomComponent(getOkayCancelToolBar());
    updateFormState();
  }

  private CheckBox getAcceptLicenseCheckBox()
  {
    if (acceptLicenseCheckBox == null)
    {
      acceptLicenseCheckBox = new CheckBox()
      {
        @Override
        protected void onClick(ComponentEvent ce)
        {
          super.onClick(ce);
          updateFormState();
        }
      };
      acceptLicenseCheckBox.setBoxLabel("I accept the terms of this agreement");
    }
    return acceptLicenseCheckBox;
  }

  private Frame getLicenseFrame()
  {
    if (frame == null)
    {
      frame = new Frame(); // defer set of url until iframe is attached
      frame.getElement().setPropertyInt("frameBorder", 0);
      frame.getElement().getStyle().setBackgroundColor("white");
    }
    return frame;
  }

  private OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(new LicenseTermsHandler());
      okayCancelToolBar.insert(getAcceptLicenseCheckBox(), 0);
    }
    return okayCancelToolBar;
  }

  @Override
  protected void onRender(Element parent, int pos)
  {
    super.onRender(parent, pos);

    Scheduler.get().scheduleFixedDelay(new RepeatingCommand()
    {
      @Override
      public boolean execute()
      {
        ClientUrlBuilder clientUrlBuilder = new ClientUrlBuilder(UrlConstants.URL_STATIC_LICENSE);
        String url = clientUrlBuilder.toString();
        frame.setUrl(url);
        return false;
      }
    }, 1000);
  }

  protected void updateFormState()
  {
    getOkayCancelToolBar().getOkayButton().setEnabled(getAcceptLicenseCheckBox().getValue());
  }

  private final class LicenseTermsHandler implements OkayCancelHandler
  {
    @Override
    public void cancel()
    {
      hide();
      okayCancelHandler.cancel();
    }

    @Override
    public void okay()
    {
      hide();
      okayCancelHandler.okay();// updateFormState prevents okay() without checking the box
    }
  }
}
