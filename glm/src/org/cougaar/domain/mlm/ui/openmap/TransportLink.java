/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.openmap;

import java.awt.Color;
import java.awt.Graphics;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMLine2D;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

class TransportLink extends OMGraphic
    implements TransportNetworkElement, Cloneable {

    /** The alp-ui link object.  This contains all the relevant
     * information for a link, within the context of the ALP UI.
     */ 
    UITxLink alpLink;
    
    /** the TransportNode endpoints */
    protected final TransportNode startNode;
    protected final TransportNode endNode;
    
    /** The link. */
    protected static final Color SELECTED_COLOR = Color.red;
    protected static final Color LINK_COLOR = Color.black;
    protected static final float LINK_WIDTH_MULTIPLIER = 1.0f;
    protected static final int lineType = OMGraphic.LINETYPE_STRAIGHT;
    
    protected boolean showLink = true;
    private boolean needToGenerate = true;

    /** Links, in general ar much larger that nodes, and are therefore
     * much easier to select.  This number is added on to the result
     * of every call to distance(), in order to "handicap" links
     * slightly, allowing users to select nodes more easily.*/
    private float selectionDistanceBias = 1.5f;
    
    protected OMLine2D linkGraphic = null;
    /** The URL to display when the object is gestured upon. */
    //protected String infoLine = "";
    
    
    TransportLink(UITxLink l, TransportNode start, TransportNode end){
        alpLink = l;
        startNode = start;
        endNode = end;
        generateGraphics();
    }
    
    /** Create the relevant OMGraphics for this link */
    public void generateGraphics(){
        linkGraphic = new OMLine2D(startNode.getLatitude(), startNode.getLongitude(),
                                   endNode.getLatitude(), endNode.getLongitude(), lineType);

        // the line-width = numberOfLanes * LINK_WIDTH_MULTIPLIER
        linkGraphic.setLineWidth(LINK_WIDTH_MULTIPLIER * alpLink.getNumberOfLanes());
        linkGraphic.setLineColor(LINK_COLOR);
        linkGraphic.setAppObject(this);
        
        selectionDistanceBias = LINK_WIDTH_MULTIPLIER * alpLink.getNumberOfLanes() + 1.5f;

        needToGenerate = false;
    }

    /** return a single line that describes this opject */
    public String getInfoLine(){
        return getID() + " (" 
            + getNumberOfLanes() + " lanes, "
            + "speed: " + getSpeed() 
            + "): " 
            //      + getStartNodeID() + " to " + getEndNodeID()
            ;
    }
    
    public TransportNode getStartNode(){ return startNode; }
    public TransportNode getEndNode(){ return endNode; }
    
    public Object duplicate(){
        try {
            return clone();
        } catch (java.lang.CloneNotSupportedException cnse){
            return null;
        }
    }

    //////////////////////////////////////////////////
    ///////////// UITxLink delegations ///////////////
    //////////////////////////////////////////////////

    /**
     * Get the value of the alpLink's ID.
     * @return Value of the alpLink's ID
     */
    public String getID() {return alpLink.getLinkID();}

    public String getStartNodeID() {return alpLink.getSourceNodeID();}
    public String getEndNodeID() {return alpLink.getDestinationNodeID();}
    public int getSpeed() {return alpLink.getSpeed();}
    public int getNumberOfLanes() {return alpLink.getNumberOfLanes();}


    ////////////////////////////////////////////////////
    ///////////  OMGraphic methods  ////////////////////
    ////////////////////////////////////////////////////

    public void setLineColor(Color c){
        linkGraphic.setLineColor(c);
    }

    /** Select */
    public void select() {
        linkGraphic.setLineColor(SELECTED_COLOR);
    }

    /** Deselect */
    public void deselect() {
        linkGraphic.setLineColor(LINK_COLOR);
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
        if (linkGraphic != null)   
            linkGraphic.generate(proj);
        return true;
    }

    /**
     * Paint the graphic.
     * This paints the graphic into the Graphics context.  This is
     * similar to <code>paint()</code> function of
     * java.awt.Components.  Note that if the graphic has not been
     * generated, it will not be rendered.  This render does not pay
     * attention to the layer settings for showNames and
     * showLinks.
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
     * account the layer showNames and showLinks settings.
     * @param g Graphics context to render into.
     * @param showNames Layer setting for all the names.
     * @param showLinks Layer setting for all the links.
     */
    public void render (Graphics g, boolean showLinks){
        if (showLink || showLinks) {
            if (linkGraphic != null) 
                linkGraphic.render(g);
            else 
                Debug.message("txnetwork", this.toString() + " has no graphic!");
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

        if (showLink && linkGraphic != null) 
            dist = linkGraphic.distance(x, y);
        
        return dist + selectionDistanceBias;
    }

    public void addLinkToQuadTree(QuadTree qt, TransportNetwork net){
        TransportNode n1 = net.getNodeWithID(getStartNodeID());
        TransportNode n2 = net.getNodeWithID(getEndNodeID());
        
        if (n1 != null && n2 != null) {
            qt.put(n1.getLatitude(),n1.getLongitude(), this);
            qt.put(n2.getLatitude(),n2.getLongitude(), this);
        }
        else {
            System.err.println("Can't display link " + getID() 
                               + ", because it looks like it had bad endpoints...");
        }
    }
}
