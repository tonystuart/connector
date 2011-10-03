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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.LockObtainFailedException;

import com.semanticexpression.connector.server.Log;

public class LuceneIndexWriter extends LuceneIndexApplication
{
  private IndexWriter writer;
  private long totalMillis;
  private long totalCharacters;
  private int totalDocuments;

  public LuceneIndexWriter(String indexRootPath)
  {
    super(indexRootPath);
  }

  public void open()
  {
    try
    {
      writer = new IndexWriter(directory, analyzer, new MaxFieldLength(50));
    }
    catch (CorruptIndexException e)
    {
      throw new RuntimeException(e);
    }
    catch (LockObtainFailedException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void close()
  {
    try
    {
      writer.optimize();
      writer.close();
      Log.debug("LuceneIndexWriter.close: totalCharacters=%d, totalDocuments=%d, totalMillis=%d", totalCharacters, totalDocuments, totalMillis);
    }
    catch (CorruptIndexException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void write(int id, String... nameValuePairs)
  {
    if ((nameValuePairs.length % 2) != 0)
    {
      throw new IllegalArgumentException("nameValuePairs isn't divisible by two");
    }
    
    try
    {
      long beginMillis = System.currentTimeMillis();
      byte[] idArray = getBytes(id);
      Document document = new Document();
      document.add(new Field(ID, idArray, Field.Store.YES)); // Must store id to get to value, see JavaDoc for Document
      for (int i = 0; i < nameValuePairs.length; i += 2)
      {
        String name = nameValuePairs[i];
        String value = nameValuePairs[i+1];
        document.add(new Field(name, value, Store.NO, Index.ANALYZED));
        totalCharacters += value.length();
      }
      writer.addDocument(document);
      long endMillis = System.currentTimeMillis();
      long elapsedMillis = endMillis - beginMillis;
      totalMillis += elapsedMillis;
      totalDocuments++;
    }
    catch (CorruptIndexException e)
    {
      throw new RuntimeException(e);
    }
    catch (LockObtainFailedException e)
    {
      throw new RuntimeException(e);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
