/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.aggregation;

import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JLabel;

import org.cougaar.domain.mlm.ui.tpfdd.util.ExceptionTools;
import org.cougaar.domain.mlm.ui.tpfdd.util.MismatchException;
import org.cougaar.domain.mlm.ui.tpfdd.util.OutputHandler;
import org.cougaar.domain.mlm.ui.tpfdd.util.Debug;
import org.cougaar.domain.mlm.ui.tpfdd.util.Fifo;
import org.cougaar.domain.mlm.ui.tpfdd.util.ProducerImpl;

import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleProducer;
import org.cougaar.domain.mlm.ui.tpfdd.gui.model.SimpleItemPoolModelProducer;

import org.cougaar.domain.mlm.ui.tpfdd.producer.ItineraryProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.CannedProducer;
import org.cougaar.domain.mlm.ui.tpfdd.producer.ClusterCache;

import org.cougaar.domain.mlm.ui.tpfdd.gui.view.QueryData;
import org.cougaar.domain.mlm.ui.tpfdd.gui.view.MessageArea;
import org.cougaar.domain.mlm.ui.tpfdd.gui.view.TaskNode;

import org.cougaar.domain.mlm.ui.tpfdd.transit.*;

/**
 * The invocation class for the aggregation server.  Front-end design is
 * a common network server architecture:<ul>
 * <li>One thread is started per client connection, the {@link
 * RequestThread} inner class.
 * <li>A single answering thread, the {@link AnswerThread} inner class.</ul><p>
 * The threads communicate using a shared, synchronized request queue,
 * the {@link Fifo} class and standard Object <code>wait</code> and
 * <code>notify</code> calls.
 * Back-end design is customized for ALP. For every cluster stored in
 * the {@link ClusterList}, an {@link ItineraryProducer} is started in
 * its own thread.  
 */
 
public class Server implements ActionListener, ServerCommands
{
    private JMenuBar menuBar = null;
    private JMenu fileMenu = null;
    private JMenuItem saveNodeItem = null;
    private JMenuItem saveItinItem = null;
    //    private JMenuItem loadItem = null;
    //    private JMenuItem pretendItem = null;
    private JMenuItem quitItem = null;

    private JPanel buttonPanel;
    private JButton reloadButton = null;
    private JButton loadliveButton = null;
    private JButton loadcannedButton = null;

    private JRadioButton liveDataButton = null;
    private JRadioButton cannedDataButton = null;
    private JLabel liveText;
    private JLabel cannedText;
  

    public static final int CLIENT_PORT = 1111;
    private ServerPlanElementProvider provider;
    private ServerPlanElementProvider cannedProvider;
    private CannedProducer cannedProducer;
    private Vector itineraryProducers = null;
    private int interval;
    private ClusterCache clusterCache;
    private OutputHandler messageHandler = null;
    private JScrollPane messageScroller = null;
    private JScrollPane debugScroller = null;
    private JPanel containerPanel = null;
    private ServerSocket listener;
    private Socket socket;
    private Fifo requests;
    private boolean useCanned = false;
    private String host;
    private String demandHost;
    private String defaultOosDir = null;
  private static boolean useSubordinatesPSP = true;

  private final static String CWD = System.getProperty("user.dir", "C:\\");

    public Server(int intval, ClusterCache clustCache, String defOosDir)
    {
        interval = intval;
        clusterCache = clustCache;
	defaultOosDir = defOosDir;
        getMessageScroller();

        JFrame frame = new JFrame();
        frame.setSize(640, 480);
        frame.setJMenuBar(getmenuBar());
        frame.setTitle(" ALPINE Aggregation Server");
        frame.getContentPane().add(getContainerPanel(), "Center");
        frame.addWindowListener(new WindowAdapter()
                                {
                                  public void windowClosing(WindowEvent e)
                                    {
                                      System.exit(0);
                                    }
                                });
        frame.setVisible(true);
        init();
        handleConnections();

    }



    private JMenuItem getsaveNodeItem()
    {
      if ( saveNodeItem == null ) {
        try {
          saveNodeItem = new JMenuItem();
          saveNodeItem.setName("saveNodeItem");
          saveNodeItem.setText("Save Current Pre-Processed State to File");
          saveNodeItem.setEnabled(true);
          saveNodeItem.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return saveNodeItem;
    }

    private JMenuItem getsaveItinItem()
    {
      if ( saveItinItem == null ) {
        try {
          saveItinItem = new JMenuItem();
          saveItinItem.setName("saveItinItem");
          saveItinItem.setText("Save Current Raw State to File");
          saveItinItem.setEnabled(true);
          saveItinItem.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return saveItinItem;
    }

//      private JMenuItem getloadItem()
//      {
//        if ( loadItem == null ) {
//          try {
//            loadItem = new JMenuItem();
//            loadItem.setName("loadItem");
//            loadItem.setText("Load Either State from File");
//            loadItem.setEnabled(true);
//            loadItem.addActionListener(this);
//          }
//          catch ( Exception e ) {
//            handleException(e);
//          }
//        }
//        return loadItem;
//      }

  /*    private JMenuItem getpretendItem()
    {
      if ( pretendItem == null ) {
        try {
          pretendItem = new JCheckBoxMenuItem();
          pretendItem.setName("pretendItem");
          pretendItem.setText("Use canned data for live queries");
          pretendItem.setSelected(false);
          pretendItem.setEnabled(true);
          pretendItem.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return pretendItem;
    }
  */
    private JMenuItem getquitItem()
    {
      if ( quitItem == null ) {
        try {
          quitItem = new JMenuItem();
          quitItem.setName("quitItem");
          quitItem.setText("Quit");
          quitItem.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return quitItem;
    }

    private JMenu getfileMenu()
    {
      if ( fileMenu == null ) {
        try {
          fileMenu = new JMenu();
          fileMenu.setName("fileMenu");
          fileMenu.setText("File");
          fileMenu.add(getsaveNodeItem());
          fileMenu.add(getsaveItinItem());
	  //          fileMenu.add(getloadItem());
          //          fileMenu.add(getpretendItem());
          fileMenu.add(new JSeparator());
          fileMenu.add(getquitItem());
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return fileMenu;
    }
    
    private JButton getreloadButton()
    {
      if ( reloadButton == null ) {
        try {
          reloadButton = new JButton();
          reloadButton.setName("ReloadButton");
          reloadButton.setText("Force Reload");
          reloadButton.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return reloadButton;
    }
    private JButton getloadliveButton()
    {
      if ( loadliveButton == null ) {
        try {
          loadliveButton = new JButton();
          loadliveButton.setName("LoadLiveButton");
          loadliveButton.setText("Load new live data");
          loadliveButton.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return loadliveButton;
    }

    private JButton getloadcannedButton()
    {
      if ( loadcannedButton == null ) {
        try {
          loadcannedButton = new JButton();
          loadcannedButton.setName("LoadCannedButton");
          loadcannedButton.setText("Load canned data");
          loadcannedButton.addActionListener(this);
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return loadcannedButton;
    }

    private JPanel getbuttonPanel()
    {
      if ( buttonPanel == null ) {
        try {
          buttonPanel = new JPanel();
          buttonPanel.setName("ButtonPanel");
          buttonPanel.setLayout(new FlowLayout());
          buttonPanel.add(getreloadButton());
          buttonPanel.add(getloadliveButton());
          buttonPanel.add(getloadcannedButton());
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return buttonPanel;
    }
    
    private JMenuBar getmenuBar()
    {
      if ( menuBar == null ) {
        try {
          menuBar = new JMenuBar();
          menuBar.setName("MenuBar");
          menuBar.add(getfileMenu());
          menuBar.add(createSourceButtons());
        }
        catch ( Exception e ) {
          handleException(e);
        }
      }
      return menuBar;
    }

  /*
   * Make the panel that holds the source buttons and fields
   */
  private JPanel createSourceButtons() {
    JPanel panel = new JPanel();
    ButtonGroup dataGroup = new ButtonGroup();
    JLabel label = new JLabel("Data Source    ");
    cannedDataButton = new JRadioButton("Canned: ");
    liveDataButton = new JRadioButton("Live: ");
    liveText = new JLabel("               ");
    cannedText = new JLabel("               ");
    
    // Make the buttons actually do some work
    dataGroup.add(cannedDataButton);
    dataGroup.add(liveDataButton);
    cannedDataButton.setName("Canned Data");
    cannedDataButton.addActionListener(this);
    liveDataButton.setName("Live Data");
    liveDataButton.addActionListener(this);
    liveDataButton.setSelected(true);

    // Make everything show up pretty
    panel.setBorder(BorderFactory.createLineBorder(Color.black));
    panel.add (label);
    panel.add(liveDataButton);
    panel.add(liveText);
    panel.add(cannedDataButton);
    panel.add(cannedText);

    return panel;
                                       
  }

    private void reload()
    {
      OutputHandler.out("Server:reload Loading all aggregation server data.");
      if ( getitineraryProducers() == null ) {
        OutputHandler.out("Server:reload canceled, no cluster host configured.");
        return;
      }
      for ( Iterator i = getitineraryProducers().iterator(); i.hasNext(); ) {
        ItineraryProducer producer = (ItineraryProducer)(i.next());
        producer.deleteConsumer(getprovider());
      }
      provider = null;
      for ( Iterator i = getitineraryProducers().iterator(); i.hasNext(); ) {
        ItineraryProducer producer = (ItineraryProducer)(i.next());
        producer.addConsumer(getprovider());
        producer.forcePoll();
      }
    }
    private void loadNewLive()
    {
	  host = clusterCache.guiSetHost();
	  demandHost = clusterCache.guiSetDemandHost();

	OutputHandler.out("Server:loadNewLive loading new aggregation server data.");
	if ( itineraryProducers == null) {
	    reload();
	}
	else {
	    for(Iterator i = getitineraryProducers().iterator();i.hasNext();) {
		ItineraryProducer producer = (ItineraryProducer)(i.next());
		producer.deleteConsumer(getprovider());
	    }
	    provider = null;
	    itineraryProducers = null; // need to null for reload to work
	    liveText.setText(host);
 	    OutputHandler.out("Server:loadNewLive deleted former producers now calling reload");
	    reload();
	}
    }
    private class RequestThread extends Thread
    {
      private Socket socket;
      private ObjectOutputStream writer;
      private ObjectInputStream reader;
      
      public RequestThread(Socket socket)
      {
        super();
        this.socket = socket;
      }

      public ObjectOutputStream getWriter()
      {
        return writer;
      }

      public ObjectInputStream getReader()
      {
        return reader;
      }
      
      public void run()
      {
        try {
          runloop();
        }
        catch ( Exception e ) {
          OutputHandler.out(ExceptionTools.toString("Server:RT:run", e));
        }
        catch ( Error e ) {
          OutputHandler.out(ExceptionTools.toString("Server:RT:run", e));
        }	    
      }

      private void runloop()
      {
        try {
          reader = new ObjectInputStream(socket.getInputStream());
          writer = new ObjectOutputStream(socket.getOutputStream());
        }
        catch ( IOException e ) {
          OutputHandler.out(ExceptionTools.toString("Server:RT:runloop", e));
          return;
        }
        String fromHost = socket.getInetAddress().getHostName();
        OutputHandler.out("Server:RT:runloop Received connection from " + fromHost);
        QueryData query;
        while ( writer != null ) {
          try {
            query = (QueryData)reader.readObject();
          }
          catch ( Exception e ) {
            OutputHandler.out("Server:RT:runloop Connection from " + fromHost + " closed, reaping.");
            try {
              reader.close();
              writer.close();
              socket.close();
            }
            catch ( IOException f ) {
              OutputHandler.out(ExceptionTools.toString("Server:RT:runloop", f));
            }
            writer = null;
            continue;
          }
          query.setClosure(RequestThread.this);
          requests.enqueue(query);
          synchronized(requests) {
            requests.notify();
          }
          // Debug.out("Server:RT:runloop notified requests");
        }
      }
    }
  
  private class AnswerThread extends Thread
  {
    public void run()
    {
      try {
        runloop();
      }
      catch ( Exception e ) {
        OutputHandler.out(ExceptionTools.toString("Server:AT:run", e));
      }
      catch ( Error e ) {
        OutputHandler.out(ExceptionTools.toString("Server:AT:run", e));
      }	    
    }
    
    private void runloop()
    {
      Debug.out("Server:AT:runloop enter");
      while ( true ) {
        QueryData request;
        synchronized (requests) {
          request = (QueryData)requests.dequeue();
          if ( request == null ) {
            try {
              Debug.out("Server:AT:runloop entering wait");
              requests.wait();
            }
            catch ( InterruptedException e ) {
              OutputHandler.out("Server:AT:runloop wait interrupted? " + e);
              continue;
            }
            request = (QueryData)requests.dequeue();
          }
        }
        request.reconstituteSerialized();
        Debug.out("Server:AT:runloop requests awakened with: " + request.toString());
        RequestThread requestor = (RequestThread)(request.getClosure());
        ObjectOutputStream writer = requestor.getWriter();
        ServerPlanElementProvider source;
        if ( useCanned) {
          getcannedProducer(); // ensure it has initialized; we auto-start it
          source = getcannedProvider();
        }
        else
          source = getprovider();
        try {
	  //source.printMissingAccount();
          // special command
          if ( request.getOtherCommand() != null ) {
            if ( request.getOtherCommand().equals(RELOAD) ) {
              reload();
              writer.writeObject(null);
              writer.flush();
            }
            else if ( request.getOtherCommand().equals(SEND_UNIT_MANIFESTS) ) {
              Debug.out("Server:AT:runloop doing " + SEND_UNIT_MANIFESTS);
              synchronized(source.getUnitManifests()) {
                writer.writeObject(source.getUnitManifests());
              }
              writer.writeObject(null);
              writer.flush();
            }
            else if ( request.getOtherCommand().equals(SEND_HIERARCHY) ) {
              Debug.out("Server:AT:runloop doing " + SEND_HIERARCHY);
              synchronized(source.getStructureLock()) {
                for ( Iterator iter = source.getStructureIterator(); 
                      iter.hasNext(); ){
                  writer.writeObject(iter.next());
                }
              }
              writer.writeObject(null);
              writer.flush();
            }
            else if ( request.getOtherCommand().
		      equals(SEND_ASSETID_TD_MAPS)){
              Debug.out("Server.AnswerThread.runloop received command " + 
			SEND_ASSETID_TD_MAPS);
              synchronized(source.getAssetIdToUTDMapLock()) {
		Map m = source.getAssetIdToUTDMap();
		writer.writeObject(m);
	      }
              synchronized(source.getAssetIdToACTDMapLock()) {
		Map m = source.getAssetIdToACTDMap();
		writer.writeObject(m);
              }
              synchronized(source.getAssetIdToCUTDMapLock()) {
		Map m = source.getAssetIdToCUTDMap();
		writer.writeObject(m);
              }
              synchronized(source.getAssetIdToAFTDMapLock()) {
		Map m = source.getAssetIdToAFTDMap();
		writer.writeObject(m);
              }
              synchronized(source.getAssetIdToADTDMapLock()) {
		Map m = source.getAssetIdToADTDMap();
		writer.writeObject(m);
              }
              writer.writeObject(null);
              writer.flush();
            }
            else {
              OutputHandler.out("Server:AT:runloop Errning: unknown command: "
                                + request.getOtherCommand());
              writer.writeObject(null);
              writer.flush();
            }
            continue;
          }
          // normal lookup request
          Vector answers = source.lookup(request);
          if ( answers == null || (answers.size() == 0) ) {
            OutputHandler.out("Server:AT:runloop request resulted in empty response.");
            writer.writeObject(null);
            writer.flush();
            continue;
          }
          int total = 0;
          for ( Iterator iter = answers.iterator(); iter.hasNext(); ) {
            TaskNode node = ((TaskNode)iter.next());
            if ( !request.admits(node) )
              continue;
            writer.writeObject(node);
            total++;
            if ( total % 250 == 0 )
              OutputHandler.out(String.valueOf(total), false, false);
            else if ( total % 50 == 0 )
              OutputHandler.out(".", false, false);
          }
          writer.writeObject(null);
          writer.flush();
          
          OutputHandler.out("", false, true);
          Debug.out("Server:AT:runloop finished responding to query.");
        }
        catch ( Exception e ) {
          OutputHandler.out("Server:AT:runloop Error: " + e + ", continuing.");
        }
      }
    }
  }


  private JPanel getContainerPanel()
  {
    if ( containerPanel == null )
      try {
        containerPanel = new JPanel();
        containerPanel.setName("containerPanel");
        containerPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints constraintsButtonPanel = new GridBagConstraints();
        constraintsButtonPanel.gridx = 0; constraintsButtonPanel.gridy = 0;
        constraintsButtonPanel.fill = GridBagConstraints.HORIZONTAL;
        constraintsButtonPanel.weightx = 1.0;
        containerPanel.add(getbuttonPanel(), constraintsButtonPanel);
        
        GridBagConstraints constraintsMessageScroller = new GridBagConstraints();
        constraintsMessageScroller.gridx = 0; constraintsMessageScroller.gridy = 1;
        constraintsMessageScroller.fill = GridBagConstraints.BOTH;
        constraintsMessageScroller.weighty = 0.6;
        containerPanel.add(getMessageScroller(), constraintsMessageScroller);
        
        GridBagConstraints constraintsDebugScroller = new GridBagConstraints();
        constraintsDebugScroller.gridx = 0; constraintsDebugScroller.gridy = 2;
        constraintsDebugScroller.fill = GridBagConstraints.BOTH;
        constraintsDebugScroller.weighty = 0.4;
        containerPanel.add(getDebugScroller(), constraintsDebugScroller);
      }
    catch(Exception e) {
      handleException(e);
    }
    return containerPanel;
  }
  
  private OutputHandler getmessageHandler()
  {
    if ( messageHandler == null ) {
      try {
        messageHandler =
          new OutputHandler(new ProducerImpl("Output Handler"), true);
      }
      catch (Exception ivjExc) {
        handleException(ivjExc);
      }
    }
    return messageHandler;
  }
  
  private JScrollPane getMessageScroller()
  {
    if ( messageScroller == null )
      try {
        MessageArea messages = new MessageArea("Messages\n", 10, 80);
        messages.setEditable(false);
        messageScroller = new JScrollPane(messages);
        messages.setScrollBar(messageScroller.getVerticalScrollBar());
        messageScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        getmessageHandler().addConsumer(messages);
      }
    catch ( Exception e ) {
      handleException(e);
    }
    return messageScroller;
  }
  
  private JScrollPane getDebugScroller()
  {
    if ( debugScroller == null )
      try {
        MessageArea debugMessages = new MessageArea("Debugging Messages\n", 10, 80);
        debugMessages.setEditable(false);
        debugScroller = new JScrollPane(debugMessages);
        debugMessages.setScrollBar(debugScroller.getVerticalScrollBar());
        debugScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        Debug.getHandler().addConsumer(debugMessages);
      }
    catch ( Exception e ) {
      handleException(e);
    }
    return debugScroller;
  }
  
  private ServerPlanElementProvider getprovider()
  {
    if ( provider == null )
      try {
        provider = new ServerPlanElementProvider(clusterCache, false);
        // provider.provideUnitTaskNodes();
      }
    catch ( RuntimeException e ) {
      handleException(e);
    }
    return provider;
  }
  
  private ServerPlanElementProvider getcannedProvider()
  {
    if ( cannedProvider == null )
      try {
        cannedProvider = new ServerPlanElementProvider(clusterCache, true);
        // cannedProvider.provideUnitTaskNodes();
      }
    catch ( RuntimeException e ) {
      handleException(e);
    }
    return cannedProvider;
  }
  
  private CannedProducer getcannedProducer()
  {
    if ( cannedProducer == null )
      try {
        String file = (String)JOptionPane.showInputDialog(null, 
                "Enter filename to load", "File name",
                JOptionPane.INFORMATION_MESSAGE, null, null,
                CWD);
        if ( file == null ) {
          OutputHandler.out("Server:gcP Warning: empty file name.");
          return null;
        }
        cannedProducer = new CannedProducer(file, 
                                            getcannedProvider(), 
                                            clusterCache.getallowOrgNames());
        if ( cannedProducer.isBad() ) {
          OutputHandler.out("Server:gcP Errning: bad file name: " + file);
          cannedProducer = null;
          return null;
        }
        cannedProducer.addConsumer(getcannedProvider());
        cannedProducer.start();
        cannedProducer.request("BLAH");
        cannedText.setText(file);
        cannedDataButton.doClick();
	// disable the reloadData button
	//reloadButton.setEnabled(false);
      }
    catch ( Exception e ) {
      handleException(e);
    }
    return cannedProducer;
  }
    
  private void handleException(Exception e)
  {
    OutputHandler.out(ExceptionTools.toString("Server:hE", e));
    e.printStackTrace(System.err);
  }
  
  
  private String gethost()
  {
    if ( host == null )
      try {
        if ( clusterCache.getHost() == null )
          clusterCache.guiSetHost();
        host = clusterCache.getHost();
        liveText.setText(host);
      }
    catch ( Exception e ) {
      handleException(e);
    }
    return host;
  }
  
  private Vector getitineraryProducers()
  {
    if ( itineraryProducers == null )
      try {
        if ( gethost() == null ) {
          OutputHandler.out("Server:gIP Note: no host set, not starting producers.");
	  //	  System.out.println("getitineraryProducers received null host");
          return null;
        }
        itineraryProducers = new Vector();
        for ( Iterator i = clusterCache.getclusterNames().iterator(); i.hasNext(); ) {
          String clusterName = (String)i.next();
          // Debug.out("Server:sPT about to gIP " + clusterName);
          ItineraryProducer producer = (ItineraryProducer)clusterCache.getItineraryProducer(clusterName, false);
          // Debug.out("Server:gIP done gIP " + clusterName);
          if ( producer == null )
            OutputHandler.out("Server:gIP Errning: bad cluster name " + clusterName);
          else {
            itineraryProducers.add(producer);
            // Debug.out("Server:sPT about to aC");
            producer.addConsumer(getprovider());
            // Debug.out("Server:sPT about to bPI");
            producer.beginPollingItineraries(interval);
            // Debug.out("Server:sPT done bPI");
          }
        }
      }
    catch ( Exception e ) {
      handleException(e);
    }
    return itineraryProducers;
  }
  
  private void init()
  {
    OutputHandler.out("Commencing ALPINE Aggregation Server ");
	host = clusterCache.guiSetHost();
	demandHost = clusterCache.guiSetDemandHost();
    getitineraryProducers();
    requests = new Fifo();
    
    AnswerThread answerer = new AnswerThread();
    try {
      answerer.start();
    }
    catch ( Exception e ) {
      OutputHandler.out(ExceptionTools.toString("Server:init", e));
    }
    catch ( Error e ) {
      OutputHandler.out(ExceptionTools.toString("Server:init", e));
    }
    
    OutputHandler.out("Server:init Done, exiting main thread. Ready for service.");
  }
  
  private void saveState(boolean useNodes)
  {
    String fileName = (String)JOptionPane.showInputDialog(null, "Enter filename to save", "File name",
                                                          JOptionPane.INFORMATION_MESSAGE, null, null,
                                                          defaultOosDir);
    if ( fileName == null )
      return;
    FileOutputStream fos = null;
    ObjectOutputStream writer = null;
    try {
      fos = new FileOutputStream(fileName);
      writer = new ObjectOutputStream(fos);
    }
    catch ( Exception e ) {
      OutputHandler.out(ExceptionTools.toString("Server:aP", e));
    }
    Debug.out("Server:aP Opened file: " + fos);
    int i = 0;
    try {
      Iterator iter = null;
      Object lock = null;
      if ( useNodes ) {
        lock = getprovider().getTaskNodeLock();
        iter = getprovider().getTaskNodeIterator();
      }
      else {
        lock = getprovider().getItineraryLock();
        iter = getprovider().getItineraryIterator();
      }
      synchronized(lock) {
        for ( ; iter.hasNext(); i++ )
          writer.writeObject(iter.next());
      }
      writer.writeObject(null);
      writer.flush();
      fos.close();
    }
    catch ( Exception e ) {
      OutputHandler.out(ExceptionTools.toString("Server:aP", e));
    }
    OutputHandler.out("Saved " + i + (useNodes ? " pre-processed" : " raw") + " objects to " + fileName + ".");
  }
  
  public void actionPerformed(ActionEvent event)
  {
    String command = event.getActionCommand();
    Object source = event.getSource();
    Debug.out("Server:aP command " + command);
    //    Debug.out("Server:aP source " + source);
    if ( source instanceof JMenuItem ) {
      JMenuItem item = (JMenuItem)source;
      // Action Menu
      if ( item == getsaveNodeItem() || item == getsaveItinItem() )
        saveState(item == getsaveNodeItem());
//        else if ( item == getloadItem() )
//  	  getcannedProducer();
      //      else if ( item == getpretendItem() )
      //  useCannedOnly = getpretendItem().isSelected();
      else if ( item == getquitItem() )
        System.exit(0);
      else
        OutputHandler.out("Server:aP Errning: unknown menu source: " + source);
    }
    else if ( source instanceof JButton ) {
      JButton button = (JButton)source;
      if ( button == reloadButton )
        reload();
      else if ( button == loadliveButton ){
	  loadNewLive();
      }
      else if ( button == loadcannedButton )
	  getcannedProducer();
      else
        OutputHandler.out("Server:aP Errning: unknown button source: " 
			  + source);
    }
    else if ( source instanceof JRadioButton ) {
	JRadioButton rButton = (JRadioButton)source;
	if (rButton.getName() == "Canned Data"){
	    useCanned = true;
	}
	else if (rButton.getName() == "Live Data"){
	    useCanned = false;
	}
	else
	    OutputHandler.out("Server:aP Errning: unknown button source: " 
			      + source);
    }
    else 
	OutputHandler.out("Server:aP Errning: unknown bean source: " + source);
  }
  
  public void handleConnections()
  {
    try {
      listener = new ServerSocket(CLIENT_PORT);
      while ( true ) {
        Debug.out("Server:handleConn Ready for a client, waiting.");
        socket = listener.accept();
        RequestThread connection = new RequestThread(socket);
        connection.start();
      }
    }
    catch ( Exception e ) {
      if (e instanceof BindException) {
        OutputHandler.out( ExceptionTools.toString("Server:handleConn -- Socket already in use -- Aggregation Server " +
                        "already running or socket hasn't cleared yet",e));
      }
      else 
        OutputHandler.out(ExceptionTools.toString("Server:handleConn", e));
    }
    catch ( Error e ) {
      OutputHandler.out(ExceptionTools.toString("Server:handleConn", e));
    }
                       
  }
  
  public static void main(String args[])
  {
    int interval = 0;
    boolean bad = false;
    String clusterList = null;
    String allowList = null;
    String defaultOosDir = CWD;

    Debug.setHandler(new Debug(new ProducerImpl("Aggregation Server"), true));
    Debug.set(true);
    for ( int i = 0; i < args.length; i++ ) {
      if ( args[i].equalsIgnoreCase("-interval") ) {
        if ( i == args.length - 1 ) {
          OutputHandler.out("Server:main Errning: -interval requires argument.");
          bad = true;
          break;
        }
        try {
          interval = Integer.parseInt(args[i + 1]);
          i++;
          if ( interval < 1 )
            throw new NumberFormatException("Negatives disallowed");
        }
        catch ( NumberFormatException e ) {
          OutputHandler.out("Server:main Errning: invalid positive integer: " + interval);
          bad = true;
          break;
        }
      }
      else if ( args[i].equalsIgnoreCase("-clusterList") ) {
        if ( i == args.length - 1 ) {
          OutputHandler.out("Server:main Errning: -clusterList requires argument.");
          bad = true;
          break;
        }
        clusterList = args[i + 1];
        i++;
      }
      else if ( args[i].equalsIgnoreCase("-disableFilter") ) {
        if ( i == args.length - 1 ) {
          OutputHandler.out("Server:main Errning -disableFilter requires argument.");
          bad = true;
          break;
        }
        allowList = args[i + 1];
        i++;
      }
      else if ( args[i].equalsIgnoreCase("-defaultOosDir") ) {
        if ( i == args.length - 1 ) {
          OutputHandler.out("Server:main Errning -defaultOosDir requires argument.");
          bad = true;
          break;
        }
        defaultOosDir = args[i + 1];
        i++;
      }
      else if ( args[i].equalsIgnoreCase("-help") ) {
        System.out.println("Usage:\t[-interval <positive integer in seconds>]\n"
                           + "\t\t[-clusterList <comma separated list of cluster names>]\n"
                           + "\t\t[-disableFilter <comma separated list of cluster names>]\n"
                           + "\t\t[-defaultOosDir <directory to fill in when asking for .oos files>]\n");
        System.out.println("Example: java org.cougaar.domain.mlm.ui.tpfdd.aggregation.Server -interval 1000"
                           + " -clusterList GlobalAir,GlobalSea,FortStewartITO");
        System.exit(0);
      }
      else {
        OutputHandler.out("Server:main Errning: unknown argument: " + args[i]);
        bad = true;
      }
    }
    if ( bad ) {
      OutputHandler.out("Bailing due to argument problem. Fix and rerun.");
      System.exit(1);
    }
    
    try {
      Server server = 
		(useSubordinatesPSP) ?
		new Server(interval, new ClusterCache(false, allowList), defaultOosDir) :
		new Server(interval, new ClusterCache(false, clusterList, allowList), defaultOosDir);
    }
    catch ( RuntimeException e ) {
      OutputHandler.out(ExceptionTools.toString("Server:Server", e));
    }
    catch ( Error e ) {
      OutputHandler.out(ExceptionTools.toString("Server:Server", e));
    }	    
  }
}


