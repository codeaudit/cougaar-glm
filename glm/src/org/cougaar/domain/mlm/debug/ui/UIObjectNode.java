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


package org.cougaar.domain.mlm.debug.ui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

/** A tree node for a Object.
  Overrides the UITreeNode loadChildren, toString and isLeaf.
  */

public class UIObjectNode extends UITreeNode {
  Object obj;
  String prefix;

  /** Create a tree node for the Object.
    @param Object s for which to create tree node
   */
  public UIObjectNode(Object obj, String prefix) {
    super(obj);
    this.obj = obj;
    this.prefix = prefix;
  }

  /** The Object is not a leaf.
    @return false
   */
  public boolean isLeaf() {
    return false;
  }

  public void loadChildren() {
    BeanInfo info = null;
    Object value = null;

    try {
      info = Introspector.getBeanInfo(obj.getClass());
    } catch (IntrospectionException e) {
      System.out.println("Introspection Exception");
    }

    PropertyDescriptor[] properties = info.getPropertyDescriptors();
    int index = 0; // child position
    for (int i = 0; i < properties.length; i++)
      {
	PropertyDescriptor pd = properties[i];
	Method rm = pd.getReadMethod();
	// here is where we need to determine if the attribute
	// is really a string or if we need to delve further
	if (rm != null) {
	  String methodName = rm.getName();
	  if (methodName.equals("getProxy"))
	    continue; // ignore this
	  //	  if (methodName.equals("isValid"))
	  //	    continue; //ignore this
	  //	  if (methodName.equals("isDeepValid"))
	  //	    continue; // ignore this too
	  try {
	    value = rm.invoke(obj, null);
	  } catch (InvocationTargetException ite) {
	    System.out.println("Invocation target exception: " + ite.getTargetException());
	  } catch (Exception e) {
	    System.out.println("Exception when invoking read method: " + e);
	  }
	  if (value != null) {
	    String s = methodName;
	    if (s.startsWith("get"))
	      s = s.substring(3);
	    else if (s.startsWith("is"))
	      s = s.substring(2);
	    s = s + ": ";
	    if (value instanceof java.lang.Boolean ||
		value instanceof java.lang.String ||
		value instanceof java.lang.Long ||
		value instanceof java.lang.Integer ||
		value instanceof java.lang.Class) {
	      s = s + value.toString();
	      insert(new UIStringNode(s),index++);
	    } else if (value instanceof java.util.Enumeration) {
	      Enumeration e = (Enumeration)value;
	      while (e.hasMoreElements()) {
		Object enumValue = e.nextElement();
		if (enumValue != null)
		  insert(new UIObjectNode(enumValue,s),index++);
	      }
	    } else {
	      insert(new UIObjectNode(value,s),index++);
	    }
	  }
	}
      }
  }

  /** Return representation of a Object in a tree.
    @return Object toString
   */

  public String toString() {
    return prefix + obj.toString();
  }
}


