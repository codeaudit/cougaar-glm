/*
 * <copyright>
 * Copyright 1997-2000 Defense Advanced Research Projects Agency (DARPA)
 * and Clark Software Engineering (CSE) This software to be used in
 * accordance with the COUGAAR license agreement.  The license agreement
 * and other information on the Cognitive Agent Architecture (COUGAAR)
 * Project can be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */
package org.cougaar.domain.mlm.ui.readiness;

import java.util.Hashtable;

import java.awt.Panel;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Choice;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import graph.ReadinessChart;
import graph.DataSet;
import graph.YYYYMMDDMath;

/***********************************************************************************************************************
<b>Description</b>: Used to completely contain all chart GUIs into one control that can be added to any GUI component
										for display.

<br><br><b>Notes</b>:<br>
									-

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
public class ReadinessChartGUI extends java.awt.Panel
{
	/*********************************************************************************************************************
  <b>Description</b>: The ReadinessChart object that the current GUI component controls and displays.

  <br><br><b>Notes</b>:<br>
										-
	*********************************************************************************************************************/
	private ReadinessChart chart = null;

	/*********************************************************************************************************************
  <b>Description</b>: Constructor for chart GUI that takes the shared memory data can creates a chart object that uses
  										the shared memory to create a graph.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @param dataSet Shared memory that the chart object will use to create a graph
	*********************************************************************************************************************/
	public ReadinessChartGUI (Hashtable dataSet)
	{

    double[] firstData = { System.currentTimeMillis(), 0.0,
                           System.currentTimeMillis() + (1000.0 * 60.0 * 60.0 * 24.0), 0.0};

		// Create the chart object this GUI will control
    // start with nothing, let the user decide what to pick

    DoubleArrayHolder dah = (DoubleArrayHolder) dataSet.get("Overall - All Assets");

    if (dah != null)
    {
      double[] dahFirstData = dah.holdThis;
      if (dahFirstData != null)
      {
        firstData = dahFirstData;
      }
    }

		chart = new ReadinessChart(firstData, "Date", "Readiness", 3000, 1.0 );
    chart.setBuffer(0.5);  // don't pad the sides of the graph too much

    // Y Axis stuff
    chart.setYMax(1.0, false);

    // X Axis stuff
    chart.setWindowHistoryRange(30.0, false); // only show 30 days at a time

    if (dah != null)
    {
      // set max first otherwise min is greater than max and chart doesn't like that
      chart.setXMax( dah.holdThis[0] + (1000.0 * 60.0 * 60.0 * 24.0 * 30.0), false );

      chart.setXMin( dah.holdThis[0], true);  // draw it too
    }
    
		// Set the GUI layout and add the chart object to the layout
		setLayout(new BorderLayout());
		setBackground(Color.lightGray);
		add(chart, BorderLayout.CENTER);

		Panel controls = new Panel(new GridLayout(4, 1));
		CheckboxGroup group = new CheckboxGroup();

		// Create the chart controls
		controls.add(new MovingWindowControl(this, group));
		controls.add(new FixedWindowControl(this, group));
		controls.add(new WholeGraphControl(this, group));

    controls.add (new ItemAssetPulldownControl (this, dataSet));

		// Add the cart controls to the GUI
		add(controls, BorderLayout.NORTH);

  	// Start the chart object's thread
    (new Thread(chart)).start();

  }

	/*********************************************************************************************************************
  <b>Description</b>: Create a checkbox control, adds it to the specfied checkbox group and adds a listener to it.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @param listener Control listener for checkbox
  @param text Text to display with checkbox
  @param group Checkbox group to add the checkbox to
  @param checked Current state of the checkbox
  @return A Checkbox control associated with the specified checkbox group
	*********************************************************************************************************************/
	public Checkbox getWindowSelectionControl(ItemListener listener, String text, CheckboxGroup group, boolean checked)
	{
		// Create a checkbox control and add the specified listener
		Checkbox checkbox = new Checkbox(text, group, checked);
		checkbox.addItemListener(listener);

		return(checkbox);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Returns the chart object for this GUI.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @return The chart object for this GUI
	*********************************************************************************************************************/
	public ReadinessChart getChart()
	{
		return(chart);
	}
}

/***********************************************************************************************************************
<b>Description</b>: Control for manipulating a chart object to display its graph in a moving range.

<br><br><b>Notes</b>:<br>
									- 

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
class MovingWindowControl extends java.awt.Panel implements java.awt.event.ItemListener
{
	/*********************************************************************************************************************
  <b>Description</b>: ReadinessChart object manipulated by this control.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
	private ReadinessChart chart = null;
	
	/*********************************************************************************************************************
  <b>Description</b>: Text field control that specifies the range of the moving window from current time minus range to
  										current time.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
	private TextField rangeTextField = null;

	/*********************************************************************************************************************
  <b>Description</b>: Creates a control that will manipulate the GUI's chart object.

  <br><b>Notes</b>:<br>
	                  - Manipulates the chart object when in Moving Window mode

  <br>
  @param gui GUI object this control is attached to
  @param group Checkbox group this control's checkbox button is to be attached to
	*********************************************************************************************************************/
	public MovingWindowControl(ReadinessChartGUI gui, CheckboxGroup group)
	{
		super(new GridLayout(2,1));

		// Get the chart object this control will manipulate and create the control
		chart = gui.getChart();
		add(gui.getWindowSelectionControl(this, "Moving Window", group, true));

		// Set the control's physical layout and label
		Panel panel = new Panel(new GridLayout(1,2));
		panel.add(new Label("Range Bound"));
		rangeTextField = getRangeControl();
		panel.add(rangeTextField);
		add(panel);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Signals the Moving Window state has been selected/deselected.  If the current state has
  										been set to the Moving Window state, this method will set the chart to the Moving Window state
  										and set the range of the graph to be the value in the rangeTextField control.

  <br><b>Notes</b>:<br>
	                  - Required by the ItemListener interface

  <br>
  @param e Event object associated with the event
	*********************************************************************************************************************/
	public void itemStateChanged(ItemEvent e)
	{
		// If this control was selected, set the chart's operational mode to the Moving Window mode
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			chart.setMode(ReadinessChart.MOVING_WINDOW);
			chart.setWindowHistoryRange(NumberUtil.parseDouble(rangeTextField.getText()), true);
		}
	}

	/*********************************************************************************************************************
  <b>Description</b>: Creates a text field control that will act on a change to its value and notify the chart object
  										of the new value to set the moving window range to.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @return The text field control for the moving window range 
	*********************************************************************************************************************/
	public TextField getRangeControl()
	{
		// Create a text field and an action listener for the text field
		TextField textField = new TextField("" + chart.getWindowHistoryRange(), 10);
		textField.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// When this control is selected, set the chart object's Window History Range to the value in the text field
					chart.setWindowHistoryRange(NumberUtil.parseDouble(rangeTextField.getText()), true);
				}
			});

		return(textField);
	}
}

/***********************************************************************************************************************
<b>Description</b>: Control for manipulating a chart object to display its graph in a fixed range.

<br><br><b>Notes</b>:<br>
									- 

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
class FixedWindowControl extends java.awt.Panel implements java.awt.event.ItemListener
{
	/*********************************************************************************************************************
  <b>Description</b>: ReadinessChart object manipulated by this control.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
	private ReadinessChart chart = null;
	
	/*********************************************************************************************************************
  <b>Description</b>: Text field control that specifies the lower bound of the fixed window.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
	TextField lowerBoundTextField = null;

	/*********************************************************************************************************************
  <b>Description</b>: Text field control that specifies the upper bound of the fixed window.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
	TextField upperBoundTextField = null;

	/*********************************************************************************************************************
  <b>Description</b>: Creates a control that will manipulate the GUI's chart object.

  <br><b>Notes</b>:<br>
	                  - Manipulates the chart object when in Fixed Window mode

  <br>
  @param gui GUI object this control is attached to
  @param group Checkbox group this control's checkbox button is to be attached to
	*********************************************************************************************************************/
	public FixedWindowControl(ReadinessChartGUI gui, CheckboxGroup group)
	{
		super(new GridLayout(2,5));

		// Get the chart object this control will manipulate and create the control
		chart = gui.getChart();
		add(gui.getWindowSelectionControl(this, "Fixed Window", group, false));

		// Set the control's physical layout and label
		Panel panel = new Panel(new GridLayout(1,4));
		panel.add(new Label("Lower Bound"));
		upperBoundTextField = getUpperBoundControl();
		lowerBoundTextField = getLowerBoundControl();
		panel.add(lowerBoundTextField);
		panel.add(new Label("Upper Bound"));
		panel.add(upperBoundTextField);
		add(panel);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Signals the Fixed Window state has been selected/deselected.  If the current state has
  										been set to the Fixed Window state, this method will set the chart to the Fixed Window state
  										and set the range of the graph to be the values in the lowerBoundTextField and upperBoundTextField
  										controls.

  <br><b>Notes</b>:<br>
	                  - Required by the ItemListener interface

  <br>
  @param e Event object associated with the event
	*********************************************************************************************************************/
	public void itemStateChanged(ItemEvent e)
	{
		// If this control was selected, set the chart's operational mode to the Fixed Window mode
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			chart.setMode(ReadinessChart.FIXED_WINDOW);
			chart.setXMin( YYYYMMDDMath.toMillis( NumberUtil.parseDouble(lowerBoundTextField.getText())), false);
			chart.setXMax( YYYYMMDDMath.toMillis( NumberUtil.parseDouble(upperBoundTextField.getText())), true);
		}
	}

	/*********************************************************************************************************************
  <b>Description</b>: Creates a text field control that will act on a change to its value and notify the chart object
  										of the new value to set the lower bound of the fixed window range to.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @return The text field control for the lower bound of the fixed window range 
	*********************************************************************************************************************/
	public TextField getLowerBoundControl()                            
	{
		// Create a text field and an action listener for the text field
		TextField textField = new TextField( "" + (long) (YYYYMMDDMath.fromMillis(chart.getXMin())), 10);
		textField.addActionListener(new ActionListener()
		{
				public void actionPerformed(ActionEvent e)
				{
					// When this control is selected, set the chart object's X Min to the value in the text field
					chart.setXMin( YYYYMMDDMath.toMillis(NumberUtil.parseDouble(lowerBoundTextField.getText())));
				}
			});

		return(textField);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Creates a text field control that will act on a change to its value and notify the chart object
  										of the new value to set the upper bound of the fixed window range to.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @return The text field control for the upper bound of the fixed window range 
	*********************************************************************************************************************/
	public TextField getUpperBoundControl()
	{
		// Create a text field and an action listener for the text field
		TextField textField = new TextField( "" + (long) YYYYMMDDMath.fromMillis (chart.getXMax()), 10);
		textField.addActionListener(new ActionListener()
		{
				public void actionPerformed(ActionEvent e)
				{
					// When this control is selected, set the chart object's X Max to the value in the text field
					chart.setXMax( YYYYMMDDMath.toMillis (NumberUtil.parseDouble(upperBoundTextField.getText())));
				}
		 });

		 return(textField);
	}
  
}

/***********************************************************************************************************************
<b>Description</b>: Control for manipulating a chart object to display its whole graph.

<br><br><b>Notes</b>:<br>
									- 

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
class WholeGraphControl extends java.awt.Panel implements java.awt.event.ItemListener
{
	/*********************************************************************************************************************
  <b>Description</b>: ReadinessChart object manipulated by this control.

  <br><br><b>Notes</b>:<br>
										- 
	*********************************************************************************************************************/
	private ReadinessChart chart = null;

	/*********************************************************************************************************************
  <b>Description</b>: Creates a control that will manipulate the GUI's chart object.

  <br><b>Notes</b>:<br>
	                  - Manipulates the chart object when in Whole Graph mode

  <br>
  @param gui GUI object this control is attached to
  @param group Checkbox group this control's checkbox button is to be attached to
	*********************************************************************************************************************/
	public WholeGraphControl(ReadinessChartGUI gui, CheckboxGroup group)
	{
		super(new GridLayout(1,1));

		// Get the chart object this control will manipulate and create the control
		chart = gui.getChart();
		add(gui.getWindowSelectionControl(this, "Whole Graph", group, false));
	}

	/*********************************************************************************************************************
  <b>Description</b>: Signals the Whole Graph state has been selected/deselected.  If the current state has
  										been set to the Whole Graph state, this method will set the chart to the Whole Graph state.

  <br><b>Notes</b>:<br>
	                  - Required by the ItemListener interface

  <br>
  @param e Event object associated with the event
	*********************************************************************************************************************/
	public void itemStateChanged(ItemEvent e)
	{
		// If this control was selected, set the chart's operational mode to the Whole Graph mode
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			chart.setMode(ReadinessChart.WHOLE_GRAPH);
		}
	}
}





class ItemAssetPulldownControl extends java.awt.Panel implements java.awt.event.ItemListener
{
	/*********************************************************************************************************************
  <b>Description</b>: ReadinessChart object manipulated by this control.

  <br><br><b>Notes</b>:<br>
										-
	*********************************************************************************************************************/
	private ReadinessChart chart = null;
  private Hashtable allData = null;

	/*********************************************************************************************************************
  <b>Description</b>: Creates a control that will manipulate the GUI's chart object.

  <br><b>Notes</b>:<br>
	                  - Manipulates the chart object when in Fixed Window mode

  <br>
  @param gui GUI object this control is attached to
  @param group Checkbox group this control's checkbox button is to be attached to
	*********************************************************************************************************************/
	public ItemAssetPulldownControl(ReadinessChartGUI gui, Hashtable dataStore)
	{

		super(new GridLayout(1,1));

    chart = gui.getChart();
    allData = dataStore;

		// Get the chart object this control will manipulate and create the control
    Choice pullDown = new Choice ();
    pullDown.addItemListener(this);

    java.util.Enumeration e = allData.keys();
    while (e.hasMoreElements())
    {
      pullDown.add((String) e.nextElement());
    }

    pullDown.select("Overall - All Assets");

    add (pullDown);

	}

	/*********************************************************************************************************************
  <b>Description</b>: Signals the Fixed Window state has been selected/deselected.  If the current state has
  										been set to the Fixed Window state, this method will set the chart to the Fixed Window state
  										and set the range of the graph to be the values in the lowerBoundTextField and upperBoundTextField
  										controls.

  <br><b>Notes</b>:<br>
	                  - Required by the ItemListener interface

  <br>
  @param e Event object associated with the event
	*********************************************************************************************************************/
	public void itemStateChanged(ItemEvent e)
	{
		// If this control was selected, set the chart's operational mode to the Fixed Window mode
		if (e.getStateChange() == ItemEvent.SELECTED)
		{

      String selected = (String) e.getItem();

      try
      {

        DoubleArrayHolder dah = (DoubleArrayHolder) allData.get(selected);

        if (dah == null)  // there's nothing to do
          return;

        double [] dbl = dah.holdThis;

        chart.changeData (dbl);
        chart.setYMax(1.0, false);
        // chart.setYAxisMinimumSize(1.5, false);   // includes buffer value

        chart.setXMin (dbl[0], false);
        chart.setXMax (dbl[dbl.length / 2], true);

      }

      catch (Exception exc)
      {
        System.out.println ("ReadinessChartGUI:itemStateChanged \t" + exc.toString());
        exc.printStackTrace();
      }

		}

	}

}
