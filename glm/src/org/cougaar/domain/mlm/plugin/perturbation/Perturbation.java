/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.domain.mlm.plugin.perturbation;
  
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
  
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Date;

import java.text.DateFormat;

import org.cougaar.core.cluster.Subscriber;

import org.cougaar.core.plugin.PlugInAdapter;

import org.cougaar.domain.planning.ldm.RootFactory;
import org.cougaar.domain.planning.ldm.asset.AggregateAssetAdapter;
import org.cougaar.domain.planning.ldm.asset.AggregateAsset;
import org.cougaar.domain.planning.ldm.asset.Asset;
import org.cougaar.domain.planning.ldm.asset.ItemIdentificationPG;
  
import org.cougaar.domain.planning.ldm.measure.Mass;
import org.cougaar.domain.planning.ldm.measure.Volume;
  
import org.cougaar.domain.glm.ldm.oplan.Oplan;

import org.cougaar.domain.planning.ldm.plan.Schedule;
import org.cougaar.domain.planning.ldm.plan.RoleSchedule;
import org.cougaar.domain.planning.ldm.plan.RoleScheduleImpl;
import org.cougaar.domain.planning.ldm.plan.ScheduleImpl;

import org.cougaar.util.ReusableThread;
import org.cougaar.util.ReusableThreadPool;
	
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.xml.parsers.DOMParser;
  
/**
 * The Perturbation class is a container for the 
 * perturbation data, as well as the methods needed
 * to perform the perturbation of the desires asset(s).
 */
public class Perturbation implements Runnable
{
   /***************************
    *   PUBLIC CONSTANTS      *
    ***************************/
   // Class Names
   private static String stringClass = "java.lang.String";
   private static String intClass = "java.lang.Integer";
   private static String boolClass = "java.lang.Boolean";
   private static String volumeClass = "org.cougaar.domain.planning.ldm.measure.Volume";
   private static String massClass = "org.cougaar.domain.planning.ldm.measure.Mass";   
      
   public static final String ASSET_CLASS = "org.cougaar.domain.planning.ldm.asset.Asset";
   public static final String AGGREGATE_ASSET_CLASS = 					"org.cougaar.domain.planning.ldm.asset.AggregateAsset";
   public static final String ASSET_ID_PROPERTY = "ItemIdentificationPG";
   public static final String OPLAN_ID_PROPERTY = "OplanId";
   public static final String QUANTITY = "Quantity";
    
   public static final String OBJECT = "object";
   public static final String UIC = "uic";
   public static final String ITEM_ID = "item_id";
   public static final String TYPE = "type";
   public static final String PROPERTY = "property";
   public static final String FIELD = "field";
   public static final String VALUE = "value";
   public static final String VALUE_TYPE = "value_type";
   public static final String START_DATE = "start_date";
   public static final String END_DATE = "end_date";
   public static final String JOB = "job";
   public static final String STRING = "String";
   public static final String BOOLEAN = "Boolean";   
   public static final String MODIFY = "MODIFY";
   public static final String REMOVE = "REMOVE";
   
   public static final String SET_PREFIX = "set";
   public static final String GET_PREFIX = "get";
   public static final String OBJECT_SUFFIX = "Object";

   private HashMap attributes_;
   private Vector objects_;
   private Subscriber objectSubscriber_;
   private PerturbationPlugIn pertPlugIn_;
   
/**
 * @param perturbationData XML Perturbation Data Node    
 */
   public Perturbation ( Node perturbationData, PerturbationPlugIn pertPlugIn )
   {
      parseData( perturbationData );
      this.pertPlugIn_ = pertPlugIn;
   }

/**
 * Sets the asset subscriber.
 * @param assetSubscriber Subscriber to the Assets
 */
   public void setSubscriber( Subscriber assetSubscriber )
   {
      this.objectSubscriber_ = assetSubscriber;
   }
   
/**
 * Sets the list of assets.
 * @param assets List of assets.
 */
   public void setAssets( Vector assets )
   {
      this.objects_ = assets;
   }
 
/**
 * Sets the Object Type.
 * @param theObjectType The type of object to be perturbed.
 */
   public void setObjectType( String theObjType ) 
   {
      attributes_.put( OBJECT, theObjType );
   }

/** 
 * Returns the object type.
 */
   public String getObjectType() 
   {
      return ( (String)attributes_.get(OBJECT) );
   }
  
/**
 * Sets the Unique Identification Code.
 * @param theUIC The Unique Identification Code
 */
   public void setUIC( String theUIC ) 
   {
      attributes_.put( UIC, theUIC );
   }

/** 
 * Returns the Unique Identification Code.
 */
   public String getUIC() 
   {
      return ( (String)attributes_.get(UIC) );
   }
/**
 * Sets the ItemId.
 * @param theItemId the ItemIndentification
 */
   public void setItemId( String theItemId ) 
   {
      attributes_.put( ITEM_ID, theItemId );
   }

/** 
 * Returns the ItemIdentification.
 */
   public String getItemId() 
   {
      return ( (String)attributes_.get(ITEM_ID) );
   }

/** 
 * Sets the Type of Perturbation.
 * @param theType Type of Perturbation (i.e. Modify, Remove )
 */
   public void setType( String theType ) 
   {
      attributes_.put( TYPE, theType );
   }

/**
 * Returns the TYPE of Perturbation.
 */
   public String getType() 
   {
      return ( (String)attributes_.get(TYPE) );
   }
  
/**
 * Sets the asset property to be perturbed.
 * @param theProperty The asset property to be perturbed.
 */
   public void setProperty ( String theProperty )
   {
      attributes_.put( PROPERTY, theProperty );
   }
   
/** 
 * Returns the asset property to be perturbed.
 */
   public String getProperty()
   {
      return ( (String)attributes_.get( PROPERTY ) );
   }
   
/**
 * Sets the property field to be perturbed.
 * @param theField The property field to be perturbed.
 */
   public void setField ( String theField )
   {
      attributes_.put( FIELD, theField );
   }
   
/** 
 * Returns the property field to be perturbed.
 */
   public String getField()
   {
      return ( (String)attributes_.get(FIELD) );
   }
   
/**
 * Sets the field value to be modified.
 * @param theValue The field value to be modified.
 */
   public void setValue ( String theValue )
   {
      attributes_.put( VALUE, theValue );
   }
   
/** 
 * Returns the field value to be modified.
 */
   public String getValue()
   {
      return ( (String)attributes_.get(VALUE) );
   }

/**
 * Sets the value type of the field to be modified.
 * @param theValue The value type of the field to be modified.
 */
   public void setValueType ( String theValueType )
   {
      attributes_.put( VALUE_TYPE, theValueType );
   }
   
/** 
 * Returns the value type of the field to be modified.
 */
   public String getValueType()
   {
      return ( (String)attributes_.get(VALUE_TYPE) );
   }
   
/**
 * Sets the new start_date for the asset's available schedule.
 * @param theValue A String representation of the new start_date.
 */
   public void setStartDate ( String theValue )
   {
      attributes_.put( START_DATE, theValue );
   }
   
/** 
 * Returns the new start_date of the asset's available schedule.
 */
   public String getStartDate()
   {
      return ( (String)attributes_.get(START_DATE) );
   }

/**
 * Sets the new end_date for the asset's available schedule.
 * @param theValue A String representation of the new end_date.
 */
   public void setEndDate ( String theValue )
   {
      attributes_.put( END_DATE, theValue );
   }
   
/** 
 * Returns the new end date of the asset's available schedule.
 */
   public String getEndDate()
   {
      return ( (String)attributes_.get(END_DATE) );
   }

   // ---------  END OF ACCESSOR METHODS -----------

/**
 * Parses the Perturbation data contained in the XML Node.
 * @param node XML Perturbation Data Node.
 */
   public void parseData(Node node) 
   {
      attributes_ = new HashMap();
	  
      // is there anything to do?
      if (node == null)
      {
         return;
      }
      realParseData( node, "firstrun");
   }

/**
 * Recursively parses the Perturbation node to create child 
 * nodes.
 * @param node XML perturbation data node.
 * @param previousName  Previous XML Node name.
 */   
   private void realParseData( Node node, String previousName )
   {
       // is there anything to do?
      if (node == null)
      {
         return;
      }
		
      int type = node.getNodeType();
      switch (type) 
      {
         // Print element with attributes
         case Node.ELEMENT_NODE: 
         {
            String tempName = node.getNodeName();
            NodeList children = node.getChildNodes();
            if (children != null) 
            {
               int len = children.getLength();
               for (int i = 0; i < len; i++) 
               {
                  realParseData(children.item(i), tempName);
               }
            }
            break;
         }
            
         // Print text
         case Node.TEXT_NODE:
         {
            processIt(previousName, node.getNodeValue());
            break;
         }
      }
   } // realParseData(Node,String)
	
/**
 * Sets the appopriate perturbation value based on the
 * XML tag.
 * @param stuff XML perturbation data tag type
 * @param value XML perturbation data tag value
 */
   private void processIt(String stuff, String value)
   {
      value = value.trim();
      if(   (stuff != null) && (value != null) )
      {
         // Set the type of the object to be perturbed.
         if (stuff.compareTo( OBJECT ) == 0)
            setObjectType ( value );
	  
         // Set the perturbation UIC
         if (stuff.compareTo( UIC ) == 0)
            setUIC ( value );

         // Set the perturbation ITEM_ID
         if (stuff.compareTo( ITEM_ID ) == 0)
            setItemId ( value );
	  
         // Set the perturbation TYPE
         if (stuff.compareTo( TYPE ) == 0)
            setType ( value );
		  
         // Set the PROPERTY tied to this perturbation
         if (stuff.compareTo( PROPERTY ) == 0)
            setProperty ( value );
		  
         // Set the FIELD of the PROPERTY to change
         if (stuff.compareTo( FIELD ) == 0)
            setField ( value );
		  
         // Set the new VALUE of the FIELD
         if (stuff.compareTo( VALUE ) == 0)
            setValue ( value );
		  
         // Set the new VALUE_TYPE of the FIELD
         if (stuff.compareTo( VALUE_TYPE ) == 0)
            setValueType ( value );

         // Set the new start date for the available schedule
         if (stuff.compareTo( START_DATE ) == 0)
            setStartDate ( value );

         // Set the new end date for the available schedule
         if (stuff.compareTo( END_DATE ) == 0)
            setEndDate ( value );

      }
   }
   
/**
 * Locates the asset to be perturbed in the asset list.
 */
   private Object locateObject()
   {
      Asset currentAsset   = null;
      Class className      = null;
      Class[] methodParams = null;
      Object ldmObject     = null;
      Oplan oplanObject    = null;
      String assetId       = null;
      String methodName    = null;  
      Object property      = null;

	//loop through and find the asset with the matching item_id
      for ( int i = 0; i < objects_.size(); i++ )
      {
         ldmObject = objects_.elementAt(i);
         if ( ldmObject instanceof Asset )
         {
            currentAsset = (Asset)ldmObject;				 
            if( (className = getAssetClassOfObject( currentAsset )) != null )
            {		
               // Using reflection, get the asset id property method to
               // determine if this asset should be perturbed.
               property = reflectOn( className, currentAsset );
			   
               try
               {
                  methodName = GET_PREFIX + ASSET_ID_PROPERTY;
                  methodParams = null;

	
                  property = reflectAndInvoke( className, currentAsset,
                               methodName, methodParams, methodParams );
               }
               catch ( Exception exception )
               {				                  System.out.println("<<<PerturbationPlugIn>>> PERTURBATION" +
                        " ERROR::Cluster with UIC " + getUIC() + 
                        "; Asset with item_id " + getItemId() + "\n" +                         exception.toString());
               }		
 	           
               // If the object id of the current asset is the same as 
               // the object id for this perturbation, return the ldmObject. 
               assetId = ( (ItemIdentificationPG)property).
					getItemIdentification();
			  
               if ( (assetId != null) && (getItemId().equals( assetId )) )
               {
                  return currentAsset;
               }
            }
         }
         else if ( ldmObject instanceof Oplan )
         {
            oplanObject = (Oplan)ldmObject;
            {		
               // If the oplan object to be perturbed is in the log plan,
               // get the property of the asset to be perturbed.
               assetId = oplanObject.getOplanId().toString();
			   
               if ( (assetId != null) && (getItemId().equals( assetId )) )
               {
                  return oplanObject;
               }
            }
         }
      }
      return null;
   }
/**
 * Determines if the specified asset property is accessible 
 * (exists and is public).
 * @param theClass The class to search for the property in
 * @return Returns the class in which the property is accessible.
 */
   private Class propertyAccessible( Class theClass )
   {
      String objMethodName  = null;
      String propertyMethod = null;
      Class returnValue     = null;
      Method[] methods      = theClass.getDeclaredMethods();

      propertyMethod = SET_PREFIX + getProperty();
      for (int i = 0; i < methods.length; i++)
      {
         objMethodName = methods[i].getName();
         if( (objMethodName.indexOf(propertyMethod)) != -1)
         {
            return (returnValue = theClass);
         }
      }
      return propertyAccessible( theClass.getSuperclass() );
   }
   
/**
 * Traverses the objects class hierarchy until it finds the 
 * actual Asset instance of the LDM class.
 * @param theObject The LDM Log Plan object
 * @return Returns the Asset class.  If not found it returns null.
 */
   private Class getAssetClassOfObject ( Object theObject )
   {  
      // In order to determine if the LDM Object is the one
      // to be perturbed, you need to get the UIC which can only 
      // be accessed from the asset class. ( For some reason, 
      // the LDM object could see its inherited access methods!!!).
	  
      Class theClass = theObject.getClass();
      while ( !(theClass.getName().equals( ASSET_CLASS )) )
      {
         theClass = theClass.getSuperclass();
         if ( theClass == null )
            return theClass;
      }
      return theClass;
   }
   
/**
 * Locates the appropriate method and invokes it.
 * @param theClass The Class of the LDM object
 * @param obj The LDM object
 * @param methodName The name of the method to invoke
 * @param locatorParams Parameter types used to locate the method
 * @param invokeParams Parameter types used to invoke the method
 * @return Returns the object returned by the method call
 */
   private Object reflectAndInvoke (
				Class theClass, 
				Object obj,
				String methodName, 
				Class[] locatorParams, 
				Object[] invokeParams )
   {
      Method method      = null;
      Object returnValue = null;
	  
      try
      {

         method = theClass.getMethod( methodName, locatorParams );
         returnValue = method.invoke( obj, invokeParams );
      }
      catch ( NoSuchMethodException nsme )
      {
         System.out.println("<<<PerturbationPlugIn>>>PERTURBATION " +
               "ERROR::Cluster with UIC " + getUIC() + 
               "; Asset with item_id " + getItemId() + "\n" +
               nsme.toString() );
      }
      catch ( Exception exception )
      {
         System.out.println("<<<PerturbationPlugIn>>> PERTURBATION " +
                               "ERROR::Cluster with UIC " +getUIC() +
               "; Asset with item_id " + getItemId() + "\n" +
               exception.toString() );
      }	
      return returnValue;
   }
   
/**
 * Initiates reflection on the current LDM Object.
 * @param theClass The Class of the LDM object
 * @param obj The LDM object
 */
   private Object reflectOn ( Class theClass, Object obj )
   {
      Class[] methodParams = null;
      String methodName    = null;
      Object property      = null;

      try
      {
         methodName = GET_PREFIX + ASSET_ID_PROPERTY;	
         property = reflectAndInvoke( theClass, obj, methodName,
                                      methodParams, methodParams );
      }
      catch ( Exception exception )
      {
         System.out.println("<<<PerturbationPlugIn>>> PERTURBATION " +
               "ERROR::Cluster with UIC " + getUIC() +
               "; Asset with item_id " + getItemId() + "\n" +
                exception.toString() );
      }
      return property;
   }
   
/** 
 * Removes the LDM object from the LogPlan.
 * @param asset The asset reference to be removed
 * @return The status of the remove transaction on the LogPlan
 */
   private boolean removeObject( Asset asset )
   {
      boolean status;
		 
      System.out.println("\n<<<PerturbationPlugIn>>> Removing asset with " +
             "item_id " + getItemId() + " of Cluster with UIC " + getUIC() );
      objectSubscriber_.openTransaction();
      status = objectSubscriber_.publishRemove( asset );
      objectSubscriber_.closeTransaction(false);
		 
      return status;      
   }
   
/** 
 * Modifies the LDM object in the LogPlan.
 * @param className Class of the LDM object
 * @param asset LDM object
 * @return The status of the asset change transaction on the LogPlan
 */
   private boolean modifyObject( Class className, Object asset )
   {
      Method method        = null;
      Class[] methodParams = null;
      Object[] params      = null;
      Object property      = null;
      Object accessor      = null;
      String assetId       = null;
      String methodName    = null;
      boolean status       = false;
      String startString   = getStartDate();
      String endString     = getEndDate();
      String prop          = getProperty();

      try
      {
         if ( prop != null )
         {
            methodName = GET_PREFIX + getProperty();

            property = reflectAndInvoke( className, asset, methodName,
                                        methodParams, methodParams );

		 if ( property != null )
            {
               // Get the field accessor method and invoke it to set the
               // new value.
	            System.out.println("\n<<<PerturbationPlugIn>>> Perturbation " +   
                  " on asset with item_id " + getItemId() + " of Cluster " +
                  " with UIC " + getUIC() + " ....Beginning.....");
			
               methodName = SET_PREFIX + getField();
		
               // Get the methods parameter data type, and 
               // instantiate and instance of it.
               if (getValueType().equalsIgnoreCase(STRING))
               {
                  Class str = Class.forName( stringClass );
                  methodParams = new Class[1];
                  methodParams[0] = str;

                  Constructor constructor = str.getConstructor( methodParams );
                  params = new Object[1];

                  params[0] = getValue();

                  Object objType = constructor.newInstance( params );	
                  params[0] = objType;
               }

               else if (getValueType().equalsIgnoreCase(BOOLEAN))
               {
                  methodParams = new Class[1];
                  methodParams[0] = Boolean.TYPE;

                  params = new Object[1];
         
			if (getValue().toLowerCase().equals("true"))
                     params[0] = Boolean.TRUE;
                  else
                     params[0] = Boolean.FALSE;
               }
               // Invoke the method.
               accessor = reflectAndInvoke( property.getClass(), property, 
                                            methodName, methodParams, params );
				
               // Publish change
               objectSubscriber_.openTransaction();
               status = objectSubscriber_.publishChange( asset );
               objectSubscriber_.closeTransaction( false );
				 
               // For testing....
               methodName = GET_PREFIX + getField();
               methodParams = null;
               method = ( property.getClass() ).getMethod ( methodName,
                                                            methodParams );
					                              System.out.println("\n<<<PerturbationPlugIn>>> " +
                  "UIC: " + getUIC() + ", item_id: " + getItemId() + "\n" +
                  getField() + " Field of Property " + getProperty() +
                  " has been changed to " + 
                  method.invoke( property, methodParams) );
            }
         }

         if ( !( startString.equals( "" ) ) || !( endString.equals( "" ) ) )
         {
            DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT );
            RoleSchedule rs = ( (Asset)asset).getRoleSchedule();
            Schedule s = ( (RoleScheduleImpl) rs).getAvailableSchedule();
            Date new_start_date = ( (ScheduleImpl)s )
                                        .getStartDate();
            Date new_end_date = ( (ScheduleImpl)s )
                                        .getEndDate();

            if ( !startString.equals( "" ) )
               new_start_date = df.parse(startString);

            if ( !endString.equals( "" ) )
               new_end_date = df.parse(endString);				

            RootFactory ldmf = pertPlugIn_.getMyRootFactory();
            Schedule sched = ldmf.newSimpleSchedule( new_start_date,
                                                     new_end_date);
            ( (RoleScheduleImpl) rs).setAvailableSchedule( sched );

            // Publish change
            objectSubscriber_.openTransaction();
            status = objectSubscriber_.publishChange( asset );
            objectSubscriber_.closeTransaction( false );

            //For testing...
            Date start = ( ( Asset ) asset ).getRoleSchedule().
                           getAvailableSchedule().getStartDate();
            Date end = ( ( Asset ) asset).getRoleSchedule().
                           getAvailableSchedule().getEndDate();
            System.out.println("\n<<<PerturbationPlugIn>>> " +
               "The available schedule for asset with item_id: " +
               getItemId() + ", from cluster with UIC: " + getUIC() +
               "has changed...\n" + "AVAILABLE SCHEDULE:\n" + 
               start	+ ":" + end );
         }     
      }
      catch ( NoSuchMethodException nsme )
      {
         System.out.println("\n<<<PerturbationPlugIn>>> PERTURBATION " +
            "ERROR::UIC: " + getUIC() + ", item_id: " + getItemId() + "\n" +  
            nsme.toString());
      }
      catch ( Exception exception )
      {
         System.out.println("\n<<<PerturbationPlugIn>>> PERTURBATION "
            + "ERROR::UIC: " + getUIC() + ", item_id: " + getItemId() +
            "\n" + exception.toString() );
         exception.printStackTrace();
      }
      return status;
   }

// ************************
//    RUNNABLE INTERFACE
// ************************
   
/**
 * Runs the Perturbation.
 */
   public synchronized void run()
   {
      Asset theAsset  = null;
      Class className = null;
      Object obj      = null;	 
      Oplan theOplan  = null;
      boolean status  = false;

      if( ( obj = locateObject() ) != null )
      {
         if ( obj instanceof Asset )
         {
            theAsset = (Asset)obj;

            if( (getType().toUpperCase()).equals(MODIFY) )
            {
               String start = getStartDate();
               String end = getEndDate();
               String property = getProperty();
               

               // If there is a new start_date or a new end_date, then the
               // asset's available schedule needs to be modified. If getProperty()
               // returns a property, then a field in a property group needs to
               // be modified.
               if ( ( !start.equals("") || !end.equals("") ) ||
                    ( !property.equals("") && (className =
                                      propertyAccessible( theAsset.getClass() ) ) != null ) )
               {
                  status = modifyObject( className, theAsset );
               }
               else
               {
                  System.out.println("\n<<<PerturbationPlugIn>>> PERTURBATION " +
                     "ERROR::" + "\tPerturbation Unsuccessful...Property " +
                     getProperty() + " is not accessible on Asset with item_id " +
                     getItemId() + " in Cluster with UIC " + getUIC() );
               }
            }
            else if( (getType().toUpperCase()).equals(REMOVE) )
            {
               status = removeObject( theAsset );				 
            }
         }
         else if ( obj instanceof Oplan )
         {
            theOplan = (Oplan)obj;	
            if( (className = propertyAccessible( theOplan.getClass() )) != null )
            {
               status = modifyObject( className, theOplan );
            }
            else
            {
               System.out.println("\n<<<PerturbationPlugIn>>> PERTURBATION ERROR::"
                  + "\tPerturbation Unsuccessful...Property " + getProperty() +
                  " is not accessible on Oplan with OplanId " + getItemId() );
            }
         }
      }
      else
      {
         System.out.println("\n<<<PerturbationPlugIn>>> PERTURBATION ERROR::" +
            "\tPerturbation Unsuccessful...Cluster: " + this.getUIC() +
            ", Asset: " + this.getItemId() + " not available!!!" );			
      }
	  
      if ( !status )
      {
         System.out.println("\n<<<PerturbationPlugIn>>> PERTURBATION ERROR::" +
            "\tPerturbation Unsuccessful...Unable to modify " + getField() + 
            " of the " + getProperty() + " on Asset with item_id " +
            this.getItemId() + " of Cluster with UIC " + this.getUIC() );
      }	
      System.out.println("\n<<<PerturbationPlugIn>>> Perturbation on object " +
            "with UIC: " + this.getUIC() + "; item_id: " + this.getItemId() +
            "....Ending");
   }
}
