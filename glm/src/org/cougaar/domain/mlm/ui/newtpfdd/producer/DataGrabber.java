
package org.cougaar.domain.mlm.ui.tpfdd.producer;

import org.cougaar.domain.mlm.ui.tpfdd.gui.view.Query;
import org.cougaar.domain.mlm.ui.tpfdd.gui.view.QueryResponse;

public interface DataGrabber {
    public QueryResponse queryDatabase(Query query); // Given a Query returns a QueryResponse
    public void cleanup(); // any necessary cleanup; may be empty
}
