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
public class PopulationData implements XMLable, DeXMLable, Serializable{

  //Variables:
  ////////////

  public static final String NAME_TAG = "Population";

  protected List conveyancePrototypes;

  protected List conveyanceInstances;

  //Constructors:
  ///////////////

  public PopulationData(){
    conveyancePrototypes = new ArrayList();
    conveyanceInstances = new ArrayList();
  }

  //Members:
  //////////

  public int getNumPrototypes(){
    return conveyancePrototypes.size();
  }

  public int getNumInstances(){
    return conveyanceInstances.size();
  }

  public ConveyancePrototype getPrototypeAt(int i){
    return (ConveyancePrototype)conveyancePrototypes.get(i);
  }

  public ConveyanceInstance getInstanceAt(int i){
    return (ConveyanceInstance)conveyanceInstances.get(i);
  }

  public void  addPrototype(ConveyancePrototype cp){
    conveyancePrototypes.add(cp);
  }

  public void addInstance(ConveyanceInstance ci){
    conveyanceInstances.add(ci);
  }

  //XMLable members:
  //----------------

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG);
    for(int i=0;i<getNumPrototypes();i++){
      getPrototypeAt(i).toXML(w);
    }
    for(int i=0;i<getNumInstances();i++){
      getInstanceAt(i).toXML(w);
    }
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
    if(!name.equals(NAME_TAG))
      throw new UnexpectedXMLException("Unexpected tag: "+name);    
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
    if(obj instanceof ConveyancePrototype){
      addPrototype((ConveyancePrototype)obj);
    }else if(obj instanceof ConveyanceInstance){
      addInstance((ConveyanceInstance)obj);
    }else{
      throw new UnexpectedXMLException("Unknown object:" + name + ":"+obj);
    }
  }
  //Inner Classes:
}
