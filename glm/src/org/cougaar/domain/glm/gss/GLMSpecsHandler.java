// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/glm/gss/Attic/GLMSpecsHandler.java,v 1.3 2001-05-25 16:48:47 rwu Exp $
/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.domain.glm.gss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

import org.cougaar.lib.gss.*;

/**
 * The class that handles parsing the XML data.
 *
 * Copyright (C) 1999 BBN Technologies
 */

public class GLMSpecsHandler extends GSSpecsHandler {

  /**
   * Overridden from base class.
   * Creates new object of specified type and uses attributes to
   * get arguments to pass to the constructor.
   * Then push the object on the stack.
   *
   * @param name the type of object
   * @param atts the arguments passed to the constructor
   */
  // for modern Xerces jar
  public void startElement (String uri, String local, String name, Attributes atts) throws SAXException {
  // for old (June 1999) IBM XML jar
  // public void startElement (String name, AttributeList atts) {

    if (name.equals ("locationAccessor"))
      addObject (new GLMLocationAccessor (atts.getValue ("representation")));
    else if (name.equals ("taskAccessor"))
      addObject (new GLMTaskAccessorImpl (atts.getValue ("preposition")));
    else if (name.equals ("travelTime"))
      addObject (new GLMTravelTime (atts.getValue ("extraFactor")));
    else if (name.equals ("taskClassMatch"))
      addObject (new GLMTaskClassMatch (atts.getValue ("preposition")));

    else
      super.startElement(uri, local, name, atts);
  }
}









