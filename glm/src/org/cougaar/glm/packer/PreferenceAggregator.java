// Copyright (11/99) Honeywell Inc.
// Unpublished - All rights reserved. This software was developed with funding 
// under U.S. government contract MDA972-97-C-0800

package org.cougaar.glm.packer;

import java.util.*;

import org.cougaar.planning.ldm.plan.Preference;
import org.cougaar.core.domain.RootFactory;

/**
  * Typically, when building an aggregator, one wants to be able to
  * specify how the Preferences on the MPTask of a Container will 
  * depend on the Preferences of its parent Task(s).  This interface
  * encapsulates that behavior.
  */
public interface PreferenceAggregator {
  /**
    * @return The return value of this method should be an Enumeration of
    * Preferences, suitable to be the input value of the NewTask interface's
    * setPreferences method.
    * @see org.cougaar.planning.ldm.plan.NewTask#setPreferences
    */ 
  ArrayList aggregatePreferences(Iterator tasks, RootFactory rootFactory);
}




