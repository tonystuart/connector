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

package com.semanticexpression.connector.server.repository;

import java.sql.Connection;
import java.util.Date;

import com.semanticexpression.connector.server.repository.RepositoryCreator.DatabaseObjectInitializer;

public class EntityTableInitializer implements DatabaseObjectInitializer
{

  @Override
  public void initialize(Connection connection, Repository repository)
  {
    boolean isNew = repository.realiseEntity(connection, Repository.SYSTEM_USER_ID, EntityType.USER, Repository.SYSTEM_USER_NAME, null, null, new Date());
    if (!isNew)
    {
      throw new IllegalStateException("SYSTEM_USER_ID already exists");
    }
  }

}
