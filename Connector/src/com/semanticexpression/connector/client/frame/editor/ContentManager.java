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

package com.semanticexpression.connector.client.frame.editor;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.events.ContentManagerUpdateEvent;
import com.semanticexpression.connector.client.events.StatusEvent;
import com.semanticexpression.connector.client.rpc.FailureReportingAsyncCallback;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.client.wiring.EventListener;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.DefaultProperties;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.IdManager;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.Properties;
import com.semanticexpression.connector.shared.Sequence;
import com.semanticexpression.connector.shared.Status;
import com.semanticexpression.connector.shared.UpdateStatus;
import com.semanticexpression.connector.shared.enums.ContentType;

public final class ContentManager
{
  private static final Object DEFAULT_STYLE = //
  "table {\n" + //
      "  border-collapse: collapse;\n" + //
      "  margin-left: auto;\n" + //
      "  margin-right: auto;\n" + //
      "}\n\n" + //
      "td, th {\n" + //
      "  border: 1px solid black;\n" + //
      "  text-align: left;\n" + //
      "}\n\n" + //
      "td {\n" + //
      "  padding: 3px 7px 2px 7px;\n" + //
      "}\n\n" + //
      "th {\n" + //
      "  padding: 5px 7px 4px 7px;\n" + //
      "  background-color: #f0f0f0;\n" + //
      "  font-weight: bold;\n" + //
      "}\n\n" + //
      ".se-mc-row-odd td {\n" + //
      "  background-color: #f8f8f8;\n" + //
      "}\n\n" + //
      ".se-mc-table, .se-mc-image, .se-mc-chart {\n" + //
      "  margin-top: 10px;\n" + //
      "  margin-bottom: 10px;\n" + //
      "}\n\n" + //
      ".se-mc-image, .se-mc-chart {\n" + //
      "  text-align: center;\n" + //
      "}\n\n" + //
      ".se-mc-caption {\n" + //
      "  margin-top: 5px;\n" + //
      "  text-align: center;\n" + //
      "  font-size: smaller;\n" + //
      "  font-weight: bold;\n" + //
      "}\n\n" + //
      ".se-mc-path-page {\n" + //
      "  width: 950px;\n" + //
      "  margin: 10px auto;\n" + //
      "  padding: 0px;\n" + //
      "  font-family: sans-serif;\n" + //
      "}\n\n" + //
      ".se-mc-path-container {\n" + //
      "  display: table;\n" + //
      "}\n\n" + //
      ".se-mc-path-navigation {\n" + //
      "  width: 300px;\n" + //
      "  display: table-cell;\n" + //
      "  font-size: 80%;\n" + //
	  "  line-height: 200%;\n" + //
      "}\n\n" + //
      ".se-mc-path-navigation ul {\n" + //
      "  list-style-type: none;\n" + //
      "  padding-left: 0px;\n" + //
      "  margin-left: 20px;\n" + //
      "}\n\n" + //
      ".se-mc-path-navigation a:link {\n" + //
      "  color: blue;\n" + //
      "}\n\n" + //
      ".se-mc-path-navigation a:visited {\n" + //
      "  color: blue;\n" + //
      "}\n\n" + //
      ".se-mc-path-content {\n" + //
      "  width: 650px;\n" + //
      "  display: table-cell;\n" + //
      "  font-size: 80%;\n" + //
      "}\n\n" + //
      ".se-mc-path-current {\n" + //
      "  font-weight: bold;\n" + //
      "}\n\n" + //
      ".se-mc-path-header {\n" + //
      "}\n\n" + //
      ".se-mc-path-footer {\n" + //
      "}\n\n" + //
      ".se-mc-path-top {\n" + //
        "display: none;\n" + //
        "width: 650px;\n" + //
      "}\n\n" + //
      ".se-mc-path-bottom {\n" + //
        "display: table;\n" + //
        "width: 650px;\n" + //
        "margin-top: 10px;\n" + //
      "}\n\n" + //
      ".se-mc-path-previous {\n" + //
        "display: table-cell;\n" + //
        "text-align: left;\n" + //
      "}\n\n" + //
      ".se-mc-path-next {\n" + //
        "display: table-cell;\n" + //
        "text-align: right;\n" + //
      "}\n\n";
      
  public static final String DS_BUSINESS = "Business";
  public static final String DS_COMMENT = "Comment";
  public static final String DS_DELETED = "Deleted";
  public static final String DS_DETAIL = "Detail";
  public static final String DS_EMPHASIS = "Emphasis";
  public static final String DS_HYPERTEXT_LINK = "Hypertext Link";
  public static final String DS_IMPORTANT = "Important";
  public static final String DS_INSERTED = "Inserted";
  public static final String DS_LIST_ITEM = "List Item";
  public static final String DS_ORDERED_LIST = "Ordered List";
  public static final String DS_PARAGRAPH = "Paragraph";
  public static final String DS_SOFTWARE_BLOCK = "Software Block";
  public static final String DS_TECHNICAL = "Technical";
  public static final String DS_TEXT_PASSAGE = "Text Passage";
  public static final String DS_UNORDERED_LIST = "Unordered List";

  private Map<Id, Content> contents = new HashMap<Id, Content>();
  private Map<Id, Integer> referenceCounts = new HashMap<Id, Integer>();

  public ContentManager()
  {
    Directory.getEventBus().addListener(StatusEvent.class, new StatusEventListener());
  }

  public Content createContent(ContentType contentType)
  {
    Content content = new Content(IdManager.createIdentifier());
    content.setTrackChanges(true);
    content.set(Keys.CONTENT_TYPE, contentType);
    content.set(Keys.TITLE, contentType.toString());

    createContentSpecificProperties(contentType, content);

    return content;
  }

  private ContentReference createContentReference(Content content)
  {
    Id id = content.getId();
    Integer count = referenceCounts.get(id);
    count = count == null ? Integer.valueOf(1) : count + 1;
    referenceCounts.put(id, count);

    System.out.println("ContentManager.createContentReference: id=" + id + ", count=" + count);
    ContentReference contentReference = new ContentReference(content);
    return contentReference;
  }

  private void createContentSpecificProperties(ContentType contentType, Content content)
  {
    Properties properties = new Properties(true);

    switch (contentType)
    {
      case CHART:
      case DOCUMENT:
      case IMAGE:
      case TABLE:
      case TEXT:
        properties.set(DefaultProperties.INCLUDE_TITLE_AS_CONTENT, true);
        break;

      case WORKFLOW:
        properties.set(DefaultProperties.NOTIFY_ASSIGNEE_ON_READY, true);
        properties.set(DefaultProperties.NOTIFY_OWNER_ON_COMPLETION, true);
        properties.set(DefaultProperties.NOTIFY_OWNER_ON_READY, true);
        properties.set(DefaultProperties.NOTIFY_OWNER_ON_REJECTION, true);
        break;
    }

    if (properties.size() > 0)
    {
      content.set(Keys.PROPERTIES, properties.getProperties());
    }
  }

  public List<Content> createDocument()
  {
    Content document = createContent(ContentType.DOCUMENT);
    Content style = createContent(ContentType.STYLE);
    Content text = createContent(ContentType.TEXT);
    List<Id> partsList = getPartsList(style, text);
    document.set(Keys.PARTS, partsList);
    initializeStyleContent(style);
    return getContentList(document, style, text);
  }

  public void initializeStyleContent(Content style)
  {
    style.set(Keys.STYLES, createNamedStyles());
    style.set(Keys.STYLE_DEFAULT, DEFAULT_STYLE);
  }

  private Association createNamedStyle(String styleName, String styleElementName, String styleValue, boolean isCommentEnabled)
  {
    String styleClassName = Utility.createStyleClassName(styleName);
    String styleSelector = Utility.createStyleSelector(styleClassName);

    Association style = new Association(IdManager.createIdentifier());
    style.set(Keys.NAME, styleName);
    style.set(Keys.VALUE, styleValue);
    style.set(Keys.STYLE_SELECTOR, styleSelector);
    style.set(Keys.STYLE_ELEMENT_NAME, styleElementName);
    style.set(Keys.STYLE_IS_COMMENT_ENABLED, isCommentEnabled);

    return style;
  }

  private Sequence<Association> createNamedStyles()
  {
    Sequence<Association> defaultStyles = new Sequence<Association>();
    defaultStyles.add(createNamedStyle(DS_COMMENT, "span", "background-color: yellow;\n", true));
    defaultStyles.add(createNamedStyle(DS_IMPORTANT, "span", "font-weight: bold;\n", false));
    defaultStyles.add(createNamedStyle(DS_EMPHASIS, "span", "font-style: italic;\n", false));
    defaultStyles.add(createNamedStyle(DS_BUSINESS, "div", "color: default;\n", false));
    defaultStyles.add(createNamedStyle(DS_TECHNICAL, "div", "color: default;\n", false));
    defaultStyles.add(createNamedStyle(DS_PARAGRAPH, "div", "margin-bottom: 1em;\n", false));
    defaultStyles.add(createNamedStyle(DS_DETAIL, "span", "color: default;\n", false));
    defaultStyles.add(createNamedStyle(DS_LIST_ITEM, "li", "font-size: 90%;", false));
    defaultStyles.add(createNamedStyle(DS_ORDERED_LIST, "ol", "color: default;", false));
    defaultStyles.add(createNamedStyle(DS_UNORDERED_LIST, "ul", "color: default;", false));
    defaultStyles.add(createNamedStyle(DS_HYPERTEXT_LINK, "a", "color: default;", false));
    defaultStyles.add(createNamedStyle(DS_INSERTED, "span", "color: #00c000;\ntext-decoration: underline;\n", false));
    defaultStyles.add(createNamedStyle(DS_DELETED, "span", "color: red;\ntext-decoration: line-through;\nfont-weight: normal !important;\nfont-size: medium !important;\n", false));
    defaultStyles.add(createNamedStyle(DS_TEXT_PASSAGE, "div", "margin: 20px;\nfont-size: 80%;\nfont-family: serif;\n", false));
    defaultStyles.add(createNamedStyle(DS_SOFTWARE_BLOCK, "div", "margin: 20px;\nfont-size: 80%;\nfont-family: monospace;\n", false));
    return defaultStyles;
  }

  private LinkedList<Content> getContentList(Content... contents)
  {
    LinkedList<Content> partsList = new LinkedList<Content>();
    for (Content content : contents)
    {
      partsList.add(content);
    }
    return partsList;
  }

  private List<Id> getPartsList(Content... contents)
  {
    List<Id> partsList = new LinkedList<Id>();
    for (Content content : contents)
    {
      partsList.add(content.getId());
    }
    return partsList;
  }

  public int getReferenceCount(Id id)
  {
    Integer count = referenceCounts.get(id);
    return count == null ? 0 : count;
  }

  public void processStatusList(List<Status> statusList)
  {
    for (Status status : statusList)
    {
      if (status instanceof UpdateStatus)
      {
        processUpdateStatus((UpdateStatus)status);
      }
    }
  }

  private void processUpdatedContent(List<Content> contentList)
  {
    for (Content newContent : contentList)
    {
      Id contentId = newContent.getId();
      Content oldContent = contents.get(contentId);
      if (oldContent != null) // all references may have been unregistered since we requested it
      {
        boolean isTrackChanges = oldContent.isTrackChanges();
        newContent.setTrackChanges(isTrackChanges);
        contents.put(contentId, newContent);
      }
    }
    Directory.getEventBus().post(new ContentManagerUpdateEvent(contentList));
  }

  private void processUpdateStatus(UpdateStatus updateStatus)
  {
    Id updatedContentId = updateStatus.getContentId();
    for (Id contentId : contents.keySet())
    {
      if (contentId.equals(updatedContentId))
      {
        retrieveContent(contentId, null, true);
      }
    }
  }

  public synchronized ContentReference register(Content newContent)
  {
    Id id = newContent.getId();
    Content content = contents.get(id);
    if (content == null)
    {
      content = newContent;
      contents.put(id, content);
      content.setTrackChanges(true);
    }
    ContentReference contentReference = createContentReference(content);
    return contentReference;
  }

  public synchronized ContentReference register(Id id)
  {
    ContentReference contentReference = null;
    Content content = contents.get(id);
    if (content != null)
    {
      contentReference = createContentReference(content);
    }
    return contentReference;
  }

  public void replace(ContentReference contentReference, Content content)
  {
    Id id = contentReference.getId();

    if (!contents.containsKey(id))
    {
      throw new IllegalStateException("Attempt to replace unregistered content");
    }

    if (referenceCounts.get(id) > 1)
    {
      MessageBox.alert("Replace", "Currently, you cannot replace content that is open in multiple windows. To replace this content, close the other windows and try again.", null);
      return;
    }

    boolean isReadOnly = contentReference.getBaseContent().isReadOnly();
    boolean isTrackChanges = !isReadOnly;
    Content newBaseContent = content.copy(isTrackChanges);
    if (isReadOnly)
    {
      newBaseContent.setReadOnly(isReadOnly);
    }

    newBaseContent.setId(id);
    contents.put(id, newBaseContent);
    contentReference.setBaseContent(newBaseContent);
  }

  public void retrieveContent(Id contentId, Date presentAt, boolean isDeep)
  {
    Directory.getConnectorService().retrieveContent(Utility.getAuthenticationToken(), contentId, presentAt, isDeep, new RetrieveUpdatedContentCallback());
  }

  public synchronized void unregister(ContentReference contentReference)
  {
    Id id = contentReference.getId();
    Integer referenceCount = referenceCounts.get(id);
    System.out.println("ContentManager.unregister: id=" + contentReference.getId() + ", count=" + referenceCount);
    if (referenceCount != null)
    {
      referenceCount = referenceCount - 1;
      if (referenceCount == 0)
      {
        referenceCounts.remove(id);
        contents.remove(id);
      }
      else
      {
        referenceCounts.put(id, referenceCount);
      }
    }

    contentReference.unregister();
  }

  private final class RetrieveUpdatedContentCallback extends FailureReportingAsyncCallback<List<Content>>
  {
    @Override
    public void onSuccess(List<Content> contentList)
    {
      processUpdatedContent(contentList);
    }

  }

  public final class StatusEventListener implements EventListener<StatusEvent>
  {
    @Override
    public void onEventNotification(StatusEvent statusEvent)
    {
      processStatusList(statusEvent.getStatusList());
    }
  }

}
