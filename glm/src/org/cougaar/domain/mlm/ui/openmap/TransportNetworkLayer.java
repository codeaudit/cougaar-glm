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

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.cgm.CgmUtils;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.SwingWorker;
import com.bbn.openmap.util.quadtree.QuadTree;

import org.cougaar.domain.mlm.ui.psp.society.SocietyUI;
import org.cougaar.domain.mlm.ui.psp.transportation.data.*;

/** 
 * The TransportNetworkLayer is a layer to display the transport
 * network for an alp-cluster.  */
class TransportNetworkLayer extends Layer     
    implements MapMouseListener, ProjectionListener, ActionListener {

    /** The storage mechanism for the links. */
    protected QuadTree quadtree = null;

    /** The graphic list of objects to draw. */
    protected OMGraphicList omGraphics;

    /** The property name of the a URL specifying the TOPS society
     * that the network will be retrieved from */
    public static final String SocietyHostURLProperty = ".societyHostURL";
    public static final String CgmArchiveNameProperty = ".cgmArchiveName";
    public static final String TxNetworkClustersProperty = ".txNetworkClusters";
    public static final String UnitClustersProperty = ".unitClusters";
    public static final String ItineraryClustersProperty = ".itineraryClusters";
    
    /** The projection we are using right now... */
    protected Projection projection;

    /** The swing worker that goes off in it's own thread to get
     * graphics. */
    protected TransportNetworkWorker currentWorker;
    /** Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early. */
    protected boolean cancelled = false;

    /** The URL from which the network data is collected */
    protected URL societyHostURL;

    /** Where the mil-std-2525B symbols graphics can be found */
    protected URL cgmArchiveURL;

    /** The names of the units to display on the map */
    Vector networkClusters;
    Vector unitNames;
    Vector itineraryClusters;

    /** The transportation network */
    TransportNetwork txnetwork;
    Vector txRoutes;
    Vector txUnits = new Vector();
    Vector itineraries = new Vector();

    private boolean showNetwork = true;
    private boolean showRoutes = true;
    private boolean showItineraries = true;
    private boolean showUnits = true;

    /** These are to keep track of what graphics are associated with
     * what parts of this layer */
    private HashSet networkGraphics = new HashSet();
    private HashSet routeGraphics = new HashSet();
    private HashSet unitGraphics = new HashSet();
    private HashSet itineraryGraphics = new HashSet();
    
    class TransportNetworkWorker extends SwingWorker {

        /** Constructor used to create a worker thread. */
        public TransportNetworkWorker () {
            super();
        }

        /**  Compute the value to be returned by the <code>get</code>
         * method.  */
        public Object construct() {
            if (Debug.debugging("TransportNetworkWorker")){
                Debug.message("TransportNetworkWorker", getName()+
                          "|TransportNetworkWorker.construct()");
            }
            fireStatusUpdate(LayerStatusEvent.START_WORKING);
            try {
                return prepare();
            } catch (OutOfMemoryError e) {
                String msg = getName() + 
                    "|TransportNetworkLayer.TransportNetworkWorker.construct(): " + e;
                System.err.println(msg);
                e.printStackTrace();
                fireRequestMessage(new InfoDisplayEvent(this, msg));
                fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
                return null;
            }
        }

        /** Called on the event dispatching thread (not on the worker
         * thread) after the <code>construct</code> method has
         * returned.  */
        public void finished() {
            workerComplete(this);
            fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
        }

    } //** END OF TransportNetworkWorker
    
    /** Do nothing, until we are told who we are talking to */
    public TransportNetworkLayer() {}

    /** 
     * The properties and prefix are managed and decoded here, for
     * the standard uses of the TransportNetworkLayer.
     *
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.  
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);

        String society_host_url_string = 
            properties.getProperty(prefix + SocietyHostURLProperty);
        
        try {
            if (society_host_url_string != null) {
                System.out.println("Society data source is " + society_host_url_string);
                URL society_data_source = new URL(society_host_url_string);
                setSocietyHostURL(society_data_source);
            }
        } catch (java.net.MalformedURLException mue) {
            mue.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // The CGM archive file, which holds all the mil-2525B symbols we need.
        String cgm_archive_name =  
            properties.getProperty(prefix + CgmArchiveNameProperty);
        
        try {
            this.cgmArchiveURL = getResourceOrFileOrURL(cgm_archive_name);
        } catch (java.net.MalformedURLException mue) {
            mue.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // The clusters where we will ask for a network.
        this.networkClusters = parseClusterNames(
            properties.getProperty(prefix + TxNetworkClustersProperty));

        // The names of the units to display
        this.unitNames = parseClusterNames(
            properties.getProperty(prefix + UnitClustersProperty));

        this.itineraryClusters = parseClusterNames(
            properties.getProperty(prefix + ItineraryClustersProperty));
        
    }

     /**
     * Parse the list of layers from the Environment containing the
     * layers that should be displayed upon map startup.
     * <p>
     * @param active a string containing a comma delimited list of layers
     * @return Vector startup layers
     */ 
    protected Vector parseClusterNames(String allnames) {
        if (allnames == null)
            return new Vector(0);
        // First, get rid of the quotation marks;
        allnames = allnames.replace('\"', '\0');
        // Next, tokenize the comma delimited string
        StringTokenizer tokens = new StringTokenizer(allnames, " ");
        Vector allnamesvector = new Vector(tokens.countTokens());
        while (tokens.hasMoreTokens()) {
            String name = tokens.nextToken().trim();
            allnamesvector.addElement(name);
        }
        return allnamesvector;
    }

     /**
     * This is called by the TransportNetworkWorker to load the CSV file
     */
    protected QuadTree createData(){
        System.out.println("TransportNetworkLayer: Figuring out the links and names");
        QuadTree qt = new QuadTree(90.0f, -180.0f, -90.0f, 180.0f, 100, 50f);

        if (societyHostURL != null){
            try {
                // Get the network.
                TransportNetwork txnetwork;
                Iterator network_cluster_iter = networkClusters.iterator();
                while (network_cluster_iter.hasNext()){
                    String clustername = (String)network_cluster_iter.next();
                    txnetwork = getNetworkFromCluster(clustername,qt);
                }

                // Load the mil-std-2525B icons
                CgmUtils.loadZipArchive(cgmArchiveURL.toString());
                
                // Now get all the info for the clusters.
                Iterator cluster_iterator = unitNames.iterator();
                while (cluster_iterator.hasNext()){
                    String unitname = (String) cluster_iterator.next();
                    TransportUnit unit = getUnitInfoFromCluster(unitname,qt);
                    
                    if (unit != null) {
                        // Now get all the info for the clusters.
                        Iterator itinerary_cluster_iterator = itineraryClusters.iterator();
                        while (itinerary_cluster_iterator.hasNext()){
                            String itineraryname = (String) itinerary_cluster_iterator.next();
                            getItineraryInfoFromCluster(itineraryname,qt, unitname, unit);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Read failure: "+e);
                e.printStackTrace();
                return qt;
            }
            GeolocTransportNode.addKnownGeolocsToQuadTree(qt);
        }
        else {
            System.err.println("TransportNetworkLayer: No Data source URL specified!!");
        }
        System.err.println("TransportNetworkLayer: Finished loading Network...");

        return qt;
    }
    


    protected void getRoutes(String clustername) {
        /*
          // Now get the Routes. 
          Collection uitxroutes = 
          SocietyUI.getTxRoutes(networkDataSourceURL.toString());
                
          if (uitxroutes != null && !uitxroutes.isEmpty()) {
                    
          if (Debug.debugging("txroutes"))
          System.out.println("Got " + uitxroutes.size()  + " Routes");
                    
          txRoutes = new Vector(uitxroutes.size());
          Iterator routesIterator = uitxroutes.iterator();
                    
          // Go over the uitxroutes, and make TransportRoutes out of each one.
          while (routesIterator.hasNext()){
          UITxRoute uitxroute = (UITxRoute)routesIterator.next();
          TransportRoute route = new TransportRoute(uitxroute,txnetwork);
          route.addToQuadTree(qt);
          txRoutes.add(route);
          }
          } else {
          if (uitxroutes == null)
          System.out.println("Got a null routelist.");
          else 
          System.out.println("Got a route list, but it was empty.");

          }
        */
    }

    
    /**
     * Collect the Network information from the cluster, and add it to the QuadTree 
     */
    protected TransportNetwork getNetworkFromCluster(String clustername, QuadTree qt){
        // Get the Network
        UITxNetwork uitxnetwork = 
            SocietyUI.getTxNetwork(societyHostURL.toString() + "$" + clustername);
        TransportNetwork txnetwork = new TransportNetwork(uitxnetwork);
        txnetwork.BuildNetwork(qt);
        networkGraphics.addAll(txnetwork.getAllGraphics());
        return txnetwork;
    }

    /** 
     * Collect the Unit information from the cluster, and add it to the QuadTree 
     */
    protected TransportUnit getUnitInfoFromCluster (String clustername, QuadTree qt){
        String cluster_url_string = societyHostURL.toString() + "$" + clustername;
        
        if (Debug.debugging("unit"))
            Debug.out.println("\nGetting unit at: " + cluster_url_string);
        
        try {
            // Get the unit's info
            UIUnit uiunit = SocietyUI.getOrgInfo(cluster_url_string);
                        
            if (uiunit != null){
                if (Debug.debugging("unit"))
                    Debug.out.println("Got a UIUnit:\n" + uiunit.toString());
                            
                TransportUnit txunit = new TransportUnit(uiunit);
                txunit.addToQuadTree(qt);
                txUnits.add(txunit);
                unitGraphics.add(txunit);
                return txunit;
            } else {
                System.err.println("No unit object from " + cluster_url_string);
            }
        } catch (NullPointerException npe) {
            System.err.println("NullPointerException while getting "
                               + cluster_url_string);
            if (Debug.debugging("unit") || Debug.debugging("itinerary"))
                npe.printStackTrace();
        }
        return null;
    }
    
    /** 
     * This will collect the itineraries for a particular unit. 
     * I'm not sure what to do with them until I see them. 
     */
    protected void getItineraryInfoFromCluster(String cluster_name,QuadTree qt, 
                                               String unit_filter,
                                               TransportUnit unit){
        Vector task_itineraries = 
            SocietyUI.getTaskItineraries(societyHostURL.toString(),
                                         cluster_name, unit_filter,
                                         null, "LIVE",
                                         false, false, false, false, false, false);
        if (task_itineraries != null){
            if (Debug.debugging("itinerary")){
                Debug.out.println("Got " + task_itineraries.size() + " itineraries");
            }
            TransportItinerary tx_itinerary = new TransportItinerary(task_itineraries);
            itineraries.add(tx_itinerary);
            tx_itinerary.addToQuadTree(qt);
            itineraryGraphics.addAll(tx_itinerary.getAllGraphics());

            //?? if the unit has no location, put it at the beginning of the 
            //?? itinerary
            if (unit.getLocation() == null){
                System.err.println("Unit " + unit_filter + " has no location.  Placing at "
                                   + tx_itinerary.getStartLocation().toString());
                unit.setLocation(tx_itinerary.getStartLocation());
                unit.addToQuadTree(qt);
            }

        } else {
            System.err.println("No itineraries for " + cluster_name);
        }
    }


    /** 
     * Sets the current graphics list to the given list.
     *
     * @param aList a vector of OMGraphics 
     */
    public synchronized void setGraphicList (OMGraphicList aList) {
        omGraphics = aList;
    }
    
    /** 
     * Retrieves a vector of the current graphics list.  
     *
     * @return vector of OMGraphics.
     */
    public synchronized OMGraphicList getGraphicList () {
        return omGraphics;
    }

    /** Used to get the projection to determine where a mouse click occured. */
    public Projection getProjection(){
        return projection;
    }

    /** 
     * Used to set the cancelled flag in the layer.  The swing worker
     * checks this once in a while to see if the projection has
     * changed since it started working.  If this is set to true, the
     * swing worker quits when it is safe. 
     */
    public synchronized void setCancelled(boolean set){
        cancelled = set;
    }

    /** Check to see if the cancelled flag has been set. */
    public synchronized boolean isCancelled(){
        return cancelled;
    }
    
    /**
     * Get the value of networkDataSourceURL.  This is the URL that
     * will give us out network object.
     * @return Value of networkDataSourceURL.  */
    public URL getSocietyHostURL() {return societyHostURL;}
    
    /**
     * Set the value of networkDataSourceURL.
     * @param v  Value to assign to networkDataSourceURL.
     */
    public void setSocietyHostURL(URL  v) {
        if (Debug.debugging("txnetwork")){
            Debug.message("txnetwork", "New societyHostURL=" + v.toString());
        }
        System.out.println("New societyHostURL=" + v.toString());
        this.societyHostURL = v;
    }

    /** 
     * The projectionListener interface method that lets the Layer
     * know when the projection has changes, and therefore new graphics
     * have to created /supplied for the screen.
     *
     * @param e The projection event, most likely fired from a map bean.
     */
    public void projectionChanged (ProjectionEvent e) {
        if (Debug.debugging("basic")){
            Debug.message("basic", getName()+"|TransportNetworkLayer.projectionChanged()");
        }
        
        if (projection != null){
            if (projection.equals(e.getProjection())){
                // Nothing to do, already have it and have acted on it...
                repaint();
                return;
            }
        }
        
        setGraphicList(null);

        projection = (Projection)e.getProjection().makeClone();

        // If there isn't a worker thread working on this already,
        // create a thread that will do the real work. If there is
        // a thread working on this, then set the cancelled flag
        // in the layer.
        if (currentWorker == null) {
            currentWorker = new TransportNetworkWorker();
            currentWorker.execute();
        }
        else setCancelled(true);
    }

    /**  
     * The TransportNetworkWorker calls this method on the layer when it is
     * done working.  If the calling worker is not the same as the
     * "current" worker, then a new worker is created.
     *
     * @param worker the worker that has the graphics.
     */
    protected synchronized void workerComplete (TransportNetworkWorker worker) {
        if (!isCancelled()) {
            currentWorker = null;
            setGraphicList((OMGraphicList)worker.get());
            repaint();
        }
        else{
            setCancelled(false);
            currentWorker = new TransportNetworkWorker();
            currentWorker.execute();
        }
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the link.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     *
     */
    public OMGraphicList prepare () {

        if (isCancelled()){
            if (Debug.debugging("txnetwork")){
                Debug.message("txnetwork", getName() +
                          "|TransportNetworkLayer.prepare(): aborted.");
            }
            return null;
        }

        if (Debug.debugging("basic")){
            Debug.message("basic", getName() + "|TransportNetworkLayer.prepare(): doing it");
        }

        // Setting the OMGraphicList for this layer.  Remember, the
        // Vector is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list.  So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        if (Debug.debugging("txnetwork")) {
            Debug.out.println(getName()
                               + "|TransportNetworkLayer.prepare(): " 
                               + "calling prepare with projection: " + projection 
                               + " ul = " + projection.getUpperLeft() + " lr = "
                               + projection.getLowerRight()); 
        }

        // IF the quadtree has not been set up yet, do it!
        if (quadtree == null){
            quadtree = createData();
        }

        LatLonPoint ul = projection.getUpperLeft();
        LatLonPoint lr = projection.getLowerRight();

        Vector omGraphicVector = null;

        if (quadtree != null){
            if (Debug.debugging("txnetwork")) {
                float delta = lr.getLongitude() - ul.getLongitude();
                System.out.println(
                                   getName()+
                                   "|TransportNetworkLayer.prepare(): " +
                                   " ul.lon = " +
                                   ul.getLongitude() +
                                   " lr.lon = " +
                                   lr.getLongitude() +
                                   " delta = " +
                                   delta); 
            }

            omGraphicVector = quadtree.get(ul.getLatitude(), ul.getLongitude(),
                                         lr.getLatitude(), lr.getLongitude());
        }
        
        /////////////////////
        // safe quit
        int size = 0;
        OMGraphicList omglist = null;
        if (omGraphicVector != null){
            size = omGraphicVector.size();      
            if (Debug.debugging("txnetwork")){
                Debug.message("txnetwork", getName()+
                              "|TransportNetworkLayer.prepare(): finished with " + 
                              size + " graphics");
            }
            
            omglist = new OMGraphicList(size);
            omglist.setTraverseMode(OMGraphicList.FIRST_ADDED_ON_TOP);
                    
            // Don't forget to project them.  Since they are only
            // being recalled if the projection hase changed, then
            // we need to force a reprojection of all of them
            // because the screen position has changed.
            Enumeration things = omGraphicVector.elements();
            while (things.hasMoreElements()){
                Object obj = things.nextElement();
                
                if (obj instanceof OMGraphic){
                    OMGraphic omg = (OMGraphic)obj;
                    omg.generate(projection);
                    if (isGraphicVisible(omg))
                        omglist.addOMGraphic(omg);
                } else {
                    System.err.println("What is " + obj.toString() + " doing here???");
                }
            }
        }
        else if (Debug.debugging("txnetwork")){
            Debug.message("txnetwork", getName()+
                          "|TransportNetworkLayer.prepare(): finished with null graphics list");
        }
        
        return omglist;
    }

    /** A graphic is visible iff it is in one of the sets of visible graphics */
    private boolean isGraphicVisible(OMGraphic g){
        
        if (Debug.debugging("txnetwork")){
            Debug.out.println(g.toString() + " is in " + 
                              (networkGraphics.contains(g) ? "networkGraphics " : "" ) + 
                              (routeGraphics.contains(g) ? "routeGraphics " : "" ) + 
                              (unitGraphics.contains(g) ? "unitGraphics " : "" ) + 
                              (itineraryGraphics.contains(g) ? "itineraryGraphics " : "" ) 
                              ); 
        }

        return ((showNetwork && networkGraphics.contains(g))
                || (showRoutes && routeGraphics.contains(g))
                || (showUnits && unitGraphics.contains(g))
                || (showItineraries && itineraryGraphics.contains(g)));
            
    }

    /**
     * Paints the layer.
     *
     * @param g the Graphics context for painting
     *
     */
    public void paint (java.awt.Graphics g) {
        if (Debug.debugging("txnetwork")){
            Debug.message("txnetwork", getName()+"|TransportNetworkLayer.paint()");
        }
        OMGraphicList list = getGraphicList();
        
        if (list != null){
            if (Debug.debugging("txnetwork")){
                Debug.message("txnetwork", getName()+" has "+ list.size() + " elements");
            }
            list.render(g);
        } else {
            if (Debug.debugging("txnetwork")){
                Debug.message("txnetwork", "paint(): Null list...");
            }
        }
    }

    
    public void rereadData () {
        quadtree = null;
        reFetchGraphics();
        repaint();
    }

    public void reFetchGraphics() {
        currentWorker = new TransportNetworkWorker();
        currentWorker.execute();
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

    //------------------------------------------------------------
    // MapMouseListener implementation
    //------------------------------------------------------------
    public synchronized MapMouseListener getMapMouseListener(){
        return this;
    }

    public String[] getMouseModeServiceList() {
        String[] services = {"Gestures"};
        return services;
    }

    private OMGraphic lastSelectedGraphic = null;
    public boolean mouseMoved(MouseEvent evt) {
        if (!isVisible()) return false;
        OMGraphic graphic;

        // OMGraphic graphic = findClosestMapObject(evt);
        int index = findClosestMapObjectIndex(evt);
        
        if (index < 0)
            graphic = null;
        else
            graphic = getGraphicList().getOMGraphicAt(index);
        
        if (graphic == null) {
            fireRequestInfoLine("");
            if (lastSelectedGraphic != null) 
                lastSelectedGraphic.deselect();
        }
        else {
            if (graphic != lastSelectedGraphic){
                if (lastSelectedGraphic != null) 
                    lastSelectedGraphic.deselect();
                lastSelectedGraphic = graphic;
                graphic.select();  // redundant?
                getGraphicList().moveIndexedToTop(index);
                if (graphic instanceof TransportNetworkElement){
                    fireRequestInfoLine(((TransportNetworkElement)graphic).getInfoLine());
                } 
            }
        }
        
        return true;
    }

    public boolean mousePressed(MouseEvent evt) { return false;}
    public boolean mouseReleased(MouseEvent e) { return false;}
    public boolean mouseClicked(MouseEvent evt) { return false;}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public boolean mouseDragged(MouseEvent e) { return false;}

    public void mouseMoved() {}

    private int findClosestMapObjectIndex(MouseEvent evt) {
        OMGraphicList graphics = getGraphicList();

        if (graphics != null){
            int x = evt.getX();
            int y = evt.getY();
            float limit = 6.0f;

            return graphics.findIndexOfClosest(x,y,limit);
        } 
        return -1;
    }


    //----------------------------------------------------------------------
    // GUI
    //----------------------------------------------------------------------
    /** 
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * 
     * @return Component object representing the palette widgets.
     */

    private String readDataCommand = "READ_DATA";
    private String showNetworkCommand = "SHOW_NETWORK_COMMAND";
    private String showRoutesCommand = "SHOW_ROUTES_COMMAND";
    private String showItinerariesCommand = "SHOW_ITINERARIES_COMMAND";

    public java.awt.Component getGUI() {
        
        JCheckBox showNetworkCheck = new JCheckBox("Show Network", showNetwork);
        showNetworkCheck.setActionCommand(showNetworkCommand);
        showNetworkCheck.addActionListener(this);

//      JCheckBox showRoutesCheck = new JCheckBox("Show Routes", showRoutes);
//      showRoutesCheck.setActionCommand(showRoutesCommand);
//      showRoutesCheck.addActionListener(this);
        
        JCheckBox showItinerariesCheck = new JCheckBox("Show Itineraries", showItineraries);
        showItinerariesCheck.setActionCommand(showItinerariesCommand);
        showItinerariesCheck.addActionListener(this);

        JButton rereadFilesButton = new JButton("Re-Fetch Network");
        rereadFilesButton.setActionCommand(readDataCommand);
        rereadFilesButton.addActionListener(this);
        
        Box box = Box.createVerticalBox();
        box.add(showNetworkCheck);
        // box.add(showRoutesCheck);
        box.add(showItinerariesCheck);
        box.add(rereadFilesButton);
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
        if (cmd == showNetworkCommand) {                
            JCheckBox locationCheck = (JCheckBox)e.getSource();
            showNetwork = locationCheck.isSelected();
            reFetchGraphics();
            if(Debug.debugging("txnetwork"))
                System.out.println("TransportNetworkLayer::actionPerformed showNetwork is " 
                                   + showNetwork);
            //repaint();
        } else if (cmd == showRoutesCommand) {
            JCheckBox namesCheck = (JCheckBox)e.getSource();
            showRoutes = namesCheck.isSelected();
            reFetchGraphics();
            repaint();
        } else if (cmd == showItinerariesCommand) {
            JCheckBox namesCheck = (JCheckBox)e.getSource();
            showItineraries = namesCheck.isSelected();
            reFetchGraphics();
            repaint();
        } else if (cmd == readDataCommand) {
            System.out.println("Re-reading Network");
            rereadData();
        } else  {
            System.err.println("Unknown action command \"" + cmd +
                               "\" in TransportNetworkLayer.actionPerformed().");
        }
    }
}
