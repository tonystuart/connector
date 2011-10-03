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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.semanticexpression.connector.server.repository.Jdbc;
import com.semanticexpression.connector.shared.HistoryItem;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.exception.AuthenticationException;
import com.semanticexpression.connector.shared.exception.AuthorizationException;

public class HistoryOperation extends BaseOperation
{
  private static Map<String, String> historyProperties = getHistoryProperties();

  private static HashMap<String, String> getHistoryProperties()
  {
    HashMap<String, String> historyProperties = new HashMap<String, String>();
    historyProperties.put(Keys.CAPTION, "Caption");
    historyProperties.put(Keys.COMMENTS, "Comments");
    historyProperties.put(Keys.IMAGE, "Image");
    historyProperties.put(Keys.PARTS, "Structure");
    historyProperties.put(Keys.PROPERTIES, "Properties");
    historyProperties.put(Keys.STYLES, "Styles");
    historyProperties.put(Keys.STYLE_DEFAULT, "Styles");
    historyProperties.put(Keys.TABLE_COLUMNS, "Table");
    historyProperties.put(Keys.TABLE_ROWS, "Table");
    historyProperties.put(Keys.KEYWORDS, "Keywords");
    historyProperties.put(Keys.TEXT, "Text");
    historyProperties.put(Keys.TITLE, "Title");
    historyProperties.put(Keys.WORKFLOW, "Workflow");
    return historyProperties;
  }

  protected HistoryOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  private String formatCollection(Collection<String> collection)
  {
    List<String> sortedCollection = new LinkedList<String>(collection);
    Collections.sort(sortedCollection);
    StringBuilder s = new StringBuilder();
    for (String item : sortedCollection)
    {
      if (s.length() > 0)
      {
        s.append(", ");
      }
      s.append(item);
    }
    return s.toString();
  }

  private void getEntityHistory(Connection connection, Id entityId, Map<Date, DateSummary> dateSummaries, Set<Id> alreadyProcessed)
  {
    if (alreadyProcessed.contains(entityId))
    {
      return;
    }
    alreadyProcessed.add(entityId);

    StringBuilder s = new StringBuilder();

    s.append("select\n");
    s.append("  p.property_name,\n");
    s.append("  p.valid_from,\n");
    s.append("  e.name,\n");
    s.append("  p.entity_value\n");
    s.append("from property as p, entity as e\n");
    s.append("where entity_id = ?\n");
    s.append("and e.id = p.modified_by\n");
    s.append("order by valid_from desc\n"); // TODO: remove, just for debugging

    PreparedStatement preparedStatement = Jdbc.createPreparedStatement(connection, s);
    try
    {
      ResultSet resultSet = Jdbc.executeQuery(preparedStatement, entityId.getId());
      while (Jdbc.next(resultSet))
      {
        String propertyName = Jdbc.getString(resultSet, 1);
        String historyProperty = historyProperties.get(propertyName);
        if (historyProperty != null)
        {
          Date validFrom = new Date(Jdbc.getLong(resultSet, 2));
          String userName = Jdbc.getString(resultSet, 3);
          DateSummary dateSummary = dateSummaries.get(validFrom);
          if (dateSummary == null)
          {
            dateSummary = new DateSummary();
            dateSummaries.put(validFrom, dateSummary);
          }
          dateSummary.add(historyProperty, userName);
          Log.debug("HistoryOperation.getEntityHistory: entityId=%s, propertyName=%s, validFrom=%s", entityId, historyProperty, validFrom);
        }
        if (propertyName.equals(Keys.DERIVED_FROM))
        {
          // Currently we do not include parent history.
        }
        else
        {
          Id entityValueId = Jdbc.getId(resultSet, 4);
          if (entityValueId != null)
          {
            getEntityHistory(connection, entityValueId, dateSummaries, alreadyProcessed);
          }
        }
      }
    }
    finally
    {
      Jdbc.close(preparedStatement);
    }
  }

  public BasePagingLoadResult<HistoryItem> getHistory(Connection connection, Id contentId, PagingLoadConfig loadConfig)
  {
    Set<Id> alreadyProcessed = new HashSet<Id>();
    Map<Date, DateSummary> dateSummaries = new HashMap<Date, HistoryOperation.DateSummary>();

    getEntityHistory(connection, contentId, dateSummaries, alreadyProcessed);

    List<Date> dates = new LinkedList<Date>(dateSummaries.keySet());
    Collections.sort(dates, new DescendingDateComparator());

    List<HistoryItem> history = new LinkedList<HistoryItem>();

    for (Date date : dates)
    {
      DateSummary dateSummary = dateSummaries.get(date);
      Set<String> propertyNames = dateSummary.getPropertyNames();
      Set<String> userNames = dateSummary.getUserNames();
      HistoryItem historyItem = new HistoryItem();
      historyItem.set(Keys.HISTORY_DATE, date);
      historyItem.set(Keys.HISTORY_PROPERTIES, formatCollection(propertyNames));
      historyItem.set(Keys.HISTORY_USER_NAMES, formatCollection(userNames));
      history.add(historyItem);
    }

    BasePagingLoadResult<HistoryItem> result = new BasePagingLoadResult<HistoryItem>(history);
    return result;
  }

  public BasePagingLoadResult<HistoryItem> getHistory(String authenticationToken, Id contentId, PagingLoadConfig loadConfig) throws AuthenticationException, AuthorizationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      validateReadAccess(connection, authenticationToken, contentId);
      return getHistory(connection, contentId, loadConfig);
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  public class DateSummary
  {
    private Set<String> propertyNames = new HashSet<String>();
    private Set<String> userNames = new HashSet<String>();

    public void add(String propertyName, String userName)
    {
      propertyNames.add(propertyName);
      userNames.add(userName);
    }

    public Set<String> getPropertyNames()
    {
      return propertyNames;
    }

    public Set<String> getUserNames()
    {
      return userNames;
    }

  }

  private final class DescendingDateComparator implements Comparator<Date>
  {
    @Override
    public int compare(Date o1, Date o2)
    {
      return -o1.compareTo(o2);
    }
  }

}