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
 */

package org.cougaar.glm.ldm.plan;

/**
 * CapacityType - the type of capacity an Asset has.
 * This interface defines most of the used Capacity types.
 * However, MOS/43M etc can also be used for Capacity type
 *
 *  @author  ALPINE <alpine-software@bbn.com>
 *
 */

public interface CapacityType { 

  String HETTRANSPORTATION = "HETTransportation";                  //Tons
  String AMMUNITIONTRANSPORTATION = "AmmunitionTransportation";    //Tons
  String CONTAINERTRANSPORTATION = "ContainerTransportation";      //Count
  String NONCONTAINERTRANSPORTATION = "NonContainerTransportation";//Tons
  String PASSENGERTRANSPORTATION = "PassengerTransportation";      //Count
  String WATERTRANSPORTATION = "WaterTransportation";              //Gallons
  String FUELTRANSPORTATION = "FuelTransportation";                //Gallons
  String AMMUNITIONHANDLING = "AmmunitionHandling";                //TonsPerDay
  String FUELHANDLING = "FuelHandling";                            //GallonsPerDay
  String FUELSTORAGE = "FuelStorage";                              //Gallons
  String AMMUNITIONSTORAGE = "AmmunitionStorage";                  //Tons
  String WATERSTORAGE = "WaterStorage";                            //Gallons
  String MATERIELSTORAGE = "MaterielStorage";                      //Tons
  String MATERIELHANDLING = "MaterielHandling";                    //TonsPerDay
  
}
