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


package org.cougaar.domain.mlm.ui.psp.naming;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.spi.*;

import java.io.*;
import java.util.*;

import org.cougaar.core.naming.*;
import org.cougaar.lib.planserver.server.FDSURL;
import org.cougaar.util.UnaryPredicate;

/**
 * YPDemoJNDI  - A helper class encapsulating JNDI access
 */
public class YPDemoJNDI
{

     //###########################################################################
     // Traverses directory structure and prints HTMLized represetnation of contents
     //
     public static String describeAllDirContexts( NamingService nserve  )
     {
         ByteArrayOutputStream barray = new ByteArrayOutputStream();
         BufferedOutputStream bstream = new BufferedOutputStream(barray);
         PrintStream stream = new PrintStream( bstream );

         try {
               // Create the initial context
               //Context ctx = new InitialContext(JNDIConfig.getEnvironment());
               Context ctx = nserve.getRootContext();
               CompositeName cn = new CompositeName("");
               stream.println("<TABLE>");
               describeNamingEnumeration(  cn, ctx.listBindings(""), stream, nserve, 1);
               stream.println("</TABLE>");
               stream.flush();
         } catch (NamingException e) {
               System.err.println("[describeAllDirContexts()] Exception: " + e);
               e.printStackTrace();
         }
         if(barray.size() < 1 ) return "Empty directory.";
         return barray.toString();
     }

     //###########################################################################
     private static void describeNamingEnumeration( CompositeName tail, NamingEnumeration en,
                                 PrintStream stream,  NamingService nserve, int depth ) throws NamingException
     {
         while( en.hasMoreElements() ) {
             Object obj = en.nextElement();
             Binding bind = (Binding)obj;

             if( bind.getObject() instanceof NamingDirContext)
             {
                  Context ctx = nserve.getRootContext();
                  CompositeName t = new CompositeName(tail.toString());
                  t.add(bind.getName());

                  stream.println("<TR>");
                  for(int i=0; i< depth-1; i++) stream.println("<TD>.</TD>");
                  stream.println("<TD BGCOLOR=RED><FONT SIZE=-1>" + t.toString() + "</FONT></TD>");
                  stream.println("</TR>");
                  //stream.println("\nNamingDirContext[ list(CompositeName)=" + t.toString()
                  //           + "]");

                  describeNamingEnumeration(t, ctx.listBindings(t.toString()), stream, nserve, depth+1);
             }
             else {
                  CompositeName t = new CompositeName(tail.toString());
                  t.add(bind.getName());
                  stream.println("<TR>");
                  for(int i=0; i< depth-1; i++) stream.println("<TD>.</TD>");
                  if( bind.getObject() instanceof FDSURL) {
                        FDSURL furl = (FDSURL)bind.getObject();
                        stream.println("<TD><FONT COLOR=RED SIZE=-1>" + bind.getName() + "</FONT>:" + furl.myURL + "</TD>");
                  }
                  if( bind.getObject() instanceof AgentRole) {
                        AgentRole role = (AgentRole)bind.getObject();
                        stream.println("<TD><FONT COLOR=RED SIZE=-1>" + bind.getName() + "</FONT>, namespace=" + role.namespace + "</TD>");
                  }
                  else {
                        stream.println("<TD><FONT COLOR=RED SIZE=-1>" + bind.getName() + "</font>:" + bind.getClassName() + "</TD>");
                  }
                  stream.println("</TR>");
                  //stream.println("\nEntity[ list(CompositeName)=" + t.toString()
                  //           + ", pair.getClassName()=" + pair.getClassName()
                  //           + "]");
                  //stream.println("NOT NamingDirContext type discovered=" + pair.getName()+ "," + pair.getClassName());
             }
         }
     }

    //###########################################################################
    // Returns list of Binding objects which match filter
     public static List collectObjects( NamingService nserve, UnaryPredicate filterPred  )
     {
         List returnList = new ArrayList();

         try {
               // Create the initial context
               //Context ctx = new InitialContext(JNDIConfig.getEnvironment());
               Context ctx = nserve.getRootContext();
               CompositeName cn = new CompositeName("");
               collectObjectTraversal(  cn, ctx.listBindings(""), returnList, filterPred, nserve);
         } catch (NamingException e) {
               System.err.println("[collectObjects()] Exception: " + e);
               e.printStackTrace();
         }
         return returnList;
     }

     //###########################################################################
     private static void collectObjectTraversal( CompositeName tail, NamingEnumeration en,
                                 List returnList,  UnaryPredicate filterPred, NamingService nserve) throws NamingException
     {
         while( en.hasMoreElements() ) {
             Object obj = en.nextElement();
             if( filterPred.execute(obj) == true ) {
                 Context ctx = nserve.getRootContext();
                 returnList.add(obj);
             }
             if( obj instanceof Binding )
             {
                 Binding bind = (Binding)obj;
                 if( bind.getObject() instanceof NamingDirContext) {
                      Context ctx = nserve.getRootContext();
                      CompositeName t = new CompositeName(tail.toString());
                      t.add(bind.getName());
                      collectObjectTraversal(t, ctx.listBindings(t.toString()), returnList, filterPred, nserve);
                 }
             }
         }
     }

    //###########################################################################
    // Returns list of Name : Attribute Strings.
     public static List collectAttributes( NamingService nserve, UnaryPredicate filterPred  )
     {
         List returnList = new ArrayList();

         try {
               // Create the initial context
               //Context ctx = new InitialContext(JNDIConfig.getEnvironment());
               Context ctx = nserve.getRootContext();
               CompositeName cn = new CompositeName("");
               collectAttributeTraversal(  cn, ctx.listBindings(""), returnList, filterPred, nserve);
         } catch (NamingException e) {
               System.err.println("[collectObjects()] Exception: " + e);
               e.printStackTrace();
         }
         return returnList;
     }

     //###########################################################################
     private static void collectAttributeTraversal( CompositeName tail, NamingEnumeration en,
                                 List returnList,  UnaryPredicate filterPred, NamingService nserve) throws NamingException
     {
         while( en.hasMoreElements() ) {
             Object obj = en.nextElement();
             if( filterPred.execute(obj) == true ) {
                 Binding b = (Binding)obj;
                 DirContext rctx = (DirContext)nserve.getRootContext();
                 DirContext ctx = (DirContext)rctx.lookup(tail.toString());

                 System.out.println("b.getName()=" + b.getName() + ", tail.toString()= " + tail.toString() );
                 Attributes atts = ctx.getAttributes( b.getName() ); // new CompositeName(b.getName()));
                 System.out.println("# Attributes for " +  b.getName() + " in " + tail.toString() + " = " + atts.size());
                 if( (atts != null) && (atts.size() > 0) ) {
                     NamingEnumeration en2 = atts.getAll();
                     while( en2.hasMore()) {
                         Attribute a = (Attribute)en2.next();
                         returnList.add("{" + tail.toString() + "::" + a.toString() + "}");
                     }
                     /**
                     for(int i=0; i< atts.size(); i++) {
                          Attribute a = (Attribute)atts.get(i);
                          returnList.add(tail.toString() + "::" + a.toString());
                     }
                     **/
                 }
                 //returnList.add(obj);
             }
             if( obj instanceof Binding )
             {
                 Binding bind = (Binding)obj;
                 if( bind.getObject() instanceof NamingDirContext)
                 {
                      Context ctx = nserve.getRootContext();
                      CompositeName t = new CompositeName(tail.toString());
                      t.add(bind.getName());
                      collectAttributeTraversal(t, ctx.listBindings(t.toString()), returnList, filterPred, nserve);
                 }
             }
         }
     }

}

