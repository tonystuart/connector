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

import java.sql.ResultSet;
import java.util.concurrent.LinkedBlockingQueue;

public class LuceneDerbyFunctions
{
  // TODO: Make this work once we have it working for a single thread (connection)
  // private static ThreadLocal<LuceneDerbyThreadLocal> luceneDerbyThreadLocal = new ThreadLocal<LuceneDerbyThreadLocal>();

  public static ResultSet search(String indexRootPath, String defaultFieldName, String searchTerms)
  {
    LinkedBlockingQueue<LuceneDerbySearchResult> linkedBlockingQueue = new LinkedBlockingQueue<LuceneDerbySearchResult>();
    LuceneDerbyResultSet luceneDerbyResultSet = new LuceneDerbyResultSet(linkedBlockingQueue);
    LuceneDerbyCollector luceneDerbyCollector = new LuceneDerbyCollector(linkedBlockingQueue);
    final LuceneIndexReader luceneIndexReader = new LuceneIndexReader(indexRootPath, defaultFieldName, searchTerms, luceneDerbyCollector);
    Thread thread = new Thread()
    {
      @Override
      public void run()
      {
        luceneIndexReader.search();
      }
    };
    thread.run();
    return luceneDerbyResultSet;
  }

}
