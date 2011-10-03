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
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;

public class ImageOperation extends BaseOperation
{

  public ImageOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  public void uploadImage(String authenticationToken, HttpServletRequest request) throws FileUploadException, IOException, AuthenticationException, AuthorizationException
  {
    int contentLength = request.getContentLength();
    int maximumUploadSize = serverProperties.getMaximumUploadSize();
    if (contentLength > maximumUploadSize)
    {
      throw new IllegalArgumentException("File is too large");
    }

    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    if (!isMultipart)
    {
      throw new IllegalArgumentException("Expecting multipart content");
    }

    Id contentId = null;
    ServletFileUpload servletFileUpload = new ServletFileUpload();
    FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);
    while (fileItemIterator.hasNext())
    {
      FileItemStream fileItemStream = fileItemIterator.next();
      String fieldName = fileItemStream.getFieldName();
      InputStream inputStream = fileItemStream.openStream();
      try
      {
        if (fieldName.equals(Keys.CONTENT_ID))
        {
          String contentIdString = Utility.getString(inputStream);
          contentId = new Id(contentIdString);
        }
        else if (fieldName.equals(Keys.FILE_NAME))
        {
          String mimeType = fileItemStream.getContentType();
          if (!mimeType.startsWith("image"))
          {
            throw new IllegalArgumentException("Expecting image");
          }
          String fileName = fileItemStream.getName();
          uploadImage(authenticationToken, contentId, fileName, mimeType, inputStream);
        }
        else
        {
          throw new IllegalArgumentException("Unexpected field " + fieldName);
        }
      }
      finally
      {
        inputStream.close();
      }
    }
  }

  public Id uploadImage(String authenticationToken, Id contentId, String newFileName, String mimeType, InputStream inputStream) throws AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Date validFrom = new Date();

      Id userId = validateWriteAuthentication(connection, authenticationToken);
      validateWriteAccess(connection, userId, contentId);

      String title = (String)retrieveProperty(connection, contentId, Keys.TITLE);
      String oldFileName = (String)retrieveProperty(connection, contentId, Keys.FILE_NAME, validFrom);

      boolean isNew = repository.realiseEntity(connection, contentId, EntityType.CONTENT, null, null, userId, validFrom);
      if (isNew || title == null || title.equals(oldFileName))
      {
        repository.updateProperty(connection, contentId, Keys.TITLE, newFileName, validFrom, userId);
      }

      repository.updateProperty(connection, contentId, Keys.FILE_NAME, newFileName, validFrom, userId);
      repository.updateProperty(connection, contentId, Keys.CONTENT_TYPE, ContentType.IMAGE, validFrom, userId);
      repository.updateProperty(connection, contentId, Keys.MIME_TYPE, mimeType, validFrom, userId);
      repository.updateProperty(connection, contentId, Keys.IMAGE, inputStream, validFrom, userId);

      updateImageDimensions(connection, contentId, validFrom, userId);

      List<Status> statusList = new LinkedList<Status>();
      String updateStatusAction = getUpdateStatusAction(isNew);
      updateStatusList(connection, userId, contentId, validFrom, updateStatusAction, statusList);
      postStatus(connection, statusList, Repository.ALL_QUEUES);

      return contentId;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

}
