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
import java.util.Random;

import com.semanticexpression.connector.server.repository.Repository;

public class ServerContext
{
  private Mailer mailer;
  private Random random;
  private Repository repository;
  private SearchEngine searchEngine;
  private ServerProperties serverProperties;
  private StatusQueues statusQueues;
  private File temporaryDirectory;

  public ServerContext(ServerProperties serverProperties, Repository repository, SearchEngine searchEngine, Mailer mailer, StatusQueues statusQueues, File temporaryDirectory, Random random)
  {
    this.repository = repository;
    this.serverProperties = serverProperties;
    this.searchEngine = searchEngine;
    this.mailer = mailer;
    this.statusQueues = statusQueues;
    this.temporaryDirectory = temporaryDirectory;
    this.random = random;
  }

  public Mailer getMailer()
  {
    return mailer;
  }

  public Random getRandom()
  {
    return random;
  }

  public Repository getRepository()
  {
    return repository;
  }

  public SearchEngine getSearchEngine()
  {
    return searchEngine;
  }

  public ServerProperties getServerProperties()
  {
    return serverProperties;
  }

  public StatusQueues getStatusManager()
  {
    return statusQueues;
  }

  public File getTemporaryDirectory()
  {
    return temporaryDirectory;
  }

}
