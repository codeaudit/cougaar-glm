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

import java.beans.*;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;


import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.util.XMLizable;

import com.ibm.xml.parser.TXDocument;

import  org.cougaar.core.util.XMLObjectProvider;

import org.cougaar.domain.mlm.ui.data.XMLUIPlanObject;
import org.cougaar.domain.mlm.ui.data.XMLUIPlanObjectConverter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates XML for plan objects (Plan Elements, Tasks, Workflows, 
 * and Assets).  Used by the Plan Service Provider in the
 * cluster to generate the XML which will be returned to the ui.
 */

public class XMLPlanObjectProvider implements XMLObjectProvider {
  TXDocument doc;
  Element root;
  Class xmlPlanObjectClass;
  Class xmlUIPlanObjectClass;
  Vector requestedFields; // each is a class name followed by a dot separated list of field names

  public XMLPlanObjectProvider() {
    // creates <?xml version="1.0"?>
    // creates <LogPlan>
    reset();
  }


  public void  reset() {
    doc = new TXDocument();
    doc.setVersion("1.0");
    root = doc.createElement("LogPlan");
    doc.appendChild(root);
    // define these, which we'll use to create the XML
    // for the "base" of the plan object
    try {
      xmlPlanObjectClass = XMLizable.class;
      xmlUIPlanObjectClass = XMLUIPlanObject.class;
    } catch (RuntimeException e) {
      System.out.println("Exception getting class for plan object: " + e);
    }
  }


  public XMLPlanObjectProvider(Vector requestedFields) {
    this();
    this.requestedFields = requestedFields;
  }

  //
  // Pass-thru:   to comply with org.cougaar.core.util.XMLObjectProvider interface
  public void addObject(Object obj) {
       addPlanObject(obj);
  }


  //
  // create XML for "base" plan object -- task, workflow, planelement or asset
  public void addPlanObject(Object obj) {
    Element element = null;
    // Test the getXML method on logplan object
    if (xmlPlanObjectClass.isInstance(obj)) {
      element = ((XMLizable)obj).getXML((Document)doc);
      //      System.out.println("XMLifying object of class: " + obj.getClass());
      root.appendChild(element);
    } else if (xmlUIPlanObjectClass.isInstance(obj)) {
      element = ((XMLUIPlanObject)obj).getXML((Document)doc, requestedFields);
      //      System.out.println("XMLifying object of class: " + obj.getClass());
      root.appendChild(element);
    }
    else {
      System.out.println("Attempted to add plan object of unknown class:" + obj.getClass());
    }
  }

  public void printDocument() {
    try {
      PrintWriter out = new PrintWriter(System.out);
      doc.print(out);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void writeDocumentToFile(String pathname) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(pathname);
      writeDocument(fos);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void writeDocument(OutputStream os) {
    try {
      PrintWriter pw = new PrintWriter(os);
      doc.print(pw);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public TXDocument getDocument() {
    // for debugging, also print it
    //    printDocument();
    return doc;
  }


  public Document getDocumentRef() {
    // for debugging, also print it
    //    printDocument();
    return doc;
  }

  // for testing
  private TXDocument testGenerateXML() {
    TXDocument doc = new TXDocument();
    doc.setVersion("1.0");

    Element root = doc.createElement("LogPlan");
    Element taskItem = doc.createElement("Task");
    Element item = doc.createElement("SOURCE");
    item.appendChild(doc.createTextNode("3ID"));
    taskItem.appendChild(item);
    item = doc.createElement("DESTINATION");
    item.appendChild(doc.createTextNode("MCCGlobalMode"));
    taskItem.appendChild(item);
    item = doc.createElement("VERB");
    item.appendChild(doc.createTextNode("Transport"));
    taskItem.appendChild(item);
    root.appendChild(taskItem);
    doc.appendChild(root);
    return doc;
  }

  /*
  // test is broken

  public static void main(String[] args) {
    // create fake task object to test against
    //    ClusterIdentifierImpl ci= new ClusterIdentifierImpl("myCluster");
    ClusterIdentifier ci = new ClusterIdentifier("myCluster");
    ClusterObjectFactoryImpl cof = new ClusterObjectFactoryImpl(ci);
    NewTask testTask = cof.newTask();
    testTask.setSource(ci);
    testTask.setDestination(ci);
    testTask.setVerb(Constants.Verb.Transport);
    testTask.setPlan(cof.getRealityPlan());
    // end creating fake task

    // test generation of XML for fake task
    XMLPlanObjectProvider provider = new XMLPlanObjectProvider();
    provider.addPlanObject(testTask);
    provider.printDocument();
  }

  */
}
