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
public class ConveyanceInstance implements XMLable, DeXMLable, Serializable{

  //Variables:
  ////////////

  //Tags:
  public static final String NAME_TAG = "ConveyanceInstance";
  protected static final String UID_TAG = "UID";
  protected static final String PROTOTYPE_TAG = "Prototype";
  protected static final String BUMPER_TAG = "BumperNo";
  protected static final String HOME_TAG = "HomeBase";
  protected static final String OWNER_TAG = "Owner";

  //Variables:
  public String UID;
  public String prototypeUID;
  public String bumperNo;
  public String homeLocID;
  public String ownerID;

  //Constructors:
  ///////////////

  public ConveyanceInstance(){
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

    w.tagln(UID_TAG,UID);
    w.tagln(PROTOTYPE_TAG,prototypeUID);
    w.tagln(BUMPER_TAG,bumperNo);
    w.tagln(HOME_TAG,homeLocID);
    w.tagln(OWNER_TAG,ownerID);

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
      }else if(name.equals(PROTOTYPE_TAG)){
	prototypeUID=data;
      }else if(name.equals(BUMPER_TAG)){
	bumperNo=data;
      }else if(name.equals(HOME_TAG)){
	homeLocID=data;
      }else if(name.equals(OWNER_TAG)){
	ownerID=data;
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
