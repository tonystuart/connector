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

package com.semanticexpression.connector.lucenederby;

public class LuceneDerbySearchResult
{
  public static final int EOF_MARKER = -1;
  
  private int id;
  private double score;

  public LuceneDerbySearchResult(int id, double score)
  {
    this.id = id;
    this.score = score;
  }

  public int getId()
  {
    return id;
  }

  public double getScore()
  {
    return score;
  }
}
