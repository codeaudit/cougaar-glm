/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

/**
 * CapacityType - the type of capacity an Asset has.
 * This interface defines most of the used Capacity types.
 * However, MOS/43M etc can also be used for Capacity type
 *
 *  @author  ALPINE <alpine-software@bbn.com>
 *  @version $Id: CapacityType.java,v 1.1 2000-12-15 20:18:01 mthome Exp $
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
