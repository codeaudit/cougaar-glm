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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.glm.ldm.Constants;

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
