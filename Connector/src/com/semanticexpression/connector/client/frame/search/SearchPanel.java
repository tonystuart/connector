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

package com.semanticexpression.connector.client.frame.search;

import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.semanticexpression.connector.shared.SearchRequest;
import com.semanticexpression.connector.shared.TagFilter;

public class SearchPanel extends LayoutContainer
{
  private AccordionLayout accordionLayout;
  private ContentPanel searchOptionsContentPanel;
  private SearchOptionTreePanel searchOptionsTreePanel;

  public SearchPanel()
  {
    addStyleName("connector-BorderTop");
    setLayout(getAccordionLayout());
    add(getSearchOptionsContentPanel());
    setLayoutOnChange(true);
  }

  public void addTagFilter(TagFilter tagFilter)
  {
    TagFilterContentPanel tagFilterContentPanel = new TagFilterContentPanel();
    tagFilterContentPanel.display(tagFilter);
    add(tagFilterContentPanel);
    getAccordionLayout().setActiveItem(tagFilterContentPanel);
  }

  private AccordionLayout getAccordionLayout()
  {
    if (accordionLayout == null)
    {
      accordionLayout = new AccordionLayout();
      accordionLayout.setHideCollapseTool(true);
    }
    return accordionLayout;
  }

  public ContentPanel getSearchOptionsContentPanel()
  {
    if (searchOptionsContentPanel == null)
    {
      searchOptionsContentPanel = new ContentPanel();
      searchOptionsContentPanel.setAnimCollapse(false);
      searchOptionsContentPanel.setHeading("Search Options");
      searchOptionsContentPanel.setCollapsible(true);
      searchOptionsContentPanel.setLayout(new FitLayout());
      searchOptionsContentPanel.add(getSearchOptionsTreePanel());
    }
    return searchOptionsContentPanel;
  }

  public SearchOptionTreePanel getSearchOptionsTreePanel()
  {
    if (searchOptionsTreePanel == null)
    {
      searchOptionsTreePanel = new SearchOptionTreePanel();
    }
    return searchOptionsTreePanel;
  }

  public void prepareSearchRequest(SearchRequest searchRequest)
  {
    getSearchOptionsTreePanel().prepareSearchRequest(searchRequest);

    List<TagFilter> tagFilters = null;

    int itemCount = getItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      Component item = getItem(itemOffset);
      if (item instanceof TagFilterContentPanel)
      {
        TagFilterContentPanel tagFilterContentPanel = (TagFilterContentPanel)item;
        TagFilter tagFilter = tagFilterContentPanel.getTagFilter();
        if (tagFilters == null)
        {
          tagFilters = new LinkedList<TagFilter>();
        }
        tagFilters.add(tagFilter);
      }
    }

    searchRequest.setTagFilters(tagFilters);
  }

  @Override
  protected boolean remove(Component item)
  {
    boolean result;
    if (item instanceof TagFilterContentPanel)
    {
      int itemOffset = indexOf(item);
      result = super.remove(item);
      if (itemOffset == getItemCount())
      {
        itemOffset--;
      }
      if (itemOffset >= 0)
      {
        Component component = getItem(itemOffset);
        if (component instanceof ContentPanel)
        {
          //getAccordionLayout().setActiveItem(component);
          ((ContentPanel)component).expand();
        }
      }
    }
    else
    {
      result = super.remove(item);
    }
    return result;
  }

  public void resetForm()
  {
    getSearchOptionsTreePanel().resetForm();

    int itemCount = getItemCount();
    for (int itemOffset = itemCount - 1; itemOffset >= 0; itemOffset--)
    {
      Component item = getItem(itemOffset);
      if (item instanceof TagFilterContentPanel)
      {
        remove(item);
      }
    }
  }

}