/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.society;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.cougaar.domain.planning.ldm.plan.*;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.mlm.ui.data.UISimpleInventory;
import org.cougaar.domain.mlm.ui.data.UISimpleNamedSchedule;
import org.cougaar.domain.mlm.ui.data.UISimpleSchedule;
import org.cougaar.domain.mlm.ui.planviewer.ConnectionHelper;

public class  InnerSocietyUI
{
  //private String _urlName  = new String("http://localhost:5555/");
  private static String _inventoryPathName = new String("/alpine/demo");
  private static String _inventoryPspName  = new String("INVENTORY.PSP");


    /**
    * @param host_cluster_URL:
    *               URL to Cluster where OrganizationUID lives.
    *               Form should be something like this:
    *               http//www.myhost.com:5555/$CLUSTERID
    *               Note on convention: no trailing slash
    *
    * @return UISimpleInventory
    **/
  public static UISimpleInventory getDummyQuantityData(String host_cluster_URL) {
       UISimpleInventory uisi = getQuantityData( host_cluster_URL + "/$3-69-ARBN");
       return uisi;
  }

  /**
    * @param host_cluster_URL:
    *               URL to Cluster where OrganizationUID lives.
    *               Form should be something like this:
    *               http//www.myhost.com:5555/$CLUSTERID
    *               Note on convention: no trailing slash
    *
    * @return UISimpleInventory
    **/
  public static UISimpleInventory getQuantityData(String host_cluster_URL)
  {
    // 1st PASS
    System.out.println("trying 1");
    Vector assetNamesVec = null;
    InputStream is = null;
    try
    {
       ConnectionHelper connection =
              new ConnectionHelper(host_cluster_URL, _inventoryPathName, _inventoryPspName);
       connection.sendData("ASSET");
       is = connection.getInputStream();
    } catch (Exception e)
    {
       e.printStackTrace();
    }
    System.out.println("finished 1");

    try {
       ObjectInputStream p = new ObjectInputStream(is);
       assetNamesVec = (Vector) p.readObject();
    } catch (Exception e) {
       e.printStackTrace();
    }

    System.out.println("size= " + assetNamesVec.size());

    String assetName = (String) assetNamesVec.get(0);
    System.out.println("assetName= " + assetName);

    System.out.println("trying 2");

    UISimpleInventory inventory =null;
    // 2nd PASS
    try
    {
      ConnectionHelper connection =
            new ConnectionHelper(host_cluster_URL, _inventoryPathName, _inventoryPspName);
      connection.sendData(assetName);
      is = connection.getInputStream();
      inventory = readReply(is);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return inventory;
  }

  private static UISimpleInventory readReply(InputStream is)
  {
    UISimpleInventory _inventory = null;

    // read result which is a UISimpleInventory object
    try
    {
      ObjectInputStream p = new ObjectInputStream(is);
      _inventory = (UISimpleInventory)p.readObject();
    } catch (Exception e) {
      System.out.println("Object read exception: " + e);
      return null;
    }
    System.out.println("Asset name: " + _inventory.getAssetName());
    Vector schedulesVec = _inventory.getSchedules();
    System.out.println("scheduleSize= " + schedulesVec.size());
    for (int i = 0; i < schedulesVec.size(); i++)
    {
      UISimpleNamedSchedule nameSchedule = (UISimpleNamedSchedule) schedulesVec.get(i);
      Vector simpleScheduleVec = nameSchedule.getSchedule();
      System.out.println("simpleVecSize " + nameSchedule.getSchedule().size());
      System.out.println("name " + nameSchedule.getName());
      for (int j = 0; j < simpleScheduleVec.size(); j++)
      {
        UISimpleSchedule finalSchedule = (UISimpleSchedule) simpleScheduleVec.get(j);
        System.out.println("q = " + finalSchedule.getQuantity());
        System.out.println("sd = " + finalSchedule.getStartDate());
      }
    }
    return _inventory;
  }

}
