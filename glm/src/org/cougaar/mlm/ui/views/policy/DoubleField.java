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
 
package org.cougaar.mlm.ui.views.policy;

import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.JTextField;
import javax.swing.text.*;

/**
 * Extension of JTextField that supports displaying/modifying doubles
 * Taken from DoubleField in xxx tutorial
 */

public class DoubleField extends JTextField {
  private DecimalFormat myFormat;
  private Toolkit myToolkit;
  
  /**
   * Constructor - takes initial value, number of columns, DecimalFormat
   *
   * @param value double specifying initial value
   * @param columns int specifying the number of columns
   * @param format DecimatFormat specifying display format
   */
  public DoubleField(double value, int columns, DecimalFormat format) {
    super(columns);
    myToolkit = Toolkit.getDefaultToolkit();
    myFormat = format;
    setDocument(new DoubleDocument());
    setValue(value);
  }
  
  /**
   * getValue - return current entry as a double
   * 
   * @return double current entry
   */
  public double getValue() {
    double retVal = 0.0;
    
    try {
      retVal = myFormat.parse(getText()).doubleValue();
    } catch (ParseException e) {
      // This should never happen because insertString allows
      // only properly formatted data to get in the field.
      myToolkit.beep();
    }
    return retVal;
  }
  
  
  /**
   * setValue - sets current entry
   *
   * @param value double specifying the value to be displayed
   */
  public void setValue(double value) {
    setText(myFormat.format(value));
  }

  /**
   * Double Document - validates inserts/removes to ensure that they're valid
   * for doubles
   */
  private class DoubleDocument extends PlainDocument {
    
    /**
     * insertString - verifies that field with insert represents a valid 
     * integer.
     * 
     * @see javax.swing.text.AbstractDocument.insertString
     */
    public void insertString(int offs, String str, AttributeSet a) 
      throws BadLocationException {
      
      String currentText = this.getText(0, getLength());
      String beforeOffset = currentText.substring(0, offs);
      String afterOffset = currentText.substring(offs, currentText.length());
      String proposedResult = beforeOffset + str + afterOffset;
      
      ParsePosition parsePosition = new ParsePosition(0);
      myFormat.parseObject(proposedResult, parsePosition);
      if (parsePosition.getIndex() == proposedResult.length()) {
        super.insertString(offs, str, a);
      } else {
        myToolkit.beep();
      }
    }
    
    /**
     * remove - verifies that field after removal represents a valid double
     * 
     * @see javax.swing.text.AbstractDocument.remove
     */
    public void remove(int offs, int len) throws BadLocationException {
      String currentText = this.getText(0, getLength());
      String beforeOffset = currentText.substring(0, offs);
      String afterOffset = currentText.substring(len + offs,
                                                 currentText.length());
      String proposedResult = beforeOffset + afterOffset;
      
      if (proposedResult.length() != 0) {    
        ParsePosition parsePosition = new ParsePosition(0);
        myFormat.parseObject(proposedResult, parsePosition);
        if (parsePosition.getIndex() == proposedResult.length()) {
          super.remove(offs, len);
        } else {
          myToolkit.beep();
        }
      }
    }
  }

}
