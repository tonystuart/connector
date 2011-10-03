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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.server.repository.Repository;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.TagConstants.TagType;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;
import com.semanticexpression.connector.shared.exception.InvalidContentIdException;
import com.semanticexpression.connector.shared.exception.ServerException;
import com.semanticexpression.connector.shared.exception.TagVisibilityException;

public class TagOperation extends BaseOperation
{

  public TagOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  /**
   * Adds content or author tags.
   * <p/>
   * Implementation Notes:
   * <ul>
   * <li>If the list traversal proves inefficient, the operations can be
   * implemented directly against the database.</li>
   * </ul>
   */
  private void addTag(Connection connection, Id userId, List<Id> contentIds, String tagName, TagType tagType, TagVisibility tagVisibility) throws AuthorizationException, TagVisibilityException
  {
    @SuppressWarnings("unchecked")
    Sequence<Association> tags = (Sequence<Association>)repository.retrieveProperty(connection, userId, Keys.TAGS);
    if (tags == null)
    {
      tags = new Sequence<Association>();
    }

    tagName = normalizeTagName(tagName);
    Association association = findTag(tags, tagName, tagVisibility);
    if (association == null)
    {
      association = new Association(IdManager.createIdentifier());
      association.set(Keys.NAME, tagName);
      association.set(Keys.TAG_IS_PRIVATE, tagVisibility == TagVisibility.MY_PRIVATE);
      tags.add(association);
    }
    else
    {
      // Disallow creation of public and private with the same name
      boolean isPrivate = association.get(Keys.TAG_IS_PRIVATE, Boolean.FALSE);
      if (isPrivate != (tagVisibility == TagVisibility.MY_PRIVATE))
      {
        throw new TagVisibilityException(tagName, isPrivate);
      }
    }

    List<Id> taggedEntityIds = association.get(Keys.TAGGED_ENTITY_IDS);
    if (taggedEntityIds == null)
    {
      taggedEntityIds = new LinkedList<Id>();
      association.set(Keys.TAGGED_ENTITY_IDS, taggedEntityIds);
    }

    for (Id contentId : contentIds)
    {
      Id idToBeTagged;
      if (tagType == TagType.AUTHOR)
      {
        idToBeTagged = repository.retrieveEntityCreatedById(connection, contentId);
      }
      else
      {
        idToBeTagged = contentId;
      }
      if (!taggedEntityIds.contains(idToBeTagged))
      {
        if (!canRead(connection, userId, contentId))
        {
          throw new AuthorizationException(contentId);
        }
        taggedEntityIds.add(idToBeTagged);
      }
    }

    repository.updateProperty(connection, userId, Keys.TAGS, tags, new Date(), userId);
  }

  public void addTag(String authenticationToken, List<Id> contentIds, String tagName, TagType tagType, TagVisibility tagVisibility) throws ServerException, AuthenticationException, InvalidContentIdException, AuthorizationException, TagVisibilityException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateWriteAuthentication(connection, authenticationToken);
      Jdbc.setAutoCommit(connection, false);
      addTag(connection, userId, contentIds, tagName, tagType, tagVisibility);
      Jdbc.setAutoCommit(connection, true);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  private Association findTag(List<Association> tags, String tagName, TagVisibility tagVisibility)
  {
    for (Association tag : tags)
    {
      if (tag.get(Keys.NAME, "").equals(tagName))
      {
        return tag;
      }
    }
    return null;
  }

  private String normalizeTagName(String tagName)
  {
    return tagName.substring(0, Math.min(tagName.length(), Repository.MAX_NAME_LENGTH)).toLowerCase();
  }

}
