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
import java.util.Hashtable;
import java.util.Iterator;

import com.bbn.openmap.omGraphics.OMCircle2D;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

class TransportNode extends OMGraphic 
    implements TransportNetworkElement, Cloneable {

    /** The alp-ui node object.  This contains all the relevant
     * information for a node, within the context of the COUGAAR UI.
     */ 
    protected UITxNode alpNode;

    /** The information that describes how we display the node. */
    public static final Color NODE_COLOR = Color.blue;
    public static final Color SELECTED_COLOR = Color.red;
    public static final Color TEXT_BG_COLOR = new Color(245,222,179); // wheat
    public static final int NODE_RADIUS = 4;
    public static final int GEOLOC_NODE_RADIUS = 8;

    private float selectionDistanceBias = 0.0f;

    /** This is the OMGraphic that actually appears on the map */
    protected OMCircle2D nodeGraphic = null;
    protected OMText nodeText = null;
    
    /** Says whether or not to show the node... */
    protected boolean showNode = true;

    /** The String to display when the object is gestured upon. */
    //    protected String details = ""; 

    public TransportNode(UITxNode alpnode){
        alpNode = alpnode;
        generateGraphics(alpnode);
        //generateDetails(alpnode);  Maybe commenting this out will speed life up...
    }

    protected TransportNode(){}
    
    /** Generate the infoline string at construction time */
    public String generateDetails(UITxNode alpnode){
        String details = new String();

        if (alpnode.getReadableName() != null){
            details = details + alpnode.getReadableName();
        }
        if (alpnode.getGeoloc() != null && !alpnode.getGeoloc().equals("")){
            details = details +  " [Geoloc: " + alpnode.getGeoloc() + "] ";
        }
        return details;
    }
            
    public void generateGraphics(UITxNode n){
        float latitude = n.getLatitude();
        float longitude = n.getLongitude();
        String node_name = n.getReadableName();
        String node_geoloc = n.getGeoloc();
        
        if (n.getGeoloc() != null &&
            !n.getGeoloc().equals("")){
            allKnownGeolocs.put(n.getGeoloc(),this);
            // Nodes with geoloc codes are drawn bigger. 
            nodeGraphic = new OMCircle2D(latitude, longitude,
                                         GEOLOC_NODE_RADIUS, GEOLOC_NODE_RADIUS);
        } else {
            nodeGraphic = 
                new OMCircle2D(latitude, longitude, NODE_RADIUS, NODE_RADIUS);
        }

        nodeGraphic.setFillColor(NODE_COLOR);
        nodeGraphic.setAppObject(this);
        
        // If the node has a name worth looking at, 
        if (node_name != null && 
            n.getGeoloc() != null &&
            !n.getGeoloc().equals("")){
            nodeText = new OMText(latitude, longitude, 
                                  NODE_RADIUS + 3, NODE_RADIUS/2, // the the right of the node 
                                  node_name, OMText.JUSTIFY_LEFT);
            nodeText.setLineColor(NODE_COLOR);
            nodeText.setAppObject(this);
            nodeText.setFillColor(TEXT_BG_COLOR);
            nodeText.setShowBounds(false);
        }
        else {
            // Try to favor the more interesting nodes over the boring
            // ones.
            selectionDistanceBias = 0.5f;
        }
        this.setSelectColor(SELECTED_COLOR);
    }

    public String getInfoLine(){
        return generateDetails(alpNode);
    }

    public Object duplicate(){
        try {
            return clone();
        } catch (java.lang.CloneNotSupportedException cnse){
            return null;
        }
    }

    //////////////////////////////////////////////////
    ///////////// UITxNode delegations ///////////////
    //////////////////////////////////////////////////

    /**
     * Get the value of the alpNode's Latitude.
     * @return Value of the alpNode's Latitude.
     */
    public float getLatitude() {return alpNode.getLatitude();}

    /**
     * Get the value of the alpNode's Longitude.
     * @return Value of the alpNode's Longitude.
     */
    public float getLongitude() {return alpNode.getLongitude();}
    
    /**
     * Get the value of the alpNode's ID.
     * @return Value of the alpNode's ID
     */
    public String getID() {return alpNode.getId();}

    public String getReadableName() { return alpNode.getReadableName();}
    public String getGeoloc() { return alpNode.getGeoloc();}
    

    ////////////////////////////////////////////////////
    ///////////  OMGraphic methods  ////////////////////
    ////////////////////////////////////////////////////

    /** This is just to make life easier... */
    public void setColor(Color c){
        nodeGraphic.setFillColor(c);
        nodeGraphic.setLineColor(c);
        if (nodeText != null){
            nodeText.setFillColor(c);
            nodeText.setLineColor(c);
        }
    }

    /** Select */
    public void select() {
        nodeGraphic.select();
        //setColor(getSelectColor());
        if (nodeText != null){
            nodeText.setShowBounds(true);
            nodeText.select();
        }
    }

    /** Deselect */
    public void deselect() {
        nodeGraphic.deselect();
        //setColor(NODE_COLOR);
        if (nodeText != null){
            nodeText.setShowBounds(false);
            nodeText.deselect();
        }
    }
    

    public void setSelectColor(Color value){
        nodeGraphic.setSelectColor(value);
        if (nodeText != null)
            nodeText.setSelectColor(value);
    }

    public Color getSelectColor(){
        return SELECTED_COLOR;
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
        if (nodeGraphic != null) nodeGraphic.generate(proj);
        if (nodeText != null)    nodeText.generate(proj);
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
        if (showNode || showNodes) {
            if (nodeGraphic != null) nodeGraphic.render(g);
            if (nodeText != null) nodeText.render(g);
        }
    }

    /**
     * Return the shortest distance from the graphic to an XY-point.
     * @param x X coordinate of the point.
     * @param y Y coordinate fo the point.
     * @return float distance from graphic to the point
     */
    public float distance (int x, int y){
        float graphicdist = Float.MAX_VALUE;
        float textdist = Float.MAX_VALUE;

        if (showNode && nodeGraphic != null) {
            // Instead of the distance to the graphic, return the distance
            // to the center of the circle, since nodes are likely to be
            // packed in tightly.
            //-- graphicdist = nodeGraphic.distance(x, y);
            int nx = nodeGraphic.getX();
            int ny = nodeGraphic.getY();
            int dx = x - nx;
            int dy = y - ny;
            graphicdist = (float)java.lang.Math.sqrt( dx*dx + dy*dy );
            //-- ignore the text distance, since nodes are too tighly packed in.
            //-- if (nodeText != null) textdist = nodeText.distance(x, y);
        }
        
        if (graphicdist < textdist)
            return graphicdist + selectionDistanceBias;
        else 
            return textdist + selectionDistanceBias;
    }
    
    public void addToQuadTree(QuadTree qt){
        qt.put(getLatitude(),getLongitude(), this);
    }

    
    /** All the known Geoloc objects, so we don't repeat them */
    protected static Hashtable allKnownGeolocs = new Hashtable();

    public static TransportNode getHashedGeolocCode(String code){
        return (TransportNode)allKnownGeolocs.get(code);
    }
    
    public static void addKnownGeolocsToQuadTree(QuadTree qt){
        Iterator geo_iter = allKnownGeolocs.values().iterator();
        while (geo_iter.hasNext()){
            ((TransportNode)geo_iter.next()).addToQuadTree(qt);
        }
    }

}
