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

package com.semanticexpression.connector.client.frame.editor.table;

import com.extjs.gxt.ui.client.store.ListStore;
import com.semanticexpression.connector.client.Utility;
import com.semanticexpression.connector.shared.Association;

/**
 * <pre>
 * expression =
 *     simple-expression
 *     | "-" expression .
 *     
 * simple-expression = term {("+"|"-") term} .
 * 
 * term = factor {("*"|"/") factor} .
 * 
 * factor =
 *     name
 *     | number
 *     | "(" expression ")" .
 *     
 * name =
 *     row-name-cell-reference
 *     | function-name "(" argument {"," argument} ")" .
 *     
 * argument =
 *     row-reference
 *     | expression .
 * 
 * row-reference =
 *     row-name-cell-reference
 *     | row-number-cell-reference .
 * 
 * </pre>
 * 
 * Example:
 * 
 * <pre>
 * (Sales + Development + Support + General) / 4
 * </pre>
 */

public final class Evaluator
{
  private String columnKey;
  private ListStore<Association> listStore;
  private String rowKey;
  private Scanner scanner;

  public Evaluator(String formula, ListStore<Association> listStore, String rowKey, String columnKey)
  {
    this.scanner = new Scanner(formula);
    this.listStore = listStore;
    this.rowKey = rowKey;
    this.columnKey = columnKey;
  }

  public double evaluateExpression()
  {
    double left;
    String operator = getOperator("-");
    if (operator == null)
    {
      left = evaluateSimpleExpression();
    }
    else
    {
      left = -evaluateExpression();
    }

    return left;
  }

  private double evaluateFactor()
  {
    double value;
    String token = scanner.getNext();
    if (isName(token))
    {
      String lookahead = scanner.getNext();
      if (lookahead.equals("("))
      {
        value = evaluateFunctionReference(token);
        expect(")");
      }
      else
      {
        scanner.retract();
        value = evaluateRowNameCellReference(token);
      }
    }
    else if (token.equals("("))
    {
      value = evaluateExpression();
      expect(")");
    }
    else
    {
      value = Utility.extractDouble(token);
    }
    return value;
  }

  private double evaluateFunctionReference(String token)
  {
    Visitor visitor = null;
    if (token.equalsIgnoreCase("avg"))
    {
      visitor = new AvgVisitor();
    }
    else if (token.equalsIgnoreCase("max"))
    {
      visitor = new MaxVisitor();
    }
    else if (token.equalsIgnoreCase("min"))
    {
      visitor = new MinVisitor();
    }
    else if (token.equalsIgnoreCase("sum"))
    {
      visitor = new SumVisitor();
    }
    else
    {
      throw new IllegalArgumentException("Expected function: avg, max, min, sum, got " + token);
    }
    visitRowRange(visitor);
    return visitor.getValue();
  }

  private double evaluateRowNameCellReference(String token)
  {
    int rowCount = listStore.getCount();
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      Association row = listStore.getAt(rowOffset);
      String rowName = row.get(rowKey);
      if (rowName.equalsIgnoreCase(token))
      {
        return getDouble(row.get(columnKey));
      }
    }
    throw new IllegalArgumentException("Cannot find row " + token);
  }

  private double evaluateSimpleExpression()
  {
    double left = evaluateTerm();

    String operator;

    while ((operator = getOperator("+", "-")) != null)
    {
      double right = evaluateTerm();

      if (operator.equals("+"))
      {
        left += right;
      }
      else if (operator.equals("-"))
      {
        left -= right;
      }
    }

    return left;
  }

  private double evaluateTerm()
  {
    double left = evaluateFactor();

    String operator;

    while ((operator = getOperator("*", "/")) != null)
    {
      double right = evaluateFactor();

      if (operator.equals("*"))
      {
        left *= right;
      }
      else if (operator.equals("/"))
      {
        left /= right;
      }
    }

    return left;
  }

  private void expect(String expectedToken)
  {
    String token = scanner.getNext();
    if (!token.equals(expectedToken))
    {
      throw new IllegalArgumentException("Expected " + expectedToken + ", got " + token);
    }
  }

  public double getDouble(Object objectValue)
  {
    double doubleValue;
    if (objectValue instanceof Number)
    {
      doubleValue = ((Number)objectValue).doubleValue();
    }
    else
    {
      doubleValue = Double.parseDouble(objectValue.toString());
    }
    return doubleValue;
  }

  private int getNumber(String token)
  {
    try
    {
      return Integer.parseInt(token);
    }
    catch (NumberFormatException e)
    {
      throw new IllegalArgumentException("Expected number, got " + token);
    }
  }

  private String getOperator(String... operators)
  {
    String token = scanner.getNext();
    for (String operator : operators)
    {
      if (token.equals(operator))
      {
        return operator;
      }
    }
    scanner.retract();
    return null;
  }

  private boolean isName(String token)
  {
    return token.length() > 0 && Utility.isAlpha(token.charAt(0));
  }

  private void visitRowRange(Visitor visitor)
  {
    boolean isInRange = false;

    Range range = new Range();
    RangeResult rangeResult = RangeResult.TO;

    int rowCount = listStore.getCount();
    visitor.initialize(rowCount);

    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      Association row = listStore.getAt(rowOffset);
      rangeResult = range.isInRange(row, rowOffset);
      switch (rangeResult)
      {
        case FROM:
          visitor.visit(row, rowOffset);
          isInRange = true;
          break;
        case FROM_AND_TO:
          visitor.visit(row, rowOffset);
          isInRange = false;
          break;
        case TO:
          if (isInRange)
          {
            visitor.visit(row, rowOffset);
            isInRange = false;
          }
          break;
        case NONE:
          if (isInRange)
          {
            visitor.visit(row, rowOffset);
          }
          break;
      }
    }
  }

  private class AvgVisitor implements Visitor
  {
    int count;
    double sum;

    public double getValue()
    {
      return count == 0 ? 0 : sum / count;
    }

    @Override
    public void initialize(int maxRowCount)
    {
      count = 0;
      sum = 0;
    }

    @Override
    public void visit(Association row, int rowOffset)
    {
      count++;
      sum += getDouble(row.get(columnKey));
    }
  }

  private class MaxVisitor implements Visitor
  {
    double max;

    public double getValue()
    {
      return max;
    }

    @Override
    public void initialize(int maxRowCount)
    {
      max = Double.MIN_NORMAL;
    }

    @Override
    public void visit(Association row, int rowOffset)
    {
      max = Math.max(max, getDouble(row.get(columnKey)));
    }
  }

  private class MinVisitor implements Visitor
  {
    double min;

    public double getValue()
    {
      return min;
    }

    @Override
    public void initialize(int maxRowCount)
    {
      min = Double.MAX_VALUE;
    }

    @Override
    public void visit(Association row, int rowOffset)
    {
      min = Math.min(min, getDouble(row.get(columnKey)));
    }
  }

  private class Range
  {
    private RowReference from;
    private RowReference to;

    private Range()
    {
      from = new RowReference();
      expect(",");
      to = new RowReference();
    }

    private RangeResult isInRange(Association row, int rowOffset)
    {
      boolean isFrom = from.isMatch(row, rowOffset);
      boolean isTo = to.isMatch(row, rowOffset);
      return isFrom ? (isTo ? RangeResult.FROM_AND_TO : RangeResult.FROM) : isTo ? RangeResult.TO : RangeResult.NONE;
    }
  }

  private enum RangeResult
  {
    FROM, FROM_AND_TO, NONE, TO
  }

  private class RowReference
  {
    private String name;
    private int offset;

    private RowReference()
    {
      String fromName = scanner.getNext();
      if (isName(fromName))
      {
        this.name = fromName;
      }
      else
      {
        offset = getNumber(fromName) - 1; // -1 because user specifies number, not offset
      }
    }

    public boolean isMatch(Association row, int rowOffset)
    {
      return name == null ? (rowOffset == offset) : name.equalsIgnoreCase(row.<String> get(rowKey));
    }
  }

  public class Scanner
  {
    private int base;
    private int current;
    private String formula;

    public Scanner(String formula)
    {
      this.formula = formula;
    }

    public String getNext()
    {
      base = current;
      int length = formula.length();
      State state = State.INITIAL;
      StringBuilder s = new StringBuilder();
      char quote = 0;

      while (current < length && state != State.FINAL)
      {
        char c = formula.charAt(current);
        switch (state)
        {
          case INITIAL:
            if (isOperator(c))
            {
              s.append(c);
              state = State.FINAL;
            }
            else if (Utility.isAlpha(c))
            {
              s.append(c);
              state = State.IDENTIFIER;
            }
            else if (c == '.' || Utility.isDigit(c))
            {
              s.append(c);
              state = State.NUMBER;
            }
            else if (c == '\'')
            {
              quote = c;
              state = State.QUOTE;
            }
            else if (!Utility.isWhitespace(c))
            {
              throw new IllegalArgumentException("Unexpected character " + c);
            }
            break;
          case IDENTIFIER:
            if (Utility.isWhitespace(c))
            {
              state = State.FINAL;
            }
            else if (isOperator(c))
            {
              current--;
              state = State.FINAL;
            }
            else
            {
              s.append(c);
            }
            break;
          case NUMBER:
            if (c == '.' || Utility.isDigit(c))
            {
              s.append(c);
            }
            else
            {
              current--;
              state = State.FINAL;
            }
            break;
          case QUOTE:
            if (c == quote)
            {
              state = State.FINAL;
            }
            else
            {
              s.append(c);
            }
            break;
        }
        current++;
      }
      return s.toString();
    }

    private boolean isOperator(char c)
    {
      return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' || c == ',';
    }

    public void retract()
    {
      current = base;
    }
  }

  public enum State
  {
    FINAL, IDENTIFIER, INITIAL, NUMBER, QUOTE

  }

  private class SumVisitor implements Visitor
  {
    double sum;

    public double getValue()
    {
      return sum;
    }

    @Override
    public void initialize(int maxRowCount)
    {
      sum = 0;
    }

    @Override
    public void visit(Association row, int rowOffset)
    {
      sum += getDouble(row.get(columnKey));
    }
  }

  private interface Visitor
  {
    public double getValue();

    public void initialize(int maxRowCount);

    public void visit(Association row, int rowOffset);
  }
}
