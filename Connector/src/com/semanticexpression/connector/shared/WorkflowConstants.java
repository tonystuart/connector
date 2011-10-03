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


public class WorkflowConstants
{
  public static final int ACCESS_1_READ_ONLY = 1;
  public static final int ACCESS_2_READ_WRITE = 2;

  public static final int ORDER_1_SEQUENTIAL = 1;
  public static final int ORDER_2_PARALLEL = 2;

  public static final int STATUS_1_PENDING = 1;
  public static final int STATUS_2_READY = 2;
  public static final int STATUS_3_IN_PROGRESS = 3;
  public static final int STATUS_4_COMPLETED = 4;
  public static final int STATUS_5_REJECTED = 5;

  public static final int TYPE_1_USER = 1;
  public static final int TYPE_2_GROUP = 2;
}
