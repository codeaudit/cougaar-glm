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

import java.util.Calendar;
import java.util.Date;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.NewRoleSchedule;
import org.cougaar.planning.ldm.plan.NewSchedule;

/** NamedAssetCreator creates sql queries (thorough its superclass)
 *  and processes the query results by creating assets with bumpernumbers.
 */

public class SQLNamedAssetCreator extends PeriodicQuery {
    
  public SQLNamedAssetCreator() { }

  public void processRow(Object[] data) {
      
    String nsn = (String)data[0];
    //Number count = (Number)data[1];
    String bumper = (String)data[1];
    String nomenclature = (String) data[2];

    System.out.println(myMessageAddress.getAddress() + ": " +
		       "Creating an instance of NSN/" + nsn + " " + bumper + " " + nomenclature);

      Asset newasset = createAsset("NSN/" + nsn, bumper, nomenclature);
      setupAvailableSchedule(newasset);
      
      publishAdd(newasset);
    
  }

  public String getQuery() { return (String) getParameter("query");  }

  protected Asset createAsset(String prototype, 
			      String uniqueid, String nomenclature) 
  {
    PlanningFactory ldmfactory = getLDM().getFactory();
        
    //    System.out.println("Creating asset : " + prototype + " " + 
    //		       uniqueid + " " + nomenclature);
        
    return ldmfactory.createInstance(prototype, uniqueid);

  }
  
  private void setupAvailableSchedule(Asset asset) {
    PlanningFactory ldmfactory = getLDM().getFactory();
    Calendar mycalendar = Calendar.getInstance();
    // set the start date of the available schedule to 01/01/1990
    mycalendar.set(1990, 0, 1, 0, 0, 0);
    Date start = mycalendar.getTime();
    // set the end date of the available schedule to 01/01/2010
    mycalendar.set(2010, 0, 1, 0, 0, 0);
    Date end = mycalendar.getTime();
    NewSchedule availsched = ldmfactory.newSimpleSchedule(start, end);
    // set the available schedule
    ((NewRoleSchedule)asset.getRoleSchedule()).setAvailableSchedule(availsched);
  }
    
  
        
}
