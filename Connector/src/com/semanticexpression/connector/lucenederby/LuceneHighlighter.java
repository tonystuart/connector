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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.util.Version;

public class LuceneHighlighter extends LuceneIndexApplication
{
  private Highlighter highlighter;
  private String defaultFieldName;

  public LuceneHighlighter(String indexRootPath, String defaultFieldName, String searchTerms)
  {
    super(indexRootPath);

    try
    {
      this.defaultFieldName = defaultFieldName;
      IndexReader reader = IndexReader.open(directory, true);
      QueryParser queryParser = new QueryParser(Version.LUCENE_30, defaultFieldName, analyzer);
      Query luceneQuery = queryParser.parse(searchTerms);

      QueryScorer scorer = new QueryScorer(luceneQuery, reader, defaultFieldName);
      Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
      highlighter = new Highlighter(scorer);
      highlighter.setTextFragmenter(fragmenter);
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

  public String[] getBestFragments(String text, int maximumFragments)
  {
    try
    {
      String[] fragments = highlighter.getBestFragments(analyzer, defaultFieldName, text, maximumFragments);
      return fragments;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    catch (InvalidTokenOffsetsException e)
    {
      throw new RuntimeException(e);
    }
  }

  public String getBestFragment(String text)
  {
    String highlight;
    String[] fragments = getBestFragments(text, 1);
    if (fragments == null)
    {
      highlight = null;
    }
    else
    {
      highlight = fragments[0];
    }
    return highlight;
  }

  public String getHighlight(String text)
  {
    StringBuilder s = new StringBuilder();
    String[] fragments = getBestFragments(text, 5);
    if (fragments != null)
    {
      for (String fragment : fragments)
      {
        if (s.length() > 0)
        {
          s.append("<br/>");
        }
        s.append(fragment);
      }
    }
    return s.toString();
  }

}
