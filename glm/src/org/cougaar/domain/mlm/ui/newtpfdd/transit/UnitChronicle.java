package org.cougaar.domain.mlm.ui.tpfdd.transit;
import java.util.*;
import java.io.Serializable;

import org.cougaar.domain.mlm.ui.tpfdd.transit.TagChronicle.TagTransitData;
import org.cougaar.domain.mlm.ui.tpfdd.transit.TagChronicle.TagTally;

/**
 * Holds a Chronicle of data about the location of a UNIT's assets
 *
 * @author Benjamin Lubin; last modified by $Author: wseitz $
 * @version $Revision: 1.1 $; Last modified on $Date: 2001-02-22 22:42:33 $
 * @since 11/15/00
 */
public class UnitChronicle extends TagChronicle{

  //Variables:
  ////////////

  //Constructors:
  ///////////////

  public UnitChronicle(){
    super();
  }

  public UnitChronicle(int binSize){
    super(binSize);
  }
  
  //Functions:
  ////////////

  /**
   * This function returns a new Tallier object that records the low-level
   * data.
   **/
  protected Tallier getNewTallier(){
    return new UnitTally();
  }

  //Inner Classes:
  ////////////////

  /** Incoming data for each unit transport task.**/
  public static class UnitTransitData extends TagTransitData{
    public UnitTransitData(Position start,
			   Position end,
			   long startDate,
			   long endDate,
			   String unit,
			   int count){
      super(start,end,startDate,endDate,
	    unit.startsWith("UIC/")?unit.substring(4).intern():unit.intern(),
	    count);
    }

    /**
     * Used to clone a copy of a TransitData, but set new values for its
     * positions and dates
     **/
    public TransitData cloneNewDatePos(Position startP,
				       Position endP,
				       long startD,
				       long endD){
      return new UnitTransitData(startP,endP,startD,endD,tag,count);
    }
  }

  /**
   * Actual data for a time-loc bin.
   **/
  public class UnitTally extends TagTally{

    public UnitTally(){
    }
  }
}
