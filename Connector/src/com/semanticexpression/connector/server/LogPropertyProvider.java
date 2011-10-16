package com.semanticexpression.connector.server;

import java.util.LinkedHashMap;

public interface LogPropertyProvider
{

  public int getLogLevel();

  public String getLogPathName();

  public LinkedHashMap<String, String> getLogPatterns();

  public boolean isLogPropertyInitializationRequired();

}
