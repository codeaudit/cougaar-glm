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

/**
 * Wraps Java's random number generator class. This is a 
 * singleton class, to get a reference to the singleton 
 * instance use the instanceOf() method.
 */

package org.cougaar.lib.util;
import java.util.Random;

public final class UTILRandom extends Random {

    /** 
     * Get an instance of the class
     * @return UTILRandom
     */
    public static UTILRandom instanceOf(){
	if(my_instance == null)
	    my_instance = new UTILRandom();
	return my_instance;
    }

    /** 
     * Get an instance of the class, if an instance of the class
     * already exists it will just reseed.
     * @return UTILRandom
     */
    public static UTILRandom instanceOf(long seed){
	if(my_instance == null)
	    my_instance = new UTILRandom(seed);
	else
	    my_instance.setSeed(seed);
	return my_instance;
    }

    /** 
     * Pseudo-random number between 0 and range-1 
     * @return int, ArithmeticException if range is 0
     */
    public int randomInt(int range) { 
	if(range == 0)
	    throw new ArithmeticException("this does not make sense");
	else if(range == 1)
	    return 0;
	else
	    return Math.abs(this.nextInt()) % range;
    }

    /** 
     * Filps a coin, true == head, false == tails 
     * @return boolean
     */
    public boolean flipCoin(){
	return(this.randomInt(2) == 1);
    }

    /** 
     * Computes a pseudo-random integer between min and max 
     * @return int, ArithmeticException is min > max
     */
    public int randomInt(int min, int max) {
	if(max < min)
	    throw new ArithmeticException("this does not make sence");
	else if(max == min)
	    return min;
	else
	    return this.randomInt(max-min+1) + min;
    }

    // noone should be accessing the contructors
    private UTILRandom(long seed){ super(seed); }
    private UTILRandom(){ super(); }
    private static UTILRandom my_instance = null;

    protected void finalize(){
	my_instance = null;
    }

    /** 
     * TEST CODE 
     */
    public static void main(String[] argc){
	int[] counter = new int[4];
	int x;
	boolean b;
	UTILRandom r = instanceOf();
      
	counter[0] = 0;
	counter[1] = 0;
	counter[2] = 0;
	counter[3] = 0;

	System.out.println("::: Test for UTILRandom class :::");

	// test several times to ensure 
	// predictability after seeding
	for(int j = 0; j < 3; j++){

	    // TESTING flipCoin Function
	    for(int i = 0; i < 100000; i++){
		b = r.flipCoin();
		if(b)
		    counter[0]++;
		else
		    counter[1]++;
	    }
	    
	    System.out.println("Number of HEADS: " + counter[0]);
	    System.out.println("Number of TAILS: " + counter[1]);

	    counter[0] = 0;
	    counter[1] = 0;
	    counter[2] = 0;
	    counter[3] = 0;

	    r = instanceOf(1);
	    System.out.println("");
	}

	r = UTILRandom.instanceOf(500);
	// The follwing throws a runtime exception by design
	// x = r.randomInt(0);
	// uncomment to tet it.
	for(int i = 0; i < 5; i++){
	    x = r.randomInt(1);
	    if(x != 0)
		System.out.println("failed");
	    else
		System.out.println("OK");
	}
	System.out.println("");
	    

	// TESTING RandomIint(int) Function
	// test several times to ensure 
	// predictability after seeding
	for(int j = 0; j < 3; j++){
	    for(int i = 0; i < 100000; i++){
		x = r.randomInt(4);
		switch(x){
		case 0:
		    counter[0]++;
		    break;
		case 1:
		    counter[1]++;
		    break;
		case 2:
		    counter[2]++;
		    break;
		case 3:
		    counter[3]++;
		    break;
		default:
		    System.out.println("there is an error " + x);
		    System.out.println("we should never be here");
		    break;
		}
	    }

	    System.out.println("Number of  0: " + counter[0]);
	    System.out.println("Number of  1: " + counter[1]);
	    System.out.println("Number of  2: " + counter[2]);
	    System.out.println("Number of  3: " + counter[3]);
	  
	    // TESTING randomInt(int, int) Function
	    counter[0] = 0;
	    counter[1] = 0;
	    counter[2] = 0;
	    counter[3] = 0;
	    System.out.println("");

	    r = UTILRandom.instanceOf(1000);
	    System.out.println("");
	}

	r = UTILRandom.instanceOf(1500);
	// The follwing throws a runtime exception by design
	//x = r.randomInt(5,4);
	// uncomment to test it.
	for(int i = 0; i < 5; i++){
	    x = r.randomInt(4,4);
	    if(x != 4)
		System.out.println("failed");
	    else
		System.out.println("OK");
	}
	System.out.println("");
	    
	// TESTING RandomIint(int) Function
	// test several times to ensure 
	// predictability after seeding
	for(int j = 0; j < 3; j++){
	    
	    for(int i = 0; i < 100000; i++){
		x = r.randomInt(100, 103);
		switch(x){
		case 100:
		    counter[0]++;
		    break;
		case 101:
		    counter[1]++;
		    break;
		case 102:
		    counter[2]++;
		    break;
		case 103:
		    counter[3]++;
		    break;
		default:
		    System.out.println("there is an error "+ x);
		    System.out.println("we should never be here");
		    break;
		}
	    }

	    System.out.println("Number of  100: " + counter[0]);
	    System.out.println("Number of  101: " + counter[1]);
	    System.out.println("Number of  102: " + counter[2]);
	    System.out.println("Number of  103: " + counter[3]);
	    
	    // TESTING randomInt(int, int) Function
	    counter[0] = 0;
	    counter[1] = 0;
	    counter[2] = 0;
	    counter[3] = 0;
	    System.out.println("");
	    
	    r = UTILRandom.instanceOf(2000);
	}
    }
}





