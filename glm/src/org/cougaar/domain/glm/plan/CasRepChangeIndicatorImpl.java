/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.plan;


import org.cougaar.domain.glm.plan.CasRep;
import org.cougaar.domain.glm.plan.NewCasRep;

import org.cougaar.core.society.UID;

import org.cougaar.util.XMLizable;

import java.lang.String;
import java.util.Vector;
import java.util.Date;
import java.io.*;
import java.io.Serializable;
import java.util.Enumeration;
import org.cougaar.domain.planning.ldm.plan.Transferable;
import org.cougaar.util.XMLizable;
import org.cougaar.domain.planning.ldm.asset.*;
 
import org.cougaar.util.XMLize;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

public class CasRepChangeIndicatorImpl extends CasRepImpl
{
    public CasRepChangeIndicatorImpl()
	{
	}// CasRepChangeIndicatorImpl Constructor
}//CasRepChangeIndicator
