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









