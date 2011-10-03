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

public class DatabaseTableCreator extends DatabaseObjectCreator
{
  public DatabaseTableCreator(String tableName)
  {
    super(tableName);
  }

  public void appendPrefix()
  {
    append("create table " + getObjectName());
    append("(");
  }

  public void appendSuffix()
  {
    append(")");
  }

  protected String getObjectExistenceTestSql()
  {
    StringBuilder s = new StringBuilder();
    s.append("select 1\n");
    s.append("from sys.systables as t, sys.sysschemas as s\n");
    s.append("where t.schemaid = s.schemaid\n");
    s.append("and s.schemaname = current schema\n");
    s.append("and t.tablename = ?");
    return s.toString();
  }

}