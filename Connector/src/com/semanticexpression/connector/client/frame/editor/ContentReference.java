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

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.semanticexpression.connector.shared.Content;
import com.semanticexpression.connector.shared.Id;

/**
 * ContentReference is the mechanism for sharing Content within and across
 * EditorFrames. It enables Content to be shared across multiple TreePanels. It
 * also handles temporary history views (formerly called snapshots, but changed
 * so as to not cause confusion with Association snapshots).
 */

public final class ContentReference extends BaseModel
{
  private Content baseContent;
  private Content content;
  private ChangeListener contentChangeListener;
  private LinkedList<ChangeListener> contentReferenceChangeListeners = new LinkedList<ChangeListener>();
  private Date historyDate;

  public ContentReference(Content content)
  {
    this.content = content;
    this.baseContent = content;
  }

  public void addChangeListener(ChangeListener... listeners)
  {
    prepareContentChangeListener();
    for (ChangeListener listener : listeners)
    {
      contentReferenceChangeListeners.add(listener);
    }
  }

  public void addChangeListener(List<ChangeListener> listeners)
  {
    prepareContentChangeListener();
    for (ChangeListener listener : listeners)
    {
      contentReferenceChangeListeners.add(listener);
    }
  }

  private void cleanupContentChangeListener()
  {
    if (contentReferenceChangeListeners.size() == 0)
    {
      removeContentChangeListener();
    }
  }

  public void clearHistory()
  {
    setHistory(null, null);
  }

  public void commit()
  {
    content.commit();
  }

  public Content copy(boolean isTrackChanges)
  {
    return content.copy(isTrackChanges);
  }

  /**
   * In order to add multiple ContentReferences that point to the same
   * underlying Content to a TreePanel, one must not delegate equals and
   * hashCode to the underlying Content.
   * <p/>
   * Otherwise a range of problems occurs, including ModelData doesn't fire a
   * change event if the new and old value are equal(), meaning you can't add an
   * item to the tree.
   * <p/>
   * See OutlineTreePanel.copy() and treeStoreModel.set("model",
   * newContentReference)
   */
  public boolean equals(Object obj)
  {
    return super.equals(obj);
  }

  public <X> X get(String name)
  {
    return content.get(name);
  }

  public <X> X get(String property, X valueWhenNull)
  {
    return content.get(property, valueWhenNull);
  }

  public Content getBaseContent()
  {
    return baseContent;
  }

  public Content getChanges()
  {
    return baseContent.getChanges(); // e.g. save window, history item selected, changes to base content
  }

  public Date getHistoryDate()
  {
    return historyDate;
  }

  public Id getId()
  {
    return content.getId();
  }

  public Map<String, Object> getProperties()
  {
    return content.getProperties();
  }

  public Collection<String> getPropertyNames()
  {
    return content.getPropertyNames();
  }

  public int hashCode()
  {
    return super.hashCode();
  }

  public boolean isAllowNestedValues()
  {
    return content.isAllowNestedValues();
  }

  public boolean isChanged()
  {
    return baseContent.isChanged(); // e.g. close window, history item selected, changes to base content
  }

  public boolean isReadOnly()
  {
    return content.isReadOnly();
  }

  public boolean isSilent()
  {
    return content.isSilent();
  }

  public boolean isTrackChanges()
  {
    return content.isTrackChanges();
  }

  public void notify(ChangeEvent evt)
  {
    content.notify(evt);
  }

  private void prepareContentChangeListener()
  {
    if (contentChangeListener == null)
    {
      contentChangeListener = new ContentChangeListener();
      content.addChangeListener(contentChangeListener);
    }
  }

  public <X> X remove(String name)
  {
    return content.remove(name);
  }

  public void removeChangeListener(ChangeListener... listeners)
  {
    for (ChangeListener listener : listeners)
    {
      contentReferenceChangeListeners.remove(listener);
    }
    cleanupContentChangeListener();
  }

  public void removeChangeListeners()
  {
    contentReferenceChangeListeners.clear();
    cleanupContentChangeListener();
  }

  private void removeContentChangeListener()
  {
    content.removeChangeListener(contentChangeListener);
    contentChangeListener = null;
  }

  private void replaceContentChangeListener(Content newBaseContent)
  {
    if (contentChangeListener != null)
    {
      content.removeChangeListener(contentChangeListener);
      newBaseContent.addChangeListener(contentChangeListener);
    }
  }

  public void rollback()
  {
    content.rollback();
  }

  public <X> X set(String name, X newValue)
  {
    return content.set(name, newValue);
  }

  public void setAllowNestedValues(boolean allowNestedValues)
  {
    content.setAllowNestedValues(allowNestedValues);
  }

  public void setBaseContent(Content content)
  {
    replaceContentChangeListener(content);

    this.content = content;
    this.baseContent = content;
  }

  public void setHistory(Date historyDate, Content historyContent)
  {
    if (historyDate == null)
    {
      historyContent = baseContent;
    }
    this.historyDate = historyDate;
    this.content = historyContent;
  }

  public void setProperties(Map<String, Object> properties)
  {
    content.setProperties(properties);
  }

  public void setReadOnly(boolean isReadOnly)
  {
    content.setReadOnly(isReadOnly);
  }

  public void setSilent(boolean silent)
  {
    content.setSilent(silent);
  }

  public void setTrackChanges(boolean isTrackChanges)
  {
    content.setTrackChanges(isTrackChanges);
  }

  public String toString()
  {
    return content.toString();
  }

  public void unregister()
  {
    removeContentChangeListener(); // Normal close does not invoke removeChangeListener, so be sure not to leave a reference
  }

  private final class ContentChangeListener implements ChangeListener
  {
    @Override
    public void modelChanged(ChangeEvent event)
    {
      event.setSource(ContentReference.this);
      for (ChangeListener contentReferenceChangeListener : contentReferenceChangeListeners)
      {
        contentReferenceChangeListener.modelChanged(event);
      }
    }
  }

}
