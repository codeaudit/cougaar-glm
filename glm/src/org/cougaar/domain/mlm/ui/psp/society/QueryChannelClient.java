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

import org.cougaar.domain.planning.ldm.plan.Location;
import org.cougaar.domain.planning.ldm.plan.ScheduleElement;
import org.cougaar.lib.planserver.*;

import org.cougaar.domain.glm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

public class QueryChannelClient {

    private static boolean verboseFlag = true;

    public static void setVerbose( boolean v ){ verboseFlag = v; }

    public static void main(String argv[]){

         String argv0 = null;
         String argv1 = null;
         if(argv.length <= 1) {
             System.out.println("Must provide parameter: Host URL");
             System.out.println("Must provide query type: TRANSPORT_TRACE, GROUND_LINKS");
             System.exit(0);
         }
         argv0 = new String( argv[0] );
         argv1 = new String( argv[1] );

         String hostURL = argv0;

         System.out.println("host URL = " + argv0);
         if( argv0.endsWith("/") != false ) {
             System.out.println("NO TRAILING BACKSLASHES:" + argv0);
             System.exit(0);
         }
         int query_type = 0;
         if( argv1.toUpperCase().startsWith("TRANSPORT_TRACE") ) {
             query_type =1;
         }
         else if( argv1.toUpperCase().startsWith("GROUND_LINKS") ) {
             query_type =2;
         }

         UITTNode rootnode = null;

         if( query_type == 1 ) // TRANSPORT_TRACE : trace transport tasks...
         {
             String urlstring = hostURL + "/alpine/demo/QUERYTRANSPORT.PSP";
             rootnode = readURL( hostURL, urlstring, null, new Hashtable());
         }
         else {
             System.out.println("UNRECOGNIZED QUERY TYPE: " + argv1 );
             System.exit(0);
         }

         printResults(rootnode, System.out, "  ");
         System.out.println("(Leaves fetched over connection)=" + countResults
                             + " (Itineraries=" + countItinerary + ")");
         System.out.println("(countConnections)(new connections)=" + countConnections);
    }


    /** Returns Vector of STrings **/
    public static Vector readTxTaskUIDs( String host_cluster_URL )
    {
      Vector v = new Vector();
      try{
         String urlstring =
                      host_cluster_URL
                      + "/alpine/demo/QUERYTRANSPORT.PSP?TX_TASK_UIDS_ONLY?FORMAT=XML";

         URL url = new URL(urlstring);
         //System.out.println("POST= " + post);
         System.out.println("URL = " + url.toExternalForm() );

         URLConnection uc = url.openConnection();
         countConnections++;

         uc.setDoInput(true);
         InputStream in = uc.getInputStream();

         org.w3c.dom.Element root =
               org.cougaar.util.XMLObjectFactory.readXMLRoot(in);
         Object obj = org.cougaar.util.XMLObjectFactory.parseObject(null, root);

         //ObjectInputStream objIn = new ObjectInputStream(in);
         //Object obj = null;

         if( obj instanceof Vector ) {
            Vector vec = (Vector)obj;
            Enumeration en = vec.elements();

            while( en.hasMoreElements() )
            {
               Object elem = en.nextElement();
               //System.out.println("Reading object: " + elem.getClass().getName() );

               if (elem instanceof String) {
                  String st = ((String)elem);
                  v.addElement(st);
               }
            } // end while
         } // end if
       } catch ( java.io.EOFException eof ) {
          System.out.println("Pipe closed");
       } catch (Exception ex ) {
          ex.printStackTrace();
       }
       return v;
    }


    /** Returns Organization/Unit information of Cluster identified by URL **/
    public static UIUnit readMilOrgUnit( String host_cluster_URL )
    {
      UIUnit unit = null;
      boolean useXMLForMilOrg = true;
      try{
         String urlstring =
                      host_cluster_URL
                      + "/alpine/demo/QUERYTRANSPORT.PSP?MIL_ORG_ONLY?FORMAT="
                      + (useXMLForMilOrg ? "XML" : "DATA");

         URL url = new URL(urlstring);
         //System.out.println("POST= " + post);
         System.out.println("URL = " + url.toExternalForm() );

         URLConnection uc = url.openConnection();
         countConnections++;

         uc.setDoInput(true);
         InputStream in = uc.getInputStream();

         Object obj = null;
         if( useXMLForMilOrg == true )
         {
             org.w3c.dom.Element root =
                  org.cougaar.util.XMLObjectFactory.readXMLRoot(in);
             if( root != null )
                  obj = org.cougaar.util.XMLObjectFactory.parseObject(null, root);
             else throw new RuntimeException("NULL XML ROOT");
         } else {
            ObjectInputStream objIn = new ObjectInputStream(in);
            obj = objIn.readObject();
	          objIn.close();
         }

         //ObjectInputStream objIn = new ObjectInputStream(in);
         //Object obj = null;


         if( obj instanceof UITTException ) {
                  System.out.println("UITTException=" + ((UITTException)obj).getString());
         }
         else if (obj instanceof UIUnit) {
                  unit = (UIUnit) obj;
         }
         else {
             if( obj != null ) System.out.println("[readMilOrgUnit] Obtained object type = "
                                 + obj.getClass().getName() );
             else System.out.println("[readMilOrgUnit] Obtained null object");
         }


       } catch ( java.io.EOFException eof ) {
          System.out.println("Pipe closed");
       } catch (Exception ex ) {
          ex.printStackTrace();
       }
       return unit;
    }



    public static UITTNode readURL(String hostURL, String urlstring, String post, Hashtable queryCache)
    {
        UITTNode node = new UITTNode(urlstring);
        Vector v = readURLFromConnection(hostURL, urlstring, post, queryCache);
        node.copyIntoChildren(v);
        return node;
    }


    private static int countConnections=0;
    private static int countResults =0;
    private static int countItinerary =0;

    private static Vector readURLFromConnection(String hostURL, String urlstring,
                              String post, Hashtable queryCache)
    {
         Vector results = new Vector();
         String buffer = new String();
         Object obj = null;
         try{
            URL url = new URL(urlstring);
            //System.out.println("POST= " + post);
            System.out.println("URL = " + url.toExternalForm() );

            URLConnection uc = url.openConnection();
            countConnections++;

            uc.setDoInput(true);
            if( post != null ) {
                uc.setDoOutput(true);
                OutputStream os = uc.getOutputStream();
                os.write(post.trim().getBytes());
                os.close();
            }
            
            InputStream in = uc.getInputStream();
            ObjectInputStream objIn = new ObjectInputStream(in);


            while(  (obj = objIn.readObject()) != null)
               {
                  ///System.out.println("Reading object: " + obj.getClass().getName() );
                  
                  if( obj instanceof String ){
                     System.out.println("<STRING>" + (String)obj + "</STRING>" );
                  }
                  else if( obj instanceof UITTMarker ){
                     System.out.println("UITTMarker: " + ((UITTMarker)obj).toString() );
                  }
                  else if( obj instanceof UITTException )
                  {
                       UITTLeaf leaf = new UITTLeaf(urlstring);
                       leaf.setValue(obj);
                       // clip string to first 40 chars ... to limit output
                       System.out.println("UITTException=" + ((UITTException)obj).getString()); //.substring(0,40) );
                       results.addElement( leaf );
                       countResults++;
                  }
                  else if( obj instanceof UITTAnswer )
                  {
                       UITTLeaf leaf = new UITTLeaf(urlstring);
                       leaf.setValue(obj);
                       //System.out.println("UITTAnswer=" + ((UITTAnswer)obj).getString() );
                       results.addElement( leaf );
                       countResults++;
                  }
                  else if( obj instanceof UITTReference )
                  {
                       //System.out.println("UITTReference=" + ((UITTReference)obj).getString() );
                       String urlstr = ((UITTReference)obj).extractParameter("URL:");
                       if( urlstr != null ) {
                          addQuery(hostURL, urlstr, queryCache );
                       }
                  }

                   else if( obj instanceof UIItinerary ) {

                       UITTLeaf leaf = new UITTLeaf(urlstring);
                       leaf.setValue(obj);
                       results.addElement( leaf );
                       countResults++;
                       countItinerary++;
                  }
               } // end while

         } catch ( java.io.EOFException eof ) {
            System.out.println("Pipe closed.");
         }
         catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION HANDLED: LAST OBJECT RETURNED=" + obj.getClass().toString() );
            if(obj instanceof UIString ) {
                System.out.println("EXCEPTION HANDLED: LAST VALUE=" + ((UIString)obj).getString());
            }
         }
         Vector vec = doQueries(hostURL, queryCache);
         copyVector(results, vec);

         return results;
    }

    /**
      * Extracts TAG:XXXXXXXXXXX;   from row,  Row = "data record" returned from PSP
      **/
    //#####################################################################
    private static Vector doQueries(String hostURL, Hashtable queryCache)
    {
        Vector results = new Vector();
        Enumeration en = queryCache.keys();
        while( en.hasMoreElements() ) {
           String host_plus_redirect = (String)en.nextElement();
           //
           //   Make sure that the cluster id is known.   PSP at server inserts
           //   UNKNOWN_CLUSTERID_MARKER
           //
           if( host_plus_redirect.toUpperCase().indexOf(UITTConstants.UNKNOWN_CLUSTERID_MARKER)
                == -1 )
           {
              Vector v = (Vector)queryCache.get(host_plus_redirect);
              String batchQuery = new String();
              Enumeration en2 = v.elements();
              while( en2.hasMoreElements() ) {
                  String q = (String)en2.nextElement();
                  String p = QueryChannelHelper.parseURLParams(q);
                  if( p != null) {
                     batchQuery += q + "^";
                     //System.out.println("POSTLN=" + q );
                  }
              } // end while (more queries -- per key)

              UITTNode node = readURL( hostURL, host_plus_redirect + "alpine/demo/QUERYTRANSPORT.PSP?BATCH",
                              batchQuery, new Hashtable());
              results.addElement(node);
           } else {
              Vector v = (Vector)queryCache.get(host_plus_redirect);
              System.out.println(
                   "Number of URLs marked as UITTConstants.UNKNOWN_CLUSTERID_MARKER ="
                   + v.size() );
           }
        } // end while (more keys)
        queryCache.clear();
        return results;
    }

    /**
      * hostURL : url of host, may include $redirect.
      *
      * url_resource:  that can answser the reference question.  May start with $redirect
      *     if referencing logplan outside of the cluster which created it.
      **/
    private static void addQuery( String hostURL, String url_resource, Hashtable queryCache )
    {
         String redirect = QueryChannelHelper.parseRedirectSubstring(url_resource);
         /** Add any redirect required by reference url **/
         String host_plus_reference_redirect = hostURL + redirect;
         String urlstring = null;

         if( hostURL.endsWith("/") && url_resource.startsWith("/") ) {
             urlstring = hostURL + url_resource.substring(1);
         } else {
             urlstring = hostURL + url_resource;
         }
         
         //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>redirectHostURL(" + redirect + ")=" + host_plus_redirect);
         //System.out.println("       >>>>>>>>>>>>>>>>>>>>urlstring=" + urlstring);

         Vector v = null;
         if( host_plus_reference_redirect != null) {
              v = (Vector)queryCache.get(host_plus_reference_redirect);
         }
         if( v == null ) {
              v = new Vector();
              v.addElement(urlstring);
              queryCache.put(host_plus_reference_redirect,v);
         }
         else{
              v.addElement(urlstring);
         }
    }

    private static Vector copyVector(Vector target, Vector source)
    {
         Enumeration en = source.elements();
         while( en.hasMoreElements() ) {
            Object obj = en.nextElement();
            target.addElement(obj);
         }
         return target;
    }

    public static Vector grabAssets(UITTNode node)
    {
         Vector results = new Vector();

         //out.println("FinalResult>" + prefix + node.toString());
         Enumeration en = node.children.elements();
         while( en.hasMoreElements() ) {
            Object obj = en.nextElement();
            if( obj instanceof UITTLeaf ) {
                  UITTLeaf leaf = (UITTLeaf)obj;
                  String type = (leaf.getValue()).getClass().getName();
                  if( leaf.getValue() instanceof UIItinerary ) {
                      results.addElement(leaf.getValue());
                  }
                  Vector v = grabAssets(leaf );
                  copyVector(results, v);
            }
            else if( obj instanceof UITTNode ){
                  UITTNode child = (UITTNode)obj;
                  Vector v = grabAssets(child );
                  copyVector(results, v);
            }
            else {
                throw new RuntimeException("[grabAssets] Unknown type in results set: " + obj.getClass().getName());
            }
         }
         return results;
    }

    public static void printResults(UITTNode node, PrintStream out, String prefix)
    {
         //out.println("FinalResult>" + prefix + node.toString());
         Enumeration en = node.children.elements();
         while( en.hasMoreElements() ) {
            Object obj = en.nextElement();
            if( obj instanceof UITTLeaf ) {
                  UITTLeaf leaf = (UITTLeaf)obj;
                  String type = (leaf.getValue()).getClass().getName();
                  out.print("FinalResult>" + prefix + "[" + type + "]["
                                + leaf.parent.url + "]"
                                + "[client_oid=" + leaf.hashCode() + "]"
                                );
                  if( leaf.getValue() instanceof UICarrierItinerary ) {
                      UICarrierItinerary carrierItinerary = ((UICarrierItinerary)leaf.getValue());

                      out.println("[ClusterID=" +  carrierItinerary.getClusterID() + "]"
                                  + "{inputTaskUID=" + carrierItinerary.getInputTaskUID() + ")");
                      Enumeration en2 = carrierItinerary.getScheduleElements().elements();
                      while( en2.hasMoreElements() )
                      {
                         UIItineraryElement se = (UIItineraryElement)en2.nextElement();
                         out.println("FinalResult>"
                                      + prefix + "        UIItineraryElement -> {Start/End="
                                      + se.getStartDate() + ", " + se.getEndDate() + "}"
                                      + "{allocation_oid=" + carrierItinerary.getAllocOID() + "}"
                                      + "{From=" +  se.getEndLocation().getGeolocCode()
                                      + ", " + se.getEndLocation().getLatitude()
                                      + ", " + se.getEndLocation().getLongitude()
                                      + "}"
                                      + "{To=" +  se.getStartLocation().getGeolocCode()
                                      + ", " + se.getStartLocation().getLatitude()
                                      + ", " + se.getStartLocation().getLongitude()
                                      + "}"
                                      + "{annotation=" +  carrierItinerary.getAnnotation() + "}"
                                      );

                      }
                  }
                  else out.println("");
                  printResults(leaf, out, prefix + "..." );
            }
            else if( obj instanceof UITTNode ){
                  UITTNode child = (UITTNode)obj;
                  out.println("FinalResult>" + prefix + child.url.toString());
                  printResults(child, out, prefix + "..." );
            }
            else if( obj instanceof UIString ) {
                out.println("FinalResult>" + prefix + obj.toString());
            }
            else {
                throw new RuntimeException("unknown type in results set: " + obj.getClass().getName());
            }
         }
    }

}
