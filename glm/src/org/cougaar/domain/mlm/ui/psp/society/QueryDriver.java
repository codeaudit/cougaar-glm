/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.society;

import java.io.*;
import java.net.*;
import java.util.*;

import com.ibm.xml.parser.Parser;
import com.ibm.xml.parser.Stderr;
import com.ibm.xml.parser.TXDocument;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;
import org.cougaar.domain.mlm.ui.planviewer.XMLClientConfiguration;

import org.w3c.dom.*;

/**
  * Affectionately known as "Query-Bot" -- a test program for exercising
  * LogPlanServer by repeatedly querying for information...  evolving pattern.
  *
  * Grab-bag sample code to test client-side query to PlanServer LogPlan 
  * related PSPs Switches to different methods based on command line keyword
  *
  * Also see "stressWeb" tester, available in ALPINE archives.
  **/

public class QueryDriver
{

    private static String hostURL = new String();
    
    public static void main(String argv[]){


         String argv0 = null;
         String argv1 = null;
         if(argv.length <= 1) {
             System.out.println("arg1. Must provide parameter: Host URL");
             System.out.println("arg2. Must provide query iterations: integer");
             System.exit(0);
         }
         argv0 = new String( argv[0] );
         argv1 = new String( argv[1] );

         hostURL=argv0; // hostURL used elsewhere -- Global Variable

         System.out.println("host URL = " + argv0);
         if( argv0.endsWith("/") != false ) {
             System.out.println("NO TRAILING BACKSLASHES:" + argv0);
             System.exit(0);
         }

         int iterations = Integer.parseInt(argv1);
         queryRandomLPS(iterations);

         try{ Thread.sleep(1000); } catch (Exception e) { e.printStackTrace(); }
    }


    public static void queryRandomLPS(int iterations)
    {
      Vector clustersCache = new Vector(4);

      // clustersCache.addElement(new String("http://128.89.0.202:5555/")); // STOUT

      //clustersCache.addElement(new String("http://localhost:5555/$3ID/"));
      //clustersCache.addElement(new String("http://localhost:5555/$1BDE/"));
      clustersCache.addElement(new String(hostURL + "/$MCCGlobalMode/"));
      clustersCache.addElement(new String(hostURL + "/$GlobalAir/"));
      clustersCache.addElement(new String(hostURL + "/$VirtualAir/"));
      clustersCache.addElement(new String(hostURL + "/$GlobalSea/"));



      //clustersCache.addElement(new String("http://localhost:5556/"));
      //clustersCache.addElement(new String("http://localhost:5557/"));
      //clustersCache.addElement(new String("http://localhost:5558/"));

      /**
      clustersCache.addElement(new String("http://192.1.100.197:5555/"));
      clustersCache.addElement(new String("http://192.1.100.197:5556/"));
      clustersCache.addElement(new String("http://192.1.100.197:5557/"));
      clustersCache.addElement(new String("http://192.1.100.197:5558/"));
      **/


      Vector queryCache = new Vector(7);
      queryCache.addElement(new String("Task"));
      queryCache.addElement(new String("Asset"));
      //queryCache.addElement(new String("PlanElement"));
      //queryCache.addElement(new String("Task.source.address=1BDE"));
      //queryCache.addElement(new String("Task.source.address=3ID"));
      //queryCache.addElement(new String("Task.source.address=MCCGlobalMode"));
      //queryCache.addElement(new String("Task.verb=ReportForDuty & Task.source.address!=3ID"));

      long start = System.currentTimeMillis();
      TestUtilities.memoryCheck1();

      for(int i=0; i< iterations; i++)
      {
        long now = System.currentTimeMillis();
        long interval = now - start;
        long minutes = (long)((interval / 1000) / 60);

      	// Connect to target cluster's URL
	      ConnectionHelper xmlconn = null;
	      byte[] reply = null;
        try {
              int idx = (int)(Math.random() * (double)(clustersCache.size()));
              String url = (String)clustersCache.elementAt(idx);

              idx = (int)(Math.random() * (double)(queryCache.size()));
              String query = (String)queryCache.elementAt(idx);

              System.out.println("====================================================");
              System.out.println("=========> ATTEMPTING URL=" + url);
              System.out.println("=========> ATTEMPTING QUERY=" + query);
              System.out.println("=========> ITERATIONS =" + i + "/" + iterations + " MINUTES=" + minutes );

              //
              // Define a URL connection to a PSP...
              //
		          xmlconn = new ConnectionHelper(url,
					          XMLClientConfiguration.PSP_package,
					          XMLClientConfiguration.UIDataPSP_id);
              System.gc();

              //
              // Post query text back to it...
              //

		          xmlconn.sendData(query);
		          reply = xmlconn.getResponse();


              /**
              // PARSE RESULT == VERIFY XML, COUNT NODES...
              int nlLength=0;
              try{
                Parser p = new Parser("LogPlan");
                TXDocument doc = p.readStream(new ByteArrayInputStream(reply));
                // Get root element of document
                Element root = doc.getDocumentElement();
                // Count UITask/UIAsset nodes
                NodeList nl = root.getElementsByTagName("UITask");
                nlLength = nl.getLength();
                nl = root.getElementsByTagName("UIAsset");
                nlLength += nl.getLength();
              } catch (Exception ex ) {
                  System.out.println("XML ERROR.................................");
                  ex.printStackTrace();
              }
              System.out.println("=========> RETURNED (nodelist length) =" + nlLength);
              **/
              System.out.println("====================================================");

              //
              // Output the response to console...
              //
              int len = reply.length;
              System.out.write(reply,0,len);
              
              //try{ Thread.sleep(50); } catch (Exception e) { e.printStackTrace(); }

        } catch (Exception e) {
		          System.err.println(e);
	      	    e.printStackTrace();
        }
        long inc = TestUtilities.memoryCheck2();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Memory used from baseline=" +  inc);

      } // end for

    }

/**
    public static void allTasks(String clusterurl_string)
    {
       System.out.println("allTasks(String clusterurl_string)..." + clusterurl_string);
         try{
            URL url = new URL(clusterurl_string + "alpine/demo/UIDATA.PSP");
            URLConnection uc = url.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            OutputStream os = uc.getOutputStream();
            PrintStream servicePrint = new PrintStream(os);
            String postmsg = "LIST_CLUSTERS"; //"Task";

            // Test, POST STRING directly, post String after conversion to bytes
            byte pb[] = postmsg.getBytes();
            servicePrint.println(postmsg);
            servicePrint.write(pb);
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

    public static Vector allClusters()
    {
        Vector retVector = new Vector();

        System.out.println("allClusters()...");
         try{
            URL url = new URL("http://localhost:5555/alpine/demo/UIDATA.PSP");
            URLConnection uc = url.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            OutputStream os = uc.getOutputStream();
            PrintStream servicePrint = new PrintStream(os);
            String postmsg = "LIST_CLUSTERS";

            // Test, POST STRING directly, post String after conversion to bytes
            byte pb[] = postmsg.getBytes();
            servicePrint.println(postmsg);
            servicePrint.write(pb);
            os.close();

            InputStream is = uc.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            while(true)
            {
               try{
                   Object obj_clustername = ois.readObject();
                   Object obj_clusterurlstring = ois.readObject();
                   retVector.addElement(obj_clusterurlstring);
                   System.out.println(obj_clustername.toString() + ", " + obj_clusterurlstring.toString());
               } catch (Exception e) {break;}
            }
            //byte b[] = new byte[512];
            //int len;
            //while( (len = is.read(b,0,512)) > -1 )
            //{
            //   System.out.write(b,0,len);
            //}

         } catch (Exception e)
         {
            e.printStackTrace();
         }

         return retVector;
    }
**/

}
