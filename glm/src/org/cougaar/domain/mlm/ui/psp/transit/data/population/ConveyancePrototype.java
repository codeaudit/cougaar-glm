package org.cougaar.domain.mlm.ui.psp.transit.data.population;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * Represents the data leaving the PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:30 $
 * @since 1/24/01
 **/
public class ConveyancePrototype implements XMLable, DeXMLable, Serializable{

  //Variables:
  ////////////

  //TAGS:
  public static final String NAME_TAG = "ConveyancePrototype";
  protected static final String UID_TAG = "UID";
  protected static final String TYPE_TAG = "Type";
  protected static final String VOL_TAG = "Vol";
  protected static final String WEIGHT_TAG = "Weight";
  protected static final String AVE_SPEED_TAG = "Speed";
  protected static final String ALP_TYPEID_TAG = "ALPTypeID";
  protected static final String NOMENCLATURE_TAG = "Nomen";

  //Constants:
  //types:
  public static final int SELF_PROP=0;
  public static final int TRUCK=1;
  public static final int TRAIN=2;
  public static final int PLANE=3;
  public static final int SHIP=4;
  public static final int DECK=5;

  //Variables:
  public String UID;
  /**One of SELF_PROP, TRUCK, TRAIN, PLANE, SHIP, DECK**/
  public int conveyanceType;
  public float volCap;
  public float weightCap;
  public float aveSpeed;
  public String alpTypeID;
  public String nomenclature;

  //Constructors:
  ///////////////

  public ConveyancePrototype(){
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
    w.optagln(NAME_TAG);

    w.tagln(UID_TAG, UID);
    w.tagln(TYPE_TAG, conveyanceType);
    w.tagln(VOL_TAG, volCap);
    w.tagln(WEIGHT_TAG, weightCap);
    w.tagln(AVE_SPEED_TAG, aveSpeed);
    w.tagln(ALP_TYPEID_TAG, alpTypeID);
    w.tagln(NOMENCLATURE_TAG, nomenclature);

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
      }else if(name.equals(UID_TAG)){
	UID=data;
      }else if(name.equals(TYPE_TAG)){
	conveyanceType=Integer.parseInt(data);
      }else if(name.equals(VOL_TAG)){
	volCap=Float.parseFloat(data);
      }else if(name.equals(WEIGHT_TAG)){
	weightCap=Float.parseFloat(data);
      }else if(name.equals(AVE_SPEED_TAG)){
	aveSpeed=Float.parseFloat(data);
      }else if(name.equals(ALP_TYPEID_TAG)){
	alpTypeID=data;
      }else if(name.equals(NOMENCLATURE_TAG)){
	nomenclature=data;
      }else{
	throw new UnexpectedXMLException("Unexpected tag: "+name);    
      }
    }catch(NumberFormatException e){
      throw new UnexpectedXMLException("Malformed Number: " + 
				       name + " : " + data);
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
  }

  //Inner Classes:
}
