package org.cougaar.domain.mlm.ui.planviewer.inventory;

import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Vector;


import org.cougaar.domain.glm.execution.common.*;

/**
 * Handler for ExecutionTimeStatus messages. The execution time
 * in the message is used to set the event generator execution time.
 **/
public class InventoryExecutionTimeStatusHandler  {

  final private static Calendar gmtCalendar = 
      Calendar.getInstance(TimeZone.getTimeZone("GMT"));

  final private boolean debug=false;
								    
  private Vector dayRolloverListeners;
  
  public long theExecutionTime=-1L;;
  public long lastRolloverTime=-1L;;
  public double theExecutionRate;

  public InventoryExecutionTimeStatusHandler() {
      dayRolloverListeners = new Vector();
  }

  public void execute(String source, ExecutionTimeStatus ets) {
      int origDayOfYear;
      int origYear;
      int newDayOfYear;
      int newYear;
      int newHour;

      //if(debug) System.out.println("InventoryExecTimeStatusHandler: execute begin");

      if(ets.theExecutionRate != theExecutionRate) {
	  if(debug) System.out.println("InventoryExecutionTimeStatusHandler: execution rate change");
	  theExecutionRate = ets.theExecutionRate;
      }


      if(theExecutionTime == -1L) {
	  theExecutionTime = ets.theExecutionTime;
	  lastRolloverTime=adjustToNoon(theExecutionTime);
	  return;
      }

      theExecutionTime = ets.theExecutionTime;
      
      Date origExecutionTime = new Date(lastRolloverTime);
      Date newExecutionTime = new Date(theExecutionTime);

      gmtCalendar.setTime(origExecutionTime);
      origDayOfYear = gmtCalendar.get(Calendar.DAY_OF_YEAR);
      origYear = gmtCalendar.get(Calendar.YEAR);

      gmtCalendar.setTime(newExecutionTime);
      newDayOfYear = gmtCalendar.get(Calendar.DAY_OF_YEAR);
      newYear = gmtCalendar.get(Calendar.YEAR);
      newHour = gmtCalendar.get(Calendar.HOUR_OF_DAY);

      if(((newYear > origYear) ||
	  ((newYear == origYear) && 
	   (origDayOfYear < newDayOfYear))) &&
	 (lastRolloverTime < theExecutionTime)) {
	  if(debug)System.out.println("InventoryExecutionTimeStatusHandler::Day Rollover - Time is:" + newExecutionTime);
	  lastRolloverTime=adjustToNoon(theExecutionTime);
	  for(int i=0; i < dayRolloverListeners.size();i++) {
	      InventoryDayRolloverListener l= 
		  (InventoryDayRolloverListener)dayRolloverListeners.elementAt(i);
	          l.dayRolloverUpdate(theExecutionTime,theExecutionRate);
	  }
      }

  }

    public void addDayRolloverListener(InventoryDayRolloverListener l) {
	dayRolloverListeners.add(l);
    }

    public void removeDayRolloverListener(InventoryDayRolloverListener l) {
	dayRolloverListeners.remove(l);
    }

    public Date getCurrentExecutionTime(){return new Date(theExecutionTime);}
    public Date getLastDayRollover(){return new Date(lastRolloverTime);}

    public static long adjustToNoon(long time) {
	  gmtCalendar.setTime(new Date(time));
	  gmtCalendar.set(Calendar.HOUR_OF_DAY,12);
	  gmtCalendar.set(Calendar.MINUTE,0);
	  gmtCalendar.set(Calendar.SECOND,0);
	  return gmtCalendar.getTime().getTime();
    }
	
}

  
