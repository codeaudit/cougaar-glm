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
package org.cougaar.mlm.ui.psp.naming;

import org.cougaar.core.service.NamingService;


import java.io.*;
import java.util.*;




//###########################################################################
//###########################################################################
//###########################################################################
//
// Session object - manages state at LogPlanServer with respect to
// "session transcript".   Sessions are on a per Agent basis.
// Meaning that there is no centralized "global session" represetnation (ahem
// cookies next time?) across a society of Agents.
//
public class YPDemoSession
{
     public final String SESSION_TRANSCRIPT_HEADERS_COLOR = "009933";

     // transcript = formatted HTML text
     private StringBuffer transcript = new StringBuffer();
     private QueryObject qobject = null;
     private StringBuffer buffer = new StringBuffer();

     public QueryObject getQueryObject(){ return qobject; }
     public void setQueryObject(QueryObject qo) { qobject = qo; }


     //
     // 'Add' = 'Append' String entry to session transcript
     public void add(String entry){
          buffer.append(entry);
     }
     //
     // 'AddHeader' = 'Format' + 'Append' String entry to session transcript
     public void addHeader(String entry){
          add(createHeaderToSessionTranscript(entry) );
     }

     public void doPrepend(){
          transcript.insert(0,buffer.toString());
          buffer = new StringBuffer();
     }
     public void doAppend() {
          transcript.append(buffer.toString());
          buffer = new StringBuffer();
     }

     public String getTranscript(){
          return transcript.toString();
     }

     public int getTranscriptLength() {
          return transcript.length();
     }

   private String createHeaderToSessionTranscript(String header)
   {
       String ret = new String("<TABLE WIDTH=\"100%\" ><TR><TD  BGCOLOR=" +  SESSION_TRANSCRIPT_HEADERS_COLOR
              + " COLOR=RED>"
              + "<FONT COLOR=white>"
              + header + "</FONT></TD></TR></TABLE>");
       return ret;
   }

}
