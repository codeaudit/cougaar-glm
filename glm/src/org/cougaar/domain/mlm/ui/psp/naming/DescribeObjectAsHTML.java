package org.cougaar.domain.mlm.ui.psp.naming;
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

import java.io.PrintStream;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.spi.*;

import org.cougaar.core.naming.*;
import org.cougaar.domain.planning.ldm.plan.Role;
import org.cougaar.lib.planserver.server.FDSURL;
import org.cougaar.util.UnaryPredicate;


/**
 * Pretty simple helper class - formats objects in HTML
 * Makes certain assumptions about HTML context (eg. within table column etc.)
 *
 * Purpose is less to encapsulate funky html generation and more
 * to enforce "standard" represetnation of objects (in HTML)
 *
 * @return HTML string description generated.
 */

public class DescribeObjectAsHTML {

     public static String describeWithinTableColumn( String name, Object obj) {
          String result = new String();
          if( obj instanceof FDSURL) {
                 FDSURL furl = (FDSURL)obj;
                 result = "<TD BGCOLOR=YELLOW><FONT COLOR=RED SIZE=-1>" + name + "</FONT>:" + furl.myURL + "</TD>";
          }
          else if( obj instanceof AgentRole) {
                 AgentRole role = (AgentRole)obj;
                 result = "<TD BGCOLOR=YELLOW><FONT COLOR=RED SIZE=-1>" + name + "</FONT>, namespace=" + role.namespace + "</TD>";
          }
          else if( obj instanceof NamingDirContext) {
                 NamingDirContext ndircontext = (NamingDirContext)obj;
                 result = "<TD BGCOLOR=ORANGE><FONT SIZE=-1>" + name + "</FONT></TD>";
          }
          else if( obj instanceof Binding) {
                 Binding binding = (Binding)obj;
                 result = "<TD BGCOLOR=YELLOW><FONT SIZE=-1>" + binding.getClassName() + "</FONT></TD>";
          }
          /**
          else if( obj instanceof Role ) {
                 Role role = (Role)obj;
                 result = "<TD BGCOLOR=YELLOW><FONT SIZE=-1 COLOR=RED>" + role.getName() + "</FONT></TD>";
          }
          **/
          else if( obj instanceof Attribute ) {
                 Attribute att = (Attribute)obj;
                 //try{
                    result = "<TD BGCOLOR=YELLOW><FONT SIZE=-1 COLOR=RED>" + att.getID() + "</FONT></TD>";
                 //} catch (NamingException ex){
                 //   ex.printStackTrace();
                // }
          }
          else{
                 result = "<TD BGCOLOR=YELLOW><FONT COLOR=RED SIZE=-1>" + name + "</font>:" + obj.getClass().toString() + "</TD>";
          }
          return result;
     }
}