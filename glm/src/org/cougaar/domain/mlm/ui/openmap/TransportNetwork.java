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

import java.util.*;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

class TransportNetwork {
    
    protected UITxNetwork alpNetwork;
    
    /** A Hashtable of all the nodes, indexed by their unique ids */
    protected Hashtable nodeTable = new Hashtable();
    /** A Hashtable of all the links, indexed by their unique ids */
    //protected Hashtable linkTable = new Hashtable();
    protected HashSet linkTable = new HashSet();

    protected Vector routes = new Vector();
    
    /** The constructor takes a UITxNetwork object, and builds the
     * list of nodes and links.  Nodes 
     */
    TransportNetwork(UITxNetwork net){
        alpNetwork = net;
    }
    
    public TransportNode getNodeWithID(String id){
        if ( nodeTable.get(id) != null )
            return (TransportNode) nodeTable.get(id);
        else 
            return null;
    }

    public TransportLink getLinkWithID(String id){
//      if ( linkTable.get(id) != null )
//          return (TransportLink) linkTable.get(id);
//      else 
//          return null;
        return null;
    }

    public void BuildNetwork(QuadTree qt) {
        // First translate all the nodes in the network.
        Iterator all_nodes = alpNetwork.getAllGroundNodes().iterator();
        if (Debug.debugging("txnetwork")){
            Debug.message("txnetwork", "TransportNetwork: building network...");
            Debug.message("txnetwork", "TransportNetwork: building nodes...");
        }

        while (all_nodes.hasNext()){
            UITxNode alpnode = (UITxNode) all_nodes.next();
            TransportNode mapnode = new TransportNode(alpnode);
            nodeTable.put(alpnode.getId(), mapnode);
            mapnode.addToQuadTree(qt);
        }

        // Now, translate all the links, which need the nodes above for their endpoints. 
        if (Debug.debugging("txnetwork")){
            Debug.message("txnetwork", "TransportNetwork: building links...");
        }
        Iterator all_links = alpNetwork.getAllGroundLinks().iterator();
        while (all_links.hasNext()){
            // UITxNode
            UITxLink alplink = (UITxLink) all_links.next();
            
            // Find the endpoints.. 
            TransportNode start = getNodeWithID(alplink.getSourceNodeID());
            TransportNode end = getNodeWithID(alplink.getDestinationNodeID());
            
            if ( start != null && end != null){
                TransportLink maplink = new TransportLink(alplink, start, end);
                //linkTable.put(alplink.getLinkID(), maplink);
                linkTable.add(maplink); // ACK! LinkIDs were not unique!! Using a hashset instead.
                maplink.addLinkToQuadTree(qt,this);
            } 
            else {
                System.err.println("link " + alplink.getLinkID() + " has a non-existant endpoint!" 
                                   + "(start=" + start.toString() 
                                   + ") (end=" + end.toString());
            }
        }
        if (Debug.debugging("txnetwork")){
            Debug.message("txnetwork", "TransportNetwork: Done building links...");
        }
    }

    public void setVisible(boolean v){
        Enumeration all_nodes_enum = nodeTable.elements();
        while (all_nodes_enum.hasMoreElements()){
            ((OMGraphic)all_nodes_enum.nextElement()).setVisible(v);
        }

        Iterator all_links_enum = linkTable.iterator();
        while (all_links_enum.hasNext()){
            ((OMGraphic)all_links_enum.next()).setVisible(v);
        }
    }

    public Collection getAllGraphics(){
        Collection allgraphics = new HashSet(linkTable.size() + nodeTable.size());
        if (Debug.debugging("txnetwork")){
            Debug.out.println("linkTable:" + linkTable.size() 
                              + " nodeTable:" + nodeTable.values().size());
        }
        allgraphics.addAll(linkTable);
        allgraphics.addAll(nodeTable.values());
        return allgraphics;
    }
}
