
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
 
package org.cougaar.mlm.ui.producers.policy;

import java.util.ArrayList;

import org.cougaar.planning.ldm.policy.RangeRuleParameterEntry;
import org.cougaar.planning.ldm.policy.RuleParameter;

import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

public class UIStringRangeEntryInfo extends UIRangeEntryInfo {
  
  public UIStringRangeEntryInfo(RangeRuleParameterEntry entry) {
    super((String) entry.getValue(), entry.getRangeMin(), entry.getRangeMax());
  }

  public UIStringRangeEntryInfo(String value, int min, int max) {
    super(value, min, max);
  }

  public UIStringRangeEntryInfo() {
  }


  /**
   * @param Object  - set the value
   */
  public void setValue(Object value) {
    if (!(value instanceof String)) {
      throw new IllegalArgumentException();
    }
    super.setValue(value);
  }
}










