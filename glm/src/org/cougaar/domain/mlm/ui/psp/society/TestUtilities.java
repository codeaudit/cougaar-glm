/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.society;

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
