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
 
import org.cougaar.planning.ldm.measure.Latitude;
import org.cougaar.planning.ldm.measure.Longitude;
 
public class GeolocLocationImpl extends NamedPositionImpl
  implements GeolocLocation, NewGeolocLocation {
        
  String GeolocCode, InstallTypeCode, CSCode, CSName, IcaoCode;
        
  public GeolocLocationImpl() {
    super();
  }
  
  public GeolocLocationImpl(Latitude la, Longitude lon, String name)
  {
    super(la, lon, name);
  }
         
        
  /** @return String - the geoloc code representing this position */
  public String getGeolocCode() {
    return GeolocCode;
  }
                
  /** @return String - the installation type code representing this position*/
  public String getInstallationTypeCode() {
    return InstallTypeCode;
  }
        
  /** @return String - the Country state code representing this position*/
  public String getCountryStateCode() {
    return CSCode;
  }
        
  /** @return String  - the Country state name representing this position*/
  public String getCountryStateName() {
    return CSName;
  }
        
  /** @return String  - the Icao code representing this position*/
  public String getIcaoCode() {
    return IcaoCode;
  }
        
  /** @param aGeolocCode - set the geoloc code representing this position */
  public void setGeolocCode(String aGeolocCode) {
    if (aGeolocCode != null) aGeolocCode = aGeolocCode.intern();
    GeolocCode = aGeolocCode;
  }
        
  /** @param aInstCode - set the installation type code representing this position*/
  public void setInstallationTypeCode(String aInstCode) {
    if (aInstCode != null) aInstCode = aInstCode.intern();
    InstallTypeCode = aInstCode;
  }
        
  /** @param aCSCode - set the Country state code representing this position*/
  public void setCountryStateCode(String aCSCode) {
    if (aCSCode != null) aCSCode = aCSCode.intern();
    CSCode = aCSCode;
  }
        
  /** @param aCSName  - set the Country state name representing this position*/
  public void setCountryStateName(String aCSName) {
    if (aCSName != null) aCSName = aCSName.intern();
    CSName = aCSName;
  }
        
  /** @param anIcaoCode  - set the Icao code representing this position*/
  public void setIcaoCode(String anIcaoCode) {
    if (anIcaoCode != null) anIcaoCode = anIcaoCode.intern();
    IcaoCode = anIcaoCode;
  }

  public String toString() {
    String n = getName();
    return
      ((n != null) ?
       (super.toString()+" "+GeolocCode+"("+n+")") :
       (super.toString()+" "+GeolocCode));
  }

  public Object clone() {
    GeolocLocationImpl gli = new GeolocLocationImpl();
    gli.setGeolocCode(GeolocCode);
    gli.setInstallationTypeCode(InstallTypeCode);
    gli.setCountryStateCode(CSCode);
    gli.setCountryStateName(CSName);
    gli.setIcaoCode(IcaoCode);
    gli.setName(getName());
    gli.setLatitude(lat);
    gli.setLongitude(lon);
    return gli;
  }

  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }

    if (object == this) {
      return true;
    }

    if (!(object instanceof GeolocLocation)) {
      return false;
    }

    GeolocLocation other = (GeolocLocation)object;

    return (matches(getGeolocCode(), other.getGeolocCode()) &&
            matches(getInstallationTypeCode(), other.getInstallationTypeCode()) &&
            matches(getCountryStateCode(), other.getCountryStateCode()) &&
            matches(getCountryStateName(), other.getCountryStateName()) &&
            matches(getIcaoCode(), other.getIcaoCode()) &&
            matches(getName(), other.getName()) &&
            matches(getLatitude(), other.getLatitude()) &&
            matches(getLongitude(), other.getLongitude()));
  }

  private transient int _hc = 0;
  public int hashCode()
  {
    if (_hc == 0) {
      String n = getGeolocCode();
      if (n == null) n = getName();
      if (n != null) {
        _hc = n.hashCode();
      } else {
        _hc = 1;
      }
    }
      
    return _hc;
  }

  private boolean matches(Object a, Object b) {
    return (a==null)?(b==null):(a.equals(b));
  }

}
