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

/**
 * Wraps Java's random number generator class. This is a 
 * singleton class, to get a reference to the singleton 
 * instance use the instanceOf() method.
 */

package org.cougaar.lib.util;
import java.util.Random;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.LoggerFactory;

public final class UTILRandom extends Random {
  private static Logger logger=LoggerFactory.getInstance().createLogger("UTILRandom");

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

    logger.debug("::: Test for UTILRandom class :::");

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
	    
      logger.debug("Number of HEADS: " + counter[0]);
      logger.debug("Number of TAILS: " + counter[1]);

      counter[0] = 0;
      counter[1] = 0;
      counter[2] = 0;
      counter[3] = 0;

      r = instanceOf(1);
      logger.debug("");
    }

    r = UTILRandom.instanceOf(500);
    // The follwing throws a runtime exception by design
    // x = r.randomInt(0);
    // uncomment to tet it.
    for(int i = 0; i < 5; i++){
      x = r.randomInt(1);
      if(x != 0)
	logger.debug("failed");
      else
	logger.debug("OK");
    }
    logger.debug("");
	    

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
	  logger.debug("there is an error " + x);
	  logger.debug("we should never be here");
	  break;
	}
      }

      logger.debug("Number of  0: " + counter[0]);
      logger.debug("Number of  1: " + counter[1]);
      logger.debug("Number of  2: " + counter[2]);
      logger.debug("Number of  3: " + counter[3]);
	  
      // TESTING randomInt(int, int) Function
      counter[0] = 0;
      counter[1] = 0;
      counter[2] = 0;
      counter[3] = 0;
      logger.debug("");

      r = UTILRandom.instanceOf(1000);
      logger.debug("");
    }

    r = UTILRandom.instanceOf(1500);
    // The follwing throws a runtime exception by design
    //x = r.randomInt(5,4);
    // uncomment to test it.
    for(int i = 0; i < 5; i++){
      x = r.randomInt(4,4);
      if(x != 4)
	logger.debug("failed");
      else
	logger.debug("OK");
    }
    logger.debug("");
	    
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
	  logger.debug("there is an error "+ x);
	  logger.debug("we should never be here");
	  break;
	}
      }

      logger.debug("Number of  100: " + counter[0]);
      logger.debug("Number of  101: " + counter[1]);
      logger.debug("Number of  102: " + counter[2]);
      logger.debug("Number of  103: " + counter[3]);
	    
      // TESTING randomInt(int, int) Function
      counter[0] = 0;
      counter[1] = 0;
      counter[2] = 0;
      counter[3] = 0;
      logger.debug("");
	    
      r = UTILRandom.instanceOf(2000);
    }
  }
}
