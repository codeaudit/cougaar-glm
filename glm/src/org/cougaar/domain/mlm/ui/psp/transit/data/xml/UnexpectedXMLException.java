package org.cougaar.domain.mlm.ui.psp.transit.data.xml;

/**
 * An error indicating unexpected XML input
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:31 $
 * @since 1/24/01
 **/
public class UnexpectedXMLException extends Exception{
  public UnexpectedXMLException(){}
  public UnexpectedXMLException(String s){
    super(s);
  }
}
