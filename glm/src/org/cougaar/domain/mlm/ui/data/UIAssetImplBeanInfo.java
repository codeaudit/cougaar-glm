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

public class UIAssetImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[10];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.domain.mlm.ui.data.UIAssetImpl");
      pd[i++] = new IndexedPropertyDescriptor("allocation",
                                              beanClass,
                                              "getAllocations", null,
                                              "getAllocation", null);
      pd[i++] = new PropertyDescriptor("availableSchedule",
                                       beanClass,
                                       "getAvailableSchedule",
                                       null);
      pd[i++] = new PropertyDescriptor("typeIdentification",
                                       beanClass,
                                       "getTypeIdentification",
                                       null);
      pd[i++] = new PropertyDescriptor("typeIdentificationPropertyNomenclature",
                                       beanClass,
                                       "getTypeIdentificationPGNomenclature",
                                       null);
      pd[i++] = new PropertyDescriptor("alternateTypeIdentification",
                                       beanClass,
                                       "getAlternateTypeIdentification",
                                       null);
      pd[i++] = new PropertyDescriptor("itemIdentification",
                                       beanClass,
                                       "getItemIdentification",
                                       null);
      pd[i++] = new PropertyDescriptor("itemIdentificationPropertyNomenclature",
                                       beanClass,
                                       "getItemIdentificationPGNomenclature",
                                       null);
      //      pd[i++] = new IndexedPropertyDescriptor("property",
      //                                              beanClass,
      //                                              "getProperties", null,
      //                                              "getProperty", null);
      pd[i++] = new IndexedPropertyDescriptor("property",
                                              beanClass,
                                              "getPropertyNameValues", null,
                                              "getPropertyNameValue", null);
      pd[i++] = new PropertyDescriptor("asset",
                                       beanClass,
                                       "getAsset",
                                       null);
      pd[i++] = new PropertyDescriptor("quantity",
                                       beanClass,
                                       "getQuantity",
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
