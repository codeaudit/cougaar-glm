package org.cougaar.domain.mlm.ui.alert;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class AlertClusterSelector extends JPanel implements ListSelectionListener {
    public static interface Listener {
        void clusterSelected(String clusterName);
        void clusterDeselected(String clusterName);
    }

    private JList list;
    private int size;
    private BitSet state = new BitSet();
    private List listeners = new ArrayList(1);

    public AlertClusterSelector(Collection clusters) {
        super(new BorderLayout());
        setBorder(BorderFactory
                  .createCompoundBorder(BorderFactory
                                        .createTitledBorder(BorderFactory.createEtchedBorder(),
                                                            "Cluster Selection"),
                                        BorderFactory
                                        .createEmptyBorder(10, 10, 10, 10)));
        SortedSet clusterNames = new TreeSet(clusters);
        size = clusterNames.size();
        list = new JList(new Vector(clusterNames));
        JScrollPane pane = new JScrollPane(list);
        list.addListSelectionListener(this);

        JPanel buttons = new JPanel();
        JButton selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAll();
            }
        });
        JButton selectNoneButton = new JButton("Select None");
        selectNoneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectNone();
            }
        });
        buttons.add(selectAllButton);
        buttons.add(selectNoneButton);
        add(pane);
        add(buttons, BorderLayout.SOUTH);
    }

    private void selectAll() {
        list.addSelectionInterval(0, size - 1);
    }

    private void selectNone() {
        list.removeSelectionInterval(0, size - 1);
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return; // Wait for things to settle
        ListSelectionModel selectionModel = list.getSelectionModel();
        ListModel listModel = list.getModel();
        for (int first = e.getFirstIndex(), last = e.getLastIndex(); first < last; first++) {
            boolean isSelected = selectionModel.isSelectedIndex(first);
            String clusterName = listModel.getElementAt(first).toString();
            if (isSelected != state.get(first)) {
                if (isSelected) {
                    state.set(first);
                    for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                        Listener l = (Listener) i.next();
                        l.clusterSelected(clusterName);
                    }
                } else {
                    state.clear(first);
                    for (Iterator i = listeners.iterator(); i.hasNext(); ) {
                        Listener l = (Listener) i.next();
                        l.clusterDeselected(clusterName);
                    }
                }
            }
        }
    }
}
