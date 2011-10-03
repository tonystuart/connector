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

package com.semanticexpression.connector.client.frame.editor.table;

/**
 * Implementation Notes:
 * <ul>
 * <li>We define setObjectValue and getObjectValue instead of setValue and
 * getValue to avoid the the following type of compile error:<br/><br/>
 * <code>
 * Name clash: The method setValue(Object) of type IntegerConstrainedField has
 * the same erasure as setValue(D) of type TextField<D> but does not override it
 * </code></li>
 * </ul>
 */
public interface ConstrainedField
{
  public Object normalizeValue(Object value);

  public Object getObjectValue();

  public void setObjectValue(Object value);
}
