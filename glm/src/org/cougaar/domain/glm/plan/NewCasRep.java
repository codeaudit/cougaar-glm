/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;

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
