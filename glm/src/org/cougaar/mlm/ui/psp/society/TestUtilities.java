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
 
package org.cougaar.mlm.ui.psp.society;

import java.io.*;
import java.util.*;

public class TestUtilities
{
  static long free =-1;
  static long tot =-1;

  public static void memoryCheck1()
  {
      Runtime rt = Runtime.getRuntime();
      System.gc();
      free = rt.freeMemory();
      tot = rt.totalMemory();
      System.out.println(">>>>>>[memoryCheck1] free/tot=" + free + "/" + tot +" mb");
  }

  /** return increment **/
  public static long memoryCheck2()
  {
      Runtime rt = Runtime.getRuntime();
      System.gc();
      long fr = rt.freeMemory();
      long tm = rt.totalMemory();
      long orig = tot - free;
      long now = tm - fr;
      long inc = now -orig;
      System.out.print(">>>>>>[memoryCheck2] free/tot=" + fr + "/" + tm +" mb, " );
      System.out.println("used increment=" + inc);
      free=-1;
      tot=-1;
      return inc;
  }

  public static long sizeTestElements(Object[] v)
  {
     long byteCount = 0;
     System.out.println(">>>>>>>>>>>>>>>>>>>>>>sizeTestElements, size=" + v.length);
     for(int i=0; i< v.length; i++)
     {
        Object obj = (Object)v[i];
        byteCount += sizeTest(obj);
     }
     return byteCount;
  }

   public static long sizeTestElements(Vector v)
  {
     long byteCount = 0;
     System.out.println(">>>>>>>>>>>>>>>>>>>>>>sizeTestElements, size=" + v.size());
     Enumeration e = v.elements();
     while( e.hasMoreElements() )
     {
        Object obj = e.nextElement();
        byteCount += sizeTest(obj);
     }
     return byteCount;
  }

  public static long sizeTest(Object target)
  {
     long size = 0;
     try{
         // for now we create anew -- later look at reuse...
         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);

         objectStream.writeObject(target);
         objectStream.flush();
         byteStream.close();
         size = byteStream.size();
         System.out.println(">>>>>>>>>>>>>>>>>>>>>>SIZETEST::::  ("
                   + target.getClass().getName() + ", size= "
                   + size +")");
     } catch (Exception e)
     {
        e.printStackTrace();
     }
     return size;
  }
}
