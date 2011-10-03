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

import java.util.Iterator;

import com.semanticexpression.connector.shared.enums.ChartType;

public class ChartSpecificationFormatter
{
  private static final String[] colors = new String[] {
      "F08080",
      "80F080",
      "8080F0",
      "F0F080",
      "80F0F0",
      "F080F0",

      "F0C0C0",
      "C0F0C0",
      "C0C0F0",
      "F0F0C0",
      "C0F0F0",
      "F0C0F0",
  };

  public static final Integer DS_CHART_HEIGHT = 250;
  public static final Integer DS_CHART_WIDTH = 400;
  public static final ChartType DT_CHART_TYPE = ChartType.BAR;

  private Content chartContent;
  private String delimiter;
  private Content tableContent;

  public ChartSpecificationFormatter(Content chartContent, Content tableContent, String delimiter)
  {
    this.chartContent = chartContent;
    this.tableContent = tableContent;
    this.delimiter = delimiter;
  }

  private String formatAxisLabels(int firstColumnOffset, int lastColumnOffset)
  {
    StringBuilder s = new StringBuilder();

    Sequence<Association> columns = tableContent.get(Keys.TABLE_COLUMNS);
    if (columns != null)
    {
      s.append("chxl=0:"); // zero is index into chxt parameter
      int columnOffset = 0;
      for (Association column : columns)
      {
        if (firstColumnOffset <= columnOffset && columnOffset <= lastColumnOffset)
        {
          String label = column.get(Keys.NAME, "");
          s.append("|");
          s.append(label.replaceAll("\\s+", "+"));
        }
        columnOffset++;
      }
    }

    return s.toString();

  }

  private String formatChartSize(int chartWidth, int chartHeight)
  {
    StringBuilder s = new StringBuilder("chs=");
    s.append(chartWidth);
    s.append("x");
    s.append(chartHeight);
    return s.toString();
  }

  private String formatChartType(ChartType chartType)
  {
    StringBuilder s = new StringBuilder();

    switch (chartType)
    {
      case BAR:
        s.append("cht=bvg");
        break;
      case LINE:
        s.append("cht=lc");
        break;
      case PIE:
        s.append("cht=p");
        break;
      default:
        throw new IllegalArgumentException();
    }

    return s.toString();
  }

  private String formatLegend(Sequence<Association> rows, int firstRowOffset, int lastRowOffset, int legendColumnOffset)
  {
    StringBuilder s = new StringBuilder("chdl=");
    String legendColumnName = getColumnName(legendColumnOffset);

    int rowOffset = 0;
    Iterator<Association> iterator = rows.iterator();

    while (rowOffset <= lastRowOffset && iterator.hasNext())
    {
      Association row = iterator.next();
      if (rowOffset >= firstRowOffset)
      {
        if (rowOffset > firstRowOffset)
        {
          s.append("|");
        }
        String label = row.get(legendColumnName, "");
        s.append(label.replaceAll("\\s+", "+"));
      }
      rowOffset++;
    }

    return s.toString();
  }

  private String formatSeriesColors(int firstRowOffset, int lastRowOffset)
  {
    StringBuilder s = new StringBuilder("chco=");

    int colorOffset = 0;
    for (int columnOffset = firstRowOffset; columnOffset <= lastRowOffset; columnOffset++)
    {
      if (columnOffset > firstRowOffset)
      {
        s.append(",");
      }
      s.append(colors[colorOffset++ % colors.length]);
    }

    return s.toString();
  }

  private String formatTableData(Sequence<Association> rows, int firstRowOffset, int lastRowOffset, int firstColumnOffset, int lastColumnOffset, String rowDataDelimiter)
  {
    StringBuilder s = new StringBuilder("chd=t:");

    double scaleMin = 0;
    double scaleMax = Double.MIN_NORMAL;

    int rowOffset = 0;
    Iterator<Association> iterator = rows.iterator();

    while (rowOffset <= lastRowOffset && iterator.hasNext())
    {
      Association row = iterator.next();
      if (rowOffset >= firstRowOffset)
      {
        if (rowOffset > firstRowOffset)
        {
          s.append(rowDataDelimiter);
        }
        for (int columnOffset = firstColumnOffset; columnOffset <= lastColumnOffset; columnOffset++)
        {
          if (columnOffset > firstColumnOffset)
          {
            s.append(",");
          }
          double floatValue = getDouble(row.get(getColumnName(columnOffset), 0d));
          scaleMin = Math.min(scaleMin, floatValue);
          scaleMax = Math.max(scaleMax, floatValue);
          s.append(floatValue);
        }
      }
      rowOffset++;
    }

    s.append(delimiter);

    double border = 0.05 * (scaleMax - scaleMin);
    scaleMin -= border;
    scaleMax += border;

    s.append("chxr="); // range
    s.append("1,"); // one is index into chxt parameter
    s.append(scaleMin);
    s.append(",");
    s.append(scaleMax);

    s.append(delimiter);

    s.append("chds="); // scale
    s.append(scaleMin);
    s.append(",");
    s.append(scaleMax);

    return s.toString();
  }

  private String formatTitle(String title)
  {
    StringBuilder s = new StringBuilder("chtt=");
    s.append(title.replaceAll("\\s+", "+"));
    return s.toString();
  }

  protected int getChartHeight(Content chartContent)
  {
    return chartContent.get(Keys.CHART_HEIGHT, DS_CHART_HEIGHT);
  }

  public String getChartSpecification()
  {
    ChartType chartType = chartContent.get(Keys.CHART_TYPE, DT_CHART_TYPE);

    Sequence<Association> rows = tableContent.get(Keys.TABLE_ROWS);
    if (rows == null)
    {
      throw new IllegalArgumentException("No Rows");
    }

    int rowCount = rows.size();
    int columnCount = getColumnCount();

    String title = chartContent.get(Keys.TITLE, "");
    int chartWidth = getChartWidth(chartContent);
    int chartHeight = getChartHeight(chartContent);
    int firstRowOffset = chartContent.get(Keys.CHART_FIRST_ROW, 1) - 1;
    int lastRowOffset = chartContent.get(Keys.CHART_LAST_ROW, rowCount) - 1;
    int legendColumnOffset = chartContent.get(Keys.CHART_LEGEND_COLUMN, 1) - 1;
    int firstColumnOffset = chartContent.get(Keys.CHART_FIRST_COLUMN, 2) - 1;
    int lastColumnOffset = chartContent.get(Keys.CHART_LAST_COLUMN, columnCount) - 1;

    if (firstRowOffset < 0 || firstRowOffset >= rowCount)
    {
      throw new IllegalArgumentException("First Row is out of range");
    }
    if (lastRowOffset < 0 || lastRowOffset >= rowCount)
    {
      throw new IllegalArgumentException("Last Row is out of range");
    }
    if (legendColumnOffset < 0 || legendColumnOffset >= columnCount)
    {
      throw new IllegalArgumentException("Legend Column is out of range");
    }
    if (firstColumnOffset < 0 || firstColumnOffset >= columnCount)
    {
      throw new IllegalArgumentException("First Column is out of range");
    }
    if (lastColumnOffset < 0 || lastColumnOffset >= columnCount)
    {
      throw new IllegalArgumentException("Last Column is out of range");
    }

    String rowDataDelimiter = "|";

    if (chartType == ChartType.PIE)
    {
      lastColumnOffset = firstColumnOffset;
      rowDataDelimiter = ",";
      title = title + " - " + getColumnName(firstColumnOffset);
    }

    StringBuilder s = new StringBuilder();

    s.append(formatChartType(chartType));
    s.append(delimiter);

    s.append("chxt=x,y"); // visible axis list
    s.append(delimiter);

    s.append(formatTableData(rows, firstRowOffset, lastRowOffset, firstColumnOffset, lastColumnOffset, rowDataDelimiter));
    s.append(delimiter);

    s.append(formatChartSize(chartWidth, chartHeight));
    s.append(delimiter);

    s.append(formatAxisLabels(firstColumnOffset, lastColumnOffset));
    s.append(delimiter);

    s.append(formatLegend(rows, firstRowOffset, lastRowOffset, legendColumnOffset));
    s.append(delimiter);

    s.append(formatSeriesColors(firstRowOffset, lastRowOffset));
    s.append(delimiter);

    s.append(formatTitle(title));
    s.append(delimiter);

    s.append("time=" + System.currentTimeMillis()); // defeats browser caching

    return s.toString();
  }

  protected int getChartWidth(Content chartContent)
  {
    return chartContent.get(Keys.CHART_WIDTH, DS_CHART_WIDTH);
  }

  private int getColumnCount()
  {
    Sequence<Association> columns = tableContent.get(Keys.TABLE_COLUMNS);
    int columnCount = columns == null ? 0 : columns.size();
    return columnCount;
  }

  private String getColumnName(int columnOffset)
  {
    String columnName = "";
    Sequence<Association> columns = tableContent.get(Keys.TABLE_COLUMNS);
    if (columns != null && (0 <= columnOffset && columnOffset < columns.size()))
    {
      columnName = columns.get(columnOffset).get(Keys.NAME, "");
    }
    return columnName;
  }

  public double getDouble(Object objectValue)
  {
    double doubleValue;
    try
    {
      if (objectValue instanceof Number)
      {
        doubleValue = ((Number)objectValue).doubleValue();
      }
      else
      {
        doubleValue = Double.parseDouble(objectValue.toString());
      }
    }
    catch (RuntimeException e) // e.g. NumberFormatException, NullPointerException, 
    {
      doubleValue = 0;
    }
    return doubleValue;
  }

}
