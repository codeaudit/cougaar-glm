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

package org.cougaar.mlm.ui.alert;

import org.cougaar.mlm.ui.planviewer.ConnectionHelper;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.lang.reflect.Method;

// Does real work of processing the alerts
public abstract class AlertThread extends Thread {
    String myClusterURL;
    private boolean stop = false;
    InputStream is;
    URLConnection urlConnection;

    public AlertThread(String name, String url) {
        super(name);
        myClusterURL = url;
    }

    protected abstract void handleItem(String uid,
                                       String title,
                                       String severity,
                                       String filename,
                                       String choices);

    protected abstract void displayError(String error);

    protected String getURLSuffix() {
        return "";
    }

    public synchronized void stopListening() {
        stop = true;
        try {
            Method disconnectMethod =
                urlConnection.getClass().getMethod("disconnect", new Class[0]);
            disconnectMethod.invoke(urlConnection, new Object[0]);
            System.out.println("Disconnected");
        } catch (Exception e) {
            System.out.println(e);
        }
        interrupt();
    }

    private synchronized void checkStop() throws IOException {
        if (stop) {
            throw new IOException("Interrupted");
        }
    }

    // See PSP_Alert for the format of the output. This table must correspond.
    private static String[] scanFor = {
        PSP_Alert.ALERT_LABEL,  	// 0
        PSP_Alert.DELIM,                // 1
        PSP_Alert.UID_LABEL,            // 2
        // UID is here
        PSP_Alert.DELIM,                // 3
        PSP_Alert.TITLE_LABEL,          // 4
        // title is here
        PSP_Alert.DELIM,                // 5
        PSP_Alert.SEVERITY_LABEL,       // 6
        // severity is here
        PSP_Alert.DELIM,                // 7
        PSP_Alert.FILENAME_LABEL,       // 8
        // filename is here
        PSP_Alert.DELIM,                // 9
        PSP_Alert.CHOICES_LABEL,        // 10
        // choices are here
        PSP_Alert.DELIM,                // 11
    };
    private static final int UID = 2;
    private static final int TITLE = 4;
    private static final int SEVERITY = 6;
    private static final int FILENAME = 8;
    private static final int CHOICES = 10;
    private static final int LAST = 11;

    private static final String INTERRUPTED = "Interrupted";

    public void run() {

        try {
            ConnectionHelper connection =
                new ConnectionHelper(myClusterURL,
                                     AlertPSPConnectionInfo.current(),
                                     false /* not an applet */,
                                     getURLSuffix());
            synchronized (AlertThread.this) {
                checkStop();
                urlConnection = connection.getConnection();
                is = urlConnection.getInputStream();
                checkStop();
            }
        } catch (Exception e) {
            if (stop || e.getMessage().equals(INTERRUPTED)) {
                System.out.println(INTERRUPTED);
                return;
            }
            System.err.println("AlertThread.run() - " +
                               "exception connecting to PSP_Alert");
            e.printStackTrace();
            displayError("Unable to connect to Alert PSP at " + myClusterURL);
            return;
        }
        Parser parser = new Parser() {
            private String filename;
            private String uid;
            private String choices;
            private String title;
            private String severity;
            protected int getBytes(byte[] buffer) throws IOException {
                checkStop();
                return is.read(buffer);
            }
            protected void handleUID(String s) { uid = s; }
            protected void handleTitle(String s) { title = s; }
            protected void handleSeverity(String s) { severity = s; }
            protected void handleFile(String s) { filename = s; }
            protected void handleChoices(String s) { choices = s; }
            protected void itemFinished() {
                handleItem(uid, title, severity, filename, choices);
            }
        };
        try {
            parser.parse();
        } catch (Exception e) {
            if (stop || e.getMessage().equals(INTERRUPTED)) {
                System.out.println(INTERRUPTED);
                return;
            }
            e.printStackTrace();
            displayError("Unable to read Alert PSP output");
        }
    }
    private static abstract class Parser {
        protected abstract int getBytes(byte[] buffer) throws IOException;
        protected abstract void handleUID(String s);
        protected abstract void handleFile(String s);
        protected abstract void handleChoices(String s);
        protected abstract void handleSeverity(String s);
        protected abstract void handleTitle(String s);
        protected abstract void itemFinished();


        String s = "";                  // The current input
        int scan = 0;                   // The beginning of unparsed input in "s"
        int charsLeft = 0;              // Unscanned chars in "s"
        int state = 0;                  // The index of the next pattern
        String token = null;            // The token being accumulated
        int tokenKind = 0;              // The kind of the token
        byte byteBuffer[] = new byte[512];

        private boolean fill() throws IOException {
            int len = getBytes(byteBuffer);
            if (len ==-1) return false;   // EOF reached
            System.out.println("Read " + len + " bytes");

            // Convert the bytes to characters and append
            StringBuffer b = new StringBuffer(s.substring(scan, scan + charsLeft));
            for (int i = 0; i < len; i++) {
                b.append((char) byteBuffer[i]); // Not UTF! ASCII encoding assumed
            }
            s = b.toString();
            scan = 0;
            charsLeft = s.length();
            return true;
        }

        private void addToToken(int nchars) {
//              System.out.println(state + ": addToToken " + nchars + " chars from " + scan + " in " + s);
            token += s.substring(scan, scan + nchars);
            scan += nchars;
            charsLeft -= nchars;
        }

        private void skip(int nchars) {
            if (nchars < 0) throw new IllegalArgumentException("Negative skip: " + nchars);
//              System.out.println(state + ": skip " + nchars + " chars from " + scan + " in " + s);
            scan += nchars;
            charsLeft -= nchars;
        }

        public void parse() throws IOException {
            while (true) {
                while (charsLeft < scanFor[state].length()) {
                    if (!fill()) return; // EOF reached
                }
                int next = s.indexOf(scanFor[state], scan);
                if (next < 0) {
                    // Not present, keep the tail because it might
                    // be the start of what we want
                    int n = charsLeft - (scanFor[state].length() - 1);
                    if (token != null) {
                        addToToken(n);
                    } else {
                        skip(n);
                    }
                } else {
                    if (token != null) {
                        addToToken(next - scan);
                        switch (tokenKind) {
                        case UID:      handleUID(token);      break;
                        case FILENAME: handleFile(token);     break;
                        case CHOICES:  handleChoices(token);  break;
                        case TITLE:    handleTitle(token);    break;
                        case SEVERITY: handleSeverity(token); break;
                        }
                        token = null;
                        tokenKind = 0;
                    } else {
                        skip(next - scan);
                    }
                    skip(scanFor[state].length());
                    switch (state) {
                    case UID:
                    case FILENAME:
                    case CHOICES:
                    case TITLE:
                    case SEVERITY:
                        tokenKind = state;
                        token = "";
                        // Fall thru
                    default:
                        state++;
                        break;
                    case LAST:
                        itemFinished();
                        state = 0;      // Start over
                        break;
                    }
                }
            }
        }
    }
}
