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
 
package org.cougaar.domain.mlm.ui.views.policy;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.*;

/**
 * Extension of JTextField that supports displaying/modifying longs
 * Taken from LongField in xxx tutorial
 * <p>
 * Not particularly bright about validating inserted characters -
 * accepts digits and a leading '-'.
 */

public  class LongField extends JTextField {
  private Toolkit myToolkit;
  private NumberFormat myLongFormatter;
  
  /**
   * Constructor - takes initial value and number of columns
   *
   * @param value long specifying initial value
   * @param columns int specifying the number of columns
   */
  public LongField(long value, int columns) {
    super(columns);
    myToolkit = Toolkit.getDefaultToolkit();
    myLongFormatter = NumberFormat.getNumberInstance();
    myLongFormatter.setParseIntegerOnly(true);
    setValue(value);
  }
  
  /**
   * getValue() - returns current entry as an long
   *
   * @return long current entry
   */
  public long getValue() {
    long retVal = 0;
    try {
      retVal = myLongFormatter.parse(getText()).longValue();
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
   * @param value long specifying the new entry
   */
  public void setValue(long value) {
    setText(myLongFormatter.format(value));
  }
  
  protected Document createDefaultModel() {
    return new LongDocument();
  }
  
  /**
   * LongDocument - validates inserts to ensure that they're
   * valid for longs
   */
  private class LongDocument extends PlainDocument {
    
    /**
     * insertString - validates character to be inserted
     * Not very sophisticated - accepts digits and a leading '-'
     *
     * @see javax.swing.text.PlanDocument
     */
    public void insertString(int offs, 
                             String str,
                             AttributeSet a) 
      throws BadLocationException {
      char[] source = str.toCharArray();
      char[] result = new char[source.length];
      int length = 0;
      boolean success = true;
      
      for (int i = 0; i < result.length; i++) {
        if (Character.isDigit(source[i])) {
          result[length++] = source[i];
        } else if ((i == 0) &&
                   (source[i] == '-') &&
                   (offs == 0) &&
                   (this.getText(0, getLength()).indexOf('-') == -1)) {
          result[length++] = source[i];
        } else {
          myToolkit.beep();
          success = false;

          break;
        }
      }
      
      if (success) {
        super.insertString(offs, new String(result, 0, length), a);
      }
    }
  }
}




