package org.cougaar.domain.mlm.ui.newtpfdd.transit;
import java.io.Serializable;

/** Position of an object beeing moved between two other Positions
 * @author Benjamin Lubin; last modified by $Author: wseitz $
 * @version $Revision: 1.2 $; Last modified on $Date: 2001-02-23 01:02:20 $
 * @since 11/14/00
 */
public class TransitPosition implements Position {
  protected Position from;
  protected Position to;
  
  public TransitPosition(Position from, Position to){
    this.from=from;
    this.to=to;
    if(from.equals(to))
      System.err.println("TransitPosition: Unexpected condition from == to");
  }
  
  public String getName(){
    return from.getName()+" -> "+to.getName();
  }

  /** 2/3 of the way to 'to' **/
  public float getLat(){
    return ( from.getLat()/3f + to.getLat()*2f/3f );
  }
  
  /** 2/3 of the way to 'to' **/
  public float getLon(){
    //For half way:  A/2 + B/2 + (|A|+|B|>180)?(A+B>0?-180:180):0

    float a1=from.getLon() / 3f;
    float a2=to.getLon() *2f / 3f;

    return a1+ a2 +
      ( ( (Math.abs(a1) + Math.abs(a2)) > 180 )?
	( ((a1+a2)>0)?-180:180):
	0);
  }
  
  public Position getPosition1(){return from;}
  public Position getPosition2(){return to;}
  
  public int hashCode(){
    return (from.hashCode() + to.hashCode())/2;
  }
  
  public boolean equals(Object o){
    return (o instanceof TransitPosition &&
	    from.equals( ((TransitPosition)o).from ) &&
	    to.equals( ((TransitPosition)o).to ));
  }
  public String toString(){
    return from + "->" + to;
  }
}
