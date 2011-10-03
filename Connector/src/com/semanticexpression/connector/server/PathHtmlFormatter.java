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

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.semanticexpression.connector.shared.DefaultProperties;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Properties;
import com.semanticexpression.connector.shared.UrlBuilder;
import com.semanticexpression.connector.shared.UrlConstants;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthorizationException;

public class PathHtmlFormatter extends HtmlFormatter
{
  private Id footer;
  private Id header;
  private boolean isSaveNext;
  private String lastPath;
  private String next;
  private String[] pathComponents;
  private String previous;
  private HtmlFormatter subDocumentFormatter; // enables collection of styles and output of content outside path
  private Map<Id, String> titles = new HashMap<Id, String>();

  public PathHtmlFormatter(RetrieveOperation retrieveOperation, Connection connection, Id userId, Date presentAt, HttpServletResponse response)
  {
    super(retrieveOperation, connection, userId, presentAt, response);
    subDocumentFormatter = new HtmlFormatter(retrieveOperation, connection, userId, presentAt, htmlWriter, styleCards);
  }

  @Override
  protected void formatDocument(Id contentId, int level, Properties contentProperties) throws AuthorizationException
  {
    if (((pathComponents.length - 1) == level))
    {
      formatPathDocument(contentId, level, contentProperties);
    }
    else
    {
      formatDocumentContent(contentId, level, contentProperties);
    }
  }

  public void formatPath(String path) throws AuthorizationException
  {
    pathComponents = path.split("/");
    Id contentId = new Id(pathComponents[0]);

    Properties properties = retrieveOperation.repository.retrieveProperties(connection, contentId, Keys.PROPERTIES);
    String headerString = properties.get(DefaultProperties.PATH_HTML_FORMATTER_HEADER, null);
    if (headerString != null)
    {
      header = new Id(headerString);
      subDocumentFormatter.collectStyles(header, 0);
    }

    String footerString = properties.get(DefaultProperties.PATH_HTML_FORMATTER_FOOTER, null);
    if (footerString != null)
    {
      footer = new Id(footerString);
      subDocumentFormatter.collectStyles(footer, 0);
    }

    formatContent(contentId);
  }

  private void formatPathAnchor(int pathComponentOffset, Id child, boolean isCurrentPath)
  {
    String path = getPathUrl(pathComponentOffset, child);
    String title = (String)getValue(child, Keys.TITLE);

    if (isCurrentPath && pathComponentOffset == pathComponents.length - 1)
    {
      htmlWriter.writeTag("a", title, "href", path, "class", HtmlConstants.SE_MC_PATH_CURRENT);
      previous = lastPath;
      isSaveNext = true;
    }
    else
    {
      htmlWriter.writeTag("a", title, "href", path);
      if (isSaveNext)
      {
        next = path;
        isSaveNext = false;
      }
    }

    lastPath = path;
  }

  private void formatPathDocument(Id contentId, int level, Properties contentProperties) throws AuthorizationException
  {
    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_PAGE);

    if (header != null)
    {
      htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_HEADER);
      subDocumentFormatter.formatContent(header, 0);
      htmlWriter.writeEnd("div");
    }

    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_CONTAINER);

    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_NAVIGATION);
    formatPathRoot();
    htmlWriter.writeEnd("div");

    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_CONTENT);

    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_TOP);
    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_PREVIOUS);
    if (previous != null)
    {
      htmlWriter.writeTag("a", "Previous", "href", previous); // just anchor goes in div to preserve space when null
    }
    htmlWriter.writeEnd("div"); // SE_MC_PATH_PREVIOUS
    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_NEXT);
    if (next != null)
    {
      htmlWriter.writeTag("a", "Next", "href", next); // just anchor goes in div to preserve space when null
    }
    htmlWriter.writeEnd("div"); // SE_MC_PATH_NEXT
    htmlWriter.writeEnd("div"); // SE_MC_PATH_TOP

    formatDocumentContent(contentId, level, contentProperties);

    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_BOTTOM);
    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_PREVIOUS);
    if (previous != null)
    {
      htmlWriter.writeTag("a", "Previous", "href", previous); // just anchor goes in div to preserve space when null
    }
    htmlWriter.writeEnd("div"); // SE_MC_PATH_PREVIOUS
    htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_NEXT);
    if (next != null)
    {
      htmlWriter.writeTag("a", "Next", "href", next); // just anchor goes in div to preserve space when null
    }
    htmlWriter.writeEnd("div"); // SE_MC_PATH_NEXT
    htmlWriter.writeEnd("div"); // SE_MC_PATH_BOTTOM

    htmlWriter.writeEnd("div"); // SE_MC_PATH_CONTENT

    htmlWriter.writeEnd("div"); // SE_MC_PATH_CONTAINER

    if (footer != null)
    {
      htmlWriter.writeBegin("div", "class", HtmlConstants.SE_MC_PATH_FOOTER);
      subDocumentFormatter.formatContent(footer, 0);
      htmlWriter.writeEnd("div");
    }

    htmlWriter.writeEnd("div"); // SE_MC_PATH_PAGE
  }

  private void formatPathItem(Id contentId, int pathComponentOffset, boolean isCurrentPath)
  {
    htmlWriter.writeBegin("li");
    formatPathAnchor(pathComponentOffset, contentId, isCurrentPath);
    if (isCurrentPath)
    {
      formatPathList(contentId, pathComponentOffset + 1);
    }
    htmlWriter.writeEnd("li");
  }

  private void formatPathList(Id contentId, int pathComponentOffset)
  {
    Id currentId = null;
    if (pathComponentOffset < pathComponents.length) // don't bother with children of last component
    {
      currentId = getPathId(pathComponentOffset);
    }

    htmlWriter.writeBegin("ul");
    @SuppressWarnings("unchecked")
    List<Id> parts = (List<Id>)getValue(contentId, Keys.PARTS);
    if (parts != null)
    {
      for (Id part : parts)
      {
        ContentType contentType = (ContentType)getValue(part, Keys.CONTENT_TYPE);
        if (contentType == ContentType.DOCUMENT)
        {
          boolean isCurrentPath = part.equals(currentId);
          formatPathItem(part, pathComponentOffset, isCurrentPath);
        }
      }
    }
    htmlWriter.writeEnd("ul");
  }

  private void formatPathRoot()
  {
    htmlWriter.writeBegin("ul");
    Id rootId = getPathId(0);
    formatPathItem(rootId, 0, true);
    htmlWriter.writeEnd("ul");
  }

  private String getPath(int pathComponentOffset, Id part)
  {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < Math.min(pathComponents.length, pathComponentOffset); i++)
    {
      if (s.length() > 0)
      {
        s.append("/");
      }
      s.append(pathComponents[i]);
    }
    if (s.length() > 0)
    {
      s.append("/");
    }
    s.append(part.formatString());
    return s.toString();
  }

  private Id getPathId(int pathComponentOffset)
  {
    return new Id(pathComponents[pathComponentOffset]);
  }

  private String getPathUrl(int pathComponentOffset, Id part)
  {
    String pathParameter = getPath(pathComponentOffset, part);
    UrlBuilder urlBuilder = new ServerUrlBuilder(UrlConstants.URL_CONTENT);
    urlBuilder.addParameter(UrlConstants.PARAMETER_PATH, pathParameter);
    if (presentAt != null)
    {
      urlBuilder.addParameter(UrlConstants.PARAMETER_PRESENT_AT, presentAt);
    }
    String title = titles.get(part);
    urlBuilder.addTitle(title);
    return urlBuilder.toString();
  }

  @Override
  protected String getTitle(Id contentId)
  {
    int leafOffset = pathComponents.length - 1;
    String leafComponent = pathComponents[leafOffset];
    Id leafContentId = new Id(leafComponent);
    String leafTitle = titles.get(leafContentId);
    return leafTitle;
  }

  protected boolean isIncludedTitle(int level)
  {
    return level == pathComponents.length - 1;
  }

  @Override
  protected boolean isInContentPath(Id contentId, int level, ContentType contentType)
  {
    if (contentType == ContentType.STYLE)
    {
      return true;
    }

    if (contentType == ContentType.DOCUMENT)
    {
      return level < pathComponents.length && contentId.formatString().equals(pathComponents[level]);
    }

    if (pathComponents.length == level)
    {
      return true; // leaf nodes of selected content
    }

    return false;
  }

  @Override
  protected boolean isInStylePath(Id contentId, int level)
  {
    String title = (String)getValue(contentId, Keys.TITLE); // take advantage of this method as an arbitrary first-pass hook
    titles.put(contentId, title); // must collect even if outside of path for children of current content

    boolean isInStylePath = pathComponents.length > level && contentId.formatString().equals(pathComponents[level]);
    return isInStylePath;
  }

  @Override
  protected boolean isRootPathStyle(String selector)
  {
    return pathComponents.length == 1 && selector.trim().startsWith("." + HtmlConstants.SE_MC_PATH);
  }

}
