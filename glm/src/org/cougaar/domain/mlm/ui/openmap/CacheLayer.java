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

import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.*;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.proj.Projection;
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
 * $Source: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/openmap/Attic/CacheLayer.java,v $
 * $RCSfile: CacheLayer.java,v $
 * $Revision: 1.2 $
 * $Date: 2001-04-05 19:28:02 $
 * $Author: mthome $
 * 
 * **********************************************************************
 */

/**
 * A Layer to display the states of the US and their corresponding 
 * FGI geography
 * <p>
 * This example shows:
 * <ul>
 * <li>OMGraphicList use
 * <li>OMLine use
 * </ul>
 */

public class CacheLayer extends Layer
    implements java.awt.event.ActionListener, MapMouseListener {

    
    public static final String CacheFileProperty = ".cacheFile";
    
    
    /** used by the gui */
    private static final String READ_DATA_COMMAND = "ReadData";

    /**
     * URL to read data from.  This data will be in the form of
     * a serialized stream of OMGraphics.
     */       
    protected URL cacheURL;

    /**
     *  A list of graphics to be painted on the map.
     */
    protected OMGraphicList omgraphics = new OMGraphicList();

    /**
     * Construct a default route layer.  Initializes omgraphics to
     * a new OMGraphicList, and invokes createGraphics to create
     * the canned list of routes.
     */
    public CacheLayer() {}
    
    /** 
     * Read a cache of OMGraphics
     */
    public void readGraphics ()
        throws java.io.IOException {

        System.out.println("Reading cached graphics");
        fireStatusUpdate(LayerStatusEvent.START_WORKING);
        
        if (omgraphics == null){
            omgraphics = new OMGraphicList();
        }
        
        try {
            ObjectInputStream objstream = 
                new ObjectInputStream(cacheURL.openStream());
            
            if (Debug.debugging("cachelayer")){
                Debug.out.println("Opened " + cacheURL.toString() );
            }

            try { 
                while (true) {
                    try {
                        OMGraphic graphic = (OMGraphic)(objstream.readObject());
                        omgraphics.add(graphic);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (EOFException e){}

            objstream.close();

            if (Debug.debugging("cachelayer")){
                Debug.out.println("closed " + cacheURL.toString() );
            }

            fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);

        } catch (ArrayIndexOutOfBoundsException aioobe){
            new com.bbn.openmap.util.HandleError(aioobe);
        }  catch (ClassCastException cce){
            cce.printStackTrace();
        }
    }
    
    /**
     * Initializes this layer from the given properties.
     *
     * @param props the <code>Properties</code> holding settings for this layer
     */
    public void setProperties (String prefix, Properties props) {
        super.setProperties(prefix, props);
        String cacheFile = props.getProperty(prefix + CacheFileProperty);

        try {
            if (cacheFile != null) {
                if (Debug.debugging("cachelayer")){
                    Debug.out.println("Getting cachefile: " + cacheFile);
                }
                
                // First find the resource, if not, then try as a file-URL... b
                cacheURL = getResourceOrFileOrURL(cacheFile);

                if ( cacheURL != null ){
                    readGraphics();
                }
            }
        } catch (java.net.MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe){
            ioe.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    /** 
     * Returns a URL that names either a resource, a local file, or an internet URL.
     * 
     */
    public URL getResourceOrFileOrURL(String name)
        throws java.net.MalformedURLException {
        
        // First see if we have a resource by that name
        URL retval = getClass().getResource(name);
        
        if (Debug.debugging("cachelayer")){
            if (retval != null){Debug.out.println("Resource " + retval.toString());}
            else {Debug.out.println("Can't find a resource named: " + name);}
        }
        
        if (retval == null){    // If there was no resource by that name available
            if (name.charAt(0) == '/'){
                // If name starts with a "/", treat it as a file: reference.
                retval = new URL (new URL("file:"), name); 
            }
            else {
                // Otherwise treat it as a raw URL.
                retval = new URL(name);
            }
            
        }
        if (Debug.debugging("cachelayer")){
            if (retval != null)
                Debug.out.println("Resource "+ name + "=" + retval.toString());
            else 
                Debug.out.println("Resource " + name + " can't be found..." );
        }
        return retval;
    }

    //----------------------------------------------------------------------
    // Layer overrides
    //----------------------------------------------------------------------


    /**
     * Renders the graphics list.  It is important to make this
     * routine as fast as possible since it is called frequently
     * by Swing, and the User Interface blocks while painting is
     * done.
     */
    public void paint(java.awt.Graphics g) {
        omgraphics.render(g);
    }
    

    //----------------------------------------------------------------------
    // ProjectionListener interface implementation
    //----------------------------------------------------------------------


    /**
     * Handler for <code>ProjectionEvent</code>s.  This function is
     * invoked when the <code>MapBean</code> projection changes.  The
     * graphics are reprojected and then the Layer is repainted.
     * <p>
     * @param e the projection event
     */
    public void projectionChanged(ProjectionEvent e) {
        omgraphics.project(e.getProjection(), true);
        repaint();
    }



    //----------------------------------------------------------------------
    /** 
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
        JButton rereadFilesButton;
        
        rereadFilesButton = new JButton("ReRead OMGraphics");
        rereadFilesButton.setActionCommand(READ_DATA_COMMAND);
        rereadFilesButton.addActionListener(this);
        
        JLabel fileLabel = new JLabel("Read from: ");
        JTextField pathText = new JTextField(cacheURL.toString());

        Box filebox = Box.createHorizontalBox();
        filebox.add(fileLabel);
        filebox.add(pathText);

        Box box = Box.createVerticalBox();
        box.add(rereadFilesButton);
        box.add(filebox);
        return box;
    }


    //----------------------------------------------------------------------
    // ActionListener interface implementation
    //----------------------------------------------------------------------

    /** 
     * The Action Listener method, that reacts to the palette widgets
     * actions.
     */
    public void actionPerformed (java.awt.event.ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == READ_DATA_COMMAND) {
            System.out.println("Reading serialized graphics");
            try { 
                readGraphics();
            } catch (java.io.IOException exc) {
                exc.printStackTrace();
            }
        } else  {
            System.err.println("Unknown action command \"" + cmd +
                               "\" in SaveShapeLayer.actionPerformed().");
        }
    }


    //----------------------------------------------------------------------
    // MapMouseListener interface implementation
    //----------------------------------------------------------------------
    
    private OMGraphic selectedGraphic;

    /**
     * Indicates which mouse modes should send events to this
     * <code>Layer</code>.
     *
     * @return An array mouse mode names
     *
     * @see com.bbn.openmap.event.MapMouseListener
     * @see com.bbn.openmap.MouseDelegator
     */
    public String[] getMouseModeServiceList () {
        String[] ret = new String[1];
        ret[0] = new String("Gestures");
        return ret;
    }



    /**
     * Called whenever the mouse is pressed by the user and
     * one of the requested mouse modes is active.
     *
     * @param e the press event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mousePressed (MouseEvent e) {
        return false;
    }
  


    /**
     * Called whenever the mouse is released by the user and
     * one of the requested mouse modes is active.
     *
     * @param e the release event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseReleased(MouseEvent e) {
        return false;
    }
  


    /**
     * Called whenever the mouse is clicked by the user and
     * one of the requested mouse modes is active.
     *
     * @param e the click event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseClicked(MouseEvent e) {
        if (selectedGraphic != null) {
            switch (e.getClickCount()) {
            case 1:  
                System.out.println("Show Info: " + 
                                   selectedGraphic.getAppObject());
                break;
            case 2:
                System.out.println("Request URL: " + selectedGraphic);
                break;
            default:
                break;
            }
            return true;
        } else {
            return false;
        }
    }



    /**
     * Called whenever the mouse enters this layer and
     * one of the requested mouse modes is active.
     *
     * @param e the enter event
     * @see #getMouseModeServiceList
     */
    public void mouseEntered(MouseEvent e) {
    }
  


    /**
     * Called whenever the mouse exits this layer and
     * one of the requested mouse modes is active.
     *
     * @param e the exit event
     * @see #getMouseModeServiceList
     */
    public void mouseExited(MouseEvent e) {
    }
    


    /**
     * Called whenever the mouse is dragged on this layer and
     * one of the requested mouse modes is active.
     *
     * @param e the drag event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    public boolean mouseDragged(MouseEvent e){
        return false;
    }



    /**
     * Called whenever the mouse is moved on this layer and
     * one of the requested mouse modes is active.
     * <p>
     * Tries to locate a graphic near the mouse, and if it
     * is found, it is highlighted and the Layer is repainted
     * to show the highlighting.
     *
     * @param e the move event
     * @return true if event was consumed (handled), false otherwise
     * @see #getMouseModeServiceList
     */
    private Color oldFillColor = java.awt.Color.yellow;
    public boolean mouseMoved(MouseEvent e){
        OMGraphic newSelectedGraphic = 
            omgraphics.selectClosest(e.getX(), e.getY(), 2.0f);

        if (newSelectedGraphic != selectedGraphic) {
            if (selectedGraphic != null)
                selectedGraphic.setFillColor(oldFillColor);

            selectedGraphic = newSelectedGraphic;
            if (newSelectedGraphic != null) {
                oldFillColor = newSelectedGraphic.getFillColor();
                newSelectedGraphic.setFillColor(Color.white);
                fireRequestInfoLine(newSelectedGraphic.getAppObject().toString());
            }
            repaint();
        }

        return true;
    }
    
    /** Called whenever the mouse is moved on this layer and one of
     * the requested mouse modes is active, and the gesture is
     * consumed by another active layer.  We need to deselect anything
     * that may be selected.
     *
     * @see #getMouseModeServiceList */
    public void mouseMoved(){
        omgraphics.deselectAll();
        repaint();
    }
    
    /**
     * Returns self as the <code>MapMouseListener</code> in order
     * to receive <code>MapMouseEvent</code>s.  If the implementation
     * would prefer to delegate <code>MapMouseEvent</code>s, it could
     * return the delegate from this method instead.
     *
     * @return The object to receive <code>MapMouseEvent</code>s or
     *         null if this layer isn't interested in
     *         <code>MapMouseEvent</code>s
     */
    public MapMouseListener getMapMouseListener(){
        return this;
    }
}
