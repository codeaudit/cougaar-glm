/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.ldm.plan;

import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Enumeration;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.glm.ldm.plan.LaborScheduleImpl;

/**
   Override the default property descriptors.
   A property descriptor contains:
   attribute name, bean class, read method name, write method name
   All other beaninfo is defaulted.
   This defines appropriate properties from the LaborSchedule INTERFACE,
   but is actually used to introspect on the LaborSchedule IMPLEMENTATION.
*/

public class LaborScheduleImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[7];
    try {
      Class LaborScheduleClass = Class.forName("org.cougaar.domain.glm.ldm.plan.LaborScheduleImpl");
      int i = 0;
      pd[i++] = new PropertyDescriptor("scheduleType",
             LaborScheduleClass,
             "getScheduleType",
             null);
      pd[i++] = new PropertyDescriptor("ScheduleElementType",
            LaborScheduleClass,
            "getScheduleElementType",
            null);
      pd[i++] = new IndexedPropertyDescriptor("scheduleElements",
                LaborScheduleClass,
                "getScheduleElements", null,
                "getScheduleElement", null);
      pd[i++] = new PropertyDescriptor("QuantitySchedule",
                LaborScheduleClass,
                "getQuantitySchedule",
                null);
      pd[i++] = new PropertyDescriptor("RateSchedule",
                LaborScheduleClass,
                "getRateSchedule",
                null);
      pd[i++] = new PropertyDescriptor("startDate",
               LaborScheduleClass,
               "getStartDate",
               null);
      pd[i++] = new PropertyDescriptor("endDate",
               LaborScheduleClass,
               "getEndDate",
               null);
      PropertyDescriptor[] additionalPDs = Introspector.getBeanInfo(LaborScheduleClass.getSuperclass()).getPropertyDescriptors();
      PropertyDescriptor[] finalPDs = new PropertyDescriptor[additionalPDs.length + pd.length];
      System.arraycopy(pd, 0, finalPDs, 0, pd.length);
      System.arraycopy(additionalPDs, 0, finalPDs, pd.length, additionalPDs.length);
      return finalPDs;
    } catch (Exception e) {
      System.out.println("Exception:" + e);
    }
    return null;
  }

}
