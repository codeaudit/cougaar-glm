/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.mlm.ui.psp.xmlservice;

import java.lang.reflect.Method;
import java.util.Vector;

/*
  Creates "terms" consisting of name (command or log object), property values,
  comparator, object value, and combiner.
  Used by the XML PlanServiceProvider in parsing the request
  from the UI client and comparing terms to plan service objects.
  */

public class Term {
  String name;
  Class requestedClass;
  Method commandMethod; // method to execute the command requested
  Vector propertyNames;
  String comparator;
  String value;
  String combiner;
  Vector requestedFields; // string vector; each string is a class name followed by a dot separated property list, i.e. Task.verb

  public Term(String name) {
    this.name = name;
  }

  public String toString() {
    String s = "";
    if (name != null) 
      s = s + name;
    if (propertyNames != null) 
      for (int i = 0; i < propertyNames.size(); i++)
	s = s + "." + propertyNames.elementAt(i);
    if (comparator != null)
       s = s + comparator;
    if (value != null)
       s = s + value;
    if (combiner != null)
       s = s + combiner;
    if (requestedFields != null) {
      s = s + ":";
      int n = requestedFields.size();
      for (int i = 0; i < n-1; i++)
	s = s + requestedFields.elementAt(i) + ",";
      s = s + requestedFields.elementAt(n-1);
    }
    return s;
  }

}
