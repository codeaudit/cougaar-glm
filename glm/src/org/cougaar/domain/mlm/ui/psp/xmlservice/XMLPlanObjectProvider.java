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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;


import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.core.util.XMLizable;

import  org.cougaar.core.util.XMLObjectProvider;

import org.cougaar.domain.mlm.ui.data.XMLUIPlanObject;
import org.cougaar.domain.mlm.ui.data.XMLUIPlanObjectConverter;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates XML for plan objects (Plan Elements, Tasks, Workflows,
 * and Assets).  Used by the Plan Service Provider in the
 * cluster to generate the XML which will be returned to the ui.
 */

public class XMLPlanObjectProvider implements XMLObjectProvider {
  protected ArrayList collection = new ArrayList();
  Document doc;
  Element root;
  Class xmlPlanObjectClass;
  Class xmlUIPlanObjectClass;
  Vector requestedFields; // each is a class name followed by a dot separated list of field names

  public XMLPlanObjectProvider() {
    // creates <?xml version="1.0"?>
    // creates <LogPlan>
    reset();
  }

  public int size() {
      return collection.size();
  }

  public ArrayList getCollection() {
      return collection;
  }

  public void  reset() {
     collection.clear();
     doc = new DocumentImpl();
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

  public void addPlanObject(Object obj) {
       collection.add(obj);
  }

  //
  // create XML for "base" plan object -- task, workflow, planelement or asset
  private void docAddPlanObject(Object obj) {
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
    writeDocument(System.out);
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

      for(int i=0; i< collection.size(); i++) {
          this.docAddPlanObject(collection.get(i));
      }

      try {
	OutputFormat format = new OutputFormat();
	format.setPreserveSpace(true);
	format.setIndent(2);

          PrintWriter pw = new PrintWriter(os);
	  XMLSerializer serializer = new XMLSerializer(pw, format);
	  serializer.serialize(doc);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  public Document getDocument() {
      // for debugging, also print it
      //    printDocument();
      return doc;
  }


  public Document getDocumentRef() {
      for(int i=0; i< collection.size(); i++) {
          this.docAddPlanObject(collection.get(i));
      }
      collection.clear();

      return doc;
  }

  // for testing
  private Document testGenerateXML() {
      Document doc = new DocumentImpl();

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
