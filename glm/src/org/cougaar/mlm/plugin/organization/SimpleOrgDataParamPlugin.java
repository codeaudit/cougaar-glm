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

package org.cougaar.mlm.plugin.organization;

import org.cougaar.planning.plugin.asset.AssetDataParamReader;
import org.cougaar.planning.plugin.asset.AssetDataReader;

/**
 * Extension of SimpleOrgDataPlugin which always uses the 
 * ParameterizedDataReader to construct the local org asset and
 * the Report tasks associated with all the local asset's relationships. Local
 * asset must have ClusterPG and RelationshipPG, Presumption is that the 
 * 'other' assets in all the relationships have both Cluster and Relationship
 * PGs. Currently assumes that each Cluster has exactly 1 local asset.
 *
 */
public class SimpleOrgDataParamPlugin extends SimpleOrgDataPlugin {

  protected AssetDataReader getAssetDataReader() {
    return new AssetDataParamReader(getDelegate().getParameters(), myAssetInitializerService);
  }
}




