/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.spi.*;

import java.io.*;
import java.util.*;

import org.cougaar.core.naming.*;
import org.cougaar.lib.planserver.server.FDSURL;
import org.cougaar.util.UnaryPredicate;

//###########################################################################
//###########################################################################
//###########################################################################
//
// YPDemoJNDI  - A helper class encapsulating JNDI access
//
public class YPDemoJNDI
{

    /**
     *   createORLookupDirectory_andBindObject() does the following:
     *
     *       0.) Looksup Context of 'rootContextName' else Creates
     *       1.) Looks up (or creates if doesn't exist) Context identified by 'rootContextName' beneath 'rootContextName'
     *       2.) Binds or rebinds 'bindingObject' of name 'objName' to Context.
     *       3.) Attaches 'attributes' to 'bindingObject'
     *
     *   @return true if successful, else false
     */
     public static boolean createORLookupDirectory_andAttributeObject(
                                     String rootContextName, String subContextName,
                                     String objName, Object bindingObject, Attributes attributes,
                                     NamingService nservice )
     {
          DirContext topDirC = null;
          try{
              DirContext dirc = (DirContext)nservice.getRootContext();
              try{
                    topDirC = (DirContext)dirc.createSubcontext(rootContextName);
              } catch( NameAlreadyBoundException ex ) {
                    //
                    // catch NameAlreadyBoundException -  directory already exists
                    // perform a lookup instead of creating new
                    //
                    topDirC = (DirContext)dirc.lookup(rootContextName);
              }
              //agents = (DirContext)dirc.lookup(rootContextName);
          } catch (NamingException ex) {
              //
              // hmmm, we gotta fail if can't find "Agents" subdirectory
              // as it should have already been created by infrastructure...
              //
              ex.printStackTrace();
              return false;
          }

          DirContext subKontext = null;
          try{
              try{
                    subKontext = (DirContext)topDirC.createSubcontext(subContextName);
              } catch( NameAlreadyBoundException ex ) {
                    //
                    // catch NameAlreadyBoundException -  directory already exists
                    // perform a lookup instead of creating new
                    //
                    subKontext = (DirContext)topDirC.lookup(subContextName);
              }
              try{
                  subKontext.bind(objName, bindingObject, attributes );
              } catch ( NameAlreadyBoundException ex) {
                  // Prevsious session may have created object and bound it with attributes.
                  // Lookup this object, and merge attributes.
                  Attributes attrs = subKontext.getAttributes(objName);
                  NamingEnumeration nen = attrs.getAll();
                  while(nen.hasMore()) {
                       attributes.put((Attribute)nen.next());
                  }
                  subKontext.rebind(objName,bindingObject,attributes);
              }

         } catch ( NamingException ex ) {
              //
              // Hmmm, no idea on how to recover from this, just complain and return
              //
              ex.printStackTrace();
              return false;
         }
          return true;
     }




     //###########################################################################
     //
     //
     public static String searchAttribute(String name, String attribute, NamingService nserve ){
          try{
               Attributes attributes = new BasicAttributes();
               Attribute att = new BasicAttribute(attribute);
               attributes.put(att);
               DirContext dctx = nserve.getRootContext();
               NamingEnumeration en = dctx.search(name,attributes);
               String ret = new String("<TABLE>");
               while( en.hasMore() ) {
                   Object obj =  en.next();
                   String each = "<TR><TD>FOUND SEARCH ATTRIBUTE: " + obj + "</TD></TR>";
                   ret += each;
                   System.out.println(each);
               }
               ret += "</TABLE>";
               return ret;
          } catch( Exception ex) {
          }
          return null;
     }


     //###########################################################################
     // Traverses directory structure starting from the JNDI root
     // and prints HTMLized representation of contents.
     //
     // @return String HTMLized representation.
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
               describeNamingEnumeration(  cn, ctx.listBindings(""), stream, nserve,  1);
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
     //  Private helper method used by describeAllDirContexts()
     //
     private static void describeNamingEnumeration( CompositeName tail, NamingEnumeration en,
                                 PrintStream stream,  NamingService nserve,  int depth ) throws NamingException
     {
         DirContext rootCtx = nserve.getRootContext();
         while( en.hasMoreElements() ) {
             Object obj = en.nextElement();
             Binding bind = (Binding)obj;

             if( bind.getObject() instanceof NamingDirContext)
             {
                  CompositeName t = new CompositeName(tail.toString());
                  t.add(bind.getName());

                  stream.println("<TR>");
                  for(int i=0; i< depth-1; i++) stream.println("<TD>.</TD>");
                  stream.println(DescribeObjectAsHTML.describeWithinTableColumn(t.toString(),bind.getObject()));
                  //stream.println("<TD BGCOLOR=RED><FONT SIZE=-1>" + t.toString() + "</FONT></TD>");
                  stream.println("</TR>");

                  describeNamingEnumeration(t, rootCtx.listBindings(t.toString()), stream, nserve, depth+1);
             }
             else {
                  CompositeName t = new CompositeName(tail.toString());
                  t.add(bind.getName());
                  stream.println("<TR>");
                  for(int i=0; i< depth-1; i++) stream.println("<TD>.</TD>");
                  if( bind.getObject() instanceof FDSURL) {
                        //FDSURL furl = (FDSURL)bind.getObject();
                        stream.println(DescribeObjectAsHTML.describeWithinTableColumn(bind.getName(),bind.getObject()));
                        //stream.println("<TD><FONT COLOR=RED SIZE=-1>" + bind.getName() + "</FONT>:" + furl.myURL + "</TD>");
                  }
                  else if( bind.getObject() instanceof AgentRole) {
                        //AgentRole role = (AgentRole)bind.getObject();
                        //stream.println("<TD><FONT COLOR=RED SIZE=-1>" + bind.getName() + "</FONT>, namespace=" + role.namespace + "</TD>");

                         stream.println(DescribeObjectAsHTML.describeWithinTableColumn(bind.getName(),bind.getObject()));
                         DirContext ctx = (DirContext)rootCtx.lookup(tail.toString());
                         Attributes atts = ctx.getAttributes(bind.getName());

                         //System.out.println("######## tail.toString()=" + tail.toString() + ", bind.getName()=" + bind.getName()
                         // + ", atts.size()=" + atts.size());

                         NamingEnumeration nen = atts.getAll();
                         stream.println("<TD><TABLE>");
                         while(nen.hasMore()){
                              Attribute a = (Attribute)nen.next();
                              //System.out.println("############# Attribute a = " + a);
                              stream.println("<TR>");
                              stream.println(DescribeObjectAsHTML.describeWithinTableColumn(a.toString(),a));
                              stream.println("</TR>");
                         }
                         stream.println("</TABLE></TD>");
                  }
                  else {
                        //stream.println("<TD><FONT COLOR=RED SIZE=-1>" + bind.getName() + "</font>:" + bind.getClassName() + "</TD>");
                        stream.println(DescribeObjectAsHTML.describeWithinTableColumn(bind.getName(),bind));
                  }
                  stream.println("</TR>");
             }
         }
     }

    //###########################################################################
    // Returns list of Binding objects which match filter
     public static List collectObjects( NamingService nserve, UnaryPredicate filterPred  )
     {
         List returnList = new ArrayList();

         try {
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
     //
     // Private helper method used by collectObjects()
     //
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
    // Returns Map, Key= Name : Value= Vector of Attribute Strings.
     public static Map collectAttributes( NamingService nserve, UnaryPredicate filterPred  )
     {
         Map returnMap = new Hashtable();

         try {
               // Create the initial context
               //Context ctx = new InitialContext(JNDIConfig.getEnvironment());
               Context ctx = nserve.getRootContext();
               CompositeName cn = new CompositeName("");
               collectAttributeTraversal(  cn, ctx.listBindings(""), returnMap, filterPred, nserve);
         } catch (NamingException e) {
               System.err.println("[collectObjects()] Exception: " + e);
               e.printStackTrace();
         }
         return returnMap;
     }

     //###########################################################################
     private static void collectAttributeTraversal( CompositeName tail, NamingEnumeration en,
                                 Map returnMap,  UnaryPredicate filterPred, NamingService nserve) throws NamingException
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
                         Vector v = (Vector)returnMap.get(tail.toString());
                         if( v == null) {
                             v= new Vector();
                             returnMap.put(tail.toString(), v);
                         }
                         //
                         // STRIP OFF ATTRIBUTE VALUE BEFORE PLACING
                         // ATTRIBUTE NAME INTO ReturnMap.
                         //
                         // TO DO: SAVE VALUES SEPARATELY.
                         //
                         v.add(stripValue(a.toString()));
                         //returnList.add("{" + tail.toString() + "::" + a.toString() + "}");
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
                      collectAttributeTraversal(t, ctx.listBindings(t.toString()), returnMap, filterPred, nserve);
                 }
             }
         }
     }


    //###########################################################################
    private static String stripValue(String attributeValue){
         int idx = attributeValue.indexOf(":");
         if( idx > -1 ) {
             return attributeValue.substring(0,idx);
         }
         return attributeValue;
    }
}

