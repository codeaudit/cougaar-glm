/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.ui.data;

import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
   Override the default property descriptors.
   A property descriptor contains:
   attribute name, bean class, read method name, write method name
   All other beaninfo is defaulted.
*/

public class UIPlanElementImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[4];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.domain.mlm.ui.data.UIPlanElementImpl");
      pd[i++] = new PropertyDescriptor("planName",
                                       beanClass,
                                       "getPlanName",
                                       null);
      pd[i++] = new PropertyDescriptor("task",
                                       beanClass,
                                       "getTask",
                                       null);
      pd[i++] = new PropertyDescriptor("estimatedResult",
                                       beanClass,
                                       "getEstimatedResult",
                                       null);
      pd[i++] = new PropertyDescriptor("reportedResult",
                                       beanClass,
                                       "getReportedResult",
                                       null);

      // note, do no introspection on sub-classes;
      // these are the only values returned
      return pd;
    } catch (Exception e) {
      System.out.println("Exception:" + e);
    }
    return null;
  }

}
