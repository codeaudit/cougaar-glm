package org.cougaar.domain.mlm.ui.psp.transit.data.hierarchy;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * Represents the organization data within the hierarchy PSP
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:28 $
 * @since 1/24/01
 **/
public class Organization
  implements XMLable, DeXMLable, Serializable{
  
  //Variables:
  ////////////
  
  public final static String NAME_TAG = "Org";
  protected final static String UID_TAG = "OrgID";
  protected final static String PRETTY_NAME_TAG = "Name";
  protected final static String RELATION_TAG = "Rel";
  
  public static int ADMIN_SUBORDINATE = 0;
  public static int SUBORDINATE = 1;
  
  protected String UID;
  protected String prettyName;
  protected List relations;
  
  //Constructors:
  ///////////////
  
  public Organization(){
    relations = new ArrayList();
  }
  
  //Members:
  //////////
  
  public void setUID(String UID){this.UID=UID;}
  public void setPrettyName(String name){prettyName=name;}
  public void addRelation(String UID, int relation){
    relations.add(new OrgRelation(UID, relation));
  }
  
  public String getUID(){return UID;}
  public String getPrettyName(){return prettyName;}
  public int getNumRelations(){return relations.size();}
  public String getRelationUIDAt(int i){
    OrgRelation or = (OrgRelation)relations.get(i);
    return or.org;
  }
  public int getRelationAt(int i){
    OrgRelation or = (OrgRelation)relations.get(i);
    return or.relation;
  }
  
  //XMLable members:
  //----------------
  
  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG);
    
    w.tagln(UID_TAG, getUID());
    w.tagln(PRETTY_NAME_TAG, getPrettyName());
    
    for(int i=0;i<getNumRelations();i++){
      w.sitagln(RELATION_TAG,
		UID_TAG, getRelationUIDAt(i),
		RELATION_TAG, Integer.toString(getRelationAt(i)));
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
    }else if(name.equals(PRETTY_NAME_TAG)){
      setPrettyName(data);
    }else if(name.equals(UID_TAG)){
      setUID(data);
    }else if (name.equals(RELATION_TAG)){
      try{
	addRelation(attr.getValue(UID_TAG),
		    Integer.parseInt(attr.getValue(RELATION_TAG)));
      }catch(NumberFormatException e){
	throw new UnexpectedXMLException("Could not parse number: "+e);
      }
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
  }

  private static class OrgRelation implements Serializable{
    public String org;
    public int relation;
    public OrgRelation(String o, int r){
      org=o;
      relation=r;
    }
  }
}
