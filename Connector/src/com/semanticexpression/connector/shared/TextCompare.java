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

package com.semanticexpression.connector.shared;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.semanticexpression.connector.client.Utility;

/**
 * See <a href=
 * 'http://en.wikipedia.org/wiki/Longest_common_subsequence_problem'>Longest
 * Common Subsequence Problem</a>.
 */
public class TextCompare
{
  private int styleCounter;

  public TextCompare(int styleCounter)
  {
    this.styleCounter = styleCounter;
  }

  public String compare(String oldText, String newText)
  {
    Map<Integer, String> tags = new HashMap<Integer, String>();
    String[] oldWords = parse(oldText, null);
    String[] newWords = parse(newText, tags);
    return compare(oldWords, newWords, tags);
  }

  public String compare(String[] oldWords, String[] newWords, Map<Integer, String> tags)
  {
    StringBuilder s = new StringBuilder();

    int oldWordCount = oldWords.length;
    int newWordCount = newWords.length;

    int[][] sequenceLengths = new int[oldWordCount + 1][newWordCount + 1];

    for (int oldWordOffset = oldWordCount - 1; oldWordOffset >= 0; oldWordOffset--)
    {
      for (int newWordOffset = newWordCount - 1; newWordOffset >= 0; newWordOffset--)
      {
        if (oldWords[oldWordOffset].equals(newWords[newWordOffset]))
        {
          sequenceLengths[oldWordOffset][newWordOffset] = sequenceLengths[oldWordOffset + 1][newWordOffset + 1] + 1;
        }
        else
        {
          int nextOldSequenceLength = sequenceLengths[oldWordOffset + 1][newWordOffset];
          int nextNewSequenceLength = sequenceLengths[oldWordOffset][newWordOffset + 1];
          sequenceLengths[oldWordOffset][newWordOffset] = Math.max(nextOldSequenceLength, nextNewSequenceLength);
        }
      }
    }

    styleCounter = 0;

    State oldState = State.UNCHANGED;
    State newState = State.UNCHANGED;

    int oldWordOffset = 0;
    int newWordOffset = 0;
    int lastNewWordOffset = -1;

    while (oldWordOffset < oldWordCount || newWordOffset < newWordCount)
    {
      if (oldWordOffset < oldWordCount && newWordOffset == newWordCount)
      {
        newState = State.DELETED;
      }
      else if (oldWordOffset == oldWordCount && newWordOffset < newWordCount)
      {
        newState = State.INSERTED;
      }
      else if (oldWords[oldWordOffset].equals(newWords[newWordOffset]))
      {
        newState = State.UNCHANGED;
      }
      else
      {
        int nextOldSequenceLength = sequenceLengths[oldWordOffset + 1][newWordOffset];
        int nextNewSequenceLength = sequenceLengths[oldWordOffset][newWordOffset + 1];

        if (nextOldSequenceLength >= nextNewSequenceLength)
        {
          newState = State.DELETED;
        }
        else
        {
          newState = State.INSERTED;
        }
      }

      if (s.length() > 0)
      {
        s.append(" ");
      }

      String tag = null;
      if (tags != null)
      {
        if (newWordOffset != lastNewWordOffset)
        {
          tag = tags.get(newWordOffset);
          lastNewWordOffset = newWordOffset;
        }
      }

      if (isTagBreak(oldState, newState, tag))
      {
        s.append("</span>");
        oldState = State.UNCHANGED;
      }

      if (tag != null)
      {
        s.append(tag);
      }

      switch (newState)
      {
        case DELETED:
          switch (oldState)
          {
            case INSERTED:
            case UNCHANGED:
              s.append("<span id='" + getId() + "' class='deleted'>");
              break;
          }
          s.append(oldWords[oldWordOffset++]);
          break;
        case INSERTED:
          switch (oldState)
          {
            case DELETED:
            case UNCHANGED:
              s.append("<span id='" + getId() + "' class='inserted'>");
              break;
          }
          s.append(newWords[newWordOffset++]);
          break;
        case UNCHANGED:
          s.append(oldWords[oldWordOffset]);
          oldWordOffset++;
          newWordOffset++;
          break;
      }

      oldState = newState;
    }

    String tag = tags.get(lastNewWordOffset + 1);
    if (tag != null)
    {
      s.append(tag);
    }

    return s.toString();
  }

  public String getId()
  {
    return HtmlConstants.SE_ID_STYLE + styleCounter++;
  }

  private boolean isTagBreak(State oldState, State newState, String tag)
  {
    if (newState == State.DELETED && oldState == State.INSERTED)
    {
      return true;
    }

    if (newState == State.INSERTED && oldState == State.DELETED)
    {
      return true;
    }

    if (newState == State.UNCHANGED && (oldState == State.DELETED || oldState == State.INSERTED))
    {
      return true;
    }

    if (tag != null && (oldState == State.DELETED || oldState == State.INSERTED))
    {
      return true;
    }

    return false;
  }

  public String[] parse(String text, Map<Integer, String> tags)
  {
    ParserState parserState = ParserState.INITIAL;
    StringBuilder token = null;
    List<String> tokens = new LinkedList<String>();
    int length = text.length();
    for (int textOffset = 0; textOffset < length; textOffset++)
    {
      char c = text.charAt(textOffset);
      switch (parserState)
      {
        case INITIAL:
          if (Utility.isWhitespace(c))
          {
            if (token != null)
            {
              tokens.add(token.toString());
              token = null;
            }
          }
          else if (c == '<')
          {
            if (token != null)
            {
              tokens.add(token.toString());
            }
            token = new StringBuilder();
            token.append(c);
            parserState = ParserState.TAG;
          }
          else
          {
            if (token == null)
            {
              token = new StringBuilder();
            }
            token.append(c);
          }
          break;
        case TAG:
          token.append(c);
          if (c == '>')
          {
            if (tags != null)
            {
              int offset = tokens.size();
              String tagValue = tags.get(offset);
              if (tagValue == null)
              {
                tagValue = token.toString();
              }
              else
              {
                tagValue += token.toString();
              }
              tags.put(offset, tagValue);
            }
            token = null;
            parserState = ParserState.INITIAL;
          }
          break;
      }
    }

    if (token != null)
    {
      tokens.add(token.toString());
    }

    String[] tokenArray = tokens.toArray(new String[tokens.size()]);

    return tokenArray;
  }

  public enum ParserState
  {
    INITIAL, TAG

  }

  public enum State
  {
    DELETED, INSERTED, UNCHANGED
  }
}
