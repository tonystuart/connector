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

package com.semanticexpression.connector.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SiteBuilderExtendedClassic
{
  private static final String HTML = ".html";

  public static void main(String[] args) throws Exception
  {
    String input = "input";
    String output = "output";
    String sequenceFileName = ".sequence";
    String templateFileName = "template.html";

    for (String arg : args)
    {
      String[] pairs = arg.split("=");
      String name = pairs[0];
      String value = pairs.length == 1 ? null : pairs[1];
      if (name.equalsIgnoreCase("--input"))
      {
        input = value;
      }
      else if (name.equalsIgnoreCase("--output"))
      {
        output = value;
      }
      else if (name.equalsIgnoreCase("--sequence"))
      {
        sequenceFileName = value;
      }
      else if (name.equalsIgnoreCase("--template"))
      {
        templateFileName = value;
      }
      else
      {
        System.err.println("Usage: SiteBuilder <options>");
        System.err.println("Options: --input=p --output=p --sequence=f --template=p");
        System.err.println("Where: p is a path name and f is a file name");
        System.exit(1);
      }
    }

    SiteBuilderExtendedClassic siteBuilderExtendedClassic = new SiteBuilderExtendedClassic();
    siteBuilderExtendedClassic.buildSite(input, output, sequenceFileName, templateFileName);
  }

  private void buildSite(File inputDirectory, File outputDirectory, String sequenceFileName, String template, int level) throws Exception
  {
    outputDirectory.mkdirs();
    String[] sequence = getSequence(inputDirectory, sequenceFileName);
    File[] inputFiles = inputDirectory.listFiles();
    for (File inputFile : inputFiles)
    {
      String inputFileName = inputFile.getName();
      String outputPathName = createPath(outputDirectory, inputFileName);
      File outputFile = new File(outputPathName);

      if (inputFile.isDirectory())
      {
        buildSite(inputFile, outputFile, sequenceFileName, template, level + 1);
      }
      else if (isContained(sequence, inputFileName))
      {
        formatFile(inputFile, outputFile, sequenceFileName, template, level);
      }
      else if (inputFileName.equals(sequenceFileName))
      {
        // do nothing
      }
      else
      {
        copyFile(inputFile, outputFile);
      }
    }

  }

  private void buildSite(String inputDirectoryName, String outputDirectoryName, String sequenceFileName, String templateFileName) throws Exception
  {
    String template = readFile(templateFileName);
    buildSite(new File(inputDirectoryName), new File(outputDirectoryName), sequenceFileName, template, 0);
  }

  private void copyFile(File inputFile, File outputFile) throws Exception
  {
    InputStream inputStream = new FileInputStream(inputFile);
    try
    {
      OutputStream outputStream = new FileOutputStream(outputFile);
      try
      {
        int rc;
        byte[] buffer = new byte[32768];
        while ((rc = inputStream.read(buffer)) > 0)
        {
          outputStream.write(buffer, 0, rc);
        }
      }
      finally
      {
        outputStream.close();
      }
    }
    finally
    {
      inputStream.close();
    }
  }

  private String createPath(File directory, String fileName)
  {
    return directory.getPath() + "/" + fileName;
  }

  private void formatFile(File file, File outputFile, String sequenceFileName, String template, int level) throws Exception
  {
    String content = readFile(file);
    int delimiter = content.indexOf('\n');
    String title = content.substring(0, delimiter);
    content = content.substring(delimiter + 1);
    String relativeRoot = getRelativeRoot(level);
    String navigation = formatNavigation(file, null, sequenceFileName, level, 0);

    String newContent = template;
    newContent = newContent.replace("{{title}}", title);
    newContent = newContent.replace("{{navigation}}", navigation);
    newContent = newContent.replace("{{content}}", content);
    newContent = newContent.replace("{{relative-root}}", relativeRoot);

    writeFile(outputFile, newContent);
  }

  private String formatNavigation(File file, String navigation, String sequenceFileName, int level, int distance) throws Exception
  {
    if (level < 0)
    {
      return navigation;
    }

    String baseName = file.getName();
    if (baseName.endsWith(HTML))
    {
      baseName = baseName.substring(0, baseName.length() - HTML.length());
    }

    StringBuilder s = new StringBuilder();
    s.append("<ul>\n");

    File parentFile = file.getParentFile();
    String[] sequence = getSequence(parentFile, sequenceFileName);

    for (String name : sequence)
    {
      s.append("<li>\n");
      String htmlFile = name + HTML;
      String relativePath = getRelativePath(htmlFile, distance);
      String title = getTitle(parentFile, htmlFile);
      if (name.equals(baseName))
      {
        if (navigation == null)
        {
          formatNavigationAnchor(s, relativePath, title, true);
          File directoryFile = new File(createPath(parentFile, name));
          formatNavigationChildren(s, name, directoryFile, sequenceFileName);
        }
        else
        {
          formatNavigationAnchor(s, relativePath, title, false);
          s.append(navigation);
        }
      }
      else
      {
        formatNavigationAnchor(s, relativePath, title, false);
      }
      s.append("</li>\n");
    }

    s.append("</ul>\n");
    String parentNavigation = formatNavigation(parentFile, s.toString(), sequenceFileName, level - 1, distance + 1);
    return parentNavigation;
  }

  private void formatNavigationAnchor(StringBuilder s, String relativePath, String title, boolean isCurrentPage)
  {
    s.append("<a href='");
    s.append(relativePath);
    s.append("'");

    if (isCurrentPage)
    {
      s.append(" class='current_page'");
    }

    s.append(">");
    s.append(title);
    s.append("</a>");
  }

  private void formatNavigationChildren(StringBuilder s, String name, File directoryFile, String sequenceFileName) throws Exception
  {
    String[] childNames = getSequence(directoryFile, sequenceFileName);
    if (childNames.length > 0)
    {
      s.append("<ul>");
      for (String childName : childNames)
      {
        String htmlName = childName + HTML;
        String title = getTitle(directoryFile, htmlName);
        String relativePath = name + "/" + htmlName; // relative to current directory
        s.append("<li>");
        formatNavigationAnchor(s, relativePath, title, false);
        s.append("</li>");
      }
      s.append("</ul>");
    }
  }

  private int getLength(File file)
  {
    long length = file.length();
    if (length >= Integer.MAX_VALUE)
    {
      throw new IllegalArgumentException(file.getName() + " is too large");
    }
    return (int)length;
  }

  private String getRelativePath(String name, int distance)
  {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < distance; i++)
    {
      s.append("../");
    }
    s.append(name);
    return s.toString();
  }

  private String getRelativeRoot(int level)
  {
    StringBuilder s = new StringBuilder();
    if (level == 0)
    {
      s.append(".");
    }
    else
    {
      for (int i = 0; i < level; i++)
      {
        if (i > 0)
        {
          s.append("/");
        }
        s.append("..");
      }
    }
    return s.toString();
  }

  private String[] getSequence(File inputDirectory, String sequenceFileName) throws Exception
  {
    String[] sequence = new String[0];
    String path = createPath(inputDirectory, sequenceFileName);
    File file = new File(path);
    if (file.canRead())
    {
      String contents = readFile(file);
      sequence = contents.split("\\n");
    }
    return sequence;
  }

  private String getTitle(File directoryFile, String htmlName) throws Exception
  {
    String path = createPath(directoryFile, htmlName);
    String content = readFile(path);
    int delimiter = content.indexOf('\n');
    String title = content.substring(0, delimiter);
    return title;
  }

  private boolean isContained(String[] sequence, String inputFileName)
  {
    for (String name : sequence)
    {
      if (inputFileName.equals(name + HTML))
      {
        return true;
      }
    }
    return false;
  }

  private String readFile(File file) throws Exception
  {
    int length = getLength(file);
    byte[] buffer = new byte[length];
    InputStream inputStream = new FileInputStream(file);
    try
    {
      inputStream.read(buffer);
    }
    finally
    {
      inputStream.close();
    }
    String rawContents = new String(buffer);
    String normalizedContents = rawContents.replace("\r\n", "\n");
    return normalizedContents;
  }

  private String readFile(String fileName) throws Exception
  {
    File file = new File(fileName);
    String contents = readFile(file);
    return contents;
  }

  private void writeFile(File file, String content) throws Exception
  {
    OutputStream outputStream = new FileOutputStream(file);
    try
    {
      outputStream.write(content.getBytes());
    }
    finally
    {
      outputStream.close();
    }
  }

}
