/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

import java.util.Hashtable;
import java.util.Map;
import java.util.Enumeration;

/**
 * A class that stores items, keying them by a generated UID.
 **/
public class UIDHashtable extends Hashtable{

    //CLASS VARIABLES:
    //////////////////
    private int nextUID = 0;

    //CONSTRUCTORS
    //////////////

    public UIDHashtable(){
	super();
    }

    public UIDHashtable(int initialCapacity){
	super(initialCapacity);
    }
    
    public UIDHashtable(int initialCapacity, float loadFactor){
	super(initialCapacity, loadFactor);
    }

    public UIDHashtable(Map t){
	super(t);
    }

    // OVERLOADED FUNCTIONS:
    ////////////////////////

    /**
     * Function puts a new key/value pair in the table.  The key
     * Better be a Integer that represents a UID that is not already
     * in the table or an IllegalArgumentException is thrown.  The
     * value better not be in the table already under a different key.
     * (multiple assignements to the same key are permitted though).
     **/
    public synchronized Object put(Object key, Object value)
	throws IllegalArgumentException {

	if(! (key instanceof Integer))
	    throw new IllegalArgumentException("UIDHashTable.put(key,value): key must be an Integer.");
	Object oldValue = get(key);
	if(oldValue != null && oldValue.equals(value))
	    return oldValue;
	else if(oldValue != null)
	    throw new IllegalArgumentException("UIDHashTable.put(key,value): key must be a valid UID -- " +
					       "Key already present in table.");
	else if(containsValue(value))
	    throw new IllegalArgumentException("UIDHashTable.put(key,value): value aready in table under" +
					       "a different key.  Value can only in table once.");
	Object ret = super.put(key,value);
	ensureNextUIDIsUnique();
	return ret;
    }

    // CLASS FUNCTIONS:
    ///////////////////

    /**
     * Use this function to put an object in the table, and let the table
     * determine the approriate UID
     * @param value The object to put in the table.  Note value <B>CAN NOT</B>
     * be null.
     * @return an Integer that was used as the key.
     **/
    public synchronized Integer put(Object value){
	Integer ret = keyForValue(value);
	if(ret != null)
	    return ret;
	Integer key = new Integer(nextUID);
	put(key, value);
	ensureNextUIDIsUnique();
	return key;
    }

    /**
     * Call this to make sure the nextUID variable is set properly for the next
     * call to put(Object)
     **/
    private synchronized void ensureNextUIDIsUnique(){
	while(get(new Integer(nextUID)) != null){
	    nextUID++;
	    if(nextUID == Integer.MAX_VALUE)
		nextUID=Integer.MIN_VALUE;
	}
    }

    /**
     * Return the key for the given value
     * @param value to look for
     * @return the key for the first entry found that matches the given value
     **/
    public synchronized Integer keyForValue(Object value){
	Enumeration enum = keys();
	Integer key;
	while(enum.hasMoreElements()){
	    key = (Integer) enum.nextElement();
	    if(get(key).equals(value))
		return key;
	}
	return null;
    }
}
