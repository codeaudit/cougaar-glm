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

package org.cougaar.domain.mlm.ui.data;

/*
  Define a location object that is created from the XML returned by
  a PlanServiceProvider.  Note that the values returned by this class
  may be null.  For example, a non-null GeolocCode will be returned
  only if the original location (in the log plan object) was a GeolocLocation.
  The comments for each get method indicate from what interfaces
  (in org.cougaar.domain.planning.ldm.plan) the information was originally obtained.
  Note that GeolocLocation, IcaoLocation, and DodaacLocations also
  include the information from NamedPosition and Position,
  and NamedPosition also includes the information from Position.
*/

public interface UILocation {

  /** @return String - the string containing the class of location, one of "Position", "NamedPosition", "GeolocLocation", "IcaoLocation", "DodaacLocation"  */

  String getLocationType();

  /*
    From Position.
  */

  /** @return double - the Latitude in degrees representing this position */
  double getLatitude();
  
  /** @return double - the Longitude in degrees representing this position */
  double getLongitude();

  /*
    From NamedPosition.
  */
        
  /** @return String - the string name representing this position */
  String getName();

  /*
    From GeolocLocation.
  */

  /** @return String - the geoloc code representing this position */
  String getGeolocCode();
        
  /** @return String - the installation type code representing this position*/
  String getInstallationTypeCode();
        
  /** @return String - the Country state code representing this position*/
  String getCountryStateCode();
        
  /** @return String  - the Country state name representing this position*/
  String getCountryStateName();

  /*
    From GeolocLocation or IcaoLocation.
  */
        
  /** @return String  - the Icao code representing this position*/
  String getIcaoCode();

  /*
    From DodaacLocation.
  */

  /** @return String - the dodaac code representing this position */
  //  String getDodaacCode();
        
  /* The mailing, shipping, and billing address information
     is from the MailingAddress interface.
   */

  /* Returns up to 5 address lines.
   */
  //  String[] getMailingAddressLines();

  //  String getMailingAddressCity();
  
  //  String getMailingAddressState();

  //  String getMailingAddressZip();

  //  String getMailingAddressCountry();
        
  /* Returns up to 5 address lines.
   */
  //  String[] getShippingAddressLines();

  //  String getShippingAddressCity();
  
  //  String getShippingAddressState();

  //  String getShippingAddressZip();

  //  String getShippingAddressCountry();
        
  /* Returns up to 5 address lines.
   */
  //  String[] getBillingAddressLines();

  //  String getBillingAddressCity();
  
  //  String getBillingAddressState();

  //  String getBillingAddressZip();

  //  String getBillingAddressCountry();
        
  /** @return String - the String representing the service ID */
  //  String getServiceID();

}
