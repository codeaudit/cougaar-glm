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
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

class TransportRoute extends OMGraphic
    implements TransportNetworkElement {

    /** The color to paint the route */
    private Color routeColor = new Color(50,205,50);

    protected UITxRoute alpRoute;
    
    protected TransportNode startNode;
    protected TransportNode endNode;
    
    /** The Links in the route, stored as TransportLink objects */
    protected Vector links;

    TransportRoute(UITxRoute route, TransportNetwork network){
        this.alpRoute = route;
        buildRoute(network);
    }

    /** Construct the route by cloning the relevant elements of the
     * TransportNetwork */
    protected void buildRoute(TransportNetwork network){

        // First set up the endpoints.
        startNode = (TransportNode)network.getNodeWithID(alpRoute.getSourceNodeID()).duplicate();
        startNode.setColor(routeColor);
        startNode.setAppObject(this);
        
        endNode = (TransportNode)network.getNodeWithID(alpRoute.getDestinationNodeID()).duplicate();
        endNode.setColor(routeColor);
        endNode.setAppObject(this);

        links = new Vector(alpRoute.getLinkIDs().size());
        Iterator link_id_iter = alpRoute.getLinkIDs().iterator();

        while (link_id_iter.hasNext()){
            String linkid = (String)link_id_iter.next();
            try {
                TransportLink routelink =
                    (TransportLink)network.getLinkWithID(linkid).duplicate();
                routelink.setLineColor(routeColor);
                routelink.setAppObject(this);
                links.add(routelink);
            }
            catch (NullPointerException npe){
                System.err.println("It doesn't seem that " + linkid 
                                   + " appears in the network!");
                npe.printStackTrace();
            }
        }
    }


    public String getInfoLine(){
        return "Route From " + startNode.getInfoLine()
            + "to " + endNode.getInfoLine();
    }
    

    //////////////////////////////////////////////////
    ///////////// UITxLink delegations ///////////////
    //////////////////////////////////////////////////

    public String getStartNodeID() {return alpRoute.getSourceNodeID();}
    public String getEndNodeID() {return alpRoute.getDestinationNodeID();}


    ////////////////////////////////////////////////////
    ///////////  OMGraphic methods  ////////////////////
    ////////////////////////////////////////////////////
    
    public void setLineColor(Color c){
        Iterator link_iter = links.iterator();
        while (link_iter.hasNext()){
            ((TransportLink)link_iter.next()).setLineColor(c);
        }
    }

    /** Select */
    public void select() {
        Iterator link_iter = links.iterator();
        while (link_iter.hasNext()){
            ((TransportLink)link_iter.next()).select();
        }
    }

    /** Deselect */
    public void deselect() {
        Iterator link_iter = links.iterator();
        while (link_iter.hasNext()){
            ((TransportLink)link_iter.next()).deselect();
        }
    }



    /**
     * Prepare the graphic for rendering.
     * This must be done before calling <code>render()</code>!  If a
     * vector graphic has lat-lon components, then we project these
     * vertices into x-y space.  For raster graphics we prepare in a
     * different fashion.
     * <p>
     * If the generate is unsuccessful, it's usually because of some
     * oversight, (for instance if <code>proj</code> is null), and if
     * debugging is enabled, a message may be output to the controlling
     * terminal.
     * <p>
     * @param proj Projection
     * @return boolean true if successful, false if not.
     */
    public boolean generate (Projection proj){
        if (links != null){
            Iterator link_iter = links.iterator();
            while (link_iter.hasNext()){
                ((TransportLink)link_iter.next()).generate(proj);
            }
        }
        return true;
    }

    /**
     * Paint the graphic.
     * This paints the graphic into the Graphics context.  This is
     * similar to <code>paint()</code> function of
     * java.awt.Components.  Note that if the graphic has not been
     * generated, it will not be rendered.  This render does not pay
     * attention to the layer settings for showNames and
     * showNodes.
     * @param g Graphics context to render into,
     */
    public void render (Graphics g){
        render(g, false);
    }

    /**
     * Paint the graphic.
     * This paints the graphic into the Graphics context.  This is
     * similar to <code>paint()</code> function of
     * java.awt.Components.  Note that if the graphic has not been
     * generated, it will not be rendered.  This render will take into
     * account the layer showNames and showNodes settings.
     * @param g Graphics context to render into.
     * @param showNames Layer setting for all the names.
     * @param showNodes Layer setting for all the nodes.
     */
    public void render (Graphics g, boolean showNodes){
        if (links != null){
            Iterator link_iter = links.iterator();
            while (link_iter.hasNext()){
                ((TransportLink)link_iter.next()).render(g);
            }
        }
    }

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * @param x X coordinate of the point.
     * @param y Y coordinate fo the point.
     * @return float distance from graphic to the point
     */
    public float distance (int x, int y){
        float dist = Float.MAX_VALUE;

        if (links != null){
            Iterator link_iter = links.iterator();
            while (link_iter.hasNext()){
                float thisdist = ((TransportLink)link_iter.next()).distance(x,y);
                if (thisdist < dist)
                    dist = thisdist;
            }
        }
        
        return dist;
    }

    
    public void addToQuadTree(QuadTree qt){
        if (links != null){
            Iterator link_iter = links.iterator();
            while (link_iter.hasNext()){
                TransportLink link = (TransportLink)link_iter.next();
                qt.put(link.getStartNode().getLatitude(), 
                       link.getStartNode().getLongitude(), 
                       this);
                qt.put(link.getEndNode().getLatitude(), 
                       link.getEndNode().getLongitude(), 
                       this);
            }
        }
    }
}
