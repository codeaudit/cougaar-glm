package org.cougaar.domain.mlm.ui.psp.transit.data.owner;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import org.xml.sax.Attributes;

/**
 * Represents the data leaving the Owner PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:30 $
 * @since 1/24/01
 **/
public class OwnerData implements XMLable, DeXMLable, Serializable{

  //Variables:
  ////////////

  //Tags:
  public static final String NAME_TAG = "Owner";
  protected static final String ASSET_TAG = "Map";
  //Attr:
  protected static final String OWNER_ATTR = "OID";
  protected static final String ASSET_ATTR = "AID";

  /** map units to lists of AssetIDs**/
  protected Map owners;

  //Constructors:
  ///////////////

  public OwnerData(){
    owners = new HashMap(31);
  }

  //Members:
  //////////

  public Set getOwnerSet(){
    return owners.keySet();
  }

  public int numOwners(){
    return owners.size();
  }

  public List assetsForOwner(String owner){
    return (List)owners.get(owner);
  }

  public void addAsset(String owner, String assetID){
    List l = (List)owners.get(owner);
    if(l==null){
      l=new ArrayList();
      owners.put(owner,l);
    }
    l.add(assetID);
  }

  //XMLable members:
  //----------------

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG);

    Set s=getOwnerSet();
    Iterator iter = s.iterator();
    while(iter.hasNext()){
      String owner=(String)iter.next();
      List assetIDs=assetsForOwner(owner);
      for(int i=0;i<assetIDs.size();i++)
	w.sitagln(ASSET_TAG, OWNER_ATTR, owner,
		  ASSET_ATTR, (String) assetIDs.get(i));
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

    if(name.equals(NAME_TAG)){
    }else if(name.equals(ASSET_TAG)){
      addAsset(attr.getValue(OWNER_ATTR),
	       attr.getValue(ASSET_ATTR));
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
    throw new UnexpectedXMLException("Unknown object:" + name + ":"+obj);
  }
  //Inner Classes:
}
