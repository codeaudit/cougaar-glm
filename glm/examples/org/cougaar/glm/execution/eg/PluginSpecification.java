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
package org.cougaar.glm.execution.eg;

/**
 * Specify a particular plugin in terms of its class and parameter.
 **/
public class PluginSpecification {
  private Class cls;
  private String parameter;
  private int hc;

  public PluginSpecification(String specificationString) throws ClassNotFoundException {
    int pos = specificationString.indexOf('/');
    if (pos != -1) {
      parameter = specificationString.substring(pos + 1);
      cls = Class.forName(specificationString.substring(0, pos));
    } else {
      parameter = null;
      cls = Class.forName(specificationString);
    }
    hc = cls.hashCode() + (parameter == null ? 0 : parameter.hashCode());
  }

  public String getParameter() {
    return parameter;
  }

  public Class getPluginClass() {
    return cls;
  }

  public int hashCode() {
    return hc;
  }

  public boolean equal(Object o) {
    if (o instanceof PluginSpecification) {
      PluginSpecification other = (PluginSpecification) o;
      if (this.cls == other.cls) {
        if (this.parameter == null) return other.parameter == null;
        return this.parameter.equals(other.parameter);
      }
    }
    return false;
  }
}
