package org.cougaar.domain.mlm.ui.psp.transit.data.xml;

import java.io.Writer;
import java.io.IOException;

/**
 * Classes conforming to this interface can serialize themselves
 * out in XML format.
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:32 $
 * @since 1/24/01
 **/
public interface XMLable{

  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException;
}
