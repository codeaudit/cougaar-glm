/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */


package org.cougaar.mlm.debug.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** A single line of text with a horizontal scroll bar.
  From Ray Tomlinson.
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

}


/** A text field that reports its preferred size as the width
  of its contents, or if it's empty, the width of the number of columns
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
      return new Dimension(getFontMetrics(getFont()).stringWidth(getText()), 
			   d.height);
  }

}








