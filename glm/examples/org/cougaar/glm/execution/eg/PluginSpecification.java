/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
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
