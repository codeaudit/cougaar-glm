/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.psp.xmlservice;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.Vector;

import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AssetIntrospection;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.policy.Policy;
import org.cougaar.core.util.PropertyNameValue;
import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.mlm.ui.data.*;

/**
 * This class implements a predicate for getting the user requested
 * plan objects.
 */

public class XMLUIObjectSelector implements UnaryPredicate {
  Vector requestedTerms;
  Vector planObjectsSelected = new Vector(1);

  public XMLUIObjectSelector(Vector requestedTerms) {
    this.requestedTerms = requestedTerms;
  }

  private Method getReadMethod(BeanInfo info, String propertyName) {
    PropertyDescriptor[] pds = info.getPropertyDescriptors();
    for (int i = 0; i < pds.length; i++) {
      PropertyDescriptor pd = pds[i];
      if (pd.getName().equalsIgnoreCase(propertyName))
	return pd.getReadMethod();
    }
    return null;
  }

  /* Get org.cougaar.domain.mlm.ui.data object corresponding to the log plan object.
   */

  private Object getUIDataObject(Object obj) {
    if (Allocation.class.isInstance(obj)) {
      return new UIAllocationImpl((Allocation)obj);
    } else if (Disposition.class.isInstance(obj)) {
      return new UIFailedDispositionImpl((Disposition)obj);
    } else if (Asset.class.isInstance(obj)) {
      return new UIAssetImpl((Asset)obj);
    } else if (AssetTransfer.class.isInstance(obj)) {
      return new UIAssetTransferImpl((AssetTransfer)obj);
    } else if (Expansion.class.isInstance(obj)) {
      return new UIExpansionImpl((Expansion)obj);
    } else if (Policy.class.isInstance(obj)) {
      return new UIPolicyImpl((Policy)obj);
    } else if (MPTask.class.isInstance(obj)) {
      return new UIMPTaskImpl((MPTask)obj);
    } else if (Task.class.isInstance(obj)) {
      return new UITaskImpl((Task)obj);
    } else if (Workflow.class.isInstance(obj)) {
      return new UIWorkflowImpl((Workflow)obj);
    } else if (Aggregation.class.isInstance(obj)) {   // nate added
      return new UIAggregationImpl((Aggregation)obj); // nate added
    } else {
      System.out.println("WARNING: Class not supported: " + obj.getClass());
      return null;
    }
  }

  private Object getAssetPropertyValue(Asset asset, String propertyName) {
    Vector pnvs = AssetIntrospection.fetchAllProperties(asset);
    for (int i = 0; i < pnvs.size(); i++) {
      PropertyNameValue pnv = (PropertyNameValue)pnvs.elementAt(i);
      if (pnv.name.equals(propertyName))
	return pnv.value;
    }
    return null;
  }

  private Object getPropertyValue(Object object, String propertyName) {
    if (Asset.class.isInstance(object)) {
      Object assetProperty = 
	getAssetPropertyValue((Asset)object, propertyName);
      if (assetProperty != null)
	return assetProperty;
    }

    BeanInfo info = null;
    try {
      info = Introspector.getBeanInfo(object.getClass());
    } catch (IntrospectionException e) {
      System.out.println("INTROSPECTION EXCEPTION");
    }

    Method readMethod = getReadMethod(info, (String)propertyName);
    if (readMethod == null) {
      //      System.out.println("No read method for property: " + propertyName);
      return null;
    }

    Object nextObject = null;
    try {
      nextObject = readMethod.invoke(object, null);
    } catch (Exception e) {
      System.out.println("Exception invoking method:" + e);
    }
    return nextObject;
  }

  private boolean testObjectAgainstTerm(Object obj, Term requestedTerm) {
    Class requestedClass = requestedTerm.requestedClass;
    Vector propertyNames = requestedTerm.propertyNames;
    String requestedValue = requestedTerm.value;

    // if the request didn't specify any property values,
    // and the object is the correct class, return true
    if (propertyNames == null)
      return true;

    // if it's a request for a specific UID
    // handle that specially as UID is not a property of the UI data objects
    String s = (String)propertyNames.elementAt(0);
    if (s.equals("UID")) {
      if (UIUniqueObject.class.isInstance(obj)) {
	UUID objectUUID = ((UIUniqueObject)obj).getUUID();
	if (objectUUID == null)
	  return false;
	return objectUUID.toString().equals(requestedValue);
      }
    }

    // get all the properties for the class of each object
    // get the read method for the property that we're interested in
    // invoke it, and if the result is non-null, add the result to 
    // the next generation
    // handles case in which an intermediate property
    // is an indexed property, for example:
    // prepositionalphrases.preposition; the first property is indexed
    // and we have to get all its values and then get the second
    // property on each of the results, etc.

    Vector objects = new Vector(1);
    objects.addElement(obj);
    for (int i = 0; i < propertyNames.size(); i++) {
      String propertyName = (String)propertyNames.elementAt(i);
      Vector requestedObjects = new Vector(1);
      for (int j = 0; j < objects.size(); j++) {
	Object nextObject = getPropertyValue(objects.elementAt(j), propertyName);
	if (nextObject != null) {
	  if (nextObject.getClass().isArray()) {
	    Object[] nextObjects = (Object[])nextObject;
	    for (int k = 0; k < nextObjects.length; k++)
	      requestedObjects.addElement(nextObjects[k]);
	  }
	  else
	    requestedObjects.addElement(nextObject);
	}
      }
      objects = requestedObjects;
      if (objects.size() == 0)
	return false;
    }

    String comparator = requestedTerm.comparator;

    // loop over all requested objects and quit if any match
    if (Integer.TYPE.isInstance(objects.elementAt(0))) {
      int requestedIntegerValue = Integer.parseInt(requestedValue);
      for (int i = 0; i < objects.size(); i++)
	if (testObjectAgainstIntegerValue(((Integer)(objects.elementAt(i))).intValue(), requestedIntegerValue, comparator))
	  return true;
      return false;
    } else {
      for (int i = 0; i < objects.size(); i++)
	if (testObjectAgainstStringValue(objects.elementAt(i).toString(), requestedValue, comparator))
	  return true;
      return false;
    }
  }

  private boolean testObjectAgainstIntegerValue(int i, int value, String comparator) {
    if (comparator.equals("="))
      return i == value;
    else if (comparator.equals("!="))
      return i != value;
    else if (comparator.equals("<"))
      return i < value;
    else if (comparator.equals("<="))
      return i <= value;
    else if (comparator.equals(">"))
      return i > value;
    else if (comparator.equals(">="))
      return i >= value;
    return false;
  }

  private boolean testObjectAgainstStringValue(String s, String value, String comparator) {
    int result = s.compareTo(value);
    //    System.out.println("Comparing requested value:" + value + " to:" + s);
    if (comparator.equals("="))
      return result == 0;
    else if (comparator.equals("!="))
      return result != 0;
    else if (comparator.equals("<"))
      return result < 0;
    else if (comparator.equals("<="))
      return result <= 0;
    else if (comparator.equals(">"))
      return result > 0;
    else if (comparator.equals(">="))
      return result >= 0;
    return false;
  }

  /* The execute method always returns false.  We don't actually want
     anything in our container; we just want to compare the objects
     against the user's request, so that we can create ui data objects
     for the log plan objects the user requested.
     */

  public boolean execute(Object obj) {
    boolean currentResult = true; // if no terms, user wants everything
    String combiner = "";

    // if no qualifications on the object, then include it
    if (requestedTerms.size() == 1 && 
	((Term)(requestedTerms.elementAt(0))).name.equals("all")) {
      Object uiDataObject = getUIDataObject(obj);
      if (uiDataObject != null) {
	planObjectsSelected.addElement(uiDataObject);
      }
      return false;
    }

    // compare the object against all the user specified qualifiers
    Object uiDataObject = null;
    for (int i = 0; i < requestedTerms.size(); i++) {
      Term requestedTerm = (Term)requestedTerms.elementAt(i);

      // if object is of wrong class, then ignore it
      Class requestedClass = requestedTerm.requestedClass;
      if (requestedClass != null)
	if (!requestedClass.isInstance(obj))
	  return false;

      // get ui.data version
      uiDataObject = getUIDataObject(obj);
      if (uiDataObject == null) {
	System.out.println("Can't create ui data object for that object.");
	return false;
      }

      boolean result = testObjectAgainstTerm(uiDataObject, requestedTerm);
      if (i == 0) {
	currentResult = result;
      } else {
	if (combiner.equals("|"))
	  currentResult = currentResult || result;
	else
	  currentResult = currentResult && result;
      }
      combiner = requestedTerm.combiner;
    }	
    //    System.out.println("Test Object Result:" + currentResult);

    // if the object matches the user's request, include it
    if (currentResult)
      planObjectsSelected.addElement(uiDataObject);
    return false;
  }

  public Vector getSelectedObjects() {
    return planObjectsSelected;
  }

}
