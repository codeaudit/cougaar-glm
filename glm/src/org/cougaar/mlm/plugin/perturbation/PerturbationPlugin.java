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
package org.cougaar.mlm.plugin.perturbation;

  import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.UnaryPredicate;

/**
  * The PerturbationPlugin is intended to be a test tool which 
  * allows for the modification and/or deletion of Log Plan
  * objects in Scenario Time.
  *
  *
  */
public class PerturbationPlugin extends SimplePlugin
/**
  * The PerturbationPlugin class subscribes for all cluster
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
  private PlanningFactory myPlanningFactory_;
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
	protected PlanningFactory getMyPlanningFactory()
	{
		return myPlanningFactory_;
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
	
	// Get the Plugin Parameters
	Vector params = getParameters();
	
	// Instantiate the PerturbationReader and get the
	// generated Perturbations.
	if (params == null)
	{
		throw new RuntimeException( "PerturbationPlugin requires " +
				"a parameter");
	}
	String xmlfilename = (String) params.elementAt(0);
	globalParameters.put( "XMLFile", xmlfilename );
	PerturbationReader pr = new PerturbationReader( xmlfilename, subscriber_, this );
	perturbations_ = pr.getPerturbations();
	System.out.println("\n<<<PerturbationPlugin>>> " + 
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
    * Normally this method would execute the Plugins functionality
	* every time it is awaken.  Currently, in the PerturbationPlugin
	* this type of scheduling is handled by the PerturbationScheduler
	* so that this method has no functionality.  This may change in
	* later releases of this Plugin.
	*/
  protected void execute()
  {
     myPlanningFactory_ = getFactory();
  }
 
}
