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

package org.cougaar.lib.util;

import java.util.Iterator;
import java.util.Vector;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.lib.param.Param;
import org.cougaar.lib.param.ParamTable;
import org.cougaar.util.log.Logger;

/**
 * <pre>
 * This is a convenience class for the UTIL plugins.  This class
 * uses the ConfigFinder to find the param files.
 *
 * To look for the parameter files in a non-default location,
 * add a system parameter like this to setarguments.csh:
 *
 * -Dorg.cougaar.config.path=$COUGAAR_INSTALL_PATH/plugins/TOPS/data;
 * 
 * WARNING : the trailing semi-colon is ABSOLUTELY necessary.
 *
 * Expects an extension of ".env.xml," which will be prefixed
 * with the name of the cluster, e.g. TRANSCOM.env.xml.  This can be
 * overridden by setting the envFile parameter (see below).
 *
 * Supports plugin specific (as opposed to Cluster-specific)
 * env files by allowing the specification
 * of both an "envDir" and/or an "envFile" following the plugin in the .ini
 * file.
 *
 * The envDir can be relative or absolute.
 *
 * For example : 
 * plugin = com.bbn.tops.plugins.ground.TOPSGAFortAggregatorPlugin
 *   (envDir={String}../../tops/data, envFile={String}FortStewartAggregator.env.xml)
 *
 * The envDir can be used if there are two files of the same name in the 
 * config file finder path (which shouldn't happen...)
 *
 * </pre>
 */
public class UTILParamTable extends ParamTable {

  /**
   * Convenience constructor.  The default name of the parameter file
   * is derived from the name of the cluster. 
   * For example TRANSCOM.env.xml.
   *
   * @see org.cougaar.lib.param.ParamTable#ParamTable
   * @param envParams vector of environment parameters
   * @param cluster pointer to the cluster
   */
  public UTILParamTable(Vector envParams, MessageAddress cluster, Logger logger){
    super(logger);
    addIniParameters (addEnvFileParam(envParams, cluster));
  }

  /**
   * Help for the constructor.  Sets the name of the parameter
   * file.  If this file name is not set in the ini file 
   * (and passed in the envParams variable) then it is set
   * to be the name of the cluster plus env.xml.
   */
  private Vector addEnvFileParam(final Vector envParams, MessageAddress cluster){
    boolean hasfile = false;
//    boolean hasdir = false;

    Vector paramCopy = new Vector (envParams);

    Iterator runtimeParams = paramCopy.iterator();
    while(runtimeParams.hasNext()){
      String runtimeParam = (String)runtimeParams.next();
      Param p = paramParser.getParam(runtimeParam);
      if(p != null){
	String name = p.getName();
	
	if(name.equals("envFile")){
	  hasfile = true;
	}
	else if(name.equals("envDir")){
//	  hasdir = true;
	}
      }
    }

    if (!hasfile) {
      String newParam = "envFile={String}" + cluster.getAddress() + ".env.xml";
      paramCopy.add(newParam);
    }

    return paramCopy;
  }
}

