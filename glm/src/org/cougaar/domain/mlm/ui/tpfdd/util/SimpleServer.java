/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/util/Attic/SimpleServer.java,v 1.1 2000-12-15 20:17:47 mthome Exp $ */


    //**************************************
    // Name: HTTP SERVER
    // Description:This is a Fully fledged HTTP 1.0 Server. See this Server in action at www.eej.ulst.ac.uk/remote-viewer
    // By: fiach Reid
    //
    //
    // Inputs:None
    //
    // Returns:None
    //
    //Assumes:None
    //
    //Side Effects:None
    //
    //Warranty:
    //Code provided by Planet Source Code(tm) (http://www.Planet-Source-Code.com) 'as is', without warranties as to performance, fitness, merchantability,and any other warranty (whether expressed or implied).
    //Terms of Agreement:
    //By using this source code, you agree to the following terms...
    // 1) You may use this source code in personal projects and may compile it into an .exe/.dll/.ocx and distribute it in binary format freely and with no charge.
    // 2) You MAY NOT redistribute this source code (for example to a web site) without written permission from the original author.Failure to do so is a violation of copyright laws.
    // 3) You may link to this code from another website, provided it is not wrapped in a frame.
    // 4) The author of this code may have retained certain additional copyright rights.If so, this is indicated in the author's description.
    //**************************************
    

     package org.cougaar.domain.mlm.ui.tpfdd.util;

    import java.net.*;
    import java.io.*;
    import java.util.*;
    // Contact: fiach@eircom.net
    // Site: www.eej.ulst.ac.uk/remote-viewer
    public class SimpleServer extends Thread {
    Socket theConnection;
    static File docroot;
    static String indexfile = "index.html";
    
    public SimpleServer(Socket s) {
    theConnection = s;
    }
    public static void main(String[] args) {
    int thePort;
    ServerSocket ss;
    // get the Document root
    try {
    docroot = new File(args[0]);
    }
    catch (Exception e) {
    docroot = new File(".");
    }
    
    // set the port to listen on
    try {
    thePort = Integer.parseInt(args[1]);
    if (thePort < 0 || thePort > 65535) thePort = 80;
    } 
    catch (Exception e) {
    thePort = 80;
    } 
    try {
    ss = new ServerSocket(thePort);
    System.out.println("Accepting connections on port " 
    + ss.getLocalPort());
    System.out.println("Document Root:" + docroot);
    while (true) {
    SimpleServer j = new SimpleServer(ss.accept());
    j.start();
    }
    }
    catch (IOException e) {
    System.err.println("Server aborted prematurely");
    }
    
    }
    public void run() {
    
    String method;
    String ct;
    String version = "";
    File theFile;
    
    try {
    PrintStream os = new PrintStream(theConnection.getOutputStream());
    DataInputStream is = new DataInputStream(theConnection.getInputStream());
    String get = is.readLine();
    StringTokenizer st = new StringTokenizer(get);
    method = st.nextToken();
    if (method.equals("GET")) {
    String file = st.nextToken();
    if (file.endsWith("/")) file += indexfile;
    ct = guessContentTypeFromName(file);
    if (st.hasMoreTokens()) {
    version = st.nextToken();
    }
    // loop through the rest of the input lines 
    while ((get = is.readLine()) != null) {
    if (get.trim().equals("")) break;
    }
    try {
    theFile = new File(docroot, file.substring(1,file.length()));
    FileInputStream fis = new FileInputStream(theFile);
    byte[] theData = new byte[(int) theFile.length()];
    // need to check the number of bytes read here
    fis.read(theData);
    fis.close();
    if (version.startsWith("HTTP/")) { // send a MIME header
    os.print("HTTP/1.0 200 OK\r\n");
    Date now = new Date();
    os.print("Date: " + now + "\r\n");
    os.print("Server: SimpleServer 1.0\r\n");
    os.print("Content-length: " + theData.length + "\r\n");
    os.print("Content-type: " + ct + "\r\n\r\n");
    } // end try
    
    // send the file
    os.write(theData);
    os.close();
    } // end try
    catch (IOException e) { // can't find the file
    if (version.startsWith("HTTP/")) { // send a MIME header
    os.print("HTTP/1.0 404 File Not Found\r\n");
    Date now = new Date();
    os.print("Date: " + now + "\r\n");
    os.print("Server: SimpleServer 1.0\r\n");
    os.print("Content-type: text/html" + "\r\n\r\n");
    } 
    os.println("<HTML><HEAD><TITLE>File Not Found</TITLE></HEAD>");
    os.println("<BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>");
    os.close();
    }
    }
    else { // method does not equal "GET"
    if (version.startsWith("HTTP/")) { // send a MIME header
    os.print("HTTP/1.0 501 Not Implemented\r\n");
    Date now = new Date();
    os.print("Date: " + now + "\r\n");
    os.print("Server: SimpleServer 1.0\r\n");
    os.print("Content-type: text/html" + "\r\n\r\n"); 
    }
    os.println("<HTML><HEAD><TITLE>Not Implemented</TITLE></HEAD>");
    os.println("<BODY><H1>HTTP Error 501: Not Implemented</H1></BODY></HTML>");
    os.close();
    }
    }
    catch (IOException e) {
    
    }
    try {
    theConnection.close();
    }
    catch (IOException e) {
    }
    }
    
    public String guessContentTypeFromName(String name) {
    if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html";
    else if (name.endsWith(".txt") || name.endsWith(".java")) return "text/plain";
    else if (name.endsWith(".gif") ) return "image/gif";
    else if (name.endsWith(".class") ) return "application/octet-stream";
    else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
    else return "text/plain";
    }
    }

