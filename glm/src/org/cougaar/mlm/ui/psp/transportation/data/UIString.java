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
 
package org.cougaar.mlm.ui.psp.transportation.data;

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
