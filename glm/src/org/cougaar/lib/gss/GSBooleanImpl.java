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

package org.cougaar.lib.gss;

import java.util.List;
import java.util.Vector;

/**
 * Provides mechanism for making arbitrary boolean expression out of
 * matches; this is the base class
 *
 */

public class GSBooleanImpl implements GSParent, GSBoolean {

  protected static final int AND = 0;
  protected static final int OR = 1;
  protected static final int XOR = 2;
  protected static final int NOT = 3;

  protected int operation = -1;
  protected boolean isAnd = false;
  protected Vector clauses = new Vector (4);

  /**
   * @param determines which boolean operation to perform
   */
  public GSBooleanImpl (String operation) {
    if (operation.equals ("and")) {
      this.operation = AND;
      isAnd = true;
    } else if (operation.equals ("or"))
      this.operation = OR;
    else if (operation.equals ("xor"))
      this.operation = XOR;
    else if (operation.equals ("not"))
      this.operation = NOT;
  }

  public void addChild (Object obj) {
    clauses.addElement (obj);
    if ((operation == NOT) && (clauses.size() > 1))
      System.out.println ("Too many clauses in NOT operation");
    if ((operation == XOR) && (clauses.size() > 2))
      System.out.println ("Too many clauses in XOR operation");
  }

  public boolean eval (List args) {
    switch (operation) {
    case NOT:
      return ! getBool(0).eval (args);
    case XOR:
      return (getBool(0).eval (args) ^ getBool(1).eval (args));
    default:
      for (int i = 0; i < clauses.size(); i++)
        if (isAnd ^ getBool(i).eval (args))
          return ! isAnd;
      return isAnd;
    }
  }

  private final GSBoolean getBool (int i) {
    return (GSBoolean) clauses.elementAt (i);
  }

}
