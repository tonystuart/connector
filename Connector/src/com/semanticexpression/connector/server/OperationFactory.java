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

public class OperationFactory
{
  private ServerContext serverContext;

  public OperationFactory(ServerContext serverContext)
  {
    this.serverContext = serverContext;
  }

  public AccountOperation createAccountOperation()
  {
    return new AccountOperation(serverContext);
  }

  public AdminOperation createAdminOperation()
  {
    return new AdminOperation(serverContext);
  }

  public CredentialOperation createCredentialOperation()
  {
    return new CredentialOperation(serverContext);
  }

  public HistoryOperation createHistoryOperation()
  {
    return new HistoryOperation(serverContext);
  }

  public ImageOperation createImageOperation()
  {
    return new ImageOperation(serverContext);
  }

  public LoginOperation createLoginOperation()
  {
    return new LoginOperation(serverContext);
  }

  public LogoutOperation createLogoutOperation()
  {
    return new LogoutOperation(serverContext);
  }

  public MatchingOperation createMatchingOperation()
  {
    return new MatchingOperation(serverContext);
  }

  public PersonalSecurityOperation createPersonalSecurityOperation()
  {
    return new PersonalSecurityOperation(serverContext);
  }

  public PublishOperation createPublishOperation()
  {
    return new PublishOperation(serverContext);
  }

  public RelationshipOperation createRelationshipOperation()
  {
    return new RelationshipOperation(serverContext);
  }

  public RetrieveOperation createRetrieveOperation()
  {
    return new RetrieveOperation(serverContext);
  }

  public SearchOperation createSearchOperation()
  {
    return new SearchOperation(serverContext);
  }

  public StatusOperation createStatusOperation()
  {
    return new StatusOperation(serverContext);
  }

  public TagOperation createTagOperation()
  {
    return new TagOperation(serverContext);
  }

  public UpdateOperation createUpdateOperation()
  {
    return new UpdateOperation(serverContext);
  }

  public WorkflowOperation createWorkflowOperation()
  {
    return new WorkflowOperation(serverContext);
  }
}
