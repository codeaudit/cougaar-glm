/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.plan;

import java.io.*;
import java.net.*;
import java.util.*;

/*  11/30/99

Relations Test program;  getOrganizationRelationships to be inserted into SocietyUI.java.


javac ui\querychannel\RelationsTest.java

java ui.querychannel.RelationsTest "http://haines:5550"

*/

public class RelationsTest  {


       public static Vector getOrganizationRelationships(String host_cluster_URL, String OrganizationUID)
       {
        String line;
              Vector itins = new Vector();
              try {
/*
            String urlstring = host_cluster_URL
                     + "/alpine/demo/UIASSETS.PSP?ORG_ASSET_RELATIONS?IS_HOST";
                     ///////+ "?ORGID="+OrganizationUID;

            System.out.println("Fetching "+urlstring);
            InputStream in = null;

            URL url = new URL(urlstring);
            URLConnection connection = url.openConnection();
            in = connection.getInputStream();

            ObjectInputStream objIn = new ObjectInputStream(in);
            Object obj = objIn.readObject();
            if (obj instanceof Vector) {
               itins = (Vector)obj;
               if( OrganizationUID != null) itins = filterByOrgUID(itins,OrganizationUID);
            }
*/

          URL url = new URL(host_cluster_URL + "/cmd/relations");
          URLConnection uc = url.openConnection();
          BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
    
          while ((line = in.readLine()) != null) {
            // Relation  3ID Supporting MCCGlobalMode 3ID/2 MCCGlobalMode/6
            StringTokenizer st = new StringTokenizer(line);
            if (!st.hasMoreElements())  return null;
            String cmd = st.nextToken();
            if (cmd.equals("Relation") && st.hasMoreElements())  {
               UIOrgRelationship relation = new UIOrgRelationship();
    
               relation.setProviderOrganizationCID(st.nextToken());
               relation.setRelationship(st.nextToken());
               relation.setTargetOrganizationCID(st.nextToken());
    
               relation.setProviderOrganizationUID(st.nextToken());
               relation.setTargetOrganizationUID(st.nextToken());
    
               itins.addElement(relation);
               }
            System.out.println(line);
            }


               } catch (EOFException eof) {
                   // ignore
               } catch (Exception e ) {
                   System.out.println("Exception on SocietyUI.getOrganizationRelationships()");
                   e.printStackTrace();
               }
               return itins;
       }


  public static void main(String[] args) {
    RelationsTest s = new RelationsTest();
    System.out.println(s.getOrganizationRelationships(args[0], null));
    }

  }
