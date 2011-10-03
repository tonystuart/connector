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
import java.util.concurrent.BlockingQueue;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;

public class LuceneDerbyCollector extends Collector
{
  private BlockingQueue<LuceneDerbySearchResult> blockingQueue;
  private Scorer scorer;
  protected Searcher searcher;
  private int totalHits;

  public LuceneDerbyCollector(BlockingQueue<LuceneDerbySearchResult> blockingQueue)
  {
    this.blockingQueue = blockingQueue;
  }

  @Override
  public boolean acceptsDocsOutOfOrder()
  {
    return true;
  }

  public void close()
  {
    try
    {
      blockingQueue.put(new LuceneDerbySearchResult(LuceneDerbySearchResult.EOF_MARKER, 0));
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void collect(int documentNumber) throws IOException
  {
    try
    {
      Document document = searcher.doc(documentNumber);
      byte[] idBytes = document.getBinaryValue(LuceneIndexApplication.ID);
      int id = LuceneIndexApplication.getInt(idBytes);
      float score = scorer.score();
      LuceneDerbySearchResult luceneDerbySearchResult = new LuceneDerbySearchResult(id, score);
      blockingQueue.put(luceneDerbySearchResult);
      totalHits++;
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException(e);
    }
  }

  public Searcher getSearcher()
  {
    return searcher;
  }

  public int getTotalHits()
  {
    return totalHits;
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException
  {
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException
  {
    this.scorer = scorer;
  }

  public void setSearcher(Searcher searcher)
  {
    this.searcher = searcher;
  }

}
