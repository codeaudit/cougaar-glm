/*--------------------------------------------------------------------------
 * <copyright>
 *  
 *  Copyright 1999-2004 BBNT Solutions, LLC
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
 * --------------------------------------------------------------------------*/
package org.cougaar.glm.plugins;

import org.cougaar.util.ConfigFinder;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

public final class FileUtils {

  // Constants
  private static final int NOTFOUND = -1;
  private static Logger logger = Logging.getLogger(FileUtils.class);

  public static Vector findFields(String line, char delim) {

    Vector fields = new Vector();
    int start = 0;
    int end = 0;
    boolean DONE = false;

    while (!DONE) {
      end = line.indexOf(delim, start);
      if (end == NOTFOUND) {
        DONE = true;
        end = line.length();
      }

      String f = line.substring(start, end);

      f = f.trim();
      if ((f.length() > 0)) {
        fields.addElement(new String(f));
      }
      start = end + 1;

    }
    return fields;
    // end of findFields
  }

  public static Enumeration readFile(String fullPath) {
    Vector readLines = new Vector();
    try {
      FileInputStream fis = new FileInputStream(new File(fullPath));
      InputStreamReader isr = new InputStreamReader(fis);
      LineNumberReader reader = new LineNumberReader(isr);
      String line;
      while ((line = reader.readLine()) != null) {
        readLines.add(line);
      }
      reader.close();
    } catch (IOException ioe) {
      if (logger.isErrorEnabled()) {
        logger.error("readFile()" + " IOEXception for " + fullPath + ": " + ioe);
      }
    }
    return readLines.elements();
  }

  /**
   * @deprecated Use readConfigFile(filename,configFinder) with plugins or clusters config finder passed *
   */
  public static Enumeration readConfigFile(String filename) {
    return readConfigFile(filename, ConfigFinder.getInstance());
  }

  public static Enumeration readConfigFile(String filename, ConfigFinder finder) {
    Vector readLines = new Vector();
    try {
      InputStreamReader isr = new InputStreamReader(finder.open(filename));
      LineNumberReader lnr = new LineNumberReader(isr);
      String line;
      while ((line = lnr.readLine()) != null) {
        readLines.add(line);
      }
      isr.close();
    } catch (IOException ioe) {
      // The file may not exist in normal operation (Inventory files).
      // Print a debug message and return null so calling module can handle it
      if (logger.isErrorEnabled()) {
        logger.error("readConfigFile()" + " IOEXception for " + filename + ": " + ioe);
      }
      return null;
    }
    return readLines.elements();
  }

  public static void writeFile(String fullPath, boolean append, Enumeration data) {
    try {
      FileOutputStream fos = new FileOutputStream(fullPath, append);
      PrintWriter pw = new PrintWriter(fos, true);
      while (data.hasMoreElements()) {
        pw.println(data.nextElement());
      }
      pw.close();
    } catch (IOException ioe) {
      if (logger.isErrorEnabled()) {
        logger.error("writeFile()" + "IOExceptionfor " + fullPath + ": " + ioe);
      }
    }
  }

  public static boolean renameFile(String from, String to) {
    // This method is actually rename file
    File orig_file = new File(from);
    try {
      if (!orig_file.exists()) {
        if (logger.isErrorEnabled()) {
          logger.error("moveFile()" + "Source file does not exist. " + from);
        }
        return false;
      }
      if (!orig_file.canWrite()) {
        if (logger.isErrorEnabled()) {
          logger.error("moveFile()" + "Permissions problem: Cannot move file " + from);
        }
        return false;
      }
      File new_file = new File(to);
      orig_file.renameTo(new_file);

    } catch (SecurityException se) {
      if (logger.isErrorEnabled()) {
        logger.error("moveFile()" + "Security Exception for " + from + ": " + se);
      }
      return false;
    }
    return true;
  }

  public static boolean deleteFile(String filename) {
    File discardFile = new File(filename);
    if (discardFile.exists()) {
      discardFile.delete();
    }
    return true;
  }

  public static boolean copyFile(String from, String to) {
    Enumeration fileContents = readFile(from);
    writeFile(to, false, fileContents);
    return true;
  }

  public static boolean asciiToBinary(String bin_path, String type, String file_name) {
    // Warning: uses a linux executable.
    String fail_msg = type + " failure to convert input file: " + file_name + " because: ";
    if (!System.getProperty("os.name").equals("Linux")) {
      if (logger.isErrorEnabled()) {
        logger.error("asciiToBinary()" + fail_msg + "icisatob exec only expected to run on Linux.");
      }
      return false;
    }
    Runtime rt = Runtime.getRuntime();
    try {
      Process atob = rt.exec(bin_path + "icisatob " + type + " " + file_name);
      atob.waitFor();
    } catch (InterruptedException ie) {
      if (logger.isErrorEnabled()) {
        logger.error("asciiToBinary()" + fail_msg + ie);
      }
      return false;
    } catch (IOException ioe) {
      if (logger.isErrorEnabled()) {
        logger.error("asciiToBinary()" + fail_msg + ioe);
      }
      return false;
    }
    return true;
  }

  public static boolean binaryToAscii(String bin_path, String type, String file_name) {
    // Warning: uses a linux executable.
    String fail_msg = type + " failure to convert input file: " + file_name + " because: ";
    if (!System.getProperty("os.name").equals("Linux")) {
      if (logger.isErrorEnabled()) {
        logger.error("binaryToAscii()" + fail_msg + "icisbtoa exec only expected to run on Linux.");
      }
      return false;
    }
    Runtime rt = Runtime.getRuntime();
    try {
      Process atob = rt.exec(bin_path + "icisbtoa " + type + " " + file_name);
      atob.waitFor();
    } catch (InterruptedException ie) {
      if (logger.isErrorEnabled()) {
        logger.error("binaryToAscii()" + fail_msg + ie);
      }
      return false;
    } catch (IOException ioe) {
      if (logger.isErrorEnabled()) {
        logger.error("binaryToAscii()" + fail_msg + ioe);
      }
      return false;
    }
    return true;
  }

  public static boolean sortFile(String input_file, String output_file, String keys) {
    // Warning: uses a unix sort
    String os = System.getProperty("os.name");
    if (!(os.equals("Linux") || (os.equals("SunOS")))) {
      if (logger.isErrorEnabled()) {
        logger.error("fileSort()" + "Uses unix sort, Not a unix system.");
      }
      return false;
    }
    String command = "sort " + keys + " " + input_file + " -o " + output_file + " -T /tmp";
    try {
      Runtime rt = Runtime.getRuntime();
      Process sort = rt.exec(command);
      sort.waitFor();
    } catch (Exception ie) {
      if (logger.isErrorEnabled()) {
        logger.error("fileSort()" + "Sorting InterruptedException: " + ie);
      }
      return false;
    }
    return true;

  }
}
