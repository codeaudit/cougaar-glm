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

public class UITaskImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[26];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.domain.mlm.ui.data.UITaskImpl");
      pd[i++] = new PropertyDescriptor("source",
                                       beanClass,
                                       "getSource",
                                       null);
      pd[i++] = new PropertyDescriptor("destination",
                                       beanClass,
                                       "getDestination",
                                       null);
      pd[i++] = new PropertyDescriptor("verb",
                                       beanClass,
                                       "getVerb",
                                       null);
      pd[i++] = new PropertyDescriptor("directObject",
                                       beanClass,
                                       "getDirectObject",
                                       null);
      pd[i++] = new PropertyDescriptor("planName",
                                       beanClass,
                                       "getPlanName",
                                       null);
      pd[i++] = new PropertyDescriptor("planElement",
                                       beanClass,
                                       "getPlanElement",
                                       null);
      pd[i++] = new PropertyDescriptor("workflow",
                                       beanClass,
                                       "getWorkflow",
                                       null);
      pd[i++] = new PropertyDescriptor("parentTask",
                                       beanClass,
                                       "getParentTask",
                                       null);
      pd[i++] = new PropertyDescriptor("priority",
                                       beanClass,
                                       "getPriority",
                                       null);
      pd[i++] = new PropertyDescriptor("commitmentDate",
                                       beanClass,
                                       "getCommitmentDate",
                                       null);
      pd[i++] = new PropertyDescriptor("toLocation",
                                       beanClass,
                                       "getToLocation",
                                       null);
      pd[i++] = new PropertyDescriptor("fromLocation",
                                       beanClass,
                                       "getFromLocation",
                                       null);
      pd[i++] = new PropertyDescriptor("forOrganization",
                                       beanClass,
                                       "getForOrganization",
                                       null);
      pd[i++] = new PropertyDescriptor("itinerary",
                                       beanClass,
                                       "getItinerary",
                                       null);
      pd[i++] = new PropertyDescriptor("reportingTo",
                                       beanClass,
                                       "getReportingTo",
                                       null);
      pd[i++] = new PropertyDescriptor("forClusterId",
                                       beanClass,
                                       "getForClusterId",
                                       null);
      pd[i++] = new PropertyDescriptor("ofRequirementsType",
                                       beanClass,
                                       "getOfRequirementsType",
                                       null);
      pd[i++] = new PropertyDescriptor("forWhom",
                                       beanClass,
                                       "getForWhom",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredStartTime",
                                       beanClass,
                                       "getPreferredStartTime",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredEndTime",
                                       beanClass,
                                       "getPreferredEndTime",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredQuantity",
                                       beanClass,
                                       "getPreferredQuantity",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredInterval",
                                       beanClass,
                                       "getPreferredInterval",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredTotalShipments",
                                       beanClass,
                                       "getPreferredTotalShipments",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredPOD",
                                       beanClass,
                                       "getPreferredPOD",
                                       null);
      pd[i++] = new PropertyDescriptor("preferredPODDate",
                                       beanClass,
                                       "getPreferredPODDate",
                                       null);
      pd[i++] = new IndexedPropertyDescriptor("preferredTypedQuantities",
                                              beanClass,
                                              "getPreferredTypedQuantities", null,
                                              "getPreferredTypedQuantity", null);
      // note, do no introspection on sub-classes;
      // these are the only values returned
      return pd;
    } catch (Exception e) {
      System.out.println("Exception:" + e);
    }
    return null;
  }

}
