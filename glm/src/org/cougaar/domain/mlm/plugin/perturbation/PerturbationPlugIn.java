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
package org.cougaar.domain.mlm.plugin.perturbation;

  import org.cougaar.core.cluster.Subscriber;
  import org.cougaar.core.cluster.Subscription;
  import org.cougaar.core.cluster.IncrementalSubscription;

  import org.cougaar.domain.planning.ldm.asset.AggregateAsset; 
  import org.cougaar.domain.planning.ldm.asset.Asset;
  import org.cougaar.domain.planning.ldm.RootFactory;
  
  import org.cougaar.domain.glm.ldm.oplan.Oplan;
  
  import org.cougaar.core.plugin.SimplePlugIn;
  
  import org.cougaar.util.UnaryPredicate;
  
  import java.util.Enumeration;
  import java.util.Vector;
  import java.util.Properties;

/**
  * The PerturbationPlugIn is intended to be a test tool which 
  * allows for the modification and/or deletion of Log Plan
  * objects in Scenario Time.
  *
  * @version PPI v1.0
  * @author  Raython Systems Company/J. Beasley
  *
  */
public class PerturbationPlugIn extends SimplePlugIn
/**
  * The PerturbationPlugIn class subscribes for all cluster
  * assets, initiates the reading of the Perturbation data 
  * and the creation of the scheduler which starts the 
  * Perturbations as necessary.
  *
  * @see PerturbationReader PerturbationScheduler
  */
{ 
  private Subscriber subscriber_;
  private Subscription myObjects_;
  private Enumeration assetList_;
  private Vector perturbations_;
  private RootFactory myRootFactory_;
	private Properties globalParameters = new Properties();

  private static UnaryPredicate assetOrOplanPredicate() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
	return ( (o instanceof Asset) || (o instanceof Oplan) );
	//return (o instanceof Asset);
      }
    };
  }

	/* LDM Access
	*/
	protected RootFactory getMyRootFactory()
	{
		return myRootFactory_;
	}
	
  /*
   * Creates a subscription by subscribing for all known
   * assets. ( Since there is no way to know what assets   
   * have been designated for perturbation).
   */
  protected void setupSubscriptions() 
  {
      myObjects_ = subscribe(assetOrOplanPredicate());
	
	// Set up the perturbations.
	setupPerturbations();
  }
  
  /**
    * Sets up the perturbations by instantiating the 
	* @see PerturbationReader and the @see PerturbationScheduler.
	*/
  protected void setupPerturbations()
  {
    subscriber_ = myObjects_.getSubscriber();
    assetList_ = ((IncrementalSubscription)myObjects_).elements();
	
	// Get the PlugIn Parameters
	Vector params = getParameters();
	
	// Instantiate the PerturbationReader and get the
	// generated Perturbations.
	if (params == null)
	{
		throw new RuntimeException( "PerturbationPlugIn requires " +
				"a parameter");
	}
	String xmlfilename = (String) params.elementAt(0);
	globalParameters.put( "XMLFile", xmlfilename );
	PerturbationReader pr = new PerturbationReader( xmlfilename, subscriber_, this );
	perturbations_ = pr.getPerturbations();
	System.out.println("\n<<<PerturbationPlugIn>>> " + 
	   perturbations_.size() + " detected.");
	   
	// Create an vector to hold the assets
	Vector theList = new Vector();
	while ( assetList_.hasMoreElements() )
	   theList.addElement( assetList_.nextElement() );
	   
	// For each perturbation, set the assets and the 
	// subscriber.
	for ( int i = 0; i < perturbations_.size(); i++ )
	{
	   ((Perturbation)((PerturbationNode)perturbations_.
	      elementAt(i)).job).setAssets( theList );

	   ((Perturbation)((PerturbationNode)perturbations_.
	      elementAt(i)).job).setSubscriber( subscriber_ );
	}
	
	// Instantiate the PerturbationScheduler
 	PerturbationScheduler ps = 
   		new PerturbationScheduler ( perturbations_ );
 }
/*for testing*/
public void printAssets()
{
	openTransaction();
	myObjects_ = subscribe(assetOrOplanPredicate());
	closeTransaction();
	assetList_ = ((IncrementalSubscription)myObjects_).elements();
 	while (assetList_.hasMoreElements())
	{
		System.out.println("\n\n");
		System.out.println(assetList_.nextElement());
	}
}

  /**
    * Normally this method would execute the PlugIns functionality
	* every time it is awaken.  Currently, in the PerturbationPlugIn
	* this type of scheduling is handled by the PerturbationScheduler
	* so that this method has no functionality.  This may change in
	* later releases of this PlugIn.
	*/
  protected void execute()
  {
     myRootFactory_ = getFactory();
  }
 
}
