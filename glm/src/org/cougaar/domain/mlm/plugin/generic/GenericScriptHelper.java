/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.mlm.plugin.generic;

import org.cougaar.domain.glm.ldm.Constants;

import org.cougaar.util.UnaryPredicate;

import org.cougaar.domain.planning.ldm.plan.Task;
import org.cougaar.domain.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.domain.planning.ldm.plan.Verb;
import org.cougaar.domain.planning.ldm.plan.Preposition;
import org.cougaar.domain.planning.ldm.plan.Expansion;
import org.cougaar.domain.planning.ldm.plan.Workflow;

import org.cougaar.domain.glm.ldm.asset.Organization;
import org.cougaar.domain.planning.ldm.asset.Asset;

import java.util.Enumeration;


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









