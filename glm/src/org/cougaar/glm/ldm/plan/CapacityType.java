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
