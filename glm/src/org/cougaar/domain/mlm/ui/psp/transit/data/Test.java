package org.cougaar.domain.mlm.ui.psp.transit.data;

import org.cougaar.domain.mlm.ui.psp.transit.data.xml.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.hierarchy.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.population.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.owner.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.prototypes.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.instances.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.legs.*;
import org.cougaar.domain.mlm.ui.psp.transit.data.locations.*;

import java.io.Writer;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.OutputStreamWriter;

import java.util.List;

/**
 * Test the PSP data XMLization
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:28 $
 * @since 1/24/01
 **/
public class Test{

  //Variables:
  ////////////

  //Constructors:
  ///////////////

  //Members:
  //////////

  public static void testHierarchy() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      HierarchyData hd1 = new HierarchyData();
      Organization od1A = 
	new Organization();
      od1A.setUID("3ID");
      od1A.setPrettyName("3rd Infantry Division");
      od1A.addRelation("6ALFSQ",
		       Organization.ADMIN_SUBORDINATE);
      hd1.addOrgData(od1A);

      hd1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new HierarchyDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void testPopulation() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      PopulationData pd1 = new PopulationData();

      ConveyancePrototype cp1 = new ConveyancePrototype();
      cp1.UID="UID1";
      cp1.conveyanceType=ConveyancePrototype.SHIP;
      cp1.volCap=1000;
      cp1.weightCap=2000;
      cp1.aveSpeed=2;
      cp1.alpTypeID="RORO";
      cp1.nomenclature="Boat";
      pd1.addPrototype(cp1);

      ConveyanceInstance ci1 = new ConveyanceInstance();
      ci1.UID="UID2";
      ci1.prototypeUID="UID1";
      ci1.bumperNo="B1";
      ci1.homeLocID="BOSP";
      ci1.ownerID="STSHIP";

      pd1.addInstance(ci1);
      
      pd1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new PopulationDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void testOwner() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      OwnerData od1 = new OwnerData();
      od1.addAsset("OID1", "AID1");
      od1.addAsset("OID2", "AID2");
      od1.addAsset("OID1", "AID3");
      od1.addAsset("OID2", "AID4");

      od1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new OwnerDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void testPrototypes() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      PrototypesData pd1 = new PrototypesData();
      Prototype p1 = new Prototype();
      p1.UID="UID3";
      p1.assetClass=Prototype.ASSET_CLASS_1;
      p1.assetType=Prototype.ASSET_TYPE_ASSET;
      p1.weight=2;
      p1.width=3;
      p1.height=4;
      p1.depth=5;
      p1.alpTypeID="NSN/asdf";
      p1.nomenclature="Box of nails";
      pd1.addPrototype(p1);

      pd1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new PrototypesDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void testInstances() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      InstancesData id1 = new InstancesData();
      Instance i1 = new Instance();
      i1.UID="UID1";
      i1.aggregateNumber=4;
      i1.prototypeUID="UID2";
      i1.addContent("UID12");
      i1.addContent("UID32");
      id1.addInstance(i1);
      Prototype p1 = new Prototype();
      p1.UID="UID2";
      p1.parentUID="UID10";
      p1.assetClass=Prototype.ASSET_CLASS_2;
      p1.assetType=Prototype.ASSET_TYPE_CONTAINER;
      p1.weight=400;
      p1.width=10;
      p1.height=10;
      p1.depth=40;
      p1.alpTypeID="NSN/zxcv";
      p1.nomenclature="40ft Container";
      id1.addPrototype(p1);

      id1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new InstancesDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void testLegs() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      LegsData ld1 = new LegsData();
      Leg l1 = new Leg();
      l1.UID="UID1";
      l1.startDate=123121;
      l1.endDate=2134123;
      l1.startLoc="gsds";
      l1.endLoc="fdss";
      l1.legType=Leg.LEG_TYPE_TRANSPORTING;
      l1.conveyanceUID="UID35463";
      l1.addCarriedAsset("UID234");

      ld1.addLeg(l1);

      ld1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new LegsDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void testLocations() throws IOException{
      XMLWriter stdOut = new XMLWriter(new BufferedWriter
	(new OutputStreamWriter(System.out)), true);

      StringWriter strOut = new StringWriter();
      XMLWriter out = new XMLWriter(strOut);
      out.writeHeader();
      
      LocationsData ld1 = new LocationsData();
      Location l1 = new Location();
      l1.UID="UID456";
      l1.lat=3.34;
      l1.lon=5.43;
      l1.geoLoc="sfsd";
      l1.icao="3453534";
      l1.prettyName="Boston";
      ld1.addLocation(l1);

      ld1.toXML(out);
      out.flush();
      out.close();

      /*
      stdOut.write("\nObj->XML:\n");
      stdOut.write(strOut.getBuffer().toString());
      stdOut.flush();
      */

      DeXMLizer dx = new DeXMLizer(new LocationsDataFactory());

      Reader strIn = new StringReader(strOut.getBuffer().toString());
      DeXMLable obj = dx.parseObject(strIn);

      stdOut.write("\nObj->XML->OBJ->XML:\n");
      
      ((XMLable)obj).toXML(stdOut);

      stdOut.flush();
  }

  public static void main(String s[]) {
    try{
      testHierarchy();
      testPopulation();
      testOwner();
      testPrototypes();
      testInstances();
      testLegs();
      testLocations();
    }catch(Exception e){
      System.err.println(e);
    }
  }
}
