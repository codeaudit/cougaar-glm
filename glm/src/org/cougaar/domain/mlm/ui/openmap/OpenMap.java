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

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.beans.Beans;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.gui.*;
import com.bbn.openmap.proj.*;
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
 * $Source: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/openmap/Attic/OpenMap.java,v $
 * $RCSfile: OpenMap.java,v $
 * $Revision: 1.1 $
 * $Date: 2000-12-15 20:17:53 $
 * $Author: mthome $
 * 
 * ***********************************************************************/

/**
 * The OpenMap Viewer application.
 * <p>
 * This is a sample application using the MapBean.
 *
 */

public class OpenMap {

    /** Property for space separated layers. */
    public static final String layersProperty = "openmap.layers";

    /** Property for space separated layers to be displayed at startup. */
    public static final String startUpLayersProperty = "openmap.startUpLayers";

    /** The name of the properties file to read. */
    public static String propsFileName = "openmap.properties";

    /** The name of the system directory containing a properties file. */
    public static String configDirProperty = "openmap.configDir";

    /** Starting X coordinate of window */
    public static transient final String xProperty = "openmap.x";

    /** Starting Y coordinate of window */
    public static transient final String yProperty = "openmap.y";

    /** The application properties. */
    protected Properties props;

    /** Map Window */
    protected MapBean map;
    /** The suite of control widgets. */
    protected ToolPanel controls;
    /** The Information and Status Manager. */
    protected InformationDelegator info;
    /** The suite of menus. */
    protected MenuPanel menu;
    /** The layer handler, for dynamic adjustments of layers. */
    protected LayerHandler layerHandler;

    // the MapBean and the gui beans
//      static final String mapClass = "com.bbn.openmap.BufferedMapBean";
//      static final String toolClass = "com.bbn.openmap.gui.ToolPanel";
//      static final String menuClass = "com.bbn.openmap.gui.MenuPanel";
//      static final String infoClass = "com.bbn.openmap.InformationDelegator";

    /**
     * Loads properties from a java resource.  This will load the
     * named resource identifier into the given properties instance.
     *
     * @param properties the Properties instance to receive the properties
     * @param resourceName the name of the resource to load
     * @param verbose indicates whether status messages should be printed
     */
    protected boolean loadPropertiesFromResource(Properties properties,
                                                 String resourceName,
                                                 boolean verbose)
    {
        InputStream propsIn = getClass().getResourceAsStream(resourceName);

        if (propsIn == null) {

            if (verbose) {
                System.err.println("Unable to locate resources: "
                                   + resourceName);
            }
            return false;

        } else {

            try {
                properties.load(propsIn);
                return true;
            } catch (java.io.IOException e) {
                if (verbose) {
                    System.err.println("Caught IOException loading resources: "
                                       + resourceName);
                }
                return false;
            }           

        }
    }

    /**
     * Loads properties from a java resource.  This will load the
     * named resource identifier into the given properties instance.
     *
     * @param properties the Properties instance to receive the properties
     * @param resourceName the name of the resource to load
     * @param verbose indicates whether status messages should be printed
     */
    public boolean loadProperties(URL url, boolean verbose)
    {

        try {
            InputStream propsIn = url.openStream();
            props.load(propsIn);
            return true;
        } catch (java.io.IOException e) {
            if (verbose) {
                System.err.println("Caught IOException loading resources: "
                                   + url);
            }
            return false;
        }               

    }

    /**
     * Load the named file from the named directory into the given
     * <code>Properties</code> instance.  If the file is not found
     * a warning is issued.  If an IOExceptio occurs, a fatal error
     * is printed and the application will exit.
     *
     * @param props the instance to receive the loaded properties
     * @param dir the directory where the properties file resides
     * @param file the name of the file
     */
    public void loadProps(Properties props, String dir, String file) {
        File propsFile = new File(dir, file);

        try {
            InputStream propsStream = new FileInputStream(propsFile);
            props.load(propsStream);
        } catch (java.io.FileNotFoundException e) {
//          System.err.println("Unable to read configuration file \""
//                             + propsFile + "\"");
        } catch (java.io.IOException e) {
            System.err.println("Caught IO Exception reading "
                               + "configuration file \""
                               + propsFile + "\"");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Initializes this application.  This is a 3 step process:
     * <ol>
     * <li>
     * Read base properties from the resource file
     * com.bbn.openmap.app.openmap.properties.
     * </li>
     * <li>
     * Read properties from the openmap installion, if they exist.
     * This is usually <openmapInstallDir>/openmap.properties.
     * </li>
     * <li>
     * Read user properties from $HOME/openmap.properties.  This is
     * based on the JDK system property <code>user.home</code>
     * </li>
     * </ol>
     */
    public void initApplication() {

        // load properties from resource file
        loadPropertiesFromResource(props, propsFileName, false);

        if (Environment.isApplication()) {

            // load properties from system area
            String configDir = System.getProperty(configDirProperty);
            if (configDir != null) {
                loadProps(props, configDir, propsFileName);
            } else {
                // For now, don't complain if the system level properties
                // File is missing.
            }

            // load properties from home directory
            String homeDir = System.getProperty("user.home");
            if (homeDir != null) {
                loadProps(props, homeDir, propsFileName);
            } else {
                // For now, don't complain if the home directory level
                // properties File is missing.
            }

            // create a properties list that contains the user properties
            // as defaults.
            props = new Properties(props);

            // System properties are the absolute toplevel
            Properties sysProps = System.getProperties();
            Enumeration keys = sysProps.keys();
            int len = sysProps.size();
            for (int i=0; i<len; i++) {
                Object key = keys.nextElement();
                props.put(key, sysProps.get(key));
            }
        }

        // initialize the Environment with the hierarchical properties
        // list
        Environment.init(props);
    }

    /**
     * Initialize the OpenMap Environment.
     *
     * @param applet an applet or null
     */
    public static void init(Applet applet) {

        if (applet == null) {

            // do if we're running as an application

            Properties p = System.getProperties();

            // First initialize debugging
            Debug.init(p);

            Environment.init(p);

        } else {

            // Initialize as an applet
            Debug.init(applet,
                       new String[] {"debug.basic",
                                         "debug.cspec",
                                         "debug.layer",
                                         "debug.mapbean",
                                         "debug.plugin"
                                         }
                       );
            Environment.init(applet);

        }
    }

    /**
     * Start OpenMap Viewer as a standalone application.
     *
     * @param args String[] curently ignored
     */
    public static void main (String[] args) {

        // static initializations of Debugging and Environment
        init(null);

        // start instance of OpenMap
        new OpenMap().init();
    }


    public OpenMap () {
        props = new Properties();
    }

    /**
     * Launch OpenMap.
     */
    public void init () {

        initApplication();

        JFrame frame = null;
        JRootPane rootPane = null;

        // get the Root window
        if (Environment.isApplication()) {
            frame = new JFrame(Environment.get(Environment.Title));
            rootPane = frame.getRootPane();

            // listen for window close event
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // need a shutdown event to notify other gui beans and
                    // then exit.
                    System.exit(0);
                }
            });
        } else {
            rootPane = ((JApplet)(Environment.getApplet())).getRootPane();
        }

        // output version information
//      System.out.println("OpenMap Viewer " + Environment.get(
//          Environment.Version));
//      System.out.println("Build " + Environment.get(
//          Environment.BuildDate, "<no build tag>"));
//      System.out.println(
//              "OpenMap Viewer running inside " +
//              Environment.get("java.vendor") + " Java VM on " +
//              Environment.get("os.name"));


        // instantiate the MapBean and other gui beans by sending them
        // off to be set.  Make the call to set the specific type of
        // beans to use.  If something was set to null, fill in the
        // default.
        setWidgets();

        map = getMapBean();
        if (map == null){
            map = new BufferedMapBean();
        }

        controls = getToolPanel();
        //  If controls aren't set, seems silly to try and set an
        //  empty default.

        info = getInformationDelegator();
        if (info == null) {
            info = new InformationDelegator();
            // InformationDelegator handles the display of popup text/html
            // information.
            info.setMap(map);
            info.setFloatable(false);
        }

        menu = getMenuPanel();
        if (menu == null) {
            menu = new MenuPanel();
        }

        Layer[] otLayers = null;
        try {
            otLayers = getLayers(props);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }

        // add the menu
        rootPane.setJMenuBar(menu);

        // need a reference to the desktop for use by JInternalFrame
        final JLayeredPane desktop = rootPane.getLayeredPane();
        desktop.setOpaque(true);


        // Create the LayersMenu and the LayersPanel.  Both show the list of
        // layers.  With the LayersPanel, you can change the position of
        // layers and bring up their palettes.
        if (otLayers == null) {
            System.err.println("No valid layers read.");
            System.err.println("Exiting.");
            System.exit(1);
            return;
        }

        layerHandler = getLayerHandler();
        if (layerHandler == null){
            layerHandler = new LayerHandler(otLayers, desktop);
        } else {
            layerHandler.init(otLayers, desktop);
        }

        menu.add(layerHandler.getLayersMenu());

        // Add an extra help menu to the menubar.  Have the InformationDelegator
        // show the help pages.
        JMenu helpMenu =  new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem mi = helpMenu.add(new JMenuItem("OpenMap"));
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                info.displayURL(Environment.get(Environment.HelpURL, 
                "http://javamap.bbn.com/projects/openmap/openmap_maindes.html"));
            }
        });
        mi.setActionCommand("showHelp");
        //menu.setHelpMenu(helpMenu);Use this when Sun decides to implement it 
        menu.add(helpMenu);

        // set up the listeners

        // listen for changes from the map
        map.addPropertyChangeListener(layerHandler.getLayersMenu());
        map.addPropertyChangeListener(layerHandler.getLayersPanel());
        // map listens for LayerEvents
        layerHandler.addLayerListener(map);
        menu.setupListeners(map);

        // Add the toolbar to the top of the window
        if (controls != null)
            rootPane.getContentPane().add("North", controls);

        // Initialize the map projection, scale, center with user prefs or
        // defaults
        String projName = Environment.get(Environment.Projection, Mercator.MercatorName);
        int projType = ProjectionFactory.getProjType(projName);
        map.setProjectionType(projType);
        map.setScale(Environment.getFloat(Environment.Scale, Float.POSITIVE_INFINITY));
        map.setCenter(new LatLonPoint(
                Environment.getFloat(Environment.Latitude, 0f),
                Environment.getFloat(Environment.Longitude, 0f)
                ));

        // Add the map and then the status line to the window
        rootPane.getContentPane().add("Center", map);
        rootPane.getContentPane().add("South", info);

        // Make it look a little prettier
        map.setBorder(new BevelBorder(BevelBorder.LOWERED));

        // Add the layers listed in the startUpLayersProperty to the map
        Vector startuplayers
            = parseStartUp(props.getProperty(startUpLayersProperty));
        for (int i = 0; i < otLayers.length; i++) {
            if (startuplayers.contains(otLayers[i].getMarker())) {
                map.add(otLayers[i]);
            }
        }

        // show the window
        if (Environment.isApplication()) {
            // get starting width and height
            int w = Integer.parseInt(Environment.get(Environment.Width, "640"));
            int h = Integer.parseInt(Environment.get(Environment.Height, "480"));

            // get starting x and y position.  default to center of
            // screen.
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int x = Environment.getInteger(xProperty, -1);
            int y = Environment.getInteger(yProperty, -1);
            if (x < 0)
                x = d.width/2 - w/2;
            if (y < 0)
                y = d.height/2 -h/2;

            // compose the frame, but don't show it here
            frame.setBounds(x, y, w, h);
            frame.show();
        }
    }

    /**  
     * Sets the type of gui components to be used in the OpenMap
     * application.  You are responsible for hooking them up here,
     * too, if you want them to communication with each other.
     */
    public void setWidgets(){
        MapBean map = new BufferedMapBean();
        setMapBean(map);

        // MouseDelegator multiplexes the mouse input to layers and beans
        // which are MapMouseListeners
        MouseDelegator md = new MouseDelegator(map);
        md.setDefaultMouseModes();

        ToolPanel tp = new ToolPanel();
        OMToolSet omts = new OMToolSet();
        omts.setupListeners(map); // Hook up the map as a listener
        omts.addMouseModes(md);
        tp.add(omts);
        tp.setFloatable(false);// cannot detach
        setToolPanel(tp);
        
        
        InformationDelegator id = new InformationDelegator();
        // InformationDelegator handles the display of popup text/html
        // information.
        id.setMap(map);
        id.setFloatable(false);
        // Get the wholine to not update lat lons if something else
        // is displayed.
        md.addPropertyChangeListener(id);
        setInformationDelegator(id);

        MenuPanel mp = new MenuPanel();
        mp.setMouseDelegator(md);
        setMenuPanel(mp);

        setLayerHandler(new LayerHandler());
    }

    /**
     * Parse the list of layers from the Environment containing the
     * layers that should be displayed upon map startup.
     * <p>
     * @param active a string containing a comma delimited list of layers
     * @return Vector startup layers
     */ 
    protected Vector parseStartUp(String active) {
        if (active == null)
            return new Vector(0);
        // First, get rid of the quotation marks;
        active = active.replace('\"', '\0');
        // Next, tokenize the comma delimited string
        StringTokenizer tokens = new StringTokenizer(active, " ");
        Vector activelayers = new Vector(tokens.countTokens());
        while (tokens.hasMoreTokens()) {
            String name = tokens.nextToken().trim();
            activelayers.addElement(name);
        }
        return activelayers;
    }

    protected String getNextOverlayToken(StreamTokenizer st)
        throws java.io.IOException
    {

        int result = st.nextToken();
        switch (result) {

        case StreamTokenizer.TT_EOF:
//          System.out.println("Got eof.");
            return null;

        case StreamTokenizer.TT_EOL:
//          System.out.println("Got eol.");
            return null;

        case StreamTokenizer.TT_WORD:
//          System.out.println("Got word: " + st.sval);
            return st.sval;

        case StreamTokenizer.TT_NUMBER:
            System.err.println("Got number: " + st.nval);
            return Double.toString(st.nval);

        case '"':
//          System.out.println("Got string: " + st.sval);
            return st.sval;

        default:
            System.err.println("Got unknown result: " + result +
                               " ('" + (char)result +
                               "') in OpenMap.getNextOverlayToken");
            return null;
        }
    }

    protected String getRestOfLine(StreamTokenizer st)
        throws java.io.IOException
    {
        StringBuffer buf = new StringBuffer();
        String token;
        while ((token = getNextOverlayToken(st)) != null) {
//          System.out.println("Appending " + token);
            buf.append(token).append(" ");
        }
        return buf.toString();
    }

    /**
     * Initializes the StreamTokenizer with comment and word characters
     * specialized for reading the layers data file.  Parsing of numbers
     * is disabled; we just want them as strings, the layers themselves
     * will do the parsing to number.  [ The real problem is that the
     * default parsing leaves you with e notation doubles, which aren't
     * what most layers are going to want.  By passing them as strings
     * we are able to pass along exactly what was in the file. ]
     *
     * @param st the tokenizer for the layer data file
     */
    protected void initLayerTokenizer(StreamTokenizer st) {
        st.resetSyntax();

        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, ' ');
        st.quoteChar('"');
        st.quoteChar('\'');

        // Use '#' as the comment character ala shell scripts and makefiles
        st.commentChar('#');

        // Include these characters in words.  They are useful in parsing
        // URLs and underscored_words
        st.wordChars('(', '(');
        st.wordChars(')', ')');
        st.wordChars('/', '/');
        st.wordChars('|', '|');
        st.wordChars(':', ':');
        st.wordChars('_', '_');
        st.wordChars('~', '~');
        st.wordChars('-', '-');
        st.wordChars('.', '.');
        st.wordChars('0', '9');

        st.eolIsSignificant(true);
    }

    /**
     * Reads a layer data file with entries for each possible layer.
     *
     * @param url the url of the layer data file
     * @return an array of <code>Layer</code> instances
     */
    protected Layer[] getLayers2(URL url) {
        InputStream in = null;

        try {
            in = url.openStream();
        } catch (java.io.IOException e) {
            System.err.println("Unable to connect to layer list at \"" +
                               url + "\"");
            return null;
        }

        // Use InputStreamReader for Internationalized files.
        StreamTokenizer st = new StreamTokenizer(new InputStreamReader(in));
        initLayerTokenizer(st);
        Vector layers = new Vector();

        while (true) {
            try {
                String layerName = getNextOverlayToken(st);
                String layerClass = getNextOverlayToken(st);
                String rest = getRestOfLine(st);

//              System.out.println("Name: " + layerName);
//              System.out.println("Class: " + layerClass);
//              System.out.println("Argv: " + rest);

                Layer l = (Layer)Beans.instantiate(null, layerClass);
                l.setName(layerName);

                try {
                    l.setArgs(makeArgv(rest));

//                  System.out.println("Created: " + l);
                    layers.addElement(l);
                } catch (java.lang.IllegalArgumentException e) {
                    System.err.println("Error initializing layer "
                                       + layerName);
                    System.err.println("\t" + e);
                    System.err.println("Skipping this entry.");
                }

            } catch (java.io.EOFException e) {
                break;
            } catch (java.lang.ClassNotFoundException e) {
                System.err.println("Error in layer file " + url);
                System.err.println("\t" + e);
                System.err.println("Skipping this entry.");
                continue;
            } catch (java.io.IOException e) {
                System.err.println("Error in layer file " + url);
                System.err.println("\t" + e);
                break;
            }
        }
        if (layers.isEmpty()) {
            return null;
        } else {
            int nLayers = layers.size();
            Layer result[] = new Layer[nLayers];
            layers.copyInto(result);
            return result;
        }
    }

    protected Layer[] getLayers(URL url) {
        InputStream is = null;

        try {
            is = url.openStream();
        } catch (java.io.IOException e) {
            System.err.println("Unable to connect to layer list at \"" +
                               url + "\"");
            return null;
        }

        // Use InputStreamReader for Internationalized files.
        InputStreamReader isr = new InputStreamReader(is);
        LineNumberReader lnr = new LineNumberReader(isr);
        Vector layers = new Vector();
        String layerEntry;

        try {

            while ((layerEntry = lnr.readLine()) != null) {
//              System.out.println("========================================");
                StringReader sr = new StringReader(layerEntry);
                StreamTokenizer st = new StreamTokenizer(sr);
                initLayerTokenizer(st);

                String layerName = getNextOverlayToken(st);
                if (layerName == null) continue;

                String layerClass = getNextOverlayToken(st);
                String rest = getRestOfLine(st);

//              System.out.println("Name: " + layerName);
//              System.out.println("Class: " + layerClass);
//              System.out.println("Argv: " + rest);

                if (layerClass == null) {
                    System.err.println("Warning -- "
                                       + "Malformed layer entry at line "
                                       + lnr.getLineNumber() + " of "
                                       + url);
                    System.err.println("\tExpecting at least a layer name"
                                       + " and class.");
                    System.err.println("\tGot \"" + layerEntry);
                    System.err.println("Skipping this entry.");
                    continue;
                }

                try {
                    Layer l = (Layer)Beans.instantiate(null, layerClass);
                    l.setName(layerName);

                    l.setArgs(makeArgv(rest));

//                  System.out.println("Created: " + l);
                    layers.addElement(l);
                } catch (java.lang.IllegalArgumentException e) {
                    System.err.println("Error initializing layer "
                                       + layerName);
                    System.err.println("\t" + e);
                    System.err.println("Skipping this entry.");
                } catch (java.lang.ClassNotFoundException e) {
                    System.err.println("Error in layer file " + url);
                    System.err.println("\t" + e);
                    System.err.println("Skipping this entry.");
                }
//              System.out.println("========================================");
            }
        } catch (java.io.IOException e) {
            System.err.println("Exception caught reading layer file "
                               + url + ":");
            System.err.println("\t" + e);
            return null;
        }

        if (layers.isEmpty()) {
            return null;
        } else {
            int nLayers = layers.size();
            Layer result[] = new Layer[nLayers];
            layers.copyInto(result);
            return result;
        }
    }

    /**
     * Creates an argv vector from the given string.
     *
     * @param args a string of whitespace separated tokens
     * @return a vector of strings corresponding to the tokens in args
     */
    protected String[] makeArgv(String args) {
        if (args == null) {

            return new String[0];

        } else {

            StringTokenizer st = new StringTokenizer(args);
            int nTokens = st.countTokens();
            String result[] = new String[nTokens];
            for (int i=0; i<nTokens; i++) {
                result[i] = st.nextToken();
            }
            return result;

        }
    }

    /**
     *
     */
    protected Layer[] getLayers(Properties p) {
//      System.out.println("Getting new layers");

        String layersValue = p.getProperty(layersProperty);
//      System.out.println("layersValue = \"" + layersValue + "\"");

        if (layersValue == null) {
            System.err.println("No property \"" + layersProperty
                               + "\" found in application properties.");
            return null;
        }

        StringTokenizer tokens = new StringTokenizer(layersValue, " ");
        Vector layerNames = new Vector();
        while(tokens.hasMoreTokens()) {
            layerNames.addElement(tokens.nextToken());
        }

        if (Debug.debugging("viewer")) {
            System.out.println("OpenMap.getLayers(): "+layerNames);
        }

        int nLayerNames = layerNames.size();
        Vector layers = new Vector(nLayerNames);
        for (int i = 0; i < nLayerNames; i++) {
            String layerName = (String)layerNames.elementAt(i);
            String classProperty = layerName + ".class";
            String className = p.getProperty(classProperty);
            if (className == null) {
                System.err.println("Failed to locate property \""
                                   + classProperty + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
                continue;
            }
            try {
                if (Debug.debugging("viewer")) {
                    System.out.println(
                            "OpenMap.getLayers(): " +
                            "instantiating layer \""+className+"\"");
                }
//              Object obj = java.beans.Beans.instantiate(null, className);// Doesn't work for applet!
                Object obj = Class.forName(className).newInstance();// Works for applet!
                if (obj instanceof Layer) {
                    Layer l = (Layer) obj;
                    l.setProperties(layerName, p);
                    layers.addElement(l);
                }
                if (false) throw new java.io.IOException();//fool javac compiler
            } catch (java.lang.ClassNotFoundException e) {
                System.err.println("Layer class not found: \""
                                   + className + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
            } catch (java.io.IOException e) {
                System.err.println("IO Exception instantiating class \""
                                   + className + "\"");
                System.err.println("Skipping layer \"" + layerName + "\"");
            } catch (Exception e) {
                System.err.println("Exception instantiating class \""
                                   + className + "\": " + e);
            }
        }

        int nLayers = layers.size();
        if (nLayers == 0) {
            return null;
        } else {
            Layer[] value = new Layer[nLayers];
            layers.copyInto(value);
            return value;
        }
    }

    /**
     * Set the MapBean to be this map.  This should be called in the
     * setWidgets() method, if you want to use some MapBean other than
     * the BufferedMapBean.
     * @param newMap the MapBean to use.
     */
    protected void setMapBean(MapBean newMap){
        map = newMap;
    }

    /**
     * Get the MapBean to use - called in init(), in case you wanted
     * to use something slightly different.
     * @return MapBean
     */
    protected MapBean getMapBean(){
        return map;
    }

    /**
     * Set the ToolPanel to use.  This should be called in the
     * setWidgets() method, if you want to use some ToolPanel other than
     * the regular ToolPanel.
     * @param newControls the tool panel to use.
     */
    protected void setToolPanel(ToolPanel newControls){
        controls = newControls;
    }

    /**
     * Get the tool panel to Use - called in init(), in case you
     * wanted to use something slightly different.
     */
    protected ToolPanel getToolPanel(){
        return controls;
    }

    /**
     * Set the Information Delegator to use.  This should be called in the
     * setWidgets() method, if you want to use something other than
     * the regular InformationDelegator.
     * @param newInfo the information delegator to use.
     */
    protected void setInformationDelegator(InformationDelegator newInfo){
        info = newInfo;
    }

    /**
     * Get the information delegator to use - called in init(), in
     * case you wanted to use something slightly different.
     * @return InformationDelegator
     */
    protected InformationDelegator getInformationDelegator(){
        return info;
    }

    /**
     * Set the Menus to use.  This should be called in the
     * setWidgets() method, if you want to use something other than
     * the regular set of menus..
     * @param newMenu the menus to use.
     */
    protected void setMenuPanel(MenuPanel newMenu){
        menu = newMenu;
    }

    /**
     * Get the menus to use - called in init(), in case you wanted
     * to use something slightly different.
     * @return MenuPanel
     */
    protected MenuPanel getMenuPanel(){
        return menu;
    }

    /** 
     * Set the layer handler for the cale - called in init(), in case
     * you want something different.  
     * @param newLayerHandler
     */
    public void setLayerHandler(LayerHandler newLayerHandler){
        layerHandler = newLayerHandler;
    }

    /**
     * Get the layer handler to use.
     * @return LayerHandler
     */
    public LayerHandler getLayerHandler(){
        return layerHandler;
    }
}
