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
  
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cougaar.core.blackboard.Subscriber;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The PerturbationNode class encapsulates all of the information
 * the PerturbationScheduler needs in or to manage the scheduling 
 * and execution of each of the specified perturbations.
 */
public class PerturbationNode
{   
  public static final String OBJECT = "object";
  public static final String START = "start";
  public static final String CYCLES = "cycles";
  public static final String PERIODICITY = "periodicity";
   
  public Runnable job;
  public long executeAt;
  public long interval;
  public int count;
  public Subscriber subscriber;
   
  public String threadId;
  /**@shapeType AggregationLink
     @associates <b>Perturbation</b>*/
  private Vector lnkUnnamed1;
  private PerturbationPlugin pertPlugin_; 

  /**
   * @param perturbationData XML perturbation data node
   * @param subscriber The asset subscriber
   */
  public PerturbationNode( Node perturbationData, Subscriber subscriber, PerturbationPlugin pertPlugin )
  {	
    this.pertPlugin_ = pertPlugin;
    this.subscriber = subscriber;
    setJob( perturbationData );
    parseData( perturbationData );
  }

  public PerturbationPlugin getPerturbationPlugin()
  {
    return pertPlugin_;
  }
  /**
   * Sets the perturbation job.
   * @param jobData The perturbation in data in XML to be run
   */	
  public void setJob ( Node jobData )
  {
    Perturbation tempJob = new Perturbation(jobData, pertPlugin_ );
    tempJob.setSubscriber(subscriber );
    job = (Runnable)tempJob;
      
  }
   
  /**
   * Sets the perturbation job.
   */
  public void setJob ( Runnable theJob )
  {
    job = theJob;
  }
   
  /**
   * Sets the execution time of the job.
   * @param theTime The execution time of the job
   */
  public void setExecutionTime ( String theTime )
  {
    Calendar executionTime;
    StringTokenizer tokenizer;
	  	  
    threadId = theTime;
    int index = 0;
    int timeAmount = 0;
	  
    // Get the current scenario time
    long currentMills = subscriber.getClient().currentTimeMillis();
    // MML Check to see if the start time is specified in hours
    // Set the execution time accordingly.
    if ((index = threadId.indexOf("hours")) != -1)
      {
	timeAmount = (new Integer((threadId.substring(0, index)).
				  trim())).intValue();
	executeAt = currentMills + ((timeAmount * 60 * 60) * 100);	       
      }// if statement
    else if ((index = threadId.indexOf("mins")) != -1)
      {
	timeAmount = (new Integer((threadId.substring(0, index)).
				  trim())).intValue();
	executeAt = currentMills + ((timeAmount * 60) * 100);	       
      }// if statement
    else if ((index = threadId.indexOf("secs")) != -1)
      {
	timeAmount = (new Integer((threadId.substring(0, index)).
				  trim())).intValue();
	executeAt = currentMills + (timeAmount * 100);       
      }// if statement
    // If the time interval is a CDAY
    else if ((index = threadId.indexOf("CDAY")) != -1)
      {
	// Search for the plus sign
	if ((index = threadId.indexOf("+")) != -1)
	  {
	    timeAmount =  (new Integer((threadId.
					substring(index + 1, threadId.length())).trim())).intValue();
	    executeAt = currentMills + ((((timeAmount * 24) * 60) * 60) * 100);
	  }  	       
      }// if statement
    // If the time interval is in milliseconds
    else if ((index = threadId.indexOf("milliseconds")) != -1)
      {
	timeAmount = (new Integer((threadId.substring(0, index)).
				  trim())).intValue();
	executeAt = currentMills + timeAmount;	       
      }// if statement	  
  }
   
  /**
   * Sets the execution time of the job.
   * @param theTime The execution time of the job
   */
  public void setExecutionTime ( long theTime )
  {
    executeAt = theTime;
  }
   
  /** 
   * Returns the execution time of the job.
   */
  public long getExecutionTime()
  {
    return executeAt;
  }
   
  /**
   * Sets the interval of the job.
   * @param theInterval The interval of the job
   */
  public void setInterval ( String theInterval )
  {
    setInterval( ( new Long( theInterval ) ).longValue() );
    setExecutionTime( System.currentTimeMillis() + interval );
  }
   
  /**
   * Sets the interval of the job.
   * @param theInterval The interval of the job
   */
  public void setInterval ( long theInterval )
  {
    interval = theInterval;
  }

  /**
   * Sets the number of times to repeat the perturbation.
   * @param theCount Number of times to repeat the perturbation
   */
  public void setCount ( String theCount )
  {
    setCount( (new Integer( theCount )).intValue() );
  }
   
  /**
   * Sets the number of times to repeat the perturbation.
   * @param theCount Number of times to repeat the perturbation
   */
  public void setCount ( int theCount )
  {
    count = theCount;
  }

  /**
   * Parses the perturbation data.
   * @param node XML perturbation data node
   */
  public void parseData(Node node) {

    // is there anything to do?
    if (node == null) {
      return;
    }

    realParseData( node, "firstrun");
  }

  /**
   * Recursively parses the perturbation data.
   * @param node XML perturbation data node
   * @param previousName Previous XML node name
   */
  private void realParseData( Node node, String previousName )
  {
    // Is there anything to do?
    if (node == null) {
      return;
    }
		
    int type = node.getNodeType();
    switch (type) 
      {
	// print element with attributes
      case Node.ELEMENT_NODE: {
	String tempName = node.getNodeName();
	NodeList children = node.getChildNodes();
	if (children != null) {
	  int len = children.getLength();
	  for (int i = 0; i < len; i++) {
	    realParseData(children.item(i), tempName);
	  }
	}
	break;
      }
            
	// print text
      case Node.TEXT_NODE: {
	processIt(previousName, node.getNodeValue());
	break;
      }
      }
  } // realParseData(Node,String)
   
  /**
   * Sets the appopriate perturbation value based on the
   * XML tag.
   * @param stuff XML perturbation data tag type
   * @param value XML perturbation data tag value
   */
  protected void processIt(String stuff, String value)
  {

    if( (stuff != null) && (value != null) )
      {
	// set the Executiontime
	if (stuff.compareTo(START) == 0)
	  setExecutionTime ( value );		 
		  
	// set the Count
	if (stuff.compareTo(CYCLES ) == 0)
	  setCount ( value );
		  
	// set the Interval tied to this perturbation
	if (stuff.compareTo( PERIODICITY ) == 0)
	  setInterval ( value );
      }
  }
}
