/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import com.ibm.xml.parser.TXText;
import com.ibm.xml.parser.util.TreeFactory;

public class XMLTreeFactory extends TreeFactory {

    public TXText createTextNode(String data, boolean ignorable) {
      data = data.trim();
      if (data.length() == 0)
        return null;
      TXText te = new TreeText(data);
      te.setFactory(this);
      te.setIsIgnorableWhitespace(ignorable);
      return te;
    }

    public TXText createTextNode(char[] ac, int offset, int length, boolean ignorable) {
      String tmp = new String(ac, offset, length);
      tmp = tmp.trim();
      if (tmp.length() == 0)
        return null;
      TXText te = new TreeText(tmp);
      te.setFactory(this);
      te.setIsIgnorableWhitespace(ignorable);
      return te;
    }

}
