/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer.stoplight;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class EquipmentInfo {
  Hashtable items;
  Vector subordinates; // equipment at the bottom of hierarchy
  public static final String TOPEQUIPMENT = "Category";

  // for equipment to be considered to be in the society
  // it's nomenclature (from nomenclatures)
  // has to be listed here with it's superior

  static final String[][] superiors = {
    { "Category", "EquipmentHigherAuthority" },
    { "Class III", "Category" },
    { "DF2", "Class III" },
    { "DFM", "Class III" },
    { "JP5", "Class III" },
    { "JP8", "Class III" },
    { "MUG", "Class III" },
    { "MG1", "Class III" },
    { "F76", "Class III" },

    { "People", "Category" },
    { "Passengers", "People" },
    { "MOS/27E", "People" },
    { "MOS/35D", "People" },
    { "MOS/35E", "People" },
    { "MOS/35F", "People" },
    { "MOS/44B", "People" },
    { "MOS/44E", "People" },
    { "MOS/45B", "People" },
    { "MOS/45G", "People" },
    { "MOS/45K", "People" },
    { "MOS/52C", "People" },
    { "MOS/52D", "People" },
    { "MOS/63H", "People" },
    { "MOS/63J", "People" },
    { "MOS/63W", "People" },

    { "Class VII", "Category" },
    { "M198 Howitzer Towed 155MM", "Class VII" },
    { "M270 Multiple Launch Rocket System", "Class VII" },
    { "M270 Multiple Launch Rocket System", "Class VII" },
    { "M270 Multiple Launch Rocket System", "Class VII" },
    { "M270 Multiple Launch Rocket System", "Class VII" },
    { "M270 Multiple Launch Rocket System", "Class VII" },
    { "UH-60A Utility Helicopter", "Class VII" },
    { "EH-60A Intellience & Electronics Helicopter", "Class VII" },
    { "AH-64A Apache Attack Helicopter", "Class VII" },
    { "OH-58D Aerial Scout Helicopter", "Class VII" },
    { "M978 WWN HEMTT Truck Tank Fuel 8x8", "Class VII" },
    { "M978 WOWN Truck Tank 2500 Gallons", "Class VII" },
    { "M985 (HMMWV) Truck, Cargo, Tactical, HE", "Class VII" },
    { "M998 (HMMWV) Truck, Utility, Cargo/Troop Carrier", "Class VII" },
    { "M1038 W/W (HMMWV) Truck, Utility, Cargo/Troop Carrier", "Class VII" },
    { "Light Armored Vehicle", "Class VII" },
    { "Light Armored Vehicle Cmd & Control", "Class VII" },
    { "Light Armored Vehicle Mortar", "Class VII" },
    { "Light Armored Vehicle Maintenance", "Class VII" },
    { "Light Armored Vehicle Anti-Tank", "Class VII" },
    { "Light Armored Vehicle Logistics", "Class VII" },
    { "M1026 HMMWV Truck Utility 1-1/4 Ton", "Class VII" },
    { "High Mobility Multipurpose Wheeled Vehicle(HMMWV)", "Class VII" },
    { "High Mobility Multipurpose Wheeled Vehicle(HMMWV)", "Class VII" },
    { "M1075 Truck, Cargo, Heavy, PLS Transport", "Class VII" },
    { "M1070 Truck, Tractor, HET", "Class VII" },
    { "Truck Cargo 5 Ton", "Class VII" },
    { "M1076 Trailer, PLS 16 1/2 Ton", "Class VII" },
    { "M1000 Semitrailer, Low Bed 70 Ton", "Class VII" },
    { "M88A1 Recovery Vehicle Full Track", "Class VII" },
    { "M577A2 Carrier, Command Post, Full Track", "Class VII" },
    { "Landing Vehicle Tracked Cmd & Control", "Class VII" },
    { "Landing Vehicle Tracked Recovery", "Class VII" },
    { "Landing Vehicle Tracked Personnel", "Class VII" },
    { "M981 Carrier, Personnel, Full Track", "Class VII" },
    { "M1A1 120 MM Tank Combat Full Track", "Class VII" },
    { "M113A3 Personnel Carrier", "Class VII" },
    { "M2A2 Fighting Vehicle HS", "Class VII" },
    { "M3A2 Fighting Vehicle HS", "Class VII" },
    { "M109A6 Howitzer, Medium, Self-Propelled", "Class VII" },
    { "M2A2 W/ODS Fighting Vehicle HS", "Class VII" },
    { "M1077 Bed, Cargo, Demountable Flatrack", "Class VII" },
    { "Class V", "Category" },
    { "120MM APFSDS-T M829A1", "Class V" },
    { "HELLFIRE, AGM-114F", "Class V" },
    { "GREN IR M76", "Class V" },
    { "2.75IN SMOKE M259", "Class V" },
    { "2.75IN HE M151", "Class V" },
    { "30MM HE DP", "Class V" },
    { "155MM ICM M483A1 2ND", "Class V" },
    { "CTG CAL .50 M8", "Class V" },
    { "7.62MM 4/1 TRCR LKD", "Class V" },
    { "25MM HEI-T M792", "Class V" },
    { "155MM RAAMS M741A1", "Class V" },
    { "PROP CHARGE M4A2", "Class V" },
    { "155MM RAP M549 SERIES", "Class V" },
    { "SMOKE SCREEN GRENADE", "Class V" },
    { "25MM APFSDS-T M919", "Class V" },
    { "120MM HEAT-MP-T M830", "Class V" },
    { "155MM ADAM M731", "Class V" },
    { "155MM ILLUM M485 SERI", "Class V" },
    { "155MM SMOKE SCR M825", "Class V" },
    { "PROP CHARGE M119A2", "Class V" },
    { "PROP CHARGE M3A1", "Class V" },
    { "2.75IN ILL M257", "Class V" },
    { "TOW, BGM-71F", "Class V" },
    { "FUZE MT", "Class V" },
    { "ARBN_M1A1_120_MM_TANK_COMBAT", "Class VII" },
    { "Class IX", "Category" },
    { "Weapons", "Class IX" },
    { "Nuclear Ordnance", "Class IX" },
    { "Fire Control", "Class IX" },
    { "Explosives", "Class IX" },
    { "Missiles", "Class IX" },
    { "Airframe Components", "Class IX" },
    { "Aircraft Components", "Class IX" },
    { "Aircraft Handling", "Class IX" },
    { "Space Vehicles", "Class IX" },
    { "Ships", "Class IX" },
    { "Marine Equipment", "Class IX" },
    { "Railway Equipment", "Class IX" },
    { "Ground Vehicles", "Class IX" },
    { "Tractors", "Class IX" },
    { "Vehicular Equipment", "Class IX" },
    { "Tires and Tubes", "Class IX" },
    { "Engines", "Class IX" },
    { "Engine Accessories", "Class IX" },
    { "Power Transmission", "Class IX" },
    { "Bearings", "Class IX" },
    { "Woodworking", "Class IX" },
    { "Metalworking", "Class IX" },
    { "Service Equipment", "Class IX" },
    { "Special Industry Machinery", "Class IX" },
    { "Agricultural", "Class IX" },
    { "Construction", "Class IX" },
    { "Materials Handling", "Class IX" },
    { "Rope", "Class IX" },
    { "Refrigeration", "Class IX" },
    { "Fire Fighting", "Class IX" },
    { "Pumps", "Class IX" },
    { "Furnace Equipment", "Class IX" },
    { "Plumbing", "Class IX" },
    { "Water and Sewage", "Class IX" },
    { "Pipe", "Class IX" },
    { "Valves", "Class IX" },
    { "Maintenance Equipment", "Class IX" },
    { "Hand Tools", "Class IX" },
    { "Measuring Tools", "Class IX" },
    { "Hardware", "Class IX" },
    { "Prefabricated Structures", "Class IX" },
    { "Lumber", "Class IX" },
    { "Construction Materials", "Class IX" },
    { "Communication", "Class IX" },
    { "Electrical", "Class IX" },
    { "Fiber Optics", "Class IX" },
    { "Electric", "Class IX" },
    { "Lighting", "Class IX" },
    { "Alarms", "Class IX" },
    { "Medical", "Class IX" },
    { "Laboratory Equipment", "Class IX" },
    { "Photographic", "Class IX" },
    { "Chemicals", "Class IX" },
    { "Training Aids", "Class IX" },
    { "ADP Equipment", "Class IX" },
    { "Furniture", "Class IX" },
    { "Household", "Class IX" },
    { "Food Preparation", "Class IX" },
    { "Office Machines", "Class IX" },
    { "Office Supplies", "Class IX" },
    { "Books", "Class IX" },
    { "Musical Instruments", "Class IX" },
    { "Recreational", "Class IX" },
    { "Cleaning Equipment", "Class IX" },
    { "Brushes, Paints", "Class IX" },
    { "TEUs", "Class IX" },
    { "Textiles", "Class IX" },
    { "Clothing", "Class IX" },
    { "Toiletries", "Class IX" },
    { "Agricultural Supplies", "Class IX" },
    { "Live Animals", "Class IX" },
    { "Subsistence", "Class IX" },
    { "Lubricants", "Class IX" },
    { "Fabricated Materials", "Class IX" },
    { "Crude Materials", "Class IX" },
    { "Metal Shapes", "Class IX" },
    { "Ores", "Class IX" },
    { "Miscellaneous", "Class IX"},
    { "NSN/10", "Weapons" },
    { "NSN/11", "Nuclear Ordnance" },
    { "NSN/12", "Fire Control" },
    { "NSN/13", "Explosives" },
    { "NSN/14", "Missiles" },
    { "NSN/15", "Airframe Components" },
    { "NSN/16", "Aircraft Components" },
    { "NSN/17", "Aircraft Handling" },
    { "NSN/18", "Space Vehicles" },
    { "NSN/19", "Ships" },
    { "NSN/20", "Marine Equipment" },
    { "NSN/22", "Railway Equipment" },
    { "NSN/23", "Ground Vehicles" },
    { "NSN/24", "Tractors" },
    { "NSN/25", "Vehicular Equipment" },
    { "NSN/26", "Tires and Tubes" },
    { "NSN/28", "Engines" },
    { "NSN/29", "Engine Accessories" },
    { "NSN/30", "Power Transmission" },
    { "NSN/31", "Bearings" },
    { "NSN/32", "Woodworking" },
    { "NSN/34", "Metalworking" },
    { "NSN/35", "Service Equipment" },
    { "NSN/36", "Special Industry Machinery" },
    { "NSN/37", "Agricultural" },
    { "NSN/38", "Construction" },
    { "NSN/39", "Materials Handling" },
    { "NSN/40", "Rope" },
    { "NSN/41", "Refrigeration" },
    { "NSN/42", "Fire Fighting" },
    { "NSN/43", "Pumps" },
    { "NSN/44", "Furnace Equipment" },
    { "NSN/45", "Plumbing" },
    { "NSN/46", "Water and Sewage" },
    { "NSN/47", "Pipe" },
    { "NSN/48", "Valves" },
    { "NSN/49", "Maintenance Equipment" },
    { "NSN/51", "Hand Tools" },
    { "NSN/52", "Measuring Tools" },
    { "NSN/53", "Hardware" },
    { "NSN/54", "Prefabricated Structures" },
    { "NSN/55", "Lumber" },
    { "NSN/56", "Construction Materials" },
    { "NSN/58", "Communication" },
    { "NSN/59", "Electrical" },
    { "NSN/60", "Fiber Optics" },
    { "NSN/61", "Electric" },
    { "NSN/62", "Lighting" },
    { "NSN/63", "Alarms" },
    { "NSN/65", "Medical" },
    { "NSN/66", "Laboratory Equipment" },
    { "NSN/67", "Photographic" },
    { "NSN/68", "Chemicals" },
    { "NSN/69", "Training Aids" },
    { "NSN/70", "ADP Equipment" },
    { "NSN/71", "Furniture" },
    { "NSN/72", "Household" },
    { "NSN/73", "Food Preparation" },
    { "NSN/74", "Office Machines" },
    { "NSN/75", "Office Supplies" },
    { "NSN/76", "Books" },
    { "NSN/77", "Musical Instruments" },
    { "NSN/78", "Recreational" },
    { "NSN/79", "Cleaning Equipment" },
    { "NSN/80", "Brushes, Paints" },
    { "NSN/81", "TEUs" },
    { "NSN/83", "Textiles" },
    { "NSN/84", "Clothing" },
    { "NSN/85", "Toiletries" },
    { "NSN/87", "Agricultural Supplies" },
    { "NSN/88", "Live Animals" },
    { "NSN/89", "Subsistence" },
    { "NSN/91", "Lubricants" },
    { "NSN/93", "Fabricated Materials" },
    { "NSN/94", "Crude Materials" },
    { "NSN/95", "Metal Shapes" },
    { "NSN/96", "Ores" },
    { "NSN/99", "Miscellaneous" }
  };

  // DODICs and NSNs should be included here with their nomenclatures

  static final String[][] nomenclatures = {
    { "DODIC/C380", "120MM APFSDS-T M829A1" },
    { "DODIC/PV29", "HELLFIRE, AGM-114F" },
    { "DODIC/G826", "GREN IR M76" },
    { "DODIC/H116", "2.75IN SMOKE M259" },
    { "DODIC/H163", "2.75IN HE M151" },
    { "DODIC/H164", "2.75IN HE M151" },
    { "DODIC/B129", "30MM HE DP" },
    { "DODIC/D563", "155MM ICM M483A1 2ND" },
    { "DODIC/A576", "CTG CAL .50 M8" },
    { "DODIC/A131", "7.62MM 4/1 TRCR LKD" },
    { "DODIC/A975", "25MM HEI-T M792" },
    { "DODIC/D514", "155MM RAAMS M741A1" },
    { "DODIC/D541", "PROP CHARGE M4A2" },
    { "DODIC/D579", "155MM RAP M549 SERIES" },
    { "DODIC/G815", "SMOKE SCREEN GRENADE" },
    { "DODIC/A986", "25MM APFSDS-T M919" },
    { "DODIC/C787", "120MM HEAT-MP-T M830" },
    { "DODIC/D502", "155MM ADAM M731" },
    { "DODIC/D505", "155MM ILLUM M485 SERI" },
    { "DODIC/D528", "155MM SMOKE SCR M825" },
    { "DODIC/D533", "PROP CHARGE M119A2" },
    { "DODIC/D540", "PROP CHARGE M3A1" },
    { "DODIC/H183", "2.75IN ILL M257" },
    { "DODIC/PV18", "TOW, BGM-71F" },
    { "DODIC/N285", "FUZE MT" },
    { "NSN/9130001601818", "MG1" },
    { "NSN/9130123232506", "MUG" },
    { "NSN/9130010315816", "JP8" },
    { "NSN/9140002865294", "DF2" },
    { "NSN/9140002732377", "F76" },
    { "NSN/9130002732379", "JP5" },
    { "NSN/1025010266648", "M198 Howitzer Towed 155MM" },
    { "NSN/1055011920357", "M270 Multiple Launch Rocket System" },
    { "NSN/1055011920358", "M270 Multiple Launch Rocket System" },
    { "NSN/1055010920596", "M270 Multiple Launch Rocket System" },
    { "NSN/1055012519756", "M270 Multiple Launch Rocket System" },
    { "NSN/1055013296826", "M270 Multiple Launch Rocket System" },
    { "NSN/1520010350266", "UH-60A Utility Helicopter" },
    { "NSN/1520010820686", "EH-60A Intellience & Electronics Helicopter" },
    { "NSN/1520011069519", "AH-64A Apache Attack Helicopter" },
    { "NSN/1520011255476", "OH-58D Aerial Scout Helicopter" },
    { "NSN/2320010970249", "M978 WWN HEMTT Truck Tank Fuel 8x8" },
    { "NSN/2320011007672", "M978 WOWN Truck Tank 2500 Gallons" },
    { "NSN/2320011007673", "M985 (HMMWV) Truck, Cargo, Tactical, HE" },
    { "NSN/2320011077155", "M998 (HMMWV) Truck, Utility, Cargo/Troop Carrier" },
    { "NSN/2320011077156", "M1038 W/W (HMMWV) Truck, Utility, Cargo/Troop Carrier" },
    { "NSN/2320011231602", "Light Armored Vehicle" },
    { "NSN/2320011231606", "Light Armored Vehicle Cmd & Control" },
    { "NSN/2320011231607", "Light Armored Vehicle Mortar" },
    { "NSN/2320011231608", "Light Armored Vehicle Maintenance" },
    { "NSN/2320011231609", "Light Armored Vehicle Anti-Tank" },
    { "NSN/2320011231612", "Light Armored Vehicle Logistics" },
    { "NSN/2320011289552", "M1026 HMMWV Truck Utility 1-1/4 Ton" },
    { "NSN/2320011467189", "High Mobility Multipurpose Wheeled Vehicle(HMMWV)" },
    { "NSN/2320011467190", "High Mobility Multipurpose Wheeled Vehicle(HMMWV)" },
    { "NSN/2320013042278", "M1075 Truck, Cargo, Heavy, PLS Transport" },
    { "NSN/2320013189902", "M1070 Truck, Tractor, HET" },
    { "NSN/2320013334129", "Truck Cargo 5 Ton" },
    { "NSN/2330013035197", "M1076 Trailer, PLS 16 1/2 Ton" },
    { "NSN/2330013038832", "M1000 Semitrailer, Low Bed 70 Ton" },
    { "NSN/2350001226826", "M88A1 Recovery Vehicle Full Track" },
    { "NSN/2350010684089", "M577A2 Carrier, Command Post, Full Track" },
    { "NSN/2350010809087", "Landing Vehicle Tracked Cmd & Control" },
    { "NSN/2350010809088", "Landing Vehicle Tracked Recovery" },
    { "NSN/2350010818138", "Landing Vehicle Tracked Personnel" },
    { "NSN/2350010853792", "M981 Carrier, Personnel, Full Track" },
    { "NSN/2350010871095", "M1A1 120 MM Tank Combat Full Track" },
    { "NSN/2350012197577", "M113A3 Personnel Carrier" },
    { "NSN/2350012487619", "M2A2 Fighting Vehicle HS" },
    { "NSN/2350012487620", "M3A2 Fighting Vehicle HS" },
    { "NSN/2350013050028", "M109A6 Howitzer, Medium, Self-Propelled" },
    { "NSN/2350014059886", "M2A2 W/ODS Fighting Vehicle HS" },
    { "NSN/3990013077676", "M1077 Bed, Cargo, Demountable Flatrack" },
    { "PAX/GenericPersonnel", "Passengers" }
  };


  /** Create a default society based on all known items
   */

  public EquipmentInfo() {
    items = new Hashtable();
    setSubordinates();
  }

  private void setSubordinates() {
    subordinates = new Vector();
    for (int i = 0; i < superiors.length; i++) {
      String s = superiors[i][0];
      boolean isSubordinate = true;
      for (int j = 0; j < superiors.length; j++)
        if (superiors[j][1].equals(s)) {
          isSubordinate = false;
          break;
        }
      if (isSubordinate)
        subordinates.addElement(s);
    }
  }

  public void createDefaultEquipmentInfo() {
    setSubordinates();
    for (int i = 0; i < subordinates.size(); i++)
      addEquipment((String)subordinates.elementAt(i), 
                   (String)subordinates.elementAt(i), 0);
  }

  /** Create a society with the specified subordinate items, i.e. items
    that are the leaves of the society and have no subordinates.
   */

  //  public EquipmentInfo(Vector itemIds) {
  //    Vector itemNames = new Vector(itemIds.size());
  //    for (int i = 0; i < itemIds.size(); i++) {
  //      String nomenclature = itemIdToNomenclature((String)itemIds.elementAt(i));
  //      if (nomenclature == null)
  //    System.out.println("No nomenclature for: " + itemIds.elementAt(i));
  //      else {
  //    System.out.println("Nomenclature is:" + nomenclature);
  //    itemNames.addElement(nomenclature);
  //      }
  //    }
  //    items = new Hashtable();
  //    for (int i = 0; i < itemNames.size(); i++)
  //      addEquipment((String)itemNames.elementAt(i), 0);
  //  }

  public String itemIdToNomenclature(String itemId) {
    //    System.out.println("Looking up item id:" + itemId);
    for (int i = 0; i < nomenclatures.length; i++) {
      if (itemId.equals(nomenclatures[i][0])) {
        //      System.out.println("Returning: " + nomenclatures[i][1]);
        return nomenclatures[i][1];
      }
    }
    return null;
  }

  public String nameToNomenclature(String name) {
    Enumeration e = getAllEquipment();
    while (e.hasMoreElements()) {
      Equipment equipment = (Equipment)e.nextElement();
      if (equipment.getName().equals(name))
        return equipment.getNomenclature();
    }
    return "";
  }

  public String getSuperiorName(String name) {
    if (name.startsWith("NSN/"))
      name = name.substring(0, 6);
    for (int i = 0; i < superiors.length; i++)
      if (superiors[i][0].equals(name))
        return superiors[i][1];
    return null;
  }

  public void addEquipment(String name, String nomenclature,
                           int equipmentLevel) {
    Equipment equipment = (Equipment)items.get(name);
    if (equipment == null) {
      equipment = new Equipment(name, nomenclature);
      equipment.setLevel(equipmentLevel);
      String superiorName = getSuperiorName(name);
      if (superiorName == null)
        return;
      if (superiorName.length() == 0) // society
        equipment.setSuperiorName(null);
      else
        equipment.setSuperiorName(superiorName);
      Equipment superiorEquipment = (Equipment)items.get(superiorName);
      if (superiorEquipment == null) {
        equipmentLevel++;
        addEquipment(superiorName, superiorName, equipmentLevel); // recursive
        superiorEquipment = (Equipment)items.get(superiorName);
        if (superiorEquipment != null)
          superiorEquipment.addSubordinateName(name);
      } else
        superiorEquipment.addSubordinateName(name);
    }
    items.put(name, equipment);
  }

  public Equipment getEquipment(String equipmentName) {
    if (items.get(equipmentName) == null) {
      // check if user is asking by nomenclature
      Enumeration e = getAllEquipment();
      while (e.hasMoreElements()) {
        Equipment equipment = (Equipment)e.nextElement();
        if (equipment.getNomenclature().equals(equipmentName))
          return equipment;
      }
      System.out.println("WARNING: NO EQUIPMENT FOR: " + equipmentName);
    }
    return (Equipment)items.get(equipmentName);
  }

  public Enumeration getAllEquipment() {
    return items.elements();
  }

  public Vector getEquipmentAtLevel(int level) {
    Vector results = new Vector();
    Enumeration e = getAllEquipment();
    while (e.hasMoreElements()) {
      Equipment equipment = (Equipment)e.nextElement();
      if (equipment.getLevel() == level)
        results.addElement(equipment);
    }
    return results;
  }

  /** Returns true if the equipment is in the default items list; 
    i.e. it has no sub-classes of equipment.
    For equipment which starts with the term "NSN",
    if the next 2 characters match a known FSG, then
    return true.
    */

  public boolean isDefaultItem(String equipmentName) {
    if (equipmentName.startsWith("NSN/"))
      equipmentName = equipmentName.substring(0, 6);
    return subordinates.contains(equipmentName);
  }

  public void printEquipmentInfo() {
    Enumeration e = items.elements();
    while (e.hasMoreElements()) {
      Equipment equipment = (Equipment)e.nextElement();
      System.out.println(equipment.toString());
    }
  }

  public static void main(String[] args) {
    EquipmentInfo equipmentInfo = new EquipmentInfo();
    equipmentInfo.createDefaultEquipmentInfo();
    Enumeration e = equipmentInfo.items.elements();
    while (e.hasMoreElements()) {
      Equipment equipment = (Equipment)e.nextElement();
      System.out.println(equipment.toString());
    }
  }
}


