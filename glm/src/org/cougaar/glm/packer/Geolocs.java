// Copyright (9/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.glm.packer;

import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;

import org.cougaar.glm.ldm.GLMFactory;
import org.cougaar.glm.ldm.plan.NewGeolocLocation;
import org.cougaar.glm.ldm.plan.GeolocLocation;


/**
  * An abstract class with some static methods that make it easier
  * for us to work with Geolocs.
  * This code mostly based on code written by LCG.
  */
public abstract class Geolocs {
  public static GeolocLocation abuDhabi() {
    NewGeolocLocation geoloc = GLMFactory.newGeolocLocation();
    geoloc.setName("Abu Dahbi, UARE");
    geoloc.setCountryStateCode("Abu Dahbi, UARE");
    geoloc.setCountryStateName("Abu Dahbi, UARE");
    geoloc.setGeolocCode("AAVW");
    geoloc.setIcaoCode("AAVW");
    geoloc.setLatitude(Latitude.newLatitude("24.4331"));
    geoloc.setLongitude(Longitude.newLongitude("54.6489"));
    geoloc.setInstallationTypeCode("MAP");			
    return (GeolocLocation)geoloc;
  }

  public static GeolocLocation blueGrass() {
    NewGeolocLocation geoloc = GLMFactory.newGeolocLocation();
    geoloc.setName("Blue Grass Depot, KY");
    geoloc.setCountryStateCode("Blue Grass Depot, KY");
    geoloc.setCountryStateName("Blue Grass Depot, KY");
    geoloc.setGeolocCode("BVJS");
    geoloc.setIcaoCode("BVJS");
    geoloc.setLatitude(Latitude.newLatitude("37.7"));
    geoloc.setLongitude(Longitude.newLongitude("-84.2167"));
    geoloc.setInstallationTypeCode("AMO");			
    return (GeolocLocation)geoloc;    
  }

  public static GeolocLocation asmara() {
    NewGeolocLocation geoloc = GLMFactory.newGeolocLocation();
    geoloc.setName("ASMARA");
    geoloc.setCountryStateCode("ER");
    geoloc.setCountryStateName("ERITREA");
    geoloc.setGeolocCode("ZNKY");
    geoloc.setIcaoCode("ZNKY");
    geoloc.setLatitude(Latitude.newLatitude("15.2906"));
    geoloc.setLongitude(Longitude.newLongitude("38.9102"));
    geoloc.setInstallationTypeCode("JAP");			
    return (GeolocLocation)geoloc;    
  }
}





