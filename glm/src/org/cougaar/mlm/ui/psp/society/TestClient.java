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
import java.net.*;
import java.util.*;

import org.cougaar.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.mlm.ui.planviewer.XMLClientConfiguration;

/**
  * TestClient
  * Grab-bag sample code to test client-side interactions with PlanServer
  * Switches to different methods based on command line keyword
  **/

public class TestClient
{
    public TestClient()
    {
    }


   public void testKEEPALIVE()
    {
         System.out.println("Testing KeepAlive....");
         try{

            URL url = new URL("http://localhost:5555/alpine/demo/KEEPALIVE.PSP");
            URLConnection uc = url.openConnection();

            uc.setDoInput(true);

            InputStream is = uc.getInputStream();


            byte b[] = new byte[1];
            int len;
            int count=0;
            while( (len = is.read(b,0,1)) > -1 )
            {
               System.out.write(b,0,len);
               count++;
            }

         } catch (Exception e)
         {
            e.printStackTrace();
         }
    }

    public void testPOSTECHO()
    {
         System.out.println("Testing Post + Echo....");
         try{
            URL url = new URL("http://localhost:5555/?ECHO"); // ?QUERY?POST");
            URLConnection uc = url.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            OutputStream os = uc.getOutputStream();
            PrintStream servicePrint = new PrintStream(os);
            String postmsg = "123456789  RANDOMTEXT";

            // Test, POST STRING directly, post String after conversion to bytes
            byte pb[] = postmsg.getBytes();
            servicePrint.println(postmsg);
            servicePrint.write(pb);
            servicePrint.println("");

            // Test, post byte values only,
            //
            pb = new byte[30];
            int i;
            for(i=0; i< 30; i++) pb[i] = (byte)i;
            servicePrint.write(pb);
            servicePrint.flush();
            os.close();

            InputStream is = uc.getInputStream();
            byte b[] = new byte[512];
            int len;
            while( (len = is.read(b,0,512)) > -1 )
            {
               System.out.write(b,0,len);
            }


         } catch (Exception e)
         {
            e.printStackTrace();
         }
    }


     public void serviceStressTest(int iterations)
    {
      Vector clustersCache = new Vector(4);
      // tic-22
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterGround/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterSea/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterAir/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterFort/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$GlobalAir/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$GlobalSea/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$3-69-ARBN/alpine/demo/TASKS.PSP?MODE=1?VERB=Transport"));

      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterGround/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterSea/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterAir/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$TheaterFort/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$GlobalAir/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$GlobalSea/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));
      clustersCache.addElement(new String("http://4.22.165.23:5555/$3-69-ARBN/alpine/demo/ITINERARY.PSP?FORMAT=XML?MODE=LIVE"));


      long start = System.currentTimeMillis();
      long totalBytes = 0;
      
      for(int i=0; i< iterations; i++)
      {
        long now = System.currentTimeMillis();
        long interval = now - start;
        long minutes = (long)((interval / 1000) / 60);

      	// Connect to target cluster's URL
	      ConnectionHelper xmlconn = null;
	      byte[] reply = null;
        try {
              int idx = (int)(Math.random() * clustersCache.size());
              String urlstr = (String)clustersCache.elementAt(idx);


              System.out.println("====================================================");
              System.out.println("=========> TestClient ATTEMPTING URL=" + urlstr);
              System.out.println("=========> ITERATIONS =" + i + "/" + iterations
                        + " MINUTES=" + minutes
                        + " CUMULATIVE_BYTES=" + totalBytes );

              URL url = new URL(urlstr);
              URLConnection uc = url.openConnection();
              uc.setDoInput(true);
              InputStream is = uc.getInputStream();
              byte b[] = new byte[1024];
              int len;
              long sum = 0;
              boolean wroteOnce = false;
              while( (len = is.read(b,0,1024)) > -1 )
              {
                 if( wroteOnce == false) {
                        System.out.write(b,0,100);
                        wroteOnce = true;
                 }
                 sum += len;
              }
             totalBytes += sum;
             System.out.println("\n=========> BYTES =" + sum );
             System.out.println("====================================================\n");


        } catch (Exception e) {
		          System.err.println(e);
	      	    e.printStackTrace();
        }

       }

    }

    public static void main(String argv[]){

         String argv0 = null;
         if(argv.length <= 0) {
             System.out.println("Must provide parameter.  Options: POST");
             System.exit(0);
         }
         argv0 = argv[0];

         System.out.println("TestClient main()... argv[0]=" + argv0);

         //try{
           //URL u1 = new URL("http://www.ultranet.com/~ncombs/nate.shtml");
           //URL u2 = new URL("http://www.ultranet.com");
           //URL u3 = new URL("http://www.ultranet.com/");

           //System.out.println("u1=" + u1.toExternalForm() + " " + u1.getFile());
           //System.out.println("u2=" + u2.toExternalForm() + " " + u2.getFile());
           //System.out.println("u3=" + u3.toExternalForm() );
         //} catch (Exception e) { e.printStackTrace(); }

         TestClient tc = new TestClient();

         if( argv0.toUpperCase().startsWith("POST") )
         {
             tc.testPOSTECHO();
         }
         else if( argv0.toUpperCase().startsWith("KEEPALIVE") )
         {
             tc.testKEEPALIVE();
         }
         else if( argv0.toUpperCase().startsWith("STRESS") )
         {
             tc.serviceStressTest(30000);
         }


         try{ Thread.sleep(1000); } catch (Exception e) { e.printStackTrace(); }
    }
}
