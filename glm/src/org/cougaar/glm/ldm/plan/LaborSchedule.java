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

package org.cougaar.glm.ldm.plan;

import org.cougaar.planning.ldm.plan.Schedule;

/**
 * A LaborSchedule is a special class of Schedule that actually contains 2
 * Schedule objects.  For now, these schedule objects should be a schedule
 * containing QuantityScheduleElements and a schedule containing
 * RateScheduleElements.  This interface extends the Schedule interface.
 * This class may become more generic after the 99 demo.
 **/
 
public interface LaborSchedule extends Schedule {
   
  /** 
   * Return the Schedule containing the QuantityScheduleElements
   * NOTE: this is a copy of the underlying Schedule!
   * @return Schedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.QuantityScheduleElement
   **/
  Schedule getQuantitySchedule();
   
  /**
   * Return the Schedule containing the RateScheduleElements
   * NOTE: this is a copy of the underlying Schedule!
   * @return Schedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.RateScheduleElement
   **/
  Schedule getRateSchedule();
   
  /**
   * Replace the QtySchedule with a new one.
   * Note that this method completely removes any current QtySchedule and
   * its elements and replaces it with the Schedule passed in the argument.
   * @param aQuantitySchedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.QuantityScheduleElement
   */
  void setQuantitySchedule(Schedule aQuantitySchedule);
   
  /**
   * Replace the RateSchedule with a new one.
   * Note that this method completely removes any current RateSchedule and
   * its elements and replaces it with the Schedule passed in the argument.
   * @param aRateSchedule
   * @see org.cougaar.planning.ldm.plan.Schedule
   * @see org.cougaar.glm.ldm.plan.RateScheduleElement
   */
  void setRateSchedule(Schedule aRateSchedule);
   
}
