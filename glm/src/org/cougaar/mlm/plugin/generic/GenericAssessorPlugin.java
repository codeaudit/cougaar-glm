/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.mlm.plugin.generic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.glm.GLMPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.util.UnaryPredicate;

import FESI.jslib.JSException;
import FESI.jslib.JSGlobalObject;
import FESI.jslib.JSUtil;

public class GenericAssessorPlugin 
  extends GLMPlugin 
{

  IncrementalSubscription[] mySubscriptions;
  JSGlobalObject global = null; 

  protected void setupSubscriptions() {
    PlanningFactory ldmf = theLDMF;

    try { 
      Vector all_parameters = getParameters();
      String scriptFile = (String)all_parameters.elementAt(0);
      System.out.println( " The scriptName is: " + scriptFile );

      String alpInstallPath = System.getProperty("org.cougaar.install.path");
      String sep = System.getProperty("file.separator");
      String helperScriptFile = alpInstallPath + sep + "external" + sep + "alpine" + sep + "data" + sep + "generic" + sep + "scripts" + sep + "HelperFunctions.es";

      //Load the two basic FESI extensions.
      String extensions[] = new String[3];
      extensions[0] = "FESI.Extensions.FileIO";
      extensions[1] = "FESI.Extensions.BasicIO";
      extensions[2] = "FESI.Extensions.JavaAccess";
 
      global = JSUtil.makeEvaluator( extensions ); 

      //setup properties in ECMAScript for the  helper methods before doing anything else.
      global.setMember("ldmf", ldmf );
      global.setMember("_plugin", this );
      global.setMember("alert", "false" );
	    
      //Read the helper methods.
      FileInputStream helperFile = new FileInputStream( helperScriptFile );
      BufferedReader helperReader = new BufferedReader( new InputStreamReader ( helperFile ) );
      global.eval( helperReader  , "Helper File Evaluator");

      //Get all the subscriptions from the script.
      FileInputStream fi = new FileInputStream( scriptFile );
      BufferedReader bf = new BufferedReader( new InputStreamReader ( fi ) );
      global.eval( bf , "Script File Evaluator");

      global.eval( "arrayPredicates = expector()" );
      global.eval( "scriptVector = arraytoVector( arrayPredicates )" );
      Vector myPredicates = (Vector) global.getMember("scriptVector");

      for (int i=0; i<myPredicates.size(); i++ ) {
        mySubscriptions[i] = (IncrementalSubscription) subscribe( (UnaryPredicate)myPredicates.elementAt(i) );
      }


    } catch (JSException e) { 
      System.err.println(e); 
      System.exit(1); 
    } catch (FileNotFoundException fe ) {
      System.err.println("FileNotFoundException. Please give the right parameters to the plugin" );
      fe.printStackTrace();
    }

  }

  public void execute() {

    try {
      global.eval(" monitor( mySubscriptions ) " );
      String alertString = (String)global.getMember("alert");
      if ( alertString.trim().equals("true") ) {
        global.eval( "alert()");
      }
    } catch ( JSException je ) {
      System.out.println( " Exception when trying to run the scriptExecute method from the script file");
      System.out.println( " Please check the scriptExecute method for ECMAScript syntax errors" );
      je.printStackTrace();
    }
	
  }


}
