/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.orgviewServer;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.domain.mlm.ui.orgviewServer.NmsPipePlugin;

public class PipeClusters extends Thread implements NmsPipePlugin {
InputStream is;
OutputStream os;
Object arg;
Vector clusters;
boolean go= true;

  public void execute(InputStream is, OutputStream os, Object arg) {
    this.os=os; this.is=is; this.arg=arg;
    start();
    }

  public void control(String command) {
        System.out.println("PipeClusters: "+command);
    if (command.equals("pollnow")) {
      this.interrupt();
      }
    else if (command.equals("terminate")) {
      go=false;
      this.interrupt();
      }

    }

  public void run() {
  String line;

    System.out.println("PipeClusters plugin:  "+arg);
    PrintWriter out = new PrintWriter(os, true);
    while (go) {
      clusters=new Vector();
      try {
//      plugin org.cougaar.domain.mlm.ui.orgviewServer.PipeClusters "http://haines:5555"

        URL url = new URL((String)arg + "/alpine/demo/ALPMAP.PSP?CLUSTERS");
        URLConnection uc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
  
        out.print("clusters [");
        while ((line = in.readLine()) != null) {
          String entry = parse(line, 1);
          //System.out.println("entry '"+entry+"'  for line: "+line);
              if (entry == null)  {
            System.out.println("PipeCluster clusters error: "+line);
            break;
            }
          out.print("'"+entry+"' ");
          clusters.addElement(entry);
          }
        out.println("]");

        Enumeration e = clusters.elements();
        while ( e.hasMoreElements() ) {
          String cluster = (String)e.nextElement();
          try {
  
            url = new URL((String)arg + "/$" + cluster + "/alpine/demo/ALPMAP.PSP?RELATIONS");
  /*
            uc = url.openConnection();
            in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
  */
            Socket sock = new Socket(url.getHost(), url.getPort());
            sock.setSoTimeout(20000);  // max 20 seconds
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter sout = 
                new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            sout.println("GET "+url.getFile()+" HTTP/1.0");
            sout.println();  // null line
            sout.flush();  
            line = in.readLine();
            line = in.readLine();
      
            out.print("relate '"+cluster+"' [");
            while ((line = in.readLine()) != null) {
              String entry = parse(line, 2);
              if (entry == null)  {
                System.out.println("PipeCluster relate error: "+line);
                break;
                }
              out.print(entry+"  ");
              }
            out.println("]");
  
            }
          catch (IOException e2) {
            System.err.println("PipeClustersRelate IO error:  " + e2);
            }
          }
        }
      catch (IOException e) {
        System.err.println("PipeClusters:  " + e);
        }
      try { Thread.sleep(120000);       }
      catch (Exception e) {System.err.println("PipeClusters Sleep Interrupted");}
      }
    out.close();
    }


/*   This worked great except when the LogPlanServer 
delivered some sort of error message.  The attempt here is to 
filter out any crud that may appear.  

New format for relations is:
relate Supporting 'MCCGlobalNode'  '3ID/2' 'MCCGlobatMode/6'
*/

  private String parse(String line, int type) {
    if (line.startsWith("<"))  return null;
    StringTokenizer st = new StringTokenizer(line);
    if (!st.hasMoreElements())  return null;
    String cmd = st.nextToken();
    if (cmd.equals("entry") && st.hasMoreElements())  
      return st.nextToken();  
    if (type==1 && st.hasMoreElements())  return null;
    if (cmd.equals("relate") && st.hasMoreElements())  
      return line.substring(7); 
    return null;
    }



// The main method is not used or needed when running as a plugin;
// it allows the plugin to run by itself for testing.
  public static void main(String[] args) {
    PipeClusters s = new PipeClusters();
    s.arg = args[0];
    s.is = System.in; s.os = System.out;
    s.start();
    }

  }
