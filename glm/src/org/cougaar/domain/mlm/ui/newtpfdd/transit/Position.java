package org.cougaar.domain.mlm.ui.tpfdd.transit;
import java.io.Serializable;

/** Position of an object 
 * @author Benjamin Lubin; last modified by $Author: wseitz $
 * @version $Revision: 1.1 $; Last modified on $Date: 2001-02-22 22:42:32 $
 * @since 11/14/00
 */
public interface Position extends Serializable{
  public String getName();
  public float getLat();
  public float getLon();
}
