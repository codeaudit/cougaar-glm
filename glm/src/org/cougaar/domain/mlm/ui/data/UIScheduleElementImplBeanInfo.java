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

public class UIScheduleElementImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[10];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.domain.mlm.ui.data.UIScheduleElementImpl");
      pd[i++] = new PropertyDescriptor("startDate",
                                       beanClass,
                                       "getStartDate",
                                       null);
      pd[i++] = new PropertyDescriptor("endDate",
                                       beanClass,
                                       "getEndDate",
                                       null);
      pd[i++] = new PropertyDescriptor("role",
                                       beanClass,
                                       "getRole",
                                       null);
      pd[i++] = new PropertyDescriptor("startLocation",
                                       beanClass,
                                       "getStartLocation",
                                       null);
      pd[i++] = new PropertyDescriptor("endLocation",
                                       beanClass,
                                       "getEndLocation",
                                       null);
      pd[i++] = new PropertyDescriptor("location",
                                       beanClass,
                                       "getLocation",
                                       null);
      pd[i++] = new PropertyDescriptor("quantity",
                                       beanClass,
                                       "getQuantity",
                                       null);
      pd[i++] = new PropertyDescriptor("startQuantity",
                                       beanClass,
                                       "getStartQuantity",
                                       null);
      pd[i++] = new PropertyDescriptor("endQuantity",
                                       beanClass,
                                       "getEndQuantity",
                                       null);
      pd[i++] = new PropertyDescriptor("rate",
                                       beanClass,
                                       "getRate",
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
