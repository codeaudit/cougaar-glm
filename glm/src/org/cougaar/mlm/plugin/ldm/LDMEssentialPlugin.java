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

package org.cougaar.mlm.plugin.ldm;


import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.planning.ldm.PropertyProvider;
import org.cougaar.planning.ldm.PrototypeProvider;
import org.cougaar.planning.plugin.legacy.SimplePlugin;

/**
 * The purpose of this class is to provide a base class for the creation of LDMPlugins.
 * This implementation leaves the actual methods of Asset creation as an exercise
 * for the reader, i.e. use of JDBC to access data.
 * @see LDMSQLPlugin
 */

public abstract class LDMEssentialPlugin
  extends SimplePlugin
  implements PropertyProvider, PrototypeProvider
{
  // For maintaining container of Assets added by this LDMPlugin
  Subscriber subscriber; 

  public LDMEssentialPlugin() {}
	
  /**
   * Empty execute(); may (should?) be overridden by subclasses.
   */
  public void execute() {}
	
  //
  // LDMService
  //
  //public abstract Asset getPrototype(String typeid, Class hint);
	
  //public abstract void provideProperties(Asset asset);
	
}
