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
package org.cougaar.glm.execution.eg;

import java.util.List;

public class InventoryScheduleFilterGUI extends FilterGUI {
  public InventoryScheduleFilterGUI(EventGenerator anEventGenerator) {
    super(anEventGenerator);
  }

  public void addAspects(List aspects) {
    aspects.add(new FilterAspect.Time("Scheduled time",
                                      SteppedInventoryReportSchedule.class,
                                      FilterAspect.timeComparisons,
                                      null) {
      public Object getItem(Object from) {
        return new FilterAspect.TimeObject(((SteppedInventoryReportSchedule) from).getTime(),
                                           theEventGenerator.getExecutionTime());
      }
    });
    aspects.add(new FilterAspect.Str_ng("Item",
                                        SteppedInventoryReportSchedule.class,
                                        FilterAspect.stringComparisons,
                                        new String[] {"", "Inventory:DODIC/", "Inventory:NSN/"}) {
      public Object getItem(Object from) {
        return ((SteppedInventoryReportSchedule) from).getItem().toUpperCase();
      }
    });
    // Add periodicity filter
  }
}
