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

package org.cougaar.mlm.plugin.ldm;

/** A QueryHandler which wants to be execute()ed periodically.
 * The requested frequency is specified as the parameter Frequency.
 * if Frequency is not supplied or is <= 0, the execute method
 * will be called exactly once.
 * Frequency is x calls per second!
 **/

public abstract class PeriodicQuery extends QueryHandler {
  public PeriodicQuery() {}

  private int myFrequency = 0;

  protected int getFrequency() { return myFrequency; }

  /** main interface to PeriodicQuery. Called each time 
   * to run the query.
   **/
  public void start() {
    String fs = getParameter("Frequency");
    if (fs != null) {
      myFrequency = 1000 * Integer.parseInt(fs);
    }
    
    execute();

    if (myFrequency > 0) {
      Thread myThread = new Thread(
                                   new Runnable() {
                                       public void run() {
                                         while (true) {
                                           try {
                                             Thread.sleep(getFrequency());
                                           } catch (Exception e) {}

                                           try {
                                             execute();
                                           } catch (Exception e) {
                                             System.err.println("PeriodicQuery loop "+this+" Caught "+e);
                                             return;
                                           }
                                         }
                                       }}, this.toString()+"/PeriodicQuery("+(getFrequency()/1000)+")");
      myThread.start();
    }
  }
  
  

  protected void execute() 
  {
    startQuery();                 // let the query have some state
	
    String q = getQuery();		
    myLDMPlugin.executeSQL(q, this);
	  
    endQuery();                   // tell the query it is done.
  }
  
  public String getQueryString(String query) { 
    return getParameter(query);
  }
}
