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

package org.cougaar.lib.callback;

import java.util.Collection;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;


/**
 * Root callback listener.  The only thing the callback needs from
 * all listeners is that they be able to create a subscription.
 * 
 * Note that this could extend org.cougaar.planning.plugin.legacy.PluginDelegate, and
 * then we could get the subscribe method, but we would also get 
 * all the other delegate methods, cluttering the interface.
 * 
 * However, if we find that we need listeners to have a lot of the 
 * methods from PluginDelegate (e.g. publishAdd, publishChange),
 * then we might want to revisit this.
 *
 * GWFV 6/1/99
 */

public interface UTILFilterCallbackListener {
  /** only thing we need from listener is way to make a subscription */
  IncrementalSubscription subscribeFromCallback(UnaryPredicate pred);


  /** 
   * Sometimes a listener wants to use a special container to put the
   * objects in. 
   *
   * See the second constructor in UTILFilterCallbackAdapter
   */
  IncrementalSubscription subscribeFromCallback(UnaryPredicate pred,
						Collection specialContainer);

}






