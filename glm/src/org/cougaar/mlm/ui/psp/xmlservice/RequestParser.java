/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import java.beans.*;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.cougaar.lib.planserver.RuntimePSPException;

/* This class is used by the plan service provider to parse
   user requests.
   This parses the following grammar:

   Query = Command | LogPlanObjectName | Term | ComplexTerm | <empty>
   Command = "LIST_CLUSTERS"
   LogPlanObjectName = "Aggregation" | "Allocation" | "Asset" | 
       "AssetTransfer" | "Expansion" | "Disposition" | 
       "PlanElement" | "Task" | "Workflow" | "Policy"
   Term = LogPlanObjectPropertyName Comparator Value |
          PropertyName Comparator Value
   LogPlanObjectPropertyName = LogPlanObjectName.PropertyName | 
        LogPlanObjectPropertyName.PropertyName
   Comparator = < | > | = | != | <= | >=
   ComplexTerm = Term | Term Combiner Term
   Combiner = "|" | "&"

   NOTE: Expanded grammar to allow terms with property names but no
   log plan object name.  If log plan object names are specified in the
   terms, then all terms must specify the same log plan object name.
   */

public class RequestParser {
  static MethodDescriptor[] commandMethods = null;
  static Class[] logPlanClasses = null;
  // classNames and logPlanObjectNames must match
  static String[] classNames = { "org.cougaar.planning.ldm.plan.Aggregation", 
        "org.cougaar.planning.ldm.plan.Allocation", 
        "org.cougaar.planning.ldm.plan.Disposition",
        "org.cougaar.planning.ldm.asset.Asset",
        "org.cougaar.planning.ldm.plan.AssetTransfer", 
        "org.cougaar.planning.ldm.plan.Expansion", 
        "org.cougaar.planning.ldm.plan.PlanElement",
        "org.cougaar.planning.ldm.policy.Policy", 
        "org.cougaar.planning.ldm.plan.Task", 
        "org.cougaar.planning.ldm.plan.Workflow" };
  static String[] logPlanObjectNames = { "Aggregation", "Allocation", 
        "Disposition", "Asset",
        "AssetTransfer", "Expansion", "PlanElement",
        "Policy", "Task", "Workflow" };

  private static void initCommands() throws RuntimePSPException {
    try {
      BeanInfo commandInfo = Introspector.getBeanInfo(
          Class.forName("org.cougaar.mlm.ui.psp.xmlservice.CommandHandler"), 
          Class.forName("java.lang.Object"));
      commandMethods = commandInfo.getMethodDescriptors();
    } catch (IntrospectionException e) {
      throw new RuntimePSPException(e.toString());
    } catch (ClassNotFoundException e) {
      throw new RuntimePSPException(e.toString());
    }
  }

  private static void initLogPlanClasses() throws RuntimePSPException {
    logPlanClasses = new Class[classNames.length];
    for (int i = 0; i < classNames.length; i++) {
      try {
	logPlanClasses[i] = Class.forName(classNames[i]);
      } catch (Exception e) {
	throw new RuntimePSPException(e.toString());
      }
    }
  }

  /* Returns vector of Terms (name, command method, propertyNames,
     comparator, value, combiner).  Note that some of these fields
     may be null.
     */

  public static Vector parseRequest(String request) throws RuntimePSPException {
    Vector terms = new Vector(1);
    String combiners =  "|&";

    request = request.trim(); // ignore leading/trailing white space

    // for queries which specify which fields to return
    // strip off list of fields
    int index = request.indexOf(":");
    Vector fieldNames = null;
    if (index != -1) {
      String returnFields = request.substring(index+1);
      StringTokenizer tmp = new StringTokenizer(returnFields, ",", false);
      fieldNames = new Vector(tmp.countTokens());
      while (tmp.hasMoreTokens()) {
	String fieldName = tmp.nextToken();
	fieldName = fieldName.trim();
	if (fieldName.length() == 0) 
	  throw new RuntimePSPException("Illegal request:" + request +
					"; empty return field not allowed.");
	fieldNames.addElement(fieldName);
      }
      request = request.substring(0, index);
    }

    // separate out the terms delimited by "and" and "or" symbols
    StringTokenizer st = new StringTokenizer(request, combiners, true);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();
      name = name.trim();
      if (name.length() == 0)
	throw new RuntimePSPException("Illegal request:" + request +
				      "; empty commands or log plan object names not allowed.");
      Term term = new Term(name);
      if (st.hasMoreTokens())
	term.combiner = st.nextToken();
      terms.addElement(term);
    }

    int nTerms = terms.size();


    // if there are no terms, it's a valid request
    // generate a term for "all" 
    // hang the requested field names off the term
    // and return
    if (nTerms == 0) {
      Term term = new Term("all");
      term.requestedFields = fieldNames;
      terms.addElement(term);
      return terms;
    }

    // hang the requested field names off the first term
    ((Term)(terms.elementAt(0))).requestedFields = fieldNames;

    // Error check: the last term must not have a combiner
    if (((Term)terms.elementAt(nTerms-1)).combiner != null)
      throw new RuntimePSPException("Illegal request:" + request +
				    "; must not end with a comparison symbol.");

    // Error check: all but the last term must have a combiner
    for (int i = 0; i < nTerms-1; i++)
      if (((Term)terms.elementAt(i)).combiner == null)
	throw new RuntimePSPException("Illegal request:" + request +
				      "; terms must be separated with a comparison symbol.");

    // parse out terms of the form: name, command, or name comparator value
    for (int i = 0; i < nTerms; i++)
      parseTerm((Term)(terms.elementAt(i)));
    
    // Error check: if one term with no comparator,
    // it must be a command name or a log plan object name
    if (nTerms == 1 && ((Term)terms.elementAt(0)).comparator == null) {
      Term term = (Term)terms.elementAt(0);
      if (!(isCommand(term.name) || isLogPlanObjectName(term.name)))
	throw new RuntimePSPException("Illegal request:" + request +
				      "; single term must be command or log plan object name.");
      else
	return terms; // valid command or log plan name
    }

    // Error check: if it's not a command or log plan object name,
    // each term must have a comparator, properties, and a value
    // if any term specifies a class name,
    // all the class names must be the same, and must be a log plan object name
    Term firstTerm = (Term)terms.elementAt(0);
    String className = firstTerm.name;
    for (int i = 0; i < nTerms; i ++) {
      Term term = (Term)terms.elementAt(i);
      if (term.comparator == null ||
	  term.propertyNames == null ||
	  term.value == null)
	throw new RuntimePSPException("Illegal request:" + request +
				      "; expected terms of the form name comparator value.");
      if (className == null && term.name != null)
	throw new RuntimePSPException("Illegal request:" + request +
				      "; all terms must either refer to the same class of log plan object or to no class.");
      if (!term.name.equalsIgnoreCase(className))
	throw new RuntimePSPException("Illegal request:" + request +
				      "; all terms must refer to same class of log plan object.");
    }

    return terms;
  }

  /* Parse terms of the form "name" or "name comparator value".
     Called with a term in which the name and combiner have
     been entered.
   */

  private static void parseTerm(Term term) throws RuntimePSPException {
    String comparators = "<>!="; // initial characters of comparator symbols

    StringTokenizer st = new StringTokenizer(term.name, comparators, true);
    int n = st.countTokens();
    if (n == 1)
      composeTerm(term, st.nextToken().trim(), null, null);
    else if (n == 3)
      composeTerm(term, st.nextToken().trim(), st.nextToken(), st.nextToken().trim());
    else if (n == 4)
      composeTerm(term, st.nextToken().trim(), st.nextToken() + st.nextToken(), st.nextToken().trim());
    else
      throw new RuntimePSPException("Illegal request:" + term.toString() +
				    "; term must be name comparator value");
  }

  /*
    Called with 1, 3, or 4 arguments:
    name or
    name comparator value or
    name comparator comparator value 
    Checks for validity of all fields and enters them in the term.
    */
    
  private static void composeTerm(Term term, 
				 String name, 
				 String comparator, 
				 String value) throws RuntimePSPException {
    char[] comparators = { '<', '>', '=', '!' };
    String request = term.toString(); // original request
    term.name = ""; // set below

    if (name.length() == 0)
      throw new RuntimePSPException("Illegal request:" + request +
				    "; expected name, received empty string.");
    if (containsComparators(name))
      throw new RuntimePSPException("Illegal request:" + request +
				    "; expected name, received:" + name);

    parsePropertyNames(term, name);

    if (value != null) {
      if (value.length() == 0)
	throw new RuntimePSPException("Illegal request:" + request +
				      "; expected value, received empty string.");
      if (containsComparators(value))
	throw new RuntimePSPException("Illegal request:" + request +
				      "; expected value, received:" + value);
      term.value = value;

      if (!isComparator(comparator))
	throw new RuntimePSPException("Illegal request:" + request +
				      "; expected comparator, received:" + comparator);
      term.comparator = comparator;
    }
  }

  /* Called with a string which should be a command or
     the (optional) log plan object name and property names.
   */

  private static void parsePropertyNames(Term term, String s) throws RuntimePSPException {
    Vector propertyNames = new Vector();
    StringTokenizer st = new StringTokenizer(s, ".");

    // check if first token is command, log plan object name, or property name
    if (st.hasMoreTokens()) {
      String firstToken = st.nextToken();
      if (firstToken.length() == 0)
	throw new RuntimePSPException("Illegal request:" + s +
				      "; empty log plan object name or property name.");
      if (isCommand(firstToken)) {
	term.name = firstToken;
	setCommandMethod(term);
      } else if (isLogPlanObjectName(firstToken)) {
	term.name = firstToken;
	setLogPlanObjectClass(term);
      } else if (containsWhitespace(firstToken)) {
	throw new RuntimePSPException("Illegal request:" + s +
				      "; property name must not contain whitespace.");
      } else
	propertyNames.addElement(firstToken); // default 
    } else
      throw new RuntimePSPException("Illegal request:" + s +
				    "; expected log plan object name or property name.");
    while (st.hasMoreTokens()) {
      String propertyName = st.nextToken();
      if (containsWhitespace(propertyName))
	throw new RuntimePSPException("Illegal request:" + s +
				      "; property name must not contain whitespace.");
      propertyNames.addElement(propertyName);
    }
    if (propertyNames.size() != 0)
      term.propertyNames = propertyNames; // only set this, if have properties
  }

  /* Returns true if the string contains comparators.
   */

  private static boolean containsComparators(String s) {
    String comparators = "<>=!";
    StringTokenizer st = new StringTokenizer(s, comparators, true);
    if (st.countTokens() > 1)
      return true;
    return false;
  }

  /* Returns true if the string contains any leading, trailing, or
     in between whitespace.
     */
  private static boolean containsWhitespace(String s) {
    if (!s.trim().equalsIgnoreCase(s))
      return true;
    StringTokenizer st = new StringTokenizer(s);
    if (st.countTokens() > 1)
      return true;
    return false;
  }

  private static boolean isComparator(String s) {
    String[] comparators = { "=", "!=", "<", ">", "<=", ">=" };
    for (int i = 0; i < comparators.length; i++)
      if (comparators[i].equals(s))
	return true;
    return false;
  }

  /* Returns true if specified string is a command name.
     */
  private static boolean isCommand(String s) throws RuntimePSPException {
    if (commandMethods == null)
      initCommands();
    for (int i = 0; i < commandMethods.length; i++) {
      Method method = commandMethods[i].getMethod();
      if (s.equalsIgnoreCase(method.getName()))
	return true;
    }
    return false;
  }

  /* Sets the command method corresponding to the command name in term.
   */
  private static void setCommandMethod(Term term) throws RuntimePSPException {
    if (commandMethods == null)
      initCommands();
    String s = term.name;
    for (int i = 0; i < commandMethods.length; i++) {
      Method method = commandMethods[i].getMethod();
      if (s.equalsIgnoreCase(method.getName())) {
	term.commandMethod = method;
	return;
      }
    }
  }

  /* Returns true if the specified string is a log plan object name.
   */

  private static boolean isLogPlanObjectName(String s) throws RuntimePSPException {
    if (logPlanClasses == null)
      initLogPlanClasses();
    for (int i = 0; i < logPlanObjectNames.length; i++)
      if (s.equalsIgnoreCase(logPlanObjectNames[i])) {
	return true;
      }
    return false;
  }

  /* Sets the log plan object class corresponding to the log
     plan object name in the term.
     */

  private static void setLogPlanObjectClass(Term term) throws RuntimePSPException {
    String s = term.name;
    for (int i = 0; i < logPlanObjectNames.length; i++)
      if (s.equalsIgnoreCase(logPlanObjectNames[i])) {
	term.requestedClass = logPlanClasses[i];
	return;
      }
  }

  // For testing, parse the request the user enters
  public static void main(String[] args) {
    String request = "";
    if (args.length != 0)
      request = args[0];
    System.out.println("Parsing request:" + request);
    Vector terms = null;
    try {
      terms = RequestParser.parseRequest(request);
      if (terms != null)
	for (int i = 0; i < terms.size(); i++)
	  System.out.println(((Term)terms.elementAt(i)).toString());
    } catch (RuntimePSPException e) {
      JOptionPane.showMessageDialog(null, 
				    e.toString(),
				    "Error Parsing Request", 
				    JOptionPane.ERROR_MESSAGE);
    }
  }

}


