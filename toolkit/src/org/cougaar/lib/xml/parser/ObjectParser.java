/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBNT Solutions (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.lib.xml.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.cougaar.core.cluster.ClusterServesPlugIn;
import org.cougaar.domain.planning.ldm.LDMServesPlugIn;
import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.Factory;
import org.cougaar.domain.planning.ldm.asset.Asset;

import org.cougaar.domain.planning.ldm.asset.PropertyGroup;
import org.cougaar.domain.planning.ldm.FactoryException;
import org.cougaar.domain.planning.ldm.measure.AbstractMeasure;
import java.lang.reflect.Method;

/**
 * Parses objects within prototypes or instances.
 *
 * Creates the objects.
 */
public class ObjectParser{
  
  static final String PG_STRING = "PG";

  public static Object getObject(LDMServesPlugIn ldm, Node node){
    RootFactory ldmFactory = ldm.getFactory();
    Object obj = null;

    if(node.getNodeName().equals("object")){
      NodeList  nlist    = node.getChildNodes();      
      int       nlength  = nlist.getLength();

      String className = node.getAttributes().getNamedItem("class").getNodeValue();

      // Optimization- try to predict whether the object is a PG or an Asset
      // based on endsWith("PG"); the other will always be tried if the
      // first one fails, though.
      if (className.endsWith(PG_STRING)) {
	// first assume that it is a property or capability
	if(obj == null){
	  try{
	    // System.out.println ("object - " + className + " class " + Class.forName(className));
	    obj = ldmFactory.createPropertyGroup(Class.forName(className));
	    // System.out.println("** OBJECT PARSER CREATED : from create PG " + obj);
	  }
	  catch(Exception e){
	    // System.out.println ("exception " + e);
	    obj = null;
	  }
	}
	//  System.out.println ("object - " + className);
	// now try to see if it is an asset
	if(obj == null){
	  try{
	    obj = ldmFactory.createAsset(Class.forName(className));
	    // System.out.println("** OBJECT PARSER CREATED create Asset: " + obj);
	  }
	  catch(Exception e){
	    obj = null;
	  }      
	}	
      } else {
	//  System.out.println ("object - " + className);
	// First try to see if it is an asset
	if(obj == null){
	  try{
	    obj = ldmFactory.createAsset(Class.forName(className));
	    //System.out.println("** OBJECT PARSER CREATED create Asset: " + obj + ":" + ((Asset)obj).getUID());
	  }
	  catch(Exception e){
	    obj = null;
	  }      
	}
	
	// now assume that it is a property or capability
	if(obj == null){
	  try{
	    // System.out.println ("object - " + className + " class " + Class.forName(className));
	    obj = ldmFactory.createPropertyGroup(Class.forName(className));
	    // System.out.println("** OBJECT PARSER CREATED : from create PG " + obj);
	  }
	  catch(Exception e){
	    // System.out.println ("exception " + e);
	    obj = null;
	  }
	}
      }
      
      // now assume that it could be build by the cluster object factory
      // notice that the ldm factory extends the cof.
      if(obj == null){
	try{
	  String method_name = ObjectParser.getMethodName(className);
	  Method method = ldmFactory.getClass().getMethod(method_name, null);
	  obj = method.invoke(ldmFactory, null);
	  // System.out.println("*** OBJECT PARSER CREATED : from factory " + obj);
	}
	catch(Exception e){
	  try {
	    if (obj == null) {
	      Factory af = ldm.getFactory("glm");
	      if (af != null) {
		  String method_name = ObjectParser.getMethodName(className);
		  Method method = af.getClass().getMethod(method_name, null);
		  obj = method.invoke(af, null);
	      }
	      //System.out.println("*** OBJECT PARSER CREATED : from GLMFactory " + obj);
	    } 
	  } catch (Exception e2) {
	    obj = null;
	  }
	}
      }

      // now assume that it is a property or capability that failed with createPropertyGroup
      if(obj == null){
	try{
	  int last =className.lastIndexOf ('.');
	  String first = className.substring (last+1,last+4);
	  if (first.equals ("New"))
	    className = className.substring (0, last+1) + className.substring (last+4, className.length());
	  // System.out.println ("object - " + className);
	  obj = ldmFactory.createPropertyGroup(className);
	  // System.out.println("** OBJECT PARSER CREATED : from create PG 2 " + obj);
	}
	catch(Exception e){
	  // System.out.println ("exception " + e);
	  obj = null;
	}
      }

      // now try the measure classes
      if(obj == null){
	try{
	  obj = Class.forName(className);
	  // System.out.println("**** OBJECT PARSER CREATED : measure " + obj);
	}
	catch(Exception e){
	  obj = null;
	}
      }
      
      if(obj == null){
	// BOZO: need to throw exception here.
	// perhaps use the new operator (?)
	System.err.println("ObjectParser: Could not create: " + className);
      }
      
      for(int i = 0; i < nlength; i++){
	Node    child       = nlist.item(i);
	String  childname   = child.getNodeName();

        if(child.getNodeType() == Node.ELEMENT_NODE){
	  if(childname.equals("field")){
	    // the setField() function returns the object that it set the
	    // field on.  In most cases that will be the same as in its 
	    // third argument, but in the weird measure classes case that
	    // return value is the actual measure class created.
	    obj = FieldParser.setField(ldm, child, obj);
	  }
        }
      }
    }

    return obj;
  } 

  /** 
   * try to get a method name that could be used in the 
   * cluster object factory.
   */
  public static String getMethodName(String name){
    char[] name_array = name.toCharArray();
    
    int marker = 0;

    for(int i = name_array.length - 1; i >=0; i--){
      if(name_array[i] == '.'){
	marker = i+1;// point to first letter after last '.'
	break;
      }
    }
    
    if(name_array[marker]   == 'N' &&
       name_array[marker+1] == 'e' &&
       name_array[marker+2] == 'w'){

      name_array[marker] = 'n';
    }
    else if(marker >= 3){
      name_array[marker-3] = 'n';
      name_array[marker-2] = 'e';
      name_array[marker-1] = 'w';
      marker -= 3;
    }

    return new String(name_array, marker, name_array.length-marker);
    
  }

}

