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

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoSuchDirectoryException;
import org.apache.lucene.util.Version;

import com.semanticexpression.connector.server.Log;

public class LuceneIndexApplication
{
  public static final String ID = "id";

  protected Directory directory;
  protected StandardAnalyzer analyzer;

  public static byte[] getBytes(int i)
  {
    return new byte[] {
        (byte)((i >> 24) & 0xff),
        (byte)((i >> 16) & 0xff),
        (byte)((i >> 8) & 0xff),
        (byte)(i & 0xff)
    };
  }

  public static int getInt(byte[] buffer)
  {
    return ((buffer[0] & 0xff) << 24) | ((buffer[1] & 0xff) << 16) | ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);
  }

  public LuceneIndexApplication(String indexRootPath)
  {
    try
    {
      Log.debug("LuceneIndex.LuceneIndex: indexRoot=%s, currentDirectory=%s", indexRootPath, new File(".").getCanonicalPath());
      directory = new NIOFSDirectory(new File(indexRootPath));
      analyzer = new StandardAnalyzer(Version.LUCENE_30);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void clear()
  {
    try
    {
      String[] fileNames = directory.listAll();
      for (String fileName : fileNames)
      {
        directory.deleteFile(fileName);
      }
    }
    catch (NoSuchDirectoryException e)
    {
      // This is normal if the index directory hasn't been created yet. Clearly listAll should provide a method to test for this rather than just throwing an exception
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
}
