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

package com.semanticexpression.connector.client.frame.editor.image;

import com.semanticexpression.connector.client.ClientUrlBuilder;
import com.semanticexpression.connector.client.frame.editor.FileUploadWindow;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.UrlBuilder;
import com.semanticexpression.connector.shared.UrlConstants;

public final class ImageFramelet extends BaseImageFramelet
{
  private FileUploadWindow fileUploadWindow;

  public ImageFramelet()
  {
    super("Image", Resources.IMAGE);
    setEmptyText("Save and refresh to view image");
  }

  @Override
  protected ImageMenu createImageMenu()
  {
    return new ImageMenu(this);
  }

  @Override
  protected ImagePropertyEditorWindow createPropertyEditorWindow()
  {
    return new ImagePropertyEditorWindow();
  }

  public FileUploadWindow getFileUploadWindow()
  {
    if (fileUploadWindow == null)
    {
      fileUploadWindow = new FileUploadWindow();
    }
    return fileUploadWindow;
  }

  @Override
  protected Integer getImageHeight()
  {
    return contentReference.get(Keys.IMAGE_NATIVE_HEIGHT);
  }

  @Override
  protected String getImageUrlString()
  {
    UrlBuilder urlBuilder = new ClientUrlBuilder(UrlConstants.URL_CONTENT);
    urlBuilder.addParameter(UrlConstants.PARAMETER_ID, contentReference.getId());
    urlBuilder.addParameter(UrlConstants.PARAMETER_PRESENT_AT, contentReference.getHistoryDate());
    return urlBuilder.toString();
  }

  @Override
  protected Integer getImageWidth()
  {
    return contentReference.get(Keys.IMAGE_NATIVE_WIDTH);
  }

  public void replaceImage()
  {
    getFileUploadWindow().display(this, contentReference.getId(), null); // updated via StatusMonitor and UpdateStatus message
  }

}