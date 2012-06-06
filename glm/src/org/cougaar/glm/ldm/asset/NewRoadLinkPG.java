/*
 * <copyright>
 *  
 *  Copyright 1997-2012 Raytheon BBN Technologies
 *  under partial sponsorship of the Defense Advanced Research Projects
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

/* @generated Wed Jun 06 08:28:58 EDT 2012 from alpprops.def - DO NOT HAND EDIT */
/** Additional methods for RoadLinkPG
 * offering mutators (set methods) for the object's owner
 **/

package org.cougaar.glm.ldm.asset;

import org.cougaar.planning.ldm.measure.*;
import org.cougaar.planning.ldm.asset.*;
import org.cougaar.planning.ldm.plan.*;
import java.util.*;

import  org.cougaar.glm.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.*;
import org.cougaar.glm.ldm.policy.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.glm.execution.common.InventoryReport;



public interface NewRoadLinkPG extends RoadLinkPG, NewPropertyGroup, org.cougaar.planning.ldm.dq.HasDataQuality {
  void setDirection(int direction);
  void setInUrbanArea(boolean in_urban_area);
  void setNumberOfLanes(int number_of_lanes);
  void setMaximumHeight(int maximum_height);
  void setMaximumWidth(int maximum_width);
  void setLinkID(int link_ID);
  void setStateCode(int state_code);
  void setRoute1(String route_1);
  void setRoute2(String route_2);
  void setMedian(String median);
  void setAccessType(String access_type);
  void setTruckRoute(int truck_route);
  void setFunctionalClass(int functional_class);
  void setMaximumConvoySpeed(float maximum_convoy_speed);
  void setConvoyTravelTime(float convoy_travel_time);
  void setNumberOfBridgesUnderHS20(long number_of_bridges_under_HS20);
  void setMaxSpeed(Speed max_speed);
  void setMaxCapacity(Capacity max_capacity);
  void setMaxWeight(Mass max_weight);
  void setLinkName(String link_name);
  void setLinkLength(Distance link_length);
}
