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

package  org.cougaar.lib.param;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class that handles what to do when we encouter a parameter in a 
 * parameter file. This class has a pointer to the parameter table
 * so that it can add the parameters when it encouters them in 
 * the file.
 *
 */
class ParamHandler extends DefaultHandler {

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
  public void startElement (String uri, String local, String name, Attributes atts) throws SAXException {
    String attributeName = null;
    String attributeType = null;
    String attributeValue = null;
    Param  param = null;
    int size = atts.getLength();

    if(name.equals("Parameter")){
      for(int i = 0; i < size; i++){
	//	String attribute = atts.getName(i);
	String attribute = atts.getLocalName(i);
	
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
       * logger.debug(" Name = " + attributeName  + 
       *                    " Type = " + attributeType + 
       *		      " Value =  " + attributeValue);  
       */
    }
  }
  
  private ParamTable paramTable = null;
}











