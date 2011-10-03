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

import com.semanticexpression.connector.shared.Id;

public class RepositoryCreator
{
  private InitializedObjectCreator[] initializedObjectCreators = new InitializedObjectCreator[] {
      new InitializedObjectCreator(new EntityTableCreator(), new EntityTableInitializer()),
      new InitializedObjectCreator(new PropertyTableCreator()),
      new InitializedObjectCreator(new AuthorityTableCreator())
  };

  public DatabaseTypeCreator getEntityTypeCreator()
  {
    DatabaseTypeCreator databaseTypeCreator = new DatabaseTypeCreator("entity", Id.class.getCanonicalName());
    return databaseTypeCreator;
  }

  public void initialize(Connection connection, Repository repository)
  {
    for (InitializedObjectCreator initializedObjectCreator : initializedObjectCreators)
    {
      boolean isNew = initializedObjectCreator.getDatabaseObjectCreator().create(connection);
      if (isNew)
      {
        DatabaseObjectInitializer databaseObjectInitializer = initializedObjectCreator.getDatabaseObjectInitializer();
        if (databaseObjectInitializer != null)
        {
          databaseObjectInitializer.initialize(connection, repository);
        }
      }
    }
  }

  public interface DatabaseObjectInitializer
  {
    public void initialize(Connection connection, Repository repository);
  }

}
