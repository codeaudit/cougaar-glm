/*
 * Copyright 1997-1999 Defense Advanced Research Projects Agency (DARPA)
 * and ALPINE (A BBN Technologies (BBN) and Raytheon Systems Company
 * (RSC) Consortium). This software to be used in accordance with the
 * COUGAAR license agreement.  The license agreement and other
 * information on the Cognitive Agent Architecture (COUGAAR) Project can
 * be found at http://www.cougaar.org or email: info@cougaar.org.
 */

package org.cougaar.domain.mlm.ui.psp.transportation.data;

import org.cougaar.domain.planning.ldm.plan.Verb;

import org.cougaar.core.util.PrettyStringPrinter;
import org.cougaar.core.util.SelfPrinter;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.cougaar.domain.glm.ldm.plan.GeolocLocation;
import org.cougaar.domain.glm.ldm.plan.Position;
import org.cougaar.domain.glm.ldm.plan.NamedPosition;

/**
 * @see AbstractPrinter
 */
public class UIPrinter extends PrettyStringPrinter {

  public static void main(String[] args) {
    testMain("prettystring");
  }

  public UIPrinter(java.io.OutputStream out) {
    super(out);
  }

  /**
   * Only <code>SelfPrinter</code>'s "print" method should call
   * this!  Use "printObject" for all other cases!!!!!
   */
  public void print(Object o, String name) {
    if (o == null) {
    } else if (o instanceof SelfPrinter) {
      printSelfPrinter((SelfPrinter)o, name);
    } else if (o instanceof Collection) {
      printCollection((Collection)o, name);
    } else {
      ObjectInfo oi = getFullObjectInfo(o);
      if (oi instanceof StringObjectInfo) {
        print(oi.getClassName(), name, ((StringObjectInfo)oi).getValue());
      } else if (oi instanceof ListObjectInfo) {
        printListObjectInfo((ListObjectInfo)oi, name);
      } else
        throw new RuntimeException("Never!");
    }
  }

  protected void printSelfPrinter (SelfPrinter sp, String name) {
    if (sp != null) {
      printBegin(sp.getClass().getName(), name);
      sp.printContent(this);
      printEnd(name);
    }
  }

  protected void printCollection(Collection col, String name) {
    if (col != null) {
      printBeginCollection(col.getClass().getName(), name, col.size());
      Iterator iter = col.iterator();
      while (iter.hasNext())
        printElement(iter.next());
      printEndCollection(name);
    }
  }

  protected void printElement(Object o) {
    if (o == null) 
      return;
    printBeginElement();
    if (o instanceof SelfPrinter) {
      printSelfPrinter((SelfPrinter)o, "element");
    } else if (o instanceof java.util.Collection) {
      printCollection((Collection)o, "element");
    } else {
      ObjectInfo oi = getFullObjectInfo(o);
      if (oi instanceof StringObjectInfo) {
	printStringElement((StringObjectInfo)oi);
      } else if (oi instanceof ListObjectInfo) {
        printListObjectInfo((ListObjectInfo)oi, "element");
      } else
        throw new RuntimeException("Never!");
    }
    printEndElement();
  }

  protected void printStringElement (StringObjectInfo soi) {
    print (soi.getClassName(), "element", soi.getValue());
  }

  /**
   * <b>ALL</b> objects that might be printed <b>MUST</b> be listed here,
   * with these exceptions:<br>
   * <ul>
   *   <li>null</li>
   *   <li>instanceof SelfPrinter</li>
   *   <li>java.lang.String</li>
   *   <li>instanceof java.util.Collection</li>
   * </ul><br>
   */
  protected ObjectInfo getFullObjectInfo(Object o) {
    if (o instanceof Date) {
      ListObjectInfo objInfo = new ListObjectInfo("java.util.Date");
      objInfo.addNameVal("Time", Long.toString(((Date)o).getTime()));
      return objInfo;
    } else if (o instanceof Verb) {
      return new StringObjectInfo("org.cougaar.domain.planning.ldm.plan.Verb", ((Verb)o).toString());
    } else if (o instanceof Position) {
      ListObjectInfo objInfo = new ListObjectInfo("org.cougaar.domain.planning.ldm.plan.Position");
      Position pos = (Position)o;
      if (pos.getLatitude() != null)
        objInfo.addNameVal("Latitude", 
                           pos.getLatitude().getDegrees()+"degrees");
      if (pos.getLongitude() != null)
        objInfo.addNameVal("Longitude", 
                           pos.getLongitude().getDegrees()+"degrees");
      if (o instanceof NamedPosition) {
        objInfo.setClassName("org.cougaar.domain.planning.ldm.plan.NamedPosition");
        NamedPosition npos = (NamedPosition)o;
        objInfo.addNameVal("Name", npos.getName());
        if (o instanceof GeolocLocation) {
          objInfo.setClassName("org.cougaar.domain.planning.ldm.plan.GeolocLocation");
          GeolocLocation loc = (GeolocLocation)o;
          objInfo.addNameVal("GeolocCode", loc.getGeolocCode());
          objInfo.addNameVal("InstallationTypeCode", 
                             loc.getInstallationTypeCode());
          objInfo.addNameVal("CountryStateCode", loc.getCountryStateCode());
          objInfo.addNameVal("CountryStateName", loc.getCountryStateName());
          objInfo.addNameVal("IcaoCode", loc.getIcaoCode());
        }
      }
      return objInfo;
    } else if ((o instanceof String) ||
               (o instanceof Integer) ||
	       (o instanceof Boolean) ||
               (o instanceof Character) ||
               (o instanceof Byte) ||
               (o instanceof Short) ||
               (o instanceof Long) ||
               (o instanceof Float) ||
               (o instanceof Double)) {
      return new StringObjectInfo(o.getClass().getName(), o.toString());
    } else {
      throw new RuntimeException(
        "Unhandled Object type in AbstractPrinter.getObjectInfo("+
        o.getClass().getName()+")!");
    }
  }
}
