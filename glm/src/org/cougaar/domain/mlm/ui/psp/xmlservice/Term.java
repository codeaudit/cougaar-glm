/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.xmlservice;

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
