/*
 * <copyright>
 * Copyright 1997-2000 Defense Advanced Research Projects Agency (DARPA)
 * and Clark Software Engineering (CSE) This software to be used in
 * accordance with the COUGAAR license agreement.  The license agreement
 * and other information on the Cognitive Agent Architecture (COUGAAR)
 * Project can be found at http://www.cougaar.org or email: info@cougaar.org.
 * </copyright>
 */
package org.cougaar.domain.mlm.ui.readiness;

import java.text.NumberFormat;

/***********************************************************************************************************************
<b>Description</b>: Utility class to facilitate compatibility between Java 1.1 and Java 1.2 and later.  Specifically
										this class replaces the Double.parseDouble(String) method available in Java 1.2 and later, but
										which is not available in Java 1.1 which most browsers currently use.

<br><br><b>Notes</b>:<br>
									- 

@author Eric B. Martin, &copy;2000 Clark Software Engineering, Ltd. & Defense Advanced Research Projects Agency (DARPA)
@version 1.0
***********************************************************************************************************************/
public class NumberUtil
{
	/*********************************************************************************************************************
  <b>Description</b>: Parses the specified string for a double number.

  <br><b>Notes</b>:<br>
	                  - 

  <br>
  @param string String to extract the double from
  @return The double extracted from the specified string
	*********************************************************************************************************************/
	public static final double parseDouble(String string)
	{
    try
    {
    	// Get the double from the start of the string
		  Number num = NumberFormat.getNumberInstance().parse(string.trim());
		  return(num.doubleValue());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return(0.0);
	}
}
