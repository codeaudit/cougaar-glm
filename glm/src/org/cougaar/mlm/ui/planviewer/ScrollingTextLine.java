/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.mlm.ui.planviewer;

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








