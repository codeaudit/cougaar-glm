/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.psp.society;

/**
 * Helper Class shared by PSP and Client
 **/

public class QueryChannelHelper
{
    /**
      * Parse redirects, but NOT resource path or PSP + args.
      * Note, grab string up to LAST redirect symbol: ".../$GOHERE/$THENHERE/..."
      * => ".../$GOHERE/$THENHERE/..."
      *
      * If none found, return null
     **/
    public static String parseRedirectSubstring(String urlstring) {
         int i_a = urlstring.indexOf("$");
         if( i_a < 0) return null;

         int itemp = i_a;
         while( itemp > -1 ) {
             i_a = itemp;
             itemp = urlstring.indexOf("$",itemp+1);
         }
         int i_b = urlstring.indexOf("/", i_a+1);
         if (i_b < 0) i_b = urlstring.length();
         
         String sub = urlstring.substring(i_a,i_b);
         return "/" + sub + "/";

    /**
         String redirect = null;
         if( urlstring == null) return null;

         int i=0;
         i = urlstring.indexOf("$");

         if( i>-1){
            int i2 = urlstring.indexOf("/",i);
            if( i2 < 0) i2 = urlstring.length();
            redirect = urlstring.substring(0,i2);
         }
         else return urlstring;

         //
         // Recurse to catch multiple redirects...
         String next = null;
         while( (next = parseRedirectHostURL(redirect)) != null) {
             redirect = next;
         }
         return redirect;
    **/
    }

    public static String parseURLParams(String urlstring) {
         String params = null;

         int i = urlstring.indexOf("?");
         if( i>-1){
            int i2 = urlstring.length();
            params = urlstring.substring(i,i2);
         }
         return params;
    }
}
