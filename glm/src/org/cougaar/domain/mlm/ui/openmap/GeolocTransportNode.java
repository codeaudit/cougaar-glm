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

import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.psp.transportation.data.UITxNode;

class GeolocTransportNode extends TransportNode {
    
    static private int idnum = 0;

    protected GeolocTransportNode(GeolocLocation location){
        UITxNode node = new UITxNode();
        node.setId("GeoTransNode" + idnum++);

        // how do I get a better readable name? 
        node.setReadableName(location.getName());       
        node.setGeoloc(location.getGeolocCode());
        node.setLatitude((float)location.getLatitude().getDegrees());
        node.setLongitude((float)location.getLongitude().getDegrees());
        alpNode = node;
        generateGraphics(node);
    }

    
    /** This will return a TransportNode.  If there is no known
        TransportNode with the given geoloc code, it will create a
        GeolocTransportNode out of the supplied GeolocLocation */
    public static TransportNode getHashedGeolocLocation(GeolocLocation loc){
        String geocode = loc.getGeolocCode();
        Object found_geo = getHashedGeolocCode(geocode);
        
        if (found_geo != null)
            return (TransportNode)found_geo;
        
        GeolocTransportNode new_geo = new GeolocTransportNode(loc);
        TransportNode.allKnownGeolocs.put(loc.getGeolocCode(),new_geo);
        
        return new_geo;
    }
}
