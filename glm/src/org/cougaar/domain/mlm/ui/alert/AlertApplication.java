package org.cougaar.domain.mlm.ui.alert;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import org.cougaar.util.ThemeFactory;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

public class AlertApplication extends JSplitPane
    implements AlertClusterSelector.Listener, ListSelectionListener
{
    public static void main(String[] args) {
        org.cougaar.core.society.Bootstrapper.launch(AlertApplication.class.getName(), args);
    }

    public static void launch(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-demo")) {
                setGUIDefaults();
                continue;
            }
            System.err.println("Unrecognized argument: " + args[i]);
        }
        JFrame frame = new JFrame("Alert Manager");
        frame.setDefaultCloseOperation(3);
        Hashtable map = ConnectionHelper.getClusterInfo(frame);
        if (map == null) System.exit(0);
        AlertApplication app = new AlertApplication(map);
        frame.getContentPane().add(app);
        frame.pack();
        frame.show();
    }

    private static void setGUIDefaults() {
        ThemeFactory.establishMetalTheme();
    }

    private static class ClusterInfo {
        String clusterName;
        String clusterURL;
        AlertThread thread;
        ClusterInfo(String clusterName, String clusterURL) {
            this.clusterName = clusterName;
            this.clusterURL = clusterURL;
        }
    }

    private static class AlertItem {
        String cluster;
        String uid;
        String title;
        String severity;
        String message;
        Action[] choices;
        public AlertItem(String cluster, String uid, String title, String severity,
                         String message, Action[] choices)
        {
            this.cluster = cluster;
            this.uid = uid;
            this.title = title;
            this.severity = severity;
            this.message = message;
            this.choices = choices;
        }
        public boolean equals(Object o) {
            if (o instanceof AlertItem) {
                return uid.equals(((AlertItem) o).uid);
            }
            return false;
        }
        public String toString() {
            return uid;
        }
    }

    private static final int CLUSTER_COLUMN  = 0;
    private static final int UID_COLUMN      = 1;
    private static final int TITLE_COLUMN    = 2;
    private static final int SEVERITY_COLUMN = 3;
    private static final int N_COLUMNS       = 4;

    private class MyTableModel extends AbstractTableModel {
        List items = new ArrayList();

        public synchronized void add(AlertItem item) {
            int row = items.size();
            items.add(item);
            fireTableRowsInserted(row, row);
            normalizeSelection(item);
        }

        public synchronized void remove(AlertItem item) {
            AlertItem selectedItem = getSelectedItem();
            if (selectedItem != null && selectedItem.equals(item)) {
                selectedItem = null;
                pendingAlerts.clearSelection();
            }
            int row = items.indexOf(item);
            if (row >= 0) {
                items.remove(row);
                fireTableRowsDeleted(row, row);
            }
            normalizeSelection(selectedItem);
        }

        public synchronized void removeCluster(String clusterName) {
            AlertItem selectedItem = getSelectedItem();
            if (selectedItem != null && selectedItem.cluster.equals(clusterName)) {
                selectedItem = null;
                pendingAlerts.clearSelection();
            }
            for (Iterator i = items.iterator(); i.hasNext(); ) {
                AlertItem item = (AlertItem) i.next();
                if (item.cluster.equals(clusterName)) i.remove();
            }
            fireTableDataChanged();
            normalizeSelection(selectedItem);
        }

        private AlertItem getSelectedItem() {
            int selectedRow = pendingAlerts.getSelectedRow();
            if (selectedRow < 0) return null;
            return (AlertItem) items.get(selectedRow);
        }

        private void normalizeSelection(AlertItem defaultItem) {
            int row = -1;
            if (defaultItem != null) {
                row = items.indexOf(defaultItem);
            }
            if (row < 0 && pendingAlerts.getSelectedRow() < 0) {
                row = getRowCount() - 1;
            }
            if (row >= 0) pendingAlerts.setRowSelectionInterval(row, row);
        }

        public int getRowCount() {
            return items.size();
        }
        public int getColumnCount() {
            return N_COLUMNS;
        }

        public String getColumnName(int column) {
            switch (column) {
            case CLUSTER_COLUMN: return "Cluster";
            case UID_COLUMN: return "Uid";
            case TITLE_COLUMN: return "Title";
            case SEVERITY_COLUMN: return "Severity";
            }
            return null;
        }

        public int widthOf(String s) {
            return new JLabel(s).getPreferredSize().width;
        }

        public int getPreferredWidth(int column) {
            switch (column) {
            case CLUSTER_COLUMN:  return widthOf("3-69-ARBN-MORE");
            case UID_COLUMN:      return widthOf("3-69-ARBN/1234567890123");
            case TITLE_COLUMN:    return widthOf("Failure/Consumption Rate Deviation");
            case SEVERITY_COLUMN: return widthOf("Severity");
            }
            return 75;
        }

        public AlertItem getRowObject(int row) {
            if (row < 0 || row >= items.size()) return null;
            return (AlertItem) items.get(row);
        }

        public Object getValueAt(int row, int column) {
            AlertItem item = getRowObject(row);
            switch (column) {
            case CLUSTER_COLUMN: return item.cluster;
            case UID_COLUMN: return item.uid;
            case TITLE_COLUMN: return item.title;
            case SEVERITY_COLUMN: return item.severity;
            }
            return null;
        }
    }

    private Map myClusterInfo = new HashMap();
    Set myClusterNames;
    private MyTableModel model = new MyTableModel();
    private JTable pendingAlerts = new JTable(model);
    private JScrollPane pane = new JScrollPane(pendingAlerts);
    SimpleAlertDisplay alertPanel = new SimpleAlertDisplay();

    public AlertApplication(Hashtable clusterInfo) {
        super(JSplitPane.VERTICAL_SPLIT);
        JPanel top = new JPanel(new GridBagLayout());
        myClusterNames = clusterInfo.keySet();
        for (Iterator i = myClusterNames.iterator(); i.hasNext(); ) {
            String clusterName = (String) i.next();
            myClusterInfo.put(clusterName,
                              new ClusterInfo(clusterName, (String) clusterInfo.get(clusterName)));
        }
        AlertClusterSelector clusterSelector = new AlertClusterSelector(myClusterNames);
        clusterSelector.addListener(this);
        pendingAlerts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingAlerts.getSelectionModel().addListSelectionListener(this);
        TableColumnModel cm = pendingAlerts.getColumnModel();
        for (int column = 0, ncolumns = model.getColumnCount(); column < ncolumns; column++) {
            cm.getColumn(column).setPreferredWidth(model.getPreferredWidth(column));
        }
        Dimension vpsize = pendingAlerts.getPreferredSize();
        System.out.println("vpsize=" + vpsize);
        vpsize = new Dimension(vpsize.width, 200);
        pendingAlerts.setPreferredScrollableViewportSize(vpsize);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = gbc.NORTH;
        gbc.fill = gbc.VERTICAL;
        top.add(clusterSelector, gbc);
        gbc.gridx = 1;
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        top.add(pane, gbc);
        setTopComponent(top);
        setBottomComponent(alertPanel);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            AlertItem item = model.getSelectedItem();
            if (item != null) {
                System.out.println("select " + item);
                alertPanel.init(item.uid, item.cluster, item.message, item.choices);
            } else {
                System.out.println("deselect");
                alertPanel.clear();
            }
        }
    }

    private void respond(AlertItem item, int choice) {
        System.out.println("ChoiceAlertDisplay:handleChoice - uid = " + item.uid +
                           " choice = " + choice);
        boolean success = true;
        ClusterInfo info = (ClusterInfo) myClusterInfo.get(item.cluster);

        try {
            ConnectionHelper connection = 
                new ConnectionHelper(info.clusterURL,
                                     ModifyAlertPSPConnectionInfo.current(),
                                     false);
      
            String modifyRequest =
                PSP_ModifyAlert.generateChoicePostData(item.uid, true, choice);
            System.out.println("ChoiceAlertDisplay:handleChoice - modify request " + 
                               modifyRequest);
            connection.sendData(modifyRequest);
      
            InputStream is = connection.getInputStream();
            
            String reply = new String(connection.getResponse());
            System.out.println("got response " + reply);
            model.remove(item);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public synchronized void clusterSelected(final String clusterName) {
        ClusterInfo info = (ClusterInfo) myClusterInfo.get(clusterName);
        if (info.thread != null) return; // Already listening
        info.thread = new AlertThread(clusterName, info.clusterURL) {
            public void displayError(String error) {
                System.err.println(error);
            }
            protected String getURLSuffix() {
                return "?" + PSP_Alert.RAW_PARAM + "true";
            }
            public void handleItem(String uid, String title, String severity,
                                   String filename, String codedChoices) {
                try {
                    String[] choices =
                    PSP_Alert.parseChoiceParameter(URLDecoder.decode(codedChoices));
                    Action[] actions = new Action[choices.length];
                    final AlertItem item = new AlertItem(clusterName, uid,
                                                         URLDecoder.decode(title),
                                                         URLDecoder.decode(severity),
                                                         URLDecoder.decode(filename),
                                                         actions);
                    for (int i = 0; i < actions.length; i++) {
                        final int choiceIndex = i;
                        actions[choiceIndex] = new AbstractAction(choices[choiceIndex]) {
                            public void actionPerformed(ActionEvent e) {
                                respond(item, choiceIndex);
                            }
                        };
                    }
                    if (item.message.equals("")) {
                        model.remove(item);
                    } else {
                        model.add(item);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        info.thread.start();
    }

    public synchronized void clusterDeselected(String clusterName) {
        ClusterInfo info = (ClusterInfo) myClusterInfo.get(clusterName);
        model.removeCluster(clusterName);
        if (info.thread == null) return; // Already not listening
        info.thread.stopListening();
        info.thread = null;
    }
}
