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
package org.cougaar.mlm.plugin.perturbation;

  import java.util.Calendar;
  import java.util.Date;
  import java.util.Vector ;

  import org.cougaar.planning.ldm.ClusterServesPlugin;
  
  import org.cougaar.util.ReusableThread ;
  import org.cougaar.util.ReusableThreadPool ;

/**
  * The PerturbationScheduler class schedules the PerturbationNodes
  * (or jobs) to be run as determined based on the Scenario Time.
  */
public class PerturbationScheduler implements Runnable 
{
	final public static long SECS_PER_MIN = 60;
	final public static long MINS_PER_HOUR = 60;
	final public static long HOURS_PER_DAY = 24;
	final public static long DAYS_PER_WEEK = 7;
	final public static long MILLI = 1000;
	final public static long MILLISECONDS = SECS_PER_MIN * MILLI; 
	
	final public static int MAX_CAPACITY = 100;
	final public static int ONCE = 1;
	final public static int FOREVER = -1;
	final public static long HOURLY = MINS_PER_HOUR * MILLISECONDS;
	final public static long DAILY = HOURS_PER_DAY * HOURLY;
	final public static long WEEKLY = DAYS_PER_WEEK * DAILY;
	final public static long MONTHLY = -1;
	final public static long YEARLY = -2;

	private boolean done_;
	private ReusableThreadPool threadPool_;
	private Vector perturbations_ = new Vector( MAX_CAPACITY );
  private PerturbationNode lnkUnnamed;
    /**
	  * @param perturbations The perturbations to be scheduled
	  */
	public PerturbationScheduler( Vector perturbations ) 
	{
	    int initialPoolSize ;

        // Get a reference to the reusable thread pool 
		// and start the perturbation scheduler using a
		// reusable thread.
		
		setPerturbations ( perturbations );
		initialPoolSize = getPerturbations().size();
		threadPool_ = (initialPoolSize > 0) ? 
			ReusableThreadPool.getDefaultThreadPool() : null;
		ReusableThread thread = new ReusableThread( threadPool_ );
		thread.setRunnable( this );
		thread.setDaemon(false);
		thread.start();
	}

    /**
	  * Sets the perturbations.
	  * @param theJobs The perturbations
	  */
    private void setPerturbations ( Vector theJobs )
	{
	  this.perturbations_ = theJobs;
	  setDone( false );
	}
	
	/** 
	  * Sets the done flag.
	  * @param flag Perturbation done flag
	  */
	private void setDone( boolean flag )
	{
	   this.done_ = flag;
	}
	
	/**
	  * Returns the perturbations.
	  * @return Returns the Perturbations.
	  */
	private Vector getPerturbations()
	{
	   return this.perturbations_;
	}
	
	/**
	  * Adds perturbation nodes to the scheduler's list of jobs.
	  * @param job erturbation node to be added
	  */
	private synchronized void addPerturbation(PerturbationNode job) 
	{
		perturbations_.addElement(job);
		notify();
	}

	/**
	  * Removes the perturbation from the schedulers job list.
	  * @param job Perturbation node to be removed
	  */
	private synchronized void deletePerturbation(PerturbationNode job) 
	{
	    Vector jobs;
		int remaining;
		
		jobs = getPerturbations();
		remaining = jobs.size();
PerturbationPlugin p = null;
		for (int i=0; i < remaining; i++) 
		{
			if (((PerturbationNode)jobs.elementAt(i)) == job) 
			{
				jobs.removeElementAt(i);
				remaining = jobs.size();
				System.out.println(
				   "\n<<<PerturbationPlugin>>> The number of perturbations " +
				   "remaining is: " + remaining );
				if ( remaining == 0 )
				{
				   setDone ( true );
				}
				break;
			}
		}
	}
	
	/**
	  * Removes the perturbation from the schedulers job list.
	  * @param jobNumber Perturbation job number
	  */
	private synchronized void deletePerturbation(int jobNumber)
	{
	   getPerturbations().removeElementAt(jobNumber);
	}

    /**
	  * Updates the perturbation node to reflect the next iteration
	  * of the perturbation ( if any ).
	  * @return Returns the updated PerturbationNode
	  */
	private PerturbationNode updatePerturbation(PerturbationNode pNode) 
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date( pNode.executeAt ) );
		
		if ( (pNode.count-1) >= 1 )
		{
		   pNode.setCount( pNode.count-1 );
		   pNode.setExecutionTime(pNode.executeAt + pNode.interval);
		}
		else
		{
		   return null;
		}
		return pNode;
	}

    /**
	  * Runs the perturbations as scheduled based on the 
	  * scenario time.
	  * @return Returns the number of milliseconds until the next perturbation should be run.
	  */
	private synchronized long runPerturbations() 
	{
		long minDiff = Long.MAX_VALUE;
		//long now = System.currentTimeMillis();
		long now;
		Vector p;

	    p = getPerturbations();
		for( int i=0; i < p.size(); ) 
		{
		   PerturbationNode pjob = 
		      (PerturbationNode) p.elementAt(i);
			
		   now = ((pjob.subscriber).getClient()).currentTimeMillis();

		   if (pjob.executeAt <= now )
		   {
			  if (threadPool_ != null) 
			  {  
			     ReusableThread pt = new ReusableThread(threadPool_);
				 pt.setRunnable(pjob.job);
				 pt.setDaemon(false);
				 pt.start();
				 
			     if (updatePerturbation(pjob) == null) 
			     {
				    deletePerturbation( pjob );
			     }
			  } 
			  else 
			  {
			     System.out.println
				 	("\n<<<PerturbationPlugin..." + pjob.threadId + 
					">>> ERROR::There are no available threads to " +
					"run the remaining perturbations");
			  }
		   } 
		   else 
		   {
		      long diff = pjob.executeAt - now;
			  minDiff = Math.min(diff, minDiff);
			  i++;
		   }
		}
		return minDiff;
	}

	/**
	  * Cancels the perturbation.
	  */
	public void cancel(PerturbationNode job) 
	{
		deletePerturbation(job);
	}

    /** 
	  * Starts and runs the Perturbation Scheduling thread.
	  */
	public synchronized void run() 
	{
	    System.out.println (
		   "\n<<<PerturbationPlugin>>> Starting Scheduler..........");
		while (true) 
		{
		   long waitTime = runPerturbations();
		   try {
		      System.out.println("\n<<<PerturbationPlugin>>> " +
			     "Next Perturbation Scheduled to begin in " + (waitTime/1000) + 
				 " seconds ");
			  wait(waitTime);
		   } catch (Exception e) {
		   }
		}
	}
	
	//
	// The following methods are not cuurently in use by the PerturbationPlugin.
    //

	public void execute(PerturbationNode jobNode) 
	{
		executeIn(jobNode, jobNode.executeAt );
	}

	public void executeIn(PerturbationNode jobNode, long millis) 
	{
		executeInAndRepeat(jobNode, millis, 1000, ONCE);
	}
	
	public void executeInAndRepeat(PerturbationNode jobNode, long millis, long repeat) 
	{
		executeInAndRepeat(jobNode, millis, repeat, FOREVER);
	}
	
	public void executeInAndRepeat(PerturbationNode jobNode, long millis, long repeat, int count) 
	{
		Date when = new Date(System.currentTimeMillis() + millis);
		executeAtAndRepeat(jobNode, when, repeat, count);
	}

	public void executeAt(PerturbationNode jobNode, Date when) 
	{
		executeAtAndRepeat(jobNode, when, 1000, ONCE);
	}

	public void executeAtAndRepeat(PerturbationNode jobNode, Date when, long repeat) 
	{
		executeAtAndRepeat(jobNode, when, repeat, FOREVER); 
	}

	public void executeAtAndRepeat(PerturbationNode jobNode, Date when, long repeat, int count) 
	{
		jobNode.setExecutionTime( when.getTime() );
		jobNode.setInterval( repeat );
		jobNode.setCount( count );
		addPerturbation(jobNode);
	}

	public void executeAtNextDOW(PerturbationNode jobNode, Date when, int DOW) 
	{
		Calendar target = Calendar.getInstance();
		target.setTime(when);
		while (target.get(Calendar.DAY_OF_WEEK) != DOW)
			target.add(Calendar.DATE, 1);
		executeAt(jobNode, target.getTime());
	}

	public void configureBackup(PerturbationNode job) {
		Calendar now = Calendar.getInstance();
		executeAtNextDOW(job, now.getTime(), Calendar.SUNDAY);
	}
}

