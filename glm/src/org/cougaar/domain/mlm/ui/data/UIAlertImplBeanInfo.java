/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
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

public class UIAlertImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[9];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.domain.mlm.ui.data.UIAlertImpl");
      pd[i++] = new PropertyDescriptor("alertText",
                                       beanClass,
                                       "getAlertText",
                                       null);
      pd[i++] = new IndexedPropertyDescriptor("alertParameters",
                                              beanClass,
                                              "getAlertParameters", null,
                                              "getAlertParameter", null);
      pd[i++] = new IndexedPropertyDescriptor("alertDescriptions",
                                              beanClass,
                                              "getAlertDescriptions", null,
                                              "getAlertDescription", null);
      pd[i++] = new IndexedPropertyDescriptor("alertResponses",
                                              beanClass,
                                              "getAlertResponses", null,
                                              "getAlertResponse", null);
      pd[i++] = new PropertyDescriptor("acknowledged",
                                       beanClass,
                                       "isAcknowledged",
                                       null);
      pd[i++] = new PropertyDescriptor("severity",
                                       beanClass,
                                       "getSeverity",
                                       null);
      pd[i++] = new PropertyDescriptor("type",
                                       beanClass,
                                       "getType",
                                       null);
      pd[i++] = new PropertyDescriptor("operatorResponseRequired",
                                       beanClass,
                                       "isOperatorResponseRequired",
                                       null);
      pd[i++] = new PropertyDescriptor("operatorResponse",
                                       beanClass,
                                       "getOperatorResponse",
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



