/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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
 
package org.cougaar.mlm.ui.alert;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;

import org.cougaar.core.blackboard.CollectionSubscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.planning.ldm.plan.Alert;
import org.cougaar.planning.ldm.plan.AlertParameter;
import org.cougaar.core.util.UID;
import org.cougaar.lib.planserver.*;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.util.CircularQueue;

/**
 * PSP_Alert - PSP for relaying alerts. Implements KeepAlive so caller will receive
 * alert as long as they are connected.
 * Currently returns alerts as HTML
 */

public class PSP_Alert extends PSP_BaseAdapter 
    implements PlanServiceProvider, KeepAlive, UISubscriber {

    public static final String ALERT_LABEL = "ALERT";
    public static final String UID_LABEL = "UID=";
    public static final String TITLE_LABEL = "TITLE=";
    public static final String CHOICES_LABEL = "CHOICES=";
    public static final String SEVERITY_LABEL = "SEVERITY=";
    public static final String FILENAME_LABEL = "FILENAME=";
    public static final String DELIM = "?";

    public static final String UID_PARAM = "UID";
    public static final String TEXT_PARAM = "TEXT";
    public static final String CHOICE_PARAM = "CHOICE";
    public static final String RAW_PARAM = "raw=";
    private CircularQueue myIncomingAlerts = new CircularQueue();

    /** 
     * Constructor -  A zero-argument constructor is required for dynamically 
     * loaded PSPs by Class.newInstance()
     **/
    public PSP_Alert() {
        super();
    }
  
    /**
     * Constructor -
     *
     * @param pkg String specifying package id
     * @param id String specifying PSP name
     * @throws org.cougaar.lib.planserver.RuntimePSPException
     */
    public PSP_Alert( String pkg, String id ) throws RuntimePSPException {
        setResourceLocation(pkg, id);
    }
  
    /**
     * infoMessagePred - subscribes for all alerts
     */
    private static UnaryPredicate alertPred = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof Alert) {
                return true;
            }
            return false;
        }
    };            
  
    /**
     * test - Always returns false as currently implemented.
     * See doc in org.cougaar.lib.planserver.PlanServiceProvider
     *
     * @param queryParamaters HttpInput
     * @param sc PlanServiceContext
     */
    public boolean test(HttpInput query_parameters, PlanServiceContext sc) {
        super.initializeTest(); // IF subclass off of PSP_BaseAdapter.java
        return false;  // This PSP is only accessed by direct reference.
    }

    //private int iterationCounter = 0;
    /**
     * execute - creates HTML with the relevant alerts
     * See doc in org.cougaar.lib.planserver.PlanServiceProvider
     *
     * @param out PrintStream to which output will be written
     * @param queryParameters HttpInput not used.
     * @param psc PlanServiceContext
     * @param psu PlanServiceUtilities
     **/
    public void execute(PrintStream out,
                        HttpInput query_parameters,
                        PlanServiceContext psc,
                        PlanServiceUtilities psu) throws Exception
    {
        Subscription subscription = 
            psc.getServerPluginSupport().subscribe(this, alertPred);
        boolean raw = false;
        for (Iterator i = query_parameters.getURLParameters().iterator(); i.hasNext(); ) {
            String param = ((String) i.next()).toLowerCase().trim();
            System.out.println("param=" + param);
            if (param.startsWith(RAW_PARAM)) {
                raw = param.substring(RAW_PARAM.length()).trim().equals("true");
            }
        }
        while (true) {
            AlertChange alertChange = nextAlert(true);
            if (alertChange.remove) {
                if (raw) sendRawRemove(out, alertChange.alert);
                // Do nothing if not raw
            } else {
                if (raw) {
                    sendRawAlert(out, alertChange.alert);
                } else {
                    sendAppletAlert(out, alertChange.alert);
                }
            }
        }
    }

    private void sendRawRemove(PrintStream out, Alert alert) {
        String alertInfo = ALERT_LABEL + DELIM
            + UID_LABEL + alert.getUID() + DELIM
            + TITLE_LABEL + DELIM
            + SEVERITY_LABEL + DELIM
            + FILENAME_LABEL + DELIM
            + CHOICES_LABEL + DELIM;
        System.out.println(alertInfo);
        out.println(alertInfo);
        out.flush();
    }

    private void sendRawAlert(PrintStream out, Alert alert) {
        String message = alert.getAlertText();
        String title;
        int pos = message.indexOf('\n');
        if (pos > 0) {
            title = message.substring(0, pos);
            if (title.endsWith("\r")) title = title.substring(0, title.length() - 1);
            title = title.trim();
        } else {
            title = "Alert Type " + alert.getType();
        }
        title = URLEncoder.encode(title);
        String severity = URLEncoder.encode(Integer.toString(alert.getSeverity()));
        message = URLEncoder.encode(message);
        String choices = URLEncoder.encode(generateChoiceParameter(alert));
        String alertInfo = ALERT_LABEL + DELIM
            + UID_LABEL + alert.getUID() + DELIM
            + TITLE_LABEL + title + DELIM
            + SEVERITY_LABEL + severity + DELIM
            + FILENAME_LABEL + message + DELIM
            + CHOICES_LABEL + choices + DELIM;
        System.out.println(alertInfo);
        out.println(alertInfo);
        out.flush();
    }

    private void sendAppletAlert(PrintStream out, Alert alert) {
        String filename = writeHTML(alert);
        String alertInfo = ALERT_LABEL + DELIM
            + UID_LABEL + alert.getUID() + DELIM
            + TITLE_LABEL + DELIM
            + SEVERITY_LABEL + DELIM
            + FILENAME_LABEL + filename + DELIM
            + CHOICES_LABEL + DELIM;
        System.out.println("<HTML><BODY> <FONT color=#CC0000>" +
                           alertInfo + 
                           "</FONT></BODY></HTML><p>");
        out.println(alertInfo);
        out.flush();
    }
  
    /**
   * returnsXML - returns true if PSP can output XML.  Currently always false.
   * 
   * @return boolean 
   **/
    public boolean returnsXML() {
        return false;
    }
  
    /**
   * returnsHTML - returns true if PSP can output HTML.  Currently always true.
   * 
   * @return boolean 
   **/
    public boolean returnsHTML() {
        return true;
    }
  
    /** 
   * getDTD - returns null. PSP does not return XML.
   * Any PlanServiceProvider must be able to provide DTD of its
   * output IFF it is an XML PSP... ie.  returnsXML() == true;
   *
   * @return String
   **/
    public String getDTD() {
        return null;
    }

    /**
   * addAlert - Adds the given alert to the alert list. Calls notifyAll()
   * to wake up blocked nextElement().
   *
   * @param alert Alert to add to list
   *
   */
    synchronized public void addAlert(Alert alert) {
        if (!alert.getAcknowledged()) {
            myIncomingAlerts.add(new AlertChange(alert, false));
        }
    
        notifyAll();
    }

    synchronized public void removeAlert(Alert alert) {
        myIncomingAlerts.add(new AlertChange(alert, true));
        notifyAll();
    }

    /**
   * nextAlert - returns the next alert on the list. If no alerts and wait is true, 
   * suspends thread until an alert is added. Otherwise returns null. 
   *
   * @param wait boolean controls whether call blocks
   *
   * @return alert Alert next alert on the list.
   */
    synchronized public AlertChange nextAlert(boolean wait) {
        while (myIncomingAlerts.size() == 0) {
            if (wait) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }
        }
    
        return (AlertChange) myIncomingAlerts.next();
    }

    private static class AlertChange {
        boolean remove;
        Alert alert;
        public AlertChange(Alert alert, boolean remove) {
            this.remove = remove;
            this.alert = alert;
        }
    }
    
    /**
   * subscriptionChanged - adds new subscriptions to myIncomingAlerts.
   *
   * @param subscription Subscription
   */
    public void subscriptionChanged(Subscription subscription) {
        IncrementalSubscription sub = (IncrementalSubscription) subscription;
        for (Enumeration e = sub.getAddedList(); e.hasMoreElements(); ) {
            Alert alert = (Alert) e.nextElement();
            addAlert(alert);
        }
        for (Enumeration e = sub.getChangedList(); e.hasMoreElements(); ) {
            Alert alert = (Alert) e.nextElement();
            removeAlert(alert);
            addAlert(alert);
        }
        for (Enumeration e = sub.getRemovedList(); e.hasMoreElements(); ) {
            Alert alert = (Alert) e.nextElement();
            removeAlert(alert);
        }
    }

    public static String[] parseChoiceParameter(String choiceParam) {
        int start = 0;
        int end = 0;
        Vector choices = new Vector();
    
        while ((end = choiceParam.indexOf(DELIM, start)) > 0) {
            choices.addElement(choiceParam.substring(start, end));
            start = end + 1;
        }

        Object []array = choices.toArray();
        String []choiceArray = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            choiceArray[i] = (String)array[i];
        }

        return choiceArray;
    }

    private static final String TITLE_STRING = 
        "<head><title>Alert Applet</title></head>";
  
    private static final String CLASSID_STRING = 
        "<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"";
  
    private static final String SIZE_STRING = 
        "height = 100% width = 100%";

    private static final String CODEBASE_STRING = 
        "codebase=\"http://java.sun.com/products/plugin/1.2/jinstall-12-win32.cab#Version=1,2,0,0\">";
  
    private static final String ARCHIVE_STRING = 
        "\"ui.jar\"";
  
    private static final String TYPE_STRING = 
        "\"application/x-java-applet;version=1.2\"";
  
    private static final String PLUGIN_STRING = 
        "\"http://java.sun.com/products/plugin/1.2/plugin-install.html\"";
  
  
    /**
   * writeHTML - writes an HTML file for the alert applet with
   * the alert info parameterized for the applet.
   * BOZO - hardcoding information from the HTMLConverter. Change the
   * converter or the plugin and this will probably need to be  rewritten,
   *
   * @param alert Alert to be handled by the alert applet
   * @return String file name of the generated file.
   */
    protected String writeHTML(Alert alert) {
        String filename = getFileName(alert);
        File file = new File(filename);
    
        System.out.println(filename);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String appletName = AlertApplet.CLASSNAME;
            String choiceParameter = generateChoiceParameter(alert);

            // BOZO - need to parameterize
            writeln(writer, "<html>");
            writeln(writer, TITLE_STRING);
            writeln(writer, "<body>");
            writeln(writer, "<center>");
            writeln(writer, CLASSID_STRING);
            writeln(writer, SIZE_STRING + " " + CODEBASE_STRING);
            writeln(writer, "<PARAM NAME = CODE VALUE = \"" + appletName + "\" >");
            writeln(writer, "<PARAM NAME = ARCHIVE VALUE = " + ARCHIVE_STRING + " >");
            writeln(writer, "<PARAM NAME= \"type\" VALUE = " + TYPE_STRING + " >");
            writeln(writer, "<PARAM NAME = \"" + UID_PARAM + 
                    "\" VALUE  = \"" + alert.getUID() + "\">");
            writeln(writer, "<PARAM NAME = \"" + TEXT_PARAM + 
                    "\" VALUE  = \"" + alert.getAlertText() + "\">");
            writeln(writer, "<PARAM NAME = \"" + CHOICE_PARAM + 
                    "\" VALUE  = \"" + choiceParameter + "\">");
            writeln(writer, "<COMMENT>");
            writeln(writer, "<EMBED type= " + TYPE_STRING + 
                    " java_CODE = \"" + appletName + "\"" + 
                    " java_ARCHIVE = " + ARCHIVE_STRING + 
                    " " + SIZE_STRING  +  
                    " " + UID_PARAM + " =  \""  + alert.getUID() + "\"" + 
                    " " + TEXT_PARAM + " =  \"" + alert.getAlertText() + "\"" + 
                    " " + CHOICE_PARAM + " =  \"" + choiceParameter + "\"" + 
                    " pluginspage = " + PLUGIN_STRING + "><NOEMBED></COMMENT>");
            writeln(writer, "</NOEMBED></EMBED>");
            writeln(writer, "</OBJECT>");
            writeln(writer, "</center>");
            writeln(writer, "</body>");
            writeln(writer, "</html>");

            writer.close();
        } catch (IOException ioException) {
            System.out.println("PSP_Alert.writeHTML() - ioException");
            ioException.printStackTrace();

            filename = null;
        }

        return filename;
    }

    private static int myFileCount = 0;
    protected String getFileName(Alert alert) {
        return  AlertPSPConnectionInfo.current().getPSPPackage() + "/" + 
            "Alert" + (myFileCount++) + ".html";
    }

    static private void writeln(BufferedWriter writer, String text) 
        throws java.io.IOException {
        writer.write(text);
        writer.newLine();
    }

    private static String quote(String s) {
        StringBuffer buf = null;
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                if (buf == null) buf = new StringBuffer(s.substring(0, i));
                buf.append("&lt;");
                break;
            case '>':
                if (buf == null) buf = new StringBuffer(s.substring(0, i));
                buf.append("&gt;");
                break;
            case '&':
                if (buf == null) buf = new StringBuffer(s.substring(0, i));
                buf.append("&amp;");
                break;
            case '"':
                if (buf == null) buf = new StringBuffer(s.substring(0, i));
                buf.append("&quot;");
                break;
            default:
                if (buf != null) buf.append(c);
                break;
            }
        }
        if (buf == null) return s;
        return buf.toString();
    }

    private static String generateChoiceParameter(Alert alert) {
        AlertParameter []alertParameters = alert.getAlertParameters();
        String paramStr;

        if ((alertParameters == null) ||
            (alertParameters.length == 0)) {
            return DELIM;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < alertParameters.length; i++) {
            if (alertParameters[i].isVisible()) {
                buf.append(quote(alertParameters[i].getDescription()));
                buf.append(DELIM);
            }
        }
        return buf.toString();
    }
}
