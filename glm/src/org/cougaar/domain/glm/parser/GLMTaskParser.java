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

package org.cougaar.domain.glm.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.domain.planning.ldm.*;
import org.cougaar.domain.planning.ldm.asset.*;
import org.cougaar.domain.planning.ldm.plan.*;
import org.cougaar.domain.planning.ldm.measure.*;

import org.cougaar.util.ConfigFinder;

import org.cougaar.lib.plugin.UTILEntityResolver;
import org.cougaar.lib.util.UTILRuntimeException;
import org.cougaar.domain.glm.xml.parser.TaskParser;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Class that creates a list of tasks.  The tasks are read in 
 * from an XML file.  Use the getTasks() method to retreive
 * an Enumeration of the tasks in the XML file.
 *
 */
public class GLMTaskParser{

  /**
   * Constructor.  The constructor will parse the given xml file
   * and the tasks represented in that file will become the 
   * "associated task list" for this instance of the task parser.
   * @param pfile the name of the parameter file.
   */
  public GLMTaskParser(String pfile, 
		       RootFactory ldmf, 
		       ClusterServesPlugIn cluster) {
    try{
      DOMParser parser = new DOMParser();
      parser.setFeature(
                 "http://apache.org/xml/features/allow-java-encodings", true);
      parser.setEntityResolver (new UTILEntityResolver ());

      InputStream inputStream = myCluster.getConfigFinder().open(pfile);
      parser.parse(new InputSource (inputStream));
      Document doc = parser.getDocument();
      //System.out.println("making dom parser ");

      myLdmf     = ldmf;
      myCluster = cluster;
      myLdm     = cluster.getLDM();
      myTasks   = this.getTaskList(doc);
    }
    catch(FileNotFoundException fnfe){
      System.err.println("\nCould not find file : " + fnfe.getMessage () + ".");
    }
    catch(Exception e){
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Get an enumeration of the tasks associated with this
   * instance of the task parser.
   */
  public Enumeration getTasks(){
    return myTasks.elements();
  }
  
  /**
   * Get the tasks out of the XML task file.
   * @param doc the xml document to parse
   */
  private Vector getTaskList(Document doc){
    Node     root     = doc.getDocumentElement();
    Vector   taskbuf  = new Vector();

    if(root.getNodeName().equals("tasklist")){
      NodeList nlist = root.getChildNodes();
      int nlength = nlist.getLength();

      for(int i = 0; i < nlength; i++){
        Node node = nlist.item(i);
        //System.out.println(node.getNodeName());
        //System.out.println(node.getNodeType());
        if(node.getNodeType() == Node.ELEMENT_NODE){
          Task task = TaskParser.getTask(myLdm, myCluster, myLdmf, node);
	  taskbuf.addElement(task);
        }
      }
    }
    else{
      throw new UTILRuntimeException("unrecognized task field: " + 
				     root.getNodeName());
    }

    return taskbuf;
  }

  private ClusterServesPlugIn   myCluster  = null;
  private RootFactory            myLdmf     = null;
  private Vector                myTasks    = null;
  private LDMServesPlugIn       myLdm      = null;
}
