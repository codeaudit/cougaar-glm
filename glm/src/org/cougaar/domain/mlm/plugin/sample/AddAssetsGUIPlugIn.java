/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

/**
 * Add[/Remove] Assets GUI PlugIn.
 * <p>
 * Parameters:<br>
 * <ul>
 *   <li>"*.xml": name of XML file containing prototype names.</li>
 *   <li>"true": set allowRemoveAssets to true</li>
 *   <li>"false": set allowRemoveAssets to false</li>
 * </ul>
 * <p>
 * If boolean "allowRemoveAssets" is false, then the random remove 
 * assets feature is turned off.  Randomly removing assets is
 * likely only useful for testing!
 * <p>
 * The initial prototype ID list is read from an XML file, either
 * the DEFAULT_XML_FILE_STRING or the plugin parameter.  The XML 
 * input expected DTD is:<br>
 * <pre>
 *   &lt;!ELEMENT prototypes (name)*&gt;
 *   &lt;!ELEMENT name (#PCDATA)&gt;
 * </pre>
 * and, as an example:<br>
 * <pre>
 *   &lt;?xml version="1.0"  encoding="US-ASCII"?&gt;
 *   &lt;!DOCTYPE prototypes SYSTEM "AddPrototypes.dtd" []&gt;
 *   &lt;prototypes&gt;
 *     &lt;name&gt;NSN/2350010871095&lt;/name&gt;
 *     &lt;name&gt;mos-88m&lt;/name&gt;
 *   &lt;/prototypes&gt;
 * </pre>
 */

package org.cougaar.domain.mlm.plugin.sample;

/*
 * Imports
 */
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import java.text.SimpleDateFormat;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.cougaar.core.cluster.ClusterIdentifier;
import org.cougaar.util.StateModelException;
import org.cougaar.core.cluster.IncrementalSubscription;
import org.cougaar.core.cluster.ClusterServesPlugIn;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.TypeIdentificationPG;
import org.cougaar.domain.planning.ldm.plan.Schedule;

import org.cougaar.core.plugin.SimplePlugIn;

import org.cougaar.util.ShortDateFormat;

// This should only be temporary...
// We are breaking the COUGAAR Code!
import org.cougaar.domain.planning.ldm.plan.RoleScheduleImpl;

import org.cougaar.util.UnaryPredicate;

import org.w3c.dom.*;

import com.ibm.xml.parsers.*;

public final class AddAssetsGUIPlugIn extends SimplePlugIn {

  /** CONFIGURATION **/
  private static final boolean DEFAULT_ALLOW_REMOVE_ASSETS = false;
  private static final String DEFAULT_XML_FILE_STRING = "AddPrototypes.xml";
  private static final int DEFAULT_END_DATE_MONTH_DELTA = 3;

  /** Allow asset removal **/
  private boolean allowRemoveAssets;

  /** Create Aggregates on request **/
  private boolean createAggregates = false;

  /** GUI Components **/
  private JComboBox prototypeNameCombo;
  private JButton addButton;
  private JTextField addQuantityText;
  private JTextField addBeginText;
  private JTextField addEndText;
  private JCheckBox aggregateCheckBox;
  private JButton removeButton;
  private JLabel statusLabel;

  /** Vector of known prototype names **/
  private Vector prototypeNames;

  private ShortDateFormat dateFormatter;

  private RootFactory ldmf_ = null;

  private IncrementalSubscription removableAssetsSub;

  /**
   * The public constructor with no args to set the default values.
   **/
  public AddAssetsGUIPlugIn() {}
  
  protected void execute() { }

  /**
   * Load the data from the file synchronously.
   * We'll not actually do anything later on.
   **/
  protected void setupSubscriptions() {
    // default configuration;
    String XMLFileString = DEFAULT_XML_FILE_STRING;
    allowRemoveAssets = DEFAULT_ALLOW_REMOVE_ASSETS;

    // read parameters:
    //   XMLFileString: name of file containing prototype names
    //   allowRemoveAssets: allow remove assets
    Enumeration eParams = getParameters().elements();
    while (eParams.hasMoreElements()) {
      String sParam = (String)eParams.nextElement();
      if (sParam.endsWith(".xml")) {
        XMLFileString = sParam;
      } else if (sParam.equals("true")) {
	allowRemoveAssets = true;
      } else if (sParam.equals("false")) {
	allowRemoveAssets = false;
      }
    }

    createGUI(XMLFileString);
    ldmf_ = getFactory();

    if (allowRemoveAssets) {
      removableAssetsSub = 
        (IncrementalSubscription) subscribe(newRemovableAssetsPred());
    }
  }

  protected void setStatus(String s) {
    setStatus(false, s);
  }

  protected void setStatus(boolean success, String s) {
    statusLabel.setForeground(
        (success ? Color.darkGray : Color.red));
    statusLabel.setText(s);
  }

  protected void watchAddButton() {
    String prototypeName = (String)prototypeNameCombo.getSelectedItem();
    if (prototypeName == null) {
      setStatus("No asset type selected");
      return;
    }
    prototypeName = prototypeName.trim();
    if (prototypeName.length() < 1) {
      setStatus("No asset type selected");
      return;
    }

    int quantity;
    Date beginDate;
    Date endDate;

    try {
      quantity = Integer.parseInt(addQuantityText.getText().trim());
    } catch (Exception quantE) {
      quantity = -1;
    }
    if (quantity < 0) {
      setStatus("Invalid quantity");
    } else if (quantity == 0) {
      setStatus(true, "Added Nothing");
    } else if ((beginDate =
                dateFormatter.toDate(addBeginText.getText().trim()))
               == null) {
      setStatus("Invalid begin date");
    } else if ((endDate =
                dateFormatter.toDate(addEndText.getText().trim()))
               == null) {
      setStatus("Invalid end date");
    } else {
      openTransaction();
      try {
        addAsset(prototypeName, quantity, beginDate, endDate);
        setStatus(true, "Added " + 
          ((quantity > 1) ? 
           (quantity+" Assets") : 
           "One Asset"));
        if (!prototypeNames.contains(prototypeName)) {
          prototypeNames.add(prototypeName);
          prototypeNameCombo.addItem(prototypeName);
        }
      } catch (Exception exc) {
        if (prototypeNames.contains(prototypeName)) {
          prototypeNames.remove(prototypeName);
          prototypeNameCombo.removeItem(prototypeName);
        }
        System.err.println("Could not add asset: "+exc);
        setStatus("Unable to add Asset Type");
      }
      closeTransaction(false);
    }
  }

  protected void watchRemoveButton() {
    if (!allowRemoveAssets) {
      return;
    }
    String prototypeName = (String)prototypeNameCombo.getSelectedItem();
    if (prototypeName != null) {
      prototypeName = prototypeName.trim();
      if ((prototypeName.length() < 1) ||
          prototypeName.equals("*"))
        prototypeName = null;
    }
    openTransaction();
    try {
      int removeCount = removeAsset(prototypeName);
      if (removeCount > 1)
        setStatus(true, "Removed "+ removeCount+ " random Assets");
      else if (removeCount > 0)
        setStatus(true, "Removed a random Asset");
      else {
        setStatus(true, "Zero \""+prototypeName+"\" assets (try typeID!)");
        prototypeNames.remove(prototypeName);
        prototypeNameCombo.removeItem(prototypeName);
      }
      if ((prototypeName != null) && 
          (!prototypeNames.contains(prototypeName))) {
        prototypeNames.add(prototypeName);
        prototypeNameCombo.addItem(prototypeName);
      }
    }
    catch (Exception exc) {
      if ((prototypeName != null) &&
          prototypeNames.contains(prototypeName)) {
        prototypeNames.remove(prototypeName);
        prototypeNameCombo.removeItem(prototypeName);
      }
      System.err.println("Could not remove asset of name: "+prototypeName);
      setStatus("Unable to remove \""+prototypeName+"\n Asset");
    }
    closeTransaction(false);
  }

  /** An ActionListener that listens to the buttons. */
  class AddButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JButton button = (JButton)e.getSource();
      if (button == addButton)
        watchAddButton();
      else
        watchRemoveButton();
    }
  };

  /** An ItemListener that listens to the Aggregates CheckBox */
  class AggregateListener implements ItemListener {
      public void itemStateChanged(ItemEvent e) {
	  createAggregates = (e.getStateChange() == ItemEvent.SELECTED);
      }
  };

  private String getClusterID() {
    try {
      return getCluster().getClusterIdentifier().toString();
    } catch (Exception e) {
      return "<UNKNOWN>";
    }
  }

  private void createGUI(String XMLFileString) {
    // read initial asset prototype names
    if (XMLFileString != null)
      prototypeNames = readInitialPrototypeNames(XMLFileString);
    if (prototypeNames == null)
      prototypeNames = new Vector();
    if (prototypeNames.size() == 0)
      prototypeNames.add("              ");

    // create buttons, labels, etc
    prototypeNameCombo = new JComboBox(prototypeNames);
    prototypeNameCombo.setEditable(true);
    addButton = new JButton("Add Asset");
    addButton.addActionListener(new AddButtonListener());
    addQuantityText = new JTextField(3);
    addBeginText = new JTextField(10);
    addEndText = new JTextField(10);
    aggregateCheckBox = new JCheckBox("Create AggregateAsset", false);
    aggregateCheckBox.addItemListener(new AggregateListener());
    addQuantityText.setText("1");
    dateFormatter = new ShortDateFormat();
    addBeginText.setText(dateFormatter.toString(null));
    addEndText.setText(
        dateFormatter.toString(
           dateFormatter.adjustDate(null, DEFAULT_END_DATE_MONTH_DELTA, 0)));
    if (allowRemoveAssets) {
      removeButton = new JButton("Remove Random Asset");
      removeButton.addActionListener(new AddButtonListener());
    }
    statusLabel = new JLabel("<                          >");

    // do layout
    JFrame frame = new JFrame("AddAssetsGUIPlugIn  "+getClusterID());
    frame.setLocation(0,0);
    frame.getContentPane().setLayout(new FlowLayout());
    JPanel rootPanel = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(15,15,15,15);
    gbc.fill = GridBagConstraints.BOTH;
    rootPanel.setLayout(gbl);

    JPanel protoNamePanel = new JPanel();
    protoNamePanel.setLayout(new GridLayout(2,1));
    JLabel protoNameLabel = new JLabel("Prototype Name:");
    protoNameLabel.setForeground(Color.blue);
    protoNamePanel.add(protoNameLabel);
    protoNamePanel.add(prototypeNameCombo);
    gbl.setConstraints(protoNamePanel, gbc);
    rootPanel.add(protoNamePanel);

    JPanel addPanel = new JPanel();
    addPanel.setLayout(new BorderLayout());
    JLabel addLabel = new JLabel("Add Assets:");
    addLabel.setForeground(Color.blue);
    addPanel.add(addLabel, BorderLayout.NORTH);
    JPanel addOptsPanel = new JPanel();
    addOptsPanel.setLayout(new BorderLayout());
    JPanel addOptsTextPanel = new JPanel();
    addOptsTextPanel.setLayout(new GridLayout(4,1));
    addOptsTextPanel.add(new JLabel("Quantity:"));
    addOptsTextPanel.add(new JLabel("Begin Date:"));
    addOptsTextPanel.add(new JLabel("End Date:"));
    addOptsTextPanel.add(new JLabel(""));
    JPanel addOptsValuePanel = new JPanel();
    addOptsValuePanel.setLayout(new GridLayout(4,1));
    addOptsValuePanel.add(addQuantityText);
    addOptsValuePanel.add(addBeginText);
    addOptsValuePanel.add(addEndText);
    addOptsValuePanel.add(aggregateCheckBox);
    addOptsPanel.add(addOptsTextPanel, BorderLayout.CENTER);
    addOptsPanel.add(addOptsValuePanel, BorderLayout.EAST);
    addPanel.add(addOptsPanel, BorderLayout.CENTER);
    addPanel.add(addButton, BorderLayout.SOUTH);
    gbc.gridy = 1;
    gbl.setConstraints(addPanel, gbc);
    rootPanel.add(addPanel);

    if (allowRemoveAssets) {
      JPanel removePanel = new JPanel();
      removePanel.setLayout(new GridLayout(2,1));
      JLabel removeLabel = new JLabel("Remove Random Asset:");
      removeLabel.setForeground(Color.blue);
      removePanel.add(removeLabel);
      removePanel.add(removeButton);
      gbc.gridy = 2;
      gbl.setConstraints(removePanel, gbc);
      rootPanel.add(removePanel);
    }

    JPanel statusPanel = new JPanel();
    statusPanel.setLayout(new GridLayout(2,1));
    JLabel statusLabelLabel = new JLabel("Status:");
    statusLabelLabel.setForeground(Color.blue);
    statusPanel.add(statusLabelLabel);
    statusPanel.add(statusLabel);
    gbc.gridy = 3 + (allowRemoveAssets ? 1 : 0);
    gbl.setConstraints(statusPanel, gbc);
    rootPanel.add(statusPanel);

    frame.getContentPane().add(rootPanel);
    frame.pack();
    frame.setVisible(true);

    setStatus(true, "Ready");
  }

  private Vector readInitialPrototypeNames(String xmlFileString) {
    DOMParser parser = new DOMParser();
    try {
      parser.parse( xmlFileString );
    } catch ( java.io.IOException ioe ) {
      ioe.printStackTrace();
      return null;
    } catch ( org.xml.sax.SAXException sae ) {
      sae.printStackTrace();
      return null;
    }

    Document document = parser.getDocument();
    Element root = document.getDocumentElement();

    if (!"prototypes".equals(root.getNodeName())) {
      System.err.println("Bad XML root: "+
	  root.getNodeName()+" != prototypes");
      return null;
    }

    Vector assetsV = new Vector();

    NodeList nlist = root.getChildNodes();
    int nlength = nlist.getLength();
    for (int i = 0; i < nlength; i++) {
      Node subNode = (Node)nlist.item(i);
      if ((subNode.getNodeType() == Node.ELEMENT_NODE) && 
          ("name".equals(subNode.getNodeName())))  {
        String value = subNode.getFirstChild().getNodeValue().trim();
        assetsV.add(value);
      } 
    }

    return assetsV;
  }
    
  private static int counter = 0;

  /**
   * add one or an aggregate of the given type_id
   * @param type_id asset prototype id name
   * @param quantity how many to add
   * @param beginDate schedule availability start date
   * @param endDate schedule availability stop date
   * @param createAggregates : Should we create aggregate asset, or individuals?
   */
  private void addAsset(
      String type_id, int quantity, Date beginDate, Date endDate) {
    //System.out.println("ADD "+type_id+
    //  " quantity: "+quantity+" begin: "+beginDate+" end: "+endDate);
    Asset newAsset;

    for (int i = 0; i < quantity; i++) {

	if (createAggregates) {
	    AggregateAsset multiAsset = 
		(AggregateAsset)ldmf_.createAggregate(type_id, quantity);
	    newAsset = multiAsset;
	} else {
	    Asset singleAsset =
		(Asset)ldmf_.createInstance(
					    type_id, ("obj-"+(++counter)));
	    newAsset = singleAsset;
	}

	RoleScheduleImpl newRoleS = (RoleScheduleImpl)(newAsset.getRoleSchedule());
	Schedule newSched = ldmf_.newSimpleSchedule(beginDate, endDate);                
	newRoleS.setAvailableSchedule(newSched);
	publishAdd(newAsset);

	// Just create a single aggregate asset if CreateAggregates = true
	if (createAggregates)
	    break;
    }
  }

  /**
   * Remove some random asset of the given type.
   *
   * Note: This doesn't seem to do the right thing when the
   * asset is in an Aggregate.  Do we want to remove the entire
   * aggregate or modify it's number of elements?
   *
   * @param type_id asset prototype id name
   * @return number of assets removed
   */
  private int removeAsset(String type_id) {
    // count assets of given type_id
    int nAssets;
    Enumeration eAssets;
    if (type_id == null) {
      nAssets = removableAssetsSub.size();
      eAssets = removableAssetsSub.elements();
    } else {
      Vector v = new Vector();
      Enumeration e = removableAssetsSub.elements();
      while (e.hasMoreElements()) {
        Asset a = (Asset)e.nextElement();
        while (a instanceof AggregateAsset)
          a = ((AggregateAsset)a).getAsset();
        Asset pt = a.getPrototype();
        if (pt != null) {
          TypeIdentificationPG tpg = pt.getTypeIdentificationPG();
          if (tpg != null) {
            if (type_id.equals(tpg.getTypeIdentification())) {
              v.addElement(a);
            }
          }
        }
      }
      nAssets = v.size();
      eAssets = v.elements();
    }
    if (nAssets > 0) {
      // pick one to remove
      int j = (int)(nAssets * Math.random());
      if (j >= nAssets) j = nAssets-1;
      try {
        // skip to it
        for (int k = j; k > 0; k--)
          eAssets.nextElement();
        // remove it
        Asset removeA = (Asset)eAssets.nextElement();
        System.out.println("Remove asset #"+j+"  "+removeA);
        publishRemove(removeA);
      } catch (Exception ex) {
        System.out.println("Failed to remove asset #"+j);
      }
      // if it's an aggregate, should we return it's quantity?
      return 1;
    } else {
      System.out.println("No assets of type: "+type_id);
      return 0;
    }
  }

  private static UnaryPredicate newRemovableAssetsPred() {
    return new UnaryPredicate() {
      public boolean execute(Object o) {
        while (o instanceof AggregateAsset)
          o = ((AggregateAsset)o).getAsset();
        return (o instanceof Asset);
      }
    };
  }
}
