/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * Replaces a task or resource accessor with the value of an object
 *
 */

public class GSValueAccessor {

  /** Constructor 
   * The type String isn't actually used, it's just there to make
   * sure you know what you're getting in to- i.e. that the class (i.e. type)
   * actually has a method getUNITS
   */
  public GSValueAccessor (String type, String units) {
    myType = type;
    myUnits = units;
  }

  /** returns the value in the given units */
  public Object value (Object obj) {
      if (obj instanceof Number)
	  return new Double(((Number)obj).doubleValue());
      Object returnValue = callGetMethod(obj, myUnits);
      // We want to call the getUnits method on this object.
      return returnValue;
  }

  /**
   * Get the value of the object from the get<Units> method.
   * To maintain correctness across the rest of the code (notably for use in
   * GSCapacityConstraint) we MUST return a Double, int, or other 
   * cast-to-Double-able object.
   * 
   * Code modeled after COUGAAR OrgRTDataPlugIn callSetMethod() method
   */
  private Object callGetMethod(Object o, String unitname) {
    if (o == null)
      throw new RuntimeException("object is null for units " + unitname);
    
    Class oc = o.getClass();
    String getname = "get"+unitname;
    String hash_str = oc.getName() + unitname;

    try {
      Method method = (Method)classMethods.get(hash_str);
      if (method == null) {
	method = oc.getMethod(getname, null);
	// method should never be null- a NoSuchMethodException 
	// will be thrown if the method isn't found
	classMethods.put(hash_str, method);
      }

      return method.invoke(o, null);

    } catch (Exception e) {
      throw new RuntimeException("Couldn't find method "+getname+" for object "+o);
    }
  }

  private String myType;
  private String myUnits;
  private static Hashtable classMethods = new Hashtable();
}
