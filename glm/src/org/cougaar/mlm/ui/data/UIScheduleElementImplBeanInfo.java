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

package org.cougaar.mlm.ui.data;

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
      Class beanClass = Class.forName("org.cougaar.mlm.ui.data.UIScheduleElementImpl");
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
