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
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

public class UITxNode implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

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

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(id, "Id");
    pr.print(readableName, "ReadableName");
    pr.print(geoloc, "Geoloc");
    pr.print(latitude, "Latitude");
    pr.print(longitude, "Longitude");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  static final long serialVersionUID = 4011433229219707962L;

}
