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

package org.cougaar.mlm.ui.psp.naming;

import org.cougaar.core.service.NamingService;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.spi.*;

import java.io.*;
import java.util.*;
import java.net.*;

import org.cougaar.planning.plugin.legacy.PluginDelegate;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.*;
import org.cougaar.core.util.*;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.lib.planserver.server.FDSURL;

import org.cougaar.core.naming.*;

import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.planning.ldm.plan.Role;
import org.cougaar.planning.ldm.plan.RoleSchedule;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.RelationshipSchedule;
import org.cougaar.planning.ldm.plan.Relationship;

/**
 * .<pre>
 * This is a simple JNDI demonstratino/illustration PSP implementation.
 *
 * The PSP_YPDemo class relies upon the YPDemoJNDI class for its
 * JNDI-specific logic.  Consult this class for the specific JNDI
 * use-patterns.
 *
 * </pre>
 **/

//###########################################################################
//###########################################################################
//###########################################################################
//
// Main JNDI Demonstration PSP
//
public class PSP_YPDemo extends PSP_BaseAdapter implements PlanServiceProvider, UISubscriber
{

   private String BUTTON_SUBMIT_YP_SEARCH = "BUTTON_SUBMIT_YP_SEARCH";
   private String BUTTON_UPDATE_YP_ATTRIBUTES = "BUTTON_UPDATE_YP_ATTRIBUTES";
   private String BUTTON_GET_YP_ATTRIBUTES = "BUTTON_GET_YP_ATTRIBUTES";
   private String TEXT_COLOR = "#006869";


   public UnaryPredicate getOrganizationPredicate() {
         UnaryPredicate pred = new UnaryPredicate() {
                 public boolean execute(Object o) {
                     if( o instanceof Organization ) return true;
                     // else
                     return false;
                }
         };
         return pred;
   }
   public UnaryPredicate getAllPredicate() {
         UnaryPredicate pred = new UnaryPredicate() {
                 public boolean execute(Object o) {
                     return true;
                }
         };
         return pred;
   }

  /**
   * A zero-argument constructor is required for dynamically loaded PSPs,
   * required by Class.newInstance()
   **/
  public PSP_YPDemo()
  {
    super();
  }

  public PSP_YPDemo(String pkg, String id)
    throws RuntimePSPException
  {
    setResourceLocation(pkg, id);
  }

  public boolean test(HttpInput query_parameters, PlanServiceContext sc)
  {
    super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
    return false;  // This PSP is only accessed by direct reference.
  }

  UnaryPredicate myAllObjsPredicate   = new UnaryPredicate() {
           public boolean execute(Object o) {
                     if( o instanceof Object ) {
                         return true;
                     }
                     return false;
           }
    };

  //################################################################################
  public void execute(
      PrintStream out,
      HttpInput query_parameters,
      PlanServiceContext psc,
      PlanServiceUtilities psu ) throws Exception
  {
        YPDemoSession session = (YPDemoSession)psu.getBlackBoard().getEntry("YPDemoSession",psc.getSessionAddress());
        if( session == null) {
              session = new YPDemoSession();
              psu.getBlackBoard().addEntry("YPDemoSession",psc.getSessionAddress(),session);
        }

        String GETLINE = query_parameters.getInputLines()[0];
        //System.out.println("[PSP_ExternalQuery] GETLINE=" + GETLINE);
        QueryObject qObject =null;
        if( session.getQueryObject() == null) {
             qObject = new QueryObject();
             session.setQueryObject(qObject);
        } else {
             qObject = session.getQueryObject();
        }
        if( query_parameters.getInputLines()[0].indexOf("?UPDATE") > -1 ) {
              processFormPOST(query_parameters, psc, qObject, session, out);
        }
        PluginDelegate pd = psc.getServerPluginSupport().getDirectDelegate();
        displayUserSearchCheckBoxes(out,  query_parameters, qObject, session, psc, pd.getMessageAddress().toString());
        out.flush();
  }

   //################################################################################
    private boolean processFormPOST(HttpInput query_parameters,
                      PlanServiceContext psc, QueryObject qObject, YPDemoSession session, PrintStream out )
    {
       char buf[] =    query_parameters.getPostData();
       String post = new String( buf).trim();
       //System.out.println("[PSP_YPDemo.processFormPOST(POST=:" + post + ")]" );

       if( (buf == null) || (buf.length == 0) ) return false;

       if( post.indexOf(BUTTON_SUBMIT_YP_SEARCH) > -1 ) {
           processSearchFromPOST(post,psc,qObject,session, out);
       }
       else if( post.indexOf(BUTTON_UPDATE_YP_ATTRIBUTES) > -1 ) {
           processUpdateAttributesFromPOST(post,psc,session, out);
       }
       else if( post.indexOf( BUTTON_GET_YP_ATTRIBUTES ) > -1) {
           processGetAttributesFromPOST(post,psc,session,out);
       }
       return true;
    }

    //###############################################################################
    private void processGetAttributesFromPOST( String post, PlanServiceContext psc,
                      YPDemoSession session, PrintStream out)
    {
        System.out.println("[PSP_YPDemo.processGetAttributesFromPOST()] Entered.");
        NamingService nservice = psc.getServerPluginSupport().getNamingService();
        /**
         *  Returns List of Binding objects which match predicate
         *
        List lst = YPDemoJNDI.collectObjects(nservice, new UnaryPredicate() {
                 public boolean execute(Object o) {
                        if( o instanceof Binding ){
                                Binding b = (Binding)o;
                                if( b.getObject() instanceof AgentRole) return true;
                        }
                        return false;
                }}
          );
          **/

        // Returns list of Name : Attribute Strings.
        Map attributesMap = YPDemoJNDI.collectAttributes(nservice, new UnaryPredicate() {
                 public boolean execute(Object o) {
                        if( o instanceof Binding ){
                                Binding b = (Binding)o;
                                if( b.getObject() instanceof AgentRole) return true;
                        }
                        return false;
                }}
          );

        //
        // Updating Session Attribute (Query) List by replacing the attributesMap
        //
        session.getQueryObject().setEntries(attributesMap);

        if( attributesMap.size() > 0 ) {
            session.addHeader("Found " + attributesMap.size() + " search attributes.");
        } else session.addHeader("No search attributes updated.");
        session.doPrepend();
    }

    //###############################################################################
    private void processUpdateAttributesFromPOST( String post, PlanServiceContext psc,
                      YPDemoSession session, PrintStream out)
    {
        System.out.println("[PSP_YPDemo.processUpdateAttributesFromPOST()] Entered.");

        //session.append( session.createHeaderToSessionTranscript("Attributes added to YP.") );
        NamingService nservice = psc.getServerPluginSupport().getNamingService();

        PluginDelegate pd = psc.getServerPluginSupport().getDirectDelegate();
        Subscription subscription = psc.getServerPluginSupport().subscribe(this,
                                           myAllObjsPredicate );
        Collection container = ((CollectionSubscription)subscription).getCollection();
        Object [] objs = container.toArray();
        for( int i=0; i< objs.length; i++) {
              Object obj = objs[i];
              processRoles(obj, session, nservice);
        }
    }

    //###############################################################################
    // Processes organization assets from blackboard and populates
    //  AgentRoles in JNDI
    //
    private void processRoles( Object obj,  YPDemoSession session, NamingService nservice ) {
          if( obj instanceof Organization ) {
              Organization og = (Organization)obj;
              if( og.isSelf() )
              {
                  //System.out.println("Organization=" + og.getItemIdentificationPG().getNomenclature());
                  String ret = new String();

                  session.addHeader("Roles attributed to " + og.getItemIdentificationPG().getNomenclature());

                  //
                  // COLLECT ALL ROLES FOR ORGANIZATION (PLACE ROLES IN ATTRIBUTES OBJ)
                  //
                  RelationshipSchedule rs = og.getRelationshipSchedule();
                  // return all Relationships
                  Collection relations = rs.getMatchingRelationships(getAllPredicate() );
                  Object[] arr  = relations.toArray();

                  BasicAttributes attributes = new BasicAttributes();
                  session.add("<TABLE>");
                  for(int i=0; i< relations.size(); i++){
                      Relationship rel = (Relationship)arr[i];
                      Role role = rel.getRoleA();
                      BasicAttribute attribute = new BasicAttribute(role.getName(), "true");
                      attributes.put(attribute);

                      session.add("<TR><TD></TD>");
                      session.add( DescribeObjectAsHTML.describeWithinTableColumn(role.getName(),attribute));
                      //<TD><FONT SIZE=-1 COLOR=RED>" + role.getName() + "</FONT></TD>");
                      session.add("</TR>");

                  }
                  session.add("</TABLE>");
                  session.doPrepend();

                  YPDemoJNDI.createORLookupDirectory_andAttributeObject(
                                     "Agents", og.getItemIdentificationPG().getNomenclature(),
                                     "Roles",
                                     new AgentRole("http://www.ultralog.net"),
                                     attributes,
                                     nservice );

                  /**
                  //
                  // BIND ATTRIBUTES OBJ TO NAME
                  //
                  try{
                         DirContext  subKontext =null;
                         DirContext dirc = (DirContext)nservice.getRootContext();
                         try{
                                  DirContext agents = (DirContext)dirc.lookup("Agents");
                                  subKontext = (DirContext)agents.createSubcontext(og.getItemIdentificationPG().getNomenclature());
                          } catch( NameAlreadyBoundException ex ) {
                                  //
                                  // catch NameAlreadyBoundException -  directory already exists
                                  // perform a lookup instead of creating new
                                  //
                                  subKontext = (DirContext)dirc.lookup(og.getItemIdentificationPG().getNomenclature());
                          }
                          System.out.println("Created Subdirectory=" + subKontext.getNameInNamespace());
                          //subKontext.bind("Roles", new AgentRole("http://www.ultralog.net")); //, new BasicAttributes("name", role.getName()));

                          System.out.println("Binding attributes, size=" + attributes.size() );
                          subKontext.bind("Roles", new AgentRole("http://www.ultralog.net"), attributes );
                    } catch( NamingException ex ){
                          // if already bound, ignore
                    }
                   **/
              }

          }
    }

    //###############################################################################
    private void processSearchFromPOST( String post, PlanServiceContext psc,
                      QueryObject qObject, YPDemoSession session, PrintStream out)
    {
          System.out.println("[PSP_YPDemo.processSearchFromPOST()] Entered.");
          String decodedpost = URLDecoder.decode(post);
          System.out.println("DECODED POST="+  decodedpost);

          NamingService nservice = psc.getServerPluginSupport().getNamingService();
          //---------------------------------------------
          String tag3 = qObject.GET_ALL;
          int ind3 = decodedpost.indexOf(tag3);
          if( ind3 > -1) {
              //qObject.allRoles=true;
              session.addHeader("List everything (JNDI directories and contents).");
              session.add("<I>Orange = Directories; Yellow = Objects; Pink = Attributes</I>");
              session.add( YPDemoJNDI.describeAllDirContexts( nservice ) );
              session.doPrepend();
          }
          else
          {
             //
             // Parse and interpet return checked fields...
             //
             Hashtable nameAttrs = new Hashtable(); // Values are Vectors of attributes associated with named obj
             int idx0 =0;
             int idx1 =0;
             int idx2 =0;
             while( true ) {
                 idx0 = decodedpost.indexOf("{", idx0);
                 if(idx0 > -1) idx1 = decodedpost.indexOf("::",idx0);
                 else break;
                 if(idx1 > -1) idx2 = decodedpost.indexOf("}",idx0);
                 else break;

                 String name ="";
                 String attribute ="";
                 if( (idx0 > -1) && (idx1 > -1) ) {
                     name = decodedpost.substring(idx0+1,idx1);
                 } else break;
                 if( (idx1 > -1) && (idx2 > -1) ) {
                     attribute = decodedpost.substring(idx1+2,idx2);
                 } else break;

                 System.out.println("ATTRIBUTE PARSER.  name="  + name + ", attribute=" + attribute);
                 String searchResults = YPDemoJNDI.searchAttribute(name, attribute, nservice );
                 session.addHeader("List objects found by attribute at named context (Search): context=" + name  + ", attribute=" + attribute );
                 session.add( searchResults );
                 session.doPrepend();
                 //
                 // Add values to nameAttrs Hashtable
                 //
                 if( nameAttrs.get(name) == null) nameAttrs.put(name, new Vector());
                 Vector v = (Vector)nameAttrs.get(name);
                 v.add(attribute);
                 idx0=idx2+2;
             }// end while
          }
    }


    //###############################################################################
    //
    private void displayUserSearchCheckBoxes(PrintStream out,
                                HttpInput query_parameters,
                                QueryObject qObject, YPDemoSession session,
                                PlanServiceContext psc, String cluster_id)
    {
        out.println("<HTML><BODY bgcolor=#FFFFCC>");
        out.println("<form method=\"post\"");
        out.println("action=\"?UPDATE\">");

        out.println("<center><h1><Font color=GREEN>JNDI Yellow Pages Demonstration Viewer</font></h1></center>");
        out.println("<center><h2><Font color=GREEN SIZE=-1>Your browser view is currently with respect to Cluster = "
                                                + cluster_id + "</font></h2></center>");

        out.println("<P>");
        out.println("<P><FONT COLOR=" + TEXT_COLOR + "><I>To change your agent view, select links below</FONT></I></P>");
        out.println(linksToOtherClusters(query_parameters, psc));

        // BUTTON 1 ------------------

        out.println("<P><FONT COLOR=" + TEXT_COLOR + "><I>Select \"Get queryable attributes...\" ");
        out.println("to fetch query attributes from the YP.  Fetched attributes appear below the \"Search Yellow Pages...\" as check boxes.");
        out.println("You need to do this each time you update YP (e.g. \"Add/Update Yellow Pages...\").");
        out.println("Note, that unlike with \"Add/Update...\" below, this will get <und>all</und> the Role attributes entered in the YP at this moment.</P></FONT></I>");
        out.println("<div align=center>");
        out.println("<input type=submit name="+BUTTON_GET_YP_ATTRIBUTES +" value=\"Get queryable attributes from Yellow Pages\">");
        out.println("</div>");
        out.println("<P>");
        out.println("</P>");


        // BUTTON 2 ------------------

        out.println("<P>");
        out.println("<P><FONT COLOR=" + TEXT_COLOR + "><I>Select \"Add/Update Yellow Pages...\" to update <B>this</B> Cluster's JNDI entries.");
        out.println("Note that only the Roles of the current agent (this view) will be added.");
        out.println("You will need to \"Add/Update...\" individually for each agent you wish to add to the YP (use the agent links above to navigate to each agent in society).");
        out.println("</P></FONT></I>");
        out.println("<div align=center>");
        out.println("<input type=submit name="+BUTTON_UPDATE_YP_ATTRIBUTES
                                              +" value=\"Add/Update Yellow Pages with attributes for this cluster (  "
                                              +cluster_id + ").\">");
        out.println("</div>");
        out.println("<P>");
        out.println("</P>");


        // BUTTON 3 ------------------

        out.println("<P><FONT COLOR=" + TEXT_COLOR + "><I>Select \"Search Yellow Pages...\" to search from all JNDI entries");
        out.println("Select <FONT COLOR=BLUE>CONTEXT NAME</FONT> and <FONT COLOR=MAGENTA>ATTRIBUTE</FONT> pairs below.");
        out.println("To update list, choose \"Get queryable attributes...\" above.");
        out.println("</I></FONT></P>");
        out.println("<div align=center>");
        out.println("<input type=submit name="+BUTTON_SUBMIT_YP_SEARCH+" value=\"Search Yellow Pages\">");
        out.println("<P>");
        out.println("</P>");
        out.println("<P><FONT COLOR=" + TEXT_COLOR + "><I>Select appropriate check boxes to constrain your search query.  When you have completed,");
        out.println("select the \"Search Yellow Pages...\" button. Note that the checkbox \"DISPLAY_ALL/DISPLAY_ALL\" must be checked/un-checked as appropriate: ");
        out.println("leaving this checked will return all attributes (uncheck it if you want to select using the other checkboxes).");
        out.println("</I></FONT></P>");
        //
        // START ROLE CHECK BOXES
        //
        /**
        Object [] arr = qObject.getEntries().values().toArray();
        for(int i=0; i< arr.length; i++) {
            Object entry = arr[i];
            if( entry == qObject.GET_ALL_NAMES ) {
                 out.println("<input type=checkbox name=" + qObject.GET_ALL_NAMES + " value=X checked><I>Search for all attributes</I>,");
            } else {
                 out.println("<input type=checkbox name=" + entry.toString() + " value=X checked><I>" + entry.toString() + "</I>,");
            }
        }
        **/
        Set keys = qObject.getEntries().keySet();
        Object [] keysArray = keys.toArray();
        for(int i=0; i< keysArray.length; i++ ){
             String name = (String)keysArray[i];
             Vector v = (Vector)qObject.getEntries().get(name);
             for(int j=0; j<v.size(); j++) {
                 String attribute = (String)v.get(j);
                  out.println("<input type=checkbox name={"
                                            + name + "::" + attribute + "} value=X checked><I><FONT COLOR=BLUE>"
                                            + name + "/</FONT><FONT COLOR=MAGENTA>"  + attribute + "</FONT></I>,");
             }
        }
        //
        // END SROLE CHECK BOXES
        //
        out.println("</div>");

        out.println("<P>");
        if( session.getTranscriptLength() > 0) {
            out.println("<center><H2><FONT COLOR=GREEN><B>~~~~~~~~~~~~~~~~ Session Transcript (most recent first) ~~~~~~~~~~~~~~~~</B></FONT></H2></CENTER>");
            out.println("<PRE>");
            out.println(session.getTranscript());
            out.println("</PRE>");
        }
        out.println("</form>");
        out.println("</BODY></HTML>");
   }


   //###########################################################################
   private String linksToOtherClusters(HttpInput query_parameters, PlanServiceContext psc) {
           String ret = new String();
           Vector urls = new Vector();
           Vector names = new Vector();
           psc.getAllURLsAndNames(urls,names);
           int sz = urls.size();
           for(int i=0; i<sz; i++) {
                String url = (String)urls.get(i);
                String name = (String)names.get(i);
                String psp_path = query_parameters.getGETUrlString();
                if (psp_path.endsWith("/")) {
                   psp_path = psp_path.substring(0, psp_path.length()-1);
                }
                if (psp_path.startsWith("/")) {
                  psp_path = psp_path.substring(1, psp_path.length());
                }
                if (false == url.endsWith("/")) {
                  url += "/";
                }
                ret += "<TABLE WIDTH=\"100%\"><TR>";
                //ret += "<TD BGCOLOR=\"#e9e9e9\"><FONT FACE=\"Arial\"SIZE=\"2\">";
                //ret += "<A href= \"" +  url +  psp_path + "?FORMS" + "\">" + name  + "</A></TD>";
                ret += "<TD BGCOLOR=\"#e9e9e9\"><FONT FACE=\"Arial\"SIZE=\"2\">";
                ret += "<A href= \"" +  url +   "/ul/demo/YPDEMO.PSP" + "\">JNDI Yellow Pages Demonstration.......... (" + url + ")</A></TD>";

                ret += "</TR></TABLE>";
           }
           return ret;
   }

   //###########################################################################
   private String extractDelimitedSubstring( String GETLINE, int idx, String open, String close)
   {
       int ivalue0 =  GETLINE.indexOf(open, idx);
       int ivalue1 =  GETLINE.indexOf(close,ivalue0);
       if( (ivalue0<0) || (ivalue1<0)) return "";

       String val  =  GETLINE.substring(ivalue0 + open.length(), ivalue1);
       return val.trim();
   }


   /**
    * A PSP can output either HTML or XML (for now).  The server
    * should be able to ask and find out what type it is.
    **/
    public boolean returnsXML() {
      return false;
   }

   public boolean returnsHTML() {
      return true;
   }

   /**  Any PlanServiceProvider must be able to provide DTD of its
    *  output IFF it is an XML PSP... ie.  returnsXML() == true;
    *  or return null
    **/
   public String getDTD()  {
      return null;
   }

    public void subscriptionChanged(Subscription subscription) {

    }

}
