/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.util;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.ConfigFinder;

import java.io.File;

import java.util.Iterator;
import java.util.Vector;

import org.cougaar.lib.param.Param;
import org.cougaar.lib.param.ParamTable;
import org.cougaar.lib.xml.parser.ParamParser;

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
 * plugin = com.bbn.tops.plugins.ground.TOPSGAFortAggregatorPlugIn
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
   * @see org.cougaar.lib.param.ParamTable#UTILParamTable(Vector)
   * @param envParams vector of environment parameters
   * @param cluster pointer to the cluster
   */
  public UTILParamTable(Vector envParams, ClusterIdentifier cluster){
    super(finalizeStuff(envParams, cluster));
  }

  /**
   * Help for the constructor.  Sets the name of the parameter
   * file.  If this file name is not set in the ini file 
   * (and passed in the envParams variable) then it is set
   * to be the name of the cluster plus env.xml.
   */
  private static Vector finalizeStuff(final Vector envParams, ClusterIdentifier cluster){
    boolean hasfile = false;
    boolean hasdir = false;

    Vector paramCopy = new Vector (envParams);

    Iterator runtimeParams = paramCopy.iterator();
    while(runtimeParams.hasNext()){
      String runtimeParam = (String)runtimeParams.next();
      Param p = ParamParser.getParam(runtimeParam);
      if(p != null){
	String name = p.getName();
	
	if(name.equals("envFile")){
	  hasfile = true;
	}
	else if(name.equals("envDir")){
	  hasdir = true;
	}
      }
    }

    if(!hasfile){
      String newParam = "envFile={String}" + cluster.getAddress() + ".env.xml";
      //System.out.println ("UTILParamTable.finalizeStuff() - Adding param " + newParam);
      paramCopy.add(newParam);
    }

    return paramCopy;
  }
}

