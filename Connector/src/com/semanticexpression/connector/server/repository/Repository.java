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

package com.semanticexpression.connector.server.repository;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.semanticexpression.connector.client.rpc.ConnectorServiceAsync.MatchingNameType;
import com.semanticexpression.connector.server.Log;
import com.semanticexpression.connector.server.exception.SqlRuntimeException;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Properties;
import com.semanticexpression.connector.shared.Relationship;
import com.semanticexpression.connector.shared.RelationshipType;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.WorkflowConstants;
import com.semanticexpression.connector.shared.enums.ContentType;

public class Repository
{
  public static final Id ALL_QUEUES = new Id(-1L); // send to all status queues
  public static final String ENUM_PACKAGE_NAME = ContentType.class.getPackage().getName();
  public static final String ENUM_VALUE_DELIMITER = ":";
  public static final Id GUEST_USER_ID = new Id(-1L); // confers no authority
  public static final int MAX_NAME_LENGTH = 32;
  public static final Integer NULL_INDEX = -1; // Derby does not enforce the unique constraint on columns with null values
  public static final Id SYSTEM_USER_ID = new Id(0L); // confers all authority
  public static final String SYSTEM_USER_NAME = "System";

  private ConnectionPool connectionPool;

  public Repository(String databaseUrl, int maximumConnections)
  {
    connectionPool = new ConnectionPool(databaseUrl, maximumConnections);
  }

  public AccessAuthorization checkEntityAccess(Connection connection, Id userId, Id contentId, AccessRequested accessRequested)
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("created_by,\n");
    s.append("published_at\n");
    s.append("from entity\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);

    ResultSet resultSet = Jdbc.executeQuery(preparedStatement, contentId.getId());
    try
    {
      AccessAuthorization accessAuthorization = getDefaultAccessForNonExistantContent(accessRequested);
      if (Jdbc.next(resultSet))
      {
        Id createdById = Jdbc.getId(resultSet, 1);
        boolean isPublished = Jdbc.getObject(resultSet, 2) != null;
        switch (accessRequested)
        {
          case READ:
            accessAuthorization = isPublished ? AccessAuthorization.GRANTED : userId.equals(createdById) ? AccessAuthorization.GRANTED : AccessAuthorization.INDETERMINATE;
            break;
          case WRITE:
            accessAuthorization = isPublished ? AccessAuthorization.DENIED : userId.equals(createdById) ? AccessAuthorization.GRANTED : AccessAuthorization.INDETERMINATE;
            break;
        }
      }
      return accessAuthorization;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public AccessAuthorization checkGroupAuthority(Connection connection, Id userId, Id contentId, AccessRequested accessRequested)
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("a.access_type\n");
    s.append("from authority as a, property as p\n");
    s.append("where a.content_id = ?\n"); // contentId.getId()
    s.append("and a.granted_to_type = ?\n"); // WorkflowConstants.TYPE_2_GROUP
    s.append("and a.granted_to_id = p.entity_id\n");
    s.append("and p.property_name = ?\n"); // Keys.GROUP
    s.append("and p.entity_value = ?\n"); // userId.getId()

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);

    ResultSet resultSet = Jdbc.executeQuery(preparedStatement, contentId.getId(), WorkflowConstants.TYPE_2_GROUP, Keys.GROUP, userId.getId());
    try
    {
      AccessAuthorization accessAuthorization = AccessAuthorization.INDETERMINATE;
      if (Jdbc.next(resultSet))
      {
        int accessType = Jdbc.getInt(resultSet, 1);
        switch (accessRequested)
        {
          case READ:
            accessAuthorization = AccessAuthorization.GRANTED;
            break;
          case WRITE:
            accessAuthorization = accessType == WorkflowConstants.ACCESS_2_READ_WRITE ? AccessAuthorization.GRANTED : AccessAuthorization.DENIED;
            break;
        }
      }
      return accessAuthorization;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public AccessAuthorization checkUserAuthority(Connection connection, Id userId, Id contentId, AccessRequested accessRequested)
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("access_type\n");
    s.append("from authority\n");
    s.append("where content_id = ?\n");
    s.append("and granted_to_id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);

    ResultSet resultSet = Jdbc.executeQuery(preparedStatement, contentId.getId(), userId.getId());
    try
    {
      AccessAuthorization accessAuthorization = AccessAuthorization.INDETERMINATE;
      if (Jdbc.next(resultSet))
      {
        int accessType = Jdbc.getInt(resultSet, 1);
        switch (accessRequested)
        {
          case READ:
            accessAuthorization = AccessAuthorization.GRANTED;
            break;
          case WRITE:
            accessAuthorization = accessType == WorkflowConstants.ACCESS_2_READ_WRITE ? AccessAuthorization.GRANTED : AccessAuthorization.DENIED;
            break;
        }
      }
      return accessAuthorization;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public int countProperties(Connection connection, Id entityId)
  {
    int entityReferenceCount = 0;
    StringBuilder s = new StringBuilder();
    s.append("select count(distinct id)\n");
    s.append("from property\n");
    s.append("where entity_id = ?");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        entityReferenceCount = Jdbc.getInt(resultSet, 1);
      }
      return entityReferenceCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public int countReferences(Connection connection, Id entityId)
  {
    int entityReferenceCount = 0;
    StringBuilder s = new StringBuilder();
    s.append("select count(distinct entity_id)\n");
    s.append("from property\n");
    s.append("where entity_value = ?");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        entityReferenceCount = Jdbc.getInt(resultSet, 1);
      }
      return entityReferenceCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Id createEntity(Connection connection, int type, String name, Id parent, Id userId, Date createdAt)
  {
    StringBuilder s = new StringBuilder();
    s.append("insert into entity (id, type, name, parent_entity_id, created_by, created_at)\n");
    s.append("values (?, ?, ?, ?, ?, ?)");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Id entityId = IdManager.createIdentifier();
      Jdbc.executeUpdate(preparedStatement, entityId.getId(), type, name, parent == null ? null : parent.getId(), userId.getId(), createdAt == null ? null : createdAt.getTime());
      return entityId;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public int createProperty(Connection connection, Id entityId, String name, Object value, Date validFrom, Id userId)
  {
    int updateCount;

    if (value instanceof Iterable<?>)
    {
      updateCount = createPropertyFromIterable(connection, entityId, name, (Iterable<?>)value, validFrom, userId);
    }
    else
    {
      updateCount = createProperty(connection, entityId, name, value, Repository.NULL_INDEX, validFrom, userId);
    }

    return updateCount;
  }

  private int createProperty(Connection connection, Id entityId, String name, Object value, int index, Date validFrom, Id userId)
  {
    int updateCount;

    if (value instanceof Association)
    {
      updateCount = createPropertyFromAssociation(connection, entityId, name, (Association)value, index, validFrom, userId);
    }
    else
    {
      updateCount = createPropertyFromScalarValue(connection, entityId, name, value, index, validFrom, userId);
    }

    return updateCount;
  }

  public int createPropertyFromAssociation(Connection connection, Id entityId, String name, Association association, int index, Date validFrom, Id userId)
  {
    Id associationId = association.getId();
    realiseEntity(connection, associationId, EntityType.ASSOCIATION, null, entityId, userId, validFrom);
    createPropertyFromScalarValue(connection, entityId, name, associationId, index, validFrom, userId);

    int updateCount = 0;

    for (Entry<String, Object> property : association.getProperties().entrySet())
    {
      String associationName = property.getKey();
      Object associationValue = property.getValue();
      updateCount += createProperty(connection, associationId, associationName, associationValue, validFrom, userId);
    }

    return updateCount;
  }

  private int createPropertyFromIterable(Connection connection, Id entityId, String name, Iterable<?> iterable, Date validFrom, Id userId)
  {
    int index = 0;
    int updateCount = 0;

    for (Object value : iterable)
    {
      updateCount += createProperty(connection, entityId, name, value, index, validFrom, userId);
      index++;
    }

    return updateCount;
  }

  public int createPropertyFromScalarValue(Connection connection, Id entityId, String name, Object value, int index, Date validFrom, Id userId)
  {
    Log.debug("Repository.createPropertyFromScalarValue: entityId=%d, name=%s, value=%s, index=%d, validFrom=%s", entityId.getId(), name, value, index, validFrom);

    if (value == null)
    {
      Log.debug("Repository.createPropertyFromScalarValue: null values are not stored, an update with a null value is a delete");
      return 0;
    }

    if (name.equals(Keys.CREATED_AT) || name.equals(Keys.CREATED_BY) || name.equals(Keys.MODIFIED_AT) || name.equals(Keys.MODIFIED_BY))
    {
      return 0;
    }

    ColumnMapping columnMapping = mapColumn(value);
    String columnName = columnMapping.getColumnName();
    value = columnMapping.getValue();

    StringBuilder s = new StringBuilder();
    s.append("insert into property\n");
    s.append("(\n");
    s.append("  entity_id,\n");
    s.append("  property_name,\n");
    s.append("  index,\n");
    s.append("  " + columnName + ",\n");
    s.append("  valid_from,\n");
    s.append("  modified_by\n");
    s.append(")\n");
    s.append("values(?, ?, ?, ?, ?, ?)");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, entityId.getId(), name, index, value, validFrom.getTime(), userId.getId());
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public boolean deleteEntity(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("delete from entity\n");
    s.append("where id = ?");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int deleteCount = Jdbc.executeUpdate(preparedStatement, entityId.getId());
      boolean isDeleted = deleteCount == 1;
      return isDeleted;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public int deleteProperty(Connection connection, Id entityId, String name)
  {
    Log.debug("Repository.deleteProperty: entityId=%d, name=%s", entityId.getId(), name);

    StringBuilder s = new StringBuilder();
    s.append("delete from property\n");
    s.append("where entity_id = ?");
    s.append("and property_name = ?");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int deleteCount = Jdbc.executeUpdate(preparedStatement, entityId.getId(), name);
      return deleteCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public ConnectionPool getConnectionPool()
  {
    return connectionPool;
  }

  public List<Id> getContainingDocuments(Connection connection, Id contentId)
  {
    List<Id> containingDocumentIds = new LinkedList<Id>();
    getContainingDocuments(connection, contentId, containingDocumentIds);
    return containingDocumentIds;
  }

  public void getContainingDocuments(Connection connection, Id contentId, List<Id> containingDocumentIds)
  {
    StringBuilder s = new StringBuilder();
    s.append("select distinct entity_id\n");
    s.append("from property\n");
    s.append("where property_name = ?\n"); // Keys.PARTS
    s.append("and entity_value = ?\n");
    s.append("and valid_to is null\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, Keys.PARTS, contentId.getId());
      while (Jdbc.next(resultSet))
      {
        Id documentId = Jdbc.getId(resultSet, 1);
        containingDocumentIds.add(documentId);
      }
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public List<Id> getContainingDocumentsRoot(Connection connection, Id contentId)
  {
    List<Id> rootDocumentIds = new LinkedList<Id>();
    Set<Id> alreadyEncounteredIds = new HashSet<Id>();
    getContainingDocumentsRoot(connection, contentId, rootDocumentIds, alreadyEncounteredIds);
    return rootDocumentIds;
  }

  public void getContainingDocumentsRoot(Connection connection, Id contentId, List<Id> rootDocumentIds, Set<Id> alreadyEncounteredIds)
  {
    List<Id> containingDocumentIds = new LinkedList<Id>();
    getContainingDocuments(connection, contentId, containingDocumentIds);
    if (containingDocumentIds.size() == 0)
    {
      rootDocumentIds.add(contentId);
    }
    else
    {
      for (Id containingDocument : containingDocumentIds)
      {
        if (!alreadyEncounteredIds.contains(containingDocument))
        {
          getContainingDocumentsRoot(connection, containingDocument, rootDocumentIds, alreadyEncounteredIds);
          alreadyEncounteredIds.add(containingDocument);
        }
      }
    }
  }

  private AccessAuthorization getDefaultAccessForNonExistantContent(AccessRequested accessRequested)
  {
    AccessAuthorization defaultAccessForNonExistantContent = null;
    switch (accessRequested)
    {
      case READ:
        defaultAccessForNonExistantContent = AccessAuthorization.DENIED;
        break;
      case WRITE:
        defaultAccessForNonExistantContent = AccessAuthorization.GRANTED;
        break;
    }
    return defaultAccessForNonExistantContent;
  }

  public BasePagingLoadResult<Association> getMatchingEntityNames(Connection connection, String wildcard, int entityType, PagingLoadConfig pagingLoadConfig)
  {
    int offset = pagingLoadConfig.getOffset();
    int limit = pagingLoadConfig.getLimit();
    SortDir sortDir = pagingLoadConfig.getSortDir();

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  id,\n");
    s.append("  name\n");
    s.append("from entity\n");
    s.append("where type = ?\n");
    s.append("and lower(name) like ?\n");
    s.append("order by lower(name) " + (sortDir == SortDir.NONE ? "asc" : sortDir.name()) + "\n");
    s.append("offset " + offset + " rows\n");
    s.append("fetch first " + limit + " rows only");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      List<Association> entityNames = new LinkedList<Association>();
      String normalizedWildcard = getNormalizedWildcard(wildcard);
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityType, normalizedWildcard);
      while (Jdbc.next(resultSet))
      {
        Id entityId = Jdbc.getId(resultSet, 1);
        String entityName = Jdbc.getString(resultSet, 2);
        Association association = new Association(entityId);
        association.set(Keys.NAME, entityName);
        entityNames.add(association);
      }
      int totalLength = getMatchingEntityNamesTotalLength(connection, wildcard, entityType);
      return new BasePagingLoadResult<Association>(entityNames, offset, totalLength);
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private int getMatchingEntityNamesTotalLength(Connection connection, String matchText, int entityType)
  {
    StringBuilder s = new StringBuilder();

    s.append("select count(*)\n");
    s.append("from entity\n");
    s.append("where type = ?\n");
    s.append("and lower(name) like ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int totalLength = 0;
      String normalizedWildcard = getNormalizedWildcard(matchText);
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityType, normalizedWildcard);
      if (Jdbc.next(resultSet))
      {
        totalLength = Jdbc.getInt(resultSet, 1);
      }
      return totalLength;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public BasePagingLoadResult<Association> getMatchingKeywords(Connection connection, String wildcard, PagingLoadConfig pagingLoadConfig)
  {
    int offset = pagingLoadConfig.getOffset();
    int limit = pagingLoadConfig.getLimit();
    SortDir sortDir = pagingLoadConfig.getSortDir();
    String sortField = pagingLoadConfig.getSortField();

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  p2.entity_id as entity_id,\n");
    s.append("  p2.varchar_value as name,\n"); // Must match client id for remote sort to work
    s.append("  count(*) as count\n"); // Must match client id for remote sort to work
    s.append("from\n");
    s.append("  property as p1,\n");
    s.append("  property as p2\n");
    s.append("where p1.property_name = ?\n"); // Keys.KEYWORDS
    s.append("  and p1.valid_to is null\n");
    s.append("  and p2.entity_id = p1.entity_value\n");
    s.append("  and p2.property_name = ?\n"); // Keys.NAME
    s.append("  and p2.varchar_value like ?\n"); // matchText
    s.append("  and p2.valid_to is null\n");
    s.append("group by\n");
    s.append("  p2.entity_id,\n");
    s.append("  p2.varchar_value\n");
    s.append("order by\n");
    s.append("  " + (sortDir == SortDir.NONE ? "name" : (sortField + " " + sortDir.name())) + "\n");
    s.append("offset " + offset + " rows\n");
    s.append("fetch first " + limit + " rows only\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      List<Association> keywords = new LinkedList<Association>();
      String normalizedWildcard = getNormalizedWildcard(wildcard);
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, Keys.KEYWORDS, Keys.NAME, normalizedWildcard);
      while (Jdbc.next(resultSet))
      {
        Id entityId = Jdbc.getId(resultSet, 1);
        String keywordName = Jdbc.getString(resultSet, 2);
        int useCount = Jdbc.getInt(resultSet, 3);
        Association keyword = new Association(entityId);
        keyword.set(Keys.NAME, keywordName);
        keyword.set(Keys.COUNT, useCount);
        keywords.add(keyword);
      }
      int totalLength = getMatchingKeywordsTotalLength(connection, wildcard);
      return new BasePagingLoadResult<Association>(keywords, offset, totalLength);
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private int getMatchingKeywordsTotalLength(Connection connection, String matchText)
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  count(distinct p2.varchar_value)\n");
    s.append("from\n");
    s.append("  property as p1,\n");
    s.append("  property as p2\n");
    s.append("where p1.property_name = ?\n"); // Keys.KEYWORDS
    s.append("  and p1.valid_to is null\n");
    s.append("  and p2.entity_id = p1.entity_value\n");
    s.append("  and p2.property_name = ?\n"); // Keys.NAME
    s.append("  and p2.varchar_value like ?\n"); // matchText
    s.append("  and p2.valid_to is null\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int totalLength = 0;
      String normalizedWildcard = getNormalizedWildcard(matchText);
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, Keys.KEYWORDS, Keys.NAME, normalizedWildcard);
      if (Jdbc.next(resultSet))
      {
        totalLength = Jdbc.getInt(resultSet, 1);
      }
      return totalLength;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public BasePagingLoadResult<Association> getMatchingTags(Connection connection, Id userId, String wildcard, MatchingNameType matchingNameType, PagingLoadConfig pagingLoadConfig)
  {
    int offset = pagingLoadConfig.getOffset();
    int limit = pagingLoadConfig.getLimit();
    SortDir sortDir = pagingLoadConfig.getSortDir();
    String sortField = pagingLoadConfig.getSortField();

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  p2.entity_id as id,\n");
    s.append("  p2.varchar_value as name,\n"); // Must match client id for remote sort to work
    s.append("  count(*) as count\n"); // Must match client id for remote sort to work
    s.append("from\n");
    s.append("  entity as e1,\n");
    s.append("  property as p1,\n");
    s.append("  property as p2,\n");
    s.append("  property as p3\n");
    s.append("where p1.entity_id = e1.id\n");

    s.append("and p1.property_name = ?\n"); // Keys.TAGS
    s.append("and p1.valid_to is null\n");

    s.append("and p2.entity_id = p1.entity_value\n");
    s.append("and p2.property_name = ?\n"); // Keys.NAME
    s.append("and p2.varchar_value like ?\n"); // wildcard
    s.append("and p2.valid_to is null\n");

    s.append("and p3.entity_id = p1.entity_value\n");
    s.append("and p3.property_name = ?\n"); // Keys.TAG_IS_PRIVATE
    s.append("and p3.valid_to is null\n");
    s.append("and ((? = false and p3.boolean_value = false) or (? = true and p3.boolean_value = true and e1.id = ?))\n"); // isPrivate, isPrivate, userId

    s.append("group by p2.entity_id, p2.varchar_value\n");
    s.append("order by\n");
    s.append("  " + (sortDir == SortDir.NONE ? "name" : (sortField + " " + sortDir.name())) + "\n");
    s.append("offset " + offset + " rows\n");
    s.append("fetch first " + limit + " rows only\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      List<Association> tags = new LinkedList<Association>();
      String normalizedWildcard = getNormalizedWildcard(wildcard);
      boolean isPrivate = matchingNameType == MatchingNameType.PRIVATE_TAG;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, Keys.TAGS, Keys.NAME, normalizedWildcard, Keys.TAG_IS_PRIVATE, isPrivate, isPrivate, userId.getId());
      while (Jdbc.next(resultSet))
      {
        Id entityId = Jdbc.getId(resultSet, 1);
        String tagName = Jdbc.getString(resultSet, 2);
        int useCount = Jdbc.getInt(resultSet, 3);
        Association tag = new Association(entityId);
        tag.set(Keys.NAME, tagName);
        tag.set(Keys.COUNT, useCount);
        tags.add(tag);
      }
      int totalLength = getMatchingTagsTotalLength(connection, userId, wildcard, matchingNameType);
      return new BasePagingLoadResult<Association>(tags, offset, totalLength);
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private int getMatchingTagsTotalLength(Connection connection, Id userId, String wildcard, MatchingNameType matchingNameType)
  {
    return -1;
  }

  private String getNameSql(int j)
  {
    StringBuilder s = new StringBuilder();
    s.append("p" + j + ".property_name ||");
    s.append("case\n");
    s.append("when p" + j + ".index != " + Repository.NULL_INDEX + "\n");
    s.append("then cast('[' || trim(cast(p" + j + ".index as char(10))) || ']' as varchar(16))\n");
    s.append("end");
    return s.toString();
  }

  private String getNormalizedWildcard(String wildcard)
  {
    return wildcard.toLowerCase() + (wildcard.contains("%") ? "" : "%");
  }

  public List<Relationship> getRelationships(Connection connection, Id contentId)
  {
    String parentSql = getRelationshipsParentSql();
    String childrenSql = getRelationshipsChildrenSql();
    List<Relationship> relationships = new LinkedList<Relationship>();

    getRelationships(connection, contentId, relationships, parentSql, Keys.DERIVED_FROM, RelationshipType.DERIVED_FROM_PARENT);
    getRelationships(connection, contentId, relationships, childrenSql, Keys.DERIVED_FROM, RelationshipType.DERIVED_FROM_CHILD);
    getRelationships(connection, contentId, relationships, childrenSql, Keys.PARTS, RelationshipType.PARTS_PARENT); // Keys.DERIVED_FROM is inverse WRT Keys.PARTS
    getRelationships(connection, contentId, relationships, parentSql, Keys.PARTS, RelationshipType.PARTS_CHILD); // Keys.DERIVED_FROM is inverse WRT Keys.PARTS

    return relationships;
  }

  private void getRelationships(Connection connection, Id contentId, List<Relationship> relationships, String sql, String relationshipName, RelationshipType relationshipType)
  {
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, sql);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, relationshipName, contentId.getId(), Keys.TITLE);
      while (Jdbc.next(resultSet))
      {
        Id relationshipId = Jdbc.getId(resultSet, 1);
        String title = Jdbc.getString(resultSet, 2);
        Date createdAt = new Date(Jdbc.getLong(resultSet, 3));
        String createdBy = Jdbc.getString(resultSet, 4);
        Relationship relationship = new Relationship();
        relationship.set(Keys.RELATIONSHIP_TYPE, relationshipType);
        relationship.set(Keys.CREATED_AT, createdAt);
        relationship.set(Keys.CREATED_BY, createdBy);
        relationship.set(Keys.TITLE, title);
        relationship.set(Keys.CONTENT_ID, relationshipId);
        relationships.add(relationship);
      }
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private String getRelationshipsChildrenSql()
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  p1.entity_id,\n");
    s.append("  p2.varchar_value,\n");
    s.append("  e1.created_at,\n");
    s.append("  e2.name\n");
    s.append("from property as p1, property as p2, entity as e1, entity as e2\n");
    s.append("where p1.property_name = ?\n"); // Keys.DERIVED_FROM or Keys.PARTS
    s.append("and p1.entity_value = ?\n");
    s.append("and p1.valid_to is null\n");
    s.append("and p2.entity_id = p1.entity_id\n"); // the title of the child
    s.append("and p2.property_name = ?\n"); // Keys.TITLE
    s.append("and p2.valid_to is null\n"); // the current title of the child
    s.append("and e1.id = p1.entity_id\n"); // the child entity
    s.append("and e2.id = e1.created_by\n"); // the owner of the child entity
    s.append("order by e1.created_at");

    return s.toString();
  }

  private String getRelationshipsParentSql()
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  p1.entity_value,\n");
    s.append("  p2.varchar_value,\n");
    s.append("  e1.created_at,\n");
    s.append("  e2.name\n");
    s.append("from property as p1, property as p2, entity as e1, entity as e2\n");
    s.append("where p1.property_name = ?\n"); // Keys.DERIVED_FROM or Keys.PARTS
    s.append("and p1.entity_id = ?\n");
    s.append("and p1.valid_to is null\n");
    s.append("and p2.entity_id = p1.entity_value\n"); // the title of the parent
    s.append("and p2.property_name = ?\n"); // Keys.TITLE
    s.append("and p2.valid_to is null\n"); // the current title of the parent
    s.append("and e1.id = p1.entity_value\n"); // the parent entity
    s.append("and e2.id = e1.created_by\n"); // the owner of the parent entity

    return s.toString();
  }

  public Sequence<Association> getTags(Connection connection, Id userId, Id taggedEntityId, boolean isMyPrivateTags, boolean isMyPublicTags, boolean isOtherPublicTags)
  {
    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  p1.entity_id as tag_id,\n");
    s.append("  p2.varchar_value as tag_name,\n");
    s.append("  p3.entity_id as user_id,\n");
    s.append("  p4.boolean_value as is_private\n");
    s.append("from property as p1, property as p2, property as p3, property as p4\n");
    s.append("where p1.entity_value = ?\n"); // taggedEntityId
    s.append("and p1.property_name = ?\n"); // Keys.TAGGED_ENTITY_IDS
    s.append("and p1.valid_to is null\n");
    s.append("and p2.entity_id = p1.entity_id\n");
    s.append("and p2.property_name = ?\n"); // Keys.NAME
    s.append("and p2.valid_to is null\n");
    s.append("and p3.entity_value = p1.entity_id\n");
    s.append("and p3.property_name = ?\n"); // Keys.TAGS
    s.append("and p3.valid_to is null\n");
    s.append("and p4.entity_id = p1.entity_id\n");
    s.append("and p4.property_name = ? \n"); // Keys.TAG_IS_PRIVATE
    s.append("and p4.valid_to is null\n");
    s.append("order by tag_name, user_id, is_private\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Sequence<Association> tags = new Sequence<Association>();
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, taggedEntityId.getId(), Keys.TAGGED_ENTITY_IDS, Keys.NAME, Keys.TAGS, Keys.TAG_IS_PRIVATE);
      while (Jdbc.next(resultSet))
      {
        Id tagId = Jdbc.getId(resultSet, 1);
        String tagName = Jdbc.getString(resultSet, 2);
        Id tagUserId = Jdbc.getId(resultSet, 3);
        boolean isPrivate = Jdbc.getBoolean(resultSet, 4);
        TagVisibility tagVisibility = getTagVisibility(userId, tagUserId, isPrivate, isMyPrivateTags, isMyPublicTags, isOtherPublicTags);
        if (tagVisibility != null)
        {
          Association tag = new Association(tagId);
          tag.set(Keys.NAME, tagName);
          tag.set(Keys.TAG_VISIBILITY, tagVisibility);
          tags.add(tag);
        }
      }
      return tags;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private TagVisibility getTagVisibility(Id userId, Id tagUserId, boolean isPrivate, boolean isMyPrivateTags, boolean isMyPublicTags, boolean isOtherPublicTags)
  {
    TagVisibility tagVisibility = null;
    if (userId.equals(tagUserId))
    {
      if (isPrivate)
      {
        if (isMyPrivateTags)
        {
          tagVisibility = TagVisibility.MY_PRIVATE;
        }
      }
      else
      {
        if (isMyPublicTags)
        {
          tagVisibility = TagVisibility.MY_PUBLIC;
        }
      }
    }
    else
    {
      if (isPrivate)
      {
        // userId does not have access to private tag created by tagUserId
      }
      else
      {
        if (isOtherPublicTags)
        {
          tagVisibility = TagVisibility.OTHER_PUBLIC;
        }
      }
    }
    return tagVisibility;
  }

  public boolean isExistingEntity(Connection connection, Id entityId, int requestedEntityType)
  {
    Integer actualEntityType = retrieveEntityType(connection, entityId);
    return actualEntityType != null && actualEntityType == requestedEntityType;
  }

  private ColumnMapping mapColumn(Object value)
  {
    String columnName;

    if (value instanceof Integer)
    {
      columnName = "integer_value";
    }
    else if (value instanceof Long)
    {
      columnName = "bigint_value";
    }
    else if (value instanceof Boolean)
    {
      columnName = "boolean_value";
    }
    else if (value instanceof String)
    {
      if (((String)value).length() <= 256)
      {
        columnName = "varchar_value";
      }
      else
      {
        columnName = "clob_value";
      }
    }
    else if (value instanceof byte[])
    {
      columnName = "blob_value";
    }
    else if (value instanceof InputStream)
    {
      columnName = "blob_value";
    }
    else if (value instanceof Date)
    {
      columnName = "utc_millis_value";
      value = ((Date)value).getTime();
    }
    else if (value instanceof Id)
    {
      columnName = "entity_value";
      value = ((Id)value).getId();
    }
    else if (value instanceof Enum<?>)
    {
      columnName = "enum_value";
      value = writeEnum(value);
    }
    else
    {
      throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
    }

    ColumnMapping columnMapping = new ColumnMapping(columnName, value);
    return columnMapping;
  }

  public int preserveProperty(Connection connection, Id entityId, String name, Date validTo)
  {
    Log.debug("Repository.preserveProperty: entityId=%s, name=%s, validTo=%s", entityId, name, validTo);

    int updateCount = 0;

    // Preserve any entities in an Association
    updateCount += preservePropertyAssociation(connection, entityId, name, validTo);

    // Preserve a scalar value, or the Association itself
    updateCount += preservePropertyScalar(connection, entityId, name, validTo);

    return updateCount;
  }

  private int preservePropertyAssociation(Connection connection, Id entityId, String name, Date validTo)
  {
    StringBuilder s = new StringBuilder();
    s.append("select\n");
    s.append("  p.entity_value,\n");
    s.append("  p.property_name\n");
    s.append("from property as p, entity as e\n");
    s.append("where p.entity_id = ?\n");
    s.append("and p.property_name = ?\n");
    s.append("and p.valid_to is null\n");
    s.append("and p.entity_value is not null\n");
    s.append("and e.id = p.entity_value\n");
    s.append("and e.parent_entity_id is not null\n");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = 0;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId(), name);
      while (Jdbc.next(resultSet))
      {
        Id entityValueId = Jdbc.getId(resultSet, 1);
        updateCount += preservePropertyNested(connection, entityValueId, validTo);
      }
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  /**
   * Recursively handles nested entities and scalars
   */
  private int preservePropertyNested(Connection connection, Id entityId, Date validTo)
  {
    StringBuilder s = new StringBuilder();
    s.append("select property_name\n");
    s.append("from property\n");
    s.append("where valid_to is null\n");
    s.append("and entity_id = ?\n");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = 0;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      while (Jdbc.next(resultSet))
      {
        String name = Jdbc.getString(resultSet, 1);
        if (!name.equals(Keys.DERIVED_FROM))
        {
          updateCount += preserveProperty(connection, entityId, name, validTo);
        }
      }
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private int preservePropertyScalar(Connection connection, Id entityId, String name, Date validTo)
  {
    StringBuilder s = new StringBuilder();
    s.append("update property\n");
    s.append("set valid_to = ?\n");
    s.append("where valid_to is null\n");
    s.append("and entity_id = ?\n");
    s.append("and property_name = ?\n");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      int updateCount = Jdbc.executeUpdate(preparedStatement, validTo.getTime(), entityId.getId(), name);
      return updateCount;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Object readEnum(Object rowValue)
  {
    try
    {
      String[] components = rowValue.toString().split(ENUM_VALUE_DELIMITER);
      String className = ENUM_PACKAGE_NAME + "." + components[0];
      Class<? extends Enum> enumClass = (Class<? extends Enum>)Class.forName(className);
      Enum<?> enumValue = Enum.valueOf(enumClass, components[1]);
      return enumValue;
    }
    catch (ClassNotFoundException e)
    {
      throw new RuntimeException(e);
    }
  }

  public boolean realiseEntity(Connection connection, Id entityId, int type, String name, Id parent, Id userId, Date createdAt)
  {
    StringBuilder s = new StringBuilder();
    s.append("insert into entity (id, type, name, parent_entity_id, created_by, created_at)\n");
    s.append("values (?, ?, ?, ?, ?, ?)");
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      boolean isNewEntity = false;
      Jdbc.initializeParameters(preparedStatement, entityId.getId(), type, name, parent == null ? null : parent.getId(), userId == null ? null : userId.getId(), createdAt == null ? null : createdAt.getTime());
      try
      {
        int updateCount = preparedStatement.executeUpdate();
        if (updateCount == 1)
        {
          isNewEntity = true;
        }
      }
      catch (SQLIntegrityConstraintViolationException e)
      {
        // expected if row already exists
      }
      catch (SQLException e)
      {
        throw new SqlRuntimeException(e);
      }
      return isNewEntity;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private void retireProperty(Connection connection, Id entityId, String name, Date validTo)
  {
    if (validTo == null)
    {
      deleteProperty(connection, entityId, name);
    }
    else
    {
      preserveProperty(connection, entityId, name, validTo);
    }
  }

  private void retrieveAssociationMetadata(Connection connection, int propertyId, Association association)
  {
    StringBuilder s = new StringBuilder();
    s.append("select\n");
    s.append("  p.valid_from as modified_at,\n");
    s.append("  e1.name as modified_by,\n");
    s.append("  e2.created_at as created_at,\n");
    s.append("  e3.name as created_by\n");
    s.append("from property as p\n");
    s.append("inner join entity as e1\n");
    s.append("on p.modified_by = e1.id\n");
    s.append("inner join entity as e2\n");
    s.append("on p.entity_value = e2.id\n");
    s.append("inner join entity as e3\n");
    s.append("on e2.created_by = e3.id\n");
    s.append("where p.id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, propertyId);
      if (Jdbc.next(resultSet))
      {
        Date modifiedAt = new Date(Jdbc.getLong(resultSet, 1));
        String modifiedBy = Jdbc.getString(resultSet, 2);
        Date createdAt = new Date(Jdbc.getLong(resultSet, 3));
        String createdBy = Jdbc.getString(resultSet, 4);
        association.set(Keys.MODIFIED_AT, modifiedAt);
        association.set(Keys.MODIFIED_BY, modifiedBy);
        association.set(Keys.CREATED_AT, createdAt);
        association.set(Keys.CREATED_BY, createdBy);
      }
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Set<Id> retrieveEntities(Connection connection, int entityType, String name, Object value)
  {
    ColumnMapping columnMapping = mapColumn(value);
    String columnName = columnMapping.getColumnName();
    value = columnMapping.getValue();

    StringBuilder s = new StringBuilder();
    s.append("select p.entity_id\n");
    s.append("from property as p, entity as e\n");
    s.append("where p.property_name = ?\n");
    s.append("and " + columnName + " = ?\n");
    s.append("and e.type = ?\n");
    s.append("and p.entity_id = e.id");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Set<Id> identities = new HashSet<Id>();
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, name, value, entityType);
      while (Jdbc.next(resultSet))
      {
        Id entityId = Jdbc.getId(resultSet, 1);
        identities.add(entityId);
      }
      return identities;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Id retrieveEntity(Connection connection, int entityType, String entityName)
  {
    StringBuilder s = new StringBuilder();
    s.append("select id\n");
    s.append("from entity\n");
    s.append("where type = ?\n");
    s.append("and name = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Id entityId = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityType, entityName);
      if (Jdbc.next(resultSet))
      {
        entityId = Jdbc.getId(resultSet, 1);
      }
      return entityId;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Date retrieveEntityCreatedAtDate(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select created_at\n");
    s.append("from entity\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Date createdAt = null;

      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        Long createdAtLong = Jdbc.getLongObject(resultSet, 1);
        if (createdAtLong != null)
        {
          createdAt = new Date(createdAtLong);
        }
      }

      return createdAt;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Id retrieveEntityCreatedById(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select created_by\n");
    s.append("from entity\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Id createdById = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        createdById = Jdbc.getId(resultSet, 1);
      }
      return createdById;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public String retrieveEntityCreatedByName(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select e2.name\n");
    s.append("from entity as e1, entity as e2\n");
    s.append("where e1.id = ?\n");
    s.append("and e1.created_by = e2.id\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      String createdByName = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        createdByName = Jdbc.getString(resultSet, 1);
      }
      return createdByName;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Id retrieveEntityId(Connection connection, String name, int type)
  {
    StringBuilder s = new StringBuilder();
    s.append("select id\n");
    s.append("from entity\n");
    s.append("where name = ?\n");
    s.append("and type = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Id id = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, name, type);
      if (Jdbc.next(resultSet))
      {
        id = Jdbc.getId(resultSet, 1);
      }
      return id;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Id retrieveEntityIgnoreCase(Connection connection, int entityType, String entityName)
  {
    StringBuilder s = new StringBuilder();
    s.append("select id\n");
    s.append("from entity\n");
    s.append("where type = ?\n");
    s.append("and lower(name) = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Id entityId = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityType, entityName.toLowerCase());
      if (Jdbc.next(resultSet))
      {
        entityId = Jdbc.getId(resultSet, 1);
      }
      return entityId;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Date retrieveEntityModifiedAtDate(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select max(valid_from)\n");
    s.append("from property\n");
    s.append("where entity_id = ?\n");
    s.append("and valid_to is null\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Date modifiedAt = null;

      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        Long modifiedAtLong = Jdbc.getLongObject(resultSet, 1);
        if (modifiedAtLong != null)
        {
          modifiedAt = new Date(modifiedAtLong);
        }
      }

      return modifiedAt;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public String retrieveEntityName(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select name\n");
    s.append("from entity\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      String name = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        name = Jdbc.getString(resultSet, 1);
      }
      return name;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Date retrieveEntityPublishedAtDate(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select published_at\n");
    s.append("from entity\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Date publishedAt = null;

      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        Long publishedAtLong = Jdbc.getLongObject(resultSet, 1);
        if (publishedAtLong != null)
        {
          publishedAt = new Date(publishedAtLong);
        }
      }

      return publishedAt;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Date retrieveEntityRetrievedAtDate()
  {
    return null;
  }

  public Integer retrieveEntityType(Connection connection, Id entityId)
  {
    StringBuilder s = new StringBuilder();
    s.append("select type\n");
    s.append("from entity\n");
    s.append("where id = ?\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Integer type = null;

      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      if (Jdbc.next(resultSet))
      {
        type = Jdbc.getInteger(resultSet, 1);
      }

      return type;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Properties retrieveProperties(Connection connection, Id entityId, String name)
  {
    @SuppressWarnings("unchecked")
    Sequence<Association> associationSequence = (Sequence<Association>)retrieveProperty(connection, entityId, name);
    Properties properties = new Properties(associationSequence);
    return properties;
  }

  public Object retrieveProperty(Connection connection, Id entityId, String name)
  {
    return retrieveProperty(connection, entityId, name, null);
  }

  public Object retrieveProperty(Connection connection, Id entityId, String name, Date presentAt)
  {
    List<Object> parameters = new LinkedList<Object>();
    parameters.add(entityId.getId());
    parameters.add(name);
    if (presentAt != null)
    {
      parameters.add(presentAt.getTime());
      parameters.add(presentAt.getTime());
    }

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("p.integer_value,\n");
    s.append("p.bigint_value,\n");
    s.append("p.boolean_value,\n");
    s.append("p.varchar_value,\n");
    s.append("p.clob_value,\n");
    s.append("p.blob_value,\n");
    s.append("p.utc_millis_value,\n");
    s.append("p.enum_value,\n");
    s.append("p.entity_value,\n");
    s.append("p.index,\n");
    s.append("e.type,\n");
    s.append("p.id\n");
    s.append("from property as p\n");
    s.append("left outer join entity as e\n");
    s.append("on e.id = p.entity_value\n");
    s.append("where p.entity_id = ?\n");
    s.append("and p.property_name = ?\n");

    if (presentAt == null)
    {
      s.append("and p.valid_to is null\n");
    }
    else
    {
      s.append("and p.valid_from <= ?\n");
      s.append("and (p.valid_to > ?\n"); // valid_to is the instant at which it was replaced
      s.append("or p.valid_to is null)\n");
    }

    s.append("order by index");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, parameters.toArray());

      Object value = null;
      List<Object> list = null;

      while (Jdbc.next(resultSet))
      {
        int valueColumn = 0;
        Object rowValue = null;

        while (valueColumn < 9 && rowValue == null)
        {
          rowValue = Jdbc.getObject(resultSet, ++valueColumn);
        }

        if (rowValue == null)
        {
          // Null values are not supported, but they sneak in during testing
          Log.debug("Repository.retrieveProperty: value is null, parameters=%s", parameters);
        }
        else if (valueColumn == 5)
        {
          Clob clobValue = (Clob)rowValue;
          int length = (int)Jdbc.getLength(clobValue);
          rowValue = Jdbc.getSubString(clobValue, 1, length);
        }
        else if (valueColumn == 6)
        {
          Blob blobValue = (Blob)rowValue;
          int length = (int)Jdbc.getLength(blobValue);
          rowValue = Jdbc.getBytes(blobValue, 1, length);
        }
        else if (valueColumn == 7)
        {
          rowValue = new Date((Long)rowValue);
        }
        else if (valueColumn == 8)
        {
          rowValue = readEnum(rowValue);
        }
        else if (valueColumn == 9)
        {
          Id entityValueId = new Id((Long)rowValue);
          int entityType = Jdbc.getInt(resultSet, 11);
          if (entityType == EntityType.ASSOCIATION)
          {
            int propertyId = Jdbc.getInt(resultSet, 12);
            Association association = retrievePropertyToAssociation(connection, propertyId, entityValueId, presentAt);
            rowValue = association;
          }
          else
          {
            rowValue = entityValueId;
          }
        }

        int rowIndex = Jdbc.getInt(resultSet, 10);
        if (rowIndex == Repository.NULL_INDEX)
        {
          value = rowValue;
        }
        else
        {
          if (list == null)
          {
            if (rowValue instanceof Association)
            {
              list = new Sequence<Object>();
            }
            else
            {
              list = new LinkedList<Object>();
            }
            value = list;
          }
          list.add(rowValue);
        }
      }
      return value;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Object retrieveProperty(Connection connection, Id entityId, String name, Date presentAt, Object defaultValue)
  {
    Object value = retrieveProperty(connection, entityId, name, presentAt);
    if (value == null)
    {
      value = defaultValue;
    }
    return value;
  }

  /**
   * The JavaDoc for ResultSet says:
   * 
   * <pre>
   * The closing of a ResultSet object does not close the Blob, Clob or NClob
   * objects created by the ResultSet.
   * </pre>
   * 
   * Testing reveals that the same is not true for closing the
   * PreparedStatement.
   * <p/>
   * Caller assumes responsibility for invoking the close() method on the
   * returned PropertyBlob.
   */
  public PropertyBlob retrievePropertyBlob(Connection connection, Id entityId, String name, Date presentAt)
  {
    List<Object> parameters = new LinkedList<Object>();
    parameters.add(entityId.getId());
    parameters.add(name);
    if (presentAt != null)
    {
      parameters.add(presentAt.getTime());
      parameters.add(presentAt.getTime());
    }

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("blob_value\n");
    s.append("from property\n");
    s.append("where entity_id = ?\n");
    s.append("and property_name = ?\n");
    s.append("and index = -1\n");

    if (presentAt == null)
    {
      s.append("and valid_to is null\n");
    }
    else
    {
      s.append("and valid_from <= ?\n");
      s.append("and (valid_to > ?\n"); // valid_to is the instant at which it was replaced
      s.append("or valid_to is null)\n");
    }

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);

    ResultSet resultSet = Jdbc.executeQuery(preparedStatement, parameters.toArray());

    PropertyBlob propertyBlob = null;

    if (Jdbc.next(resultSet))
    {
      Blob blob = Jdbc.getBlob(resultSet, 1);
      propertyBlob = new PropertyBlob(preparedStatement, blob);
    }

    return propertyBlob;
  }

  public Map<String, Object> retrievePropertyMap(Connection connection, Id entityId, String name)
  {
    ArrayList<Object> parameters = new ArrayList<Object>();
    Path path = new Path(name);
    PathComponent[] components = path.getComponents();
    int componentCount = path.getComponentCount();

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("p" + componentCount + ".integer_value,\n");
    s.append("p" + componentCount + ".bigint_value,\n");
    s.append("p" + componentCount + ".boolean_value,\n");
    s.append("p" + componentCount + ".varchar_value,\n");
    s.append("p" + componentCount + ".clob_value,\n");
    s.append("p" + componentCount + ".blob_value,\n");
    s.append("p" + componentCount + ".utc_millis_value,\n");
    s.append("p" + componentCount + ".entity_value,\n");
    for (int j = 1; j <= componentCount; j++)
    {
      if (j > 1)
      {
        s.append(" || '/' || ");
      }
      s.append(getNameSql(j));
    }
    s.append(" as path_name\n");

    s.append("from ");

    for (int j = 1; j <= componentCount; j++)
    {
      if (j > 1)
      {
        s.append(",");
        s.append("\n");
      }
      s.append("property as p" + j);
    }

    s.append("\n");
    s.append("where ");

    for (int i = 0, j = 1, k = 2; j <= componentCount; i++, j++, k++)
    {
      if (j == 1)
      {
        s.append("p" + j + ".entity_id = ?\n");
        parameters.add(entityId.getId());
      }
      PathComponent pathComponent = components[i];
      switch (pathComponent.getComponentType())
      {
        case ENTITY_ID:
          s.append("and p" + j + ".entity_id = ?\n");
          parameters.add(pathComponent.getEntityId());
          break;
        case PROPERTY_NAME:
          s.append("and p" + j + ".property_name = ?\n");
          parameters.add(pathComponent.getName());
          break;
        case WILDCARD:
          // No qualification necessary
          break;
      }
      if (pathComponent.isIndexed())
      {
        s.append("and p" + j + ".index = ?\n");
        parameters.add(pathComponent.getIndex());
      }
      if (j < componentCount)
      {
        s.append("and p" + j + ".entity_value = p" + k + ".entity_id\n");
      }
    }

    s.append("order by path_name\n");

    Map<String, Object> properties = new LinkedHashMap<String, Object>();
    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, parameters.toArray());

      while (Jdbc.next(resultSet))
      {
        Object value = null;
        for (int i = 1; i <= 8 && value == null; i++)
        {
          value = Jdbc.getObject(resultSet, i);
          if (value != null)
          {
            if (i == 7)
            {
              value = new Date((Long)value);
            }
            else if (i == 8)
            {
              value = new Id((Long)value);
            }
          }
        }
        String pathName = Jdbc.getString(resultSet, 8);
        properties.put(pathName, value);
      }

      return properties;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Id retrievePropertyModifiedBy(Connection connection, Id entityId, int index, String propertyName)
  {
    StringBuilder s = new StringBuilder();
    s.append("select modified_by\n");
    s.append("from property\n");
    s.append("where entity_id = ?\n");
    s.append("and property_name = ?\n");
    s.append("and index = ?\n");
    s.append("and valid_to is null\n");

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Id modifiedBy = null;
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId(), propertyName, index);
      if (Jdbc.next(resultSet))
      {
        modifiedBy = Jdbc.getId(resultSet, 1);
      }
      return modifiedBy;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public Set<String> retrievePropertyNames(Connection connection, Id entityId, Date presentAt)
  {
    List<Object> parameters = new LinkedList<Object>();
    parameters.add(entityId.getId());
    if (presentAt != null)
    {
      parameters.add(presentAt.getTime());
      parameters.add(presentAt.getTime());
    }

    StringBuilder s = new StringBuilder();
    s.append("select distinct\n");
    s.append("property_name\n");
    s.append("from property\n");
    s.append("where entity_id = ?\n");

    if (presentAt == null)
    {
      s.append("and valid_to is null\n");
    }
    else
    {
      s.append("and valid_from <= ?\n");
      s.append("and (valid_to > ?\n"); // valid_to is the instant at which it was replaced
      s.append("or valid_to is null)\n");
    }

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      Set<String> names = new HashSet<String>();
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, parameters.toArray());
      while (Jdbc.next(resultSet))
      {
        String name = Jdbc.getString(resultSet, 1);
        names.add(name);
      }
      return names;
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  private Association retrievePropertyToAssociation(Connection connection, int propertyId, Id entityId, Date presentAt)
  {
    Association association = new Association(entityId);
    Set<String> names = retrievePropertyNames(connection, entityId, presentAt);
    for (String name : names)
    {
      Object propertyValue = retrieveProperty(connection, entityId, name, presentAt);
      association.set(name, propertyValue);
    }
    retrieveAssociationMetadata(connection, propertyId, association);
    return association;
  }

  public int updateProperty(Connection connection, Id entityId, String name, Object value, Date validFrom, Id userId)
  {
    Log.debug("Repository.updateProperty: entityId=%d, name=%s, value=%s, validFrom=%s", entityId.getId(), name, value, validFrom);

    int createCount = 0;

    retireProperty(connection, entityId, name, validFrom);

    if (value != null)
    {
      createCount = createProperty(connection, entityId, name, value, validFrom, userId);
    }

    return createCount;
  }

  private Object writeEnum(Object value)
  {
    String className = value.getClass().getName();
    if (!className.startsWith(ENUM_PACKAGE_NAME))
    {
      throw new IllegalArgumentException("Enum not in package " + ENUM_PACKAGE_NAME);
    }
    String enumName = className.substring(ENUM_PACKAGE_NAME.length() + 1); // +1 for dot
    String enumValue = ((Enum<?>)value).name();
    String result = enumName + ENUM_VALUE_DELIMITER + enumValue;
    return result;
  }

  public enum AccessAuthorization
  {
    DENIED, GRANTED, INDETERMINATE
  }

  public enum AccessRequested
  {
    READ, WRITE
  }

  public class ColumnMapping
  {
    private String columnName;
    private Object value;

    public ColumnMapping(String columnName, Object value)
    {
      this.columnName = columnName;
      this.value = value;
    }

    public String getColumnName()
    {
      return columnName;
    }

    public Object getValue()
    {
      return value;
    }
  }

  public static class PropertyBlob
  {
    private Blob blob;
    private PreparedStatement preparedStatement;

    public PropertyBlob(PreparedStatement preparedStatement, Blob blob)
    {
      this.preparedStatement = preparedStatement;
      this.blob = blob;
    }

    public void close()
    {
      Jdbc.close(preparedStatement);
    }

    public InputStream getBinaryStream()
    {
      return Jdbc.getBinaryStream(blob);
    }

    public int getLength()
    {
      return (int)Jdbc.getLength(blob);
    }
  }

}
