package org.cougaar.domain.mlm.ui.logplanview;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

/**
 * Java JFrame object used to display contents of a Cougaar Agent's BlackBoard.
 */
public class LogPlanFrame extends JFrame {

  public LogPlanFrame( String clusterName, LogPlanModel model) {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    getContentPane().setLayout(gridBagLayout1);

    LogPlanTree logPlanTree = new LogPlanTree( model);

    JScrollPane scrollPane = new JScrollPane( logPlanTree);
    scrollPane.setPreferredSize(new Dimension(800, 400));
    scrollPane.setMinimumSize(new Dimension(200, 100));
    getContentPane().add( scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    setTitle( clusterName + ":LogPlan");
    setBounds( 50, 0, 200, 400);
  }
}


