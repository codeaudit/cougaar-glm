/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
