/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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
    @param obj for which to create tree node
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


