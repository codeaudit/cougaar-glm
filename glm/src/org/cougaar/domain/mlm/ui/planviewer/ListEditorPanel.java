/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;
import org.cougaar.util.OptionPane;

import javax.swing.*;

public class ListEditorPanel extends JPanel
{
   private JTextField addField = new JTextField(25);
   private JButton addButton = new JButton("Add");
   private JButton removeButton = new JButton("Remove Selected Items");
   private JList itemList = new JList();

   private ActionListener addButtonListener = new ActionListener()
                  { public void actionPerformed(ActionEvent ae)
                    {
                       final DefaultListModel model = (DefaultListModel)itemList.getModel();
                       model.addElement(addField.getText());
                             addField.setText("");

                       SwingUtilities.invokeLater(new Runnable()
                              {
                                 public void run()
                                 {
                                    itemList.ensureIndexIsVisible(
                                                model.getSize()-1);
                                 }
                              } );
                      
                    }  
                  } ;
   
   private ActionListener removeButtonListener = new ActionListener()
                  { public void actionPerformed(ActionEvent ae)
                    {
                        int[] selections = itemList.getSelectedIndices();
                        DefaultListModel model = (DefaultListModel)itemList.getModel();

                        for (int i = 0; i < selections.length; i++)
                                {
                           model.removeElementAt(selections[i]-i);
                                }
                    }
                  } ;

   public ListEditorPanel(Vector items)
   {
      setLayout(new BorderLayout(5,5));

        JPanel northPanel = new JPanel(new BorderLayout(10,10));
        northPanel.add(addField, BorderLayout.CENTER);

      addButton.addActionListener(addButtonListener);
      northPanel.add(addButton, BorderLayout.EAST);

      add(northPanel, BorderLayout.NORTH);

        DefaultListModel model = new DefaultListModel();
      for (int i=0; i<items.size(); i++)
      {
           model.addElement(items.elementAt(i));
      }
      itemList.setModel(model);
      JScrollPane sp = new JScrollPane(itemList);
 
      add(sp, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      removeButton.addActionListener(removeButtonListener);
        southPanel.add(removeButton);
        add(southPanel, BorderLayout.SOUTH);
   }

   public Vector getListData()
   {
      Vector v = new Vector();
      DefaultListModel m = (DefaultListModel)itemList.getModel();
      for(Enumeration e = m.elements();e.hasMoreElements();)
         v.addElement(e.nextElement());
      return v;
   }

////////////For Testing Purposes/////////////
   public static void main(String[] args)
   {
        Vector listItems = new Vector();
        String[] items = { "Apples", "Oranges", "Grapes", "Bananas", "Peaches",
                         "Pears", "Watermelon", "Persimmons", "Guava", "Kiwi" };
      for(int i = 0; i < items.length; i++)
      {
           listItems.addElement(items[i]);
      }
      ListEditorPanel p = new ListEditorPanel(listItems);
      String title = "Add/Remove \"To\" Items";
        int selection = OptionPane.showOptionDialog(null, p, title, 
                                                   OptionPane.OK_CANCEL_OPTION,
                                                   OptionPane.PLAIN_MESSAGE,
                                                    null, null, null); 
      if(selection == OptionPane.OK_OPTION)
         System.out.println("Items have been edited!");
   }
////////////For Testing Purposes/////////////
}

