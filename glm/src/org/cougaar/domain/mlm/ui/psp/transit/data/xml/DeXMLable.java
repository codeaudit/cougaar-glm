package org.cougaar.domain.mlm.ui.psp.transit.data.xml;

import org.xml.sax.Attributes;

/**
 * Classes conforming to this interface can DEserialize themselves
 * from a slightly restricted XML format.<P>
 * The XML format used, does not allow character data between or
 * following nested tags. For example: <P>
 * <PRE>
 * <A>OK
 *   <B>OK</B>
 *   NOT OK
 *   <C>OK</C>
 *   NOT OK
 * </A>
 * </PRE>
 * <BR><P>
 * 
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:31 $
 * @since 1/24/01
 **/
public interface DeXMLable{

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
    throws UnexpectedXMLException;

  /**
   * Report an endElement.
   * @param name endElement tag
   * @return true iff the object is DONE being deXMLized
   **/
  public boolean closeTag(String name)
    throws UnexpectedXMLException;

  /**
   * This function will be called whenever a subobject has
   * completed de-XMLizing and needs to be encorporated into
   * this object.
   * @param name the startElement tag that caused this subobject
   * to be created
   * @param obj the object itself
   **/
  public void completeSubObject(String name, DeXMLable obj)
    throws UnexpectedXMLException;
}

