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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.semanticexpression.connector.shared.TagFilter;

public class TagFilterContentPanel extends ContentPanel
{
  private Text actionText;
  private CheckBox authorCheckBox;
  private CheckBox contentCheckBox;
  private Radio excludeRadio;
  private Radio includeRadio;
  private CheckBox myPrivateCheckBox;
  private CheckBox myPublicCheckBox;
  private String name;
  private CheckBox otherPublicCheckBox;
  private CheckBox semanticCheckBox;
  private Text typeText;
  private Text visibilityText;

  public TagFilterContentPanel()
  {
    setLayout(new RowLayout(Orientation.VERTICAL));
    setCollapsible(true);
    setAnimCollapse(false);
    getHeader().addTool(new ToolButton("x-tool-close", new CloseListener()));
    add(getActionText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(5, 0, 0, 5)));
    add(getIncludeRadio(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getExcludeRadio(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getTypeText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 0, 0, 5)));
    add(getAuthorCheckBox(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getContentCheckBox(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getSemanticCheckBox(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getVisibilityText(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 0, 0, 5)));
    add(getMyPrivateCheckBox(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getMyPublicCheckBox(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
    add(getOtherPublicCheckBox(), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 0, 0, 10)));
  }

  public void display(TagFilter tagFilter)
  {
    name = tagFilter.getName();
    setHeading("Tag Filter: <b>" + name + "</b>");
    getIncludeRadio().setValue(tagFilter.isInclude());
    //getExcludeRadio().setValue(!tagFilter.isInclude());
    getAuthorCheckBox().setValue(tagFilter.isAuthor());
    getContentCheckBox().setValue(tagFilter.isContent());
    getSemanticCheckBox().setValue(tagFilter.isSemantic());
    getMyPrivateCheckBox().setValue(tagFilter.isMyPrivate());
    getMyPublicCheckBox().setValue(tagFilter.isMyPublic());
    getOtherPublicCheckBox().setValue(tagFilter.isOtherPublic());
  }

  public Text getActionText()
  {
    if (actionText == null)
    {
      actionText = new Text("Action:");
    }
    return actionText;
  }

  public CheckBox getAuthorCheckBox()
  {
    if (authorCheckBox == null)
    {
      authorCheckBox = new CheckBox();
      authorCheckBox.setBoxLabel("Author");
      authorCheckBox.setHideLabel(true);
    }
    return authorCheckBox;
  }

  public CheckBox getContentCheckBox()
  {
    if (contentCheckBox == null)
    {
      contentCheckBox = new CheckBox();
      contentCheckBox.setBoxLabel("Content");
      contentCheckBox.setHideLabel(true);
    }
    return contentCheckBox;
  }

  public Radio getExcludeRadio()
  {
    if (excludeRadio == null)
    {
      excludeRadio = new Radio();
      excludeRadio.setBoxLabel("Exclude");
      excludeRadio.setHideLabel(true);
      excludeRadio.setName(getRadioGroupName());
    }
    return excludeRadio;
  }

  public Radio getIncludeRadio()
  {
    if (includeRadio == null)
    {
      includeRadio = new Radio();
      includeRadio.setBoxLabel("Include");
      includeRadio.setHideLabel(true);
      includeRadio.setName(getRadioGroupName());
    }
    return includeRadio;
  }

  public CheckBox getMyPrivateCheckBox()
  {
    if (myPrivateCheckBox == null)
    {
      myPrivateCheckBox = new CheckBox();
      myPrivateCheckBox.setBoxLabel("My Private");
      myPrivateCheckBox.setHideLabel(true);
    }
    return myPrivateCheckBox;
  }

  public CheckBox getMyPublicCheckBox()
  {
    if (myPublicCheckBox == null)
    {
      myPublicCheckBox = new CheckBox();
      myPublicCheckBox.setBoxLabel("My Public");
      myPublicCheckBox.setHideLabel(true);
    }
    return myPublicCheckBox;
  }

  public CheckBox getOtherPublicCheckBox()
  {
    if (otherPublicCheckBox == null)
    {
      otherPublicCheckBox = new CheckBox();
      otherPublicCheckBox.setBoxLabel("Other Public");
      otherPublicCheckBox.setHideLabel(true);
    }
    return otherPublicCheckBox;
  }

  private String getRadioGroupName()
  {
    return getId() + "-includeRadio";
  }

  public CheckBox getSemanticCheckBox()
  {
    if (semanticCheckBox == null)
    {
      semanticCheckBox = new CheckBox();
      semanticCheckBox.setBoxLabel("Semantic");
      semanticCheckBox.setHideLabel(true);
    }
    return semanticCheckBox;
  }

  public TagFilter getTagFilter()
  {
    TagFilter tagFilter = new TagFilter();
    tagFilter.setName(name);
    tagFilter.setInclude(getIncludeRadio().getValue());
    tagFilter.setAuthor(getAuthorCheckBox().getValue());
    tagFilter.setContent(getContentCheckBox().getValue());
    tagFilter.setSemantic(getSemanticCheckBox().getValue());
    tagFilter.setMyPrivate(getMyPrivateCheckBox().getValue());
    tagFilter.setMyPublic(getMyPublicCheckBox().getValue());
    tagFilter.setOtherPublic(getOtherPublicCheckBox().getValue());
    return tagFilter;
  }

  public Text getTypeText()
  {
    if (typeText == null)
    {
      typeText = new Text("Type:");
    }
    return typeText;
  }

  public Text getVisibilityText()
  {
    if (visibilityText == null)
    {
      visibilityText = new Text("Visibility:");
    }
    return visibilityText;
  }

  private final class CloseListener extends SelectionListener<IconButtonEvent>
  {
    @Override
    public void componentSelected(IconButtonEvent ce)
    {
      removeFromParent();
    }
  }
}
