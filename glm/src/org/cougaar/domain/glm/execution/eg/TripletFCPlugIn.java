package org.cougaar.domain.glm.execution.eg;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import org.cougaar.domain.glm.execution.common.FailureConsumptionRate;
import org.cougaar.util.OptionPane;
import org.cougaar.util.Random;

/**
 * Example F/C plugin illustrating how one might write a plugin that
 * applies to a certain class of supply and how to use the GUI
 * configuration features. Consult the default plugin for more mundane
 * matters.
 **/
public abstract class TripletFCPlugIn implements FailureConsumptionPlugIn, TimeConstants {
  protected long MAX_INTERVAL = ONE_DAY;
  protected long MIN_INTERVAL = 6 * ONE_HOUR;
  protected final String ALL_CLUSTERS = "All Clusters";
  protected final String ALL_CONSUMERS = "All Consumers";
  protected final String ALL_CONSUMABLES = "All Consumables";

  protected static final Random random = new Random();
  protected EventGenerator theEventGenerator = null;
  protected String currentConsumer = null;
  protected String currentConsumable = null;
  protected String currentCluster = null;
  private TripletKey currentTripletKey = null;

  protected static Map consumerNameToId = new TreeMap();
  protected static Map consumerIdToName = new HashMap();
  protected static Map consumableNameToId = new TreeMap();
  protected static Map consumableIdToName = new HashMap();
  protected static SortedMap triplets = new TreeMap();

  protected static final int BUTTON_ROW = 100;
  protected static final int COMMENT_ROW = 90;

  private JComboBox cb1 = new JComboBox();
  private JComboBox cb2 = new JComboBox();
  private JComboBox cb3 = new JComboBox();

  private JTextField[] valueTextField;
  private JLabel[] valueLabel;
  private JButton nextButton = new JButton(">>");
  private JButton prevButton = new JButton("<<");
  private JButton deleteButton = new JButton("Delete");
  private JButton saveButton = new JButton("Save");
  private JPanel buttons = new JPanel();
  protected JPanel message;

  private boolean enableListener = false;
  private ActionListener cbListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (!enableListener) return;
      Object src = e == null ? null : e.getSource();
      if (e == null || src == cb1) currentConsumable = (String) cb1.getSelectedItem();
      if (e == null || src == cb2) currentConsumer   = (String) cb2.getSelectedItem();
      if (e == null || src == cb3) currentCluster    = (String) cb3.getSelectedItem();
      if ((currentConsumable != null) &&
          (currentConsumer   != null) &&
          (currentCluster    != null)) {
            TripletKey tk_temp = new TripletKey((String) consumableNameToId.get(currentConsumable),
                                                (String) consumerNameToId.get(currentConsumer),
                                                (String) currentCluster);
            setTripletKey(tk_temp);
          }
    }
  };

  private void setTripletKey(TripletKey key) {
    TripletValue tv;
    if (key == null) {
      tv = null;
    } else {
      tv = (TripletValue) triplets.get(key);
      cb1.setSelectedItem(consumableIdToName.get(key.getConsumable()));
      cb2.setSelectedItem(consumerIdToName.get(key.getConsumer()));
      cb3.setSelectedItem(key.getCluster());
    }
    if (tv != null || key == null) {
      currentTripletKey = key;
      setTripletValue(tv);
    } else {
      currentTripletKey = null;         // Keep the values, but no key
    }
    updateButtons();
  }

  private void updateButtons() {
    if (currentTripletKey == null) {
      if (triplets.isEmpty()) {
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);
      } else {
        prevButton.setEnabled(true);
        nextButton.setEnabled(true);
      }        
      deleteButton.setEnabled(false);
    } else {
      nextButton.setEnabled(getNextTripletKey(currentTripletKey) != null);
      prevButton.setEnabled(getPreviousTripletKey(currentTripletKey) != null);
      deleteButton.setEnabled(true);
    }
    if (computeTripletKey() != null && computeTripletValue() != null) {
      saveButton.setEnabled(true);
    } else {
      if (saveButton.isSelected()) saveButton.setSelected(false);
      saveButton.setEnabled(false);
    }
  }

  private ActionListener nextListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      TripletKey newKey = getNextTripletKey(currentTripletKey);
      setTripletKey(newKey);
    }
  };

  private ActionListener prevListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      TripletKey newKey = getPreviousTripletKey(currentTripletKey);
      setTripletKey(newKey);
    }
  };

  private ActionListener deleteListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (currentTripletKey != null) {
        TripletKey nextKey = getNextTripletKey(currentTripletKey);
        triplets.remove(currentTripletKey);
        setTripletKey(nextKey);
      }
    }
  };

  private ActionListener saveListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (!saveTriplet()) {
//          SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//              JOptionPane.showMessageDialog(message,
//                                            "Invalid parameters entered",
//                                            "Invalid Parameters",
//                                            JOptionPane.ERROR_MESSAGE);
//            }
//          });
      }
    }
  };

  private TripletKey getNextTripletKey(TripletKey key) {
    TripletKey result = null;
    if (key != null) {
      Iterator tail = triplets.tailMap(key).keySet().iterator();
      tail.next();                      // Skip past the current key
      if (tail.hasNext()) {
        result = (TripletKey) tail.next();
      }
    } else if (!triplets.isEmpty()) {
      result = (TripletKey) triplets.firstKey();
    }
    return result;
  }

  private TripletKey getPreviousTripletKey(TripletKey key) {
    TripletKey result = null;
    if (key != null) {
      SortedMap head = triplets.headMap(key);
      if (!head.isEmpty()) {
        result = (TripletKey) head.lastKey();
      }
    } else if (!triplets.isEmpty()) {
      result = (TripletKey) triplets.lastKey();
    }
    return result;
  }

  private class TripletKey implements Comparable {
    private String consumable;
    private String consumer;
    private String cluster;
    private int hashCode;

    public TripletKey(String ft, String co, String cl) {
      if (ft == null) throw new IllegalArgumentException("ft is null");
      if (co == null) throw new IllegalArgumentException("co is null");
      if (cl == null) throw new IllegalArgumentException("cl is null");
      consumable = ft;
      consumer = co;
      cluster = cl;
      setHashCode();
    }

    private void setHashCode() {
      hashCode = consumable.hashCode() + consumer.hashCode() + cluster.hashCode();
    }

    public String getConsumable() {
      return consumable;
    }

    public String getConsumer() {
      return consumer;
    }

    public String getCluster() {
      return cluster;
    }

    public boolean equals(Object o) {
      TripletKey tk = (TripletKey) o;
      if (tk.getConsumable().equals(consumable) &&
          tk.getConsumer().equals(consumer) &&
          tk.getCluster().equals(cluster)) {
        return true;
      } else {
        return false;
      }
    }

    public int compareTo(Object o) {
      TripletKey tk = (TripletKey) o;
      int diff;
      if ((diff = getConsumable().compareTo(tk.getConsumable())) != 0) return diff;
      if ((diff = getConsumer().compareTo(tk.getConsumer())) != 0) return diff;
      if ((diff = getCluster().compareTo(tk.getCluster())) != 0) return diff;
      return 0;
    }

    public int hashCode() {
      return hashCode;
    }

    public TripletKey(String key) {
      StringTokenizer tokens = new StringTokenizer(key, ",");
      consumable = tokens.nextToken();
      consumer = tokens.nextToken();
      cluster = tokens.nextToken();
      if (tokens.hasMoreTokens()) throw new IllegalArgumentException("Too many tokens");
      setHashCode();
    }

    public String toString() {
      return getConsumable() + "," + getConsumer() + "," + getCluster();
    }
  }

  protected static interface TripletValue {
    long getStartDate(); 		// For basic computation
    long getEndDate();                  // For basic computation
    AnnotatedDouble getMultiplier();    // For basic computation
    int getFieldCount();
    Object getFieldValue(int ix);       // For GUI
  }

  protected class Item extends FailureConsumptionPlugInItem {
    long previousTime = 0L;
    TripletKey[] tks;

    public Item(FailureConsumptionRate aRate, long theExecutionTime, FailureConsumptionSegment aSegment) {
      super(aRate);
      tks = new TripletKey[] {
        new TripletKey(aRate.theItemIdentification, aRate.theConsumerId, aSegment.theSource),
        new TripletKey(aRate.theItemIdentification, aRate.theConsumerId, ALL_CLUSTERS),
        new TripletKey(aRate.theItemIdentification, ALL_CONSUMERS, aSegment.theSource),
        new TripletKey(ALL_CONSUMABLES, aRate.theConsumerId, aSegment.theSource),
        new TripletKey(aRate.theItemIdentification, ALL_CONSUMERS, ALL_CLUSTERS),
        new TripletKey(ALL_CONSUMABLES, aRate.theConsumerId, ALL_CLUSTERS),
        new TripletKey(ALL_CONSUMABLES, ALL_CONSUMERS, aSegment.theSource),
        new TripletKey(ALL_CONSUMABLES, ALL_CONSUMERS, ALL_CLUSTERS)
      };
      long quantum = getRawTimeQuantum(theExecutionTime);
      previousTime = Math.max(theExecutionTime - quantum, aRate.theStartTime);
    }

    private AnnotatedDouble getQPerMilli(long executionTime) {
      AnnotatedDouble multiplier = null;
      TripletValue tv = null;
      for (int i = 0; tv == null && i < tks.length; i++) {
        tv = (TripletValue) triplets.get(tks[i]);
      }
      if (tv != null) {
        long startDate = tv.getStartDate();
        long endDate = tv.getEndDate();
        if (startDate <= executionTime &&
  	    endDate > executionTime) {
          multiplier = tv.getMultiplier();
        }
      }
      if (multiplier == null) multiplier = new AnnotatedDouble(1.0);
      multiplier.value = ((theFailureConsumptionRate.theRateValue
                           * multiplier.value
                           / theFailureConsumptionRate.theRateMultiplier)
                          / ONE_DAY);
      return multiplier;
    }

    public AnnotatedDouble getQuantity(long executionTime) {
      checkExecutionTime(executionTime);
      AnnotatedDouble v = getQPerMilli(executionTime);
      if (v.value <= 0.0) {
        v.value = 0.0;
        return v;
      }
      long elapsed = executionTime - previousTime;
      previousTime = executionTime;
      v.value = random.nextPoisson(elapsed * v.value);
      return v;
    }

    /**
     * Use a time quantum such that a quantity of at least one is expected.
     **/
    public long getTimeQuantum(long executionTime) {
      long interval = getRawTimeQuantum(executionTime);
      return previousTime + interval - executionTime;
    }

    public long getRawTimeQuantum(long executionTime) {
      AnnotatedDouble v = getQPerMilli(executionTime);
      if (v.value <= 0.0) return MAX_INTERVAL;
      long interval = (long) (1.0 / v.value);
      if (interval < MIN_INTERVAL) interval = MIN_INTERVAL;
      if (interval > MAX_INTERVAL) interval = MAX_INTERVAL;
      return interval;
    }
  }

  private class WatchDocument extends PlainDocument {
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
      super.insertString(offset, str, a);
      updateButtons();
    }
    public void remove(int offs, int len) throws BadLocationException {
      super.remove(offs, len);
      updateButtons();
    }
  }

  protected void checkExecutionTime(long executionTime) {
    // Base class does nothing.
  }

  protected TripletFCPlugIn() {
    consumerNameToId.put(ALL_CONSUMERS, ALL_CONSUMERS);
    consumerIdToName.put(ALL_CONSUMERS, ALL_CONSUMERS);
    consumableNameToId.put(ALL_CONSUMABLES, ALL_CONSUMABLES);
    consumableIdToName.put(ALL_CONSUMABLES, ALL_CONSUMABLES);
  }

  private void guiInit() {
    String[] labels = getTripletValueNames();
    Class[] classes = getTripletValueClasses();
    Object[] values = getTripletDefaultValues();
    valueTextField = new JTextField[labels.length];
    valueLabel = new JLabel[labels.length];
    for (int i = 0; i < labels.length; i++) {
      Class cls = classes[i];
      String s = values[i].toString();
      if (cls == Double.class) {
        valueTextField[i] = new JTextField(new WatchDocument(), s, 10);
      } else if (cls == EGDate.class) {
        valueTextField[i] = new JTextField(new WatchDocument(), s, 20);
      } else {
        valueTextField[i] = new JTextField(new WatchDocument(), s, 20);
      }
      valueLabel[i] = new JLabel(labels[i]);
    }
    prevButton.addActionListener(prevListener);
    nextButton.addActionListener(nextListener);
    saveButton.addActionListener(saveListener);
    deleteButton.addActionListener(deleteListener);
    buttons.add(prevButton);
    buttons.add(nextButton);
    buttons.add(saveButton);
    buttons.add(deleteButton);
    cb1.addActionListener(cbListener);
    cb2.addActionListener(cbListener);
    cb3.addActionListener(cbListener);
  }

  /**
   * @return the name of this plugin
   **/
  public abstract String getPlugInName();

  public abstract String getDescription();

  protected abstract String[] getTripletValueNames();

  protected abstract Class[] getTripletValueClasses();
  
  protected abstract Object[] getTripletDefaultValues();

  protected abstract TripletValue createTripletValue(String[] args);

  public boolean isConfigurable() {
    return true;
  }

  public abstract void setParameter(String parameter);

  protected static void addItem(JPanel message, int x, int y, JComponent c) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.anchor = gbc.WEST;
    message.add(c, gbc);
  }

  protected static void addItem(JPanel message, int y, JComponent c) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.gridy = y;
    gbc.anchor = gbc.CENTER;
    message.add(c, gbc);
  }

  private void setEnableListener(boolean newEnable) {
    enableListener = newEnable;
  }

  public void configure(java.awt.Component c) {
    message = createMessage();
    while (true) {
      int result = OptionPane.showOptionDialog(c,
                                               message,
                                               "Configure " + getPlugInName(),
                                               OptionPane.OK_CANCEL_OPTION,
                                               OptionPane.QUESTION_MESSAGE,
                                               null, null, null);
      
      if (result != OptionPane.OK_OPTION) break;
      if (saveTriplet()) break;
      result = JOptionPane.showConfirmDialog(message,
                                             "Invalid parameters entered",
                                             "Invalid Parameters", JOptionPane.OK_CANCEL_OPTION,
                                             JOptionPane.ERROR_MESSAGE);
      if (result != OptionPane.OK_OPTION) break;
    }
  }

  protected JPanel createMessage() {
    if (valueTextField == null) guiInit();
    JPanel message = new JPanel(new GridBagLayout());
    setEnableListener(false);
    cb1.removeAllItems();
    cb1.addItem(ALL_CONSUMABLES);
    Iterator consumablesIter = consumableNameToId.keySet().iterator();
    while (consumablesIter.hasNext()) {
      String s = (String) consumablesIter.next();
      if (!s.equals(ALL_CONSUMABLES)) {
        cb1.addItem(s);
      }
    }
    if (currentConsumable == null) {
      currentConsumable = ALL_CONSUMABLES;
    }
    cb1.setSelectedItem(currentConsumable);

    cb2.removeAllItems();
    cb2.addItem(ALL_CONSUMERS);
    Iterator consumersIter = consumerNameToId.keySet().iterator();
    while (consumersIter.hasNext()) {
      String s = (String) consumersIter.next();
      if (!s.equals(ALL_CONSUMERS)) {
        cb2.addItem(s);
      }
    }
    if (currentConsumer == null) {
      currentConsumer = ALL_CONSUMERS;
    }
    cb2.setSelectedItem(currentConsumer);

    cb3.removeAllItems();
    cb3.addItem(ALL_CLUSTERS);
    if (theEventGenerator != null) {
      // First, add an "All" option for clusters and consumers

      // Get the cluster names from the EventGenerator and add to the list
      String[] clusterNames = theEventGenerator.getClusterNames();
      Arrays.sort(clusterNames);
      for (int i = 0; i < clusterNames.length; i++) {
        cb3.addItem(clusterNames[i]);
      }
    }
    if (currentCluster == null) {
      currentCluster = ALL_CLUSTERS;
    }
    cb3.setSelectedItem(currentCluster);

    setEnableListener(true);
    cbListener.actionPerformed(null);

    int row = 0;
    addItem(message, 0, row, new JLabel("Item"));
    addItem(message, 1, row, cb1);
    row++;

    addItem(message, 0, row, new JLabel("Consumer"));
    addItem(message, 1, row, cb2);
    row++;

    addItem(message, 0, row, new JLabel("Cluster"));
    addItem(message, 1, row, cb3);
    row++;

    for (int i = 0; i < valueTextField.length; i++, row++) {
      addItem(message, 0, row, valueLabel[i]);
      addItem(message, 1, row, valueTextField[i]);
    }

    addItem(message, 100, buttons);
    return message;
  }

  protected boolean saveTriplet() {
    TripletKey key = computeTripletKey();
    TripletValue val = computeTripletValue();
    if (key == null || val == null) {
      return false;
    } else {
      currentTripletKey = key;
      triplets.put(key, val);
      updateButtons();
      return true;
    }
  }

  private TripletKey computeTripletKey() {
    try {
      String consumable = (String) cb1.getSelectedItem();
      String consumer = (String) cb2.getSelectedItem();
      String cluster = (String) cb3.getSelectedItem();
      return new TripletKey((String) consumableNameToId.get(consumable),
                            (String) consumerNameToId.get(consumer),
                            cluster);
    } catch (RuntimeException re) {
      return null;
    }
  }

  private TripletValue computeTripletValue() {
    try {
      String[] values = new String[valueTextField.length];
      for (int i = 0; i < values.length; i++) {
        values[i] = valueTextField[i].getText();
      }
      return createTripletValue(values);
    } catch (RuntimeException re) {
      return null;
    }
  }

  private void setTripletValue(TripletValue tv) {
    for (int i = 0; i < valueTextField.length; i++) {
      String s;
      if (tv != null) {
        s = tv.getFieldValue(i).toString();
      } else {
        s = getTripletDefaultValues()[i].toString();
      }
      try {
        valueTextField[i].setText(s);
      } catch (IllegalArgumentException iae) {
        valueTextField[i].setText(getTripletDefaultValues()[i].toString());
      }
    }
  }

  private static interface ValueCoder {
    int getFieldCount();
    String getValue(Object o, int i);
    Object createKey(String keyString);
    Object createValue(String[] args);
  }

  private ValueCoder singleValueCoder = new ValueCoder() {
    public int getFieldCount() {
      return 1;
    }
    public String getValue(Object o, int i) {
      return (String) o;
    }
    public Object createKey(String keyString) {
      return keyString;
    }
    public Object createValue(String[] args) {
      return args[0];
    }
  };

  private ValueCoder tripleValueCoder = new ValueCoder() {
    public int getFieldCount() {
      return getTripletValueNames().length;
    }
    public String getValue(Object o, int i) {
      TripletValue tv = (TripletValue) o;
      return tv.getFieldValue(i).toString();
    }
    public Object createKey(String keyString) {
      return new TripletKey(keyString);
    }
    public Object createValue(String[] args) {
      return createTripletValue(args);
    }
  };

  private void saveMap(Properties props, Map map, String prefix, ValueCoder vc) {
    int count = 0;
    int nfields = vc.getFieldCount();
    for (Iterator keys = map.keySet().iterator(); keys.hasNext(); count++) {
      Object key = keys.next();
      String keyString = key.toString();
      Object val = map.get(key);
      props.setProperty(prefix + count + ".key", keyString);
      for (int j = 0; j < nfields; j++) {
        props.setProperty(prefix + count + ".val." + j, vc.getValue(val, j));
      }
    }
    props.setProperty(prefix + "count", String.valueOf(count));
  }

  public void save(Properties props, String prefix) {
    saveMap(props, consumableNameToId, prefix + "consumables.", singleValueCoder);
    saveMap(props, consumerNameToId, prefix + "consumers.", singleValueCoder);
    saveMap(props, triplets, prefix + "triplets.", tripleValueCoder);
    props.setProperty(prefix + "currentConsumer", currentConsumer);
    props.setProperty(prefix + "currentConsumable", currentConsumable);
    props.setProperty(prefix + "currentCluster", currentCluster);
  }

  private void restoreMap(Properties props, Map map, Map reverseMap, String prefix, ValueCoder vc) {
    int count = Integer.parseInt(props.getProperty(prefix + "count"));
    int nfields = vc.getFieldCount();
    String[] args = new String[nfields];
    for (int i = 0; i < count; i++) {
      try {
        Object key = vc.createKey(props.getProperty(prefix + i + ".key"));
        for (int j = 0; j < nfields; j++) {
          args[j] = props.getProperty(prefix + i + ".val." + j);
        }
        Object val = vc.createValue(args);
        map.put(key, val);
        if (reverseMap != null) reverseMap.put(val, key);
      } catch (RuntimeException re) {
        re.printStackTrace();
      }
    }
  }

  public void restore(Properties props, String prefix) {
    try {
      restoreMap(props, consumableNameToId, consumableIdToName, prefix + "consumables.", singleValueCoder);
      restoreMap(props, consumerNameToId, consumerIdToName, prefix + "consumers.", singleValueCoder);
      restoreMap(props, triplets, null, prefix + "triplets.", tripleValueCoder);
      currentConsumer = props.getProperty(prefix + "currentConsumer");
      currentConsumable = props.getProperty(prefix + "currentConsumable");
      currentCluster = props.getProperty(prefix + "currentCluster");
    } catch (Exception e) {
      e.printStackTrace();// State not present in props
    }
  }

  public void setEventGenerator(EventGenerator eg) {
    theEventGenerator = eg;
  }

  /**
   * Create a FailureConsumptionItem for this plugin to handle a
   * particular FailureConsumptionRate. Override this to filter the
   * items being handled.
   **/
  public FailureConsumptionPlugInItem createFailureConsumptionItem
    (FailureConsumptionRate aRate,
     FailureConsumptionSegment aSegment,
     long theExecutionTime,
     FailureConsumptionPlugInItem aFailureConsumptionPlugInItem)
  {
    consumableNameToId.put(aRate.theItemName, aRate.theItemIdentification);
    consumableIdToName.put(aRate.theItemIdentification, aRate.theItemName);
    consumerNameToId.put(aRate.theConsumer, aRate.theConsumerId);
    consumerIdToName.put(aRate.theConsumerId, aRate.theConsumer);
    if (aFailureConsumptionPlugInItem instanceof Item
        && aFailureConsumptionPlugInItem.theFailureConsumptionRate == aRate) {
      return aFailureConsumptionPlugInItem;
    }
    return new Item(aRate, theExecutionTime, aSegment);
  }
}
