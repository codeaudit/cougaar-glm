/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.producers.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cougaar.core.society.UID;
import org.cougaar.core.util.AsciiPrinter;
import org.cougaar.core.util.SelfPrinter;

public class UIPolicyInfo implements SelfPrinter, java.io.Serializable {
  private ArrayList myParameters = new ArrayList();
  private UID myUID = null;
  private String myName = null;

  public UIPolicyInfo() {
  }

  public UIPolicyInfo(String name, String id) {
    myName = name;
    myUID = new UID(id);
  }

  public void setUID(String uidStr) {
    myUID = new UID(uidStr);
  }

  public UID getUID() {
    return myUID;
  }

  public void setName(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }

  /**
   * returns a snapshot array of the current myParameters
   * @return ArrayList - the associated policy parameters
   */
  public ArrayList getParameters() {
    return (ArrayList)myParameters.clone();
  }

  /**
   * sets the associated parameters
   * @param ArrayList the associated policy parameters
   */
  public void setParameters(ArrayList parameters) {
    myParameters = new ArrayList(parameters);
  }
  

    
  // Redefine equals to be UID.equal()
  public boolean equals(Object o) {
      if (o instanceof UIPolicyInfo) {
	  return myUID.equals(((UIPolicyInfo)o).getUID());
      }
      return false;
  }


  /**
   * adds a UIPolicyParameterInfo to this composite
   * @param child  the child UIPolicyParameterInfo to add
   */
  public void add(UIPolicyParameterInfo child) {
    synchronized(this) {
      if (myParameters == null) {
        myParameters = new ArrayList();
      }
      myParameters.add(child);
    }
  }

  /**
   * removes a UIPolicyParameterInfo from this composite
   * @param child  the child UIPolicyParameterInfo to remove
   */
  public boolean remove(UIPolicyParameterInfo child) {
    boolean removed = false;
    if (myParameters != null) {
      synchronized(this) {
        if (myParameters.remove(child)) {
          removed = true;
        }
      }
    }
    return removed;
  }
  
  /**
   * removes a child at a given index.
   * @param index  0-based index of the child
   * @return      the child removed
   * @throws IndexOutOfBoundsException if there is no child at the given index
   */
  public UIPolicyParameterInfo remove(int index) {
    synchronized(this) {
      if (myParameters == null) {
        throw new IndexOutOfBoundsException();
      }
      return (UIPolicyParameterInfo)myParameters.remove(index);
    }
  }
  /**
   * gets a child at a given index.
   * @param index  0-based index of the child
   * @return      the child to get
   * @throws IndexOutOfBoundsException if there is no child at the given index
   */
  public UIPolicyParameterInfo get(int index) {
    synchronized(this) {
      if (myParameters == null) {
        throw new IndexOutOfBoundsException();
      }
      return (UIPolicyParameterInfo)myParameters.get(index);
    }
  }
  
  

  public void printContent(AsciiPrinter pr) {
    pr.print(myName, "Name");
    pr.print(myUID.toString(), "UID");
    pr.print(myParameters, "Parameters");
  }


  public String toString() {
    return org.cougaar.core.util.PrettyStringPrinter.toString(this);
  }

}






