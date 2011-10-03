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

package com.semanticexpression.connector.client.frame.monitor;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.DefaultButtonWindow;
import com.semanticexpression.connector.client.widget.IntegerSpinnerField;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar;
import com.semanticexpression.connector.client.widget.OkayCancelToolBar.OkayCancelHandler;
import com.semanticexpression.connector.client.widget.YesNoField;

public final class MonitorPropertiesWindow extends DefaultButtonWindow
{
  private static final RowData FIELD_ROW_DATA = new RowData(1.0, Style.DEFAULT);
  private static final Margins LABEL_MARGINS = new Margins(5, 0, 0, 0);
  private static final RowData LABEL_ROW_DATA = new RowData(Style.DEFAULT, Style.DEFAULT, LABEL_MARGINS);

  private IntegerSpinnerField maximumRowsSpinnerField;
  private Text maximumRowsText;
  private MonitorFrame monitorFrame;
  private OkayCancelToolBar okayCancelToolBar;
  private Text trackLatestText;
  private YesNoField trackLatestYesNoField;

  public MonitorPropertiesWindow(MonitorFrame monitorFrame)
  {
    this.monitorFrame = monitorFrame;

    setHeading("Monitor Properties");
    setIcon(Resources.MONITOR_PROPERTIES);
    setSize(250, 200);
    setLayout(new FitLayout());
    LayoutContainer c = new LayoutContainer();
    c.setLayout(new RowLayout(Orientation.VERTICAL));
    c.add(getMaximumRowsText(), LABEL_ROW_DATA);
    c.add(getMaximumRowsSpinnerField(), FIELD_ROW_DATA);
    c.add(getTrackLatestText(), LABEL_ROW_DATA);
    c.add(getTrackLatestYesNoField(), FIELD_ROW_DATA);
    add(c, new FitData(5)); // work around for GXT.isIE margin issue
    setBottomComponent(getOkayCancelToolBar());
  }

  public void display(int maximumRows, boolean isTrackLatest)
  {
    getMaximumRowsSpinnerField().setValue(maximumRows);
    getTrackLatestYesNoField().setValue(isTrackLatest);
  }

  private IntegerSpinnerField getMaximumRowsSpinnerField()
  {
    if (maximumRowsSpinnerField == null)
    {
      maximumRowsSpinnerField = new IntegerSpinnerField();
      maximumRowsSpinnerField.setMinValue(0);
      maximumRowsSpinnerField.setAllowNegative(false);
      maximumRowsSpinnerField.setAllowBlank(false);
    }
    return maximumRowsSpinnerField;
  }

  private Text getMaximumRowsText()
  {
    if (maximumRowsText == null)
    {
      maximumRowsText = new Text("Maximum Rows:");
    }
    return maximumRowsText;
  }

  private OkayCancelToolBar getOkayCancelToolBar()
  {
    if (okayCancelToolBar == null)
    {
      okayCancelToolBar = new OkayCancelToolBar(new OkayCancelHandler()
      {

        @Override
        public void cancel()
        {
          hide();
        }

        @Override
        public void okay()
        {
          monitorFrame.setMaximumRows(getMaximumRowsSpinnerField().getValue().intValue());
          monitorFrame.setTrackLatest(getTrackLatestYesNoField().getValue());
          hide();
        }
      });
    }
    return okayCancelToolBar;
  }

  private Text getTrackLatestText()
  {
    if (trackLatestText == null)
    {
      trackLatestText = new Text("Track Latest:");
    }
    return trackLatestText;
  }

  private YesNoField getTrackLatestYesNoField()
  {
    if (trackLatestYesNoField == null)
    {
      trackLatestYesNoField = new YesNoField();
    }
    return trackLatestYesNoField;
  }

}
