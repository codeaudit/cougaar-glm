package org.cougaar.domain.mlm.ui.tpfdd.transit;
import java.io.Serializable;

/** Position of an object at a specific geoloc/lat/lon
 * @author Benjamin Lubin; last modified by $Author: mthome $
 * @version $Revision: 1.1 $; Last modified on $Date: 2000-12-15 20:17:47 $
 * @since 11/14/00
 */
public class FixedPosition implements Position{
  protected String name;
  protected float lat;
  protected float lon;
  
  public FixedPosition(String name, double lat, double lon){
    if(name != null)
      this.name=name.intern();
    else
      this.name=null;
    this.lat=(float)lat;
    this.lon=(float)lon;
  }
  public FixedPosition(String name, float lat, float lon){
    if(name != null)
      this.name=name.intern();
    else
      this.name=null;
    this.lat=lat;
    this.lon=lon;
  }
  
  public String getName(){return name;}
  public float getLat(){return lat;}
  public float getLon(){return lon;}
  
  public int hashCode(){
    if(name==null)
      return super.hashCode();
    return name.hashCode();
  }
  
  public boolean equals(Object o){
    return (o instanceof FixedPosition &&
	    name.equals( ((FixedPosition)o).name));
  }
  public String toString(){
    return name + "("+lat+","+lon+")";
  }
}
