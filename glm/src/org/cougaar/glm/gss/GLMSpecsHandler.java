// $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/glm/gss/Attic/GLMSpecsHandler.java,v 1.1 2001-12-27 22:41:41 bdepass Exp $
/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
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

package org.cougaar.glm.gss;

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









