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
  public void setDisplayDate(String param);
  public void setDate(Date param);
  public void setTimeStamp(long param);
  public void setTicketNumber(String param);
  public void setOperator(String param);
  public void setMachine(String param);
  public void setSerialNumber(String param);
  public void setSpace(String param);
  public void setSymptoms(String param);
  public void setRemarks(String param);
  public void setMissionCritical(boolean param);
  public void setStatus(String param);
  public void setBrokenPart(String param);
  public void setBrokenPartDesc(String param);
  public void setBrokenPartStatus(String param);
  public void setCasRep (boolean param);
  public void setNewCasRep (boolean param);
  public void setVersion (String param);
  public void setFrom(String param);
  public void setFromCluster(String param);
  public void setTo(String param);
  public void setInfo(String param);
  public void setClassification(String param);
  public void setCriticalityCode(String param);
  public void setBrokenMEI(Object param);
  public void setData(String param);
}
