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

import java.util.Vector;
import java.util.Hashtable;

import java.net.URL;
import java.net.URLConnection;

import java.io.InputStream;

import java.awt.BorderLayout;
import java.awt.ScrollPane;
import java.awt.Color;
import java.awt.Font;
import java.awt.MenuBar;
import graph.Chart;
import graph.YYYYMMDDMath;

//import org.cougaar.tutorial.booksonline.ui.BOLPSPState;
//import org.cougaar.lib.uiframework.ui.components.desktop.NChart;
//import org.cougaar.lib.uiframework.ui.components.graph.DataSet;
import java.applet.*;

/***********************************************************************************************************************
<b>Description</b>: Applet which contacts a data stream KeepAlive PSP to get a stream of inventory count information and
										displays the information in a bar graph.

<br><br><b>Notes</b>:<br>
									-

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
public class ReadinessAssessorApplet extends Applet implements java.lang.Runnable
{
	/*********************************************************************************************************************
  <b>Description</b>: The data input stream opened when the data stream KeepAlive PSP is contacted.

  <br><br><b>Notes</b>:<br>
										-
	*********************************************************************************************************************/
  private InputStream input = null;

	/*********************************************************************************************************************
  <b>Description</b>: An array of colors used to display each bar in the graph.  This array is cycled through to
  										provide each bar in the graph with a different color.  The colors are repeated when the end of
  										the array is reached.

  <br><br><b>Notes</b>:<br>
										-
	*********************************************************************************************************************/
//	private Color[] colorSet = new Color[] {Color.red, Color.green, Color.blue, Color.pink, Color.orange, Color.magenta, Color.cyan, Color.yellow};

	/*********************************************************************************************************************
  <b>Description</b>: The GUI chart component for the current graph.

  <br><br><b>Notes</b>:<br>
										-
	*********************************************************************************************************************/
	private Chart chartUI = null;

	/*********************************************************************************************************************
  <b>Description</b>: The scroll pane for the bar chart.  This is used to create a scrollable view port if the bar
  										graph besomes larger than the viewing area.

  <br><br><b>Notes</b>:<br>
										-
	*********************************************************************************************************************/
	private ScrollPane pane = null;

	/*********************************************************************************************************************
  <b>Description</b>: Initializes the applet GUI interface.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @see #start()
  @see #stop()
  @see #run()
	*********************************************************************************************************************/

  private Hashtable stores = new Hashtable();

	public void init()
	{

		setLayout(new BorderLayout());

    int numSupplyClasses = Integer.parseInt(getParameter(ReadinessAssessorPspUtil.NUMMAINTAINED));
    int numDirectObjs, numItems;
    double[] dataSet = null;
    
    for (int chartNum = 1; chartNum <= numSupplyClasses; chartNum ++)
    {

      String maintName = new String (getParameter (new String (ReadinessAssessorPspUtil.MAITAINEDCLASSNAME + chartNum)) );

      numDirectObjs = Integer.parseInt (getParameter (new String (ReadinessAssessorPspUtil.NUMDIRECTOBJS + chartNum)) );

      Vector oneChartVecs[] = new Vector [numDirectObjs];

      String assetName = new String();
      for (int dirObjNum = 1; dirObjNum <= numDirectObjs; dirObjNum ++)
      {

        assetName = new String (getParameter ( new String (ReadinessAssessorPspUtil.ASSETNAMEPARAM + chartNum + "_" + dirObjNum ) ) );

        numItems = Integer.parseInt (getParameter ( new String (ReadinessAssessorPspUtil.NUMASPECTITEMS + chartNum + "_" + dirObjNum ) ));

        dataSet = new double [numItems*2];

        int itemNum, loadIdx;
        for (itemNum = 1, loadIdx = 0; itemNum <= numItems; itemNum ++, loadIdx +=2)
        {

//             System.out.println ("\t" + ReadinessAssessorPspUtil.ATDATEPARAM + chartNum + "_" + dirObjNum + "_" + itemNum  );
//             System.out.println ("\t" + ReadinessAssessorPspUtil.READINESSPARAM + chartNum + "_" + dirObjNum + "_" + itemNum );

           // x axis go in even indices, y axis goes in odd indices
           dataSet [loadIdx] = YYYYMMDDMath.toMillis (NumberUtil.parseDouble(getParameter (new String (ReadinessAssessorPspUtil.ATDATEPARAM + chartNum + "_" + dirObjNum + "_" + itemNum ))));
           dataSet [loadIdx+1] = NumberUtil.parseDouble(getParameter (new String (ReadinessAssessorPspUtil.READINESSPARAM + chartNum + "_" + dirObjNum + "_" + itemNum )));
        }

        DoubleArrayHolder dataSetHolder = new DoubleArrayHolder (dataSet);
        stores.put(new String (maintName + " - " + assetName), dataSetHolder);

      }

    }

 		add( new ReadinessChartGUI(stores), BorderLayout.CENTER);
  	setVisible(true);
	}

	/*********************************************************************************************************************
  <b>Description</b>: Called by the browser window when the applet is started.  This method will call stop() to close
  										the data stream, causing any thread this applet had already started to exit and start a second
  										thread to open and monitor the connection to the data stream KeepAlive PSP

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @see #init()
  @see #stop()
  @see #run()
	*********************************************************************************************************************/
	public void start()
	{
		// pane.repaint();
		// Scroll to bottom left corner initially
		// pane.setScrollPosition(0, 500);

		stop();

		(new Thread(this)).start();
	}

	/*********************************************************************************************************************
  <b>Description</b>: Called by the browser window when the applet is no longer displayed.  This method will close the
  										data stream, causing the second applet thread to exit.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @see #init()
  @see #start()
  @see #run()
	*********************************************************************************************************************/
	public void stop()
	{
    try
    {
	  	input.close();
	  	input = null;
    }
    catch (Exception e )
    {
    }
	}

	/*********************************************************************************************************************
  <b>Description</b>: Method for spawned applet thread (second applet thread) which opens the connection to and reads
  										data from the data stream KeepAlive PSP.  If the connection is closed (either by the KeepAlive
  										PSP or the stop() method of this applet) this method will exit and its thread will end.

  <br><b>Notes</b>:<br>
	                  -

  <br>
  @see #init()
  @see #start()
  @see #stop()
	*********************************************************************************************************************/
	public void run()
	{
/*

		try
		{
			// Create the connection to the data stream KeepAlive PSP URL specified in the
			// applet parameters of the HTML
			URL url = new URL(getParameter(BOLPSPState.PSP_URL_NAME));
			URLConnection urlCon = url.openConnection();

			// Set the connection parameters
			urlCon.setDoInput(true);
			urlCon.setAllowUserInteraction(false);

			// Connect to the URL and get the stream
			urlCon.connect();
      input = urlCon.getInputStream();

			// While there is data in the stream, read it and put the data in the chart
			String streamData = null;
			int dataIndex = 0;
			int separatorIndex = 0;
			int bookNumber = -1;
			int count = -1;
			while ((streamData = readLine()) != null)
			{
				// The data format is of the form <DATA #A:#B> where #A is the book number and #B is the quantity in stock
				// Because this is a keep alive PSP, there may be inserted characters (of the form <ACK>)
				// to keep the reader of the stream from closing it during periods of inactivity
				dataIndex = streamData.lastIndexOf("<DATA ");
				if (dataIndex == -1)
				{
					continue;
				}

				separatorIndex = streamData.indexOf(":", dataIndex);

				// Get the book number and count
				bookNumber = Integer.parseInt(streamData.substring((dataIndex+6), separatorIndex));
				count = Integer.parseInt(streamData.substring(separatorIndex+1, streamData.indexOf(">", separatorIndex+1)));

				// Set the new value on the bar chart
				barChart.setValue(bookNumber, count);
//				barChart.setScale(1.0);

				barChart.repaint();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
*/
	}

	/*********************************************************************************************************************
  <b>Description</b>: Reads a line from the input stream.

  <br><b>Notes</b>:<br>
	                  - This method was created to fix a difference in implementation between Netscape and Internet
	                  	Explorer concerning java.io.Reader.readLine() methods.

  <br>
  @return The first new-line terminated string from the stream or null if the end of the stream is reached or an
  				error occurs
	*********************************************************************************************************************/
	private String readLine()
	{
		String buffer = "";
		int ch = -1;

    try
    {
    	// Loop until the end of the line is found and then return
		  while ((ch = input.read()) != -1)
		  {
			  buffer += (char)ch;
			  if ((char)ch == '\n')
			  {
				  return(buffer);
			  }
      }
    }
    catch (Exception e)
    {
    }

		// End of stream or error
		return(null);
	}
}
