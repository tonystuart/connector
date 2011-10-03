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

package com.semanticexpression.connector.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.DefaultProperties;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Properties;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.UrlBuilder;
import com.semanticexpression.connector.shared.UrlConstants;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthorizationException;

// TODO: Rationalize class life-cycle and fields
// TODO: Use stack for globalProperties with !important to invert order
// TODO: Rewrite parser in writeStyles to collapse white space
// TODO: Remove trim() once white space is collapsed

/**
 * Please see the following:
 * 
 * <pre>
 * <a href='http://www.w3.org/TR/html4/cover.html#minitoc>HTML 4.01 Specification</a>
 * <a href='http://dev.w3.org/html5/spec/Overview.html>HTML 5 Draft Specification</a>
 * <a href='http://www.w3.org/TR/CSS21/cover.html#minitoc'>Cascading Style Sheets Level 2 Revision 1 (CSS 2.1) Specification</a>
 * </pre>
 */
public class HtmlFormatter
{
  private static final String UNICODE = "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
  public static final String DOCTYPE = HtmlConstants.HTML5_DOCTYPE;
  public static final String MULTIPLE_WHITESPACE = "[ \t\n\r]+";

  private static HtmlWriter getWriter(HttpServletResponse response)
  {
    try
    {
      // Must set content type before getting writer
      response.setContentType("text/html");
      response.setCharacterEncoding("UTF-8");
      PrintWriter printWriter = response.getWriter();
      HtmlWriter htmlWriter = new HtmlWriter(printWriter);
      return htmlWriter;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  protected Connection connection;
  protected Map<String, Object> globalProperties = new HashMap<String, Object>();
  protected HtmlWriter htmlWriter;
  private int nestedStyleCounter;
  protected Date presentAt;
  protected RetrieveOperation retrieveOperation;
  protected Map<Id, Integer> styleCards = new LinkedHashMap<Id, Integer>(); // must be emitted in order encountered
  private LinkedList<Id> tableStack = new LinkedList<Id>();
  protected Id userId;

  public HtmlFormatter(RetrieveOperation retrieveOperation, Connection connection, Id userId, Date presentAt, HtmlWriter htmlWriter)
  {
    this.retrieveOperation = retrieveOperation;
    this.connection = connection;
    this.userId = userId;
    this.presentAt = presentAt;
    this.htmlWriter = htmlWriter;
  }

  public HtmlFormatter(RetrieveOperation retrieveOperation, Connection connection, Id userId, Date presentAt, HttpServletResponse response)
  {
    this(retrieveOperation, connection, userId, presentAt, getWriter(response));
  }

  public HtmlFormatter(RetrieveOperation retrieveOperation, Connection connection, Id userId, Date presentAt, HtmlWriter htmlWriter, Map<Id, Integer> styleCards)
  {
    this(retrieveOperation, connection, userId, presentAt, htmlWriter);
    this.styleCards = styleCards;
  }

  public void collectStyles(Id contentId, int level)
  {

    ContentType contentType = (ContentType)getValue(contentId, Keys.CONTENT_TYPE);

    switch (contentType)
    {
      case DOCUMENT:
      {
        if (isInStylePath(contentId, level))
        {
          @SuppressWarnings("unchecked")
          List<Id> parts = (List<Id>)getValue(contentId, Keys.PARTS);
          if (parts != null)
          {
            for (Id part : parts)
            {
              collectStyles(part, level + 1);
            }
          }
        }
        break;
      }
      case STYLE:
      {
        styleCards.put(contentId, styleCards.size() + 1);
        break;
      }
    }

  }

  private void formatBaseImage(Id contentId, String src, String alt, String className)
  {
    htmlWriter.writeBegin("div", className, contentId);
    htmlWriter.writeImage(src, alt);

    String caption = (String)getValue(contentId, Keys.CAPTION);
    if (caption != null)
    {
      caption = resolveSubstitutions(caption);
      htmlWriter.writeTag("div", caption, "class", HtmlConstants.SE_MC_CAPTION);
    }

    htmlWriter.writeEnd("div");
  }

  private void formatChart(Id contentId, Properties contentProperties)
  {
    Id tableId;
    String tableOverride = contentProperties.get(DefaultProperties.TABLE_OVERRIDE, null);
    if (tableOverride == null)
    {
      if (tableStack.size() == 0)
      {
        throw new IllegalStateException("Chart " + contentId + " appears without a corresponding Table of data");
      }
      tableId = tableStack.peek();
    }
    else
    {
      tableId = new Id(tableOverride);
    }

    UrlBuilder urlBuilder = new ServerUrlBuilder(UrlConstants.URL_CONTENT);
    urlBuilder.addParameter(UrlConstants.PARAMETER_ID, contentId.formatString());
    urlBuilder.addParameter(UrlConstants.PARAMETER_PRESENT_AT, presentAt);
    urlBuilder.addParameter(UrlConstants.PARAMETER_TABLE, tableId.formatString());

    String src = urlBuilder.toString();
    String alt = (String)getValue(contentId, Keys.TITLE);

    formatBaseImage(contentId, src, alt, HtmlConstants.SE_MC_CHART);
  }

  public void formatContent(Id contentId) throws AuthorizationException
  {
    collectStyles(contentId, 0);

    htmlWriter.writeText(DOCTYPE);
    htmlWriter.writeBegin("html");
    htmlWriter.writeBegin("head");
    htmlWriter.writeText(UNICODE); // required when output is saved as static web pages that are served

    String title = getTitle(contentId);
    if (title != null)
    {
      htmlWriter.writeTag("title", title);
    }

    if (styleCards.size() > 0)
    {
      writeStyles();
    }

    htmlWriter.writeEnd("head");
    htmlWriter.writeBegin("body");

    formatContent(contentId, 0);

    htmlWriter.writeEnd("body");
    htmlWriter.writeEnd("html");
  }

  public void formatContent(Id contentId, int level) throws AuthorizationException
  {
    retrieveOperation.validateReadAccess(connection, userId, contentId);

    Properties contentProperties = retrieveOperation.repository.retrieveProperties(connection, contentId, Keys.PROPERTIES);

    for (Association contentProperty : contentProperties.getProperties())
    {
      String name = contentProperty.get(Keys.NAME);
      if (!globalProperties.containsKey(name)) // first occurrence takes precedence
      {
        Object value = contentProperty.get(Keys.VALUE);
        globalProperties.put(name, value);
      }
    }

    ContentType contentType = (ContentType)getValue(contentId, Keys.CONTENT_TYPE);
    if (isInContentPath(contentId, level, contentType))
    {
      switch (contentType)
      {
        case CHART:
          formatChart(contentId, contentProperties);
          break;
        case DOCUMENT:
          formatDocument(contentId, level, contentProperties);
          break;
        case IMAGE:
          formatImage(contentId);
          break;
        case STYLE:
          formatStyle(contentId);
          break;
        case TABLE:
          formatTable(contentId);
          break;
        case TEXT:
          formatText(contentId, level, contentProperties);
          break;
      }

    }
  }

  protected void formatDocument(Id contentId, int level, Properties contentProperties) throws AuthorizationException
  {
    formatDocumentContent(contentId, level, contentProperties);
  }

  protected void formatDocumentContent(Id contentId, int level, Properties contentProperties) throws AuthorizationException
  {
    htmlWriter.writeBegin("div", HtmlConstants.SE_MC_DOCUMENT, contentId);

    int tableCount = tableStack.size();

    int nestedStyleCounter = this.nestedStyleCounter;

    if (level > 0) // level zero is HTML title
    {
      if (isIncludedTitle(level))
      {
        formatHeading(contentId, level, contentProperties);
      }
    }

    @SuppressWarnings("unchecked")
    List<Id> parts = (List<Id>)getValue(contentId, Keys.PARTS);
    if (parts != null)
    {
      for (Id part : parts)
      {
        formatContent(part, level + 1);
      }
    }

    while (this.nestedStyleCounter > nestedStyleCounter) // styles persist through end of *containing* div
    {
      htmlWriter.writeEnd("div");
      this.nestedStyleCounter--;
    }

    while (tableStack.size() > tableCount)
    {
      tableStack.pop();
    }

    htmlWriter.writeEnd("div");
  }

  protected void formatHeading(Id contentId, int level, Properties properties)
  {
    Boolean isIncludeTitleAsContent = properties.get(DefaultProperties.INCLUDE_TITLE_AS_CONTENT, false);
    if (isIncludeTitleAsContent)
    {
      String title = (String)getValue(contentId, Keys.TITLE);
      if (title != null)
      {
        if (level == 0)
        {
          // Level is 0 when content is formatted without a document
          level = 1;
        }
        htmlWriter.writeHeading(level, title);
      }
    }
  }

  private void formatImage(Id contentId)
  {
    UrlBuilder urlBuilder = new ServerUrlBuilder(UrlConstants.URL_CONTENT);
    urlBuilder.addParameter(UrlConstants.PARAMETER_ID, contentId);
    urlBuilder.addParameter(UrlConstants.PARAMETER_PRESENT_AT, presentAt);

    String src = urlBuilder.toString();
    String alt = (String)getValue(contentId, Keys.TITLE);

    formatBaseImage(contentId, src, alt, HtmlConstants.SE_MC_IMAGE);
  }

  private void formatStyle(Id contentId)
  {
    int styleId = styleCards.get(contentId);
    htmlWriter.writeBegin("div", styleId, HtmlConstants.SE_MC_STYLE, contentId);
    nestedStyleCounter++;
  }

  private void formatTable(Id contentId)
  {
    htmlWriter.writeBegin("div", HtmlConstants.SE_MC_TABLE, contentId);

    htmlWriter.writeBegin("table");

    @SuppressWarnings("unchecked")
    Sequence<Association> columns = (Sequence<Association>)getValue(contentId, Keys.TABLE_COLUMNS);
    if (columns != null)
    {
      htmlWriter.writeBegin("tr");
      for (Association column : columns)
      {
        htmlWriter.writeBegin("th");
        String columnHeading = column.get(Keys.NAME);
        columnHeading = resolveSubstitutions(columnHeading);
        htmlWriter.writeText(columnHeading);
        htmlWriter.writeEnd("th");
      }
      htmlWriter.writeEnd("tr");

      @SuppressWarnings("unchecked")
      Sequence<Association> rows = (Sequence<Association>)getValue(contentId, Keys.TABLE_ROWS);
      if (rows != null)
      {
        int rowCount = 0;
        for (Association row : rows)
        {
          String className = rowCount % 2 == 0 ? HtmlConstants.SE_MC_ROW_EVEN : HtmlConstants.SE_MC_ROW_ODD;
          htmlWriter.writeBegin("tr", "class", className);
          for (Association column : columns)
          {
            htmlWriter.writeBegin("td");
            String columnName = column.get(Keys.NAME);
            Object cellValue = row.get(columnName);
            if (cellValue instanceof String)
            {
              cellValue = resolveSubstitutions((String)cellValue);
            }
            else if (cellValue == null)
            {
              cellValue = "";
            }
            htmlWriter.writeText(cellValue.toString());
            htmlWriter.writeEnd("td");
          }
          htmlWriter.writeEnd("tr");
          rowCount++;
        }
      }
    }
    htmlWriter.writeEnd("table");

    String caption = (String)getValue(contentId, Keys.CAPTION);
    if (caption != null)
    {
      caption = resolveSubstitutions(caption);
      htmlWriter.writeTag("div", caption, "class", HtmlConstants.SE_MC_CAPTION);
    }

    htmlWriter.writeEnd("div");

    tableStack.push(contentId);
  }

  private void formatText(Id contentId, int level, Properties properties)
  {
    htmlWriter.writeBegin("div", HtmlConstants.SE_MC_TEXT, contentId);

    formatHeading(contentId, level, properties);

    String text = (String)getValue(contentId, Keys.TEXT);
    if (text != null)
    {
      text = resolveSubstitutions(text);
      htmlWriter.writeText(text);
    }

    htmlWriter.writeEnd("div");
  }

  protected String getTitle(Id contentId)
  {
    return (String)getValue(contentId, Keys.TITLE);
  }

  protected Object getValue(Id contentId, String name)
  {
    return retrieveOperation.repository.retrieveProperty(connection, contentId, name, presentAt);
  }

  protected boolean isIncludedTitle(int level)
  {
    return true;
  }

  protected boolean isInContentPath(Id contentId, int level, ContentType contentType)
  {
    return true;
  }

  protected boolean isInStylePath(Id contentId, int level)
  {
    return true;
  }

  protected boolean isRootPathStyle(String selector)
  {
    return false;
  }

  private String resolveSubstitutions(String input)
  {
    ParameterSubstituter parameterSubstituter = new ParameterSubstituter();
    String output = parameterSubstituter.substitute(globalProperties, input);
    return output;
  }

  protected void writeStyle(int contentCounter, String selector, String declarations)
  {
    StringBuilder s = new StringBuilder();
    if (isRootPathStyle(selector))
    {
      // Let style default to global scope
    }
    else
    {
      s.append("#");
      s.append(htmlWriter.getElementId(contentCounter));
      s.append(" ");
    }
    s.append(selector);
    s.append(" { ");
    if (declarations != null)
    {
      s.append(declarations);
    }
    s.append(" }");
    htmlWriter.writeText(s.toString().replaceAll(MULTIPLE_WHITESPACE, " "));
  }

  private void writeStyles()
  {
    htmlWriter.writeBegin("style", "type", "text/css");

    for (Entry<Id, Integer> entry : styleCards.entrySet())
    {
      Id contentId = entry.getKey();
      int styleId = entry.getValue();

      String defaultStyleDefinition = (String)getValue(contentId, Keys.STYLE_DEFAULT);
      if (defaultStyleDefinition != null)
      {
        writeStyles(styleId, defaultStyleDefinition);
      }
      @SuppressWarnings("unchecked")
      Sequence<Association> styleDefinitions = (Sequence<Association>)getValue(contentId, Keys.STYLES);
      if (styleDefinitions != null)
      {
        for (Association styleDefinition : styleDefinitions)
        {
          String selector = styleDefinition.get(Keys.STYLE_SELECTOR);
          String declarations = styleDefinition.get(Keys.VALUE);
          writeStyle(styleId, selector, declarations);
        }
      }

    }
    htmlWriter.writeEnd("style");
  }

  private void writeStyles(int contentCounter, LinkedList<StringBuilder> selectors, StringBuilder declarations)
  {
    for (StringBuilder selector : selectors)
    {
      writeStyle(contentCounter, selector.toString(), declarations.toString());
    }
  }

  public void writeStyles(int contentCounter, String definition)
  {
    State state = State.SELECTOR;
    StringBuilder selector = null;
    StringBuilder declarations = null;
    LinkedList<StringBuilder> selectors = null;

    definition = definition.replaceAll(MULTIPLE_WHITESPACE, " ");
    int length = definition.length();

    for (int i = 0; i < length; i++)
    {
      char c = definition.charAt(i);
      switch (state)
      {
        case SELECTOR:
          if (c == ',')
          {
            selector = null;
          }
          else if (c == '{')
          {
            selector = null;
            state = State.DECLARATIONS;
          }
          else
          {
            if (selector == null)
            {
              selector = new StringBuilder();
              if (selectors == null)
              {
                selectors = new LinkedList<StringBuilder>();
              }
              selectors.add(selector);
            }
            selector.append(c);
          }
          break;
        case DECLARATIONS:
          if (c == '}')
          {
            if (selectors != null && declarations != null)
            {
              writeStyles(contentCounter, selectors, declarations);
            }
            selectors = null;
            declarations = null;
            state = State.SELECTOR;
          }
          else
          {
            if (declarations == null)
            {
              declarations = new StringBuilder();
            }
            declarations.append(c);
          }
          break;
      }
    }
  }

  public enum State
  {
    DECLARATIONS, SELECTOR
  }

}