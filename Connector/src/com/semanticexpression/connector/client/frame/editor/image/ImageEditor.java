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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.semanticexpression.connector.client.frame.editor.CaptionBaseEditor;
import com.semanticexpression.connector.client.frame.editor.ContentReference;

public final class ImageEditor extends CaptionBaseEditor
{
  private ImageFramelet imageFramelet;

  public ImageEditor()
  {
    add(getImageFramelet(), new BorderLayoutData(LayoutRegion.CENTER));
  }

  @Override
  public void display(ContentReference contentReference)
  {
    super.display(contentReference);
    getImageFramelet().display(contentReference);
  }

  public ImageFramelet getImageFramelet()
  {
    if (imageFramelet == null)
    {
      imageFramelet = new ImageFramelet();
    }
    return imageFramelet;
  }

}
