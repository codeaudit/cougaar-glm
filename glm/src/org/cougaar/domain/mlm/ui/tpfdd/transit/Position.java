package org.cougaar.domain.mlm.ui.tpfdd.transit;
import java.io.Serializable;

/** Position of an object 
 * @author Benjamin Lubin; last modified by $Author: mthome $
 * @version $Revision: 1.1 $; Last modified on $Date: 2000-12-15 20:17:47 $
 * @since 11/14/00
 */
public interface Position extends Serializable{
  public String getName();
  public float getLat();
  public float getLon();
}
