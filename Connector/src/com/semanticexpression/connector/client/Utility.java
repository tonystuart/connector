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

package com.semanticexpression.connector.client;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;
import com.semanticexpression.connector.client.frame.editor.style.StyleExampleRenderer;
import com.semanticexpression.connector.client.services.GetCredentialServiceRequest;
import com.semanticexpression.connector.client.wiring.Directory;
import com.semanticexpression.connector.shared.Credential;
import com.semanticexpression.connector.shared.Id;
import com.semanticexpression.connector.shared.Keys;
import com.semanticexpression.connector.shared.exception.ServerException;

/**
 * NB: This classes uses ASCII character values to avoid dragging in the GWT
 * Character class.
 */

public final class Utility
{
  public static String abbreviate(String value)
  {
    return value == null ? "" : value.length() < 16 ? value : (value.substring(0, 16) + "&hellip;");
  }

  public static <T> void addAll(List<T> target, Iterable<T> source)
  {
    for (T item : source)
    {
      target.add(item);
    }
  }

  public static void closeWindow(Widget widget)
  {
    Window window = getWindow(widget);
    if (window != null)
    {
      window.hide();
    }
  }

  public static <X> X coalesce(X value, X defaultValue)
  {
    return value != null ? value : defaultValue;
  }

  public static int compareAlphaNumerically(String a, String b)
  {
    int aAlphaOffset = findAlphaOffset(a);
    if (aAlphaOffset > 0)
    {
      int bAlphaOffset = findAlphaOffset(b);
      if (bAlphaOffset > 0)
      {
        int aNumber = Integer.parseInt(a.substring(0, aAlphaOffset));
        int bNumber = Integer.parseInt(b.substring(0, bAlphaOffset));
        int delta = aNumber - bNumber;
        if (delta != 0)
        {
          return delta;
        }
      }
    }
    return a.compareTo(b);
  }

  public static String concatenate(List<ModelData> modelDataList, String name, String delimiter)
  {
    StringBuilder s = new StringBuilder();
    for (ModelData modelData : modelDataList)
    {
      if (s.length() > 0)
      {
        s.append(delimiter);
      }
      String value = modelData.get(name);
      s.append(value);
    }
    return s.toString();
  }

  public static ColumnConfig createColumnConfig(String id, String name, int width, DateTimeFormat dateTimeFormat)
  {
    ColumnConfig columnConfig = new ColumnConfig(id, name, width);
    columnConfig.setDateTimeFormat(dateTimeFormat);
    return columnConfig;
  }

  public static ColumnConfig createColumnConfig(String id, String name, int width, NumberFormat numberFormat)
  {
    ColumnConfig columnConfig = new ColumnConfig(id, name, width);
    columnConfig.setNumberFormat(numberFormat);
    return columnConfig;
  }

  public static ColumnConfig createColumnConfigStyleExample(String id, String name, int width)
  {
    ColumnConfig styleExample = new ColumnConfig(id, name, width);
    styleExample.setRenderer(new StyleExampleRenderer());
    return styleExample;
  }

  public static ColumnConfig createColumnConfigYesNo(String id, String name, int width)
  {
    ColumnConfig columnConfig = new ColumnConfig(id, name, width);
    columnConfig.setRenderer(new GridCellRenderer<ModelData>()
    {
      @Override
      public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
      {
        String html = null;
        Boolean value = model.get(property);
        if (value != null)
        {
          html = value ? "yes" : "no";
        }
        return html;
      }
    });
    return columnConfig;
  }

  public static ColumnConfig createDateColumnConfig(String id, String name, int width)
  {
    return createColumnConfig(id, name, width, DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
  }

  public static ColumnConfig createDateTimeColumnConfig(String id, String name, int width)
  {
    return createColumnConfig(id, name, width, DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT));
  }

  public static ModelData createModel(String name, Object value)
  {
    BaseModelData modelData = new BaseModelData();
    modelData.set(Keys.NAME, name);
    modelData.set(Keys.VALUE, value);
    return modelData;
  }

  public static String createStyleClassName(String styleName)
  {
    int length = styleName.length();
    StringBuilder s = new StringBuilder(length);

    for (int i = 0; i < length; i++)
    {
      char c = styleName.charAt(i);
      c = toLowerCase(c);

      if (c == ' ')
      {
        c = '-';
      }
      else if (s.length() == 0 && !isLowerCase(c))
      {
        c = 0;
      }
      else if (!isLowerCase(c) && !isDigit(c) && c != '-' && c != '_')
      {
        c = 0;
      }

      if (c != 0)
      {
        s.append(toLowerCase(c));
      }
    }

    String styleClassName = s.toString();
    if (styleClassName.length() == 0)
    {
      styleClassName = "invalid";
    }

    return styleClassName;
  }

  public static String createStyleSelector(String styleClassName)
  {
    String selector = "." + styleClassName;
    return selector;
  }

  public static void displaySelectionMessageBox()
  {
    MessageBox.alert("Nothing Selected", "Please select an item and try again.", null);
  }

  public static void displayStackTrace(Throwable throwable)
  {
    String message;
    if (throwable instanceof ServerException)
    {
      message = throwable.getLocalizedMessage();
    }
    else if (throwable instanceof UmbrellaException)
    {
      StringBuilder s = new StringBuilder();
      s.append("The exception was wrapped in an UmbrellaException. The contents are:");
      Set<Throwable> causes = ((UmbrellaException)throwable).getCauses();
      for (Throwable cause : causes)
      {
        s.append("<br/>");
        s.append(getStackTrace(cause));
      }
      message = s.toString();
    }
    else
    {
      message = getStackTrace(throwable);
    }
    Dialog w = new Dialog();
    w.setSize(600, 400);
    w.setHeading("An Exception Occurred");
    w.setLayout(new FitLayout());
    Html html = new Html(message);
    html.setStyleAttribute("overflow", "auto");
    w.add(html);
    w.setHideOnButtonClick(true);
    w.show();
    w.toFront();
  }

  public static boolean equals(ModelData p, ModelData q, String... keys)
  {
    for (String key : keys)
    {
      Object pValue = p.get(key);
      Object qValue = q.get(key);
      if ((pValue == null && qValue != null) || !pValue.equals(qValue))
      {
        return false;
      }
    }
    return true;
  }

  public static boolean equalsWithNull(Object left, Object right)
  {
    return left == right ? true //
        : (left != null && right != null) ? left.equals(right) //
            : (left instanceof Collection<?> && right == null) ? ((Collection<?>)left).size() == 0 //
                : (right instanceof Collection<?> && left == null) ? ((Collection<?>)right).size() == 0 //
                    : false;
  }

  public static double extractDouble(String token)
  {
    double value;
    try
    {
      value = Double.valueOf(token);
    }
    catch (NumberFormatException e1)
    {
      token = token.replaceAll("[^0-9\\.]", ""); // TODO: Resolve NLS issue with thousands and decimal separator
      try
      {
        value = Double.valueOf(token);
      }
      catch (NumberFormatException e2)
      {
        value = 0;
      }
    }
    return value;
  }

  private static int findAlphaOffset(String a)
  {
    int length = a.length();
    for (int i = 0; i < length; i++)
    {
      char c = a.charAt(i);
      if (c < '0' || c > '9')
      {
        return i;
      }
    }
    return 0;
  }

  public static String formatDate(Date date)
  {
    return DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(date);
  }

  public static int getAccountCreationOptions()
  {
    return getCredential().getAccountCreationOptions();
  }

  public static String getAuthenticationToken()
  {
    return getCredential().getAuthenticationToken();
  }

  public static Credential getCredential()
  {
    return Directory.getServiceBus().invoke(new GetCredentialServiceRequest()).getCredential();
  }

  public static Id getMonitorId()
  {
    return Directory.getStatusMonitor().getMonitorId();
  }

  public static String getRootCause(Throwable throwable)
  {
    Throwable rootThrowable = throwable;
    while ((throwable = throwable.getCause()) != null)
    {
      rootThrowable = throwable;
    }
    String rootCause = rootThrowable.getLocalizedMessage();
    if (rootCause == null)
    {
      rootCause = rootThrowable.toString();
    }
    return rootCause;
  }

  public static String getStackTrace(Throwable throwable)
  {
    StringBuilder s = new StringBuilder();
    s.append("<b>");
    s.append(getRootCause(throwable));
    s.append("</b>\n<br/>\n<br/>\nIf you need additional assistance with this problem,\n<br/>\nplease forward the following information to your local support staff:\n<br/>\n<br/>\n");
    s.append(throwable.toString());
    s.append("\n<br/>\n");

    for (StackTraceElement ste : throwable.getStackTrace())
    {
      s.append("at ");
      s.append(ste.getClassName());
      s.append(".");
      s.append(ste.getMethodName());
      s.append("(");
      s.append(ste.getFileName());
      s.append(":");
      s.append(ste.getLineNumber());
      s.append(")");
      s.append("<br/>\n");
    }
    return s.toString();
  }

  public static <M extends ModelData> List<M> getStoreEventModels(StoreEvent<M> storeEvent)
  {
    List<M> models;
    M model = storeEvent.getModel();
    if (model != null)
    {
      models = new LinkedList<M>();
      models.add(model);
    }
    else
    {
      models = storeEvent.getModels();
    }
    return models;
  }

  public static <M extends ModelData> List<M> getTreeStoreEventModels(TreeStoreEvent<M> treeStoreEvent)
  {
    List<M> models;
    M model = treeStoreEvent.getChild();
    if (model != null)
    {
      models = new LinkedList<M>();
      models.add(model);
    }
    else
    {
      models = treeStoreEvent.getChildren();
    }
    return models;
  }

  public static String getUserName()
  {
    return getCredential().getUserName();
  }

  public static Window getWindow(Widget widget)
  {
    while (widget != null)
    {
      if (widget instanceof Window)
      {
        return (Window)widget;
      }
      widget = widget.getParent();
    }
    return null;
  }

  public static boolean isAlpha(char c)
  {
    return isLowerCase(c) || isUpperCase(c);
  }

  public static boolean isAlphanumeric(char c)
  {
    return isAlpha(c) || isDigit(c);
  }

  public static boolean isDigit(char c)
  {
    return '0' <= c && c <= '9';
  }

  public static boolean isLowerCase(char c)
  {
    return 'a' <= c && c <= 'z';
  }

  public static boolean isSet(ModelData modelData, String name)
  {
    Object value = modelData.get(name);
    return isSet(value);
  }

  public static boolean isSet(Object value)
  {
    return value instanceof Boolean ? (Boolean)value : false;
  }

  public static boolean isUpperCase(char c)
  {
    return 'A' <= c && c <= 'Z';
  }

  public static boolean isWhitespace(char c)
  {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
  }

  public static Date parseDate(String string)
  {
    return DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).parse(string);
  }

  public static Integer parseInteger(String token)
  {
    Integer value;
    try
    {
      value = Integer.parseInt(token);
    }
    catch (NumberFormatException e)
    {
      value = null;
    }
    return value;
  }

  public static char toLowerCase(char c)
  {
    return isUpperCase(c) ? (char)(c + ('a' - 'A')) : c;
  }

  public static char toUpperCase(char c)
  {
    return isLowerCase(c) ? (char)(c - ('a' - 'A')) : c;
  }

}
