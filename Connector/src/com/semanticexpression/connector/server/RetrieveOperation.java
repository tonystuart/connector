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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.server.repository.Repository.PropertyBlob;
import com.semanticexpression.connector.shared.ChartSpecificationFormatter;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.TextCompare;
import com.semanticexpression.connector.shared.UrlConstants;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;

public class RetrieveOperation extends BaseOperation
{
  private static final String BODY = "<" + HtmlConstants.SE_STRUCTURE_BODY + ">";

  public RetrieveOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  protected void formatContentHistory(Connection connection, HttpServletResponse response, Id userId, Id contentId, Date leftDate, Date rightDate) throws AuthorizationException, ServletException
  {
    try
    {
      String leftText = retrieveHtml(userId, contentId, leftDate);
      String rightText = retrieveHtml(userId, contentId, rightDate);

      int leftBodyIndex = leftText.indexOf(BODY);
      int rightBodyIndex = rightText.indexOf(BODY);

      String leftBody = leftText.substring(leftBodyIndex);
      String rightBody = rightText.substring(rightBodyIndex);
      String rightHead = rightText.substring(0, rightBodyIndex);

      TextCompare textCompare = new TextCompare(0);
      String result = textCompare.compare(leftBody, rightBody);
      PrintWriter writer = response.getWriter();
      writer.write(rightHead);
      writer.write(result);
      writer.flush();
      // servlet container closes writer
    }
    catch (IOException e)
    {
      throw new ServletException(e);
    }
  }

  void initializeParts(Connection connection, Id userId, Id contentId, List<Content> contentList, Content content, Date presentAt, boolean isDeep, LinkedList<Id> cycleDetector, Set<Id> duplicateDetector) throws AuthorizationException
  {
    @SuppressWarnings("unchecked")
    List<Id> parts = (List<Id>)initializeProperty(connection, contentId, Keys.PARTS, content, presentAt);
    for (Id part : parts)
    {
      int topOfStack = cycleDetector.size();
      retrieveContent(connection, userId, part, contentList, presentAt, isDeep, cycleDetector, duplicateDetector);
      while (cycleDetector.size() > topOfStack)
      {
        cycleDetector.pop();
      }
    }
  }

  Object initializeProperty(Connection connection, Id contentId, String propertyName, Content content, Date presentAt)
  {
    Object propertyValue = retrieveProperty(connection, contentId, propertyName, presentAt);
    content.set(propertyName, propertyValue);
    return propertyValue;
  }

  private PropertyBlob performLazyBlobCopyOnRequest(Connection connection, Id userId, Id contentId, Date presentAt)
  {
    PropertyBlob propertyBlob = null;
    Id derivedFrom = (Id)retrieveProperty(connection, contentId, Keys.DERIVED_FROM, presentAt);
    if (derivedFrom != null)
    {
      Date validFrom = repository.retrieveEntityModifiedAtDate(connection, contentId);
      PropertyBlob derivedFromBlob = repository.retrievePropertyBlob(connection, derivedFrom, Keys.IMAGE, presentAt);
      if (derivedFromBlob != null)
      {
        try
        {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          InputStream inputStream = derivedFromBlob.getBinaryStream();
          try
          {
            Utility.copyFile(inputStream, outputStream);
          }
          finally
          {
            inputStream.close();
          }
          inputStream = new ByteArrayInputStream(outputStream.toByteArray());
          repository.updateProperty(connection, contentId, Keys.IMAGE, inputStream, validFrom, userId);
          propertyBlob = repository.retrievePropertyBlob(connection, contentId, Keys.IMAGE, presentAt);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    }
    return propertyBlob;
  }

  public List<Content> retrieveContent(Connection connection, Id userId, Id contentId, Date presentAt, boolean isDeep) throws AuthorizationException
  {
    List<Content> contentList = new LinkedList<Content>();
    LinkedList<Id> cycleDetector = new LinkedList<Id>();
    HashSet<Id> duplicateDetector = new HashSet<Id>();
    retrieveContent(connection, userId, contentId, contentList, presentAt, isDeep, cycleDetector, duplicateDetector);
    return contentList;
  }

  public void retrieveContent(Connection connection, Id userId, Id contentId, List<Content> contentList, Date presentAt, boolean isDeep, LinkedList<Id> cycleDetector, Set<Id> duplicateDetector) throws AuthorizationException
  {
    validateReadAccess(connection, userId, contentId);

    if (cycleDetector.contains(contentId))
    {
      throw new IllegalStateException("Detected cyclic content, cycleDetector=" + cycleDetector + ", contentId=" + contentId);
    }

    cycleDetector.push(contentId);

    Content content = new Content(contentId);
    contentList.add(content);

    if (duplicateDetector.contains(contentId))
    {
      return; // only first occurrence contains properties
    }

    duplicateDetector.add(contentId);

    Set<String> propertyNames = repository.retrievePropertyNames(connection, contentId, presentAt);

    for (String propertyName : propertyNames)
    {
      if (isDeep && propertyName.equals(Keys.PARTS))
      {
        initializeParts(connection, userId, contentId, contentList, content, presentAt, isDeep, cycleDetector, duplicateDetector);
      }
      else if (propertyName.equals(Keys.IMAGE))
      {
        // No further action necessary, content requested separately based on id
      }
      else
      {
        initializeProperty(connection, contentId, propertyName, content, presentAt);
      }
    }

    if (presentAt != null || !canWrite(connection, userId, contentId))
    {
      content.setReadOnly(true);
    }
  }

  public List<Content> retrieveContent(Id contentId, Date presentAt, boolean isDeep) throws AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      List<Content> content = retrieveContent(connection, Repository.SYSTEM_USER_ID, contentId, presentAt, isDeep);
      return content;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  public List<Content> retrieveContent(String authenticationToken, Id contentId, Date presentAt, boolean isDeep) throws AuthenticationException, InvalidContentIdException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateReadAuthentication(connection, authenticationToken);

      if (!repository.isExistingEntity(connection, contentId, EntityType.CONTENT))
      {
        throw new InvalidContentIdException(contentId);
      }

      List<Content> content = retrieveContent(connection, userId, contentId, presentAt, isDeep);

      return content;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  public String retrieveContentString(String authenticationToken, HttpServletRequest request, Parameters parameters) throws ServletException, AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id contentId = parameters.getId(UrlConstants.PARAMETER_ID);
      Date presentAt = parameters.getDate(UrlConstants.PARAMETER_PRESENT_AT, null);
      Id userId = validateReadAuthentication(connection, authenticationToken);
      String formattedContent = retrieveHtml(userId, contentId, presentAt);
      return formattedContent;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  private String retrieveHtml(Id userId, Id contentId, Date presentAt) throws AuthorizationException
  {
    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
    PrintWriter printWriter = new PrintWriter(byteOutputStream);
    HtmlWriter htmlWriter = new HtmlWriter(printWriter);
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      HtmlFormatter htmlFormatter = new HtmlFormatter(this, connection, userId, presentAt, htmlWriter);
      htmlFormatter.formatContent(contentId);
      printWriter.flush();
      return byteOutputStream.toString();
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  private void retrieveHttpChart(Connection connection, HttpServletRequest request, HttpServletResponse response, Parameters parameters, Id userId, Id chartContentId, Date presentAt) throws ServletException, AuthorizationException
  {
    try
    {
      List<Content> chartContentList = retrieveContent(connection, userId, chartContentId, presentAt, false);
      Content chartContent = chartContentList.get(0);

      Id tableContentId = parameters.getId(UrlConstants.PARAMETER_TABLE);
      List<Content> tableContentList = retrieveContent(connection, userId, tableContentId, presentAt, false);
      Content tableContent = tableContentList.get(0);

      ChartSpecificationFormatter chartSpecificationFormatter = new ChartSpecificationFormatter(chartContent, tableContent, "&");
      String chartSpecification = chartSpecificationFormatter.getChartSpecification();

      String contextPath = request.getContextPath();
      String requestUri = UrlConstants.URL_CHART;
      HttpServletRequestWrapper chartRequest = new ChartRequest(request, contextPath, requestUri, chartSpecification);
      RequestDispatcher dispatcher = request.getRequestDispatcher(requestUri);
      dispatcher.forward(chartRequest, response);
    }
    catch (ServletException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void retrieveHttpContent(String authenticationToken, HttpServletRequest request, HttpServletResponse response, Parameters parameters) throws ServletException, AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Date presentAt = parameters.getDate(UrlConstants.PARAMETER_PRESENT_AT, null);
      Id userId = validateReadAuthentication(connection, authenticationToken);
      String path = parameters.getString(UrlConstants.PARAMETER_PATH, null);
      if (path != null)
      {
        retrieveHttpPathHtml(connection, request, response, parameters, userId, path, presentAt);
      }
      else
      {
        Id contentId = parameters.getId(UrlConstants.PARAMETER_ID);
        ContentType contentType = (ContentType)retrieveProperty(connection, contentId, Keys.CONTENT_TYPE);
        if (contentType != null)
        {
          switch (contentType)
          {
            case CHART:
              retrieveHttpChart(connection, request, response, parameters, userId, contentId, presentAt);
              break;
            case IMAGE:
              retrieveHttpImage(connection, response, userId, contentId, presentAt);
              break;
            default:
              retrieveHttpHtml(connection, request, response, parameters, userId, contentId, presentAt);
              break;
          }
        }
      }
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  protected void retrieveHttpHtml(Connection connection, HttpServletRequest request, HttpServletResponse response, Parameters parameters, Id userId, Id contentId, Date presentAt) throws AuthorizationException, ServletException
  {
    Date compareDate = parameters.getDate(UrlConstants.PARAMETER_COMPARE_AT, null);
    if (compareDate == null)
    {
      HtmlFormatter htmlFormatter = new HtmlFormatter(this, connection, userId, presentAt, response);
      htmlFormatter.formatContent(contentId);
    }
    else
    {
      formatContentHistory(connection, response, userId, contentId, presentAt, compareDate);
    }
  }

  private void retrieveHttpImage(Connection connection, HttpServletResponse response, Id userId, Id contentId, Date presentAt) throws AuthorizationException
  {
    try
    {
      validateReadAccess(connection, userId, contentId);
      String mimeType = (String)retrieveProperty(connection, contentId, Keys.MIME_TYPE);
      response.setContentType(mimeType);

      PropertyBlob propertyBlob = repository.retrievePropertyBlob(connection, contentId, Keys.IMAGE, presentAt);
      if (propertyBlob == null)
      {
        propertyBlob = performLazyBlobCopyOnRequest(connection, userId, contentId, presentAt);
      }

      try
      {
        int length = propertyBlob.getLength();
        response.setContentLength(length);

        InputStream inputStream = propertyBlob.getBinaryStream();
        try
        {
          ServletOutputStream outputStream = response.getOutputStream();

          int rc;
          byte[] buffer = new byte[32768];

          while ((rc = inputStream.read(buffer)) != -1)
          {
            outputStream.write(buffer, 0, rc);
          }

        }
        finally
        {
          inputStream.close();
        }
      }
      finally
      {
        propertyBlob.close();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  private void retrieveHttpPathHtml(Connection connection, HttpServletRequest request, HttpServletResponse response, Parameters parameters, Id userId, String path, Date presentAt) throws AuthorizationException
  {
    PathHtmlFormatter pathHtmlFormatter = new PathHtmlFormatter(this, connection, userId, presentAt, response);
    pathHtmlFormatter.formatPath(path);
  }

  private final class ChartRequest extends HttpServletRequestWrapper
  {
    private final String chartSpecification;
    private final String contextPath;
    private String requestUri;

    private ChartRequest(HttpServletRequest request, String contextPath, String requestUri, String chartSpecification)
    {
      super(request);
      this.contextPath = contextPath;
      this.requestUri = requestUri;
      this.chartSpecification = chartSpecification;
    }

    @Override
    public String getContextPath()
    {
      return contextPath;
    }

    @Override
    public String getQueryString()
    {
      return chartSpecification;
    }

    @Override
    public String getRequestURI()
    {
      return requestUri;
    }
  }

}
