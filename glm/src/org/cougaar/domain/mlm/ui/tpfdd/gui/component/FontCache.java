/* $Header: /opt/rep/cougaar/glm/glm/src/org/cougaar/domain/mlm/ui/tpfdd/gui/component/Attic/FontCache.java,v 1.1 2000-12-15 20:17:48 mthome Exp $ */

/*
  Copyright (C) 1999-2000 Ascent Technology Inc. (Program).  All rights
  Reserved.
  
  This material has been developed pursuant to the BBN/RTI "ALPINE"
  Joint Venture contract number MDA972-97-C-0800, by Ascent Technology,
  Inc. 64 Sidney Street, Suite 380, Cambridge, MA 02139.

  @author Daniel Bromberg
*/

package org.cougaar.domain.mlm.ui.tpfdd.gui.component;


import java.awt.Font;


public class FontCache
{
    private static Font[] fonts;

    static {
	fonts = new Font[2048];
	for ( int i = 0; i <= 14; i++ )
	    fonts[i] = new Font("SansSerif", Font.PLAIN, 8);
	for ( int i = 15; i <= 19; i++ )
	    fonts[i] = new Font("SansSerif", Font.PLAIN, 9);
	for ( int i = 20; i <= 128; i++ )
	    fonts[i] = new Font("SansSerif", Font.PLAIN, (int)(Math.round(Math.pow(i, 0.75)) + 1));
	Font font36 = new Font("SansSerif", Font.PLAIN, 36);
	for ( int i = 128; i < 2048; i++ )
	    fonts[i] = font36;
    }

    public static Font get(int i)
    {
	return fonts[i];
    }
}
