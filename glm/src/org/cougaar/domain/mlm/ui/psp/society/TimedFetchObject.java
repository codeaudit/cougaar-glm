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
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

public class TimedFetchObject extends FetchObject implements Runnable
{

    private String MyURLString;
    private String MyPostField;
    private boolean MyUseXML;
    private Thread MyThread;
    private long TimeOutMillis =15000; // 15 secs default

    // we cache handles to old TimedFetchObject instances which have timed out...
    private static Vector MyOrphanedThreads =new Vector();


    public String toString() {
        String ret = new String();

        ret += "MyURLString=" + MyURLString + "\n";
        ret += "MyPostField=" + MyPostField + "\n";
        ret += "TimeOutMillis=" + TimeOutMillis + "\n";
        ret += "MyUseXML=" + MyUseXML + "\n";
        return ret;
    }

    public void dumpAndClearOrphanedThreads() {
        Enumeration en = MyOrphanedThreads.elements();
        while(en.hasMoreElements())
        {
            TimedFetchObject tf = (TimedFetchObject)en.nextElement();
            System.out.println("[TimedFetchObject.DumpAndClearOrphanedThreads]: Timed-out: "
                                   + tf.toString() );
        }
        TimedFetchObject.MyOrphanedThreads.clear();
    }

    public void setTimeOut( long timeout ) {
        TimeOutMillis = timeout;
    }

    public Object fetchObject(String urlstring, String postfield, boolean useXML)
        throws Exception {
        MyPostField = postfield;
        return fetchObject(urlstring,useXML);
    }

    public synchronized Object fetchObject(String urlstring, boolean useXML)
        throws Exception {
        returnObject = null;
        MyPostField = null;
        MyURLString = urlstring;
        MyUseXML = useXML;
        MyThread  = new Thread(this);
        MyThread.start();

        Object obj = null;
        long startTime = System.currentTimeMillis();
        while( (obj = getReturnObject()) == null )
        {
           //System.out.print("+");
           if(  (System.currentTimeMillis() - startTime ) > TimeOutMillis){
              System.out.println("[TimedFetchObject.fetchObject] !! Connection Timed-out."
                                 + " Interval (millis)=" + TimeOutMillis
                                 + " URL=" + urlstring );
              MyOrphanedThreads.addElement(this);
              return null;
           }
           try{ Thread.sleep(150); } catch(Exception e) {};
        }
        System.out.println("Returned called");
        return obj;
    }

    private Object returnObject =null;
    private Object getReturnObject() { return returnObject; }
    private void setReturnObject(Object obj ) {
         //System.out.println("Set called");
         returnObject = obj;
    }

    public void run()
    {
        System.out.println("Timed Fetch Object Thread started....");
        try {
           Object obj = super.fetchObject(MyURLString, MyPostField, MyUseXML);
           setReturnObject(obj);
        } catch( Exception e) { e.printStackTrace(); }
    }
}
