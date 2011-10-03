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

import java.io.PrintWriter;

import com.semanticexpression.connector.shared.HtmlConstants;
import com.semanticexpression.connector.shared.Id;

public class HtmlWriter
{
  private boolean lineSeparatorRequired;
  private PrintWriter printWriter;

  public HtmlWriter(PrintWriter printWriter)
  {
    this.printWriter = printWriter;
  }

  private String getAttributeSpecification(String[] attributeNameValuePairs)
  {
    if (attributeNameValuePairs.length % 2 != 0)
    {
      throw new IllegalArgumentException("Must specify pairs");
    }

    StringBuilder s = new StringBuilder();
    int nameValuePairOffset = 0;

    while (nameValuePairOffset < attributeNameValuePairs.length)
    {
      s.append(" ");
      s.append(attributeNameValuePairs[nameValuePairOffset++]);
      s.append("='");
      s.append(attributeNameValuePairs[nameValuePairOffset++]);
      s.append("'");
    }

    return s.toString();
  }

  public String getContentClassName(Id contentId)
  {
    return HtmlConstants.SE_MC_CONTENT + contentId.formatString();
  }

  public String getElementId(int contentCounter)
  {
    return HtmlConstants.SE_ID_HTML + contentCounter;
  }

  public void writeBegin(String tag, int styleId, String className, Id contentId)
  {
    String elementId = getElementId(styleId);
    writeBegin(tag, "id", elementId, "class", className + " " + getContentClassName(contentId));
  }

  public void writeBegin(String tag, String... attributeNameValuePairs)
  {
    writeText("<" + tag + getAttributeSpecification(attributeNameValuePairs) + ">");
  }

  public void writeBegin(String tag, String className, Id contentId)
  {
    writeBegin(tag, "class", className + " " + getContentClassName(contentId));
  }

  public void writeEnd(String tag)
  {
    writeText("</" + tag + ">");
  }

  public void writeHeading(int level, String title)
  {
    writeText("<h" + level + " class='" + HtmlConstants.SE_MC_HEADING + "'>" + title + "</h" + level + ">");
  }

  public void writeImage(String src, String alt)
  {
    writeText("<img src='" + src + "' alt='" + alt + "'/>");
  }

  private void writeLine(String text)
  {
    printWriter.print(text);
    lineSeparatorRequired = true;
  }

  private void writeLineSeparator()
  {
    printWriter.println();
    lineSeparatorRequired = false;
  }

  public void writeTag(String tag, String content, String... attributeNameValuePairs)
  {
    writeText("<" + tag + getAttributeSpecification(attributeNameValuePairs) + ">" + content + "</" + tag + ">");
  }

  public void writeText(String text)
  {
    if (lineSeparatorRequired)
    {
      writeLineSeparator();
    }
    writeLine(text);
  }
}