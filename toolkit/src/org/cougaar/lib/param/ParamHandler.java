/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package  org.cougaar.lib.param;

import org.xml.sax.HandlerBase;
import org.xml.sax.AttributeList;

/**
 * Class that handles what to do when we encouter a parameter in a 
 * parameter file. This class has a pointer to the parameter table
 * so that it can add the parameters when it encouters them in 
 * the file.
 *
 */
class ParamHandler extends HandlerBase {

  /**
   * Constructor
   * @param ptable the associated parameter table
   */
  ParamHandler(ParamTable ptable){
    this.paramTable = ptable;
  }
  

  /**
   * Overriden from base class.  This is the function that actually
   * knows what to do when we encouter a parameter.
   * @param name the name of the xml element (parameter name)
   * @param atts the attribute list of the xml element
   */
  public void startElement (String name, AttributeList atts) {
    String attributeName = null;
    String attributeType = null;
    String attributeValue = null;
    Param  param = null;
    int size = atts.getLength();

    if(name.equals("Parameter")){
      for(int i = 0; i < size; i++){
	String attribute = atts.getName(i);
	
	if (attribute.equals("name")){
	  attributeName = atts.getValue(i);
	}
	else if(attribute.equals("type")){
	  attributeType = atts.getValue(i);
	}
	else if(attribute.equals("value")){
	  attributeValue = atts.getValue(i);
	}
      }
      
      if(attributeName == null){
	throw new ParamRuntimeException("parameter missing NAME");
      }
      if(attributeType == null){
	throw new ParamRuntimeException("parameter missing TYPE");
      }
      if(attributeValue == null){
	throw new ParamRuntimeException("parameter missing VALUE");
      }
      
      if(attributeType.equals("boolean")){
	Boolean b = new Boolean(attributeValue);
	param = new BooleanParam(attributeName, b.booleanValue());
      }
      else if(attributeType.equals("double")){
	Double d = new Double(attributeValue);
	param = new DoubleParam(attributeName, d.doubleValue());
      }
      else if(attributeType.equals("float")){
	Float f = new Float(attributeValue);
	param = new FloatParam(attributeName, f.floatValue());
      }
      else if(attributeType.equals("int")){
	Integer i = new Integer(attributeValue);
	param = new IntParam(attributeName, i.intValue());
      }
      else if(attributeType.equals("long")){
	Long l = new Long(attributeValue);
	param = new LongParam(attributeName, l.longValue());
      }
      else if(attributeType.equals("short")){
	Short s = new Short(attributeValue);
	param = new ShortParam(attributeName, s.shortValue());
      }
      else if(attributeType.equals("String")){
	String s = new String(attributeValue);
	param = new StringParam(attributeName, s);
      }
      else{
	throw new RuntimeException("unknown type in parameter");
      }
      
      // finally add the parameter in the table
      paramTable.addParam(attributeName, param);
      
      /*
       * System.out.println(" Name = " + attributeName  + 
       *                    " Type = " + attributeType + 
       *		      " Value =  " + attributeValue);  
       */
    }
  }
  
  private ParamTable paramTable = null;
}











