/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.oplan;

import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Enumeration;

/**
   Override the default property descriptors.
   A property descriptor contains:
   attribute name, bean class, read method name, write method name
   All other beaninfo is defaulted.

 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: OrgRelationBeanInfo.java,v 1.1 2000-12-15 20:18:01 mthome Exp $

*/

public class OrgRelationBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[8];
    int i = 0;
    try {
      Class myClass = Class.forName("org.cougaar.domain.glm.oplan.OrgRelation");
      pd[i++] = new PropertyDescriptor("orgRelationId",
				       myClass,
				       "getOrgRelationId",
				       null);
      pd[i++] = new PropertyDescriptor("orgID",
				       myClass,
				       "getOrgID",
				       null);
      pd[i++] = new PropertyDescriptor("forcePackageId",
				       myClass,
				       "getForcePackageId",
				       null);
      pd[i++] = new PropertyDescriptor("oplanUID",
				       myClass,
				       "getOplanUID",
				       null);
      pd[i++] = new PropertyDescriptor("timeSpan",
				       myClass,
				       "getTimeSpan",
				       null);
      pd[i++] = new PropertyDescriptor("otherOrgId",
				       myClass,
				       "getOtherOrgId",
				       null);
      pd[i++] = new PropertyDescriptor("relationType",
				       myClass,
				       "getRelationType",
				       null);
      pd[i++] = new PropertyDescriptor("assignedRole",
				       myClass,
				       "getAssignedRole",
				       null);

      PropertyDescriptor[] additionalPDs = Introspector.getBeanInfo(myClass.getSuperclass()).getPropertyDescriptors();
      PropertyDescriptor[] finalPDs = new PropertyDescriptor[additionalPDs.length + pd.length];
      System.arraycopy(pd, 0, finalPDs, 0, pd.length);
      System.arraycopy(additionalPDs, 0, finalPDs, pd.length, additionalPDs.length);
      return finalPDs;
    } catch (Exception e) {
      System.out.print("OrgRelationBeanInfo ");
      e.printStackTrace();
    }
    return null;
  }

}
