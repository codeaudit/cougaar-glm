/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.plugin.generic;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.glm.GLMPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.util.UnaryPredicate;

import FESI.jslib.JSException;
import FESI.jslib.JSGlobalObject;
import FESI.jslib.JSUtil;

public class GenericScriptablePlugin  
  extends GLMPlugin
{

  IncrementalSubscription[] mySubscriptions = new IncrementalSubscription[10];
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
      //	    String helperScriptFile = "/home/pcheruku/alpine/alp/external/alpine/data/generic/scripts/HelperFunctions.es";

      //Load the two basic FESI extensions.
      String extensions [] = new String[3];
      extensions[0] = "FESI.Extensions.FileIO";
      extensions[1] = "FESI.Extensions.BasicIO";
      extensions[2] = "FESI.Extensions.JavaAccess";
 
      global = JSUtil.makeEvaluator( extensions ); 

      //Read the helper methods.
      FileInputStream helperFile = new FileInputStream( helperScriptFile );
      BufferedReader helperReader = new BufferedReader( new InputStreamReader ( helperFile ) );
      global.eval( helperReader  , "Helper File Evaluator");

      //Get all the subscriptions from the script.
      FileInputStream fi = new FileInputStream( scriptFile );
      BufferedReader bf = new BufferedReader( new InputStreamReader ( fi ) );
      global.eval( bf , "Script File Evaluator");

      //setup properties in ECMAScript for the  helper methods before doing anything else.
      global.setMember("ldmf", ldmf );
      global.setMember("_plugin", this );  //for now use the _plugin variable, but later use of this should be avoided. 

      global.eval( "arrayPredicates = scriptSubscription()" );
      //global.eval( "writeln( \"array predicates are : \"); " );
      //global.eval( "writeln( arrayPredicates )" );
      global.eval( "scriptVector = arrayToVector( arrayPredicates )" );
      Vector myPredicates = new Vector();
      myPredicates = (Vector) global.getMember("scriptVector");

      System.out.println( " The no. of predicates returned by ECMAScript are: " + myPredicates.size()  ); 

      int i = 0;
      Enumeration enumpreds = myPredicates.elements();
      while ( enumpreds.hasMoreElements() ) {
        UnaryPredicate myPred = (UnaryPredicate)(enumpreds.nextElement());
        mySubscriptions[i] =  (IncrementalSubscription) subscribe(myPred);
        i++;
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
      global.setMember( "mySubscriptions", mySubscriptions );
      global.eval(" scriptExecute( mySubscriptions ) " );
    } catch ( JSException je ) {
      System.out.println( " Exception when trying to run the scriptExecute method from the script file");
      je.printStackTrace();
    }
	
  }


  //Work around for a possible ( have to investigate more ) bug in ECMAScript. Just calls the getSubscriber();
  public BlackboardService getPluginSubscriber() {
    return getBlackboardService();
  }
}

