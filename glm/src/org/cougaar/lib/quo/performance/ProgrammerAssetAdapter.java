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
 *  THIS SOFTWARE IS MODIFIED FOR TESTING QUO_ULTRALLOG INTEGRATION
 */
//package org.cougaar.lib.quo.performance;
package org.cougaar.lib.quo.performance;

import org.cougaar.planning.ldm.asset.Asset;

/**
 * This COUGAAR Asset class serves as a base class for the ProgrammerAsset class.
 * The ProgrammerAsset class is generated using the AssetWriter utility
 * @author ALPINE (alpine-software@bbn.com)
 *
 */
public class ProgrammerAssetAdapter extends Asset {
    //private org.cougaar.lib.quo.performance.Schedule schedule = new Schedule();

  /**
   * Create a new ProgrammerAssetAdapter
   */
  public ProgrammerAssetAdapter() {
    super();
  }

  /**
   * Create a new ProgrammerAssetAdapter
   * @param prototype the Asset's prototype
   */
  public ProgrammerAssetAdapter(Asset prototype) {
    super(prototype);
  }

  /**
   * Get the schedule of assignments for this programmer
   * @return this programmer's schedule
   */
 //   public org.cougaar.lib.quo.performance.Schedule getSchedule() {
//      return schedule;
//    }

  /**
   * Set the schedule of assignments for this programmer
   * @param this programmer's new schedule
   */
 //   public void setSchedule(org.cougaar.lib.quo.performance.Schedule newSchedule) {
//      schedule = newSchedule;
//    }

}






