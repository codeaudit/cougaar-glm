/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.transportation.data;

public class UIString implements org.cougaar.core.util.SelfPrinter, java.io.Serializable {

  protected String STR;

  public String extractParameter(String TAG)
  {
         int i_a = STR.indexOf(TAG);
         int i_b=0;
         if( i_a > -1 ) {
             i_a += TAG.length();
             i_b = STR.indexOf(";", i_a);
             if( i_b < 0 ) i_b = STR.length();
         }else{
            i_a = 0;
            i_b = STR.length();
         }
         String arg = STR.substring(i_a,i_b);
         return arg;
  }

  public String getString() { return STR; }
  public void setSTR(String s) {STR = s;}

  public void printContent(org.cougaar.core.util.AsciiPrinter pr) {
    pr.print(STR, "STR");
  }

  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

  /** set 3/29/00 **/
  final static long serialVersionUID = 6559545086002036669L;

}
