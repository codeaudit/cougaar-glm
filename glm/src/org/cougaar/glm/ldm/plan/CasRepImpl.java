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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.Hashtable;

import org.cougaar.core.util.OwnedUniqueObject;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.planning.ldm.plan.Transferable;

public class CasRepImpl extends OwnedUniqueObject
  implements CasRep, NewCasRep, Transferable, UniqueObject
{
  private UID uid;
  private String casRepNumber;
  private String displayDate;
  private long timeStamp;
  private String ticketNumber;
  private String operator;
  private String machine;
  private String serialNumber;
  private String space;
  private String symptoms;
  private String remarks;
  private boolean missionCritical;
  private String status;
  private String brokenPart;
  private String brokenPartDesc;
  private String brokenPartStatus;
  private boolean casRep;
  private boolean newCasRep;
  private String version = "";
  private String from;
  private String fromCluster;
  private String to;
  private String info;
  private String classification;
  private String criticalityCode = "C2";
  private Object brokenMEI;
  private String data;
  private Hashtable misc = new Hashtable();
	
  public CasRepImpl ()
  {
    misc.put("PROBLEM_DESC","");
    misc.put("MISSION_IMPACT","");
  }

  //SETTERS
  public void setUID(UID u) 
  {
    uid = u;
    String param = u.toString();
    int i = param.indexOf("/");
    String paddedNumber = param.substring(i + 1);
    String year = new Date().toString();
    year = year.substring(year.length() - 2);
    while (paddedNumber.length() < 3) {
      paddedNumber = "0" + paddedNumber;
    }
    casRepNumber = param.substring(0, i) + "/" + year + paddedNumber;
  }

  public void setDisplayDate(String param)
  {
    displayDate = param;
  }

  public void setDate(Date param)
  {
    String date = param.toString();
    String hours = date.substring(11,13);
    String minutes = date.substring(14,16);
    String dom = date.substring(8,10);
    String month = date.substring(4,7).toUpperCase();
    String year = date.substring(date.length() - 2);
    displayDate = dom + hours + minutes + "Z" + month + year;
  }

  public String formatDate(Date param)
  {
    String date = param.toString();
    String hours = date.substring(11,13);
    String minutes = date.substring(14,16);
    String dom = date.substring(8,10);
    String month = date.substring(4,7).toUpperCase();
    String year = date.substring(date.length() - 2);
    return dom + hours + minutes + "Z" + month + year;
  }

  public String formatShortDate(Date param)
  {
    String date = param.toString();
    String hours = date.substring(11,13);
    String minutes = date.substring(14,16);
    String dom = date.substring(8,10);
    String month = date.substring(4,7).toUpperCase();
    String year = date.substring(date.length() - 2);
    return dom + month + year;
  }

  public void setTimeStamp(long param)
  {
    timeStamp = param;
  }

  public void setTicketNumber(String param)
  {
    ticketNumber = param;
  }

  public void setOperator(String param)
  {
    operator = param;
  }

  public void setMachine(String param)
  {
    machine = param;
  }

  public void setSerialNumber(String param)
  {
    serialNumber = param;
  }

  public void setSpace(String param)
  {
    space = param;
  }

  public void setSymptoms(String param)
  {
    symptoms = param;
  }

  public void setRemarks(String param)
  {
    remarks = param;
  }

  public void setMissionCritical(boolean param)
  {
    missionCritical = param;
  }

  public void setStatus(String param)
  {
    status = param;
  }

  public void setBrokenPart(String param)
  {
    brokenPart = param;
  }

  public void setBrokenPartDesc(String param)
  {
    brokenPartDesc = param;
  }

  public void setBrokenPartStatus(String param)
  {
    brokenPartStatus = param;
  }

  public void setCasRep (boolean param)
  {
    casRep = param;
  }

  public void setNewCasRep (boolean param)
  {
    newCasRep = param;
  }

  public void setVersion (String param)
  {
    version = param;
  }

  public void setFrom(String param)
  {
    from = param;
  }

  public void setFromCluster(String param)
  {
    fromCluster = param;
  }

  public void setTo(String param)
  {
    to = param;
  }

  public void setInfo(String param)
  {
    info = param;
  }

  public void setClassification(String param)
  {
    classification = param;
  }

  public void setCriticalityCode(String param)
  {
    criticalityCode = param;
  }

  public void setBrokenMEI(Object param)
  {
    brokenMEI = param;
  }

  public void setData(String param)
  {
    data = param;
  }


  //GETTERS
  public UID getUID() 
  {
    return uid;
  }

  public String getDisplayDate()
  {
    return displayDate;
  }

  public long getTimeStamp()
  {
    return timeStamp;
  }

  public String getTicketNumber()
  {
    return ticketNumber;
  }

  public String getOperator()
  {
    return operator;
  }

  public String getMachine()
  {
    return machine;
  }

  public String getSerialNumber()
  {
    return serialNumber;
  }

  public String getSpace()
  {
    return space;
  }

  public String getSymptoms()
  {
    return symptoms;
  }

  public String getRemarks()
  {
    return remarks;
  }

  public boolean isMissionCritical()
  {
    return missionCritical;
  }

  public String getStatus()
  {
    return status;
  }

  public String getBrokenPart()
  {
    return brokenPart;
  }

  public String getBrokenPartDesc()
  {
    return brokenPartDesc;
  }

  public String getBrokenPartStatus()
  {
    return brokenPartStatus;
  }

  public boolean isCasRep()
  {
    return casRep;
  }

  public boolean isNewCasRep()
  {
    return newCasRep;
  }

  public String getVersion()
  {
    return version;
  }

  public String getNextVersion()
  {
    if (!isCasRep()) {
      return "";
    }
    else if (version == null || version.equals("")) {
      return "A";
    }
    char c = (char)(version.charAt(0) + 1);
		
    return new Character(c).toString();
  }

  public String getFrom()
  {
    return from;
  }

  public String getFromCluster()
  {
    return fromCluster;
  }

  public String getTo()
  {
    return to;
  }

  public String getInfo()
  {
    return info;
  }

  public String getClassification()
  {
    return classification;
  }

  public String getCriticalityCode()
  {
    return criticalityCode;
  }

  public Object getBrokenMEI()
  {
    return brokenMEI;
  }

  public String getData()
  {
    return data;
  }
   
  public String getCode()
  {
    if (status.equals(TICKET_COMPLETED) || status.equals(REPAIR_COMPLETED)) {
      return "G";
    }
    else if (isCasRep()) {
      return "R";
    }
    else {
      if (isMissionCritical()) {
        return "Y";
      }
      else {
        return "W";
      }
    }
  }

  public Hashtable getMisc()
  {
    return misc;
  }

  public String getCasRepNumber()
  {
    return (casRepNumber + version).toUpperCase();
  }

  public String getOnShipCasRepNumber()
  {
    int i = casRepNumber.indexOf("/");
    return casRepNumber.substring(i + 1) + version;
  }

  public String getNextOnShipCasRepNumber()
  {
    System.out.println(getUID());
    int i = casRepNumber.indexOf("/");
    return casRepNumber.substring(i + 1) + getNextVersion();
  }

  public boolean same(Transferable other) {

    return false;
  }//same

  public Object clone() {
    CasRepImpl casrep = new CasRepImpl();
    casrep.setUID(uid);
    casrep.setDisplayDate(displayDate);
    casrep.setTimeStamp(timeStamp);
    casrep.setTicketNumber (ticketNumber);
    casrep.setOperator(operator);
    casrep.setMachine(machine);
    casrep.setSerialNumber(serialNumber);
    casrep.setSpace(space);
    casrep.setSymptoms(symptoms);
    casrep.setRemarks(remarks);
    casrep.setMissionCritical(missionCritical);
    casrep.setStatus(status);
    casrep.setBrokenPart(brokenPart);
    casrep.setCasRep(casRep);
    casrep.setVersion(version);
    casrep.setFrom(from);
    casrep.setFromCluster(fromCluster);
    casrep.setTo(to);
    casrep.setInfo(info);
    casrep.setClassification(classification);
    casrep.setCriticalityCode (criticalityCode);
    casrep.setBrokenMEI (brokenMEI);
    casrep.setData(data);
    return casrep;
  }// clone
  
  public void setAll(Transferable other) {
  }// setAll

  //dummy PropertyChangeSupport for the Jess Interpreter.
  protected transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    pcs.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl)   {
    pcs.removePropertyChangeListener(pcl);
  }

}//CasRepImpl
