
package org.cougaar.domain.mlm.ui.newtpfdd.producer;

import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.Query;
import org.cougaar.domain.mlm.ui.newtpfdd.gui.view.QueryResponse;

public interface DataGrabber {
    public QueryResponse queryDatabase(Query query); // Given a Query returns a QueryResponse
    public void cleanup(); // any necessary cleanup; may be empty
}