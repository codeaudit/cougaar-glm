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
import java.util.*;

import com.bbn.openmap.omGraphics.OMLine2D;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItinerary;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITaskItineraryElement;

/** 
 * A TransportItinerary is like a TransportRoute, but it contains a
 * set of legs of travel for a unit, and these legs may involve trips
 * over Land (via a TransportRoute) or simple "Legs", which describe
 * travel from one Geoloc to another. 
 */

class TransportItinerary implements TransportNetworkElement {
    
    /** The travel Legs of the itinerary */
    protected Vector itineraryLegs; 

    /** The unit doing the travelling */
    protected TransportUnit itineraryUnit;
    
    protected GeolocLocation startLocation;
    protected GeolocLocation endLocation;

    /** a leg is basically a special kind of OMLine */ 

    public class ItineraryLeg extends OMLine2D 
        implements TransportNetworkElement {
        TransportNode begin;
        TransportNode end;
        String travelMode;
        String asset;
    
        public ItineraryLeg(TransportNode start, TransportNode finish, String mode, String carrier){
        super(start.getLatitude(), start.getLongitude(),
              finish.getLatitude(), finish.getLongitude(), 
              LINETYPE_STRAIGHT);
            begin = start;
            end = finish;
            travelMode = mode;
            asset = carrier;
            setAppObject(this);
            setSelectColor(Color.red);
                    
            if (Debug.debugging("itinerary")){
                Debug.out.println("Created: " + getInfoLine());
            }
        }
        
        public void setTransportedUnit(TransportUnit unit){
            itineraryUnit = unit;
        }
        
        public String getInfoLine(){
            return  begin.getGeoloc() + " to " + end.getGeoloc() + " by " + travelMode 
                + (asset != null ? " (Carrier: " + asset + ")" : "") ;
        }

        public void addToQuadTree(QuadTree qt){
            if (begin != null && end != null) {
                qt.put(begin.getLatitude(),begin.getLongitude(), this);
                qt.put(end.getLatitude(),end.getLongitude(), this);
            }
            else {
                System.err.println("Can't display leg " + this
                                   + ", because it looks like it had bad endpoints...");
            }
        }
        
        /**
         * Return the shortest distance from the graphic to an XY-point.
         * @param x X coordinate of the point.
         * @param y Y coordinate fo the point.
         * @return float distance from graphic to the point
         */
        float selectionDistanceBias = 3.0f;
        public float distance (int x, int y){
            float dist = Float.MAX_VALUE;
            
            dist = super.distance(x, y);
            
            return dist + selectionDistanceBias;
        }
        
    }

    /** 
     * Creates a TransportItinerary with an empty list of legs
     */
    public TransportItinerary(){
        itineraryLegs = new Vector(0);
        if (Debug.debugging("itinerary")){
            Debug.out.println("Created an itinerary of size 0");
        }
    }

    /** TransportItinerary constructor
     * @param unit the unit doing the travelling
     * @param itineraries a Vector of UITaskItineraries 
     */
    public TransportItinerary(Vector itineraries){
        if (itineraries != null)
            itineraryLegs = computeLegs(itineraries);
        if (Debug.debugging("itinerary")){
            Debug.out.println("Created an itinerary with " 
                              + itineraryLegs.size() + " legs");
        }
    }

    

//     /** 
//      * Creates a TransportItinerary with an empty list of legs
//      */
//     public TransportItinerary(TransportUnit unit){
//      itineraryUnit = unit;
//      itineraryLegs = new Vector(0);
//      if (Debug.debugging("itinerary")){
//          Debug.out.println("Created an itinerary for " 
//                            + unit.toString() + " of size 0");
//      }
//     }

//     /** TransportItinerary constructor
//      * @param unit the unit doing the travelling
//      * @param itineraries a Vector of UITaskItineraries 
//      */
//     public TransportItinerary(TransportUnit unit, Vector itineraries){
//      itineraryUnit = unit;
//      if (itineraries != null)
//          itineraryLegs = computeLegs(itineraries);
//      if (Debug.debugging("itinerary")){
//          Debug.out.println("Created an itinerary for " 
//                            + unit.toString() + " with " 
//                            + itineraryLegs.size() + " legs");
//      }
//     }


    /** Build the list of legs from an Vector */
    protected Vector computeLegs(Vector itineraries){
        Iterator iter = itineraries.iterator();
        Vector legs = new Vector(itineraries.size());
        while (iter.hasNext()){
            Object obj = iter.next();
                    
            if (obj instanceof UITaskItinerary){
                // set the start location to the first location in the 
                if (startLocation == null){
                    startLocation = ((UITaskItinerary)obj).getFromRequiredLocation();
                    endLocation  = ((UITaskItinerary)obj).getToRequiredLocation();
                }
                
                Vector leg = 
                    computeLegFromTaskItinerary((UITaskItinerary)obj);
                
                if (leg != null)
                    legs.addAll(leg);
                else
                    System.err.println("Didn't get a leg out of:\n" + obj.toString());
                
            } else {
                System.out.println("computeLegs() doesn't know what to do with:");
                System.out.println(obj.toString());
            }
        }
        
        if (Debug.debugging("itinerary")){
            Debug.out.println("Got " + legs.size() + " travel legs");
        }
        return legs;
    }

    /** Make ItineraryLegs out of a UITaskItinerary */
    protected Vector computeLegFromTaskItinerary(UITaskItinerary uiti){
        Vector retval;

        Vector schedule_elements = uiti.getScheduleElements();
        if (schedule_elements != null){
            retval = new Vector(schedule_elements.size());
            
            Iterator elem_iter = schedule_elements.iterator();
            while (elem_iter.hasNext()){
                UITaskItineraryElement element = (UITaskItineraryElement)elem_iter.next();
                if (element.getStartLocation() != null 
                    && element.getEndLocation() != null){
                    
//                  if(Debug.debugging("itinerary")){
//                      Debug.out.println("Element: " + element.toString()); 
//                  }

                    TransportNode start = 
                        GeolocTransportNode.getHashedGeolocLocation(element.getStartLocation());
                    TransportNode end = 
                        GeolocTransportNode.getHashedGeolocLocation(element.getEndLocation());
                    //              String travelmode = element.getCarrierUID();
                    String travelmode = getTransportationModeName(element.getTransportationMode());
                    String carrier = getCarrierString(element);
                    ItineraryLeg leg = new ItineraryLeg(start, end, travelmode, carrier);
                    
                    float[] dasharray = new float[2];
                    dasharray[0] = 5.0f;
                    dasharray[1] = 5.0f;

                    leg.setLineDash(dasharray,0f);
                    
                    leg.setLineColor(Color.blue);
                    leg.setLineWidth(2);
                    retval.add(leg);
                } else {
                    System.err.println("missing endpoints?: " + element.toString());
                }
            }
        } else {
            // No schedule elements ??
            retval = new Vector(1);
            TransportNode start = new GeolocTransportNode(uiti.getFromRequiredLocation());
            TransportNode end = new GeolocTransportNode(uiti.getToRequiredLocation());
            String travelmode = "UNKNOWN";
            
            ItineraryLeg leg = new ItineraryLeg(start, end, travelmode, "");    
            leg.setLineColor(Color.gray);

            float[] dasharray = new float[2];
            dasharray[0] = 10.0f;
            dasharray[1] = 10.0f;
            
            leg.setLineDash(dasharray,0f);

            retval.add(leg);
        }
        return retval;
    }
    
    public String getInfoLine(){
        return "Itinerary with " + itineraryLegs.size() + " travel legs";
    }

    public void addToQuadTree(QuadTree qt){
        Iterator iter = itineraryLegs.iterator();
        while (iter.hasNext()){
            ((ItineraryLeg)iter.next()).addToQuadTree(qt);
        }
    }

    public static String getTransportationModeName(int mode){
        if (mode == UITaskItineraryElement.SEA_MODE)
            return "Sea";
        else if (mode == UITaskItineraryElement.AIR_MODE)
            return "Air";
        else if (mode == UITaskItineraryElement.GROUND_MODE)
            return "Ground";
        else if (mode == UITaskItineraryElement.NONE_MODE)
            return "None";
        else 
            return "unknown";
    }

    public static String getCarrierString(UITaskItineraryElement element){
      //return element.getCarrierUID();
      return getTransportationModeName(element.getTransportationMode()); 
    }
    
    public GeolocLocation getStartLocation(){
        return startLocation;
    }
    public GeolocLocation getEndLocation(){
        return endLocation;
    }


//     public void setVisible(boolean v){
//      Enumeration all_links_enum = linkTable.elements();
//      while (all_links_enum.hasMoreElements()){
//          ((OMGraphic)all_links_enum.nextElement()).setVisible(v);
//      }
//     }

    public Collection getAllGraphics(){
        Collection allgraphics = new HashSet(itineraryLegs);

        Iterator all_links_enum = itineraryLegs.iterator();
        while (all_links_enum.hasNext()){
            allgraphics.add(((ItineraryLeg)all_links_enum.next()).begin);
            allgraphics.add(((ItineraryLeg)all_links_enum.next()).end);
        }
        return allgraphics;
    }

}

