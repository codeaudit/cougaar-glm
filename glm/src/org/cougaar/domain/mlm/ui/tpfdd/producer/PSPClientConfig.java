/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/producer/Attic/PSPClientConfig.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

/*
  All the static info for the XML user interface is here, 
  so it can be easily configured.
*/


package org.cougaar.domain.mlm.ui.tpfdd.producer;


public class PSPClientConfig
{
    // the package and id are used in the URL specified by the client
    // to connect to the PSP

    public static final String PSP_package = "alpine/demo";

    public static final String PSP_id = "DEBUG.PSP";

    public static final String UIDataPSP_id = "UIDATA.PSP";

    public static final String ItineraryPSP_id = "ITINERARY.PSP";

    public static final String SubordinatesPSP_id = "SUBORDINATES.PSP";

    public static final String AssetPopulationPSP_id = "ASSET_POPULATION.PSP";
}
