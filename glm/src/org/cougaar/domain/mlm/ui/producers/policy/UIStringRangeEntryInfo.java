
/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers.policy;

import java.util.ArrayList;

import org.cougaar.domain.planning.ldm.policy.RangeRuleParameterEntry;
import org.cougaar.domain.planning.ldm.policy.RuleParameter;

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










