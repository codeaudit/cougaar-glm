/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;

/** 
 * A single line of text with a horizontal scroll bar.
 */

public class ScrollingTextLine extends JPanel {
  MyTextField tf = new MyTextField();
  JScrollBar sb = new JScrollBar(SwingConstants.HORIZONTAL);

  public ScrollingTextLine(int ncolumns)
  {
    super(new BorderLayout());
    tf.setColumns(ncolumns);
    final BoundedRangeModel rm = tf.getHorizontalVisibility();
    // make text field's visibility model be the scrollbar's model
    sb.setModel(rm);
    sb.updateUI();  // JScrollBar fails to update its UI when its model changes
    sb.setUnitIncrement(16);// Move 16 pixels at a time.
    add(tf, BorderLayout.CENTER);
    add(sb, BorderLayout.SOUTH);
    tf.setEditable(false);
    tf.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        sb.setBlockIncrement(tf.getSize().width - 16);
      }
    });
  }

  public void setText(String s) {
    tf.setText(s);
    tf.setScrollOffset(0);
  }

  public String getText() {
    return tf.getText();
  }

}


/** A text field that reports its preferred size as the 
  maximum of the width of its contents, and the width
  of the number of columns with which is was constructed,
  or if it's empty, the width of the number of columns
  with which it was constructed.
  */

class MyTextField extends JTextField {

  public MyTextField() {
    super();
  }

  public MyTextField(int ncolumns) {
    super(ncolumns);
  }

  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (getText().length() == 0)
      return new Dimension(getColumns() * getColumnWidth(), d.height);
    else
      return new Dimension(Math.max(getColumns() * getColumnWidth(),
                       getFontMetrics(getFont()).stringWidth(getText())),
                           d.height);
  }

}








