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

import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.util.Version;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.SearchRequest;
import com.semanticexpression.connector.shared.SearchResult;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.TagConstants.TagVisibility;
import com.semanticexpression.connector.shared.TagFilter;
import com.semanticexpression.connector.shared.enums.ContentType;
import com.semanticexpression.connector.shared.exception.AuthenticationException;

public class SearchOperation extends BaseOperation
{
  private static final int MAXIMUM_SUMMARY_LENGTH = 1000;
  private static final int MINIMUM_SUMMARY_LENGTH = 30;

  public SearchOperation(ServerContext serverContext)
  {
    super(serverContext);
  }

  private List<SearchResult> createSearchResults(Connection connection, Id userId, SearchRequest searchRequest, SearchCollector collector)
  {
    List<Hit> hitList = collector.getHitList();
    List<SearchResult> searchResults = new LinkedList<SearchResult>();
    String searchTerms = searchRequest.getTerms();
    String[] searchFields = getSearchFields(searchRequest);

    SearchOperation.SearchResultHighlighter searchResultHighlighter = new SearchResultHighlighter(searchTerms, searchFields);

    for (Hit hit : hitList)
    {
      Id contentId = hit.getContentId();
      Id searchResultId = hit.getContainingDocumentId();
      if (searchResultId == null)
      {
        searchResultId = contentId;
      }

      SearchResult searchResult = new SearchResult(searchResultId);
      searchResults.add(searchResult);

      Object title = repository.retrieveProperty(connection, searchResultId, Keys.TITLE);
      Object contentType = repository.retrieveProperty(connection, searchResultId, Keys.CONTENT_TYPE);
      Id createdById = repository.retrieveEntityCreatedById(connection, searchResultId);
      String createdByName = repository.retrieveEntityCreatedByName(connection, searchResultId);
      Date modifiedAtDate = repository.retrieveEntityModifiedAtDate(connection, searchResultId);
      String summary = getSummary(connection, contentId, searchResultHighlighter, searchResultId);

      searchResult.set(Keys.TITLE, title);
      searchResult.set(Keys.CONTENT_TYPE, contentType);
      searchResult.set(Keys.CREATED_BY, createdByName);
      searchResult.set(Keys.MODIFIED_AT, modifiedAtDate);
      searchResult.set(Keys.SUMMARY, summary);
      searchResult.set(Keys.SCORE, hit.getScore());

      boolean isMyPrivateTags = searchRequest.isMyPrivateTags();
      boolean isMyPublicTags = searchRequest.isMyPublicTags();
      boolean isOtherPublicTags = searchRequest.isOtherPublicTags();

      if (searchRequest.isContentTags())
      {
        Sequence<Association> tagListContent = repository.getTags(connection, userId, searchResultId, isMyPrivateTags, isMyPublicTags, isOtherPublicTags);
        searchResult.set(Keys.TAG_LIST_CONTENT, tagListContent);
      }

      if (searchRequest.isAuthorTags())
      {
        Sequence<Association> tagListAuthor = repository.getTags(connection, userId, createdById, isMyPrivateTags, isMyPublicTags, isOtherPublicTags);
        searchResult.set(Keys.TAG_LIST_AUTHOR, tagListAuthor);
      }

      if (searchRequest.isSemanticTags())
      {
        Sequence<Association> semanticTags = getSemanticTags(connection, contentId);
        searchResult.set(Keys.SEMANTIC_TAGS, semanticTags);
      }
    }

    return searchResults;
  }

  public ContentType getContentType(Connection connection, Id contentId)
  {
    return (ContentType)retrieveProperty(connection, contentId, Keys.CONTENT_TYPE);
  }

  private String getDefaultSummary(Connection connection, Id contentId)
  {
    StringAppender s = new StringAppender(" &hellip; ", MAXIMUM_SUMMARY_LENGTH);
    getDefaultSummary(connection, contentId, s);
    return s.toString();
  }

  private void getDefaultSummary(Connection connection, Id contentId, StringAppender s)
  {
    String title = (String)retrieveProperty(connection, contentId, Keys.TITLE);
    if (title != null)
    {
      s.append(normalizeText(title));
    }
    String text = (String)retrieveProperty(connection, contentId, Keys.TEXT);
    if (text != null)
    {
      s.append(normalizeText(text));
    }
    if (s.isFull())
    {
      return;
    }
    @SuppressWarnings("unchecked")
    List<Id> parts = (List<Id>)retrieveProperty(connection, contentId, Keys.PARTS);
    if (parts != null)
    {
      for (Id part : parts)
      {
        getDefaultSummary(connection, part, s);
      }
    }
  }

  private String[] getSearchFields(SearchRequest searchRequest)
  {
    List<String> searchFields = new LinkedList<String>();
    if (searchRequest.isTitle())
    {
      searchFields.add(Keys.TITLE);
    }
    if (searchRequest.isContent())
    {
      searchFields.add(Keys.TEXT);
    }
    if (searchRequest.isCaption())
    {
      searchFields.add(Keys.CAPTION);
    }
    return searchFields.toArray(new String[searchFields.size()]);
  }

  private Sequence<Association> getSemanticTags(Connection connection, Id contentId)
  {
    Sequence<Association> searchResultSemanticTags = new Sequence<Association>();
    List<?> semanticTags = (List<?>)repository.retrieveProperty(connection, contentId, Keys.SEMANTIC_TAGS);
    if (semanticTags != null)
    {
      for (Object semanticTag : semanticTags)
      {
        Association association = new Association();
        association.set(Keys.NAME, semanticTag);
        association.set(Keys.TAG_VISIBILITY, TagVisibility.OTHER_PUBLIC);
        searchResultSemanticTags.add(association);
      }
    }
    return searchResultSemanticTags;
  }

  private String getSummary(Connection connection, Id contentId, SearchOperation.SearchResultHighlighter searchResultHighlighter, Id searchResultId)
  {
    String summary = searchResultHighlighter.getHighlight(connection, contentId, this); // High relevance: search term(s) occur in hit
    if (summary.length() < MINIMUM_SUMMARY_LENGTH)
    {
      String defaultSummary = getDefaultSummary(connection, searchResultId);
      if (defaultSummary.length() > summary.length())
      {
        String defaultSummaryHighlight = searchResultHighlighter.getSummaryHighlight(defaultSummary);
        if (defaultSummaryHighlight.length() > MINIMUM_SUMMARY_LENGTH)
        {
          summary = defaultSummaryHighlight; // Medium relevance: search term(s) occur in summarized contained content
        }
        else
        {
          summary = defaultSummary; // Low relevance: search terms do not occur in summarized contained content (but do occur somewhere)
        }
      }
    }
    return summary;
  }

  private boolean inRange(Date fromDate, Date subjectDate, Date toDate)
  {
    return subjectDate == null ? false : (fromDate.compareTo(subjectDate) <= 0 && subjectDate.compareTo(toDate) <= 0);
  }

  private boolean matchesContentType(Connection connection, Id contentId, SearchRequest searchRequest)
  {
    ContentType contentType = getContentType(connection, contentId);
    boolean isMatchingContentType = false;
    if (contentType != null)
    {
      switch (contentType)
      {
        case CHART:
          isMatchingContentType = searchRequest.isChart();
          break;
        case DOCUMENT:
          isMatchingContentType = searchRequest.isDocument();
          break;
        case IMAGE:
          isMatchingContentType = searchRequest.isImage();
          break;
        case STYLE:
          isMatchingContentType = searchRequest.isStyle();
          break;
        case TABLE:
          isMatchingContentType = searchRequest.isTable();
          break;
        case TEXT:
          isMatchingContentType = searchRequest.isText();
          break;
        case WORKFLOW:
          isMatchingContentType = searchRequest.isWorkflow();
          break;
        default:
          throw new IllegalArgumentException("contentType=" + contentType);
      }
    }
    return isMatchingContentType;
  }

  public boolean matchesCriteria(Connection connection, Id userId, Id contentId, SearchRequest searchRequest)
  {
    boolean matchesCriteria = true;
    if (!matchesContentType(connection, contentId, searchRequest))
    {
      matchesCriteria = false;
    }
    else if (!matchesDates(connection, contentId, searchRequest))
    {
      matchesCriteria = false;
    }
    else if (!matchesPublishStatus(connection, contentId, searchRequest))
    {
      matchesCriteria = false;
    }
    else if (!matchesTags(connection, userId, contentId, searchRequest))
    {
      matchesCriteria = false;
    }
    return matchesCriteria;
  }

  private boolean matchesDates(Connection connection, Id contentId, SearchRequest searchRequest)
  {
    boolean match = false;
    Date fromDate = searchRequest.getFromDate();
    Date toDate = searchRequest.getToDate();
    if (fromDate == null && toDate == null)
    {
      match = true;
    }
    else
    {
      if (fromDate == null)
      {
        fromDate = new Date(0);
      }
      else if (toDate == null)
      {
        toDate = new Date();
      }
      if (searchRequest.isDateCreated() && inRange(fromDate, repository.retrieveEntityCreatedAtDate(connection, contentId), toDate))
      {
        match = true;
      }
      else if (searchRequest.isDateModified() && inRange(fromDate, repository.retrieveEntityModifiedAtDate(connection, contentId), toDate))
      {
        match = true;
      }
      else if (searchRequest.isDatePublished() && inRange(fromDate, repository.retrieveEntityPublishedAtDate(connection, contentId), toDate))
      {
        match = true;
      }
      else if (searchRequest.isDateViewed() && inRange(fromDate, repository.retrieveEntityRetrievedAtDate(), toDate))
      {
        match = true;
      }
    }
    return match;
  }

  private boolean matchesPublishStatus(Connection connection, Id contentId, SearchRequest searchRequest)
  {
    boolean matchesPublishStatus = false;
    boolean isPublished = searchRequest.isPublished();
    boolean isUnpublished = searchRequest.isUnpublished();
    if (isPublished && isUnpublished)
    {
      matchesPublishStatus = true;
    }
    else if (isPublished && !isUnpublished)
    {
      matchesPublishStatus = repository.retrieveEntityPublishedAtDate(connection, contentId) != null;
    }
    else if (!isPublished && isUnpublished)
    {
      matchesPublishStatus = repository.retrieveEntityPublishedAtDate(connection, contentId) == null;
    }
    return matchesPublishStatus;
  }

  private boolean matchesTagFilter(TagFilter tagFilter, Sequence<Association> tags)
  {
    String tagFilterName = tagFilter.getName();
    for (Association tag : tags)
    {
      String tagName = tag.get(Keys.NAME);
      if (tagFilterName.equals(tagName))
      {
        boolean isMatch = false;
        TagVisibility tagVisibility = tag.get(Keys.TAG_VISIBILITY);
        switch (tagVisibility)
        {
          case MY_PRIVATE:
            isMatch = tagFilter.isMyPrivate();
            break;
          case MY_PUBLIC:
            isMatch = tagFilter.isMyPublic();
            break;
          case OTHER_PUBLIC:
            isMatch = tagFilter.isOtherPublic();
            break;
        }
        if (isMatch)
        {
          return true;
        }
        // else keep trying
      }
    }
    return false;
  }

  private boolean matchesTags(Connection connection, Id userId, Id contentId, SearchRequest searchRequest)
  {
    Sequence<Association> authorTags = null;
    Sequence<Association> contentTags = null;
    Sequence<Association> semanticTags = null;

    List<TagFilter> tagFilters = searchRequest.getTagFilters();
    if (tagFilters != null)
    {
      for (TagFilter tagFilter : tagFilters)
      {
        if (tagFilter.isAuthor())
        {
          if (authorTags == null)
          {
            Id authorId = repository.retrieveEntityCreatedById(connection, contentId);
            authorTags = repository.getTags(connection, userId, authorId, true, true, true);
          }
          if (matchesTagFilter(tagFilter, authorTags))
          {
            return tagFilter.isInclude();
          }
        }
        if (tagFilter.isContent())
        {
          if (contentTags == null)
          {
            contentTags = repository.getTags(connection, userId, contentId, true, true, true);
          }
          if (matchesTagFilter(tagFilter, contentTags))
          {
            return tagFilter.isInclude();
          }
        }
        if (tagFilter.isSemantic())
        {
          if (semanticTags == null)
          {
            semanticTags = getSemanticTags(connection, contentId);
          }
          if (matchesTagFilter(tagFilter, semanticTags))
          {
            return tagFilter.isInclude();
          }
        }
      }
      return false;
    }
    return true;
  }

  public BasePagingLoadResult<SearchResult> search(String authenticationToken, SearchRequest searchRequest, PagingLoadConfig pagingLoadConfig) throws AuthenticationException
  {
    Connection connection = repository.getConnectionPool().getConnection();
    try
    {
      Id userId = validateReadAuthentication(connection, authenticationToken);
      int offset = pagingLoadConfig.getOffset();
      int limit = pagingLoadConfig.getLimit();

      SearchCollector collector = new SearchCollector(connection, userId, offset, limit, searchRequest);
      String searchTerms = searchRequest.getTerms();
      String[] searchFields = getSearchFields(searchRequest);

      searchEngine.search(collector, searchTerms, searchFields);

      List<SearchResult> searchResults = createSearchResults(connection, userId, searchRequest, collector);

      BasePagingLoadResult<SearchResult> basePagingLoadResult = new BasePagingLoadResult<SearchResult>(searchResults, offset, collector.getTotalCount());
      return basePagingLoadResult;
    }
    finally
    {
      repository.getConnectionPool().putConnection(connection);
    }
  }

  public class Hit implements Comparable<Hit>
  {
    private Id containingDocumentId;
    private Id contentId;
    private int doc;
    private double score;

    public Hit(int doc, Id contentId, Id containingDocumentId, double score)
    {
      this.contentId = contentId;
      this.containingDocumentId = containingDocumentId;
      this.score = score;
    }

    public void addScore(double score)
    {
      this.score += score;
    }

    @Override
    public int compareTo(Hit that)
    {
      if (this.score < that.score)
      {
        return -1;
      }
      if (this.score > that.score)
      {
        return 1;
      }
      if (this.doc < that.doc)
      {
        return -1;
      }
      if (this.doc > that.doc)
      {
        return 1;
      }
      return 0;
    }

    public Id getContainingDocumentId()
    {
      return containingDocumentId;
    }

    public Id getContentId()
    {
      return contentId;
    }

    public double getScore()
    {
      return score;
    }

    public boolean isContainingDocumentOnly()
    {
      return contentId.equals(containingDocumentId);
    }

    public void setContent(Id contentId)
    {
      this.contentId = contentId;
    }

  }

  private class HitCollector
  {
    private Map<Id, Hit> documentHits;
    private int maximumHitCount;
    private int offset;
    private PriorityQueue<Hit> priorityQueue = new PriorityQueue<Hit>();
    private int totalCount;

    public HitCollector(int offset, int limit, boolean isContainingDocumentsOnly)
    {
      this.offset = offset;
      this.maximumHitCount = offset + limit;
      if (isContainingDocumentsOnly)
      {
        this.documentHits = new HashMap<Id, Hit>();
      }
    }

    private Hit add(int doc, Id contentId, Id containingDocumentId, double score)
    {
      totalCount++;
      Hit hit = new Hit(doc, contentId, containingDocumentId, score);
      if (priorityQueue.size() < maximumHitCount || score > priorityQueue.peek().getScore())
      {
        priorityQueue.add(hit);
        while (priorityQueue.size() > maximumHitCount)
        {
          priorityQueue.remove();
        }
      }
      return hit;
    }

    private void addDocument(int doc, Id contentId, Id containingDocumentId, double score)
    {
      Hit hit = documentHits.get(containingDocumentId);
      if (hit == null)
      {
        hit = add(doc, contentId, containingDocumentId, score);
        documentHits.put(containingDocumentId, hit);
      }
      else
      {
        hit.addScore(score); // the score of the containing document is the sum of the score of the contents
        if (hit.isContainingDocumentOnly())
        {
          hit.setContent(contentId); // enrich existing document-only hit by adding contained content
        }
      }
    }

    public void collect(Connection connection, int doc, Id contentId, double score)
    {
      if (documentHits == null)
      {
        add(doc, contentId, null, score);
      }
      else
      {
        collectContainingDocument(connection, doc, contentId, score);
      }
    }

    private void collectContainingDocument(Connection connection, int doc, Id contentId, double score)
    {
      List<Id> containingDocumentIds = repository.getContainingDocumentsRoot(connection, contentId);
      for (Id containingDocumentId : containingDocumentIds)
      {
        addDocument(doc, contentId, containingDocumentId, score);
      }
    }

    private List<Hit> getHitList()
    {
      List<Hit> hitList = new LinkedList<Hit>();
      if (priorityQueue.size() >= offset)
      {
        int count = 0;
        while (priorityQueue.size() > 0)
        {
          Hit hit = priorityQueue.remove();
          if (count >= offset)
          {
            hitList.add(hit);
          }
          count++;
        }
      }
      return hitList;
    }

    public int getTotalCount()
    {
      return totalCount;
    }

  }

  public class SearchCollector extends Collector
  {
    private Connection connection;
    private int docBase;
    private HitCollector hitCollector;
    private Scorer scorer;
    private Searcher searcher;
    private SearchRequest searchRequest;
    private Id userId;

    public SearchCollector(Connection connection, Id userId, int offset, int limit, SearchRequest searchRequest)
    {
      this.connection = connection;
      this.userId = userId;
      this.searchRequest = searchRequest;
      this.hitCollector = new HitCollector(offset, limit, searchRequest.isContainingDocuments());
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
      return true;
    }

    @Override
    public void collect(int doc) throws IOException
    {
      Document document = searcher.doc(docBase + doc);
      String contentIdString = document.get(Keys.LUCENE_ID);
      Id contentId = new Id(contentIdString);

      if (canRead(connection, userId, contentId) && matchesCriteria(connection, userId, contentId, searchRequest))
      {
        hitCollector.collect(connection, doc, contentId, getScore());
      }
    }

    public List<Hit> getHitList()
    {
      List<Hit> hitList = hitCollector.getHitList();
      return hitList;
    }

    private double getScore()
    {
      try
      {
        return scorer.score();
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    public int getTotalCount()
    {
      return hitCollector.getTotalCount();
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException
    {
      this.docBase = docBase;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException
    {
      this.scorer = scorer;
    }

    public void setSearcher(Searcher searcher)
    {
      this.searcher = searcher;
    }

  }

  public class SearchResultHighlighter
  {
    private String[] searchFields;
    private SearchOperation.SearchTermHighlighter[] searchTermHighlighters;

    public SearchResultHighlighter(String searchTerms, String[] searchFields)
    {
      this.searchTermHighlighters = new SearchOperation.SearchTermHighlighter[searchFields.length];
      this.searchFields = searchFields;

      for (int i = 0; i < searchFields.length; i++)
      {
        String searchField = searchFields[i];
        SearchOperation.SearchTermHighlighter searchTermHighlighter = new SearchTermHighlighter(searchField);
        searchTermHighlighter.parse(searchTerms);
        searchTermHighlighters[i] = searchTermHighlighter;
      }
    }

    public String getHighlight(Connection connection, Id contentId, SearchOperation searchOperation)
    {
      StringAppender s = new StringAppender(" &hellip; ", MAXIMUM_SUMMARY_LENGTH);

      for (int i = 0; i < searchFields.length; i++)
      {
        String searchField = searchFields[i];
        String searchText = (String)searchOperation.retrieveProperty(connection, contentId, searchField);
        if (searchText != null)
        {
          String summary = searchTermHighlighters[i].getHighlight(searchText);
          if (summary != null && summary.length() > 0)
          {
            s.append(summary);
          }
        }
      }

      return s.toString();
    }

    public String getSummaryHighlight(String searchText)
    {
      StringAppender s = new StringAppender(" &hellip; ", MAXIMUM_SUMMARY_LENGTH);

      for (int i = 0; i < searchFields.length; i++)
      {
        if (searchText != null)
        {
          String summary = searchTermHighlighters[i].getHighlight(searchText);
          if (summary != null && summary.length() > 0)
          {
            s.append(summary);
          }
        }
      }

      return s.toString();
    }
  }

  public class SearchTermHighlighter
  {
    private Highlighter highlighter;
    private String searchField;

    public SearchTermHighlighter(String searchField)
    {
      this.searchField = searchField;
    }

    public String[] getBestFragments(String text, int maximumFragments)
    {
      try
      {
        return highlighter.getBestFragments(searchEngine.getAnalyzer(), searchField, text, maximumFragments);
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

    public String getHighlight(String text)
    {
      StringAppender s = new StringAppender(" &hellip; ");
      String normalizedText = normalizeText(text);
      String[] fragments = getBestFragments(normalizedText, 5);
      if (fragments != null)
      {
        for (String fragment : fragments)
        {
          s.append(fragment);
        }
      }
      return s.toString();
    }

    public void parse(String searchTerms)
    {
      try
      {
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, searchField, searchEngine.getAnalyzer());
        Query query = queryParser.parse(searchTerms);

        // TODO: Use query.rewrite per LuceneFAQ

        QueryScorer scorer = new QueryScorer(query, searchEngine.getIndexReader(), searchField);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);
        highlighter = new Highlighter(scorer);
        highlighter.setTextFragmenter(fragmenter);
      }
      catch (ParseException e)
      {
        throw new RuntimeException(e);
      }
    }

  }
}
