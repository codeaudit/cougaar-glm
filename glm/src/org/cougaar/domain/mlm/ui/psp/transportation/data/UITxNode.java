/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

public class UITxNode implements org.cougaar.util.SelfPrinter, java.io.Serializable {

  private String id;
  private String readableName;
  private String geoloc;
  private float latitude;
  private float longitude;

  public UITxNode() {}

  /**
   * get and set access methods for <code>Id</code>
   * <p>
   * some unique identifier
   */
  public String getId() {return id;}
  public void setId(String id) {this.id=id;}

  /**
   * get and set access methods for <code>ReadableName</code>
   * <p>
   * a more useful name
   */
  public String getReadableName() {return readableName;}
  public void setReadableName(String readableName) {
    this.readableName = readableName;
  }

  /**
   * get and set access methods for <code>Geoloc</code>
   * <p>
   * GEOLOC code, where applicable
   */
  public String getGeoloc() {return geoloc;}
  public void setGeoloc(String geoloc) {this.geoloc=geoloc;}

  /**
   * get and set access methods for <code>Latitude</code>
   * <p>
   * geographic location latitude in degrees
   */
  public float getLatitude() {return latitude;}
  public void setLatitude(float latitude) {
    this.latitude = latitude;
  }

  /**
   * get and set access methods for <code>Longitude</code>
   * <p>
   * geographic location longitude in degrees
   */
  public float getLongitude() {return longitude;}
  public void setLongitude(float longitude) {
    this.longitude = longitude;
  }

  public void printContent(org.cougaar.util.AsciiPrinter pr) {
    pr.print(id, "Id");
    pr.print(readableName, "ReadableName");
    pr.print(geoloc, "Geoloc");
    pr.print(latitude, "Latitude");
    pr.print(longitude, "Longitude");
  }

  public String toString() {
    return org.cougaar.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 4011433229219707962L;

}
