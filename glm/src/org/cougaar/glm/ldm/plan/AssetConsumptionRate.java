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


/* original contributed by ALPIcis group, BBN Technologies/GTE */

package org.cougaar.glm.ldm.plan;
import java.io.Serializable;
import java.util.Enumeration;

import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.measure.Rate;

/** Representation of Consumption Rate structure for a particular Asset.
 * May also be used as Failure rate for 
 */
public interface AssetConsumptionRate extends Serializable {
  /** 
   * @return Enumeration of Assets which have failure/consumption rates.
   **/
  Enumeration getAssets();

  /** The actual Failure/Consumption rate for a given Asset
   *
   * @param asset to be consumed or repaired
   * @param optempo current rate of use
   * @return failure/consumption rate of the part at the given optempo, 
   * in terms of Unit of Issue (usually Each).
   **/
  Rate getRate(Asset asset, String optempo);
}
