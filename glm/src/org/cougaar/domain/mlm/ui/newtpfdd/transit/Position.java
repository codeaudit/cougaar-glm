package org.cougaar.domain.mlm.ui.newtpfdd.transit;
import java.io.Serializable;

/** Position of an object 
 * @author Benjamin Lubin; last modified by $Author: wseitz $
 * @version $Revision: 1.2 $; Last modified on $Date: 2001-02-23 01:02:20 $
 * @since 11/14/00
 */
public interface Position extends Serializable{
  public String getName();
  public float getLat();
  public float getLon();
}
