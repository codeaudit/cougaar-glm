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

public interface Plugin {
  /**
   * A name to use in menus and such.
   **/
  String getPluginName();

  /**
   * Get a description of this plugin.
   **/
  String getDescription();

  /**
   * Plugins can be parameterized with a string.
   **/
  void setParameter(String parameter);

  /**
   * Test if this plugin can be configured
   * @return true if it is possible to configure this plugin (has a GUI)
   **/
  boolean isConfigurable();


  void setEventGenerator(EventGenerator eg);


  void configure(java.awt.Component frame);

  /**
   * Plugins should save and restore their state in a property file.
   * @param props the Properties map in which the values are saved.
   * @param prefix the prefix for every property name. The prefix
   * uniquely identifies the plugin.
   **/
  void save(java.util.Properties props, String prefix);
  void restore(java.util.Properties props, String prefix);
}
