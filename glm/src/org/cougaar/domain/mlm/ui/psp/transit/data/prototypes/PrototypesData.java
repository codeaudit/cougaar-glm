package org.cougaar.domain.mlm.ui.psp.transit.data.prototypes;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * Represents the data leaving the Prototypes PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:31 $
 * @since 1/28/01
 **/
public class PrototypesData implements XMLable, DeXMLable, Serializable{

  //Constants:

  //Tags:
  public static final String NAME_TAG = "Prototypes";
  //Attr:

  //Variables:
  ////////////

  protected List prototypes;

  //Constructors:
  ///////////////

  public PrototypesData(){
    prototypes = new ArrayList();
  }

  //Members:
  //////////

  public int numPrototypes(){
    return prototypes.size();
  }

  public Prototype getPrototypeAt(int i){
    return (Prototype)prototypes.get(i);
  }

  public void addPrototype(Prototype p){
    prototypes.add(p);
  }

  //XMLable members:
  //----------------

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG);

    for(int i=0;i<numPrototypes();i++)
      getPrototypeAt(i).toXML(w);

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

    if(name.equals(NAME_TAG)){
    }else{
      throw new UnexpectedXMLException("Unexpected tag: "+name);    
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
    if(obj instanceof Prototype){
      addPrototype((Prototype)obj);
    }else{
      throw new UnexpectedXMLException("Unknown object:" + name + ":"+obj);
    }
  }
  //Inner Classes:
}
