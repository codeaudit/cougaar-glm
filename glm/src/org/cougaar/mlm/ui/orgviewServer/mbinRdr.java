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
 
package org.cougaar.mlm.ui.orgviewServer;

import java.io.*;
import java.net.*;
import java.util.*;

import org.cougaar.mlm.ui.orgviewServer.*;

/**
 * .<pre>
 * mbin reader
 *
 * This class reads and processes a mapbox input stream.
 *
 * Instantiations:       type    
 * 
 *   startup nms.init    Readfile  in NetMapServer
 * 
 *   socket call         Socket    in Listener
 * 
 *   NmsPipePlugin       Pipe      in mbinRdr - plugin
 *   
 *  readfile command    Readfile  in mbinRdr - readfile
 *  
 * Change History
 * Initial version September 1999
 *
 * As of 10/15/99
 * Commands: quit, clusters, lines, nodeview, pipecontrol, readfile,
 *   jmap, jmsh, plugin, plugout, relate, shc, object
 * Plugin types:  NmsPipePlugin plus two experimental versions
 * 
 * 10/19/99:  initial HTTP support
 * 10/20/99:  add NmsWebPlugin;  new commands load, excmd
 * 10/21/99:  mbin command;  serve files via HTTP (support JMap)
 * 10/26/99:  add objectl command
 * 11/10/99:  http "/" -> "/index.html"
 * 11/19/99:  support title (label)
 *   lines:  show relationship in status area
 * 11/22/99:  getRel:  work with extended relationship vector
 *   (has self and target UIDs following relation and target)
 * 11/23/99:  jmap:  delete call to outlines (initial db lines) except main.vw
 *   add relations command
 * 11/30/99:  outlines(relation):  reverse line for Supporting
 *   add relations to http commands
 *
 * </pre>
 */

public class mbinRdr extends Thread {
static Vector Rdrs = new Vector();  // all Rdrs
static int rdrcnt = 0;  // rdr count

NetMapServer nms;
InputStream is;
OutputStream os;
String type;
String id;
String line;

String view=null;
Vector nobjlist=null;
int rdrid=0;
PrintWriter out=null;
Object instan;  // PipePlugin class instantiation

static Hashtable excmds = new Hashtable();


  public mbinRdr(NetMapServer nms, InputStream is, OutputStream os, 
      String type, String id) {
    super("mbinRdr");
    this.nms=nms; this.type = type; this.id = id; this.is = is; this.os = os;  
    Rdrs.addElement(this);
    rdrid = ++rdrcnt;
    }

  public void run() {
    ctTokenizer t;
    System.out.println((new Date()) +"  mbinRdr "+rdrid+
      " started  "+type+" "+id);
    try { 
      BufferedReader in = new BufferedReader(new InputStreamReader(is));
      if (os != null) out = new PrintWriter(os, true);

      while ((line = in.readLine())  != null) {
        System.out.println(rdrid+">>"+line);
        t= new ctTokenizer(line);
        String a=null;
        if (t.v.size() > 0 && t.v.firstElement() instanceof String) 
          a = (String)t.v.firstElement();   
      
        if (t.v.size()>1 && a.equals("GET"))  {  // HTTP
          do { a= in.readLine(); }
          while (a != null && a.length()>1);
          out.println("HTTP/1.0 200 Ok");
          out.println();
          http(out, line);
          break;
          }
        else if (t.v.size() == 1)  {
          if ( a.equals("quit") )  break;
          if ( a.equals("relations") )  relations(out);
          }
        else if (t.v.size() == 2)  {
          if ( a.equals("clusters") )  
            clusters(out, (Vector)t.v.elementAt(1));
          else if (a.equals("lines") ) 
            lines(out, (String)t.v.elementAt(1));
          else if (a.equals("mbin") ) 
            mbin(out, (String)t.v.elementAt(1));
          else if ( a.equals("nodeview") )  
            nodeview(out, (String)t.v.elementAt(1));
          else if ( a.equals("pipecontrol") )  
            pipecontrol(out, (String)t.v.elementAt(1));
          else if (a.equals("readfile") ) 
            readfile(out, (String)t.v.elementAt(1));
          }
        else if (t.v.size() > 2)  {
          if ( a.equals("jmap") )  jmap(out, (String)t.v.elementAt(1));
          else if (a.equals("excmd") ) 
             excmd(out, (String)t.v.elementAt(1), (String)t.v.elementAt(2));
          else if (a.equals("load") ) 
             load(out, (String)t.v.elementAt(1), (String)t.v.elementAt(2), (String)t.v.elementAt(3));
          else if (a.equals("jmsh") ) 
             jmsh(out, (String)t.v.elementAt(1), (String)t.v.elementAt(3));
          else if (a.equals("plugin") ) 
             plugin(out, (String)t.v.elementAt(1), t.v.elementAt(2));
          else if (a.equals("plugout") ) 
             plugout(out, (String)t.v.elementAt(1), t.v.elementAt(2));
          else if (a.equals("relate") ) 
             relate(out, (String)t.v.elementAt(1), (Vector)t.v.elementAt(2));
          else if ( a.equals("shc") )  {
            BufferedReader in2 = new BufferedReader(
              new FileReader(new File("alpmap", "jmap.views")) );
      
            while ((line=in2.readLine()) != null) {
              out.println(line);
              }
            break;
            }
          else if (a.equals("object") ) 
             mbobject(out, (String)t.v.elementAt(1), (String)t.v.elementAt(2));
          else if (a.equals("objectl") ) 
             mbobjectl(out, (String)t.v.elementAt(1), (String)t.v.elementAt(2));
          }  //vsize>2
        else {}
        }  //while
      } //try
    catch (Exception e) {System.out.println("mbinRdr:  "+e);} 

    try { 
      is.close();   
//      if (os != null) os.close();
      if (os != System.out) os.close();
      } //try
    catch (Exception e) {} 

    if (nobjlist != null) {
      Enumeration e = nobjlist.elements();  // remove our nobj's from Node.nobjlist
        while (e.hasMoreElements()) {
          NOBJ nobj = (NOBJ)e.nextElement();
          nobj.Node.nobjlist.removeElement(nobj);
          }
      }
    Rdrs.removeElement(this);
    System.out.println((new Date()) +"  mbinRdr "+rdrid+" finished  ");
    }


  public void  http(PrintWriter out, String line) {
    StringTokenizer st = new StringTokenizer(line);
    st.nextToken();
    String name = st.nextToken();
    String parm = "";
    String arg = "";
//    out.println(name);

    int i = name.indexOf('?');  // split out parameters if any
    if (i > -1) {
      parm = name.substring(i+1);  // strip out first '?'
      name = name.substring(0, i);
      }
    if (name.equals("/")) name="/index.html";
    if (!name.startsWith("/cmd/")) {
      // send requested file to user
      try {
        BufferedInputStream in2 = new BufferedInputStream(
          new FileInputStream(name.substring(1)) );
        BufferedOutputStream os2 = new BufferedOutputStream(os);
        while ((i = in2.read()) > -1)  os2.write(i);
        in2.close(); os2.close();
        }
      catch (Exception e) {
        System.out.println("http load error:  "+e);
        out.println("http load error:  "+e);
        } 
      return;
      }

    //  We got a local command
    name=name.substring(5);
    i = name.lastIndexOf('/');  
    if (i > -1) {
      arg = name.substring(i+1);    //  org.cougaar.mlm.ui.orgviewServer.PipeClusters
      name = name.substring(0, i);    //  plugout
      }
System.out.println("http: "+name + "  "+arg + "  "+parm);

    if (name.equals("plugout")) 
        plugout(out, arg, parm);
    else if (name.equals("excmd")) 
        excmd(out, arg, parm);
    else if (name.equals("plugin")) 
        plugin(out, arg, parm);
    else if (name.equals("relations")) 
        relations(out);
    else out.println("Command not found: "+name);
    }




//   ---------- Following are command routines - alphabetical   ----------------------



  public void clusters(PrintWriter out, Vector v) {
    //  Set attr green for active clusters specified here
    //  called from PipeClusters plugin
    nms.Clusters = v;
    Enumeration e = v.elements();
    while (e.hasMoreElements()) {
      String label = (String)e.nextElement();
      NODE Node = (NODE)nms.NodeLabelhash.get(label);
      if (Node==null) 
        System.out.println("clusters  Node not found: "+label);
      else updateAttr(Node, "1");
      }
    }


  public void excmd(PrintWriter out, String resourcename, String arg) {
    Object ob = excmds.get(resourcename);  
    if (ob == null) {
      out.println("excmd not found: "+resourcename);
      return;
      }
    ((NmsWebPlugin)ob).exCommand(out, arg);
    }



  public void jmap(PrintWriter out, String view) {
    //  Bring up a remote Jmap window
    ctTokenizer t;

    this.view=view;
    nobjlist = new Vector();
    try {
      synchronized (nms.Nodehash) {
            nobjlist = new Vector();
        out.println("winid "+ rdrid);
        out.println("lobjlist "+nms.Lines.size());
        out.println("nobjlist "+nms.Nodehash.size());
        int i=1;
        BufferedReader in = new BufferedReader(
            new FileReader(new File("alpmap", view)) );
  
        while ((line=in.readLine()) != null) {  // read vw file
            t= new ctTokenizer(line);
            if (t.v.size() == 1  && t.v.firstElement() instanceof Vector) {
              Vector a = (Vector)t.v.firstElement();
              if (a.size() == 5)  {     // it's a nobj entry
                        String name = (String)a.elementAt(0);
                        NODE Node = (NODE)nms.Nodehash.get(name);
              if (Node == null)  {
                System.out.println("Node not found: "+name);
                continue;
                 }
                        String xx = (String)a.elementAt(1);
                        String yy = (String)a.elementAt(2);
                        String menu = nms.getTypeMenu(Node.type);
                        //if (Node.type.equals("ALPCLUSTER") && !Node.attr.equals("1")) menu=" 0 ";
              out.println("nobj "+i+" "+
                          xx+" "+yy+ nms.getTypeShape(Node.type)+
                          Node.attr +menu+"  '" + Node.label +"'");
                        NOBJ nobj = new NOBJ();
                        nobj.Node = Node;
                        nobj.Rdr = this;
                        nobj.nobjnum = i;
                        nobjlist.addElement(nobj);
                        Node.nobjlist.addElement(nobj);
                        i++;
                        }
              else if (a.size() == 3)  {  // it's a title node
                        String name = (String)a.elementAt(0);
                        String xx = (String)a.elementAt(1);
                        String yy = (String)a.elementAt(2);
              out.println("nobj "+i+" "+
                          xx+" "+yy+ "  20 0 0  1 0  '" + name +"'");
                        i++;
                }
              else if (a.size() > 4)  {
                        String name = (String)a.elementAt(0);
                        if (name.equals("Height")) 
                  out.println("size "+a.elementAt(3) +" "+a.elementAt(1));
                }
              }  //if (t.v.size() == 1  && 
            }  //while
          if (view.equals("main.vw"))  outLines();
        }  //synchronized

      out.println("refresh");
      out.println("update");

      BufferedReader in = new BufferedReader(
        new FileReader(new File("alpmap", "alpmap.mbx")) );

      while ((line=in.readLine()) != null) {
        out.println(line);
        }

      }  //try
    catch (Exception e) {System.out.println("jmap:  "+e);} 
    }



  public void jmsh(PrintWriter out, String a, String cmd) {
  // This is a crude way to support menu commands
    if (cmd.startsWith("mcmd Relate ")) {
      out.println("lobjlist "+nms.Nodehash.size());
      outLines(cmd.substring(12));
      out.println("refresh");
      out.println("update");
      }
    else if (cmd.equals("mcmd noLines")) {  // Clear lines
      out.println("lobjlist "+nms.Nodehash.size());
      out.println("refresh");
      out.println("update");
      }
    else if (cmd.equals("mcmd Lines")) {  // Restore original lines
      out.println("lobjlist "+nms.Nodehash.size());
      outLines();
      out.println("refresh");
      out.println("update");
      }
    }


  public void lines(PrintWriter out, String cmd) {
  // This is a crude way to support menu commands
    out.println("mcmd  ");
    if (cmd.startsWith("Relate ")) {
      out.println("lobjlist "+nms.Nodehash.size());
      out.println("mcmd Relationship: "+cmd.substring(7));
      outLines(cmd.substring(7));
      out.println("refresh");
      out.println("update");
      }
    else if (cmd.equals("noLines")) {  // Clear lines
      out.println("lobjlist "+nms.Nodehash.size());
      out.println("refresh");
      out.println("update");
      }
    else if (cmd.equals("Lines")) {  // Restore original lines
      out.println("lobjlist "+nms.Nodehash.size());
      outLines();
      out.println("refresh");
      out.println("update");
      }
    }


  public void load(PrintWriter out, String classname, String resourcename, String arg) {
    try {
      Class c = Class.forName(classname);
      Object ob = c.newInstance();
      if (ob instanceof NmsWebPlugin) {
        ((NmsWebPlugin)ob).exStart(arg); 
        excmds.put(resourcename, ob);  
        out.println("Class loaded;  excmd "+resourcename+" defined.");
        }
      else out.println("Class "+classname+" not a NmsWebPlugin");
      } 
    catch (Exception e) {out.println(e);} 
    }


  public void mbin(PrintWriter out, String a) {
    try {
      Listener mbinLis =  new Listener(nms, Integer.parseInt(a));
      mbinLis.start();
      }
    catch (Exception e) {out.println(e);} 
    }


  public void mbobject(PrintWriter out, String name, String attr) {
    //  set attribute (color) for Node
    NODE Node = (NODE)nms.Nodehash.get(name);
    if (Node == null) {
      System.out.println("Node "+name+" not found");
      return;
      }
    updateAttr(Node, attr);
    }

  public void mbobjectl(PrintWriter out, String name, String attr) {
    //  set attribute (color) for Node  by label
    NODE Node = (NODE)nms.NodeLabelhash.get(name);
    if (Node == null) {
      System.out.println("Node "+name+" not found");
      return;
      }
    updateAttr(Node, attr);
    }


  public void nodeview(PrintWriter out, String label) {
  // This is for debugging
    out.print("Active readers:");
    Enumeration e = Rdrs.elements();
    while (e.hasMoreElements()) {
      mbinRdr r = (mbinRdr)e.nextElement();
      out.print("  "+r.rdrid);
      }
    out.println();
    NODE Node = (NODE)nms.NodeLabelhash.get(label);
    //  display winid's for a Node  (Which windows show that Node)
    if (Node == null)  {out.println("node not found  "+label); return;}
    out.print("winids for Node:");
    e = Node.nobjlist.elements();
    while (e.hasMoreElements()) {
          NOBJ nobj = (NOBJ)e.nextElement();
          out.print("  "+nobj.Rdr.rdrid);
          }
    out.println();
    }


  public void pipecontrol(PrintWriter out, String command) {
    // Send command to all pipe plugins
    Enumeration e = Rdrs.elements();
    while (e.hasMoreElements()) {
      mbinRdr r = (mbinRdr)e.nextElement();
      if (!r.type.equals("Pipe")) continue;
//      System.out.println("Pipe Control "+r.id +" "+command);
      ((NmsPipePlugin)r.instan).control(command); 
      }
    }


  public void plugin(PrintWriter out, String name, Object arg) {
    //  Invoke a plugin - pipe output to new reader
    try {
      Class c = Class.forName(name);
      Object ob = c.newInstance();
      if (ob instanceof NmsPlugin) 
        ((NmsPlugin)ob).run(arg); 
      else if (ob instanceof NmsPipePlugin) {
        PipedOutputStream os1 = new PipedOutputStream();
        PipedInputStream is1 = new PipedInputStream(os1);
        PipedOutputStream os2 = new PipedOutputStream();
        PipedInputStream is2 = new PipedInputStream(os2);
        ((NmsPipePlugin)ob).execute(is1, os2, arg); 
        mbinRdr r = new mbinRdr(nms, is2, os1, "Pipe", name);
        r.instan = ob;
        r.start();
        }
      else System.out.println("Class "+name+" not a NmsPlugin");
      } 
    catch (Exception e) {System.out.println(e);} 
    }


  public void plugout(PrintWriter out, String name, Object arg) {
    //  Invoke a plugin - pipe output to our outputstream
    try {
      Class c = Class.forName(name);
      Object ob = c.newInstance();
      if (ob instanceof NmsPlugin) 
        ((NmsPlugin)ob).run(arg); 
      else if (ob instanceof NmsNodePlugin) 
        ((NmsNodePlugin)ob).run(nms.Nodehash, arg); 
      else if (ob instanceof NmsPipePlugin) {
        ((NmsPipePlugin)ob).execute(is, os, arg); 
        }
      else System.out.println("Class "+name+" not a NmsPlugin");
      } 
    catch (Exception e) {System.out.println(e);} 
    }



  public void readfile(PrintWriter out, String file) {
    //  read in a file of mapbox commands
    try {
      mbinRdr r = new mbinRdr(nms, 
        new FileInputStream(new File("alpmap", file)),
        System.out, "Readfile", file );
      r.start();
      } 
    catch (Exception e) {System.out.println("readfile "+file+": "+e);} 
    }


  public void relate(PrintWriter out, String label, Vector v) {
    //  set Alp relationships for a Node
    NODE Node = (NODE)nms.NodeLabelhash.get(label);
    if (Node != null) Node.relate = v;
    }


  public void relations(PrintWriter out) {
    Enumeration e = nms.Nodehash.elements();
    int i=0;
    while (e.hasMoreElements()) {
      NODE Node = (NODE)e.nextElement();
      if (Node.relate == null)  continue;   // no relationships for this Node
      Vector r = Node.relate; 
      Enumeration f = r.elements();
      while (f.hasMoreElements()) {
        String c1 = (String)f.nextElement();
        String c2 = (String)f.nextElement();
        String c3 = (String)f.nextElement();
        String c4 = (String)f.nextElement();
        String c5 = (String)f.nextElement();
        out.println("Relation  "+Node.label +" "+c1+" "+c2+" "+c3+" "+c4+" "+c5);
        }
      }
    }





//   --------------   Following are misc routines  ---------------------------

  public void updateAttr(NODE Node, String attr) {
  // Update atrribute (color) of Node if changed
    if (!Node.attr.equals(attr))  {
      Node.attr = attr;
//      System.out.println("Node "+Node.name+" set to "+Node.attr);
      Enumeration e = Node.nobjlist.elements();
        while (e.hasMoreElements()) {
          NOBJ nobj = (NOBJ)e.nextElement();
          nobj.Rdr.out.println("nobjstat "+nobj.nobjnum+" "+attr);
          nobj.Rdr.out.println("update");
          }
      
      }
    }


  public int nobjnum(String name) {  // find nobjnum for Node
    if (name == null) return -1;
    return nobjnum((NODE)nms.Nodehash.get(name));
    }

  public int nobjnuml(String label) {  // find nobjnum for Node
    if (label == null) return -1;
    return nobjnum((NODE)nms.NodeLabelhash.get(label));
    }

  public int nobjnum(NODE Node) {  // find nobjnum for Node
    if (Node == null) return -1;
    Enumeration e = Node.nobjlist.elements();
        while (e.hasMoreElements()) {
          NOBJ nobj = (NOBJ)e.nextElement();
          if (nobj.Rdr == this) return nobj.nobjnum;
          }
    return -1;
    }


  private void outLines() {  // send database lines to jmap window
//System.out.println("outLines ");
    Enumeration e = nms.Lines.elements();
    int i=0;
    while (e.hasMoreElements()) {
      Vector Line = (Vector)e.nextElement();
      String b = (String)Line.elementAt(2);
      String c = (String)Line.elementAt(3);
      int j=nobjnum(b);  if (j<0) continue;
      int k=nobjnum(c);  if (k<0) continue;
      out.println("lobj "+(i++)+" 1 "+j+" "+k +" 5");
      }
    }


  private void outLines(String relation) {  // send relation lines to jmap window
//System.out.println("outLines S "+relation);
    Enumeration e = nms.Nodehash.elements();
    int i=0;
    while (e.hasMoreElements()) {
      NODE Node = (NODE)e.nextElement();
      if (Node.relate == null)  continue;   // no relationships for this Node
      int j=nobjnum(Node);  if (j<0) continue;  // Node not in view
      Vector r = getRel(Node.relate, relation); 
      Enumeration f = r.elements();
      while (f.hasMoreElements()) {
        String c = (String)f.nextElement();
        int k=nobjnuml(c);  if (k<0) continue;
        if (relation.equals("Supporting")) 
          out.println("lobj "+(i++)+" 1 "+k+" "+j +" 25");  // backwards
        else out.println("lobj "+(i++)+" 1 "+j+" "+k +" 25");
        }
      }
    }


    public Vector getRel(Vector v, String prop) {
//System.out.println("getRel "+v+prop);
      Vector r = new Vector();
      Enumeration e = v.elements();
      while (e.hasMoreElements()) {
        Object a = e.nextElement();
        if (a instanceof String && a.equals(prop))  {
          if (e.hasMoreElements())  a = e.nextElement(); 
          r.addElement( (String)a);
          }
        else  if (e.hasMoreElements())  e.nextElement(); 
          if (e.hasMoreElements())  e.nextElement(); // skip self UID
          if (e.hasMoreElements())  e.nextElement(); // skip target UID
        }
      return r;
      }



  }  //  end of  class mbinRdr 



