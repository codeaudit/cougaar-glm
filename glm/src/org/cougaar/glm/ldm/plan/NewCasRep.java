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

package org.cougaar.glm.ldm.plan;

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
