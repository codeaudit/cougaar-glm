package org.cougaar.domain.mlm.ui.psp.transit.data.legs;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;

/**
 * A single instance leaving the Legs PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:29 $
 * @since 1/28/01
 **/
public class Leg implements XMLable, DeXMLable, Serializable{

  //Constants:
  ////////////

  /**Guaranteed should not overlap for a given asset or conveyance**/
  public static final int LEG_TYPE_TRANSPORTING=0;
  /**Loading cargo (cargo does not show up on this leg)**/
  public static final int LEG_TYPE_LOADING=1;
  /**Unloading cargo (cargo does not show up on this leg)**/
  public static final int LEG_TYPE_UNLOADING=2;
  /**Positioning to pick up cargeo (no cargo on this leg)**/
  public static final int LEG_TYPE_POSITIONING=3;
  /**Returning from dropping off cargeo (no cargo on this leg)**/
  public static final int LEG_TYPE_RETURNING=4;
  /**Refueling**/
  public static final int LEG_TYPE_REFEULING=5;

  //Tags:
  public static final String NAME_TAG = "Leg";
  protected static final String ASSET_TAG = "CarriedAsset";
  //Attr:

  protected static final String UID_ATTR = "UID";
  protected static final String START_DATE_ATTR = "SDate";
  protected static final String END_DATE_ATTR = "EDate";
  protected static final String START_LOC_ATTR = "SLoc";
  protected static final String END_LOC_ATTR = "ELoc";
  protected static final String LEG_TYPE_ATTR = "Type";
  protected static final String CONV_ID_ATTR = "ConvID";

  //Variables:
  ////////////

  public String UID;
  public long startDate;
  public long endDate;
  public String startLoc;
  public String endLoc;
  /**Use LEG_TYPE_* constants**/
  public int legType;
  public String conveyanceUID;

  protected List assetsOnLeg;

  //Constructors:
  ///////////////

  public Leg(){
    assetsOnLeg=new ArrayList();
  }

  //Members:
  //////////

  public boolean assetsCarried(){
    return assetsOnLeg==null;
  }

  public int numCarriedAssets(){
    if(assetsOnLeg==null)
      return 0;
    return assetsOnLeg.size();
  }

  /** Get the UID of a carried asset**/
  public String getCarriedAssetAt(int i){
    return (String)assetsOnLeg.get(i);
  }

  public void addCarriedAsset(String uid){
    if(assetsOnLeg==null)
      assetsOnLeg = new ArrayList();
    assetsOnLeg.add(uid);
  }

  //XMLable members:
  //----------------

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG,
	      UID_ATTR, UID,
	      START_DATE_ATTR, Long.toString(startDate),
	      END_DATE_ATTR, Long.toString(endDate),
	      START_LOC_ATTR, startLoc,
	      END_LOC_ATTR, endLoc,
	      LEG_TYPE_ATTR, Integer.toString(legType),
	      CONV_ID_ATTR, conveyanceUID);

    for(int i=0;i<numCarriedAssets();i++)
      w.tagln(ASSET_TAG,getCarriedAssetAt(i));

    w.cltagln(NAME_TAG);
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
	startDate=Long.parseLong(attr.getValue(START_DATE_ATTR));
	endDate=Long.parseLong(attr.getValue(END_DATE_ATTR));
	startLoc=attr.getValue(START_LOC_ATTR);
	endLoc=attr.getValue(END_LOC_ATTR);
	legType=Integer.parseInt(attr.getValue(LEG_TYPE_ATTR));
	conveyanceUID=attr.getValue(CONV_ID_ATTR);
      }else if(name.equals(ASSET_TAG)){
	addCarriedAsset(data);
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
