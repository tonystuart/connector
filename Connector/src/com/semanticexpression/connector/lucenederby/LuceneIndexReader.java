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

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.semanticexpression.connector.server.Log;

public class LuceneIndexReader extends LuceneIndexApplication
{
  private String searchTerms;
  private LuceneDerbyCollector collector;
  private String defaultFieldName;

  public LuceneIndexReader(String indexRootPath, String defaultFieldName, String searchTerms, LuceneDerbyCollector collector)
  {
    super(indexRootPath);
    this.defaultFieldName = defaultFieldName;
    this.searchTerms = searchTerms;
    this.collector = collector;
  }

  // TODO: Factor out RuntimeException handling
  
  public void search()
  {
    try
    {
      IndexReader reader = IndexReader.open(directory, true);
      try
      {
        long beginMillis = System.currentTimeMillis();
        IndexSearcher searcher = new IndexSearcher(reader);
        try
        {
          collector.setSearcher(searcher);
          QueryParser queryParser = new QueryParser(Version.LUCENE_30, defaultFieldName, analyzer);
          Query query = queryParser.parse(searchTerms);
          searcher.search(query, collector);
          collector.close();
        }
        finally
        {
          long endMillis = System.currentTimeMillis();
          long elapsedMillis = endMillis - beginMillis;
          searcher.close();
          Log.debug("LuceneIndexReader.search: totalHits=%d, elapsedMillis=%d", collector.getTotalHits(), elapsedMillis);
        }
      }
      finally
      {
        reader.close();
      }
    }
    catch (CorruptIndexException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }
}
