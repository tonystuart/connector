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

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.semanticexpression.connector.shared.TagConstants.TagType;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.enums.ChartType;
import com.semanticexpression.connector.shared.enums.ContentType;

public class SerializableClasses implements Serializable
{
  public BaseModel baseModel;
  public ContentType contentType;
  public Id identity;
  public TagVisibility tagVisibility;
  public ChartType chartType;
  public RelationshipType relationshipType;
  public byte[] byteArray; // required for Importer
  public TagType tagType; // required for Importer
  
  public SerializableClasses()
  {
    
  }
}
