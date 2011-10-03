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

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.shared.TagConstants.TagType;

public final class SearchMenu extends Menu
{
  private MenuItem addAuthorTag;
  private MenuItem addContentTag;
  private MenuItem addTagFilter;
  private MenuItem openItem;
  private SearchFrame searchFrame;
  private MenuItem runSearch;
  private MenuItem resetForm;
  private MenuItem getMyContent;

  public SearchMenu(SearchFrame searchFrame)
  {
    this.searchFrame = searchFrame;
    add(getOpenItem());
    add(new SeparatorMenuItem());
    add(getAddContentTag());
    add(getAddAuthorTag());
    add(new SeparatorMenuItem());
    add(getAddTagFilter());
    add(new SeparatorMenuItem());
    add(getRunSearch());
    add(getResetForm());
    add(getGetMyContent());
  }

  private MenuItem getRunSearch()
  {
    if (runSearch == null)
    {
      runSearch = new MenuItem("Run Search");
      runSearch.setIcon(Resources.SEARCH_RUN_SEARCH);
      runSearch.addSelectionListener(new RunSearchListener());
    }
    return runSearch;
  }

  private MenuItem getResetForm()
  {
    if (resetForm == null)
    {
      resetForm = new MenuItem("Reset Form");
      resetForm.setIcon(Resources.SEARCH_RESET_FORM);
      resetForm.addSelectionListener(new ResetFormListener());
    }
    return resetForm;
  }

  private MenuItem getGetMyContent()
  {
    if (getMyContent == null)
    {
      getMyContent = new MenuItem("Get My Content");
      getMyContent.setIcon(Resources.SEARCH_GET_MY_CONTENT);
      getMyContent.addSelectionListener(new GetMyContentListener());
    }
    return getMyContent;
  }

  private MenuItem getAddAuthorTag()
  {
    if (addAuthorTag == null)
    {
      addAuthorTag = new MenuItem("Add Author Tag");
      addAuthorTag.setIcon(Resources.TAG_ADD_AUTHOR_TAG);
      addAuthorTag.addSelectionListener(new AddAuthorTagListener());
    }
    return addAuthorTag;
  }

  private MenuItem getAddContentTag()
  {
    if (addContentTag == null)
    {
      addContentTag = new MenuItem("Add Content Tag");
      addContentTag.setIcon(Resources.TAG_ADD_CONTENT_TAG);
      addContentTag.addSelectionListener(new AddContentTagListener());
    }
    return addContentTag;
  }

  private MenuItem getAddTagFilter()
  {
    if (addTagFilter == null)
    {
      addTagFilter = new MenuItem("Add Tag Filter");
      addTagFilter.setIcon(Resources.TAG_ADD_TAG_FILTER);
      addTagFilter.addSelectionListener(new AddTagFilterListener());
    }
    return addTagFilter;
  }

  private MenuItem getOpenItem()
  {
    if (openItem == null)
    {
      openItem = new MenuItem("Open in Editor");
      openItem.setIcon(Resources.OPEN_IN_EDITOR);
      openItem.addSelectionListener(new OpenListener());
    }
    return openItem;
  }

  private final class AddAuthorTagListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.addTag(TagType.AUTHOR);
    }
  }

  private final class RunSearchListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.runSearch();
    }
  }

  private final class ResetFormListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.resetForm();
    }
  }

  private final class GetMyContentListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.getMyContent();
    }
  }

  private final class AddContentTagListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.addTag(TagType.CONTENT);
    }
  }

  private final class AddTagFilterListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.addTagFilter();
    }
  }

  private final class OpenListener extends SelectionListener<MenuEvent>
  {
    @Override
    public void componentSelected(MenuEvent ce)
    {
      searchFrame.openSelectedItems();
    }
  }
}