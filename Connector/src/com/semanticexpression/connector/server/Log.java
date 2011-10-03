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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Log
{
  private static boolean isLogDate = true;
  private static boolean isLogThread = true;
  private static boolean isLogTime = true;
  private static boolean isLogType = true;

  public static final int LOG_0_FATAL = 0;
  public static final int LOG_1_ERROR = 1;
  public static final int LOG_2_WARN = 2;
  public static final int LOG_3_INFO = 3;
  public static final int LOG_4_DEBUG = 4;
  public static final int LOG_5_TRACE = 5;

  public static final String LOG_PATTERN_ENABLE = "logPatternEnable";
  public static final String LOG_PATTERN_DISABLE = "logPatternDisable";

  private static String logFileName = null;
  private static int logLevel = LOG_3_INFO;

  private static LogPattern[] logPatterns;
  public static final String SIMPLE_DATE_FORMAT_DATE = "yyyy-MM-dd";

  public static final String SIMPLE_DATE_FORMAT_TIME = "HH:mm:ss.SSS";
  private static SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat(SIMPLE_DATE_FORMAT_DATE);
  private static SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat(SIMPLE_DATE_FORMAT_TIME);

  private static void appendFile(String message)
  {
    try
    {
      BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFileName, true));
      try
      {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
      }
      finally
      {
        bufferedWriter.close();
      }
    }
    catch (Exception e)
    {
      System.err.println("Cannot write to log file, logFileName=" + logFileName + ", e=" + e);
      System.err.println(message);
    }
  }

  public static void debug(String template, Object... arguments)
  {
    if (logLevel >= LOG_4_DEBUG)
    {
      logPattern('D', template, arguments);
    }
  }

  public static void enter(String template, Object... arguments)
  {
    if (logLevel >= LOG_5_TRACE)
    {
      logPattern('>', template, arguments);
    }
  }

  public static void error(String template, Object... arguments)
  {
    if (logLevel >= LOG_1_ERROR)
    {
      log('E', template, arguments);
    }
  }

  public static RuntimeException exception(RuntimeException runtimeException)
  {
    fatal("A runtimeException occurred, e=" + runtimeException);
    return runtimeException;
  }

  public static void exit(String template, Object... arguments)
  {
    if (logLevel >= LOG_5_TRACE)
    {
      logPattern('<', template, arguments);
    }
  }

  public static void fatal(String template, Object... arguments)
  {
    log('F', template, arguments);
  }

  private static String format(char type, String template, Object... arguments)
  {
    Date date = null;
    StringBuilder s = new StringBuilder();

    for (int i = 0; i < arguments.length; i++)
    {
      Object argument = arguments[i];
      if (argument instanceof Throwable)
      {
        Throwable throwable = (Throwable)argument;
        String stackTrace = formatStackTrace(throwable);
        arguments[i] = stackTrace;
      }
    }

    if (isLogDate)
    {
      date = new Date();
      s.append(simpleDateFormatDate.format(date));
      s.append(" ");
    }

    if (isLogTime)
    {
      if (date == null)
      {
        date = new Date();
      }
      s.append(simpleDateFormatTime.format(date));
      s.append(" ");
    }

    if (isLogType)
    {
      s.append(type);
      s.append(" ");
    }

    if (isLogThread)
    {
      s.append(Thread.currentThread().getName());
      s.append(" ");
    }

    if (s.length() > 0)
    {
      s.append(": ");
    }

    s.append(String.format(template, arguments));
    String logMessage = s.toString();
    return logMessage;
  }

  private static String formatStackTrace(Throwable throwable)
  {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    printWriter.close();
    String stackTrace = stringWriter.toString();
    return stackTrace;
  }

  public static String getLogFileName()
  {
    return logFileName;
  }

  public static int getLogLevel()
  {
    return logLevel;
  }

  private static String getPath(File file)
  {
    try
    {
      return file.getCanonicalPath();
    }
    catch (IOException e)
    {
      return file.getPath();
    }
  }

  public static SimpleDateFormat getSimpleDateFormatDate()
  {
    return simpleDateFormatDate;
  }

  public static SimpleDateFormat getSimpleDateFormatTime()
  {
    return simpleDateFormatTime;
  }

  public static void info(String template, Object... arguments)
  {
    if (logLevel >= LOG_3_INFO)
    {
      log('I', template, arguments);
    }
  }

  public static boolean isLogDate()
  {
    return isLogDate;
  }

  public static boolean isLogged(int level)
  {
    return Log.logLevel >= level;
  }

  public static boolean isLogThread()
  {
    return isLogThread;
  }

  public static boolean isLogTime()
  {
    return isLogTime;
  }

  public static boolean isLogType()
  {
    return isLogType;
  }

  private static void log(char type, String template, Object... arguments)
  {
    String logMessage = format(type, template, arguments);
    write(logMessage);
  }

  private static void logPattern(char type, String template, Object... arguments)
  {
    String logMessage = format(type, template, arguments);
    for (LogPattern logPattern : logPatterns)
    {
      if (logPattern.matches(logMessage))
      {
        if (logPattern.isEnabled())
        {
          write(logMessage);
        }
        return;
      }
    }
  }

  public static void setLogDate(boolean isLogDate)
  {
    Log.isLogDate = isLogDate;
  }

  public static void setLogFileName(String logFileName)
  {
    if (logFileName != null && logFileName.length() == 0)
    {
      logFileName = null;
    }
    Log.logFileName = logFileName;
    if (logFileName == null)
    {
      info("Log.setLogFileName: Logging to System.out");
    }
    else
    {
      File logFile = new File(logFileName);
      String parent = logFile.getParent();
      if (parent != null)
      {
        File parentFile = new File(parent);
        parentFile.mkdirs();
      }
      info("Log.setLogFileName: Logging to " + getPath(logFile));
    }
  }

  public static void setLogLevel(int logLevel)
  {
    if (logLevel > LOG_5_TRACE)
    {
      throw new IllegalArgumentException("logLevel > " + LOG_5_TRACE);
    }
    Log.logLevel = logLevel;
    log('L', "Log.setLogLevel: logLevel=%d", logLevel);
  }

  public static void setLogPatterns(LinkedHashMap<String, String> logPatternProperties)
  {
    int patternOffset = 0;
    int patternCount = logPatternProperties.size();
    logPatterns = new LogPattern[patternCount];
    for (Entry<String, String> entry : logPatternProperties.entrySet())
    {
      String name = entry.getKey();
      String value = entry.getValue();
      boolean isEnabled = name.startsWith(LOG_PATTERN_ENABLE);
      if (!isEnabled && !name.startsWith(LOG_PATTERN_DISABLE))
      {
        throw new IllegalArgumentException("Log property name must begin with " + LOG_PATTERN_ENABLE + " or " + LOG_PATTERN_DISABLE);
      }
      Log.info("Log.setLogPatterns: name=%s value=%s", name, value);
      logPatterns[patternOffset++] = new LogPattern(isEnabled, value);
    }
  }

  public static void setLogThread(boolean isLogThread)
  {
    Log.isLogThread = isLogThread;
  }

  public static void setLogTime(boolean isLogTime)
  {
    Log.isLogTime = isLogTime;
  }

  public static void setLogType(boolean isLogLevel)
  {
    Log.isLogType = isLogLevel;
  }

  public static void setSimpleDateFormatDate(SimpleDateFormat simpleDateFormatDate)
  {
    Log.simpleDateFormatDate = simpleDateFormatDate;
  }

  public static void setSimpleDateFormatTime(SimpleDateFormat simpleDateFormatTime)
  {
    Log.simpleDateFormatTime = simpleDateFormatTime;
  }

  public static void warn(String template, Object... arguments)
  {
    if (logLevel >= LOG_2_WARN)
    {
      log('W', template, arguments);
    }
  }

  private static void write(String logMessage)
  {
    if (logFileName == null)
    {
      System.out.println(logMessage);
    }
    else
    {
      appendFile(logMessage);
    }
  }

  public static class LogPattern
  {
    private boolean isEnabled;
    private Pattern pattern;

    public LogPattern(boolean isEnabled, String regularExpression)
    {
      this.isEnabled = isEnabled;
      pattern = Pattern.compile(regularExpression);
    }

    public boolean isEnabled()
    {
      return isEnabled;
    }

    public boolean matches(String logMessage)
    {
      return pattern.matcher(logMessage).matches();
    }

  }

}
