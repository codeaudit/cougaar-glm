/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.mlm.plugin.ldm;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.asset.NewTypeIdentificationPG;

/** A QueryHandler which can convert a TypeIdentification code
 * into an Asset Prototype.
 **/

public class AssetPropertyProvider 
	extends PropertyProvider 
{
  public AssetPropertyProvider() {}

  public void provideProperties(Asset asset) {
    try {
      NewTypeIdentificationPG p1 = (NewTypeIdentificationPG)asset.getTypeIdentificationPG();
      String nomen = NSNtoNomenclature(p1.getTypeIdentification());
      p1.setNomenclature(nomen);
    } catch (Exception ee) {
      System.out.println(ee.toString());
      ee.printStackTrace();
    }
  }

  private String NSNtoNomenclature(String nsn) {
    // hel
    if (nsn.equals("1520011069519"))// hel
      return "Helicopter";

    if (nsn.equals("2350010871095")) // tank
      return "M1A1 Tank";

    if (nsn.equals("2350013050028")) // hwtz
      return " Howitzer";
    
    return "Recovery Vehicle";
  }

  // fake query
  public String getQuery() { return null; }
  public void processRow(Object[] row) { return; }

}
