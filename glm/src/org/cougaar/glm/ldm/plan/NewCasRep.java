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

package org.cougaar.glm.ldm.plan;

import java.util.Enumeration;
import java.util.*;
import java.util.Date;

public interface NewCasRep extends CasRep
{
  void setDisplayDate(String param);
  void setDate(Date param);
  void setTimeStamp(long param);
  void setTicketNumber(String param);
  void setOperator(String param);
  void setMachine(String param);
  void setSerialNumber(String param);
  void setSpace(String param);
  void setSymptoms(String param);
  void setRemarks(String param);
  void setMissionCritical(boolean param);
  void setStatus(String param);
  void setBrokenPart(String param);
  void setBrokenPartDesc(String param);
  void setBrokenPartStatus(String param);
  void setCasRep (boolean param);
  void setNewCasRep (boolean param);
  void setVersion (String param);
  void setFrom(String param);
  void setFromCluster(String param);
  void setTo(String param);
  void setInfo(String param);
  void setClassification(String param);
  void setCriticalityCode(String param);
  void setBrokenMEI(Object param);
  void setData(String param);
}
