package org.cougaar.domain.mlm.ui.tpfdd.transit;
import java.io.Serializable;


/** Incoming data for each transport task.
 * @author Benjamin Lubin; last modified by $Author: mthome $
 * @version $Revision: 1.1 $; Last modified on $Date: 2000-12-15 20:17:47 $
 * @since 11/14/00
 */
public abstract class TransitData implements Serializable, Comparable{
  public abstract Position getStartPosition();
  public abstract Position getEndPosition();
  public abstract long getStartDate();
  public abstract long getEndDate();
  
  public int compareTo(Object o){
    return (int)(getStartDate() - ((TransitData)o).getStartDate());
  }

  /**
   * Used to clone a copy of a TransitData, but set new values for its
   * positions and dates
   **/
  public abstract TransitData cloneNewDatePos(Position startP,
					      Position endP,
					      long startD,
					      long endD);
}
