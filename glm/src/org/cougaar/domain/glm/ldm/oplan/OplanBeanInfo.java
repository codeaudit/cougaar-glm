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

package org.cougaar.domain.glm.ldm.oplan;

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
 * @version $Id: OplanBeanInfo.java,v 1.3 2001-08-22 20:27:23 mthome Exp $

*/
public class OplanBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[16];
    int i = 0;
    try {
      Class myClass = Class.forName("org.cougaar.domain.glm.ldm.oplan.Oplan");
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
