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


import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;


//###########################################################################
//###########################################################################
//###########################################################################
//
// Identifies Query Tags against JNDI NS.  Populated at runtime when
// user BUTTON_UPDATE_YP_ATTRIBUTES selected
//
public class QueryObject {
     public  QueryObject() {
          Vector v = new Vector();
          v.add(GET_ALL);
          myEntries.put(GET_ALL, v);
     }

     public final static String GET_ALL = "GET_ALL";

     //public boolean allRoles = true;
     private Map myEntries = new Hashtable(); // Values = vectors of Attributes, Keys = Names

     public Map getEntries() { return myEntries; }
     public void setEntries( Map entries) {
           // make sure Query Tag Entries included the "catch all" GET ALL!
           Vector v = new Vector();
           v.add(QueryObject.GET_ALL);
           entries.put(QueryObject.GET_ALL,v);
           myEntries = entries;
    }

}


