/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.orgviewServer.NmsPipePlugin;

public class PipeTasks extends Thread implements NmsPipePlugin {
InputStream is;
OutputStream os;
PrintWriter out;
Object arg;
Vector clusters;
boolean go= true;
int state=0;
String cluster=null;
static int count=0;
int instance;


  public void execute(InputStream is, OutputStream os, Object arg) {
    this.os=os; this.is=is; this.arg=arg;
    instance=++count;
    start();
    }

  public void control(String command) {
    if (command.equals("pollnow")) {
      this.interrupt();
      }
    else if (command.equals("terminate")) {
      go=false;
      this.interrupt();
      }
    else if (command.equals("state")) {
      System.out.println(instance+" PipeTasks State: "+state+ "  "+cluster);
      }
//    else System.out.println("PipeTasks command: "+command);

    }

  public void run() {
  String line;

    System.out.println("PipeTasks plugin:  "+arg);
    out = new PrintWriter(os, true);
    while (go) {
      clusters=new Vector();
      try {
//      plugin org.cougaar.domain.mlm.ui.orgviewServer.PipeTasks "http://haines:5555"

        state=1;
        URL url = new URL((String)arg + "/alpine/demo/MAP.PSP?CLUSTERS");
        URLConnection uc = url.openConnection();
        state=2;
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
  
        state=3;
        while ((line = in.readLine()) != null) {
          String entry = parse(line, 1);
          //System.out.println("entry '"+entry+"'  for line: "+line);
            if (entry == null) break;
          clusters.addElement(entry);
          }

        state=11;
        Enumeration e = clusters.elements();
        while ( e.hasMoreElements() ) {

          state=12;
          cluster = (String)e.nextElement();
          url = new URL((String)arg + "/$" + cluster + "/alpine/demo/MAP.PSP?TASKS");
          state=13;
          uc = url.openConnection();
          state=14;
          in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//          out.println("trying '"+cluster+"' ");
    
          state=15;
          line= in.readLine();
          String entry = parse(line, 2);
              if (entry == null)  break;
          state=16;
          nodecolor(cluster, Integer.parseInt(entry));
          }
        }
      catch (IOException e) {
        System.err.println("PipeTasks:  " + e);
        }
      try { Thread.sleep(10000);        }
      catch (Exception e) {System.err.println("PipeTasks Sleep Interrupted");}
      }
    out.close();
    state=99;
    }

  private void nodecolor(String cluster, int num) {
    out.println("objectl '"+cluster+"' "+mapcolor(num));
    }

  private int mapcolor(int i) {
    if (i<1) return 2;  // red
    else if (i<5) return 1;  // green
    else if (i<25) return 3;  // 
    else if (i<50) return 4;
    else return 5;
    }



/*  The lines returned from PSP_Map clusters are single word 
entries.  This worked great except when the LogPlanServer 
delivered some sort of error message.  The attempt here is to 
filter out any crud that may appear.  

The relate directive returns lines of the form
Subordinate 'GlobalAir'
I don't check these as carefully because we won't get to relations
if the clusters don't pass muster.
*/

  private String parse(String line, int type) {
    if (line==null)  return null;
    if (line.startsWith("<"))  return null;
    StringTokenizer st = new StringTokenizer(line);
    if (!st.hasMoreElements())  return null;
    String cmd = st.nextToken();
    if (cmd.equals("entry") && st.hasMoreElements())  
      return st.nextToken();  
    if (cmd.equals("numtasks") && st.hasMoreElements())  
      return st.nextToken();  
    if (type==1 && st.hasMoreElements())  return null;
    return line;
    }



// The main method is not used or needed when running as a plugin;
// it allows the plugin to run by itself for testing.
  public static void main(String[] args) {
    PipeTasks s = new PipeTasks();
    s.arg = args[0];
    s.is = System.in; s.os = System.out;
    s.start();
    }

  }
