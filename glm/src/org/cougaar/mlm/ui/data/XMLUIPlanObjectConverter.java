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

/*
  Generates XML for UI versions of plan objects.  Used by the
  implementations of the UI plan objects
  to generate the XML which will be returned to the ui or other clients.
  */


package org.cougaar.mlm.ui.data;

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.LockedPG;
import org.cougaar.planning.ldm.asset.AssetIntrospection;

public class XMLUIPlanObjectConverter {
  static Class assetClass;

  public XMLUIPlanObjectConverter() {
  }

  /*
    Create and return xml for first class log plan objects.
    Element name is extracted from object class, by taking the
    last field of the object class, and dropping a trailing "Impl",
    if it exists.
  */

  public static Element getPlanObjectXML(Object obj, Document doc,
                                         Vector requestedFields) {
    Element root = null;
    String className = obj.getClass().getName();
    int i = className.lastIndexOf("Impl");
    if (i != -1)
      className = className.substring(0, i);
    // generates <UITask>
    // root = doc.createElement(className);

    // generates for example <object class="org.cougaar.mlm.ui.data.UITask" UID="3ID/53">
    root = doc.createElement("object");
    root.setAttribute("class", className);
    if (UIUniqueObject.class.isInstance(obj)) {
      UUID objectUUID = ((UIUniqueObject)obj).getUUID();
      if (objectUUID == null)
        System.out.println("WARNING: NULL UUID");
      else
        root.setAttribute("UID", objectUUID.toString());
    }
    addRequestedNodes(doc, obj, root, requestedFields);
    return root;
  }

  /*
    Get the property names and values (returned in a vector)
    from the given property descriptors and for the given object.
  */

  private static Vector getPropertyNamesAndValues(PropertyDescriptor[] properties, Object obj) {
    ArrayList pv = new ArrayList();

    for (int i = 0; i < properties.length; i++) {
      PropertyDescriptor pd = properties[i];
      Method rm = pd.getReadMethod();
      if (rm == null)
        continue;

      // invoke the read method for each property
      Object childObject = null;
      try {
        childObject = rm.invoke(obj, null);
      } catch (InvocationTargetException ie) {
        System.out.println("Invocation target exception invoking: " + 
                           rm.getName() + " on object of class:" + 
                           obj.getClass().getName());
        System.out.println(ie.getTargetException().getMessage());
      } catch (Exception e) {
        System.out.println("Exception " + e.toString() + " invoking: " + 
                           rm.getName() + " on object of class:" + 
                           obj.getClass().getName());
        System.out.println(e.getMessage());
      }
      if (childObject == null)
        continue;
      
      // add property name and value to vector
      String name = pd.getName();
      if ( pd.getPropertyType().isArray() ) {
        int length = Array.getLength(childObject);
        if ( length > 0 ) {
          boolean isPrimitive = 
            Array.get(childObject, 0).getClass().isPrimitive(); 
          ArrayList pvArray = new ArrayList();
          for (int j = 0; j < length; j++) {
            if (isPrimitive)
              pvArray.add(new PropertyNameValue(name, String.valueOf(Array.get(childObject, j))));
            else
              pvArray.add(new PropertyNameValue(name, Array.get(childObject, j)));
          }
          pv.add(new PropertyNameValue(name, pvArray));
        } // if have zero length array, ignore the value
      } else {
        if (childObject.getClass().isPrimitive())
          pv.add(new PropertyNameValue(name, String.valueOf(childObject)));
        else
          pv.add(new PropertyNameValue(name, childObject));
      }
    }

    Collections.sort(pv, lessStringIgnoreCase);
    return new Vector(pv);
  }

  private static void addRequestedNodes(Document doc, Object obj,
                                        Element parentElement,
                                        Vector requestedFields) {
    Hashtable assetsTraversed = new Hashtable();
    addNodes(doc, obj, parentElement, requestedFields, assetsTraversed);
  }

  // special case, treat UUID as a primitive field

  private static boolean isLeafNode(Class propertyClass) {
    String propertyClassName = propertyClass.getName();
    return (propertyClass.isPrimitive()) ||
      (propertyClassName.equals("java.lang.Boolean")) ||
      (propertyClassName.equals("java.lang.Byte")) ||
      (propertyClassName.equals("java.lang.Character")) ||
      (propertyClassName.equals("java.lang.Class")) ||
      (propertyClassName.equals("java.lang.Double")) ||
      (propertyClassName.equals("java.lang.Float")) ||
      (propertyClassName.equals("java.lang.Integer")) ||
      (propertyClassName.equals("java.lang.Long")) ||
      (propertyClassName.equals("java.lang.Short")) ||
      (propertyClassName.equals("java.lang.String")) ||
      (propertyClassName.equals("org.cougaar.mlm.ui.data.UUID")) ||
      (propertyClassName.equals("java.util.Date")); // special case date
  }

  private static void addCollectionNodes(Document doc, 
                                         Element parentElement,
                                         String propertyName,
                                         Object propertyValue,
                                         Hashtable assetsTraversed) {
    Element fieldElement = doc.createElement("field");
    fieldElement.setAttribute("name", propertyName);
    fieldElement.setAttribute("type", "collection");
    parentElement.appendChild(fieldElement);
    Element collectionElement = doc.createElement("collection");
    fieldElement.appendChild(collectionElement);
    // type is the type of the first child element
    // we don't support collections of heterogeneous types
    ArrayList pnvs = (ArrayList)propertyValue;
    Class childClass = ((PropertyNameValue)(pnvs.get(0))).value.getClass();

    // if it's an array of leaf nodes, create xml of the form
    //  <collection type="unqualified class name">
    //     <value>
    //     propertyvalue
    //     </value>
    //      </collection>
    if (isLeafNode(childClass)) {
      String childClassName = getUnqualifiedClassName(childClass);
      collectionElement.setAttribute("type", childClassName);
      for (Iterator j = pnvs.iterator(); j.hasNext(); ) {
        PropertyNameValue arrayElementPNV = (PropertyNameValue)(j.next());
        Element item = doc.createElement("value");
        Object value = arrayElementPNV.value;
        // special case Date handling to return Date.getTime()
        if (Date.class.isInstance(value))
          item.appendChild(doc.createTextNode(String.valueOf(((Date)value).getTime())));
        else
          item.appendChild(doc.createTextNode(value.toString()));
        collectionElement.appendChild(item);
      }
      return;
    }

    // handle collections of objects, creates xml of the form
    // <collection type=object>
    //   <object class="fully qualified path name">
    //    ...
    //   </object>
    // </collection>
    collectionElement.setAttribute("type", "object");
    for (Iterator j = pnvs.iterator(); j.hasNext(); ) {
      PropertyNameValue arrayElementPNV = (PropertyNameValue)(j.next());
      Element item = doc.createElement("object");
      item.setAttribute("class", arrayElementPNV.value.getClass().getName());
      collectionElement.appendChild(item);
      addNodes(doc, arrayElementPNV.value, item, null, assetsTraversed);
    }
  }

  private static void addNodes(Document doc, Object obj, 
                               Element parentElement, 
                               Vector fieldNames,
                               Hashtable assetsTraversed) {
    BeanInfo info = null;
    Vector propertyNameValues = null;
    Class objectClass = obj.getClass();
    if (LockedPG.class.isInstance(obj))
      objectClass = ((LockedPG)obj).getIntrospectionClass();
    if (Asset.class.isInstance(obj)) {
      if (assetsTraversed.get(obj) != null)
        return;
      assetsTraversed.put(obj, obj);
    }
    try {
      info = Introspector.getBeanInfo(objectClass);
    } catch (IntrospectionException e) {
      System.out.println("Exception in converting object to XML" + 
                         e.getMessage());
    }
    propertyNameValues = 
      getPropertyNamesAndValues(info.getPropertyDescriptors(), obj);
            // get all dynamic properties of asset
    if (Asset.class.isInstance(obj)) {
      Enumeration dynamicProperties = ((Asset)obj).getOtherProperties();
      while (dynamicProperties.hasMoreElements()) {
        Object dynamicProperty = dynamicProperties.nextElement();
        String name = prettyName(dynamicProperty.getClass().toString());
        propertyNameValues.add(new PropertyNameValue(name,
                                                     dynamicProperty));
      }
    }

    // add the nodes for the properties and values
    for (int i = 0; i < propertyNameValues.size(); i++) {
      PropertyNameValue pnv = 
        (PropertyNameValue)propertyNameValues.elementAt(i);
      String propertyName = pnv.name;
      if (fieldNames != null) {
        if (!fieldNames.contains(propertyName))
          continue;
      }
      Object propertyValue = pnv.value;
      // check for arrays and add as "collections"
      if (ArrayList.class.isInstance(propertyValue))
        addCollectionNodes(doc, parentElement, propertyName, propertyValue,
                           assetsTraversed);
      else 
        addSingleNode(doc, propertyName, propertyValue.getClass(), 
                      propertyValue, parentElement, assetsTraversed);
    }
  }

  // Return the last field of a fully qualified name.
  // If the input string contains an "@" then it's assumed
  // that the fully qualified name preceeds it.

  private static String prettyName(String s) {
    int i = s.indexOf("@");
    if (i != -1)
      s = s.substring(0, i);
    return (s.substring(s.lastIndexOf(".")+1));
  }

  private static String getUnqualifiedClassName(Class c) {
    String className = c.getName();
    int index = className.lastIndexOf(".");
    if (index != -1)
      className = className.substring(index+1);
    return className;
  }

  private static void addSingleNode(Document doc, String propertyName,
                                    Class propertyClass, Object propertyValue,
                                    Element parentElement,
                                    Hashtable assetsTraversed) {
    if (isLeafNode(propertyClass)) {
      //System.out.println("Creating node named: " + propertyName + " with value: " + propertyValue + " and appending to: " + parentElement.getTagName());
      Element item = doc.createElement("field");
      item.setAttribute("name", propertyName);
      String className = getUnqualifiedClassName(propertyClass);
      item.setAttribute("type", className);
      // special case Date handling to return Date.getTime()
      if (Date.class.isInstance(propertyValue))
        item.appendChild(doc.createTextNode(String.valueOf(((Date)propertyValue).getTime())));
      else
        item.appendChild(doc.createTextNode(propertyValue.toString()));
      parentElement.appendChild(item);
    } else {
      //        System.out.println("Creating node named: " + propertyName + " with class: " + propertyClass.getName() + " and appending to: " + parentElement.getTagName());
      //        Element item = doc.createElement(propertyName);
      Element fieldItem = doc.createElement("field");
      fieldItem.setAttribute("name", propertyName);
      fieldItem.setAttribute("type", "object");
      Element item = doc.createElement("object");
      item.setAttribute("class", propertyClass.getName());
      fieldItem.appendChild(item);
      parentElement.appendChild(fieldItem);
      // ignore field names except at the highest level
      addNodes(doc, propertyValue, item, null, assetsTraversed);
    }
  }

  static class PropertyNameValue {
    String name;
    Object value;
    
    public PropertyNameValue(String name, Object value) {
      this.name = name;
    this.value = value;
    }
    
  }
    
  private static Comparator lessStringIgnoreCase = 
    new Comparator () {
        public int compare(Object first, Object second) {
          String firstName = ((PropertyNameValue)first).name;
          String secondName = ((PropertyNameValue)second).name;
          return firstName.toLowerCase().compareTo(secondName.toLowerCase());
          //return first.toString().toLowerCase().compareTo(second.toString().toLowerCase()) < 0;

        }

        static final long serialVersionUID = -5398274395395704408L;
      };
}


