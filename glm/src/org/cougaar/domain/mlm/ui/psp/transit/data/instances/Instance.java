package org.cougaar.domain.mlm.ui.psp.transit.data.instances;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * A single instance leaving the Instance PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:29 $
 * @since 1/28/01
 **/
public class Instance implements XMLable, DeXMLable, Serializable{

  //Constants:
  ////////////

  //Tags:
  public static final String NAME_TAG = "Instance";
  protected static final String CONTENT_TAG = "Contains";
  //Attr:

  protected static final String UID_ATTR = "UID";
  protected static final String AGGREGATE_ATTR = "Agg";
  protected static final String PROTOTYPE_UID_ATTR = "PUID";

  //Variables:
  ////////////

  public String UID;
  public int aggregateNumber;
  public String prototypeUID;

  public List contents;

  //Constructors:
  ///////////////

  public Instance(){
    contents = null;
  }

  //Members:
  //////////

  public boolean hasContents(){
    return contents!=null;
  }

  public int numContents(){
    if(contents==null)
      return 0;
    return contents.size();
  }

  /**Returns the instanceUID of the given content**/
  public String getContentAt(int i){
    return (String)contents.get(i);
  }

  /**Add the given uid as contents of this container**/
  public void addContent(String uid){
    if(contents == null)
      contents=new ArrayList();
    contents.add(uid);
  }

  //XMLable members:
  //----------------

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG, 
	      UID_ATTR,UID,
	      AGGREGATE_ATTR,Integer.toString(aggregateNumber),
	      PROTOTYPE_UID_ATTR,prototypeUID);

    for(int i=0;i<numContents();i++)
      w.sitagln(CONTENT_TAG,UID_ATTR, getContentAt(i));

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
	aggregateNumber=Integer.parseInt(attr.getValue(AGGREGATE_ATTR));
	prototypeUID=attr.getValue(PROTOTYPE_UID_ATTR);
      }else if(name.equals(CONTENT_TAG)){
	addContent(attr.getValue(UID_ATTR));
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
