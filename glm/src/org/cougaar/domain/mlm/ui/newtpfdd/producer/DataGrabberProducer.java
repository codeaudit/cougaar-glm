package org.cougaar.domain.mlm.ui.newtpfdd.producer;

public class DataGrabberProducer extends ThreadedProducer {

    DataGrabber dataGrabber;

    public DataGrabberProducer(PlanElementProvider provider, ???) {
	super(???);
	dataGrabber = new ???(provider, ???jdbc stuff???);
    }

    public Object[] produce(Object query) {
	if (!query instanceof QueryData) {
	    System.err.println("ALERT: Non-Query sent to DataGrabberProducer");
	    System.err.println(quert+" being sent.");
	    return null;
	} else {
	    Query modifiedQuery = manipulateQuery((QueryData)query);
	    QueryResponse = dataGrabber.queryDatabase((Query)modifiedQuery);
	    
	    // You could put code here that handles Out-of-band info in QueryResponse

	    return QueryResponse.getNodes();
	}	
    }

    public void cleanup() {
	dataGrabber.cleanup();
    }

    private Query manipulateQuery(QueryData query) {
	???
    }
}
