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

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AssetGroup;

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Accesses the specified property value for a given asset
 *
 */

public class GSAssetAccessorImpl implements GSAssetAccessor, GSDebugable {
  public boolean debug = false;
  public void setDebug (boolean d) { debug = d; }

  /**
   * Constructor sets up which property value to access
   */
  public GSAssetAccessorImpl (String propertyGroup, String propertyField,
			      String units, String multiplyByQuantity) {
    this.propertyGroup = propertyGroup;
    this.propertyField = propertyField;
    this.units = units;
    this.multiplyByQuantity = multiplyByQuantity.equals ("true");
  }

    public Object value (Asset theAsset) {
	if (theAsset instanceof AssetGroup) {
	    if (debug)
		System.out.println ("AssetGroup has " + 
				    ((AssetGroup) theAsset).getAssets().size () + 
				    " assets.");

	    Object running_total = null;
	    for (Iterator i = ((AssetGroup) theAsset).getAssets().iterator ();
		 i.hasNext(); ) {
		if (running_total == null)
		    running_total = value ((Asset) i.next());
		else
		    running_total = addIt(running_total, value ((Asset) i.next()));
	    }
	    return running_total;
	} else if (theAsset instanceof AggregateAsset) {
	    double qty =((AggregateAsset) theAsset).getQuantity();
	    if (debug)
		System.out.println ("Aggregate asset with " + qty + " items."); 

	    Object aggValue = value (((AggregateAsset) theAsset).getAsset());
	    
	    if (aggValue instanceof Integer)
		return new Integer ((int) (((Integer) aggValue).doubleValue() * qty));
	    if (aggValue instanceof Float)
		return new Float ((float) (((Float) aggValue).doubleValue() * qty));
	    if (aggValue instanceof Double)
		return new Double ((double) (((Double) aggValue).doubleValue() * qty));
	    System.out.println ("Need either float, int, or double, so can't" +
				" multiply by quantity");
	    return aggValue;
	}
	else 
	    return valueOfSimpleAsset (theAsset);
    }

    public Object valueOfSimpleAsset (Asset theAsset) {
      //      Thread.dumpStack ();

	if (debug)
	    System.out.println ("getting value of simple asset " + theAsset);

	Object group = applyGetMethod (theAsset, propertyGroup);

      // If the asset is an AssetGroup, we want to allow some tasks to not
      // have a particular PG
	if (group == null) {
	    if (debug)
		System.out.println ("applyGetMethod to asset " + theAsset + 
				    " looking for group " + propertyGroup + 
				    " -> null?");
	    return null;
	}

	// If one has a PG, though, the value is required.
	Object obj = applyGetMethod (group, propertyField);
	if (obj == null) {
	    if (debug)
		System.out.println ("applyGetMethod to group " + group + 
				    " looking for propery " + propertyField + 
				    " -> null?");
	    return null;
	}

	if (! units.equals ("none"))
	    obj = applyGetMethod (obj, units);
	if ((obj == null) ||
	    (! multiplyByQuantity) ||
	    (! ((theAsset instanceof AggregateAsset) ||
		(theAsset instanceof AssetGroup)))) {

	    //	    if (debug)
	    //		System.out.println ("can't get units for obj " + obj + 
	    //				    " units " + units);
	}
	return obj;
    }

  /**
   * Accesses the specified property value for asset
   */
  public Object oldValue (Asset theAsset) {
    Asset asset = theAsset;

    Vector assets_to_handle = new Vector();

    if (asset instanceof AssetGroup) {
      assets_to_handle = ((AssetGroup) asset).getAssets();
      if (debug)
	  System.out.println ("AssetGroup has " + assets_to_handle.size () + 
			      " assets.");
    } else {
      if (asset instanceof AggregateAsset) {
	asset = ((AggregateAsset) asset).getAsset();
	if (debug)
	    System.out.println ("aggregate asset ");
      }

      assets_to_handle.add(asset);
    }

    Object running_total = null;
    Iterator i = assets_to_handle.iterator();
    while (i.hasNext()) {
      Asset a = (Asset)i.next();

      Object group = applyGetMethod (a, propertyGroup);

      // If the asset is an AssetGroup, we want to allow some tasks to not
      // have a particular PG
      if (group == null) {
	if (theAsset instanceof AssetGroup)
	  continue;

	if (debug)
	    System.out.println ("applyGetMethod to asset " + a + 
				" looking for group " + propertyGroup + 
				" -> null?");
	return null;
      }

      // If one has a PG, though, the value is required.
      Object obj = applyGetMethod (group, propertyField);
      if (obj == null) {
	if (debug)
	    System.out.println ("applyGetMethod to group " + group + 
				" looking for propery " + propertyField + 
				" -> null?");
	return null;
      }

      if (! units.equals ("none"))
	obj = applyGetMethod (obj, units);
      if ((obj == null) ||
	  (! multiplyByQuantity) ||
	  (! ((theAsset instanceof AggregateAsset) ||
	      (theAsset instanceof AssetGroup)))) {

	  if (debug)
	      System.out.println ("can't get units for obj " + obj + 
				  " units " + units);

	return obj;
      }
      
      if (theAsset instanceof AggregateAsset) {
	double qty =((AggregateAsset) theAsset).getQuantity();
	if (obj instanceof Integer)
	  return new Integer ((int) (((Integer) obj).doubleValue() * qty));
	if (obj instanceof Float)
	  return new Float ((float) (((Float) obj).doubleValue() * qty));
	if (obj instanceof Double)
	  return new Double ((double) (((Double) obj).doubleValue() * qty));
	System.out.println ("Need either float, int, or double, so can't" +
			    " multiply by quantity");
	return obj;
      } else if (theAsset instanceof AssetGroup) {
	if (running_total == null)
	  running_total = obj;
	else
	  running_total = addIt(running_total, obj);
      }
    }
    return running_total;
  }
  
  private Object addIt(Object total, Object added) {
    Object new_total = null;
    if ((total instanceof Integer) && (added instanceof Integer))
      new_total = new Integer(((Integer)total).intValue() + 
			      ((Integer)added).intValue());
    else if ((total instanceof Float) && (added instanceof Float))
      new_total = new Float(((Float)total).floatValue() + 
			    ((Float)added).floatValue());
    else if ((total instanceof Double) && (added instanceof Double))
      new_total = new Double(((Double)total).doubleValue() + 
			     ((Double)added).doubleValue());
    else
	System.out.println ("Adding total " + total + 
			    " to added " + added + 
			    " = " + new_total + "?");
    return new_total;
  }

  /** 
   * Includes fix for alp asset _Locked problem.
   *  
   * Inside of alp assets, there is a _Locked object, which is sometimes what
   * gets returned when you ask an asset for one of it's properties.  So
   * instead of Organization.getAssignedPG () returning AssignedPGImpl, it
   * returns AssignedPGImpl$_Locked.
   * 
   * This _Locked class implements the same interface as its enclosing class,
   * but it's private.  So when we ask for the class of the object and it's
   * private and try to invoke a method, an IllegalAccessException is thrown.  
   * 
   * The fix here is to look at the interfaces implemented by the _Locked class
   * and use the appropriate one instead if we find a _Locked object.
   *
   * GWFV 9-9-99  (Look at that! Only once a century...)
   */

  private Object applyGetMethod (Object obj, String field) {
    Class theclass = obj.getClass();
    String classname = theclass.getName();
    Method method = null;
    if (debug)
      System.out.println ("applyGetMethod to " + classname + " field get" + field);

    if (classname.indexOf ("Locked") != -1) {
      Class [] ifaces = theclass.getInterfaces ();
      for (int i = 0; i < ifaces.length; i++)
	if (classname.startsWith (ifaces[i].getName ())) {
	  if (debug)
	    System.out.println ("interface " + ifaces[i] + " replaces " + classname); 
	  theclass = ifaces[i];
	  break;
	}
    }

    try {
      String hash_str = classname.concat(field);
      method = (Method)classMethods.get(hash_str);
      if (method == null) {
	// Passing in null is just as good as new Class[0]
	method = theclass.getMethod ("get" + field, null);
	// method should never be null- a NoSuchMethodException 
	// will be thrown if the method isn't found
	classMethods.put(hash_str, method);
      }
    } catch (Exception e) {
      System.out.println ("Cannot find accessor for field " + field +
                          " for class " + classname);
      return null;
    }
    try {
      // Passing in null is just as good as new Object[0]
      return method.invoke (obj, null);
    } catch (Exception e) {
      System.out.println ("Error invoking method " + method);
      System.out.println ("Exception was " + e);
      return null;
    }
  }

  public void reportError (Asset theAsset) {
    Asset asset = theAsset;
    if (asset instanceof AggregateAsset)
      asset = ((AggregateAsset) asset).getAsset();

    Object group = applyGetMethod (asset, propertyGroup);
    if (group == null) {
      System.out.println ("GSAssetAccessor.reportError - " + theAsset + " has no PG " + propertyGroup);
      return;
    }
    Object obj = applyGetMethod (group, propertyField);
    if (obj == null) {
      System.out.println ("GSAssetAccessor.reportError - " + theAsset + " has no field " + propertyField);
      return;
    }
    if (! units.equals ("none"))
      obj = applyGetMethod (obj, units);
    if ((obj == null) ||
        (! multiplyByQuantity) ||
        (! (theAsset instanceof AggregateAsset))) {
      System.out.println ("GSAssetAccessor.reportError - " + theAsset + " has no units " + 
			  units + " and isn't an aggregate asset.");
      return;
    }
  }

  public String getPropertyGroup () { return propertyGroup; }
  public String getPropertyField () { return propertyField; }
  public String getUnits () { return units; }

  private String propertyGroup;
  private String propertyField;
  private String units;
  private boolean multiplyByQuantity;

  private static Hashtable classMethods = new Hashtable(0);
}
