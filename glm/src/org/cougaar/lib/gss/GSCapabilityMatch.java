/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.asset.Asset;
import java.util.List;

/**
 * Determines whether a resource can perform a task by comparing the
 * values of specified property fields (or constants)
 *
 */

public class GSCapabilityMatch extends GSDoubleAccessor implements GSBoolean, GSDebugable {

  /* data types */
  private static final int INT = 0;
  private static final int STRING = 1;
  private static final int DOUBLE = 2;
  private static final int BOOLEAN = 3;

  /* types of comparisons */
  private static final int EQ = 0;
  private static final int NE = 1;
  private static final int LT = 2;
  private static final int LE = 3;
  private static final int GT = 4;
  private static final int GE = 5;

  /** Constructor */
  public GSCapabilityMatch (String type, String relationship) {

    if (type.equals ("int"))
      this.type = INT;
    else if (type.equals ("string"))
      this.type = STRING;
    else if (type.equals ("double"))
      this.type = DOUBLE;
    else if (type.equals ("boolean"))
      this.type = BOOLEAN;
    else
      System.out.println ("Warning: unknown type " + type +
                          " for capability match");

    if (relationship.equals ("eq"))
      this.relationship = EQ;
    else if (relationship.equals ("ne"))
      this.relationship = NE;
    else if (relationship.equals ("lt"))
      this.relationship = LT;
    else if (relationship.equals ("le"))
      this.relationship = LE;
    else if (relationship.equals ("gt"))
      this.relationship = GT;
    else if (relationship.equals ("ge"))
      this.relationship = GE;
    else
      System.out.println ("Warning: unknown relationship " +
                          relationship + " for capability match");
  }


  public boolean eval (List args) {
    if (args.size() != 2) {
      System.err.println("GSS Error: Wrong number of args passed to GSCapabilityMatch\nExpected 2 and got " + args.size() + " so will ALWAYS return false");
      return false;
    }
    
    Object obj1 = args.get(0);
    Object obj2 = args.get(1);
    if (!(obj1 instanceof Asset) || !(obj2 instanceof Task)) {
      System.err.println("GSS Error: Wrong type of args passed to GSCapabilityMatch"
			 + "\nExpected Asset, Task and got " + obj1.getClass()
			  + ", " + obj2.getClass() + 
			 " so will ALWAYS return false");
      return false;
    } 

    Asset resource = (Asset)obj1;
    Task task = (Task)obj2;

    if (debug)
      System.out.println("Getting value of resource");

    Object resValue = resourceAccessor.value (resource);
    if (resValue == null)
      return false;

    if (debug)
      System.out.println("Getting value of task.");

    Object taskValue = taskAccessor.value (task);
    if (taskValue == null)
      return false;

    boolean retval = false;

    switch (type) {

    case INT:
    case DOUBLE:
      double resDouble = (type == INT) ? ((Integer) resValue).doubleValue() :
                                       ((Double) resValue).doubleValue();
      double taskDouble = (type == INT) ? ((Integer) taskValue).doubleValue() :
                                        ((Double) taskValue).doubleValue();
      switch (relationship) {
      case EQ:
        retval = (resDouble == taskDouble);
	break;
      case NE:
        retval = (resDouble != taskDouble);
	break;
      case LT:
        retval = (resDouble < taskDouble);
	break;
      case LE:
        retval = (resDouble <= taskDouble);
	break;
      case GT:
        retval = (resDouble > taskDouble);
	break;
      case GE:
        retval = (resDouble >= taskDouble);
	break;
      }
      break;
    case STRING:
	String resString  = resValue.toString ();
	String taskString = taskValue.toString ();
      switch (relationship) {
      case EQ:
	  retval = resString.equals (taskString);
	  //	   retval = resValue.equals (taskValue);
	  break;
      case NE:
	  retval = ! resString.equals (taskString);
	  //	  retval = ! resValue.equals (taskValue);
	  break;
      default:
	  System.out.println ("Only = or != relationships allowed for type string");
	  break;
      }
      break;
    case BOOLEAN:
      switch (relationship) {
      case EQ:
	  retval = resValue.equals (taskValue);
	  break;
      case NE:
	  retval = ! resValue.equals (taskValue);
	  break;
      default:
	  System.out.println ("Only = or != relationships allowed for type boolean");
	  break;
      }
      break;
    default:
	System.err.println ("GSCapabilityMatch.eval () - unknown comparison type : " + type);
    }

    if (debug) {
	if (!retval)
	  System.out.println ("CapabilityMatch.matches - NO match of task " + task.getUID () + 
			      "'s value " + taskValue + 
			      " and resource " + resource.getUID() + 
			      "'s value " + resValue);
	else
	  System.out.println ("CapabilityMatch.matches - task value " + 
			      taskValue + " matches " + resValue);
    }

    return retval;
  }

  protected boolean debug = false;
  
  public void setDebug (boolean debug) { this.debug = debug; }

  private int type;
  private int relationship;

}
