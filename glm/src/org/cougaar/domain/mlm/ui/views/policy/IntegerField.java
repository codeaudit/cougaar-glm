/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
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
 * Extension of JTextField that supports displaying/modifying integers
 * Taken from IntegerField in xxx tutorial
 * <p>
 * Not particularly bright about validating inserted characters -
 * accepts digits and a leading '-'.
 */

public  class IntegerField extends JTextField {
  private Toolkit myToolkit;
  private NumberFormat myIntegerFormatter;
  
  /**
   * Constructor - takes initial value and number of columns
   *
   * @param value int specifying initial value
   * @param columns int specifying the number of columns
   */
  public IntegerField(int value, int columns) {
    super(columns);
    myToolkit = Toolkit.getDefaultToolkit();
    myIntegerFormatter = NumberFormat.getNumberInstance();
    myIntegerFormatter.setParseIntegerOnly(true);
    setValue(value);
  }
  
  /**
   * getValue() - returns current entry as an integer
   *
   * @return int current entry
   */
  public int getValue() {
    int retVal = 0;
    try {
      retVal = myIntegerFormatter.parse(getText()).intValue();
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
   * @param value int specifying the new entry
   */
  public void setValue(int value) {
    setText(myIntegerFormatter.format(value));
  }
  
  protected Document createDefaultModel() {
    return new IntegerDocument();
  }
  
  /**
   * IntegerDocument - validates inserts to ensure that they're
   * valid for integers
   */
  private class IntegerDocument extends PlainDocument {
    
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




