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

public class UIWorkflowImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[7];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.domain.mlm.ui.data.UIWorkflowImpl");
      pd[i++] = new PropertyDescriptor("parentTask",
                                       beanClass,
                                       "getParentTask",
                                       null);
      pd[i++] = new IndexedPropertyDescriptor("task",
                                              beanClass,
                                              "getTasks", null,
                                              "getTask", null);
      pd[i++] = new IndexedPropertyDescriptor("constraint",
                                              beanClass,
                                              "getConstraints", null,
                                              "getConstraint", null);
      pd[i++] = new PropertyDescriptor("constraintViolated",
                                       beanClass,
                                       "isConstraintViolated",
                                       null);
      pd[i++] = new IndexedPropertyDescriptor("violatedConstraint",
                                              beanClass,
                                              "getViolatedConstraints", null,
                                              "getViolatedConstraint", null);
      pd[i++] = new PropertyDescriptor("propagatingToSubtasks",
                                       beanClass,
                                       "isPropagatingToSubtasks",
                                       null);
      pd[i++] = new PropertyDescriptor("UUID",
                                       beanClass,
                                       "getUUID",
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
