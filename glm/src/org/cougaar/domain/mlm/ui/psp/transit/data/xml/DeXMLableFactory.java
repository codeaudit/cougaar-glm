package org.cougaar.domain.mlm.ui.psp.transit.data.xml;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;

import org.xml.sax.Attributes;

/**
 * Factory that produces sub-objects based on tags and attributes
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:31 $
 * @since 1/24/01
 **/
public interface DeXMLableFactory{

  //Variables:
  ////////////

  //Members:
  //////////
  
  /**
   * This is a look ahead to see if we should start a sub object.
   * The caller will first call this function on startElement.  If
   * this function returns null, the startElement will be reported
   * to the current object with a call to openTag(...).  Otherwise
   * this function should return a new DeXMLable subobject that
   * further output will be deligated to, until the subobject returns
   * true from a call to endElement().
   *
   * @param name startElement tag
   * @param attr startElement attributes
   * @return a new DeXMLable subobject if a subobject should be created,
   * otherwise null.
   **/
  public DeXMLable beginSubObject(DeXMLable curObj, String name, 
				  Attributes attr)
    throws UnexpectedXMLException;
}


