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

package org.cougaar.mlm.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Collection;
import org.apache.xerces.parsers.DOMParser;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.domain.Factory;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.core.service.UIDServer;
import org.cougaar.core.util.*;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.PropertyGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML-based Object factory, using reflection and COUGAAR factories.  Also
 * uses StringObjectFactory.
 * <p>
 * The usage is best seen by example:
 * <p>
 * <code>
 * &lt;IGNORED type="java.util.Vector"&gt;
 *   &lt;element type="GeolocLocation"&gt;
 *     GeolocCode=HKUZ, Name=FortStewart, Latitude=Latitude 31.85degrees, Longitude=Longitude -81.5degrees
 *   &lt;element&gt;
 *   &lt;element type="GeolocLocation"&gt;
 *     &lt;GeolocCode&gt; HKUZ &lt;GeolocCode&gt;
 *     &lt;Name&gt; FortStewart &lt;Name&gt;
 *     &lt;Latitude&gt; 31.85degrees &lt;Latitude&gt;
 *     &lt;Longitude&gt; 81.5degrees &lt;Longitude&gt;
 *   &lt;element&gt;
 * &lt;IGNORED&gt;
 * </code>
 * <p>
 * The above creates a vector of two GeolocLocations, each with the
 * same information.  In the first GeolocLocation the fields were
 * specified in a "StringObjectFactory" manner, and in the second 
 * GeolocLocation the fields were specified in an "XMLObjectFactory" 
 * manner.
 * <p>
 * The "XMLObjectFactory" is used for the XML tree, and at the leaf 
 * nodes the String value is given to the "StringObjectFactory".  In 
 * the second Vector "element" was specified by "type=" to be an 
 * instance of "GeolocLocation" and, for the subNodes, reflection 
 * was used to find setter methods, i.e. it saw a node named
 * "GeolocCode" and looked up "setGeolocCode(?)" in class 
 * "GeolocLocation".
 * <p>
 * The node attribute "type=" must be specied when:<br>
 * <ul>
 *   <li>At the top-most node, where the name is ignored and
 *       the type is unknown</li>
 *   <li>When these are "element"s of some java.util.Collection</li>
 *   <li>When the method is overloaded and the type would be
 *       ambiguous, e.g. if setX(int) and setX(Object) both exist</li>
 *   <li>When the type from the method definition is abstract</li>
 *   <li>When the type from the method definition is an interface 
 *       and not directly supported by the PlanningFactory, 
 *       e.g. "Location" v.s. "GeolocLocation"</li>
 * </ul>
 * But there are many instances when the "type=" is not needed
 * since only one "set" method exists, and the type for that
 * method is:</br>
 * <ul>
 *   <li>A Java Primative, String, or can otherwise be created
 *       directly via "new".  I didn't need to add "type=\"String\""
 *       for the GeolocCode...</li>
 *   <li>An org.cougaar.util.measure class, if properly presented</li>
 *   <li>Any StringObjectFactory-parsable Object</li>
 * <p>
 * As I first explained, it's best understood by example!
 * <p>
 * @see #main(String[]) for sample usage
 */
public class XMLObjectFactory {

  /** sample usage **/
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Specify single XML file name");
      return;
    }
    Object obj = null;
    try {
      Element root = readXMLRoot(args[0]);
      obj = parseObject(root);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("obj: "+obj);
  }

  /**
   * This <code>emptyFactory</code> can be used to create simple
   * ldm objects (e.g. <code>GeolocLocation</code>).
   */

  public static Factory emptyLFactory;
  private static Factory getEmptyFactory() {
    throw new UnsupportedOperationException(
        "XMLObjectFactory has been disabled!");
  }
  /*
  static {
    try {
      emptyLFactory = new PlanningFactory(new emptyLDMServesPlugin());
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }
  protected static class emptyLDMServesPlugin implements LDMServesPlugin {
    public void throwError() {
      throw new RuntimeException("XMLObjectFactory empty Factory!");
    }
    public MessageAddress getMessageAddress() { return null; }
    public void cachePrototype(String t, Asset a) { throwError(); }
    public boolean isPrototypeCached(String t) { throwError(); return false; }
    public Asset getPrototype(String t, Class c) { throwError(); return null; }
    public Asset getPrototype(String t) { throwError(); return null; }
    public void fillProperties(Asset a) { throwError(); }
    public PropertyGroup lateFillPropertyGroup(Asset anAsset, Class pgclass, long t) { return null; }
    public PlanningFactory getFactory() { throwError(); return null; }
    public Factory getFactory(String s) { throwError(); return null; }
    public Factory getFactory(Class c) { throwError(); return null; }
    public ClassLoader getLDMClassLoader() {
      return this.getClass().getClassLoader();
    }
    public UIDServer getUIDServer() { return null; } // don't throw the exception
    public LDMServesPlugin getLDM() { throwError(); return null; }
  }
  */

  /**
   * Public methods
   */

  public static Element readXMLRoot(String xmlFileString) {
    DOMParser parser = new DOMParser();
    try {
      parser.parse(xmlFileString);
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();
      return null;
    } catch (org.xml.sax.SAXException sae) {
      sae.printStackTrace();
      return null;
    }

    Document document = parser.getDocument();
    return document.getDocumentElement();
  }

  public static Element readXMLRoot(InputStream in) {
    DOMParser parser = new DOMParser();
    try {
      InputSource inSource = new InputSource(in);
      parser.parse(inSource);
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace();
      return null;
    } catch (org.xml.sax.SAXException sae) {
      sae.printStackTrace();
      return null;
    }

    Document document = parser.getDocument();
    return document.getDocumentElement();
  }

  /** This will limit you to simple Ldm objects **/
  public static Object parseObject(Element elem) {
    return parseObject(getEmptyFactory(), elem);
  }

  /** This will limit you to simple Ldm objects **/
  public static Object parseObject(Element elem, String type) {
    return parseObject(getEmptyFactory(), elem, type);
  }

  public static Object parseObject(Factory ldmf, 
      Element elem) {
    String type = elem.getAttribute("type");
    if ((type == null) || (type.length() <= 0)) {
      System.err.println("Object type not specified in "+elem);
      return null;
    }
    return parseObject(ldmf, elem, type);
  }

  public static Object parseObject(Factory ldmf, 
      Element elem, String type) {
    Object obj;
    if (isElementLeaf(elem)) {
      String leafVal = elem.getFirstChild().getNodeValue().trim();
      obj =  StringObjectFactory.parseObject(ldmf, type, leafVal);
    } else {
      try {
        Class cl = StringObjectFactory.findClass(type);
        if (javaUtilCollectionClass.isAssignableFrom(cl)) {
          try {
            obj = cl.newInstance();
          } catch (Exception newInstE) {
            System.err.println(
               "Unable to create instance of collection "+cl);
            return null;
          }
          fillCollection(ldmf, elem, (java.util.Collection)obj);
        } else {
          if (cl.isInterface()) {
            String name = cl.getName();
            int dot = name.lastIndexOf('.');
            if (dot != -1) name = name.substring(dot+1);
            obj = StringObjectFactory.callFactoryMethod(ldmf, name);
          } else {
            obj = cl.newInstance();
          }
          fillObject(ldmf, elem, obj);
        }
      } catch (Exception e) {
        System.err.println("Parse Object failure "+e+" in "+elem);
        e.printStackTrace();
        obj = null;
      }
    }
    return obj;
  }

  /**
   * Protected methods
   */

  protected static final boolean isElementLeaf(Element elem) {
    NodeList nlist = elem.getChildNodes(); // should memoize!
    return ((nlist.getLength() == 1) &&
            (((Node)nlist.item(0)).getNodeType() != Node.ELEMENT_NODE));
  }

  protected static final Class javaUtilCollectionClass;
  static {
    Class colClass;
    try {
      colClass = Class.forName("java.util.Collection");
    } catch (Exception e) {
      colClass = null; //never!
    }
    javaUtilCollectionClass = colClass;
  }

  protected static void fillCollection(Factory ldmf, 
      Element elem, Collection col) {
    NodeList nlist = elem.getChildNodes();
    int nlength = nlist.getLength();
    for (int i = 0; i < nlength; i++) {
      Node subNode = (Node)nlist.item(i);
      if ((subNode.getNodeType() == Node.ELEMENT_NODE) && 
          ("element".equals(subNode.getNodeName())))  {
        Object obj = parseObject(ldmf, (Element)subNode);
        if (obj != null)
          col.add(obj);
      } 
    }
  }

  protected static void fillObject(Factory ldmf,
      Element elem, Object obj) {
    NodeList nlist = elem.getChildNodes();
    int nlength = nlist.getLength();
    for (int i = 0; i < nlength; i++) {
      Node subNode = (Node)nlist.item(i);
      if (subNode.getNodeType() == Node.ELEMENT_NODE) {
        String subNodeName = subNode.getNodeName();
        if ((subNodeName == null) || (subNodeName.length() <= 0)) {
          System.err.println("Suspicious name: "+subNodeName);
          continue;
        }
        String type = ((Element)subNode).getAttribute("type");
        if ((type != null) && (type.length() == 0))
          type = null;
        Method m;
        try {
  	  if (type == null) {
            m = StringObjectFactory.findMethod(
                  obj.getClass(), 
		  "set"+subNodeName);
  	  } else {
            Class pClass = StringObjectFactory.findClass(type);
            m = StringObjectFactory.findMethod(
                  obj.getClass(), 
		  "set"+subNodeName, 
                  new Class[] {pClass});
          }
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("No such method: set"+
              subNodeName+" exception: "+e);
          continue;
        }
        if (m == null) {
          System.err.println("No method: set"+subNodeName+
              " in class: "+obj.getClass());
          continue;
        }
        if (type == null) {
          Class mps[] = m.getParameterTypes();
          if (mps.length != 1) {
            System.err.println("No method set"+subNodeName+
                " of class "+obj.getClass()+" with 1 argument");
            continue;
          }
          type = mps[0].getName();
        }
        Object oSetToObj = parseObject(ldmf, (Element)subNode, type);
        if (oSetToObj == null) {
          System.err.println("Null oSetToObj");
          continue;
        }
        try {
          Object[] invArgs = new Object[] {oSetToObj};
          m.invoke(obj, invArgs);
        } catch (Exception badInvokeE) {
          System.err.println("Bad invoke: "+badInvokeE);
        }
      }
    }
  }
}
