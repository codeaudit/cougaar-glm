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

package org.cougaar.lib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Task;

import org.cougaar.util.LRUCache;

import org.cougaar.util.log.Logger;

/** 
 * This class contains utility functions for creating
 * and accessing PrepositionalPhrases.
 */

public class UTILPrepPhrase {
  private static String myName = "UTILPrepPhrase";
  protected Logger logger;

  // 0 = LRU
  // 1 = hashmap
  private static final int CACHE_STYLE_DEFAULT = 0;
  private static final int CACHE_STYLE = 
    Integer.getInteger("org.cougaar.lib.util.UTILPreference.cacheStyle",
                       CACHE_STYLE_DEFAULT).intValue();

  private static final int CACHE_SIZE_DEFAULT = 256;
  private static final int CACHE_SIZE =
    Integer.getInteger("org.cougaar.lib.util.UTILPreference.cacheSize",
                       CACHE_SIZE_DEFAULT).intValue();

  /** maps prepositions to map of indirect objects to prep phrases */
  protected Map prepToIndirectMap;

  public UTILPrepPhrase (Logger log) { 
    logger = log;

    if (CACHE_STYLE == 1) {
      prepToIndirectMap = new HashMap(CACHE_SIZE);
    } else {
      prepToIndirectMap = new LRUCache(CACHE_SIZE);
    }
  }

  /**
   * Utility method for extracting the indirect object from a 
   * prepositional phrase.
   * @param t the task
   * @param prepName the Preposition in String
   * @return Object
   */
  public Object getIndirectObject (Task t, String prepName) {
    Enumeration prepphrases = t.getPrepositionalPhrases();
    PrepositionalPhrase pp = getPrepNamed(prepphrases, prepName);
    if (pp == null) 
      throw new UTILRuntimeException ("\nAsking for prep named <" + prepName + 
				     "> which is not in task :\n" + t);

    return pp.getIndirectObject();
  }

  /**
   * @return true if Task <code>t</code> has <code>prepName</code> on list of 
   *         its prepphrases.
   */
  public boolean hasPrepNamed (Task t,
			       String prepName) {
    Enumeration prepphrases = t.getPrepositionalPhrases();
    return (getPrepNamed (prepphrases, prepName) != null);
  }
  
  /**
   * Utility method extracting a PrepositionalPhrase from a
   * task
   * @param t task with preps
   * @param prepName the String name of the Preposition
   * @return the preposition named prepName
   */
  public PrepositionalPhrase getPrepNamed (Task t,
						  String prepName) {
    return getPrepNamed (t.getPrepositionalPhrases (), prepName); 
  }

  /**
   * Utility method extracting a PrepositionalPhrase from an
   * Enumeration of PrepositionalPhrases.
   * @param prepphrases an Enum of PrepositionalPhrases
   * @param prepName the String name of the Preposition
   * @return the preposition named prepName
   */
  public PrepositionalPhrase getPrepNamed (Enumeration prepphrases, 
						  String prepName) {
    while (prepphrases.hasMoreElements()) {
      PrepositionalPhrase pp = (PrepositionalPhrase) prepphrases.nextElement();
      if (pp.getPreposition().equals(prepName))
	return pp;
    }
    return null;
  }

  /** 
   * Add a prepositionalPhrase to a task with replacement.
   * Changes list of phrases on task.
   * Does NOT require the existence of the prep on the task already, i.e. if
   * can't replace, just adds.
   * 
   */
  public void replacePrepOnTask(Task taskToAddTo, PrepositionalPhrase prepPhrase) {
    if (hasPrepNamed(taskToAddTo, prepPhrase.getPreposition()))
      removePrepNamed(taskToAddTo, prepPhrase.getPreposition());

    addPrepToTask(taskToAddTo, prepPhrase);
  }

  /**
   * Add a prepositionalPhrase to a task. Changes list of phrases on task.
   *
   * @param tasktoAddTo task to add prep phrase to
   * @param prepPhrase to add to list of prep phrases on task
   */
  public void addPrepToTask (Task taskToAddTo, PrepositionalPhrase prepPhrase) {
    // Save all of the old prep phrases of the task
    List newPPs = getPrepListPlusPhrase (taskToAddTo, prepPhrase);
    ((NewTask)taskToAddTo).setPrepositionalPhrases(Collections.enumeration(newPPs));
  }

  /**
   * Adds prepositionalPhrases to a task. Changes list of phrases on task.
   * Risky to use because it does not check whether the added Preps already exist.
   *
   * @param taskToAddTo task to add prep phrase to
   * @param preps Vector of PrepositionalPhrases to add to list of prep phrases on task
   */
  public void addPreps (Task taskToAddTo, Vector preps) {
    List newPPs = UTILAllocate.enumToList(taskToAddTo.getPrepositionalPhrases());
    for (int i=0; i < preps.size(); i++)
      newPPs.add(preps.elementAt(i));
    ((NewTask)taskToAddTo).setPrepositionalPhrases(Collections.enumeration(newPPs));
  }

  /**
   * Return list of phrases on task plus added one.  
   *
   * Does NOT change original task.
   *
   * @param task to get prep phrases from
   * @param prepPhrase to add to list of prep phrases
   * @return List with given prep phrase added
   */
  public List getPrepListPlusPhrase (Task task, PrepositionalPhrase prepPhrase) {
    // Save all of the old prep phrases of the task
    List newPPs = UTILAllocate.enumToList(task.getPrepositionalPhrases());
    // Then add the new one
    newPPs.add(prepPhrase);
    return newPPs;
  }

  /**
   * Removes a PrepositionalPhrase from a task's
   * set of PrepositionalPhrases.
   *
   * @param taskToChange task to remove the prep from
   * @param prepName the String name of the Preposition to remove
   * @return the PrepositionalPhrase named prepName that was removed
   */
  public PrepositionalPhrase removePrepNamed (Task taskToChange, String prepName) {
    // Save all of the old prep phrases of the task
    List newPrepPhrases = new ArrayList ();
    PrepositionalPhrase returnedPrep = null;

    Enumeration prepphrases = taskToChange.getPrepositionalPhrases();
    while (prepphrases.hasMoreElements()) {
      PrepositionalPhrase pp = (PrepositionalPhrase) prepphrases.nextElement();
      if (pp.getPreposition().equals(prepName))
	returnedPrep = pp;
      else
	newPrepPhrases.add (pp);
    }

    ((NewTask) taskToChange).setPrepositionalPhrases (Collections.enumeration(newPrepPhrases));

    return returnedPrep;
  }

  /**
   * Removes a series of PrepositionalPhrases from a task's
   * set of PrepositionalPhrases.
   *
   * @param taskToChange task to remove the prep from
   * @param preps a Vector of String names of Prepositions to remove
   */
  public void removePreps (Task taskToChange, Vector preps) {
    // Save all of the old prep phrases of the task
    List newPrepPhrases = new ArrayList ();

    Enumeration prepphrases = taskToChange.getPrepositionalPhrases();
    while (prepphrases.hasMoreElements()) {
      PrepositionalPhrase pp = (PrepositionalPhrase) prepphrases.nextElement();
      if (!preps.contains(pp.getPreposition()))
	newPrepPhrases.add(pp);
    }
    
    ((NewTask) taskToChange).setPrepositionalPhrases (Collections.enumeration(newPrepPhrases));
  }

  /**
   * Utility methods for creating a PrepositionalPhrases 
   *
   * Uses a cache - only makes unique prepositional phrases
   *
   * Uses the prepToIndirectMap cache so only distinct instances are created.
   *
   * @param ldmf the PlanningFactory
   * @param prep the Preposition
   * @param indirectObject the indirect object - any generic Object
   * @return the new PrepositionalPhrase
   */
  public PrepositionalPhrase makePrepositionalPhrase(PlanningFactory ldmf,
						     String prep,
						     Object indirectObject) {
    Map indirectToPhrase = (Map) prepToIndirectMap.get (prep);
    if (indirectToPhrase == null) {
      if (CACHE_STYLE == 1) {
	indirectToPhrase = new HashMap(CACHE_SIZE);
      } else {
	indirectToPhrase = new LRUCache(CACHE_SIZE);
      }

      prepToIndirectMap.put (prep, indirectToPhrase);
    }

    PrepositionalPhrase phrase = (PrepositionalPhrase) indirectToPhrase.get (indirectObject);

    if (phrase == null) {
      NewPrepositionalPhrase npp = ldmf.newPrepositionalPhrase();
      npp.setPreposition(prep);
      npp.setIndirectObject(indirectObject);
      indirectToPhrase.put (indirectObject, npp);
      phrase = npp;
    }

    return phrase;
  }
}
