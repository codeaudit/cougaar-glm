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

package org.cougaar.mlm.plugin.generic;

import java.util.Enumeration;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Workflow;
import org.cougaar.util.UnaryPredicate;


public class GenericScriptHelper {

    public static UnaryPredicate getAllocatableGLSPredicate() {
	UnaryPredicate allocGLSPred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Task) {
		    Task t = (Task) o;
		    if ( t.getPlanElement() == null ) {
			if (t.getVerb().toString().equals(Constants.Verb.GETLOGSUPPORT)) {
			    Enumeration pp = t.getPrepositionalPhrases();
			    while (pp.hasMoreElements()) {
				PrepositionalPhrase app = (PrepositionalPhrase) pp.nextElement();
				if ((app.getPreposition().equals(Constants.Preposition.FOR)) && (app.getIndirectObject() instanceof Asset) ) {
				    String name = null;
				    try {
					name = ((Asset)app.getIndirectObject()).getTypeIdentificationPG().getTypeIdentification();
				    } catch (Exception e) {
					System.out.println("GLSAlloc error while trying to get the TypeIdentification of an asset");
					e.printStackTrace();
				    }
				    if (name.equals("Subordinates")) {
					return true;
				    }
				}
			    }
			} 
		    }
		}
		return false;
	    }
	};
	return ((UnaryPredicate)allocGLSPred);
    }
    
    public static UnaryPredicate getExpChangePred() {
	UnaryPredicate myExpsPred = new UnaryPredicate() {
	    public boolean execute(Object o) {
		if (o instanceof Expansion) {
		    Workflow wf = ((Expansion)o).getWorkflow();
		    Enumeration wftasks = wf.getTasks();
		    while (wftasks.hasMoreElements()) {
			Task t = (Task) wftasks.nextElement();
			if (t.getVerb().toString().equals(Constants.Verb.GETLOGSUPPORT)) {
			    Enumeration pp = t.getPrepositionalPhrases();
			    while (pp.hasMoreElements()) {
				PrepositionalPhrase app = (PrepositionalPhrase) pp.nextElement();
				if ((app.getPreposition().equals(Constants.Preposition.FOR)) && (app.getIndirectObject() instanceof Organization) ) {
				    return true;
				}
			    }
			}
		    }
		}
		return false;
	    }
	};
	return myExpsPred;
    }	

}









