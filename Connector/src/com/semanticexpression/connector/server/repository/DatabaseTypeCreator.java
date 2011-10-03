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

public class DatabaseTypeCreator extends DatabaseObjectCreator
{
  public DatabaseTypeCreator(String typeName, String className)
  {
    super(typeName);
    append("create type " + getObjectName());
    append("external name '" + className + "'");
    append("language java");
  }

  protected String getObjectExistenceTestSql()
  {
    StringBuilder s = new StringBuilder();
    s.append("select 1\n");
    s.append("from sys.sysaliases as a, sys.sysschemas as s\n");
    s.append("where a.schemaid = s.schemaid\n");
    s.append("and s.schemaname = current schema\n");
    s.append("and a.aliastype = 'A'\n");
    s.append("and a.alias = ?");
    return s.toString();
  }

}