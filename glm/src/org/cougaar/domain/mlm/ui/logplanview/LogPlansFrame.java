package org.cougaar.domain.mlm.ui.logplanview;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Java JFrame object used to display buttons that give access to multiple Cougaar Agents' BlackBoard contents.
 */
public class LogPlansFrame extends JFrame {
  static private boolean deactivate = false;

  static int buttonCount = 0;

  static Vector list = new Vector();

  JPanel buttonsPanel = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  static LogPlansFrame theFrame = null;

  private LogPlansFrame() {
    try  {
      jbInit();
    }
    catch (Exception ex) {
      System.err.println( "Caught: " + ex);
      ex.printStackTrace();
    }
  }

  static private LogPlansFrame getLogPlansFrame() {
    if ( theFrame == null) {
      theFrame = new LogPlansFrame();
      theFrame.setTitle( "Plans");
      theFrame.setLocation( 240, 0);
      theFrame.pack();
      theFrame.show();
    }
    return theFrame;
  }

  /**
   *
   */
  static public void addLogPlan( final LogPlanModelServerPlugIn logPlanModelServerPlugIn) {
    if ( deactivate) {
      return;
    }
    String clusterName = logPlanModelServerPlugIn.getClusterName();
    if ( !(list.contains( clusterName))) {
      list.add( clusterName);
      JButton button = new JButton( clusterName);
      button.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          LogPlanFrame logPlanFrame = new LogPlanFrame( logPlanModelServerPlugIn.getClusterName(), logPlanModelServerPlugIn.getLogPlanModel());
          logPlanFrame.pack();
          logPlanFrame.show();
        }
      });
      button.setEnabled( true);
      LogPlansFrame frame = getLogPlansFrame();
      if ( frame != null) {
        buttonCount++;
        GridBagConstraints gridBagConstraints = null;
        if ( (buttonCount % 4) == 0) {
          gridBagConstraints = new GridBagConstraints(  GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
                                                        GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        } else {
          gridBagConstraints = new GridBagConstraints(  GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
                                                        1, 1, 1.0, 1.0,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        }
        frame.buttonsPanel.add( button, gridBagConstraints);
        frame.pack();
      }
    }
  }

  private void jbInit() throws Exception {
    buttonsPanel.setLayout(gridBagLayout1);
    //buttonsPanel.setMinimumSize(new Dimension(180, 80));
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }
    });
    this.getContentPane().setLayout(gridBagLayout2);
    this.getContentPane().add(buttonsPanel, new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
  }


  void this_windowClosing(WindowEvent e) {
    System.exit(0);
  }
}


