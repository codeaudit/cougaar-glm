package org.cougaar.domain.mlm.ui.newtpfdd.transit;
import java.io.Serializable;

/**
 * Instances of this class contain the actual bins of data
 * @author Benjamin Lubin; last modified by $Author: wseitz $
 * @version $Revision: 1.2 $; Last modified on $Date: 2001-02-23 01:02:20 $
 * @since 11/14/00
 */
public interface Tallier extends Serializable{
  
  /** tally in the TransitData **/
  public boolean increment(TransitData td);
  
  /** remove data from tally **/
  public boolean decrement(TransitData td);
  
  /** This should return a deep copy of this instance **/
  public Tallier deepClone();
}
