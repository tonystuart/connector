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

package com.semanticexpression.connector.client.frame.editor.style;

import java.util.List;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.RichTextArea;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.client.icons.Resources;
import com.semanticexpression.connector.client.widget.BaseEditorWindow;
import com.semanticexpression.connector.client.widget.SafeTextArea;
import com.semanticexpression.connector.client.widget.SafeTextField;
import com.semanticexpression.connector.shared.Association;
import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Keys;

public final class NamedStyleEditorWindow extends BaseEditorWindow
{
  private ModelData defaultStyleElement;
  private Button editButton;
  private CheckBox isCommentEnabledCheckBox;
  private LayoutContainer leftLayoutContainer;
  private AdapterField previewAdapterField;
  private LayoutContainer previewLayoutContainer;
  private RichTextArea previewRichTextArea;
  private Text previewText;
  private LayoutContainer rightLayoutContainer;
  private LayoutContainer selectorLayoutContainer;
  private SafeTextField<String> selectorSafeTextField;
  private Text styleDeclarationsText;
  private ComboBox<ModelData> styleElementComboBox;
  private ListStore<ModelData> styleElementComboBoxListStore;
  private Text styleElementText;
  private SafeTextArea valueField;

  public NamedStyleEditorWindow(ListStore<Association> listStore)
  {
    super(Resources.STYLE, "Named Style Editor", "Style Name:", "Style Selector:", listStore);
  }

  private ModelData createModelData(String name, String value)
  {
    BaseModelData modelData = new BaseModelData();
    modelData.set(Keys.NAME, name);
    modelData.set(Keys.VALUE, value);
    return modelData;
  }

  private void displayPreview()
  {
    String styleValue = (String)getValueField().getValue();
    if (styleValue == null)
    {
      styleValue = "";
    }
    String styleElementName = getStyleElementComboBox().getValue().get(Keys.NAME);
    String exampleHtml;
    if (styleElementName.equals(HtmlConstants.SE_STYLE_LIST_ITEM))
    {
      exampleHtml = "Unordered list:" + //
          "<ul>" + //
          "<li>List item</li>" + //
          "<li style='" + styleValue + "'>Styled list item</li>" + //
          "<li>List item</li>" + //
          "</ul>" + //
          "Ordered list:" + //
          "<ol>" + //
          "<li>List item</li>" + //
          "<li style='" + styleValue + "'>Styled list item</li>" + //
          "<li>List item</li>" + //
          "</ol>"; //
    }
    else if (styleElementName.equals(HtmlConstants.SE_STYLE_UNORDERED_LIST))
    {
      exampleHtml = "Normal unordered list:" + //
          "<ul>" + //
          "<li>List item</li>" + //
          "<li>List item</li>" + //
          "</ul>" + //
          "Styled unordered list:" + //
          "<ul style='" + styleValue + "'>" + //
          "<li>List item</li>" + //
          "<li>List item</li>" + //
          "</ul>"; //
    }
    else if (styleElementName.equals(HtmlConstants.SE_STYLE_ORDERED_LIST))
    {
      exampleHtml = "Normal ordered list:" + //
          "<ol>" + //
          "<li>List item</li>" + //
          "<li>List item</li>" + //
          "</ol>" + //
          "Styled ordered list:" + //
          "<ol style='" + styleValue + "'>" + //
          "<li>List item</li>" + //
          "<li>List item</li>" + //
          "</ol>"; //
    }
    else
    {
      String si = "<" + styleElementName + " style='" + styleValue + "'>";
      String so = "</" + styleElementName + ">";
      exampleHtml = "When defining styles, be sure to " + si + "select a name that conveys its semantic significance" + so + " instead of its visual appearance. For example, if you wish to use a style to " + si + "highlight" + so + " something because it is important, choose a name like " + si + "important" + so + " instead of a name like " + si + "bold. " + so + "This makes the meaning of the style clear as well as enabling you to later change the appearance just by changing the style definition.";
    }

    getPreviewRichTextArea().setHTML(exampleHtml); // Adding <!DOCTYPE HTML><html><body>...</body></html> does not appear to have an effect, it still accepts poorly formed styles
  }

  @Override
  protected void displayValue(Association association)
  {
    getValueField().setValue(association.<String> get(Keys.VALUE));
  }

  @Override
  public void edit(Association association, boolean isNew)
  {
    super.edit(association, isNew);

    if (isNew)
    {
      getStyleElementComboBox().setValue(defaultStyleElement);
      propagateDefaultSelector();
      setUserDefinedSelector(false);
      getIsCommentEnabledCheckBox().setValue(false);
    }
    else
    {
      String styleElementName = association.get(Keys.STYLE_ELEMENT_NAME);
      initializeStyleElement(styleElementName);
      String styleSelector = association.get(Keys.STYLE_SELECTOR);
      initializeStyleSelector(styleSelector);
      getIsCommentEnabledCheckBox().setValue(association.get(Keys.STYLE_IS_COMMENT_ENABLED, false));
    }

    displayPreview();
  }

  private String formatDefaultSelector()
  {
    String styleName = getNameTextField().getValue();
    if (styleName == null)
    {
      styleName = "undefined";
    }
    String styleClassName = Utility.createStyleClassName(styleName);
    String styleSelector = Utility.createStyleSelector(styleClassName);
    return styleSelector;
  }

  public Button getEditButton()
  {
    if (editButton == null)
    {
      editButton = new Button("Edit");
      editButton.addSelectionListener(new EditButtonListener());
    }
    return editButton;
  }

  private CheckBox getIsCommentEnabledCheckBox()
  {
    if (isCommentEnabledCheckBox == null)
    {
      isCommentEnabledCheckBox = new CheckBox();
      isCommentEnabledCheckBox.setBoxLabel("Use this style for comments");
    }
    return isCommentEnabledCheckBox;
  }

  private LayoutContainer getLeftLayoutContainer()
  {
    if (leftLayoutContainer == null)
    {
      leftLayoutContainer = new LayoutContainer();
      leftLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      leftLayoutContainer.add(getNameText(), new RowData(1.0, Style.DEFAULT));
      leftLayoutContainer.add(getNameTextField(), new RowData(1.0, Style.DEFAULT));
      leftLayoutContainer.add(getStyleElementText(), new RowData(1.0, Style.DEFAULT, new Margins(5, 0, 0, 0)));
      leftLayoutContainer.add(getStyleElementComboBox(), new RowData(1.0, Style.DEFAULT));
      leftLayoutContainer.add(getValueText(), new RowData(1.0, Style.DEFAULT, new Margins(5, 0, 0, 0)));
      leftLayoutContainer.add(getSelectorLayoutContainer(), new RowData(1.0, 22));
      leftLayoutContainer.add(getStyleDeclarationsText(), new RowData(1.0, Style.DEFAULT, new Margins(5, 0, 0, 0)));
      leftLayoutContainer.add(getValueField(), new RowData(1.0, 1.0));
    }
    return leftLayoutContainer;
  }

  @Override
  public SafeTextField<String> getNameTextField()
  {
    if (nameTextField == null)
    {
      nameTextField = new SafeTextField<String>();
      nameTextField.setWidth("200");
      nameTextField.addListener(Events.Change, new NameChangeListener());
      nameTextField.addKeyListener(new NameKeyListener());
    }
    return nameTextField;
  }

  private AdapterField getPreviewAdapterField()
  {
    if (previewAdapterField == null)
    {
      previewAdapterField = new AdapterField(getPreviewRichTextArea());
      previewAdapterField.setResizeWidget(true);
    }
    return previewAdapterField;
  }

  private LayoutContainer getPreviewLayoutContainer()
  {
    if (previewLayoutContainer == null)
    {
      // This really seems like overkill just to get a border, but our gwt-RichTextArea sets border: none to turn off the ugly gwt border and I can't figure out how to turn on the gxt default.
      previewLayoutContainer = new LayoutContainer(new FitLayout());
      previewLayoutContainer.setBorders(true);
      previewLayoutContainer.add(getPreviewAdapterField());
    }
    return previewLayoutContainer;
  }

  public RichTextArea getPreviewRichTextArea()
  {
    if (previewRichTextArea == null)
    {
      previewRichTextArea = new RichTextArea();
    }
    return previewRichTextArea;
  }

  public Text getPreviewText()
  {
    if (previewText == null)
    {
      previewText = new Text("Style Preview:");
    }
    return previewText;
  }

  private LayoutContainer getRightLayoutContainer()
  {
    if (rightLayoutContainer == null)
    {
      rightLayoutContainer = new LayoutContainer();
      rightLayoutContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      rightLayoutContainer.add(getPreviewText(), new RowData(1.0, Style.DEFAULT));
      rightLayoutContainer.add(getPreviewLayoutContainer(), new RowData(1.0, 1.0));
    }
    return rightLayoutContainer;
  }

  private LayoutContainer getSelectorLayoutContainer()
  {
    if (selectorLayoutContainer == null)
    {
      selectorLayoutContainer = new LayoutContainer();
      selectorLayoutContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
      selectorLayoutContainer.add(getSelectorSafeTextField(), new RowData(1, Style.DEFAULT));
      selectorLayoutContainer.add(getEditButton(), new RowData(40, Style.DEFAULT, new Margins(0, 0, 0, 5)));
    }
    return selectorLayoutContainer;
  }

  public SafeTextField<String> getSelectorSafeTextField()
  {
    if (selectorSafeTextField == null)
    {
      selectorSafeTextField = new SafeTextField<String>();
    }
    return selectorSafeTextField;
  }

  public Text getStyleDeclarationsText()
  {
    if (styleDeclarationsText == null)
    {
      styleDeclarationsText = new Text("Style Declaration(s):");
    }
    return styleDeclarationsText;
  }

  public ComboBox<ModelData> getStyleElementComboBox()
  {
    if (styleElementComboBox == null)
    {
      styleElementComboBox = new ComboBox<ModelData>();
      styleElementComboBox.setStore(getStyleElementComboBoxListStore());
      styleElementComboBox.setDisplayField(Keys.VALUE);
      styleElementComboBox.setTriggerAction(TriggerAction.ALL);
      styleElementComboBox.setEditable(false);
      styleElementComboBox.setForceSelection(true);
      styleElementComboBox.setValue(defaultStyleElement);
      styleElementComboBox.setWidth("200");
      styleElementComboBox.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
      {
        @Override
        public void selectionChanged(SelectionChangedEvent<ModelData> se)
        {
          displayPreview();
        }
      });
    }
    return styleElementComboBox;
  }

  private ListStore<ModelData> getStyleElementComboBoxListStore()
  {
    if (styleElementComboBoxListStore == null)
    {
      styleElementComboBoxListStore = new ListStore<ModelData>();
      styleElementComboBoxListStore.add(createModelData(HtmlConstants.SE_STYLE_DIVISION, "Block (div)"));
      styleElementComboBoxListStore.add(createModelData(HtmlConstants.SE_STYLE_ANCHOR, "Hyperlink (a)"));
      styleElementComboBoxListStore.add(createModelData(HtmlConstants.SE_STYLE_LIST_ITEM, "List Item (li)"));
      styleElementComboBoxListStore.add(createModelData(HtmlConstants.SE_STYLE_ORDERED_LIST, "Ordered List (ol)"));
      styleElementComboBoxListStore.add(defaultStyleElement = createModelData(HtmlConstants.SE_STYLE_SPAN, "Inline (span)"));
      styleElementComboBoxListStore.add(createModelData(HtmlConstants.SE_STYLE_UNORDERED_LIST, "Unordered List (ul)"));
    }
    return styleElementComboBoxListStore;
  }

  public Text getStyleElementText()
  {
    if (styleElementText == null)
    {
      styleElementText = new Text("Style Element:");
    }
    return styleElementText;
  }

  @Override
  protected String getValue()
  {
    return getValueField().getValue();
  }

  @Override
  public SafeTextArea getValueField()
  {
    if (valueField == null)
    {
      valueField = new SafeTextArea();
      valueField.addListener(Events.Change, new PropertyValueChangeListener());
      valueField.addKeyListener(new PropertyValueKeyListener());
      valueField.setEmptyText("Enter CSS style declaration(s) here");
    }
    return valueField;
  }

  @Override
  protected void initializeForm()
  {
    LayoutContainer c = new LayoutContainer();
    c.setLayout(new RowLayout(Orientation.HORIZONTAL));
    c.add(getLeftLayoutContainer(), new RowData(0.5, 1.0));
    c.add(getRightLayoutContainer(), new RowData(0.5, 1.0, new Margins(0, 0, 0, 5)));
    add(c, new RowData(1.0, 1.0));
    getOkayCancelToolBar().insert(getIsCommentEnabledCheckBox(), 0);
  }

  private void initializeStyleElement(String styleElementName)
  {
    List<ModelData> models = getStyleElementComboBoxListStore().getModels();
    for (ModelData model : models)
    {
      String name = model.get(Keys.NAME);
      if (name.equals(styleElementName))
      {
        getStyleElementComboBox().setValue(model);
        return;
      }
    }
  }

  private void initializeStyleSelector(String styleClassName)
  {
    getSelectorSafeTextField().setValue(styleClassName);
    String defaultSelectorValue = formatDefaultSelector();
    boolean isUserDefinedSelector = !defaultSelectorValue.equals(styleClassName);
    setUserDefinedSelector(isUserDefinedSelector);
  }

  @Override
  public void okay()
  {
    // Fetch values before super.okay() hides the window
    String styleElementName = getStyleElementComboBox().getValue().get(Keys.NAME);
    String styleSelector = getSelectorSafeTextField().getValue();
    Boolean isCommentEnabled = getIsCommentEnabledCheckBox().getValue();

    super.okay();

    association.set(Keys.STYLE_ELEMENT_NAME, styleElementName);
    association.set(Keys.STYLE_SELECTOR, styleSelector);
    association.set(Keys.STYLE_IS_COMMENT_ENABLED, isCommentEnabled);
  }

  @Override
  protected void onNameChange()
  {
    propagateDefaultSelectorConditional();
    super.onNameChange();
  }

  protected void propagateDefaultSelector()
  {
    String selector = formatDefaultSelector();
    getSelectorSafeTextField().setValue(selector);
  }

  protected void propagateDefaultSelectorConditional()
  {
    if (!getSelectorSafeTextField().isEnabled())
    {
      propagateDefaultSelector();
    }
  }

  private void setUserDefinedSelector(boolean isUserDefinedSelector)
  {
    getSelectorSafeTextField().setEnabled(isUserDefinedSelector);
    getEditButton().setText(isUserDefinedSelector ? "Auto" : "Edit");
  }

  @Override
  protected void setWindowSize()
  {
    setSize("450", "350");
  }

  private final class EditButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      setUserDefinedSelector(!getSelectorSafeTextField().isEnabled());
      propagateDefaultSelectorConditional();
    }
  }

  private final class PropertyValueChangeListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      displayPreview();
    }
  }

  private final class PropertyValueKeyListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      displayPreview();
    }
  }
}
