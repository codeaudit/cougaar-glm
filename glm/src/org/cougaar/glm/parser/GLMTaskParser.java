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

package org.cougaar.glm.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.domain.*;
import org.cougaar.planning.ldm.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.planning.ldm.measure.*;

import org.cougaar.util.ConfigFinder;

import org.cougaar.lib.plugin.UTILEntityResolver;
import org.cougaar.lib.util.UTILRuntimeException;
import org.cougaar.glm.xml.parser.TaskParser;
import org.cougaar.util.log.Logger;

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
		       PlanningFactory ldmf, 
		       MessageAddress clusterIdentifier, 
		       ConfigFinder configFinder,
		       LDMServesPlugin ldmServesPlugin,
		       Logger logger) {
    try{
      this.logger = logger;
      taskParser = new TaskParser (logger);
      DOMParser parser = new DOMParser();
      parser.setFeature(
                 "http://apache.org/xml/features/allow-java-encodings", true);
      parser.setEntityResolver (new UTILEntityResolver (logger));

      InputStream inputStream = configFinder.open(pfile);
      parser.parse(new InputSource (inputStream));
      Document doc = parser.getDocument();
      //System.out.println("making dom parser ");

      myLdmf    = ldmf;
      this.clusterIdentifier = clusterIdentifier;
      myLdm     = ldmServesPlugin;
      myTasks   = this.getTaskList(doc);
    }
    catch(FileNotFoundException fnfe){
      logger.error("\nCould not find file : " + fnfe.getMessage () + ".", fnfe);
    }
    catch(Exception e){
      logger.error ("error", e);
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
          Task task = taskParser.getTask(myLdm, clusterIdentifier, myLdmf, node);
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

  private MessageAddress   clusterIdentifier  = null;
  private PlanningFactory           myLdmf     = null;
  private Vector                myTasks    = null;
  private LDMServesPlugin       myLdm      = null;
  protected Logger logger;

  protected TaskParser taskParser;
}
