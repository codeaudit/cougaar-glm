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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.planning.ldm.plan.Verb;

public interface GLSConstants {
  final String FOR_OPLAN_STAGES = "ForOplanStages";
  final String WITH_C0 = "WithC0";
  final String FOR_ROOT = "ForRoot";
  final String FOR_ORGANIZATION = Constants.Preposition.FOR;


  final Verb PROPAGATE_REGISTER_SERVICES = Constants.Verb.PropagateRegisterServices;
  final Verb PROPAGATE_FIND_PROVIDERS = Constants.Verb.PropagateFindProviders;
  final Verb GET_LOG_SUPPORT = Constants.Verb.GetLogSupport;

  final Verb GLSInitVerbs[] = {
    PROPAGATE_REGISTER_SERVICES,
    PROPAGATE_FIND_PROVIDERS,
    GET_LOG_SUPPORT
  };

}
