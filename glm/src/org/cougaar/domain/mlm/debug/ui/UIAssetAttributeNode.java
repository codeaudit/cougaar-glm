/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */


package org.cougaar.domain.mlm.debug.ui;

import org.cougaar.domain.planning.ldm.asset.Asset;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.reflect.*;
import java.beans.*;
import org.cougaar.domain.planning.ldm.asset.PropertyGroup;
import java.util.Hashtable;

/** A tree node for an asset and its attributes.
  Overrides the UITreeNode loadChildren, toString and isLeaf
  methods to dynamically display the asset which has no children.
  */

public class UIAssetAttributeNode extends UITreeNode {
  Asset asset;
  int treeIndex = 0;

  /** Create a tree node for the Asset.
    @param asset the asset for which to create a tree node
   */

  public UIAssetAttributeNode(Asset asset) {
    super(asset);
    this.asset = asset;
  }

  /** The Asset is a parent (of its properties and attributes).
    @return false
   */
  public boolean isLeaf() {
    return false;
  }

  /** Children are its properties and attributes.
   */

  public void loadChildren() {
    fetchAllProperties();
  }

  /** Return the representation of a Asset in a tree.
    This is the asset name, and 
    for physical objects, the serial number, if specified, 
    and the capabilities.
    @return asset name followed by serial number (optional) and capabilities
   */

  public String toString() {
    return prettyName(asset.toString());
  }

  public void fetchAllProperties() {
    // getMethods key is a property Class object
    // getMethods value is the Method that returns the instance of the Property
    Hashtable getMethods = new Hashtable() ;
    Class thisClass = asset.getClass();
    Field fields[] = null;
    Vector fieldVector = new Vector(2);
    Method methods[] = null;
    Object result = null;

    // retrieve all public methods of this class and its ancestor classes
    // retrieve all attributes of this class and ancestors
    try {
      methods = thisClass.getMethods();
      Class ancestorClass = thisClass;
      while (Asset.class.isAssignableFrom(ancestorClass)) {
	fields = ancestorClass.getDeclaredFields();
	for (int i = 0; i < fields.length; i++) {
	  fieldVector.addElement( fields[i] );
	}
	ancestorClass = ancestorClass.getSuperclass();
      }
    } catch (SecurityException se) {
      System.out.println("In Asset.fetchAllProperties: " + se.toString());
    }

    // Build a Hashtable of the methods with the right signature, where
    // "right signature" means the method returns a subtype of Property
    // and requires no arguments.
    for ( int methodIndex = 0; methodIndex < methods.length; methodIndex++ ) {
      Class returntype = methods[methodIndex].getReturnType();
      if (returntype != null &&
	  PropertyGroup.class.isAssignableFrom( returntype )  &&
	  methods[methodIndex].getParameterTypes().length == 0) {
	getMethods.put( methods[methodIndex].getReturnType(), 
			methods[methodIndex] );
      }
    }

    // Walk through the list of fields looking for subtypes of Property.
    // If the field's Class is in the Hashtable, do introspection
    // this section is a bit complex because we want to introspect
    // on the DECLARED return property CLASS to determine what
    // attributes to fetch, but we want to fetch
    // the attributes by invoking methods on the actual property object
    Enumeration fieldEnum = fieldVector.elements();
    while ( fieldEnum.hasMoreElements() ) {
      Field currentField = (Field)fieldEnum.nextElement();
      Method currentMethod = (Method)getMethods.get(currentField.getType());
      if (currentMethod != null) {
	try {
	  // invoke the get property method on the asset
	  Object returnedObject = currentMethod.invoke(asset, null);
	  if (returnedObject != null) {
	    Class returnType = currentMethod.getReturnType();
	    // introspect on property class to get attributes
	    // throws Introspection exception
	    //System.out.println("Introspecting on: " + returnType + 
	    //" for " + asset.toString());
	    BeanInfo info = Introspector.getBeanInfo(returnType);
	    PropertyDescriptor[] pds = info.getPropertyDescriptors();
	    // invoke read methods to get attributes of property
	    for (int j = 0; j < pds.length; j++) {
	      Method readMethod = pds[j].getReadMethod();
	      if (readMethod != null) {
		try {
		  //System.out.println("Invoking: " + readMethod.getName() +
		  //" on " + returnedObject);
		  result = readMethod.invoke(returnedObject, null);

                  String s = prettyName(returnType.toString()) + ": " +
                    pds[j].getName() + ": " +
                    result;
                  insert(new UIStringNode(s), treeIndex++);
		} catch (NullPointerException e) {
		  System.out.println("In Asset.fetchAllProperties: " + 
				     e.toString());
                  e.printStackTrace();
                } catch (InvocationTargetException e) {
                  System.out.println("Invocation target error; method: " +
                                     currentMethod.getName() +
                                     " exception: " + e.toString());
                  e.printStackTrace();
                }

	      }
	    }
	  }
	} catch (Exception e) {
	  System.out.println("Exception caught; method: " + 
			     currentMethod.getName() +
			     " exception: " + e.toString());
          e.printStackTrace();
	}
      }
    }

    // now handle the dynamically added properties
    Method readAttributeMethod = null;
    Enumeration dynamic = asset.getOtherProperties();
    while (dynamic.hasMoreElements()) {
      Object o = dynamic.nextElement();
      try {
        PropertyGroup p = (PropertyGroup)o;
        Class interfaceToUse = p.getPrimaryClass();
	//	System.out.println("Introspecting on: " + interfaceToUse);
	//System.out.println("Introspecting on: " + interfaceToUse +
	//" for " + asset.toString());
	BeanInfo propertyBeanInfo = Introspector.getBeanInfo(interfaceToUse);
	PropertyDescriptor[] attributePds = 
	  propertyBeanInfo.getPropertyDescriptors();
	for (int k = 0; k < attributePds.length; k++) {
	  readAttributeMethod = attributePds[k].getReadMethod();
	  if (readAttributeMethod != null) {
	    //	    System.out.println("From other properties, Invoking: " + readAttributeMethod.getName() +
	    //			       " on " + o);
	    result = readAttributeMethod.invoke(o, null);
	    String s = prettyName(interfaceToUse.toString()) + ": " +
              attributePds[k].getName() + ": " +
              result;
	    insert(new UIStringNode(s), treeIndex++);
	  }
	}
      } catch (Exception e) {
        String n = "(null method)";
        if (readAttributeMethod != null)
          n = readAttributeMethod.getName();
	System.out.println("Exception introspecting on other properties: " + 
			   e.toString() + " while invoking " +
			   n + " on " +
			   o.getClass());
      }
    }
  }

  // Return the last field of a fully qualified name.
  // If the input string contains an "@" then it's assumed
  // that the fully qualified name preceeds it.

  private String prettyName(String s) {
    int i = s.indexOf("@");
    if (i != -1)
      s = s.substring(0, i);
    return (s.substring(s.lastIndexOf(".")+1));
  }

}


