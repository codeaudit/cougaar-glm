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

package org.cougaar.mlm.ui.data;


public interface UISimpleNamedScheduleNames {
  String ON_HAND = "On Hand";
  String ON_HAND_DETAILED = "On Hand Detailed";
  String DUE_IN = "Due In";
  String DUE_OUT = "Due Out";
  String PROJECTED_DUE_OUT = "Projected Due Out";
  String REQUESTED_DUE_IN = "Requested Due In";
  String PROJECTED_DUE_IN = "Projected Due In";
  String REQUESTED_DUE_OUT = "Requested Due Out";
  String PROJECTED_REQUESTED_DUE_OUT = "Projected Requested Due Out";
  String PROJECTED_REQUESTED_DUE_IN = "Projected Requested Due In";
  String REQUESTED_DUE_OUT_SHORTFALL = "Requested Due Out Shortfall";
  String UNCONFIRMED_DUE_IN = "Unconfirmed Due In";
  String ALLOCATED = "Allocated";
  String AVAILABLE = "Available";
  String TOTAL_LABOR = "Labor";
  String TOTAL_LABOR_8 = "Labor 8 Hours/Day";
  String TOTAL_LABOR_10 = "Labor 10 Hours/Day";
  String TOTAL_LABOR_12 = "Labor 12 Hours/Day";

  String GOAL_LEVEL = "Goal Level";
  String REORDER_LEVEL = "Reorder Level";
  String AVERAGE_DEMAND_LEVEL = "Average Demand";
  String ON_HAND_MOCK_PERIOD = "On Hand w/Mock Period";    
  String PROJECTED_DUE_IN_MOCK_PERIOD = "Projected Due In w/Mock Period";
  String PROJECTED_DUE_OUT_MOCK_PERIOD = "Requested Due Out w/Mock Period";
  String PROJECTED_REQUESTED_DUE_OUT_MOCK_PERIOD = "Projected Requested Due Out w/Mock Period";
  String PROJECTED_REQUESTED_DUE_IN_MOCK_PERIOD = "Projected Requested Due In w/Mock Period";
   
  String INACTIVE = "_INACTIVE"; // Suffix for inactive schedules
}
