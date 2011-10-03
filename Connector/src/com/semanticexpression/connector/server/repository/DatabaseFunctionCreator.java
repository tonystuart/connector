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

public class DatabaseFunctionCreator extends DatabaseObjectCreator
{
  /**
   * @param parameterSpec e.g. entityTypeValue EntityType
   * @param returnSpec e.g. returns integer
   */
  public DatabaseFunctionCreator(String functionName, String methodName, String parameterSpec, String returnSpec)
  {
    super(functionName);
    append("create function " + getObjectName() + "");
    append("(");
    append(parameterSpec);
    append(")");
    append(returnSpec);
    append("language java");
    append("deterministic");
    append("external name '" + methodName + "'");
    append("parameter style java");
    append("external security invoker");
    append("no sql");
    append("returns null on null input");
  }

  protected String getObjectExistenceTestSql()
  {
    StringBuilder s = new StringBuilder();
    s.append("select 1\n");
    s.append("from sys.sysaliases as a, sys.sysschemas as s\n");
    s.append("where a.schemaid = s.schemaid\n");
    s.append("and s.schemaname = current schema\n");
    s.append("and a.aliastype = 'F'\n");
    s.append("and a.alias = ?");
    return s.toString();
  }

}