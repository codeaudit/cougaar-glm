/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.openmap;

import java.net.URL;

import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.util.Debug;

/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBN Technologies
 *                          A Division of
 *                         BBN Corporation
 *                        10 Moulton Street
 *                       Cambridge, MA 02138
 *                          (617) 873-3000
 *  
 *        Copyright 1998 by BBN Technologies, A Division of
 *              BBN Corporation, all rights reserved.
 *  
 * **********************************************************************
 * 
 * $Source: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/openmap/Attic/TxNetworkApplet.java,v $
 * $RCSfile: TxNetworkApplet.java,v $
 * $Revision: 1.2 $
 * $Date: 2001-04-05 19:28:02 $
 * $Author: mthome $
 * 
 * **********************************************************************
 */

/**
 * OpenMap Applet
 *
 */

public class TxNetworkApplet extends JApplet {

    protected final String pinfo[][] = {
        {Environment.Latitude, "float", "Starting center latitude"},
        {Environment.Longitude, "float", "Starting center longitude"},
        {Environment.Scale, "float", "Starting Scale"},
        {Environment.Projection, "String", "Default projection type"},
        {"ORBdisableLocator", "boolean", "disable Visiborker Gatekeeper"},
        {"ORBgatekeeperIOR", "boolean", "URL to gatekeeper IOR."},
        {"debug.basic", "none", "enable basic debugging"},
        {Environment.HelpURL, "String", "URL location of OpenMap help pages"}
    };

    OpenMap om;

    /**
     * Returns information about this applet.<p>
     *
     * @return  a string containing information about the author, version, and
     *          copyright of the applet.
     * @since   JDK1.0
     */
    public String getAppletInfo() {
        return MapBean.getCopyrightMessage();
    }


    /**
     * Returns information about the parameters that are understood by 
     * this applet.
     * <p>
     * Each element of the array should be a set of three 
     * <code>Strings</code> containing the name, the type, and a 
     * description. For example:
     * <p><blockquote><pre>
     * String pinfo[][] = {
     *   {"fps",    "1-10",    "frames per second"},
     *   {"repeat", "boolean", "repeat image loop"},
     *   {"imgs",   "url",     "images directory"}
     * };
     * </pre></blockquote>
     * <p>
     *
     * @return  an array describing the parameters this applet looks for.
     * @since   JDK1.0
     */
    public String[][] getParameterInfo() {
        return pinfo;
    }


    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it has been loaded into the system. It is always 
     * called before the first time that the <code>start</code> method is 
     * called. 
     * <p>
     * The implementation of this method provided by the 
     * <code>Applet</code> class does nothing. 
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     * @since   JDK1.0
     */
    public void init() {
        OpenMap.init(this);// initialize Environment and debugging
        Debug.message("app", "OpenMapApplet.init()");
        
        /* First, get the properties parameter */
        String societyHostPortString = getParameter("societyHostPort");
        int societyHostPort;
        URL societyHostURL;
        try {
            societyHostPort = (new Integer(societyHostPortString)).intValue();

        } catch (NumberFormatException nfe) {
            System.err.println("Bad port number: " + societyHostPortString);
            societyHostPort = 5555;
        }
        
        try {
            societyHostURL = new URL("http", getCodeBase().getHost(), societyHostPort, "");
            System.out.println("societyHostURL = " + societyHostURL);
            
            OpenMap.init(this);// initialize Environment and debugging
            OpenMap om = new OpenMap();
            om.init();  // is this necessary?
            
            if (societyHostURL != null){
                getAndDisplayNetwork(om, societyHostURL);
            }
        } catch (java.net.MalformedURLException e) {
            System.err.println("societyHostURLString is not a valid url: ");
        }       
    }

    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it should start its execution. It is called after 
     * the <code>init</code> method and each time the applet is revisited 
     * in a Web page. 
     * <p>
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#init()
     * @see     java.applet.Applet#stop()
     * @since   JDK1.0
     */
    public void start() {
        Debug.message("app", "OpenMapApplet.start()");
        super.start();
    }

    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it should stop its execution. It is called when 
     * the Web page that contains this applet has been replaced by 
     * another page, and also just before the applet is to be destroyed. 
     * <p>
     *
     * @see     java.applet.Applet#destroy()
     * @see     java.applet.Applet#init()
     * @since   JDK1.0
     */
    public void stop() {
        Debug.message("app", "OpenMapApplet.stop()");
        super.stop();
    }

    /**
     * Called by the browser or applet viewer to inform 
     * this applet that it is being reclaimed and that it should destroy 
     * any resources that it has allocated. The <code>stop</code> method 
     * will always be called before <code>destroy</code>. 
     * <p>
     *
     * @see     java.applet.Applet#init()
     * @see     java.applet.Applet#start()
     * @see     java.applet.Applet#stop()
     * @since   JDK1.0
     */
    public void destroy() {
        Debug.message("app", "OpenMapApplet.destroy()");
        super.destroy();
    }


    

    private void getAndDisplayNetwork(OpenMap om, URL societyHostURL){
        Layer [] all_layers = om.getLayerHandler().getLayers();
        
        System.out.println("Called getAndDisplayNetwork");
        
        // a little awkward, but it should work. 
        int i;
        for (i = 0; i < all_layers.length ; i++){
            Layer l = all_layers[i];
            if (l instanceof TransportNetworkLayer){
                System.out.println("Setting up new data source: " + societyHostURL.toString());
                ((TransportNetworkLayer)l).setSocietyHostURL(societyHostURL);
                ((TransportNetworkLayer)l).rereadData();
                return;
            }
        }
    }

}
