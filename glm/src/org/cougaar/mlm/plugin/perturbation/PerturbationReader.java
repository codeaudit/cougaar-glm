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

package org.cougaar.mlm.plugin.perturbation;

import java.io.File;
import java.util.Vector;

import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.util.ConfigFinder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
  * The PerturbationReader class reads the perturbation
  * data ( which is in XML format ) and parses that data
  * using an XML Parser to create the specified 
  * Perturbations.
  */
public class PerturbationReader
{
   private String xmlfilename_; //default filename
   private File XMLFile;
   private Document doc;
   private Vector perturbations_;
   private Subscriber assetSubscriber_;
   private PerturbationPlugin pertPlugin_;

   /**
     * @param subscriber Asset Subscriber
	 */
   
   public PerturbationReader( String fileName, Subscriber subscriber, PerturbationPlugin pertPlugin )
   {
      setFileName( fileName );
      setSubscriber( subscriber );
	setPerturbationPlugin( pertPlugin );
      createPerturbations();
      readPerturbationData();
   }
   
   /**
     * Sets the name of the XML file to be read.
     * @param String XML File Name.
	 */
   private void setFileName( String filename )
   {
      this.xmlfilename_ = filename;
   }

	/**
	* Sets the PerturbationPlugin.
	* @param PerturbationPlugin The calling plugin.
	*/
   private void setPerturbationPlugin( PerturbationPlugin pertPlugin_ )
   {
      this.pertPlugin_ = pertPlugin_ ;
   }

   /**
     * Sets the asset subscriber.
     * @param obj Asset Subscriber
	 */
   private void setSubscriber( Subscriber obj )
   {
      this.assetSubscriber_ = obj;
   }
   
   /**
	 * Returns a refernce to the asset subscriber.
	 * @returns Returns the asset Subscriber.
	 */
   private Subscriber getSubscriber()
   {
      return this.assetSubscriber_;
   }
 
   /**
     * Reads the perturbation data from the XML file.
	 */
  public void readPerturbationData()
  {
    try {
      doc = ConfigFinder.getInstance().parseXMLConfigFile(xmlfilename_);
      parseFileData(doc);
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

   /**
     * Parses the perturbation data read in from the XML file.
	 * @param node XML Node
	 */
   protected void parseFileData(Node node) 
   {
      if( node != null)
	  {
		 int type = node.getNodeType();
		 Node nextTodo = null;
         if ( type == Node.DOCUMENT_NODE )
		 {
            node = ((Document)node).getDocumentElement();
         } 
		 else
		 {
			System.err.println("\nErrror: not DOCUMENT NODE\n");
		 }
		 NodeList theNodes = ((Element)node).getElementsByTagName("perturbation");
		 if (theNodes != null)
		 {
		    int len = theNodes.getLength();
			for ( int i = 0; i < len; i++ )
			{
			   perturbations_.addElement( new PerturbationNode(theNodes.item(i), 
			      getSubscriber(), pertPlugin_ ) );
			}
		 }
	  } 
   }

   /** 
     * Creates the perturbations.
	 */
   protected void createPerturbations()
   {
      Vector perturbations;
	 
	  perturbations = new Vector();
	 
	  setPerturbations ( perturbations );
   }
  
   /**
     * Sets the perturbations.
	 * @param pertDataIn Vector of perturbations.
	 */
   protected void setPerturbations( Vector pertDataIn )
   {
      this.perturbations_ = pertDataIn;
   }
  
   /**
     * Returns the perturbations.
	 * @return Returns the Perturbations.
     */
   public Vector getPerturbations()
   {
      return perturbations_;
   } 
}
