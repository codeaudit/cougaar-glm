package org.cougaar.domain.mlm.ui.psp.transit.data.xml;

import java.io.InputStream;
import java.io.Reader;

import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

/**
 * Gets deXMLable objects back from an XML stream
 * @author Benjamin Lubin; last modified by: $Author: blubin $
 * @version $Revision: 1.1 $ on $Date: 2001-01-29 21:44:31 $
 * @since 1/24/01
 **/
public class DeXMLizer extends DefaultHandler{

  //Variables:
  ////////////

  /** Default parser name. */
  protected static final String 
    PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
  

  protected static boolean setValidation    = false;
  protected static boolean setNameSpaces    = false;
  protected static boolean setSchemaSupport = true;
  
  protected XMLReader parser;

  /**Buffer data across characters() calls to make sure we have it all**/
  protected StringBuffer data;
  /**Buffer data across characters() calls to make sure we have it all**/
  protected AttributesImpl lastAttr;
  /**Buffer data across characters() calls to make sure we have it all**/
  protected String lastTag;

  /**Stack of objects being built**/
  protected List stack;

  /**Return values**/
  protected List objects;

  /**Our Factory **/
  protected DeXMLableFactory factory;

  //Constructors:
  ///////////////

  public DeXMLizer(DeXMLableFactory f){
    try{
      parser = (XMLReader)Class.forName(PARSER_NAME).newInstance();
      parser.setFeature( "http://xml.org/sax/features/validation", 
			 setValidation);
      parser.setFeature( "http://xml.org/sax/features/namespaces",
			 setNameSpaces );
      parser.setFeature( "http://apache.org/xml/features/validation/schema",
			 setSchemaSupport );
      parser.setContentHandler(this);
      parser.setErrorHandler(this);
    }catch(Exception e){
      reportMessage("Exception setting up parser: ",e);
    }
    objects=new ArrayList();
    stack=new ArrayList();
    data=new StringBuffer();
    factory=f;
  }

  //Members:
  //////////

  public DeXMLable parseObject(Reader r){
    return parseObject(new InputSource(r));
  }

  public DeXMLable parseObject(InputStream is){
    return parseObject(new InputSource(is));
  }

  public DeXMLable parseObject(InputSource is){
    return (DeXMLable)parseObjects(is).get(0);
  }

  public List parseObjects(Reader r){
    return parseObjects(new InputSource(r));
  }

  public List parseObjects(InputStream is){
    return parseObjects(new InputSource(is));
  }

  public List parseObjects(InputSource is){
    try{
      parser.parse(is);
    }catch(Exception e){
      reportMessage("Exception parsing",e);
      e.printStackTrace();
    }
    return objects;
  }

  protected DeXMLable curObj(){
    if(stack.size()<1)
      return null;
    return (DeXMLable) stack.get(stack.size()-1);
  }

  protected void setCache(String tag, Attributes attrs){
    lastTag=tag;
    if(attrs != null)
      lastAttr=new AttributesImpl(attrs);
    else
      lastAttr=null;
    data.setLength(0);
  }

  protected boolean cacheEmpty(){
    return lastTag==null;
  }

  protected void processCachedTag(){
    try{
      //Check if this should be a new SubObj:
      DeXMLable subObj;
      subObj = factory.beginSubObject(curObj(), lastTag, lastAttr);
      if(curObj()==null&&subObj==null){
	throw new UnexpectedXMLException("No object registered for tag: "+
					 lastTag);
      }
      
      //New subObj:
      if(subObj!=null){
	stack.add(subObj);
      }
      curObj().openTag(lastTag,lastAttr,data.toString());
    }catch(UnexpectedXMLException e){
      reportMessage(e);
    }
  }

  //
  // DocumentHandler methods
  //
  
  /** Start document. */
  public void startDocument() {
    lastTag=null;
    objects.clear();
    stack.clear();
  }
  
  /** Start element. */
  public void startElement(String uri, String local, String raw, 
			   Attributes attrs) {
    if(!cacheEmpty()){
      processCachedTag();
    }
    setCache(raw,attrs);
  }
  
  /** Characters. */
  public void characters(char ch[], int start, int length) {
    data.append(ch,start,length);
  }
  
  /** Ignorable whitespace. */
  public void ignorableWhitespace(char ch[], int start, int length) {
  }
  
  /** End element. */
  public void endElement(String uri, String local, String raw) {
    if(!cacheEmpty()){
      processCachedTag();
    }
    setCache(null,null);

    try{
      DeXMLable curObj = curObj();
      if(curObj==null){
	reportMessage("Unexpected 0 stack");
	return;
      }
      if(curObj.closeTag(raw)){
	stack.remove(stack.size()-1);
	if(stack.size()==0){
	  objects.add(curObj);
	}else{
	  curObj().completeSubObject(raw,curObj);
	}
      }
    }catch(UnexpectedXMLException e){
      reportMessage(e);
    }
  }
  
  //
  // ErrorHandler methods
  //
  
  /** Warning. */
  public void warning(SAXParseException ex) {
    reportMessage("[Warning] "+
		  getLocationString(ex),
		  ex);
  }

  /** Error. */
  public void error(SAXParseException ex) {
    reportMessage("[Error] "+
		  getLocationString(ex),
		  ex);
  }

  /** Fatal error. */
  public void fatalError(SAXParseException ex) throws SAXException {
    reportMessage("[Fatal Error] "+
		  getLocationString(ex),
		  ex);
  }

  /** Returns a string of the location. */
  private String getLocationString(SAXParseException ex) {
    StringBuffer str = new StringBuffer();
    
    String systemId = ex.getSystemId();
    if (systemId != null) {
      int index = systemId.lastIndexOf('/');
      if (index != -1) 
	systemId = systemId.substring(index + 1);
      str.append(systemId);
    }
    str.append(':');
    str.append(ex.getLineNumber());
    str.append(':');
    str.append(ex.getColumnNumber());
    
    return str.toString(); 
  }

  protected void reportMessage(String m){
    System.out.println(m);
  }

  protected void reportMessage(String m, Exception e){
    reportMessage(m+": " + e);
  }

  protected void reportMessage(Exception e){
    reportMessage(e.toString());
  }
}
