package org.cougaar.domain.mlm.ui.psp.transit.data.locations;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;

import org.xml.sax.Attributes;

/**
 * A single location leaving the Locations PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:29 $
 * @since 1/29/01
 **/
public class Location implements XMLable, DeXMLable, Serializable{

  //Constants:
  ////////////

  //Tags:
  public static final String NAME_TAG = "Loc";
  //Attr:

  protected static final String UID_ATTR = "UID";
  protected static final String LAT_ATTR = "Lat";
  protected static final String LON_ATTR = "Lon";
  protected static final String GEOLOC_ATTR = "geoLoc";
  protected static final String ICAO_ATTR = "ICAO";
  protected static final String PRETTY_NAME_ATTR = "PName";

  //Variables:
  ////////////

  public String UID;
  public double lat;
  public double lon;
  public String geoLoc;
  public String icao;
  public String prettyName;

  //Constructors:
  ///////////////

  public Location(){
  }

  //Members:
  //////////

  //XMLable members:
  //----------------

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.sitagln(NAME_TAG,
	      UID_ATTR,UID,
	      LAT_ATTR,Double.toString(lat),
	      LON_ATTR,Double.toString(lon),
	      GEOLOC_ATTR,geoLoc,
	      ICAO_ATTR,icao,
	      PRETTY_NAME_ATTR,prettyName);

  }

  //DeXMLable members:
  //------------------

  /**
   * Report a startElement that pertains to THIS object, not any
   * sub objects.  Call also provides the elements Attributes and data.  
   * Note, that  unlike in a SAX parser, data is guaranteed to contain 
   * ALL of this tag's data, not just a 'chunk' of it.
   * @param name startElement tag
   * @param attr attributes for this tag
   * @param data data for this tag
   **/
  public void openTag(String name, Attributes attr, String data)
    throws UnexpectedXMLException{

    try{
      if(name.equals(NAME_TAG)){
	UID=attr.getValue(UID_ATTR);
	lat=Double.parseDouble(attr.getValue(LAT_ATTR));
	lon=Double.parseDouble(attr.getValue(LON_ATTR));
	geoLoc=attr.getValue(GEOLOC_ATTR);
	icao=attr.getValue(ICAO_ATTR);
	prettyName=attr.getValue(PRETTY_NAME_ATTR);
      }else{
	throw new UnexpectedXMLException("Unexpected tag: "+name);    
      }
    }catch(NumberFormatException e){
      throw new UnexpectedXMLException("Could not parse number: "+e);
    }
  }

  /**
   * Report an endElement.
   * @param name endElement tag
   * @return true iff the object is DONE being DeXMLized
   **/
  public boolean closeTag(String name)
    throws UnexpectedXMLException{
    return name.equals(NAME_TAG);
  }

  /**
   * This function will be called whenever a subobject has
   * completed de-XMLizing and needs to be encorporated into
   * this object.
   * @param name the startElement tag that caused this subobject
   * to be created
   * @param obj the object itself
   **/
  public void completeSubObject(String name, DeXMLable obj)
    throws UnexpectedXMLException{
    throw new UnexpectedXMLException("Unknown object:" + name + ":"+obj);
  }
  //Inner Classes:
}
