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
 * @version $Id: OplanBeanInfo.java,v 1.1 2000-12-15 20:18:00 mthome Exp $

*/
public class OplanBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[16];
    int i = 0;
    try {
      Class myClass = Class.forName("org.cougaar.domain.glm.oplan.Oplan");
      pd[i++] = new PropertyDescriptor("PODs",
				       myClass,
				       "getPODArray", null);
      pd[i++] = new PropertyDescriptor("DFSPs",
				       myClass,
				       "getDFSPArray", null);
      pd[i++] = new PropertyDescriptor("theaterID",
				       myClass,
				       "getTheaterID",
				       null);
      pd[i++] = new PropertyDescriptor("terrainType",
				       myClass,
				       "getTerrainType",
				       null);
      pd[i++] = new PropertyDescriptor("season",
				       myClass,
				       "getSeason",
				       null);
      pd[i++] = new PropertyDescriptor("enemyForceType",
				       myClass,
				       "getEnemyForceType",
				       null);
      pd[i++] = new PropertyDescriptor("HNSPOL",
				       myClass,
				       "getHNSPOL",
				       null);
      pd[i++] = new PropertyDescriptor("HNSPOLCapacity",
				       myClass,
				       "getHNSPOLCapacity",
				       null);
      pd[i++] = new PropertyDescriptor("HNSForWater",
				       myClass,
				       "getHNSForWater",
				       null);
      pd[i++] = new PropertyDescriptor("HNSWaterCapability",
				       myClass,
				       "getHNSWaterCapability",
				       null);
      pd[i++] = new PropertyDescriptor("XMLFileName",
				       myClass,
				       "getXMLFileName",
				       null);
      pd[i++] = new PropertyDescriptor("version",
				       myClass,
				       "getVersion",
				       null);
      pd[i++] = new PropertyDescriptor("oplanId",
				       myClass,
				       "getOplanId",
				       null);
      pd[i++] = new PropertyDescriptor("operationName",
				       myClass,
				       "getOperationName",
				       null);
      pd[i++] = new PropertyDescriptor("priority",
				       myClass,
				       "getPriority",
				       null);
      pd[i++] = new PropertyDescriptor("Cday",
				       myClass,
				       "getCday",
				       null);

      PropertyDescriptor[] additionalPDs = Introspector.getBeanInfo(myClass.getSuperclass()).getPropertyDescriptors();
      PropertyDescriptor[] finalPDs = new PropertyDescriptor[additionalPDs.length + pd.length];
      System.arraycopy(pd, 0, finalPDs, 0, pd.length);
      System.arraycopy(additionalPDs, 0, finalPDs, pd.length, additionalPDs.length);
      return finalPDs;
    } catch (Exception e) {
      System.out.print("OplanBeanInfo ");
      e.printStackTrace();
    }
    return null;
  }

}
