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


/*
 * File: DMPIClusterStartup.java
 * Heavily modified from original to be an LDMPlugin
 */

package org.cougaar.mlm.plugin;

/*
 * Imports
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import org.cougaar.planning.ldm.LDMPluginServesLDM;
import org.cougaar.planning.ldm.LDMServesPlugin;
import org.cougaar.planning.ldm.PlanningFactory;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.plugin.legacy.SimplePlugin;
import org.cougaar.util.ConfigFinder;

/**
 * This Data Management Plugin class reads a .ini file and creates a Cluster'
 * startup data.  For the moment the Plugin only instantiates the LDM
 * classes of interest to this Cluster.
 * <p>
 **/

public final class DMPIClusterStartup extends SimplePlugin
  implements LDMPluginServesLDM {

  private LDMServesPlugin ldm = null;
  private PlanningFactory ldmf = null;

  /** The file name for the assets **/
  private String theFileName;
  /** The UIC code to parse for **/
  private String theKey = null;

  /**
   * The public constructor wiht no args to set the default values.
   **/
  public DMPIClusterStartup() {
    this( "ClusterAsset.ini");
  }
  
  /**
   * The public constructor that enables the user to set the sepecific file to use in startup.
   **/
  public DMPIClusterStartup( String fileName ) {
    theFileName = fileName;
  }


  /**
   * Method to construct an Assetand add it to the internal BagOfAsset object.
   * <p><PRE>
   * PRE CONDITION:  The parseDataFile has located the key int he underlying file.
   * POST CONDITION: begin copying the data into a new vector.
   * INVARIANCE:         The start point of the parsing is the next line in the BufferedReader in.
   * </PRE>
   * @param aSpec The string object with the format (Type, NSN, qty, VIN).
   * @exception NumberFormatException If the qunatitiy can not be converted to a number.
   * @exception DMPluginException If unable to create the requested Asset.
   * @exception PlanningFactoryExceptionm I fPlanningFactory throws exception during creation.
   **/
  private Asset buildAsset( String aSpec ) throws NumberFormatException
  {
    Asset myAsset = null;
    String myClassName = null;
    String myNSN = null;
    try {
      //First we break the string down into the three specific data types.

      //locate the break points
      char listDelim = ',';
      int delim1 = aSpec.indexOf( listDelim ) ;
      int delim2 = aSpec.indexOf( listDelim, delim1 + 1 );
      int delim3 = aSpec.indexOf( listDelim, delim2 + 1 ) ;

      //now convert then into the three string values
      myClassName = aSpec.substring( 0, delim1 ).trim() ;
      myNSN = aSpec.substring( delim1 + 1, delim2 ).trim();
      String myQuantity = null;
      String myId = null;
      if( delim3 == -1 )
        myQuantity = aSpec.substring( delim2 + 2).trim();
      else{
        myQuantity = aSpec.substring( delim2 + 1, delim3 ).trim();
        myId = aSpec.substring( delim3 + 1).trim();
      }

      //convert the data types into something more useful
      int myCount = 0;
      if( myQuantity != null )
        myCount = new Integer( myQuantity ).intValue();

      //now we have the data so lets go shopping
      if( myCount > 0 ){
        Asset myObject = null;
        // If there's no identifier, this must be an aggregate, i.e., the count must be >= 1
        if( myId == null ) {
          myObject = ldmf.createAggregate( myClassName, myCount );
        }
        else
          myObject = ldmf.createInstance( myClassName, myId );

        myAsset = myObject;
      }
    } catch (Exception e) {
      String message = "DMPIClusterStartup.buildAssets(): Caught exception " + 
        e + 
        " while building " +
        aSpec +
        "." ;
      System.err.println( message + "\n" );
      e.printStackTrace();
    }

    return myAsset;
  }

  /**
   * Method to iterate over the Vector argunmet entries and build the asset.
   * <p><PRE>
   * PRE CONDITION:  The Vector contains all the data related to this key found in the data source.
   * POST CONDITION: A new asset is constructed for each of the required Asset objects
   * INVARIANCE:         
   * </PRE>
   * @param myParsedData The Vector of strings pulled out of the underlying data source.
   **/
  private void createObjects( Vector myParsedData ) {
    int i = 0;
    for(Enumeration e = myParsedData.elements(); e.hasMoreElements(); ){
      String element = (String)e.nextElement();
      try{
        Asset asset = buildAsset( element );           
        if (asset != null) {
          publishAdd(asset);
        }
      }catch( Exception ex ){
        System.out.println( "DMPIClusterStartup.createObjects failed to construct an asset (" + element + ")! " + ex);
      }
      i++;
    }
    //System.err.println("//DMPIClusterStartup built "+i+" Assets");
  }
        
    
  /**
   * Accessor for theFileName.
   * <p>
   * @return String The object currently referenced by theFileName variable
   **/
  public String getFileName() { return theFileName; }
    
  /**
   * Accessor for theKey.
   * <p>
   * @return String The object currently referenced by theKey variable
   **/
  private String getKey() { return theKey; }
    
  /**
   * Method to parse the values out of the INI file and construct the node profile.
   * <p>
   * This is a temporary method that enables the data to be stored in a simple text file until 
   * we construct the contemporary data source link for construction of cluster assets.
   * <p><PRE>
   * PRE CONDITION:  The parseDataFile has located the key int he underlying file.
   * POST CONDITION: begin copying the data into a new vector.
   * INVARIANCE:         The start point of the parsing is the next line in the BufferedReader in.
   * </PRE>
   * @param in BufferedReader object that contains the current stream of file data.
   * @return Vector Object with the Parsed strings in it.
   **/
  private Vector parseData( BufferedReader in )  {
    Vector myParsedData = new Vector();
                
    try {
      char openSquare = '[';
      char equals = '='; 
                        
      while( in.ready() ) {
        String sCheck = in.readLine();
        int nIndex = sCheck.indexOf( equals );
        if( nIndex != -1 && nIndex < sCheck.length() )
          myParsedData.addElement( sCheck.substring( nIndex + 1 ).trim() );
                                                                        
        int closeIn = sCheck.indexOf( openSquare );
        if( closeIn != -1 && closeIn < sCheck.length() )
          break;
      }
      in.close();
    }
    catch(IOException e) {
      System.out.println("Error: " + e);
      e.printStackTrace();
    }
                
    return myParsedData;
  }
        
  /**
   * Method to locate the key value in the underlying data source.  currently the source is a file but
   * the future may change this to some other data source and this method provides the hook for changing
   * the source and only having to rewrite one method.  The rest of the class will function properly if
   * this mehtod comnverts the data source into a BufferedReader stream.
   * <p>
   * This is a temporary method that enables the data to be stored in a simple text file until 
   * we construct the contemporary data source link for construction of cluster assets.
   * <p><PRE>
   * PRE CONDITION:  Runnable target is created and has not been run.
   * POST CONDITION: Object creates all Assets and notifuies the Componet of each one. Object blocks.
   * INVARIANCE:
   * </PRE>
   * @return Vector The vector object that contains the parsed data.
   * @exception DMPluginException If the key is not located in the file.
   **/
  private Vector parseDataFile()  {
    Vector myParsedData = null;
    try {
      BufferedReader in = new BufferedReader( new InputStreamReader( ConfigFinder.getInstance().open(getFileName()) ) );
      while( in.ready() ) {
        String sCheck = in.readLine();
        int locateUic = sCheck.indexOf( getKey() );
        if( locateUic != -1 && locateUic < sCheck.length() ){
          myParsedData = parseData( in );
          break;
        }
      }
      in.close();
    }
    catch(IOException e) {
      System.out.println("Error: " + e);
      e.printStackTrace();
    }
                
                
    if( myParsedData == null) {
      System.err.println("DMPIClusterStartup: in parseDataFile could not find section for [Cluster " + getKey() +"]");
    }
                        
    return myParsedData;                

  }
        
  /**
   * Load the data from the file synchronously.
   * We'll not actually do anything later on.
   **/
  protected void setupSubscriptions() {
    ldm = (LDMServesPlugin) getLDM();
    ldmf = ldm.getFactory();
    theKey = getMessageAddress().getAddress();

    if (!didRehydrate()) {	// Objects are already created after rehydration
      //first parse the data from the underlying file
      Vector myParsedData = parseDataFile();
      if (myParsedData != null) {
	//now create all the required objects from the parsed data
	createObjects( myParsedData );
      }
    }
  }

  protected void execute() {}
}
