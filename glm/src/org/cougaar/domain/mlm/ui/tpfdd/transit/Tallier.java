/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.ui.tpfdd.transit;
import java.io.Serializable;

/**
 * Instances of this class contain the actual bins of data
 * @author Benjamin Lubin; last modified by $Author: mthome $
 * @version $Revision: 1.2 $; Last modified on $Date: 2001-04-05 19:28:17 $
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
