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

package org.cougaar.domain.glm.ldm.plan;

/**
 * CapacityType - the type of capacity an Asset has.
 * This interface defines most of the used Capacity types.
 * However, MOS/43M etc can also be used for Capacity type
 *
 *  @author  ALPINE <alpine-software@bbn.com>
 *  @version $Id: CapacityType.java,v 1.3 2001-08-22 20:27:24 mthome Exp $
 */

public interface CapacityType { 

  static final String HETTRANSPORTATION = "HETTransportation";                  //Tons
  static final String AMMUNITIONTRANSPORTATION = "AmmunitionTransportation";    //Tons
  static final String CONTAINERTRANSPORTATION = "ContainerTransportation";      //Count
  static final String NONCONTAINERTRANSPORTATION = "NonContainerTransportation";//Tons
  static final String PASSENGERTRANSPORTATION = "PassengerTransportation";      //Count
  static final String WATERTRANSPORTATION = "WaterTransportation";              //Gallons
  static final String FUELTRANSPORTATION = "FuelTransportation";                //Gallons
  static final String AMMUNITIONHANDLING = "AmmunitionHandling";                //TonsPerDay
  static final String FUELHANDLING = "FuelHandling";                            //GallonsPerDay
  static final String FUELSTORAGE = "FuelStorage";                              //Gallons
  static final String AMMUNITIONSTORAGE = "AmmunitionStorage";                  //Tons
  static final String WATERSTORAGE = "WaterStorage";                            //Gallons
  static final String MATERIELSTORAGE = "MaterielStorage";                      //Tons
  static final String MATERIELHANDLING = "MaterielHandling";                    //TonsPerDay
  

  
}
