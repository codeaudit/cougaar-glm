/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

/**
 * All the static info for the XML user interface is here, 
 * so it can be easily configured.
 */

package org.cougaar.mlm.ui.planviewer;

public class XMLClientConfiguration {

  public static final String debuggerTitle = "COUGAAR Debugger";

  public static final String uiDataProviderTitle = "COUGAAR UI Data Provider";

  public static final String[] planObjectNames =
        { "PlanElement", "Task", "Asset", "Policy", "Aggregation", 
	  "Allocation", "Disposition",
	  "AssetTransfer", "Expansion", "all" };

  public static final String[] UIPlanElementNames = { "" };

  public static final String[] planElementNames = { "UID.UID" };

  public static final String[] UIAggregationNames = { "" };

  public static final String[] aggregationNames = { "" };

  public static final String[] UIAllocationNames = {
    "UIAsset.itemIdentification",
    "UIAsset.itemIdentificationPropertyNomenclature",
    "UIAsset.typeIdentification",
    "UIAsset.typeIdentificationPropertyNomenclature",
    "planName",
    "UITask.UUID",
    "UUID"
  };

  public static final String[] failedAllocationNames = { "" };

  public static final String[] UIFailedDispositionNames = { "" };

  public static final String[] allocationNames = {
    "asset.ItemIdentificationPG.item_identification",
    "asset.ItemIdentificationPG.nomenclature",
    "asset.TypeIdentificationPG.nomenclature",
    "asset.TypeIdentificationPG.type_identification",
    "plan.planName",
    "task.ID",
    "UID.UID"
  };

  public static final String[] UIAssetTransferNames = {
    "UIAsset.itemIdentification",
    "UIAsset.itemIdentificationPropertyNomenclature",
    "UIAsset.typeIdentification",
    "UIAsset.typeIdentificationPropertyNomenclature",
    "UIAsset.itemIdentification",
    "UIAsset.itemIdentificationPropertyNomenclature",
    "UIAsset.typeIdentification",
    "UIAsset.typeIdentificationPropertyNomenclature",
    "assignor",
    "planName",
    "UIScheduleElement.endDate",
    "UIScheduleElement.startDate",
    "UUID"
  };

  public static final String[] assetTransferNames = {
    "asset.ItemIdentificationPG.item_identification",
    "asset.ItemIdentificationPG.nomenclature",
    "asset.TypeIdentificationPG.nomenclature",
    "asset.TypeIdentificationPG.type_identification",
    "assignee.ItemIdentificationPG.item_identification",
    "assignee.ItemIdentificationPG.nomenclature",
    "assignee.TypeIdentificationPG.nomenclature",
    "assignee.TypeIdentificationPG.type_identification",
    "assignor.address",
    "plan.planName",
    "schedule.endDate.date",
    "schedule.endDate.month",
    "schedule.endDate.year",
    "schedule.startDate.date",
    "schedule.startDate.month",
    "schedule.startDate.year",
    "UID.UID"
  };

  public static final String[] UIExpansionNames = {
    "planName",
    "UITask.UUID",
    "UUID" };

  public static final String[] expansionNames = {
    "plan.planName",
    "task.ID",
    "UID.UID" };

  public static final String [] UITaskNames = {
    "destination",
    "directObject.itemIdentification",
    "directObject.itemIdentificationPropertyNomenclature",
    "directObject.typeIdentification",
    "directObject.typeIdentificationPropertyNomenclature",
    "UUID",
    "parentTaskUUID",
    "planName",
    "prepositionalPhrase.preposition",
    "source",
    "verb",
    "workflowUUID"};

  public static final String [] taskNames = {
    "destination.address", 
    "directObject.ItemIdentificationPG.item_identification",
    "directObject.ItemIdentificationPG.nomenclature",
    "directObject.TypeIdentificationPG.nomenclature",
    "directObject.TypeIdentificationPG.type_identification",
    "ID",
    "parentTask",
    "plan.planName",
    "prepositionalPhrases.preposition",
    "prepositionalPhrases.indirectObject.clusterIdentifier.address",
    "source.address",
    "verb" };

  public static final String [] UIAssetNames = {
    "allocationID",
    "itemIdentification",
    "itemIdentificationPropertyNomenclature",
    "typeIdentificationPropertyNomenclature",
    "typeIdentification",
    "UUID"
  };

  public static final String [] assetNames = {
    "ItemIdentificationPG.item_identification",
    "ItemIdentificationPG.nomenclature",
    "TypeIdentificationPG.nomenclature",
    "TypeIdentificationPG.type_identification",
    "UID.UID"
  };

  public static final String [] UIPolicyNames = {
    "policyName",
    "policyParameter.name",
    "policyParameter.value",
    "UUID"
  };

  public static final String [] policyNames = {
    "name",
    "UID.UID"
  };

  public static final String [] allNames = {""};

  public static final String[][] planObjectFieldNames = {
    planElementNames, taskNames, assetNames, policyNames, aggregationNames, 
    allocationNames, failedAllocationNames,
    assetTransferNames, expansionNames, allNames };

  public static final String[][] UIPlanObjectFieldNames = {
    UIPlanElementNames, UITaskNames, UIAssetNames, UIPolicyNames, 
    UIAggregationNames, 
    UIAllocationNames, 
    UIFailedDispositionNames,
    UIAssetTransferNames, UIExpansionNames, allNames };

  // the package and id are used in the URL specified by the client
  // to connect to the PSP

  public static final String PSP_package = "alpine/demo";

  public static final String DebugPSP_id = "DEBUG.PSP";

  public static final String UIDataPSP_id = "UIDATA.PSP";
}
