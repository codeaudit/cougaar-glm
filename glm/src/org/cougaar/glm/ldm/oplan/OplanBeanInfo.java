/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.glm.ldm.oplan;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
   Override the default property descriptors.
   A property descriptor contains:
   attribute name, bean class, read method name, write method name
   All other beaninfo is defaulted.

 * @author  ALPINE <alpine-software@bbn.com>
 *

*/
public class OplanBeanInfo extends SimpleBeanInfo {

  public PropertyDescriptor[] getPropertyDescriptors() {
    PropertyDescriptor[] pd = new PropertyDescriptor[16];
    int i = 0;
    try {
      Class myClass = Class.forName("org.cougaar.glm.ldm.oplan.Oplan");
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
