/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
/*
 * Copyright 1999 BBN Systems and Technologies, A Division of BBN Corporation
 * 10 Moulton Street, Cambridge, MA 02138 (617) 873-3000
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

public class UIAssetTransferImplBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[4];
    int i = 0;
    try {
      Class beanClass = Class.forName("org.cougaar.mlm.ui.data.UIAssetTransferImpl");
      pd[i++] = new PropertyDescriptor("UIAsset",
                                       beanClass,
                                       "getUIAsset",
                                       null);
      pd[i++] = new PropertyDescriptor("assignee",
                                       beanClass,
                                       "getAssignee",
                                       null);
      pd[i++] = new PropertyDescriptor("assignor",
                                       beanClass,
                                       "getAssignor",
                                       null);
      pd[i++] = new PropertyDescriptor("UISchedule",
                                       beanClass,
                                       "getUISchedule",
                                       null);

      PropertyDescriptor[] additionalPDs = Introspector.getBeanInfo(beanClass.getSuperclass()).getPropertyDescriptors();
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
