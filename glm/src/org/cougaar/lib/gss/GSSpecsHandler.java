/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.gss;

// old IBM XML references
// import org.xml.sax.HandlerBase;
// import org.xml.sax.AttributeList;

// modern xerces references
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * The class that handles parsing the XML data.
 *
 */
// old IBM XML jar
// public class GSSpecsHandler extends HandlerBase {
// new xerces jar
public class GSSpecsHandler extends DefaultHandler {

  private String className;
  private java.util.Stack objectStack = new java.util.Stack();

  /** push new object on stack */
  protected final void addObject (Object obj) {
    objectStack.push (obj);
  }

  /**
   * Get the scheduler object after done parsing
   */
  public GSScheduler getScheduler() {
    return new GSScheduler ((GSSchedulingSpecs) objectStack.pop(), debugables);
  }

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
  //public void startElement (String name, AttributeList atts) {

    if (name.equals ("taskFilter"))
      addObject (new GSTaskFilter (atts.getValue ("verb"),
                                   atts.getValue ("generatingCluster")));

    else if (name.equals ("genericBoolean"))
      addObject (new GSBooleanImpl (atts.getValue ("operation")));

    else if (name.equals ("assetFilter"))
      addObject (new GSAssetFilter ());

    else if (name.equals ("assetClassMatch")) {
      String className = atts.getValue ("className");
      addObject (new GSAssetClassMatch (className));
    }

    //    else if (name.equals ("taskClassMatch"))
    //      addObject (new GSTaskClassMatch (atts.getValue ("preposition")));

    else if (name.equals ("taskVerbMatch"))
      addObject (new GSTaskVerbMatch (atts.getValue ("verb")));

    else if (name.equals ("constantAccessor"))
      addObject (new GSConstantAccessor (atts.getValue ("value"),
                                         atts.getValue ("type")));

    else if (name.equals ("valueAccessor"))
      addObject (new GSValueAccessor (atts.getValue ("type"),
				      atts.getValue ("units")));

    else if (name.equals ("assetAccessor")) {
	GSAssetAccessor aa = 
	    new GSAssetAccessorImpl (atts.getValue ("propGroup"),
				     atts.getValue ("propField"),
				     atts.getValue ("units"),
				     atts.getValue ("multiplyByQuantity"));

	addObject (aa);

	debugables.add (aa);
    }

    else if (name.equals ("assetMethod"))
      addObject (new GSAssetMethod
                 (atts.getValue ("methodName"), className,
                  (GSSchedulingSpecs) objectStack.elementAt (0)));

    //     else if (name.equals ("locationAccessor"))
    //       addObject (new GSLocationAccessor (atts.getValue ("representation")));

    //    else if (name.equals ("taskAccessor"))
    //      addObject (new GSTaskAccessorImpl (atts.getValue ("preposition")));

    else if (name.equals ("taskPrepAccessor"))
      addObject (new GSTaskPrepAccessor (atts.getValue ("preposition")));

    else if (name.equals ("taskMethod"))
      addObject (new GSTaskMethod
                 (atts.getValue ("methodName"), className,
                  (GSSchedulingSpecs) objectStack.elementAt (0)));

    else if (name.equals ("capabilityMatch")) {
      GSCapabilityMatch gscm = new GSCapabilityMatch (atts.getValue ("type"),
						      atts.getValue ("relationship"));
      addObject      (gscm);
      debugables.add (gscm);
    }

    else if (name.equals ("capabilityFilter"))
      addObject (new GSCapabilityFilter());

    else if (name.equals ("taskMatch"))
      addObject (new GSTaskMatch ());

    else if (name.equals ("taskGrouping"))
      addObject (new GSTaskGrouping());

    else if (name.equals ("capacityConstraint"))
      addObject (new GSCapacityConstraint());

    else if (name.equals ("taskDuration"))
      addObject (new GSTaskDuration());

    //    else if (name.equals ("travelTime"))
    //      addObject (new GSTravelTime (atts.getValue ("extraFactor")));

    else if (name.equals ("schedulingSpecs")) {
      GSSchedulingSpecs specs = null;
      className = atts.getValue ("codeClassName");
      if (className.equals (""))
        className = "org.cougaar.lib.gss.GSSchedulingSpecs";
      try {
        specs = (GSSchedulingSpecs) Class.forName (className).newInstance();
      } catch (Exception e) {
        System.out.println ("Problem making instance of class " + className);
        e.printStackTrace();
      }
      specs.setTaskGroupingMode (atts.getValue ("taskGroupingMode"));
      addObject (specs);
    }

    else
      System.out.println ("Bad object type " + name);
  }

  /**
   * Pop object off stack and add to parent
   */
  public void endElement (String uri, String localName, String name) {
    if (name.equals ("schedulingSpecs"))
      return;
    Object obj = objectStack.pop();
    ((GSParent) objectStack.peek()).addChild (obj);
  }

    List debugables = new ArrayList ();
}

