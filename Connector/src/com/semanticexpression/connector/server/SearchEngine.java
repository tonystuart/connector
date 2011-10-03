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

package com.semanticexpression.connector.server;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.semanticexpression.connector.server.SearchOperation.SearchCollector;

public class SearchEngine
{
  protected StandardAnalyzer analyzer;
  protected Directory directory;
  private IndexReader indexReader;
  private IndexWriter indexWriter;

  public SearchEngine(String indexRootPath)
  {
    try
    {
      File file = new File(indexRootPath);
      Log.info("SearchEngine: directory=%s", file.getCanonicalPath());
      directory = new NIOFSDirectory(file);
      analyzer = new StandardAnalyzer(Version.LUCENE_30);
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
      indexReader.close();
      indexWriter.optimize();
      indexWriter.close();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void commit()
  {
    try
    {
      indexWriter.commit();
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

  public Analyzer getAnalyzer()
  {
    return analyzer;
  }

  public synchronized IndexReader getIndexReader()
  {
    try
    {
      IndexReader newReader = indexReader.reopen();
      if (newReader != indexReader)
      {
        indexReader.close();
      }
      indexReader = newReader;
      return indexReader;
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

  public void open()
  {
    try
    {
      indexWriter = new IndexWriter(directory, analyzer, MaxFieldLength.UNLIMITED);
      indexReader = IndexReader.open(directory, true);
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

  public void search(SearchCollector collector, String searchTerms, String[] searchFields)
  {
    try
    {
      IndexSearcher searcher = new IndexSearcher(getIndexReader());
      try
      {
        collector.setSearcher(searcher);
        QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_30, searchFields, analyzer);
        queryParser.setDefaultOperator(Operator.AND);
        Query query = queryParser.parse(searchTerms);
        searcher.search(query, collector);
      }
      finally
      {
        searcher.close();
      }
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

  public void update(String documentFieldName, String documentFieldValue, Iterable<Entry<String, String>> properties)
  {
    try
    {
      Document document = new Document();
      Field documentField = new Field(documentFieldName, documentFieldValue, Store.YES, Index.ANALYZED);
      document.add(documentField);

      for (Entry<String, String> property : properties)
      {
        String name = property.getKey();
        String value = property.getValue();
        Log.debug("SearchEngine.update: id=%s, name=%s, value=%s", documentFieldValue, name, value.substring(0, Math.min(30, value.length())));
        Field propertyField = new Field(name, value, Store.NO, Index.ANALYZED);
        document.add(propertyField);
      }

      Term term = new Term(documentFieldName, documentFieldValue);
      indexWriter.updateDocument(term, document);
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
