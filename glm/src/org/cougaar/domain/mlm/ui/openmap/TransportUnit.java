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
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.cgm.CgmUtils;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

class TransportUnit extends OMGraphic
    implements TransportNetworkElement {
    
    public static String cgmArchive;
    
    /**  the alp-ui unit object.  This contains all teh relevant
     * information for a unit, within the context of the COUGAAR-UI */
    protected UIUnit alpUnit;
    
    protected float latitude;
    protected float longitude;  

    protected OMCgmGraphic unitGraphic;
    protected OMLine2D positionArrow;
    protected OMText unitText;

    public static final Color SELECTED_COLOR = Color.red;
    public static final Color TEXT_BG_COLOR = new Color(245,222,179); // wheat

    /** Dimensions of the Cgm Graphic */
    int graphicWidth=75, graphicHeight=75;
    
    String unitName;

    boolean showUnit = true;
    boolean showUnits = true;

    float selectionDistanceBias = 
        (float)java.lang.Math.sqrt(graphicHeight*graphicHeight/4 + graphicWidth*graphicWidth/4);

    /** all itineraries are of type TransportItinerary */
    Vector itineraries = new Vector(0);
       
    TransportUnit(UIUnit unit){
        alpUnit = unit;
        unitName = getSelfAssetUID();
        generateGraphics(unit);
    }

    
    public void generateGraphics(UIUnit unit){
        try {
            String symbolname = unit.getMs2525IconName();
            String iconName = getIconNameFromSymbolName(symbolname);

            if ( !hasRealLocation() ) {
                System.err.println("Cluster " + alpUnit.getSelfAssetUID()
                                   + " has no location");
                return;
            }

            if (iconName != null){
                unitGraphic = 
                    new OMCgmGraphic(iconName,
                                     getLatitude(), getLongitude(),
                                     graphicWidth,graphicHeight);

                if  (Debug.debugging("unit")){
                    Debug.out.println("Created graphic for "
                                      + getSelfAssetUID()
                                      + " at: "
                                      + getLatitude() + "lat," 
                                      + getLongitude() + "lon" );
                }
            } 
            
            // The accompanying text
            if (getSelfAssetUID() != null){
                if (iconName != null){
                    unitText = new OMText(getLatitude(), getLongitude(), 
                                          // The following offsets ONLY apply to 
                                          // friendly units.
                                          graphicWidth/2, graphicHeight -10,   
                                          getSelfAssetUID(),
                                          OMText.JUSTIFY_CENTER);

                    // The positionArrow is a line drawn from the center of the 
                    // icon to the position that the unit is at on the map.
                    positionArrow = new OMLine2D(getLatitude(), getLongitude(),
                                                 graphicWidth/2,graphicHeight/2,0,0);
                    positionArrow.addArrowHead(true); 
                    positionArrow.setLineColor(Color.black);

                } else {
                    // If there's no icon, draw text where the icon would be.
                    unitText = new OMText(getLatitude(), getLongitude(), 
                                          0, 0, 
                                          getSelfAssetUID(),
                                          OMText.JUSTIFY_CENTER);
                    unitText.setFillColor(Color.white);
                    unitText.setShowBounds(true);
                } 
                
                unitText.setLineColor(Color.black);
                unitText.setAppObject(this);
            } 
            
        } catch (NullPointerException npe){
            npe.printStackTrace();
        }
    }

    /** Generate the infoline string */
    public String generateDetails(UIUnit alpunit){
        return "No information avalible for icon" + getSymbolName();
    }

    
    public void addToQuadTree(QuadTree qt){
        if (hasRealLocation())
            qt.put(getLatitude(),getLongitude(), this);
        else 
            System.err.println("No place to put " + getSelfAssetUID());
 
        if (itineraries != null){
            Iterator iter = itineraries.iterator();
            while (iter.hasNext()){
                ((TransportItinerary)iter.next()).addToQuadTree(qt);
            }
        } else {
            System.err.println("No itineraries for " + getSelfAssetUID());
        }
    }


    public static String getIconNameFromSymbolName(String symbolname){
        String retval = "";
        int i;
        
        if (Debug.debugging("unit"))
            Debug.out.println("Figuring icon name from '" + symbolname + "'");

        if (symbolname == null){
            return null;
        }
        
        for (i = 0; i < symbolname.length() ; i++){
            char c = symbolname.charAt(i);
            if (c != '.')
                retval += c;
        }

        retval = retval.substring(2, retval.length()) 
            + "f"       // All units are friendly
            + ".cgm";   // standard extension for CGM files
        
        if (Debug.debugging("unit"))
            Debug.out.println("returning " + retval);
        
        return retval;
    }

    //////////////////////////////////////////////////
    // Accessor methods
    
    public boolean hasRealLocation() {
        try {
            return (alpUnit.getLocation().getLatitude() != null
                    && alpUnit.getLocation().getLongitude() != null);
        } catch (NullPointerException npe){
            return false;
        }      
    }

    /**
     * Get the value of latitude.
     * @return Value of latitude.
     */
    public float getLatitude() {
        return (float) alpUnit.getLocation().getLatitude().getDegrees();
    }
//     void setLatitude(float  v) {
//      alpUnit.getLocation().setLatitude(v);
//     }
    
    /**
     * Get the value of longitude.
     * @return Value of longitude.
     */
    public float getLongitude() {
        return (float)alpUnit.getLocation().getLongitude().getDegrees();
    }
//     public void setLongitude(float  v) {
//      alpUnit.getLocation().setLongitude(v);
//     }
    

    public int getX() {
        if (unitGraphic != null)
            return unitGraphic.getX();
        else 
            return unitText.getX();
    }

    public int getY() {
        if (unitGraphic != null)
            return unitGraphic.getY();
        else 
            return unitText.getY();
    }

    
    public void addItinerary(TransportItinerary itin){
        itineraries.add(itin);
    }

    
    ////////////////////////////////////////////////////
    ////////////// UIUnit delegations //////////////////
    ////////////////////////////////////////////////////

    /**
     * Get the value of the alpUnit's ID.
     * @return Value of the alpUnit's ID
     */
    public String getSymbolName() {return alpUnit.getMs2525IconName();}

    public String getSelfAssetUID() { return alpUnit.getSelfAssetUID(); }
    public void setSelfAssetUID(String uid) { alpUnit.setSelfAssetUID(uid); }

    public GeolocLocation getLocation() {return alpUnit.getLocation();}
    public void setLocation(GeolocLocation  v) {
        alpUnit.setLocation(v); 
        generateGraphics(alpUnit);
    }

    
    ////////////////////////////////////////////////////
    ////////  TransportNetworkElement methods  /////////
    ////////////////////////////////////////////////////

    /** Get a string that will be printed below the map */
    public String getInfoLine(){
        return generateDetails(alpUnit);
    }


    
    ////////////////////////////////////////////////////
    /////////////  OMGraphic methods  //////////////////
    ////////////////////////////////////////////////////
    
    /** Select */
    public void select() {
        unitGraphic.select();
        //setColor(getSelectColor());
        if (unitText != null){
            //unitText.setShowBounds(true);
            unitText.select();
        }
    }

    /** Deselect */
    public void deselect() {
        unitGraphic.deselect();
        //setColor(UNIT_COLOR);
        if (unitText != null){
            //unitText.setShowBounds(false);
            unitText.deselect();
        }
    }
    
    
    public void setSelectColor(Color value){
        unitGraphic.setSelectColor(value);
        if (unitText != null)
            unitText.setSelectColor(value);
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
        if (unitGraphic != null) unitGraphic.generate(proj);
        if (unitText != null)    unitText.generate(proj);
        if (positionArrow != null)    positionArrow.generate(proj);
        return true;
    }

    /**
     * Paint the graphic.
     * This paints the graphic into the Graphics context.  This is
     * similar to <code>paint()</code> function of
     * java.awt.Components.  Note that if the graphic has not been
     * generated, it will not be rendered.  This render does not pay
     * attention to the layer settings for showNames and
     * showUnits.
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
     * account the layer showNames and showUnits settings.
     * @param g Graphics context to render into.
     * @param showNames Layer setting for all the names.
     * @param showUnits Layer setting for all the units.
     */
    public void render (Graphics g, boolean showUnits){
        if (showUnit || showUnits) {
            if (positionArrow != null) positionArrow.render(g);
            if (unitGraphic != null) unitGraphic.render(g);
            if (unitText != null) unitText.render(g);
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

        if (showUnit && unitGraphic != null) {
            // Instead of the distance to the graphic, return the distance
            // to the center of the circle, since units are likely to be
            // packed in tightly.
            //-- graphicdist = unitGraphic.distance(x, y);
            int nx = getX() + graphicWidth/2;
            int ny = getY() + graphicHeight/2;
            int dx = x - nx;
            int dy = y - ny;
            graphicdist = (float)java.lang.Math.sqrt( dx*dx + dy*dy );
            //-- ignore the text distance, since units are too tighly packed in.
            //-- if (unitText != null) textdist = unitText.distance(x, y);
        }
        
        if (graphicdist < textdist)
            return graphicdist + selectionDistanceBias;
        else 
            return textdist + selectionDistanceBias;
    }

}
