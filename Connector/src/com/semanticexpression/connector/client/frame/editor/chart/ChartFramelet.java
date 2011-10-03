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

import java.util.List;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.semanticexpression.connector.client.ClientUrlBuilder;
import com.semanticexpression.connector.client.frame.editor.ContentReference;
import com.semanticexpression.connector.client.frame.editor.EditorFrame;
import com.semanticexpression.connector.client.frame.editor.EditorFrame.ModificationContext;
import com.semanticexpression.connector.client.frame.editor.image.BaseImageFramelet;
import com.semanticexpression.connector.client.frame.editor.image.BaseImagePropertyEditorWindow;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.ChartSpecificationFormatter;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.DefaultProperties;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Properties;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.UrlBuilder;
import com.semanticexpression.connector.shared.UrlConstants;
import com.semanticexpression.connector.shared.enums.ChartType;
import com.semanticexpression.connector.shared.enums.ContentType;

public class ChartFramelet extends BaseImageFramelet
{
  private EditorFrame editorFrame;
  private ContentReference tableContentReference;

  public ChartFramelet(EditorFrame editorFrame, ModificationContext modificationContext)
  {
    super("Chart", Resources.CHART);

    this.editorFrame = editorFrame;
  }

  public void changeType(ChartType chartType)
  {
    if (contentReference.get(Keys.CHART_TYPE) != chartType)
    {
      contentReference.set(Keys.CHART_TYPE, chartType);
      display();
    }
  }

  @Override
  protected Menu createImageMenu()
  {
    return new ChartMenu(this);
  }

  @Override
  protected BaseImagePropertyEditorWindow createPropertyEditorWindow()
  {
    return new ChartPropertyEditorWindow(this);
  }

  public Integer getColumnCount()
  {
    Integer columnCount = null;
    if (tableContentReference != null)
    {
      Sequence<Association> columns = tableContentReference.get(Keys.TABLE_COLUMNS);
      if (columns != null)
      {
        columnCount = columns.size();
      }
    }
    return columnCount;
  }

  @Override
  protected Integer getImageHeight()
  {
    Integer imageHeight;
    if (isScaleToFit)
    {
      imageHeight = getImageHtml().getHeight();
    }
    else
    {
      imageHeight = contentReference.get(Keys.CHART_HEIGHT, ChartSpecificationFormatter.DS_CHART_HEIGHT);
    }
    return imageHeight;
  }

  @Override
  protected String getImageUrlString()
  {
    String imageUrlString = "";
    tableContentReference = getTable();
    if (tableContentReference == null)
    {
      MessageBox.alert("Missing Table", "Please place this Chart after a Table of data", null);
    }
    else
    {
      Content chartContent = contentReference.getBaseContent();
      Content tableContent = tableContentReference.getBaseContent();
      ChartSpecificationFormatter chartSpecificationFormatter = new ClientChartSpecificationFormatter(chartContent, tableContent, "&amp;");
      try
      {
        String chartSpecification = chartSpecificationFormatter.getChartSpecification();
        UrlBuilder urlBuilder = new ClientUrlBuilder(UrlConstants.URL_CHART);
        urlBuilder.addQueryString(chartSpecification);
        imageUrlString = urlBuilder.toString();
      }
      catch (IllegalArgumentException e)
      {
        MessageBox.alert("Invalid Table Data or Chart Properties", "Cannot create chart: " + e.getMessage(), null);
      }
    }
    return imageUrlString;
  }

  @Override
  protected Integer getImageWidth()
  {
    Integer imageWidth;
    if (isScaleToFit)
    {
      imageWidth = getImageHtml().getWidth();
    }
    else
    {
      imageWidth = contentReference.get(Keys.CHART_WIDTH, ChartSpecificationFormatter.DS_CHART_WIDTH);
    }
    return imageWidth;
  }

  public Integer getRowCount()
  {
    Integer rowCount = null;
    if (tableContentReference != null)
    {
      Sequence<Association> rows = tableContentReference.get(Keys.TABLE_ROWS);
      rowCount = rows.size();
    }
    return rowCount;
  }

  @Override
  protected String getScaledDimension()
  {
    // Charts interpret scale-to-fit as size-to-fit so no additional scaling is necessary
    return "";
  }

  private ContentReference getTable()
  {
    ContentReference table = null;
    Sequence<Association> sequence = contentReference.get(Keys.PROPERTIES);
    Properties properties = new Properties(sequence);
    String tableOverride = properties.get(DefaultProperties.TABLE_OVERRIDE, null);
    if (tableOverride == null)
    {
      List<ContentReference> tables = editorFrame.getContext(contentReference, ContentType.TABLE);
      int tableCount = tables.size();
      if (tableCount > 0)
      {
        table = tables.get(tableCount - 1);
      }
    }
    else
    {
      Id tableOverrideId = new Id(tableOverride);
      List<ContentReference> contentReferences = editorFrame.findContentReferences(tableOverrideId);
      if (contentReferences.size() > 0)
      {
        table = contentReferences.get(0);
      }
    }
    return table;
  }

  public class ClientChartSpecificationFormatter extends ChartSpecificationFormatter
  {

    public ClientChartSpecificationFormatter(Content chartContent, Content tableContent, String delimiter)
    {
      super(chartContent, tableContent, delimiter);
    }

    @Override
    protected int getChartHeight(Content chartContent)
    {
      return getImageHeight();
    }

    @Override
    protected int getChartWidth(Content chartContent)
    {
      return getImageWidth();
    }

  }

}
