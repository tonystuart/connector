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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.server.repository.EntityType;
import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.DefaultProperties;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Properties;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.WorkflowConstants;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;

public class WorkflowOperation extends BaseOperation
{
  private static final String NOT_AVAILABLE = "n/a";

  protected WorkflowOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  private int addAuthorities(Connection connection, Id workflowId, Id contentId, Id grantedToId, int grantedToType, int access)
  {
    StringBuilder s = new StringBuilder();
    s.append("insert into authority\n");
    s.append("(\n");
    s.append("  workflow_id,\n");
    s.append("  content_id,\n");
    s.append("  granted_to_id,\n");
    s.append("  granted_to_type,\n");
    s.append("  access_type\n");
    s.append(")\n");
    s.append("values(?, ?, ?, ?, ?)");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, workflowId.getId(), contentId.getId(), grantedToId.getId(), grantedToType, access);
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private State checkPending(Connection connection, Id contentId, Properties properties, Association task, State state, int order)
  {
    if (state == State.READY)
    {
      processPending(connection, contentId, properties, task);
      state = State.SCHEDULING;
    }
    else if (state == State.SCHEDULING && order == WorkflowConstants.ORDER_2_PARALLEL)
    {
      processPending(connection, contentId, properties, task);
    }
    else
    {
      state = State.NOT_READY;
    }
    return state;
  }

  private String createContentUrl(Id contentId)
  {
    String contentUrl = serverProperties.getApplicationBaseUrl() + contentId;
    return contentUrl;
  }

  private String formatDateComponent(Object object)
  {
    String value = NOT_AVAILABLE;
    if (object != null)
    {
      value = new SimpleDateFormat("yyyy-MM-dd").format(object);
    }
    return value;
  }

  private String formatDocumentList(Connection connection, List<Id> controlledDocumentIds, List<String> emailAddresses)
  {
    Set<Id> ccIds = new HashSet<Id>();
    StringBuilder s = new StringBuilder();

    for (Id controlledDocumentId : controlledDocumentIds)
    {
      if (emailAddresses != null)
      {
        Id ownerId = repository.retrieveEntityCreatedById(connection, controlledDocumentId);
        ccIds.add(ownerId);
      }

      if (s.length() > 0)
      {
        s.append("\n");
      }

      String contentUrl = createContentUrl(controlledDocumentId);
      s.append(contentUrl);
    }

    for (Id ccId : ccIds)
    {
      String emailAddress = (String)repository.retrieveProperty(connection, ccId, Keys.EMAIL_ADDRESS);
      emailAddresses.add(emailAddress);
    }

    return s.toString();
  }

  private void getControlledContent(Connection connection, Id documentId, Set<Id> controlledContentIds)
  {
    StringBuilder s = new StringBuilder();
    s.append("select entity_value\n");
    s.append("from property\n");
    s.append("where property_name = ?\n"); // Keys.PARTS
    s.append("and entity_id = ?\n");
    s.append("and valid_to is null\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, Keys.PARTS, documentId.getId());
      while (Jdbc.next(resultSet))
      {
        Id contentId = Jdbc.getId(resultSet, 1);
        if (!controlledContentIds.contains(contentId))
        {
          controlledContentIds.add(contentId);
          getControlledContent(connection, contentId, controlledContentIds);
        }
      }
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private void getEmailAddresses(Connection connection, String name, int type, List<String> toEmailAddresses)
  {
    if (type == WorkflowConstants.TYPE_1_USER)
    {
      Id userId = repository.retrieveEntityId(connection, name, EntityType.USER);
      if (userId != null)
      {
        String emailAddress = (String)repository.retrieveProperty(connection, userId, Keys.EMAIL_ADDRESS);
        if (emailAddress != null)
        {
          toEmailAddresses.add(emailAddress);
        }
      }
    }
    else if (type == WorkflowConstants.TYPE_2_GROUP)
    {
      Id groupId = repository.retrieveEntityId(connection, name, EntityType.GROUP);
      if (groupId != null)
      {
        @SuppressWarnings("unchecked")
        Sequence<Id> members = (Sequence<Id>)repository.retrieveProperty(connection, groupId, Keys.GROUP);
        if (members != null)
        {
          for (Id userId : members)
          {
            String emailAddress = (String)repository.retrieveProperty(connection, userId, Keys.EMAIL_ADDRESS);
            if (emailAddress != null)
            {
              toEmailAddresses.add(emailAddress);
            }
          }
        }
      }
    }
  }

  private void grantAccess(Connection connection, Id contentId, List<Id> controlledDocumentIds, Association task)
  {
    String assignedToName = task.get(Keys.WORKFLOW_ASSIGNED_TO_NAME);
    if (assignedToName != null)
    {
      Set<Id> controlledContentIds = new HashSet<Id>();
      for (Id controlledDocumentId : controlledDocumentIds)
      {
        controlledContentIds.add(controlledDocumentId);
        getControlledContent(connection, controlledDocumentId, controlledContentIds);
      }

      int assignedToType = task.get(Keys.WORKFLOW_ASSIGNED_TO_TYPE);
      int access = task.get(Keys.WORKFLOW_ACCESS);

      Id assignedToId = repository.retrieveEntityId(connection, assignedToName, assignedToType == WorkflowConstants.TYPE_1_USER ? EntityType.USER : EntityType.GROUP);

      for (Id controlledContentId : controlledContentIds)
      {
        addAuthorities(connection, contentId, controlledContentId, assignedToId, assignedToType, access);
      }

      Id taskId = task.getId();
      addAuthorities(connection, contentId, taskId, assignedToId, assignedToType, WorkflowConstants.ACCESS_2_READ_WRITE);
    }
  }

  private boolean isUserUpdate(Connection connection, Association task)
  {
    Id modifiedBy = repository.retrievePropertyModifiedBy(connection, task.getId(), Repository.NULL_INDEX, Keys.WORKFLOW_STATUS);
    return !modifiedBy.equals(Repository.SYSTEM_USER_ID);
  }

  private void processComplete(Connection connection, Id contentId, Properties properties, Association task)
  {
    if (isUserUpdate(connection, task))
    {
      Log.debug("WorkflowOperation.processComplete: task is complete, task=%s", task);
      List<Id> controlledDocumentIds = repository.getContainingDocuments(connection, contentId);
      sendCompletionMessage(connection, properties, task, controlledDocumentIds);
    }
  }

  private void processPending(Connection connection, Id contentId, Properties properties, Association task)
  {
    Log.debug("WorkflowOperation.processPending: task is ready, task=%s", task);
    List<Id> controlledDocumentIds = repository.getContainingDocuments(connection, contentId);
    grantAccess(connection, contentId, controlledDocumentIds, task);
    sendReadyMessage(connection, properties, task, controlledDocumentIds);
    task.set(Keys.WORKFLOW_STATUS, WorkflowConstants.STATUS_2_READY);
  }

  private void processReadyOrInProgress(Connection connection, Id contentId, Association task)
  {
    Log.debug("WorkflowOperation.processReadyOrInProgress: task is ready or in progress, task=%s", task);
    List<Id> controlledDocumentIds = repository.getContainingDocuments(connection, contentId);
    grantAccess(connection, contentId, controlledDocumentIds, task);
  }

  private void processRejected(Connection connection, Id contentId, Properties properties, Association task)
  {
    if (isUserUpdate(connection, task))
    {
      Log.debug("WorkflowOperation.processRejected: task is rejected, task=%s", task);
      List<Id> controlledDocumentIds = repository.getContainingDocuments(connection, contentId);
      sendRejectionMessage(connection, properties, task, controlledDocumentIds);
    }
  }

  private int removeAuthorities(Connection connection, Id workflowId)
  {
    StringBuilder s = new StringBuilder();
    s.append("delete from authority\n");
    s.append("where workflow_id = ?");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, workflowId.getId());
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private void sendCompletionMessage(Connection connection, Properties properties, Association task, List<Id> controlledDocumentIds)
  {
    boolean isNotifyOwnerOnCompletion = properties.get(DefaultProperties.NOTIFY_OWNER_ON_COMPLETION, false);
    if (isNotifyOwnerOnCompletion)
    {
      String subjectTemplate = serverProperties.getWorkflowCompletionSubject();
      String bodyTemplate = serverProperties.getWorkflowCompletionBody();
      sendMessage(connection, properties, task, controlledDocumentIds, subjectTemplate, bodyTemplate);
    }
  }

  private void sendMessage(Association task, String subjectTemplate, String bodyTemplate, List<String> toEmailAddresses, List<String> ccEmailAddresses, String readyDocumentList)
  {
    Object[] parameters = new Object[] {
        task.get(Keys.WORKFLOW_TASK),
        formatDateComponent(task.get(Keys.WORKFLOW_DUE_DATE)),
        Utility.coalesce(task.get(Keys.WORKFLOW_ASSIGNED_TO_NAME), NOT_AVAILABLE),
        Utility.coalesce(task.get(Keys.WORKFLOW_ASSIGNED_TO_TYPE), NOT_AVAILABLE),
        formatDateComponent(task.get(Keys.WORKFLOW_COMPLETION_DATE)),
        Utility.coalesce(task.get(Keys.WORKFLOW_COMPLETED_BY_NAME), NOT_AVAILABLE),
        Utility.coalesce(task.get(Keys.WORKFLOW_COMPLETED_BY_TYPE), NOT_AVAILABLE),
        readyDocumentList
    };

    String subject = MessageFormat.format(subjectTemplate, parameters);
    String body = MessageFormat.format(bodyTemplate, parameters);
    body = body.replaceAll("\n", "\r\n");

    mailer.send(toEmailAddresses, ccEmailAddresses, subject, body);
  }

  private void sendMessage(Connection connection, Properties properties, Association task, List<Id> controlledDocumentIds, String subjectTemplate, String bodyTemplate)
  {
    List<String> toEmailAddresses = new LinkedList<String>();
    String readyDocumentList = formatDocumentList(connection, controlledDocumentIds, toEmailAddresses);
    sendMessage(task, subjectTemplate, bodyTemplate, toEmailAddresses, null, readyDocumentList);
  }

  private void sendReadyMessage(Connection connection, Properties properties, Association task, List<Id> controlledDocumentIds)
  {
    boolean isNotifyAssigneeOnReady = properties.get(DefaultProperties.NOTIFY_ASSIGNEE_ON_READY, false);
    boolean isNotifyOwnerOnReady = properties.get(DefaultProperties.NOTIFY_OWNER_ON_READY, false);

    if (isNotifyAssigneeOnReady || isNotifyOwnerOnReady)
    {
      List<String> toEmailAddresses = new LinkedList<String>();

      if (isNotifyAssigneeOnReady)
      {
        String name = task.get(Keys.WORKFLOW_ASSIGNED_TO_NAME);
        int type = task.get(Keys.WORKFLOW_ASSIGNED_TO_TYPE);
        getEmailAddresses(connection, name, type, toEmailAddresses);
      }

      List<String> ccEmailAddresses = isNotifyOwnerOnReady ? new LinkedList<String>() : null;
      String readyDocumentList = formatDocumentList(connection, controlledDocumentIds, ccEmailAddresses);

      String subjectTemplate = serverProperties.getWorkflowReadySubject();
      String bodyTemplate = serverProperties.getWorkflowReadyBody();
      sendMessage(task, subjectTemplate, bodyTemplate, toEmailAddresses, ccEmailAddresses, readyDocumentList);
    }
  }

  private void sendRejectionMessage(Connection connection, Properties properties, Association task, List<Id> controlledDocumentIds)
  {
    boolean isNotifyOwnerOnRejection = properties.get(DefaultProperties.NOTIFY_OWNER_ON_REJECTION, false);
    if (isNotifyOwnerOnRejection)
    {
      String subjectTemplate = serverProperties.getWorkflowRejectionSubject();
      String bodyTemplate = serverProperties.getWorkflowRejectionBody();
      sendMessage(connection, properties, task, controlledDocumentIds, subjectTemplate, bodyTemplate);
    }
  }

  /**
   * Update workflow status.
   * <p/>
   * Parallel means parallel to the previous the one. The first task in a
   * parallel set is marked Sequential to distinguish it from the previous set.
   * <p/>
   * An alternative to the switch statement would be a table of task-status,
   * current-state where each cell contains action, new state.
   * <p/>
   * It may be worthwhile to validate all parameters on input and convert to
   * enums and strongly typed values.
   */
  private void updateWorkflow(Connection connection, Content content)
  {
    removeAuthorities(connection, content.getId());

    @SuppressWarnings("unchecked")
    Sequence<Association> sequence = (Sequence<Association>)retrieveProperty(connection, content, Keys.PROPERTIES);
    Properties properties = new Properties(sequence);

    State state = State.READY;
    Id contentId = content.getId();
    Sequence<Association> workflowTasks = content.get(Keys.WORKFLOW);

    for (Association task : workflowTasks)
    {
      int order = task.get(Keys.WORKFLOW_ORDER, 0);
      int status = task.get(Keys.WORKFLOW_STATUS, 0);

      // A task is ready to schedule if it is:
      // 1. Pending, and either:
      //    a) the first sequential task or
      //    b) a subsequent parallel task
      // 2. Only preceded by completed or completed_server tasks.

      switch (status)
      {
        case WorkflowConstants.STATUS_1_PENDING:
          state = checkPending(connection, contentId, properties, task, state, order);
          break;
        case WorkflowConstants.STATUS_2_READY:
        case WorkflowConstants.STATUS_3_IN_PROGRESS:
          processReadyOrInProgress(connection, contentId, task);
          state = State.NOT_READY;
          break;
        case WorkflowConstants.STATUS_4_COMPLETED:
          processComplete(connection, contentId, properties, task);
          break;
        case WorkflowConstants.STATUS_5_REJECTED:
          processRejected(connection, contentId, properties, task);
          state = State.NOT_READY;
          break;
      }
    }
  }

  public void updateWorkflow(List<Content> contents) throws InvalidContentException, InvalidContentIdException, AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Jdbc.setAutoCommit(connection, false);
      Date date = new Date();
      List<Status> statusList = new LinkedList<Status>();
      for (Content content : contents)
      {
        ContentType contentType = (ContentType)retrieveProperty(connection, content, Keys.CONTENT_TYPE);
        if (contentType == ContentType.WORKFLOW)
        {
          Sequence<Association> workflowTasks = content.get(Keys.WORKFLOW);
          if (workflowTasks != null)
          {
            updateWorkflow(connection, content);
            repository.updateProperty(connection, content.getId(), Keys.WORKFLOW, workflowTasks, date, Repository.SYSTEM_USER_ID);
            updateStatusList(connection, Repository.SYSTEM_USER_ID, content.getId(), date, ACTION_UPDATED, statusList);
          }
        }
      }
      postStatus(connection, statusList, Repository.ALL_QUEUES);
      Jdbc.setAutoCommit(connection, true);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  public enum State
  {
    NOT_READY, READY, SCHEDULING
  }
}
