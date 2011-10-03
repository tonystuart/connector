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

import java.util.HashMap;
import java.util.Map;

import com.semanticexpression.connector.server.ParameterSubstituter;

public class SubstituterTest
{
  public static void main(String[] args)
  {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("test1", "Test 1");
    parameters.put(" test2 ", "Test 2");
    parameters.put("test3", " ");
    parameters.put("test4", "{{Test 4}}");

    ParameterSubstituter parameterSubstituter = new ParameterSubstituter();
    System.out.println(parameterSubstituter.substitute(parameters, "Hello world"));
    System.out.println(parameterSubstituter.substitute(parameters, "{{test1}}Hello world"));
    System.out.println(parameterSubstituter.substitute(parameters, "Hello world{{test1}}"));
    System.out.println(parameterSubstituter.substitute(parameters, "Hello {{test1}} world"));
    System.out.println(parameterSubstituter.substitute(parameters, "{test1}}Hello world"));
    System.out.println(parameterSubstituter.substitute(parameters, "{{test1}Hello world"));
    System.out.println(parameterSubstituter.substitute(parameters, "Hello world{{test1}"));
    System.out.println(parameterSubstituter.substitute(parameters, "Hello world{ {test1}}"));
    System.out.println(parameterSubstituter.substitute(parameters, "Hello world{{test1} }"));
    System.out.println(parameterSubstituter.substitute(parameters, "{{test1}}{{ test2 }}{{test3}}"));
    System.out.println(parameterSubstituter.substitute(parameters, "{{test4}}"));
  }


}
