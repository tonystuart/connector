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

package com.semanticexpression.connector.client.frame.editor.chart;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.image.BaseImagePropertyEditorWindow;
import com.semanticexpression.connector.client.widget.Numeric;
import com.semanticexpression.connector.shared.ChartSpecificationFormatter;
import com.semanticexpression.connector.shared.Keys;

public class ChartPropertyEditorWindow extends BaseImagePropertyEditorWindow
{
  private ChartFramelet chartFramelet;
  private Numeric chartHeightNumeric;
  private Text chartHeightPixelsText;
  private Numeric chartWidthNumeric;
  private Text chartWidthPixelsText;
  private Numeric firstColumnNumeric;
  private Text firstColumnText;
  private Numeric firstRowNumeric;
  private Text firstRowText;
  private Numeric lastColumnNumeric;
  private Text lastColumnText;
  private Numeric lastRowNumeric;
  private Text lastRowText;
  private Numeric legendColumnNumeric;
  private Text legendColumnText;

  public ChartPropertyEditorWindow(ChartFramelet chartFramelet)
  {
    this.chartFramelet = chartFramelet;
    
    setSize(300, 375);
    setHeading("Chart Properties");
    setLayout(new FitLayout());
    Margins labelMargins = new Margins(5, 0, 0, 0);
    LayoutContainer c = new LayoutContainer(); // work around for GXT.isIE margin issue
    c.setLayout(new RowLayout(Orientation.VERTICAL));
    c.add(getFirstRowText(), new RowData(Style.DEFAULT, Style.DEFAULT));
    c.add(getFirstRowNumeric(), new RowData(1.0, Style.DEFAULT));
    c.add(getLastRowText(), new RowData(Style.DEFAULT, Style.DEFAULT, labelMargins));
    c.add(getLastRowNumeric(), new RowData(1.0, Style.DEFAULT));
    c.add(getLegendColumnText(), new RowData(Style.DEFAULT, Style.DEFAULT, labelMargins));
    c.add(getLegendColumnNumeric(), new RowData(1.0, Style.DEFAULT));
    c.add(getFirstColumnText(), new RowData(Style.DEFAULT, Style.DEFAULT, labelMargins));
    c.add(getFirstColumnNumeric(), new RowData(1.0, Style.DEFAULT));
    c.add(getLastColumnText(), new RowData(Style.DEFAULT, Style.DEFAULT, labelMargins));
    c.add(getLastColumnNumeric(), new RowData(1.0, Style.DEFAULT));
    c.add(getChartWidthPixelsText(), new RowData(Style.DEFAULT, Style.DEFAULT, labelMargins));
    c.add(getChartWidthNumeric(), new RowData(1.0, Style.DEFAULT));
    c.add(getChartHeightPixelsText(), new RowData(Style.DEFAULT, Style.DEFAULT, labelMargins));
    c.add(getChartHeightNumeric(), new RowData(1.0, Style.DEFAULT));
    add(c, new FitData(5));
  }

  @Override
  public void cancel()
  {
    super.cancel();
  }

  @Override
  public void edit(ContentReference contentReference)
  {
    super.edit(contentReference);

    Integer firstRow = contentReference.get(Keys.CHART_FIRST_ROW);
    Integer lastRow = contentReference.get(Keys.CHART_LAST_ROW);
    Integer legendColumn = contentReference.get(Keys.CHART_LEGEND_COLUMN);
    Integer firstColumn = contentReference.get(Keys.CHART_FIRST_COLUMN);
    Integer lastColumn = contentReference.get(Keys.CHART_LAST_COLUMN);
    Integer chartWidth = contentReference.get(Keys.CHART_WIDTH);
    Integer chartHeight = contentReference.get(Keys.CHART_HEIGHT);

    Integer rowCount = chartFramelet.getRowCount();
    Integer columnCount = chartFramelet.getColumnCount();
    
    getFirstRowNumeric().setValue(firstRow, rowCount);
    getLastRowNumeric().setValue(lastRow, rowCount);
    getLegendColumnNumeric().setValue(legendColumn, columnCount);
    getFirstColumnNumeric().setValue(firstColumn, columnCount);
    getLastColumnNumeric().setValue(lastColumn, columnCount);
    getChartWidthNumeric().setValue(chartWidth);
    getChartHeightNumeric().setValue(chartHeight);
  }

  public Numeric getChartHeightNumeric()
  {
    if (chartHeightNumeric == null)
    {
      chartHeightNumeric = new Numeric("Defaults to " + ChartSpecificationFormatter.DS_CHART_HEIGHT);
    }
    return chartHeightNumeric;
  }

  public Text getChartHeightPixelsText()
  {
    if (chartHeightPixelsText == null)
    {
      chartHeightPixelsText = new Text("Chart Height (pixels):");
    }
    return chartHeightPixelsText;
  }

  public Numeric getChartWidthNumeric()
  {
    if (chartWidthNumeric == null)
    {
      chartWidthNumeric = new Numeric("Defaults to " + ChartSpecificationFormatter.DS_CHART_WIDTH);
    }
    return chartWidthNumeric;
  }

  public Text getChartWidthPixelsText()
  {
    if (chartWidthPixelsText == null)
    {
      chartWidthPixelsText = new Text("Chart Width (pixels):");
    }
    return chartWidthPixelsText;
  }

  public Numeric getFirstColumnNumeric()
  {
    if (firstColumnNumeric == null)
    {
      firstColumnNumeric = new Numeric("Defaults to second column in table");
    }
    return firstColumnNumeric;
  }

  public Text getFirstColumnText()
  {
    if (firstColumnText == null)
    {
      firstColumnText = new Text("First Column of Chart Data:");
    }
    return firstColumnText;
  }

  public Numeric getFirstRowNumeric()
  {
    if (firstRowNumeric == null)
    {
      firstRowNumeric = new Numeric("Defaults to first row in table");
    }
    return firstRowNumeric;
  }

  public Text getFirstRowText()
  {
    if (firstRowText == null)
    {
      firstRowText = new Text("First Row of Chart Data:");
    }
    return firstRowText;
  }

  public Numeric getLastColumnNumeric()
  {
    if (lastColumnNumeric == null)
    {
      lastColumnNumeric = new Numeric("Defaults to last column in table");
    }
    return lastColumnNumeric;
  }

  public Text getLastColumnText()
  {
    if (lastColumnText == null)
    {
      lastColumnText = new Text("Last Column of Chart Data:");
    }
    return lastColumnText;
  }

  public Numeric getLastRowNumeric()
  {
    if (lastRowNumeric == null)
    {
      lastRowNumeric = new Numeric("Defaults to last row in table");
    }
    return lastRowNumeric;
  }

  public Text getLastRowText()
  {
    if (lastRowText == null)
    {
      lastRowText = new Text("Last Row of Chart Data:");
    }
    return lastRowText;
  }

  public Numeric getLegendColumnNumeric()
  {
    if (legendColumnNumeric == null)
    {
      legendColumnNumeric = new Numeric("Defaults to first column in table");
    }
    return legendColumnNumeric;
  }

  public Text getLegendColumnText()
  {
    if (legendColumnText == null)
    {
      legendColumnText = new Text("Legend Column:");
    }
    return legendColumnText;
  }

  @Override
  public void okay()
  {
    contentReference.set(Keys.CHART_WIDTH, getChartWidthNumeric().getValue());
    contentReference.set(Keys.CHART_HEIGHT, getChartHeightNumeric().getValue());
    contentReference.set(Keys.CHART_FIRST_ROW, getFirstRowNumeric().getValue());
    contentReference.set(Keys.CHART_LAST_ROW, getLastRowNumeric().getValue());
    contentReference.set(Keys.CHART_LEGEND_COLUMN, getLegendColumnNumeric().getValue());
    contentReference.set(Keys.CHART_FIRST_COLUMN, getFirstColumnNumeric().getValue());
    contentReference.set(Keys.CHART_LAST_COLUMN, getLastColumnNumeric().getValue());
    chartFramelet.display();
    super.okay();
  }
}
