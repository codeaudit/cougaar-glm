// Copyright (10/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.glm.packer; 

import org.cougaar.util.UnaryPredicate;

import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.planning.ldm.plan.Preposition;
 
import org.cougaar.glm.ldm.Constants;
import org.cougaar.glm.ldm.asset.Ammunition;

/**
  * This UnaryPredicate is used to test whether Tasks should be
  * packed together, in the Packer's packing rule.  It picks out
  * all supply tasks that request Ammunition.
  */

public class AmmoPackerPredicate  {

  public static UnaryPredicate getInputTaskPredicate() {
    return new UnaryPredicate() {
      
      public boolean execute(Object o) {
        if ((o instanceof Task) &&
            (((Task)o).getVerb().equals(Constants.Verb.SUPPLY)) &&
            (((Task)o).getPrepositionalPhrase(GenericPlugin.INTERNAL) == null) &&
            (((Task)o).getDirectObject() instanceof Ammunition)) {
          return true;
        } else {
          return false;
        }
      }
    };
  }

  public static UnaryPredicate getPlanElementPredicate() {
    return new UnaryPredicate() {
      private UnaryPredicate myInputTaskPredicate = getInputTaskPredicate();

      public boolean execute(Object o) {
        if (o instanceof PlanElement) {
          Task task = ((PlanElement)o).getTask();
          if (myInputTaskPredicate.execute(task) ||
              ((task.getPrepositionalPhrase(GenericPlugin.INTERNAL) !=null) &&
               (task.getDirectObject() instanceof Ammunition))) {
            return true;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    };
  }
}





