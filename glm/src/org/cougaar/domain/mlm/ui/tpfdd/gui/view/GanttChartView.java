/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/view/Attic/GanttChartView.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Jason Leatherman, Daniel Bromberg
*/

/**
   Simple wrapper to set up a Task-based GanttChart view.
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.view;


import java.util.Date;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Color;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.swing.JPanel;

import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.SwingQueue;

import org.cougaar.domain.mlm.ui.tpfdd.producer.PlanElementProvider;

import org.cougaar.domain.mlm.ui.tpfdd.gui.component.GanttChart;
import org.cougaar.domain.mlm.ui.tpfdd.gui.component.LongXRuler;

import org.cougaar.domain.mlm.ui.tpfdd.gui.component.TPFDDColor;


public class GanttChartView extends JPanel
{
    private static final int DAYLEN = 1000*3600*24;
    private TaskGanttChart gc;
    private ControlBar cb;
    private long last;

    public GanttChartView(PlanElementProvider provider, String startDate)
    {
	gc = new TaskGanttChart(provider);
      
	gc.setVirtualXLocation(0L);
	gc.setVirtualXSize(20 * DAYLEN);
	gc.setTicInterval(DAYLEN);
	gc.setTicLabelInterval(DAYLEN);
	SimpleDateFormat dFormat = new SimpleDateFormat("MM/dd/yy");
	Date CDayZeroDate = new Date();
	try {
	    CDayZeroDate = dFormat.parse(startDate);
	}
	catch ( ParseException e ) {
	    OutputHandler.out("GCV:GCV Bad date string: " + startDate + ": " + e);
	}
	Date now = new Date();
	Debug.out("GCV:GCV startDate: " + startDate + " time: " + CDayZeroDate.getTime() + " now: " + now.getTime());
	gc.setCDayZeroTime(CDayZeroDate.getTime());
	gc.setVisibleAmount(15);
	gc.setLabelUnitsMode(LongXRuler.PLAINDAYS_UNITS);
	
	cb = new ControlBar(gc, provider);
	
	setBackground(Color.black);
	setLayout(new BorderLayout());
	
	add(gc, BorderLayout.CENTER);
	add(cb, BorderLayout.NORTH);
	
	setVisible(true);
	UpdateThread updateThread = new UpdateThread();
	updateThread.start();
    }

    
    public GanttChart getWidget()
    {
	return gc;
    }

  private class UpdateThread extends Thread
  {
    private Runnable updateCountRunnable = new Runnable() {
	public void run() {
	  cb.getcountLabel().setText(String.valueOf(gc.getNumRows()));
	}
      };
    
    public void run()
    {
      try {
	runloop();
      }
      catch ( Exception e ) {
	OutputHandler.out(ExceptionTools.toString("GCV:uT:run", e));
      }
      catch ( Error e ) {
	OutputHandler.out(ExceptionTools.toString("GCV:uT:run", e));
      }	    
    }
    
    private void runloop()
    {
      while ( true ) {
	try {
	  synchronized (this) {
	    this.wait(3000);
	  }
	}
	catch ( Exception e ) {
	  OutputHandler.out(ExceptionTools.toString("GCV:uT:runloop", e));
	}
	SwingQueue.invokeLater(updateCountRunnable);
      }
    }
  }

  public void paint(Graphics g)
  {
    int numRows = gc.getNumRows();
    if ( numRows % 10 == 0 )
      cb.getcountLabel().setText(String.valueOf(numRows));
    super.paint(g);
  }
}
